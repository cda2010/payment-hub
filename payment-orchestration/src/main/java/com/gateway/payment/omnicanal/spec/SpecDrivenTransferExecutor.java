package com.gateway.payment.omnicanal.spec;

import com.gateway.payment.common.config.GatewayPerformanceProperties;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.omnicanal.concurrency.StepClaimResult;
import com.gateway.payment.omnicanal.concurrency.StepClaimService;
import com.gateway.payment.omnicanal.domain.CasExecution;
import com.gateway.payment.omnicanal.policy.CompensationPolicy;
import com.gateway.payment.omnicanal.policy.ErrorAction;
import com.gateway.payment.omnicanal.policy.ErrorClassifier;
import com.gateway.payment.omnicanal.policy.ErrorDecision;
import com.gateway.payment.omnicanal.policy.ErrorPolicyResolver;
import com.gateway.payment.omnicanal.policy.ErrorResolveRequest;
import com.gateway.payment.omnicanal.policy.ErrorType;
import com.gateway.payment.omnicanal.policy.RetryPolicy;
import com.gateway.payment.omnicanal.policy.StepCriticality;
import com.gateway.payment.omnicanal.policy.StepOutcome;
import com.gateway.payment.omnicanal.policy.StepPolicies;
import com.gateway.payment.omnicanal.policy.WorkflowErrorPolicy;
import com.gateway.payment.omnicanal.response.CanalLegResult;
import com.gateway.payment.omnicanal.response.OrchErrorCode;
import com.gateway.payment.omnicanal.response.SpecRunResult;
import com.gateway.payment.orchestration.api.dto.SchemaLegResult;
import com.gateway.payment.orchestration.api.dto.SchemaMatchRequest;
import com.gateway.payment.orchestration.api.dto.SchemaMatchResponse;
import com.gateway.payment.orchestration.domain.LigneSchemaEcriture;
import com.gateway.payment.orchestration.domain.SchemaEcriture;
import com.gateway.payment.orchestration.repository.LigneSchemaEcritureRepository;
import com.gateway.payment.orchestration.repository.SchemaEcritureRepository;
import com.gateway.payment.orchestration.service.SchemaLegExecutorService;
import com.gateway.payment.orchestration.service.SchemaMatchingService;
import com.gateway.payment.payment.domain.PaymentTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Exécuteur SpecDriven : match schéma → vagues de jambes (parallèles si indépendantes)
 * avec retry / policies / compensation.
 */
@Service
public class SpecDrivenTransferExecutor {

    private static final Logger log = LoggerFactory.getLogger(SpecDrivenTransferExecutor.class);

    private final SchemaMatchingService matchingService;
    private final SchemaEcritureRepository schemaRepository;
    private final LigneSchemaEcritureRepository ligneRepository;
    private final SchemaLegExecutorService legExecutor;
    private final StepClaimService stepClaimService;
    private final ErrorPolicyResolver errorPolicyResolver;
    private final boolean parallelLegs;
    private final ExecutorService legExecutorPool;

    public SpecDrivenTransferExecutor(
            SchemaMatchingService matchingService,
            SchemaEcritureRepository schemaRepository,
            LigneSchemaEcritureRepository ligneRepository,
            SchemaLegExecutorService legExecutor,
            StepClaimService stepClaimService,
            ErrorPolicyResolver errorPolicyResolver,
            @Autowired(required = false) GatewayPerformanceProperties performance,
            @Autowired(required = false) @Qualifier("connectorLegExecutor") ExecutorService connectorLegExecutor
    ) {
        this.matchingService = matchingService;
        this.schemaRepository = schemaRepository;
        this.ligneRepository = ligneRepository;
        this.legExecutor = legExecutor;
        this.stepClaimService = stepClaimService;
        this.errorPolicyResolver = errorPolicyResolver;
        this.parallelLegs = performance == null || performance.isParallelLegs();
        this.legExecutorPool = connectorLegExecutor;
    }

