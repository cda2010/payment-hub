package io.github.jhipster.web.filter;

import io.github.jhipster.config.JHipsterProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CachingHttpHeadersFilter extends OncePerRequestFilter {

    private final long cacheTimeToLive = TimeUnit.DAYS.toMillis(1461);

    public CachingHttpHeadersFilter(JHipsterProperties jHipsterProperties) {
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "max-age=" + cacheTimeToLive / 1000 + ", public");
        response.setHeader("Pragma", "cache");
        response.setDateHeader("Expires", System.currentTimeMillis() + cacheTimeToLive);
        filterChain.doFilter(request, response);
    }
}
