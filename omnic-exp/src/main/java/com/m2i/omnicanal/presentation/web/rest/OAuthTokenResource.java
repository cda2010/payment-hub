package com.m2i.omnicanal.presentation.web.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Password grant expected by the existing AngularJS login screen.
 */
@RestController
public class OAuthTokenResource {

    private static final long EXPIRES_IN = 1800;

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;

    public OAuthTokenResource(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
    }

    @PostMapping(value = "/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Map<String, Object>> token(@RequestParam String username, @RequestParam String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
            Instant now = Instant.now();
            JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(EXPIRES_IN))
                .claim("roles", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .build();
            String accessToken = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("access_token", accessToken);
            body.put("token_type", "bearer");
            body.put("expires_in", EXPIRES_IN);
            body.put("scope", "read write");
            return ResponseEntity.ok(body);
        } catch (AuthenticationException ex) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("error", "invalid_grant");
            body.put("error_description", "Bad credentials");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
    }
}
