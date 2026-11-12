package com.gateway.payment.omnicanal.external;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.orchestration.api.dto.LigneSchemaEcritureResponse;
import com.gateway.payment.orchestration.api.dto.ParametreSchemaResponse;
import com.gateway.payment.orchestration.api.dto.SchemaEcritureResponse;
import com.gateway.payment.orchestration.domain.ImputeAgence;
import com.gateway.payment.orchestration.domain.ParametreCategorie;
import com.gateway.payment.orchestration.domain.ProprieteApi;
import com.gateway.payment.orchestration.service.OmnicanalSchemaSource;
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
public class ExternalOmnicanalSchemaSource implements OmnicanalSchemaSource {

    private final ExternalOmnicanalClient client;
    private final ExternalOmnicanalProperties properties;

    public ExternalOmnicanalSchemaSource(
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
    public List<SchemaEcritureResponse> list(ServiceTypeCode serviceType) {
        return load().stream()
                .filter(schema -> serviceType == null || serviceType == schema.serviceType())
                .toList();
    }

    @Override
    public Optional<SchemaEcritureResponse> get(UUID id) {
        return load().stream().filter(schema -> id.equals(schema.id())).findFirst();
    }

    @Override
    public Optional<List<LigneSchemaEcritureResponse>> lignes(UUID schemaId) {
        return get(schemaId).map(schema -> schema.lignes() == null ? List.of() : schema.lignes());
    }

    @Override
    public Optional<LigneSchemaEcritureResponse> ligne(UUID id) {
        for (SchemaEcritureResponse schema : load()) {
            if (schema.lignes() == null) {
                continue;
            }
            for (LigneSchemaEcritureResponse line : schema.lignes()) {
                if (id.equals(line.id())) {
                    return Optional.of(line);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<List<ParametreSchemaResponse>> parametres(UUID ligneId) {
        return ligne(ligneId).map(line -> line.parametres() == null ? List.of() : line.parametres());
    }

    private List<SchemaEcritureResponse> load() {
        List<SchemaEcritureResponse> out = new ArrayList<>();
        for (Map<String, Object> row : client.schemas()) {
            out.add(toSchema(row));
        }
        return out;
    }

    private SchemaEcritureResponse toSchema(Map<String, Object> row) {
        long id = number(row.get("id"));
        UUID schemaId = stableId("omni-schema-", id);
        List<LigneSchemaEcritureResponse> lines = new ArrayList<>();
        for (Object raw : list(row.get("lignes"))) {
            if (raw instanceof Map<?, ?> line) {
                lines.add(toLine(schemaId, line));
            }
        }
        Integer version = integer(row.get("workflowVersion"));
        return new SchemaEcritureResponse(
                schemaId,
                text(row.get("nomSchema")),
                serviceType(text(row.get("typeService"))),
                text(row.get("patternCode")),
                text(row.get("workflowName")),
                version == null ? 1 : version,
                firstText(row.get("engine"), "flowable"),
                null,
                true,
                Instant.EPOCH,
                lines
        );
    }

    private LigneSchemaEcritureResponse toLine(UUID schemaId, Map<?, ?> row) {
        long id = number(row.get("id"));
        UUID lineId = stableId("omni-ligne-", id);
        List<ParametreSchemaResponse> params = new ArrayList<>();
        for (Object raw : list(row.get("parametres"))) {
            if (raw instanceof Map<?, ?> param) {
                params.add(toParam(lineId, param));
            }
        }
        return new LigneSchemaEcritureResponse(
                lineId,
                schemaId,
                integer(row.get("ordre")),
                text(row.get("stepId")),
                text(row.get("typeMethode")),
                enumValue(ProprieteApi.class, row.get("apiAUtiliser")),
                enumValue(ImputeAgence.class, row.get("agenceImputer")),
                text(row.get("priorite")),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Instant.EPOCH,
                params
        );
    }

    private ParametreSchemaResponse toParam(UUID lineId, Map<?, ?> row) {
        return new ParametreSchemaResponse(
                stableId("omni-schparam-", number(row.get("id"))),
                lineId,
                text(row.get("nomParametre")),
                text(row.get("codeParametre")),
                text(row.get("codeParametreMethode")),
                null,
                text(row.get("typeParametre")),
                enumValue(ParametreCategorie.class, row.get("codeCategorie")) == null
                        ? ParametreCategorie.USER
                        : enumValue(ParametreCategorie.class, row.get("codeCategorie")),
                text(row.get("valeur")),
                Instant.EPOCH
        );
    }

    private static ServiceTypeCode serviceType(String raw) {
        if (!StringUtils.hasText(raw)) {
            return ServiceTypeCode.DEFAUT;
        }
        return switch (raw.trim().toUpperCase()) {
            case "TRANSFERTARGENT", "TRANSFERT" -> ServiceTypeCode.TRANSFERT;
            case "PAIEMENTFACTURE", "PAIEMENT_FACTURE" -> ServiceTypeCode.PAIEMENT_FACTURE;
            case "CONSULTATION_SOLDE" -> ServiceTypeCode.CONSULTATION_SOLDE;
            case "CONSULTATIONHIST" -> ServiceTypeCode.CONSULTATIONHIST;
            case "PAYEMENT" -> ServiceTypeCode.PAYEMENT;
            default -> ServiceTypeCode.DEFAUT;
        };
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, Object raw) {
        String value = text(raw);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
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

    private static Integer integer(Object raw) {
        if (raw instanceof Number number) {
            return number.intValue();
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

    private static String firstText(Object preferred, String fallback) {
        String value = text(preferred);
        return StringUtils.hasText(value) ? value : fallback;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> list(Object raw) {
        if (raw instanceof List<?> values) {
            return (List<Object>) values;
        }
        return List.of();
    }
}