    public SpecRunResult execute(SpecDrivenRequest request) {
        SchemaEcriture schema;
        try {
            schema = resolveSchema(request);
        } catch (Exception e) {
            return SpecRunResult.fail(null, OrchErrorCode.ORCH_NO_SCHEMA, e.getMessage());
        }

        List<LigneSchemaEcriture> lignes = ligneRepository.findBySchemaIdWithParametres(schema.getId());
        if (lignes.isEmpty()) {
            return SpecRunResult.fail(schema.getId(), OrchErrorCode.ORCH_NO_SCHEMA, "Schéma sans lignes");
        }

        PaymentTransaction tx = request.transaction();
        Map<String, String> userParams = request.userParams() != null
                ? new HashMap<>(request.userParams())
                : new HashMap<>();
        String paymentKey = tx.getPaymentId() != null ? tx.getPaymentId() : UUID.randomUUID().toString();

        SpecRunResult result = SpecRunResult.ok(schema.getId(), schema.getWorkflowName(), schema.getPatternCode());
        List<LigneSchemaEcriture> succeeded = new ArrayList<>();
        Set<String> doneSteps = new HashSet<>();
        List<LigneSchemaEcriture> remaining = new ArrayList<>(lignes);

        while (!remaining.isEmpty()) {
            List<LigneSchemaEcriture> wave = selectReadyWave(remaining, doneSteps);
            if (wave.isEmpty()) {
                wave = List.of(remaining.get(0));
            }

            List<LegOutcome> outcomes = runWave(
                    wave, tx, request, userParams, paymentKey, schema, result, succeeded
            );

            boolean stop = false;
            for (LegOutcome outcome : outcomes) {
                remaining.remove(outcome.ligne());
                if (outcome.stopWorkflow()) {
                    stop = true;
                }
                if (outcome.success()) {
                    doneSteps.add(outcome.stepKey());
                }
            }
            if (stop || result.error()) {
                break;
            }
        }

        return result;
    }

    private List<LegOutcome> runWave(
            List<LigneSchemaEcriture> wave,
            PaymentTransaction tx,
            SpecDrivenRequest request,
            Map<String, String> userParams,
            String paymentKey,
            SchemaEcriture schema,
            SpecRunResult result,
            List<LigneSchemaEcriture> succeeded
    ) {
        boolean canParallel = parallelLegs && wave.size() > 1 && legExecutorPool != null;
        if (!canParallel) {
            List<LegOutcome> outs = new ArrayList<>();
            for (LigneSchemaEcriture ligne : wave) {
                outs.add(runSingleLeg(ligne, tx, request, userParams, paymentKey, schema, result, succeeded, false));
                if (outs.get(outs.size() - 1).stopWorkflow()) {
                    break;
                }
            }
            return outs;
        }

        List<CompletableFuture<LegOutcome>> futures = wave.stream()
                .map(ligne -> CompletableFuture.supplyAsync(
                        () -> runSingleLeg(ligne, tx, request, userParams, paymentKey, schema, result, succeeded, true),
                        legExecutorPool
                ))
                .toList();
            List<LegOutcome> outs = new ArrayList<>();
            for (CompletableFuture<LegOutcome> f : futures) {
                try {
                    outs.add(f.get(60, TimeUnit.SECONDS));
                } catch (Exception e) {
                    log.error("Parallel leg failed: {}", e.getMessage());
                    result.setError(true);
                    result.setErrorCode(OrchErrorCode.ORCH_STEP_FAILED);
                    result.setMessage(e.getMessage());
                    outs.add(new LegOutcome(wave.get(0), "parallel", false, true, false, null, null));
                }
            }
            maybeCompensateAfterWave(outs, tx, request, userParams, paymentKey, result, succeeded);
            return outs;
        }

    private LegOutcome runSingleLeg(
            LigneSchemaEcriture ligne,
            PaymentTransaction tx,
            SpecDrivenRequest request,
            Map<String, String> userParams,
            String paymentKey,
            SchemaEcriture schema,
            SpecRunResult result,
            List<LigneSchemaEcriture> succeeded,
            boolean deferCompensation
    ) {
        StepPolicies policies = StepPolicies.fromLigne(ligne);
        String stepKey = StringUtils.hasText(ligne.getStepId()) ? ligne.getStepId() : ligne.getTypeMethode();

        StepClaimResult claim = stepClaimService.tryClaim(paymentKey, stepKey, "spec-driven");
        if (claim == StepClaimResult.ALREADY_DONE) {
            return new LegOutcome(ligne, stepKey, true, false, false, null, null);
        }
        if (claim == StepClaimResult.BUSY) {
            synchronized (result) {
                result.setError(true);
                result.setErrorCode(OrchErrorCode.ORCH_STEP_FAILED);
                result.setMessage("Step busy: " + stepKey);
            }
            return new LegOutcome(ligne, stepKey, false, true, false, null, null);
        }

        SchemaLegResult legResult = executeWithRetry(
                tx, ligne, request.sourceCodeFse(), request.destCodeFse(), userParams, policies
        );
        CanalLegResult canalLeg = toCanalLeg(legResult);
        synchronized (result) {
            result.getLegs().add(canalLeg);
        }

        if (legResult.success()) {
            synchronized (succeeded) {
                succeeded.add(ligne);
            }
            stepClaimService.machine(paymentKey).transition(stepKey, StepOutcome.SUCCESS);
            stepClaimService.markDone(paymentKey, stepKey);
            stepClaimService.release(paymentKey, stepKey);
            return new LegOutcome(ligne, stepKey, true, false, false, null, null);
        }

        stepClaimService.machine(paymentKey).transition(stepKey, StepOutcome.FAILURE);
        ErrorDecision decision = errorPolicyResolver.resolve(ErrorResolveRequest.builder()
                .stepKey(stepKey)
                .message(legResult.providerMessage())
                .criticality(policies.criticality())
                .stepErrorPolicyCode(policies.errorPolicyCode())
                .schemaErrorPolicyCode(schema.getErrorPolicyCode())
                .schemaEcritureId(schema.getId())
                .stepDefaultAction(ErrorAction.parseOptional(ligne.getDefaultErrorAction()))
                .workflowPolicy(WorkflowErrorPolicy.fromSchema(schema))
                .build());

        if (decision.actionNormalized() == ErrorAction.CONTINUE && policies.criticality().allowsContinue()) {
            stepClaimService.release(paymentKey, stepKey);
            return new LegOutcome(ligne, stepKey, false, false, false, null, null);
        }

        synchronized (result) {
            result.setError(true);
            result.setErrorCode(OrchErrorCode.ORCH_STEP_FAILED);
            result.setMessage(legResult.providerMessage() != null ? legResult.providerMessage() : "Échec " + stepKey);
        }

        boolean compensateNow = shouldCompensate(decision, succeeded, policies);
        if (compensateNow && !deferCompensation) {
            synchronizeCompensate(tx, succeeded, request, userParams, paymentKey, result);
        }

        stepClaimService.release(paymentKey, stepKey);
        boolean stop = policies.blocking() || decision.actionNormalized().stopsWorkflow();
        // En parallèle : toujours réévaluer après la vague (succeeded peut être incomplet ici).
        boolean compensateAfter = deferCompensation;
        return new LegOutcome(ligne, stepKey, false, stop, compensateAfter, decision, policies);
    }

