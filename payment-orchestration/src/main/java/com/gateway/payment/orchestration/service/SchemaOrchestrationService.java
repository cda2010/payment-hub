package com.gateway.payment.orchestration.service;

import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.omnicanal.response.CanalLegResult;
import com.gateway.payment.omnicanal.response.SpecRunResult;
import com.gateway.payment.omnicanal.spec.SpecDrivenTransferExecutor;
import com.gateway.payment.orchestration.api.dto.*;
import com.gateway.payment.orchestration.domain.LigneSchemaEcriture;
import com.gateway.payment.orchestration.domain.SchemaEcriture;
import com.gateway.payment.orchestration.repository.LigneSchemaEcritureRepository;
import com.gateway.payment.orchestration.repository.SchemaEcritureRepository;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.payment.repository.PaymentTransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Orchestration runtime : matching schéma → exécution séquentielle des {@link LigneSchemaEcriture}.
 */
@Service
public class SchemaOrchestrationService {

    private final SchemaEcritureRepository schemaRepository;
    private final LigneSchemaEcritureRepository ligneRepository;
    private final SchemaLegExecutorService legExecutor;
    private final PaymentTransactionRepository transactionRepository;
    private final PaymentProviderRepository providerRepository;
    private final SpecDrivenTransferExecutor specDrivenTransferExecutor;

    public SchemaOrchestrationService(
            SchemaEcritureRepository schemaRepository,
            LigneSchemaEcritureRepository ligneRepository,
            SchemaLegExecutorService legExecutor,
            PaymentTransactionRepository transactionRepository,
            PaymentProviderRepository providerRepository,
            SpecDrivenTransferExecutor specDrivenTransferExecutor
    ) {
        this.schemaRepository = schemaRepository;
        this.ligneRepository = ligneRepository;
        this.legExecutor = legExecutor;
        this.transactionRepository = transactionRepository;
        this.providerRepository = providerRepository;
        this.specDrivenTransferExecutor = specDrivenTransferExecutor;
    }

    @Transactional
    public SchemaExecuteResponse execute(SchemaExecuteRequest request) {
        PaymentTransaction tx = resolveTransaction(request);
        Map<String, String> userParams = request.userParams() != null
                ? new HashMap<>(request.userParams())
                : new HashMap<>();

        SpecRunResult run = specDrivenTransferExecutor.execute(new SpecDrivenTransferExecutor.SpecDrivenRequest(
                request.serviceType(),
                request.sourceCodeFse(),
                request.destCodeFse(),
                request.siege(),
                request.schemaEcritureId(),
                userParams,
                tx
        ));

        List<SchemaLegResult> results = new ArrayList<>();
        for (CanalLegResult leg : run.getLegs()) {
            results.add(new SchemaLegResult(
                    null,
                    leg.getStepId(),
                    null,
                    leg.getCodeFse(),
                    leg.getTypeMethode(),
                    null,
                    leg.getProviderCode(),
                    null,
                    leg.isSuccess(),
                    leg.getProviderStatus(),
                    leg.getProviderMessage(),
                    leg.getProviderReference(),
                    leg.isSuccess() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED,
                    Map.of()
            ));
        }

        SchemaEcriture schema = run.getSchemaId() != null
                ? schemaRepository.findById(run.getSchemaId()).orElse(null)
                : null;

        return new SchemaExecuteResponse(
                run.getSchemaId(),
                schema != null ? schema.getNomSchema() : run.getPatternCode(),
                run.getPatternCode(),
                run.getWorkflowName(),
                request.serviceType(),
                !run.error(),
                run.isCompensated()
                        ? "Orchestration compensée: " + run.getMessage()
                        : (run.error() ? run.getMessage() : "Orchestration terminée"),
                results
        );
    }

    @Transactional
    public SchemaLegResult executeLeg(LegExecuteRequest request) {
        PaymentTransaction tx = resolveLegTransaction(request);
        Map<String, String> userParams = request.userParams() != null
                ? new HashMap<>(request.userParams())
                : new HashMap<>();

        if (request.ligneSchemaEcritureId() != null) {
            LigneSchemaEcriture ligne = ligneRepository.findById(request.ligneSchemaEcritureId())
                    .orElseThrow(() -> new BusinessException(
                            "LIGNE_NOT_FOUND", "Ligne de schéma introuvable", HttpStatus.NOT_FOUND));
            return legExecutor.executeLeg(
                    tx, ligne, request.sourceCodeFse(), request.destCodeFse(), userParams);
        }

        return legExecutor.executeLegByStep(
                tx,
                null,
                request.typeMethode(),
                request.sourceCodeFse(),
                request.destCodeFse(),
                userParams,
                request.apiAUtiliser(),
                request.typeMethode()
        );
    }

    private PaymentTransaction resolveLegTransaction(LegExecuteRequest request) {
        if (StringUtils.hasText(request.paymentId())) {
            return transactionRepository.findByPaymentId(request.paymentId())
                    .orElseThrow(() -> new BusinessException(
                            "PAYMENT_NOT_FOUND",
                            "Paiement introuvable: " + request.paymentId(),
                            HttpStatus.NOT_FOUND
                    ));
        }
        String providerCode = StringUtils.hasText(request.providerCode())
                ? request.providerCode().trim()
                : providerRepository.findByCodeFse(request.sourceCodeFse())
                .map(PaymentProvider::getCode)
                .orElse("ORCHESTRATION");
        ServiceTypeCode serviceType = ServiceTypeCode.fromString(request.typeMethode());
        if (serviceType == null) {
            serviceType = "CONSULTATIONSOLDE".equalsIgnoreCase(request.typeMethode())
                    ? ServiceTypeCode.CONSULTATION_SOLDE
                    : ServiceTypeCode.TRANSFERT;
        }
        return PaymentTransaction.builder()
                .paymentId("LEG-" + UUID.randomUUID())
                .providerCode(providerCode)
                .serviceType(serviceType)
                .amount(BigDecimal.ZERO)
                .currency("XOF")
                .status(PaymentStatus.PROCESSING)
                .currentStep(request.typeMethode())
                .build();
    }

    private PaymentTransaction resolveTransaction(SchemaExecuteRequest request) {
        if (StringUtils.hasText(request.paymentId())) {
            return transactionRepository.findByPaymentId(request.paymentId())
                    .orElseThrow(() -> new BusinessException(
                            "PAYMENT_NOT_FOUND",
                            "Paiement introuvable: " + request.paymentId(),
                            HttpStatus.NOT_FOUND
                    ));
        }
        return PaymentTransaction.builder()
                .paymentId("ORCH-" + UUID.randomUUID())
                .providerCode("ORCHESTRATION")
                .serviceType(request.serviceType())
                .amount(extractAmount(request.userParams()))
                .currency("XOF")
                .status(PaymentStatus.PROCESSING)
                .currentStep("SCHEMA_ORCHESTRATION")
                .build();
    }

    private static BigDecimal extractAmount(Map<String, String> userParams) {
        if (userParams == null) {
            return BigDecimal.ZERO;
        }
        for (String key : List.of("montant", "MONTANT", "amount")) {
            String v = userParams.get(key);
            if (StringUtils.hasText(v)) {
                try {
                    return new BigDecimal(v.trim());
                } catch (NumberFormatException ignored) {
                    // try next key
                }
            }
        }
        return BigDecimal.ZERO;
    }
}
