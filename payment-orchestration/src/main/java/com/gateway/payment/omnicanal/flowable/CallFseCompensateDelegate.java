package com.gateway.payment.omnicanal.flowable;

import com.gateway.payment.omnicanal.domain.CasExecution;
import com.gateway.payment.omnicanal.policy.CompensationPolicy;
import com.gateway.payment.orchestration.api.dto.SchemaLegResult;
import com.gateway.payment.orchestration.domain.LigneSchemaEcriture;
import com.gateway.payment.orchestration.domain.ProprieteApi;
import com.gateway.payment.orchestration.service.SchemaLegExecutorService;
import com.gateway.payment.orchestration.service.SchemaStepBindingService;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.payment.repository.PaymentTransactionRepository;
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
 * Compensation d'une jambe FSE (miroir Omnicanal CallFseCompensateDelegate).
 * À brancher sur les BPMN same_fse / inter_fse (boundary compensate).
 */
@Component("gatewayCallFseCompensateDelegate")
public class CallFseCompensateDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(CallFseCompensateDelegate.class);

    private final PaymentTransactionRepository transactionRepository;
    private final SchemaStepBindingService stepBindingService;
    private final SchemaLegExecutorService legExecutor;

    public CallFseCompensateDelegate(
            PaymentTransactionRepository transactionRepository,
            SchemaStepBindingService stepBindingService,
            SchemaLegExecutorService legExecutor
    ) {
        this.transactionRepository = transactionRepository;
        this.stepBindingService = stepBindingService;
        this.legExecutor = legExecutor;
    }

    @Override
    public void execute(DelegateExecution execution) {
        String paymentId = stringVar(execution, "paymentId");
        if (!StringUtils.hasText(paymentId)) {
            log.warn("Compensation sans paymentId — ignorée");
            return;
        }
        PaymentTransaction tx = transactionRepository.findByPaymentId(paymentId).orElse(null);
        if (tx == null) {
            log.warn("Compensation paymentId={} introuvable", paymentId);
            return;
        }

        UUID schemaId = uuidVar(execution, "schemaEcritureId");
        String source = stringVar(execution, "sourceCodeFse");
        String dest = stringVar(execution, "destCodeFse");
        String activityId = execution.getCurrentActivityId();
        String stepKey = stringVar(execution, "compensateStepKey");
        if (!StringUtils.hasText(stepKey)) {
            stepKey = activityId;
        }

        Optional<LigneSchemaEcriture> line = schemaId != null
                ? stepBindingService.findLine(schemaId, stepKey)
                : Optional.empty();

        String typeMethode = line.map(LigneSchemaEcriture::getCompensationStepCode)
                .filter(StringUtils::hasText)
                .orElseGet(() -> reverseType(line.map(LigneSchemaEcriture::getTypeMethode).orElse("TRANSFERT")));

        if (line.isPresent()) {
            CompensationPolicy policy = CompensationPolicy.fromCode(line.get().getCompensationPolicy());
            if (policy == CompensationPolicy.NONE && !StringUtils.hasText(line.get().getCompensationStepCode())) {
                log.info("Pas de compensation configurée pour step={}", stepKey);
                return;
            }
        }

        @SuppressWarnings("unchecked")
        Map<String, String> userParams = execution.getVariable("userParams") instanceof Map<?, ?> m
                ? castStringMap(m)
                : new HashMap<>();

        log.info("Compensation Flowable paymentId={} step={} cas=REVERSION", paymentId, stepKey);
        userParams.put("CAS", CasExecution.REVERSION.name());
        SchemaLegResult result;
        if (line.isPresent()) {
            result = legExecutor.executeLeg(
                    tx, line.get(), source, dest, userParams, CasExecution.REVERSION
            );
        } else {
            result = legExecutor.executeLegByStep(
                    tx, schemaId, "compensate-" + stepKey, source, dest, userParams,
                    ProprieteApi.FSESOURCE, typeMethode
            );
        }
        execution.setVariable("lastCompensateSuccess", result.success());
        execution.setVariable("lastCompensateMessage", result.providerMessage());
    }

    private static String reverseType(String typeMethode) {
        if (typeMethode == null) {
            return "NOP";
        }
        return switch (typeMethode.toUpperCase()) {
            case "DEBIT" -> "CREDIT";
            case "CREDIT" -> "DEBIT";
            default -> "NOP";
        };
    }

    private static Map<String, String> castStringMap(Map<?, ?> m) {
        Map<String, String> out = new HashMap<>();
        m.forEach((k, v) -> {
            if (k != null && v != null) {
                out.put(String.valueOf(k), String.valueOf(v));
            }
        });
        return out;
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
}
