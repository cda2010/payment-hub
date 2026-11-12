package com.gateway.payment.omnicanal.canal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authentification canal par {@code X-Api-Key} (miroir Omnicanal channel-api).
 * N'affecte pas le JWT admin / checkout.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class CanalApiKeyFilter extends OncePerRequestFilter {

    @Value("${gateway.canal.api-key:change-me-mobile}")
    private String configuredApiKey;

    @Value("${gateway.canal.enabled:true}")
    private boolean enabled;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!enabled) {
            return true;
        }
        String path = request.getRequestURI();
        return !(path.startsWith("/api/v1/boundary")
                || path.startsWith("/api/boundary")
                || path.startsWith("/api/v1/omnicanalPresentation")
                || path.startsWith("/api/omnicanalPresentation")
                || path.startsWith("/api/v1/canal/"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        String key = request.getHeader("X-Api-Key");
        if (!StringUtils.hasText(key) || !key.equals(configuredApiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"codeErr\":\"401\",\"msg\":\"X-Api-Key invalide\"}");
            return;
        }
        var auth = new UsernamePasswordAuthenticationToken(
                "canal-api",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_CANAL"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }
}
