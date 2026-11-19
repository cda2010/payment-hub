package com.gateway.payment.edge;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

import java.util.concurrent.TimeUnit;

/**
 * Compte chaque passage par route : volume, issue, durée.
 */
@Component
public class EdgeMetricsFilter implements GlobalFilter, Ordered {

    private final MeterRegistry registry;

    public EdgeMetricsFilter(MeterRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.nanoTime();
        return chain.filter(exchange).doFinally(signal -> record(exchange, start, signal));
    }

    private void record(ServerWebExchange exchange, long start, SignalType signal) {
        long durationMs = Math.max(0L, (System.nanoTime() - start) / 1_000_000L);
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        String routeId = route != null && StringUtils.hasText(route.getId()) ? route.getId() : "none";
        int status = exchange.getResponse().getStatusCode() != null
                ? exchange.getResponse().getStatusCode().value()
                : 0;
        String outcome = EdgeMetrics.outcome(status, signal == SignalType.ON_ERROR, signal == SignalType.CANCEL);
        registry.counter(
                EdgeMetrics.REQUESTS,
                "route", routeId,
                "outcome", outcome,
                "status", Integer.toString(status)
        ).increment();
        registry.timer(EdgeMetrics.LATENCY, "route", routeId, "outcome", outcome)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
