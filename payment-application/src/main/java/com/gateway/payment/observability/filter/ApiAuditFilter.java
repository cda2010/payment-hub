package com.gateway.payment.observability.filter;

import com.gateway.payment.observability.metrics.GatewayMetrics;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

/**
 * Audit API sur {@code /api/v1/**} : acteur, route, statut, duree + metriques Micrometer.
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 20)
public class ApiAuditFilter extends OncePerRequestFilter {

    private static final Logger AUDIT = LoggerFactory.getLogger("API_AUDIT");
    private static final String V1_PREFIX = "/api/v1/";

    private final GatewayMetrics gatewayMetrics;

    public ApiAuditFilter(GatewayMetrics gatewayMetrics) {
        this.gatewayMetrics = gatewayMetrics;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String relative = relativePath(request);
        return !relative.startsWith(V1_PREFIX) && !relative.equals("/api/v1");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long start = System.nanoTime();
        ContentCachingResponseWrapper wrapped = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(request, wrapped);
        } finally {
            long durationMs = (System.nanoTime() - start) / 1_000_000L;
            String channel = resolveChannel();
            String route = normalizeRoute(request);
            int status = wrapped.getStatus();
            AUDIT.info(
                    "channel={} method={} path={} route={} status={} durationMs={} traceId={}",
                    channel,
                    request.getMethod(),
                    request.getRequestURI(),
                    route,
                    status,
                    durationMs,
                    MDC.get("traceId")
            );
            gatewayMetrics.recordChannelRequest(channel, route, status, durationMs);
            wrapped.copyBodyToResponse();
        }
    }

    private static String resolveChannel() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() != null) {
            return String.valueOf(auth.getPrincipal());
        }
        return "anonymous";
    }

    private static String normalizeRoute(HttpServletRequest request) {
        String relative = relativePath(request);
        int q = relative.indexOf('?');
        return q >= 0 ? relative.substring(0, q) : relative;
    }

    private static String relativePath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String context = request.getContextPath() != null ? request.getContextPath() : "";
        return path.startsWith(context) ? path.substring(context.length()) : path;
    }
}