    private void maybeCompensateAfterWave(
            List<LegOutcome> outcomes,
            PaymentTransaction tx,
            SpecDrivenRequest request,
            Map<String, String> userParams,
            String paymentKey,
            SpecRunResult result,
            List<LigneSchemaEcriture> succeeded
    ) {
        for (LegOutcome outcome : outcomes) {
            if (!outcome.compensateAfterWave() || outcome.failureDecision() == null) {
                continue;
            }
            if (shouldCompensate(outcome.failureDecision(), succeeded, outcome.failurePolicies())) {
                synchronizeCompensate(tx, succeeded, request, userParams, paymentKey, result);
                return;
            }
        }
    }

    private void synchronizeCompensate(
            PaymentTransaction tx,
            List<LigneSchemaEcriture> succeeded,
            SpecDrivenRequest request,
            Map<String, String> userParams,
            String paymentKey,
            SpecRunResult result
    ) {
        boolean allOk;
        synchronized (succeeded) {
            allOk = compensate(
                    tx, new ArrayList<>(succeeded), request.sourceCodeFse(), request.destCodeFse(),
                    userParams, paymentKey, result
            );
        }
        synchronized (result) {
            result.setCompensated(allOk);
            result.setErrorCode(allOk ? OrchErrorCode.ORCH_COMPENSATED : OrchErrorCode.ORCH_COMPENSATION_FAILED);
            if (!allOk && result.getMessage() == null) {
                result.setMessage("Échec de compensation (reversion) après un step en erreur");
            }
        }
    }

    private static boolean shouldCompensate(
            ErrorDecision decision,
            List<LigneSchemaEcriture> succeeded,
            StepPolicies failedStepPolicies
    ) {
        // Pas de jambe OK → rien à compenser (évite ORCH_COMPENSATED à vide).
        if (succeeded == null || succeeded.isEmpty()) {
            return false;
        }
        if (decision != null && decision.compensate()) {
            return true;
        }
        // AUTO_REVERSE sur la jambe en échec = « compenser les précédentes »
        if (failedStepPolicies != null && failedStepPolicies.compensationPolicy().requiresReverse()) {
            return true;
        }
        for (LigneSchemaEcriture ligne : succeeded) {
            StepPolicies policies = StepPolicies.fromLigne(ligne);
            if (policies.compensationPolicy().requiresReverse()
                    || StringUtils.hasText(policies.compensationStepCode())) {
                return true;
            }
        }
        return false;
    }

    private static List<LigneSchemaEcriture> selectReadyWave(
            List<LigneSchemaEcriture> remaining,
            Set<String> doneSteps
    ) {
        List<LigneSchemaEcriture> ready = new ArrayList<>();
        for (LigneSchemaEcriture ligne : remaining) {
            StepPolicies policies = StepPolicies.fromLigne(ligne);
            if (!policies.dependency().hasDependency()) {
                ready.add(ligne);
                continue;
            }
            String dep = policies.dependency().dependsOnStepCode();
            if (doneSteps.contains(dep)) {
                ready.add(ligne);
            }
        }
        return ready;
    }

