package com.gateway.payment.bc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Client HTTP payment-alias — comptes rattachés à une UO ({@code ownerKind=UNITE}).
 */
@Component
public class AliasBcClient {

    private static final Logger log = LoggerFactory.getLogger(AliasBcClient.class);

    private final RestClient http;
    private final GatewayBcProperties properties;

    public AliasBcClient(RestClient.Builder builder, GatewayBcProperties properties) {
        this.properties = properties;
        Duration timeout = Duration.parse(properties.alias().timeout());
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        this.http = builder.baseUrl(properties.alias().url()).requestFactory(factory).build();
    }

    public boolean enabled() {
        return properties.aliasEnabled();
    }

    /**
     * Alias actifs d'une unité (compte agent / till).
     */
    @SuppressWarnings("unchecked")
    public List<AliasAccount> listForUnite(UUID uniteId, String tenantId) {
        if (!enabled() || uniteId == null) {
            return List.of();
        }
        try {
            List<?> body = http.get()
                    .uri(u -> {
                        var b = u.path("/api/v1/aliases")
                                .queryParam("ownerKind", "UNITE")
                                .queryParam("ownerId", uniteId.toString());
                        if (StringUtils.hasText(tenantId)) {
                            b = b.queryParam("tenantId", tenantId.trim());
                        }
                        return b.build();
                    })
                    .headers(this::auth)
                    .retrieve()
                    .body(List.class);
            if (body == null || body.isEmpty()) {
                return List.of();
            }
            List<AliasAccount> out = new ArrayList<>();
            for (Object row : body) {
                AliasAccount a = parse(row);
                if (a != null && a.active()) {
                    out.add(a);
                }
            }
            return out;
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return List.of();
            }
            log.warn("payment-alias list UNITE={} → {}", uniteId, e.getStatusCode().value());
            throw new IllegalStateException("payment-alias a refusé la lecture (" + e.getStatusCode().value() + ")");
        } catch (Exception e) {
            log.warn("payment-alias injoignable: {}", e.getMessage());
            throw new IllegalStateException("payment-alias injoignable: " + e.getMessage(), e);
        }
    }

    /**
     * Choisit le compte destination agent : préfère {@code providerCode}, puis WALLET/TILL.
     */
    public Optional<AliasAccount> resolveUniteAccount(UUID uniteId, String tenantId, String preferredProvider) {
        List<AliasAccount> all = listForUnite(uniteId, tenantId);
        if (all.isEmpty()) {
            return Optional.empty();
        }
        String provider = preferredProvider != null ? preferredProvider.trim().toUpperCase(Locale.ROOT) : "";
        Optional<AliasAccount> byProvider = all.stream()
                .filter(a -> StringUtils.hasText(a.providerCode())
                        && a.providerCode().equalsIgnoreCase(provider))
                .findFirst();
        if (byProvider.isPresent()) {
            return byProvider;
        }
        return all.stream()
                .filter(a -> "WALLET".equalsIgnoreCase(a.aliasType()) || "TILL".equalsIgnoreCase(a.aliasType()))
                .findFirst()
                .or(() -> Optional.of(all.getFirst()));
    }

    @SuppressWarnings("unchecked")
    private static AliasAccount parse(Object o) {
        if (!(o instanceof Map<?, ?> m)) {
            return null;
        }
        String externalRef = str(m.get("externalRef"));
        if (!StringUtils.hasText(externalRef)) {
            return null;
        }
        UUID ownerId = null;
        Object rawOwner = m.get("ownerId");
        if (rawOwner != null) {
            try {
                ownerId = UUID.fromString(String.valueOf(rawOwner));
            } catch (IllegalArgumentException ignored) {
                // ignore
            }
        }
        boolean active = !(m.get("active") instanceof Boolean b) || b;
        return new AliasAccount(
                str(m.get("aliasCode")),
                str(m.get("label")),
                str(m.get("aliasType")),
                externalRef.trim(),
                str(m.get("currency")),
                str(m.get("providerCode")),
                ownerId,
                active
        );
    }

    private void auth(HttpHeaders headers) {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            String token = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (token != null && !token.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, token);
            }
        }
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    public record AliasAccount(
            String aliasCode,
            String label,
            String aliasType,
            String externalRef,
            String currency,
            String providerCode,
            UUID ownerId,
            boolean active
    ) {
    }
}
