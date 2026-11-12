package com.gateway.payment.omnicanal.canal;

import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.omnicanal.idempotency.CanalIdempotencyService;
import com.gateway.payment.omnicanal.response.CanalLegResult;
import com.gateway.payment.omnicanal.response.CanalOrchestrationInfo;
import com.gateway.payment.omnicanal.response.CanalTransferResponse;
import com.gateway.payment.omnicanal.response.OrchErrorCode;
import com.gateway.payment.omnicanal.response.SpecRunResult;
import com.gateway.payment.omnicanal.spec.SpecDrivenTransferExecutor;
import com.gateway.payment.omnicanal.util.CanalParamParser;
import com.gateway.payment.omnicanal.validation.ChannelPayloadValidator;
import com.gateway.payment.omnicanal.validation.ChannelValidationException;
import com.gateway.payment.orchestration.api.dto.LegCompileRequest;
import com.gateway.payment.orchestration.api.dto.LegCompileResponse;
import com.gateway.payment.orchestration.api.dto.SchemaLegResult;
import com.gateway.payment.orchestration.domain.InterfaceUtilisateur;
import com.gateway.payment.orchestration.domain.ProprieteApi;
import com.gateway.payment.orchestration.service.LegCompilerService;
import com.gateway.payment.orchestration.service.SchemaLegExecutorService;
import com.gateway.payment.payment.domain.PaymentTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Façade canal Omnicanal (boundaryByTypeMethode + omnicanalPresentation).
 * N'altère pas {@code PaymentEngine} checkout.
 */
@Service
public class CanalPresentationService {

    private static final Logger log = LoggerFactory.getLogger(CanalPresentationService.class);

    private final ChannelPayloadValidator validator;
    private final LegCompilerService legCompilerService;
    private final SchemaLegExecutorService legExecutor;
    private final SpecDrivenTransferExecutor specDrivenTransferExecutor;
    private final CanalIdempotencyService idempotencyService;

    public CanalPresentationService(
            ChannelPayloadValidator validator,
            LegCompilerService legCompilerService,
            SchemaLegExecutorService legExecutor,
            SpecDrivenTransferExecutor specDrivenTransferExecutor,
            CanalIdempotencyService idempotencyService
    ) {
        this.validator = validator;
        this.legCompilerService = legCompilerService;
        this.legExecutor = legExecutor;
        this.specDrivenTransferExecutor = specDrivenTransferExecutor;
        this.idempotencyService = idempotencyService;
    }

    public CanalTransferResponse boundaryByTypeMethode(String codeFse, String codeMethode, String otrParametre) {
        InterfaceUtilisateur iface = validator.validateBoundaryByTypeMethode(codeFse, codeMethode, otrParametre);
        Map<String, String> userParams = CanalParamParser.parseOtrParametre(otrParametre);
        userParams.putIfAbsent("codeFse", codeFse);

        PaymentProvider provider = iface.getProvider();
        PaymentTransaction tx = ephemeralTx(provider.getCode(), ServiceTypeCode.fromString(codeMethode));

        SchemaLegResult leg = legExecutor.executeLegByStep(
                tx,
                null,
                codeMethode,
                codeFse,
                codeFse,
                userParams,
                ProprieteApi.FSESOURCE,
                codeMethode
        );

        CanalTransferResponse response = leg.success()
                ? CanalTransferResponse.ok(leg.providerMessage() != null ? leg.providerMessage() : "OK")
                : CanalTransferResponse.fail(
                leg.providerStatus() != null ? leg.providerStatus() : "1",
                leg.providerMessage() != null ? leg.providerMessage() : "Échec FSE"
        );
        response.setPaymentId(tx.getPaymentId());
        response.setCodeOperation(leg.operationCode());
        CanalLegResult canalLeg = new CanalLegResult();
        canalLeg.setStepId(leg.stepId());
        canalLeg.setTypeMethode(leg.typeMethode());
        canalLeg.setCodeFse(leg.codeFse());
        canalLeg.setProviderCode(leg.providerCode());
        canalLeg.setSuccess(leg.success());
        canalLeg.setProviderStatus(leg.providerStatus());
        canalLeg.setProviderMessage(leg.providerMessage());
        canalLeg.setProviderReference(leg.providerReference());
        response.getLegs().add(canalLeg);
        response.setResponseEntity(Map.of(
                "code", leg.success() ? "0" : "1",
                "message", leg.providerMessage() != null ? leg.providerMessage() : "",
                "providerReference", leg.providerReference() != null ? leg.providerReference() : "",
                "codeFse", codeFse,
                "typeMethode", codeMethode
        ));
        return response;
    }

