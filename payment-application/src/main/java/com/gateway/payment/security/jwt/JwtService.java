package com.gateway.payment.security.jwt;

import com.gateway.payment.security.config.SecurityProperties;
import com.gateway.payment.security.domain.GatewayUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    /** Défaut aligné sur application.yml — utilisé si JWT_SECRET est vide ou trop court (NetBeans / env). */
    static final String DEFAULT_SECRET =
            "change-me-to-a-very-long-secret-key-at-least-256-bits";

    private final SecurityProperties properties;
    private final SecretKey key;

    public JwtService(SecurityProperties properties) {
        this.properties = properties;
        String configured = properties.jwt() != null ? properties.jwt().secret() : null;
        // Spring remplace ${JWT_SECRET:défaut} par "" si la variable existe mais est vide
        // (cas fréquent sous NetBeans) → WeakKeyException 0 bits.
        String secret = (configured == null || configured.isBlank()) ? DEFAULT_SECRET : configured;
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            log.warn(
                    "gateway.security.jwt.secret trop court ({} octets, min 32) — secret local par défaut utilisé",
                    bytes.length);
            secret = DEFAULT_SECRET;
            bytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        this.key = Keys.hmacShaKeyFor(bytes);
    }

    public String createAccessToken(GatewayUser user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(properties.jwt().accessTokenTtl());
        List<String> roles = user.getRoles().stream().map(r -> r.getCode()).toList();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(properties.jwt().issuer())
                .subject(user.getUsername())
                .claim("uid", user.getId().toString())
                .claim("roles", roles)
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(GatewayUser user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(properties.jwt().refreshTokenTtl());

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .issuer(properties.jwt().issuer())
                .subject(user.getUsername())
                .claim("uid", user.getId().toString())
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(properties.jwt().issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isAccessToken(Claims claims) {
        return "access".equals(claims.get("type", String.class));
    }

    public boolean isRefreshToken(Claims claims) {
        return "refresh".equals(claims.get("type", String.class));
    }
}