    private SchemaLegResult executeWithRetry(
            PaymentTransaction tx,
            LigneSchemaEcriture ligne,
            String sourceFse,
            String destFse,
            Map<String, String> userParams,
            StepPolicies policies
    ) {
        RetryPolicy retry = policies.retryPolicy();
        SchemaLegResult last = null;
        for (int attempt = 1; attempt <= Math.max(1, retry.maxAttempts()); attempt++) {
            last = legExecutor.executeLeg(tx, ligne, sourceFse, destFse, userParams);
            if (last.success()) {
                return last;
            }
            ErrorType type = ErrorClassifier.classifyType(null, null, last.providerMessage());
            if (type != ErrorType.TRANSIENT && type != ErrorType.TIMEOUT) {
                break;
            }
            if (!retry.allowsRetry(attempt)) {
                break;
            }
            sleepQuietly(retry.initialDelayMs());
        }
        return last;
    }

    private boolean compensate(
            PaymentTransaction tx,
            List<LigneSchemaEcriture> succeeded,
            String sourceFse,
            String destFse,
            Map<String, String> userParams,
            String paymentKey,
            SpecRunResult result
    ) {
        List<LigneSchemaEcriture> reverse = new ArrayList<>(succeeded);
        Collections.reverse(reverse);
        boolean allOk = true;
        for (LigneSchemaEcriture ligne : reverse) {
            StepPolicies policies = StepPolicies.fromLigne(ligne);
            if (policies.compensationPolicy() == CompensationPolicy.NONE
                    && !StringUtils.hasText(policies.compensationStepCode())) {
                continue;
            }
            Map<String, String> revParams = userParams != null ? new HashMap<>(userParams) : new HashMap<>();
            revParams.put("CAS", CasExecution.REVERSION.name());
            log.info("Compensation jambe step={} cas=REVERSION", ligne.getStepId());
            try {
                SchemaLegResult comp = legExecutor.executeLeg(
                        tx,
                        ligne,
                        sourceFse,
                        destFse,
                        revParams,
                        CasExecution.REVERSION
                );
                CanalLegResult canalLeg = toCanalLeg(comp);
                canalLeg.setStepId("compensate-" + ligne.getStepId());
                synchronized (result) {
                    result.getLegs().add(canalLeg);
                }
                String stepKey = StringUtils.hasText(ligne.getStepId()) ? ligne.getStepId() : ligne.getTypeMethode();
                if (comp.success()) {
                    stepClaimService.machine(paymentKey).transition(stepKey, StepOutcome.COMPENSATED);
                } else {
                    allOk = false;
                    log.warn("Compensation échouée pour {}: {}", ligne.getStepId(), comp.providerMessage());
                }
            } catch (Exception e) {
                allOk = false;
                log.warn("Compensation échouée pour {}: {}", ligne.getStepId(), e.getMessage());
            }
        }
        return allOk;
    }

    private SchemaEcriture resolveSchema(SpecDrivenRequest request) {
        if (request.schemaEcritureId() != null) {
            return schemaRepository.findById(request.schemaEcritureId())
                    .orElseThrow(() -> new IllegalArgumentException("Schéma introuvable"));
        }
        ServiceTypeCode st = request.serviceType() != null ? request.serviceType() : ServiceTypeCode.TRANSFERT;
        SchemaMatchResponse matched = matchingService.match(new SchemaMatchRequest(
                st, request.sourceCodeFse(), request.destCodeFse(), request.siege()
        ));
        return schemaRepository.findById(matched.schemaId())
                .orElseThrow(() -> new IllegalArgumentException("Schéma matché introuvable"));
    }

    private static CanalLegResult toCanalLeg(SchemaLegResult leg) {
        CanalLegResult c = new CanalLegResult();
        c.setStepId(leg.stepId());
        c.setTypeMethode(leg.typeMethode());
        c.setCodeFse(leg.codeFse());
        c.setProviderCode(leg.providerCode());
        c.setSuccess(leg.success());
        c.setProviderStatus(leg.providerStatus());
        c.setProviderMessage(leg.providerMessage());
        c.setProviderReference(leg.providerReference());
        return c;
    }

    private static void sleepQuietly(long ms) {
        if (ms <= 0) {
            return;
        }
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private record LegOutcome(
            LigneSchemaEcriture ligne,
            String stepKey,
            boolean success,
            boolean stopWorkflow,
            boolean compensateAfterWave,
            ErrorDecision failureDecision,
            StepPolicies failurePolicies
    ) {}

    public record SpecDrivenRequest(
            ServiceTypeCode serviceType,
            String sourceCodeFse,
            String destCodeFse,
            Integer siege,
            UUID schemaEcritureId,
            Map<String, String> userParams,
            PaymentTransaction transaction
    ) {}
}
