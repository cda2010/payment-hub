package com.gateway.payment.bpm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.catalog.omnicanal.ExternalOmnicanalCatalogGateway;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.orchestration.api.dto.SchemaLegResult;
import com.gateway.payment.orchestration.domain.LigneSchemaEcriture;
import com.gateway.payment.orchestration.domain.ProprieteApi;
import com.gateway.payment.orchestration.service.SchemaLegExecutorService;
import com.gateway.payment.orchestration.service.SchemaStepBindingService;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.payment.repository.PaymentTransactionRepository;
import com.gateway.payment.provider.ProviderExecutionResult;
import org.flowable.engine.delegate.BpmnError;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * ServiceTask BPMN → {@link LigneSchemaEcriture} (via stepId) → appel FSE compilé.
 * Équivalent Omnicanal {@code callFseTaskDelegate}.
 */
@Component("gatewayCallFseTaskDelegate")
public class GatewayCallFseTaskDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(GatewayCallFseTaskDelegate.class);

    private final PaymentTransactionRepository transactionRepository;
    private final SchemaStepBindingService stepBindingService;
    private final SchemaLegExecutorService legExecutor;
    private final ExternalOmnicanalCatalogGateway externalGateway;
    private final ObjectMapper objectMapper;

    public GatewayCallFseTaskDelegate(
            PaymentTransactionRepository transactionRepository,
            SchemaStepBindingService stepBindingService,
            SchemaLegExecutorService legExecutor,
            ExternalOmnicanalCatalogGateway externalGateway,
            ObjectMapper objectMapper
    ) {
        this.transactionRepository = transactionRepository;
        this.stepBindingService = stepBindingService;
        this.legExecutor = legExecutor;
        this.externalGateway = externalGateway;
        this.objectMapper = objectMapper;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String paymentId = stringVar(execution, PaymentProcessSupport.VAR_PAYMENT_ID);
        if (!StringUtils.hasText(paymentId)) {
            throw new BusinessException("PAYMENT_ID_MISSING", "paymentId manquant pour orchestration schéma");
        }
        PaymentTransaction tx = transactionRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new BusinessException("PAYMENT_NOT_FOUND", "Paiement introuvable: " + paymentId));

        UUID schemaId = uuidVar(execution, GatewayCallFseBpmnErrors.VAR_SCHEMA_ECRITURE_ID);
        String sourceCodeFse = stringVar(execution, GatewayCallFseBpmnErrors.VAR_SOURCE_CODE_FSE);
        String destCodeFse = stringVar(execution, GatewayCallFseBpmnErrors.VAR_DEST_CODE_FSE);
        Map<String, String> userParams = readUserParams(execution);

        String activityId = execution.getCurrentActivityId();
        String stepKey = readField(execution, "stepKey");
        String lookupKey = StringUtils.hasText(stepKey) ? stepKey : activityId;

        ProprieteApi fallbackApi = parseApi(readField(execution, "apiUtil"));
        String fallbackTypeMethode = readField(execution, "typeMethode");
        if (!StringUtils.hasText(fallbackTypeMethode)) {
            fallbackTypeMethode = defaultTypeMethode(activityId);
        }

        Optional<LigneSchemaEcriture> line = schemaId != null
                ? stepBindingService.findLine(schemaId, lookupKey)
                : Optional.empty();
        if (line.isEmpty() && StringUtils.hasText(activityId) && !activityId.equals(lookupKey) && schemaId != null) {
            line = stepBindingService.findLine(schemaId, activityId);
        }

        if (externalGateway != null && externalGateway.enabled()
                && "CONSULTATIONSOLDE".equalsIgnoreCase(fallbackTypeMethode)) {
            String fse = StringUtils.hasText(sourceCodeFse) ? sourceCodeFse : destCodeFse;
            ProviderExecutionResult external = externalGateway.boundary(
                    tx, "CONSULTATIONSOLDE", "CONSULTATIONSOLDE", fse, userParams);
            execution.setVariable("lastStepKey", lookupKey);
            execution.setVariable("lastStepSuccess", external.isSuccess());
            execution.setVariable("lastProviderReference", external.getProviderReference());
            if (external.getResponseBody() != null && external.getResponseBody().get("solde") != null) {
                execution.setVariable("solde", external.getResponseBody().get("solde"));
                execution.setVariable("balance", external.getResponseBody().get("solde"));
            }
            if (!external.isSuccess()) {
                throw new BpmnError(
                        GatewayCallFseBpmnErrors.STEP_FAILED,
                        external.getProviderMessage() != null
                                ? external.getProviderMessage() : GatewayCallFseBpmnErrors.STEP_FAILED
                );
            }
            return;
        }

        SchemaLegResult result;
        if (line.isPresent()) {
            log.info(
                    "Flowable schema leg activityId={} stepId={} schemaId={} ordre={} type={}",
                    activityId,
                    line.get().getStepId(),
                    schemaId,
                    line.get().getOrdre(),
                    line.get().getTypeMethode()
            );
            result = legExecutor.executeLeg(tx, line.get(), sourceCodeFse, destCodeFse, userParams);
        } else {
            log.warn("Aucune LigneSchemaEcrit pour schemaId={} step={} — fallback BPMN fields", schemaId, lookupKey);
            result = legExecutor.executeLegByStep(
                    tx, schemaId, lookupKey, sourceCodeFse, destCodeFse, userParams, fallbackApi, fallbackTypeMethode
            );
        }

        execution.setVariable("lastStepKey", result.stepId());
        execution.setVariable("lastStepSuccess", result.success());
        execution.setVariable("lastProviderReference", result.providerReference());

        if (!result.success()) {
            throw new BpmnError(
                    GatewayCallFseBpmnErrors.STEP_FAILED,
                    result.providerMessage() != null ? result.providerMessage() : GatewayCallFseBpmnErrors.STEP_FAILED
            );
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> readUserParams(DelegateExecution execution) {
        Map<String, String> out = new HashMap<>();
        Object raw = execution.getVariable(GatewayCallFseBpmnErrors.VAR_USER_PARAMS);
        if (raw instanceof Map<?, ?> map) {
            map.forEach((k, v) -> {
                if (k != null && v != null) {
                    out.put(String.valueOf(k), String.valueOf(v));
                }
            });
        } else if (raw instanceof String s && StringUtils.hasText(s)) {
            try {
                out.putAll(objectMapper.readValue(s, new TypeReference<>() {}));
            } catch (Exception ignored) {
                // fall through to execution vars
            }
        }
        supplementUserParamsFromExecution(execution).forEach(out::putIfAbsent);
        return out;
    }

    private Map<String, String> supplementUserParamsFromExecution(DelegateExecution execution) {
        Map<String, String> out = new HashMap<>();
        putIfPresent(out, "accountId", execution.getVariable("accountId"));
        putIfPresent(out, "numeroCompte", execution.getVariable("numeroCompte"));
        putIfPresent(out, "compteDebiteur", execution.getVariable("compteDebiteur"));
        putIfPresent(out, "CPTSOURCE", execution.getVariable("CPTSOURCE"));
        putIfPresent(out, "codeFse", execution.getVariable("codeFse"));
        if (!out.containsKey("numeroCompte") && out.containsKey("accountId")) {
            out.put("numeroCompte", out.get("accountId"));
        }
        if (!out.containsKey("CPTSOURCE") && out.containsKey("numeroCompte")) {
            out.put("CPTSOURCE", out.get("numeroCompte"));
        }
        String sourceFse = stringVar(execution, GatewayCallFseBpmnErrors.VAR_SOURCE_CODE_FSE);
        if (StringUtils.hasText(sourceFse)) {
            out.putIfAbsent("codeFse", sourceFse);
        }
        return out;
    }

    private static void putIfPresent(Map<String, String> out, String key, Object value) {
        if (value != null && StringUtils.hasText(String.valueOf(value))) {
            out.put(key, String.valueOf(value));
        }
    }

    private static String defaultTypeMethode(String activityId) {
        return switch (activityId != null ? activityId : "") {
            case "getBalance" -> "CONSULTATIONSOLDE";
            case "transfertSource" -> "TRANSFERT";
            case "transfertDest" -> "CREDIT";
            default -> "TRANSFERT";
        };
    }

    private static String readField(DelegateExecution execution, String name) {
        Object field = execution.getVariable(name);
        if (field != null) {
            return String.valueOf(field);
        }
        return null;
    }

    private static String stringVar(DelegateExecution execution, String name) {
        Object v = execution.getVariable(name);
        return v != null ? String.valueOf(v) : null;
    }

    private static UUID uuidVar(DelegateExecution execution, String name) {
        Object raw = execution.getVariable(name);
        if (raw == null) {
            return null;
        }
        if (raw instanceof UUID u) {
            return u;
        }
        try {
            return UUID.fromString(String.valueOf(raw));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static ProprieteApi parseApi(String raw) {
        if (!StringUtils.hasText(raw)) {
            return ProprieteApi.FSESOURCE;
        }
        try {
            return ProprieteApi.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ProprieteApi.FSESOURCE;
        }
    }
}
