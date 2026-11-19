package com.gateway.payment.edge;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;

/**
 * Refuse un appel d'API dont le jeton est absent ou mal signé.
 * Les chemins publics passent sans jeton. Le rôle n'est pas lu.
 */
@Component
public class JetonSignatureFilter implements GlobalFilter, Ordered {

    private static final byte[] REFUS = """
            {"status":401,"error":"UNAUTHORIZED","message":"Jeton absent ou signature invalide"}
            """.trim().getBytes(StandardCharsets.UTF_8);

    private final JetonVerifier verifier;

    public JetonSignatureFilter(JetonVerifier verifier) {
        this.verifier = verifier;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (path == null || !path.startsWith("/api/")) {
            return chain.filter(exchange);
        }
        String method = exchange.getRequest().getMethod() == null
                ? ""
                : exchange.getRequest().getMethod().name();
        String token = bearer(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        if (token == null && CheminsPublics.ouvert(method, path)) {
            return chain.filter(exchange);
        }
        if (token == null) {
            return refus(exchange);
        }
        return Mono.fromCallable(() -> verifier.signatureValide(token))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(valide -> Boolean.TRUE.equals(valide) ? chain.filter(exchange) : refus(exchange));
    }

    private static String bearer(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        String token = header.substring(7).trim();
        return token.isEmpty() ? null : token;
    }

    private static Mono<Void> refus(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(REFUS);
        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
