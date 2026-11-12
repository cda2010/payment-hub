package com.gateway.payment.omnicanal.external;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.orchestration.api.dto.CompteFseResponse;
import com.gateway.payment.orchestration.service.OmnicanalCompteSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
public class ExternalOmnicanalCompteSource implements OmnicanalCompteSource {

    private final ExternalOmnicanalClient client;
    private final ExternalOmnicanalProperties properties;

    public ExternalOmnicanalCompteSource(
            ExternalOmnicanalClient client,
            ExternalOmnicanalProperties properties
    ) {
        this.client = client;
        this.properties = properties;
    }

    @Override
    public boolean enabled() {
        return properties.isEnabled();
    }

    @Override
    public List<CompteFseResponse> list() {
        List<CompteFseResponse> out = new ArrayList<>();
        for (Map<String, Object> row : client.comptes()) {
            out.add(toCompte(row));
        }
        return out;
    }

    @Override
    public Optional<CompteFseResponse> get(UUID id) {
        return list().stream().filter(compte -> id.equals(compte.id())).findFirst();
    }

    private CompteFseResponse toCompte(Map<String, Object> row) {
        long id = number(row.get("id"));
        Long mirror = numberOrNull(row.get("mirroirId"));
        Long relation = numberOrNull(row.get("relationId"));
        return new CompteFseResponse(
                stableId("omni-compte-", id),
                id,
                text(row.get("numeroCompte")),
                text(row.get("codeBanque")),
                text(row.get("codeAgence")),
                text(row.get("cle")),
                text(row.get("extype")),
                text(row.get("typeCompteCode")),
                text(row.get("typeCompteLibelle")),
                text(row.get("devise")),
                text(row.get("fseTeneur")),
                text(row.get("fseTitulaire")),
                serviceType(text(row.get("serviceCode")), text(row.get("serviceLibelle"))),
                text(row.get("libelleCompte")),
                text(row.get("codeUoTitulaire")),
                mirror == null ? null : stableId("omni-compte-", mirror),
                text(row.get("agenceCode")),
                relation == null ? null : stableId("omni-rel-", relation),
                text(row.get("fseSource")),
                text(row.get("fseDestination")),
                serviceType(text(row.get("relationService")), text(row.get("relationServiceLibelle"))),
                Instant.EPOCH
        );
    }

    private static ServiceTypeCode serviceType(String code, String libelle) {
        ServiceTypeCode fromCode = serviceType(code);
        if (fromCode != null) {
            return fromCode;
        }
        return serviceType(libelle);
    }

    private static ServiceTypeCode serviceType(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String normalized = raw.trim().toUpperCase().replace(' ', '_').replace('-', '_').replace("'", "");
        return switch (normalized) {
            case "10", "TRANSFERTARGENT", "TRANSFERT_DARGENT" -> ServiceTypeCode.TRANSFERT;
            case "16", "PAIEMENTFACTURE", "PAIEMENT_DE_FACTURE" -> ServiceTypeCode.PAIEMENT_FACTURE;
            case "17", "CONSULTATION" -> ServiceTypeCode.CONSULTATION_SOLDE;
            default -> ServiceTypeCode.fromString(raw);
        };
    }

    private static UUID stableId(String prefix, long id) {
        return UUID.nameUUIDFromBytes((prefix + id).getBytes(StandardCharsets.UTF_8));
    }

    private static long number(Object raw) {
        if (raw instanceof Number number) {
            return number.longValue();
        }
        return 0L;
    }

    private static Long numberOrNull(Object raw) {
        if (raw instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private static String text(Object raw) {
        if (raw == null) {
            return null;
        }
        String value = String.valueOf(raw).trim();
        return value.isEmpty() || "null".equals(value) ? null : value;
    }
}