    public CanalTransferResponse omnicanalPresentation(
            ServiceTypeCode serviceType,
            String sourceCodeFse,
            String destCodeFse,
            Integer siege,
            Map<String, String> userParams,
            String idempotencyKey,
            String correlationId
    ) {
        if (!StringUtils.hasText(sourceCodeFse) || !StringUtils.hasText(destCodeFse)) {
            throw new ChannelValidationException("400", "codeFSE source/dest obligatoires");
        }
        ServiceTypeCode st = serviceType != null ? serviceType : ServiceTypeCode.TRANSFERT;

        if (StringUtils.hasText(idempotencyKey)) {
            Object lock = idempotencyService.lockFor(idempotencyKey);
            synchronized (lock) {
                try {
                    var replay = idempotencyService.findCompletedResponse(idempotencyKey);
                    if (replay.isPresent()) {
                        return replay.get();
                    }
                    if (idempotencyService.hasInFlight(idempotencyKey)) {
                        return CanalTransferResponse.fail(
                                OrchErrorCode.ORCH_IN_FLIGHT.name(),
                                "Opération en cours pour cette Idempotency-Key"
                        );
                    }
                    return runPresentation(st, sourceCodeFse, destCodeFse, siege, userParams, correlationId, idempotencyKey);
                } finally {
                    idempotencyService.releaseLock(idempotencyKey);
                }
            }
        }
        return runPresentation(st, sourceCodeFse, destCodeFse, siege, userParams, correlationId, null);
    }

    public LegCompileResponse compilePreview(
            String sourceCodeFse,
            String destCodeFse,
            String typeMethode,
            ProprieteApi api,
            Map<String, String> userParams
    ) {
        return legCompilerService.compile(new LegCompileRequest(
                sourceCodeFse, destCodeFse, typeMethode,
                api != null ? api : ProprieteApi.FSESOURCE,
                userParams, null
        ));
    }

    private CanalTransferResponse runPresentation(
            ServiceTypeCode serviceType,
            String sourceCodeFse,
            String destCodeFse,
            Integer siege,
            Map<String, String> userParams,
            String correlationId,
            String idempotencyKey
    ) {
        PaymentTransaction tx = ephemeralTx("ORCHESTRATION", serviceType);
        if (StringUtils.hasText(idempotencyKey)) {
            tx.setIdempotencyKey(idempotencyKey);
        }

        SpecRunResult run = specDrivenTransferExecutor.execute(new SpecDrivenTransferExecutor.SpecDrivenRequest(
                serviceType,
                sourceCodeFse,
                destCodeFse,
                siege,
                null,
                userParams,
                tx
        ));

        CanalTransferResponse response = !run.error()
                ? CanalTransferResponse.ok(run.getMessage())
                : CanalTransferResponse.fail(
                run.getErrorCode() != null ? run.getErrorCode().name() : "1",
                run.getMessage()
        );
        response.setPaymentId(tx.getPaymentId());
        response.setCorrelationId(correlationId);
        response.setLegs(run.getLegs());

        CanalOrchestrationInfo orch = new CanalOrchestrationInfo();
        orch.setEngine("SPECDRIVEN");
        orch.setWorkflow(run.getWorkflowName());
        orch.setPatternCode(run.getPatternCode());
        orch.setSchemaId(run.getSchemaId() != null ? run.getSchemaId().toString() : null);
        if (run.error()) {
            orch.setErrorCode(run.getErrorCode() != null ? run.getErrorCode().name() : null);
            orch.setErrorMessage(run.getMessage());
        }
        response.setOrchestration(orch);
        log.info(
                "Canal presentation serviceType={} {}→{} success={} compensated={}",
                serviceType, sourceCodeFse, destCodeFse, !run.error(), run.isCompensated()
        );
        return response;
    }

    private static PaymentTransaction ephemeralTx(String providerCode, ServiceTypeCode serviceType) {
        ServiceTypeCode st = serviceType != null ? serviceType : ServiceTypeCode.TRANSFERT;
        return PaymentTransaction.builder()
                .paymentId("C" + UUID.randomUUID().toString().replace("-", ""))
                .providerCode(providerCode)
                .serviceType(st)
                .amount(BigDecimal.ZERO)
                .currency("XOF")
                .status(PaymentStatus.PROCESSING)
                .currentStep("CANAL")
                .build();
    }
}
