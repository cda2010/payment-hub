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

import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Client HTTP payment-identity — party (KYC / statut) pour EVALUATE_RULES.
 */
@Component
public class IdentityBcClient {

    private static final Logger log = LoggerFactory.getLogger(IdentityBcClient.class);

    private final RestClient http;
    private final GatewayBcProperties properties;

    public IdentityBcClient(RestClient.Builder builder, GatewayBcProperties properties) {
        this.properties = properties;
        Duration timeout = Duration.parse(properties.identity().timeout());
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        this.http = builder.baseUrl(properties.identity().url()).requestFactory(factory).build();
    }

    public boolean enabled() {
        return properties.identityEnabled();
    }

    /**
     * Politique d'organisation pour un lancement corporate.
     * Vide si Identity n'a pas de règle prioritaire, ou s'il est injoignable : le produit décide alors.
     */
    public Optional<OrgApproval> resolveCorporateApproval(
            UUID orgNodeId,
            String productCode,
            BigDecimal amount,
            Integer lineCount,
            Long maxLineAmount
    ) {
        if (!enabled() || orgNodeId == null || !StringUtils.hasText(productCode)) {
            return Optional.empty();
        }
        long amountValue = amount == null ? 0L : amount.longValue();
        try {
            Map<?, ?> body = http.get()
                    .uri(uri -> {
                        var b = uri.path("/api/v1/identity/org-nodes/{id}/approval-resolution")
                                .queryParam("productCode", productCode.trim())
                                .queryParam("amount", amountValue);
                        if (lineCount != null) {
                            b.queryParam("lineCount", lineCount);
                        }
                        if (maxLineAmount != null) {
                            b.queryParam("maxLineAmount", maxLineAmount);
                        }
                        return b.build(orgNodeId);
                    })
                    .headers(this::auth)
                    .retrieve()
                    .body(Map.class);
            return Optional.ofNullable(parseApproval(body));
        } catch (HttpStatusCodeException e) {
            log.warn("payment-identity résolution approbation {} → {}", orgNodeId, e.getStatusCode().value());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("payment-identity résolution approbation injoignable: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<PartyProfile> getParty(UUID partyId) {
        if (!enabled() || partyId == null) {
            return Optional.empty();
        }
        try {
            Map<?, ?> body = http.get()
                    .uri("/api/v1/identity/parties/{id}", partyId)
                    .headers(this::auth)
                    .retrieve()
                    .body(Map.class);
            return Optional.ofNullable(parse(body));
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return Optional.empty();
            }
            log.warn("payment-identity get party {} → {}", partyId, e.getStatusCode().value());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("payment-identity injoignable: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private void auth(HttpHeaders headers) {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            String token = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (token != null && !token.isBlank()) {
                headers.set(HttpHeaders.AUTHORIZATION, token);
            }
        }
    }

    private static PartyProfile parse(Map<?, ?> m) {
        if (m == null) {
            return null;
        }
        UUID id = null;
        Object rawId = m.get("id");
        if (rawId != null) {
            try {
                id = UUID.fromString(String.valueOf(rawId));
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
        boolean active = !(m.get("active") instanceof Boolean b) || b;
        String status = str(m.get("customerStatus"));
        if (!StringUtils.hasText(status)) {
            status = active ? "ACTIVE" : "INACTIVE";
        }
        String kyc = str(m.get("kycLevel"));
        if (!StringUtils.hasText(kyc)) {
            kyc = "STANDARD";
        }
        return new PartyProfile(id, str(m.get("tenantId")), kyc, status, active);
    }

    private static String str(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static OrgApproval parseApproval(Map<?, ?> body) {
        if (body == null || !Boolean.TRUE.equals(body.get("applied"))) {
            return null;
        }
        String profile = str(body.get("profileCode"));
        if (!StringUtils.hasText(profile)) {
            return null;
        }
        Map<Integer, String> roles = new LinkedHashMap<>();
        Object rawLevels = body.get("levels");
        if (rawLevels instanceof List<?> list) {
            for (Object item : list) {
                if (!(item instanceof Map<?, ?> level)) {
                    continue;
                }
                int number = level.get("level") instanceof Number n ? n.intValue() : 0;
                String role = str(level.get("role"));
                if (number > 0 && StringUtils.hasText(role)) {
                    roles.put(number, role);
                }
            }
        }
        return new OrgApproval(profile, str(body.get("orgCode")), str(body.get("ruleCode")), Map.copyOf(roles));
    }

    public record OrgApproval(
            String profileCode,
            String orgCode,
            String ruleCode,
            Map<Integer, String> rolesByLevel
    ) {
        public boolean requiresApproval() {
            return profileCode != null && !"NO_APPROVAL".equalsIgnoreCase(profileCode);
        }
    }

    public record PartyProfile(
            UUID id,
            String tenantId,
            String kycLevel,
            String customerStatus,
            boolean active
    ) {
    }
}
