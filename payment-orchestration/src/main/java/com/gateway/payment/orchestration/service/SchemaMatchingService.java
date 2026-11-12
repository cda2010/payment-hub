package com.gateway.payment.orchestration.service;

import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.domain.FseStatut;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.orchestration.api.dto.SchemaMatchRequest;
import com.gateway.payment.orchestration.api.dto.SchemaMatchResponse;
import com.gateway.payment.orchestration.dmn.DmnSchemaMatchingConfig;
import com.gateway.payment.orchestration.domain.SchemaEcriture;
import com.gateway.payment.orchestration.repository.LigneSchemaEcritureRepository;
import com.gateway.payment.orchestration.repository.SchemaEcritureRepository;
import com.rapidframe.bpm.dmn.DmnEvaluateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Matching schéma d'écriture via DMN {@code schema-matching.dmn}
 * (même règles qu'Omnicanal ; runtime Flowable via RapidFrame).
 */
@Service
public class SchemaMatchingService {

    private static final Logger log = LoggerFactory.getLogger(SchemaMatchingService.class);

    private final SchemaEcritureRepository schemaRepository;
    private final LigneSchemaEcritureRepository ligneRepository;
    private final PaymentProviderRepository providerRepository;
    private final OrchestrationMapper mapper;
    private final DmnEvaluateService dmnEvaluateService;

    public SchemaMatchingService(
            SchemaEcritureRepository schemaRepository,
            LigneSchemaEcritureRepository ligneRepository,
            PaymentProviderRepository providerRepository,
            OrchestrationMapper mapper,
            ObjectProvider<DmnEvaluateService> dmnEvaluateService
    ) {
        this.schemaRepository = schemaRepository;
        this.ligneRepository = ligneRepository;
        this.providerRepository = providerRepository;
        this.mapper = mapper;
        this.dmnEvaluateService = dmnEvaluateService.getIfAvailable();
        if (this.dmnEvaluateService == null) {
            log.warn("DmnEvaluateService indisponible (rapidframe.bpm.dmn-enabled=false) — matching schéma désactivé");
        }
    }

    @Transactional
    public SchemaMatchResponse match(SchemaMatchRequest request) {
        PaymentProvider source = requireProviderByCodeFse(request.sourceCodeFse());
        PaymentProvider dest = requireProviderByCodeFse(request.destCodeFse());

        boolean sameFse = Objects.equals(source.getCodeFse(), dest.getCodeFse());
        boolean samePays = source.getCountryCode() != null
                && dest.getCountryCode() != null
                && source.getCountryCode().equalsIgnoreCase(dest.getCountryCode());
        boolean bothBanque = source.getFseStatut() == FseStatut.BANQUE && dest.getFseStatut() == FseStatut.BANQUE;
        Integer siege = request.siege();
        boolean siegeTruthy = siege != null && siege != 0;

        List<SchemaEcriture> candidates = schemaRepository
                .findByServiceTypeAndActifTrueOrderByNomSchemaAsc(request.serviceType());

        SchemaEcriture matched = candidates.stream()
                .filter(schema -> matches(
                        schema.getPatternCode(),
                        sameFse,
                        samePays,
                        siege,
                        siegeTruthy,
                        bothBanque,
                        source.getCodeFse(),
                        dest.getCodeFse()
                ))
                .min(Comparator.comparing(SchemaEcriture::getPatternCode))
                .orElseThrow(() -> new BusinessException(
                        "SCHEMA_NO_MATCH",
                        "Aucun schéma pour " + request.serviceType() + " (" + request.sourceCodeFse() + " → " + request.destCodeFse() + ")",
                        HttpStatus.NOT_FOUND
                ));

        var lignes = ligneRepository.findBySchemaIdWithParametres(matched.getId()).stream()
                .map(mapper::toLigneResponse)
                .toList();

        return new SchemaMatchResponse(
                matched.getId(),
                matched.getNomSchema(),
                matched.getPatternCode(),
                matched.getWorkflowName(),
                matched.getWorkflowVersion(),
                matched.getServiceType(),
                lignes
        );
    }

    boolean matches(
            String patternCode,
            boolean sameFse,
            boolean samePays,
            Integer siege,
            boolean siegeTruthy,
            boolean bothBanque,
            String sourceFse,
            String destFse
    ) {
        if (!StringUtils.hasText(patternCode)) {
            return false;
        }

        String code = patternCode.trim().toUpperCase();

        // DMN Omnicanal (schema-matching.dmn) — si indisponible / erreur TX, fallback déterministe.
        if (dmnEvaluateService != null) {
            Map<String, Object> vars = new HashMap<>();
            vars.put("patternCode", code);
            vars.put("sameFse", sameFse);
            vars.put("samePays", samePays);
            vars.put("siege", siege);
            vars.put("siegeTruthy", siegeTruthy);
            vars.put("bothBanque", bothBanque);
            vars.put("sourceFse", sourceFse);
            vars.put("destFse", destFse);
            try {
                Map<String, Object> result = evaluateSchemaMatch(vars);
                boolean matched = isTruthyMatch(result);
                log.debug("DMN match pattern={} sourceFse={} destFse={} => {}",
                        code, sourceFse, destFse, matched);
                return matched;
            } catch (Exception e) {
                log.warn(
                        "DMN schema-matching failed for pattern={} ({}→{}) : {} — fallback déterministe",
                        code, sourceFse, destFse, e.getMessage()
                );
            }
        } else {
            log.debug("DMN indisponible — matching déterministe pattern={} ({}→{})",
                    code, sourceFse, destFse);
        }

        return matchesDeterministic(code, sameFse, samePays, siegeTruthy, bothBanque);
    }

    /**
     * Règles alignées Omnicanal / {@code SAME_FSE_ANY} / {@code DIFF_FSE_ANY}
     * (ex. Coris Money 0011 → 0011).
     */
    static boolean matchesDeterministic(
            String patternCode,
            boolean sameFse,
            boolean samePays,
            boolean siegeTruthy,
            boolean bothBanque
    ) {
        if (patternCode == null || patternCode.isBlank()) {
            return false;
        }
        return switch (patternCode.trim().toUpperCase()) {
            case "SAME_FSE_ANY" -> sameFse;
            case "DIFF_FSE_ANY" -> !sameFse;
            case "SAME_FSE_SAME_COUNTRY" -> sameFse && samePays;
            case "DIFF_FSE_SAME_COUNTRY" -> !sameFse && samePays;
            case "SIEGE" -> siegeTruthy;
            case "BOTH_BANQUE" -> bothBanque;
            default -> false;
        };
    }

    private Map<String, Object> evaluateSchemaMatch(Map<String, Object> vars) {
        try {
            return dmnEvaluateService.evaluate(DmnSchemaMatchingConfig.DECISION_KEY, vars);
        } catch (Exception primary) {
            // Flowable peut enregistrer la clé sous l'id OMG ou le name Omnicanal
            log.debug("DMN key {} failed ({}), retry {}", DmnSchemaMatchingConfig.DECISION_KEY,
                    primary.getMessage(), DmnSchemaMatchingConfig.DECISION_NAME);
            return dmnEvaluateService.evaluate(DmnSchemaMatchingConfig.DECISION_NAME, vars);
        }
    }

    static boolean isTruthyMatch(Map<String, Object> result) {
        if (result == null || result.isEmpty()) {
            return false;
        }
        Object matched = result.get(DmnSchemaMatchingConfig.OUTPUT_MATCHED);
        if (matched == null && result.size() == 1) {
            matched = result.values().iterator().next();
        }
        if (matched instanceof Boolean b) {
            return b;
        }
        if (matched instanceof Number n) {
            return n.intValue() != 0;
        }
        return matched != null && Boolean.parseBoolean(matched.toString());
    }

    private PaymentProvider requireProviderByCodeFse(String codeFse) {
        return providerRepository.findByCodeFse(codeFse)
                .orElseThrow(() -> new BusinessException(
                        "FSE_NOT_FOUND",
                        "Fournisseur FSE " + codeFse + " introuvable",
                        HttpStatus.NOT_FOUND
                ));
    }
}
