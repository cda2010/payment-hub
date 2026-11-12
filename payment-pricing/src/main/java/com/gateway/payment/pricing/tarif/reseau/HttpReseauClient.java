package com.gateway.payment.pricing.tarif.reseau;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client HTTP de payment-network. Le token de l'appel en cours est relayé (les deux services acceptent les mêmes
 * tokens). Les positions sont gardées en cache quelques instants : une grille se calcule souvent pour les mêmes
 * unités, et le réseau change rarement.
 */
@Slf4j
@Component
public class HttpReseauClient implements ReseauClient {

    private final RestClient http;
    private final Duration ttl;
    private final Clock clock;
    private final Map<String, Entree> cache = new ConcurrentHashMap<>();

    public HttpReseauClient(RestClient.Builder builder, Clock clock,
                            @Value("${pricing.reseau.url:http://localhost:8096}") String url,
                            @Value("${pricing.reseau.cache-ttl:PT1M}") Duration ttl,
                            @Value("${pricing.reseau.timeout:PT3S}") Duration timeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        this.http = builder.baseUrl(url).requestFactory(factory).build();
        this.ttl = ttl;
        this.clock = clock;
    }

    @Override
    public PositionReseau unite(UUID id) {
        return lire("unite", id.toString());
    }

    @Override
    public PositionReseau pays(String code) {
        return lire("pays", code);
    }

    @Override
    public PositionReseau zone(String code) {
        return lire("zone", code);
    }

    private PositionReseau lire(String critere, String valeur) {
        String cle = critere + "=" + valeur;
        Instant maintenant = clock.instant();
        Entree entree = cache.get(cle);
        if (entree != null && entree.expire().isAfter(maintenant)) {
            return entree.position();
        }
        PositionReseau position = appeler(critere, valeur);
        cache.put(cle, new Entree(position, maintenant.plus(ttl)));
        return position;
    }

    private PositionReseau appeler(String critere, String valeur) {
        try {
            return http.get()
                    .uri(u -> u.path("/api/v1/network/positions").queryParam(critere, valeur).build())
                    .headers(h -> {
                        String token = tokenCourant();
                        if (token != null) {
                            h.set(HttpHeaders.AUTHORIZATION, token);
                        }
                    })
                    .retrieve()
                    .body(PositionReseau.class);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                throw new ReseauException(true, message(e));
            }
            log.warn("payment-network a répondu {} pour la position {}={}", e.getStatusCode().value(), critere, valeur);
            throw new ReseauException(false, "Le réseau de distribution a refusé la lecture (" + e.getStatusCode().value() + ").");
        } catch (ResourceAccessException e) {
            log.warn("payment-network injoignable : {}", e.getMessage());
            throw new ReseauException(false, "Le réseau de distribution est injoignable.");
        }
    }

    private static String message(HttpStatusCodeException e) {
        try {
            Map<?, ?> corps = e.getResponseBodyAs(Map.class);
            if (corps != null && corps.get("message") instanceof String m) {
                return m;
            }
        } catch (RuntimeException ignored) {
            // corps illisible : message générique
        }
        return "Élément introuvable dans le réseau de distribution.";
    }

    private static String tokenCourant() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }

    private record Entree(PositionReseau position, Instant expire) {
    }
}
