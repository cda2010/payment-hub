package com.gateway.payment.orchestration.service;

import com.gateway.payment.omnicanal.domain.CasExecution;
import com.gateway.payment.orchestration.api.dto.LegCompileRequest;
import com.gateway.payment.orchestration.domain.LigneSchemaEcriture;
import com.gateway.payment.orchestration.repository.LigneSchemaEcritureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Relie un step BPMN ({@code stepId}) à {@link LigneSchemaEcriture} + {@link com.gateway.payment.orchestration.domain.ParametreSchema}.
 */
@Service
public class SchemaStepBindingService {

    private final LigneSchemaEcritureRepository ligneRepository;

    public SchemaStepBindingService(LigneSchemaEcritureRepository ligneRepository) {
        this.ligneRepository = ligneRepository;
    }

    @Transactional(readOnly = true)
    public Optional<LigneSchemaEcriture> findLine(UUID schemaEcritureId, String stepId) {
        if (schemaEcritureId == null || !StringUtils.hasText(stepId)) {
            return Optional.empty();
        }
        String key = stepId.trim();
        Optional<LigneSchemaEcriture> exact = ligneRepository.findBySchemaEcritureIdAndStepId(schemaEcritureId, key);
        if (exact.isPresent()) {
            return exact;
        }
        String alt = key.contains("-") ? toCamel(key) : toKebab(key);
        if (!alt.equals(key)) {
            Optional<LigneSchemaEcriture> viaAlias = ligneRepository.findBySchemaEcritureIdAndStepId(schemaEcritureId, alt);
            if (viaAlias.isPresent()) {
                return viaAlias;
            }
        }
        return ligneRepository.findBySchemaIdWithParametres(schemaEcritureId).stream()
                .filter(l -> l.getStepId() != null && (
                        key.equalsIgnoreCase(l.getStepId())
                                || alt.equalsIgnoreCase(l.getStepId())
                                || normalize(key).equals(normalize(l.getStepId()))
                ))
                .findFirst();
    }

    public LegCompileRequest toLegCompileRequest(
            LigneSchemaEcriture line,
            String sourceCodeFse,
            String destCodeFse,
            java.util.Map<String, String> userParams
    ) {
        return toLegCompileRequest(line, sourceCodeFse, destCodeFse, userParams, CasExecution.NORMAL);
    }

    public LegCompileRequest toLegCompileRequest(
            LigneSchemaEcriture line,
            String sourceCodeFse,
            String destCodeFse,
            java.util.Map<String, String> userParams,
            CasExecution cas
    ) {
        return new LegCompileRequest(
                sourceCodeFse,
                destCodeFse,
                line.getTypeMethode(),
                line.getApiAUtiliser(),
                userParams,
                line.getId(),
                cas != null ? cas : CasExecution.NORMAL
        );
    }

    private static String normalize(String s) {
        return s == null ? "" : s.toLowerCase(Locale.ROOT).replace("-", "").replace("_", "");
    }

    private static String toKebab(String camel) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                sb.append('-');
            }
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }

    private static String toCamel(String kebab) {
        StringBuilder sb = new StringBuilder();
        boolean up = false;
        for (char c : kebab.toCharArray()) {
            if (c == '-' || c == '_') {
                up = true;
                continue;
            }
            sb.append(up ? Character.toUpperCase(c) : c);
            up = false;
        }
        return sb.toString();
    }
}
