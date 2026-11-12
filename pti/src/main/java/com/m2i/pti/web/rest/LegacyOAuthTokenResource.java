package com.m2i.pti.web.rest;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ancien point {@code /oauth/token} attendu par le client AngularJS (password grant).
 */
@RestController
public class LegacyOAuthTokenResource {

    private static final String CLIENT_ID = "ptiapp";
    private static final String CLIENT_SECRET = "my-secret-token-to-change-in-production";
    private static final long ACCESS_TTL = 1800;
    private static final long REFRESH_TTL = 604800;

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public LegacyOAuthTokenResource(@Qualifier("userDetailsService") UserDetailsService userDetailsService,
                                    PasswordEncoder passwordEncoder,
                                    JwtEncoder jwtEncoder,
                                    JwtDecoder jwtDecoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
    }

    @PostMapping(value = "/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> token(@RequestParam("grant_type") String grantType,
                                                     @RequestParam(value = "username", required = false) String username,
                                                     @RequestParam(value = "password", required = false) String password,
                                                     @RequestParam(value = "refresh_token", required = false) String refreshToken,
                                                     @RequestParam(value = "client_id", required = false) String clientId,
                                                     @RequestParam(value = "client_secret", required = false) String clientSecret,
                                                     @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (!clientAccepted(clientId, clientSecret, authorization)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error("invalid_client"));
        }
        try {
            UserDetails user = switch (grantType) {
                case "password" -> authenticate(username, password);
                case "refresh_token" -> refresh(refreshToken);
                case "client_credentials" -> serviceClient();
                default -> null;
            };
            if (user == null) {
                return ResponseEntity.badRequest().body(error("unsupported_grant_type"));
            }
            return ResponseEntity.ok(tokens(user));
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error("invalid_grant"));
        }
    }

    private UserDetails serviceClient() {
        return org.springframework.security.core.userdetails.User
                .withUsername(CLIENT_ID)
                .password("")
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                .build();
    }

    private UserDetails authenticate(String username, String password) {
        if (username == null || password == null) {
            throw new IllegalArgumentException("missing credentials");
        }
        UserDetails user = userDetailsService.loadUserByUsername(username);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("bad password");
        }
        return user;
    }

    private UserDetails refresh(String refreshToken) {
        if (refreshToken == null) {
            throw new IllegalArgumentException("missing refresh token");
        }
        Jwt jwt = jwtDecoder.decode(refreshToken);
        if (!"refresh".equals(jwt.getClaimAsString("token_use"))) {
            throw new IllegalArgumentException("not a refresh token");
        }
        return userDetailsService.loadUserByUsername(jwt.getSubject());
    }

    private Map<String, Object> tokens(UserDetails user) {
        List<String> roles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("access_token", encode(user.getUsername(), roles, "access", ACCESS_TTL));
        body.put("refresh_token", encode(user.getUsername(), roles, "refresh", REFRESH_TTL));
        body.put("token_type", "bearer");
        body.put("expires_in", ACCESS_TTL);
        body.put("scope", "read write");
        return body;
    }

    private String encode(String subject, List<String> roles, String use, long ttl) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(com.m2i.pti.security.PtiJwtConfiguration.ISSUER)
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ttl))
                .claim("roles", roles)
                .claim("token_use", use)
                .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    private static boolean clientAccepted(String clientId, String clientSecret, String authorization) {
        if (CLIENT_ID.equals(clientId) && CLIENT_SECRET.equals(clientSecret)) {
            return true;
        }
        if (authorization == null || !authorization.regionMatches(true, 0, "Basic ", 0, 6)) {
            return false;
        }
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(authorization.substring(6).trim()), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return false;
        }
        int split = decoded.indexOf(':');
        if (split < 0) {
            return false;
        }
        return CLIENT_ID.equals(decoded.substring(0, split)) && CLIENT_SECRET.equals(decoded.substring(split + 1));
    }

    private static Map<String, Object> error(String code) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", code);
        return body;
    }
}
