package com.gateway.payment.security.jwt;

import com.gateway.payment.security.service.GatewayUserDetailsService;
import com.nimbusds.jwt.JWTParser;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Accepte deux émetteurs : Keycloak (JWKS) et les tokens maison {@code /api/v1/auth/login} (HMAC),
 * ces derniers restant nécessaires aux clients non migrés. Un token invalide laisse la requête
 * anonyme : les routes publiques restent accessibles, les autres répondent 401.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final GatewayUserDetailsService userDetailsService;
    private final KeycloakTokenVerifier keycloakTokenVerifier;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            GatewayUserDetailsService userDetailsService,
            KeycloakTokenVerifier keycloakTokenVerifier
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.keycloakTokenVerifier = keycloakTokenVerifier;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")
                || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            UserDetails userDetails = authenticate(token);
            if (userDetails != null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ignored) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private UserDetails authenticate(String token) throws Exception {
        String issuer = JWTParser.parse(token).getJWTClaimsSet().getIssuer();
        if (keycloakTokenVerifier.handles(issuer)) {
            return keycloakTokenVerifier.verify(token);
        }
        Claims claims = jwtService.parse(token);
        if (!jwtService.isAccessToken(claims)) {
            return null;
        }
        return userDetailsService.loadUserByUsername(claims.getSubject());
    }
}
