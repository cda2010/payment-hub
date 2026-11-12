package com.gateway.payment.orchestration.service;

import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.omnicanal.domain.CasExecution;
import com.gateway.payment.orchestration.api.dto.LegCompileRequest;
import com.gateway.payment.orchestration.api.dto.LegCompileResponse;
import com.gateway.payment.orchestration.api.dto.SchemaLegResult;
import com.gateway.payment.orchestration.domain.LigneSchemaEcriture;
import com.gateway.payment.orchestration.domain.ProprieteApi;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.provider.ProviderExecutionResult;
import com.gateway.payment.provider.ProviderInvocationPort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Exécute une jambe {@link LigneSchemaEcriture} : compile bindings → appel FSE.
 */
@Service
public class SchemaLegExecutorService {

    private final SchemaStepBindingService stepBindingService;
    private final LegCompilerService legCompilerService;
    private final ProviderInvocationPort providerInvocationPort;
    private final PaymentProviderRepository providerRepository;

    public SchemaLegExecutorService(
            SchemaStepBindingService stepBindingService,
            LegCompilerService legCompilerService,
            ProviderInvocationPort providerInvocationPort,
            PaymentProviderRepository providerRepository
    ) {
        this.stepBindingService = stepBindingService;
        this.legCompilerService = legCompilerService;
        this.providerInvocationPort = providerInvocationPort;
        this.providerRepository = providerRepository;
    }

    public SchemaLegResult executeLeg(
            PaymentTransaction tx,
            LigneSchemaEcriture line,
            String sourceCodeFse,
            String destCodeFse,
            Map<String, String> userParams
    ) {
        return executeLeg(tx, line, sourceCodeFse, destCodeFse, userParams, resolveCas(userParams));
    }

    public SchemaLegResult executeLeg(
            PaymentTransaction tx,
            LigneSchemaEcriture line,
            String sourceCodeFse,
            String destCodeFse,
            Map<String, String> userParams,
            CasExecution cas
    ) {
        LegCompileRequest compileReq = stepBindingService.toLegCompileRequest(
                line, sourceCodeFse, destCodeFse, userParams, cas
        );
        LegCompileResponse compiled = legCompilerService.compile(compileReq);
        return invokeCompiled(tx, line, compiled);
    }

    private static CasExecution resolveCas(Map<String, String> userParams) {
        if (userParams == null) {
            return CasExecution.NORMAL;
        }
        return CasExecution.fromParam(userParams.get("CAS"));
    }

    public SchemaLegResult executeLegByStep(
            PaymentTransaction tx,
            UUID schemaEcritureId,
            String stepId,
            String sourceCodeFse,
            String destCodeFse,
            Map<String, String> userParams,
            ProprieteApi fallbackApi,
            String fallbackTypeMethode
    ) {
        return stepBindingService.findLine(schemaEcritureId, stepId)
                .map(line -> executeLeg(tx, line, sourceCodeFse, destCodeFse, userParams))
                .orElseGet(() -> executeFallbackLeg(
                        tx, stepId, sourceCodeFse, destCodeFse, userParams, fallbackApi, fallbackTypeMethode
                ));
    }

    private SchemaLegResult executeFallbackLeg(
            PaymentTransaction tx,
            String stepId,
            String sourceCodeFse,
            String destCodeFse,
            Map<String, String> userParams,
            ProprieteApi fallbackApi,
            String fallbackTypeMethode
    ) {
        ProprieteApi api = fallbackApi != null ? fallbackApi : ProprieteApi.FSESOURCE;
        String typeMethode = StringUtils.hasText(fallbackTypeMethode) ? fallbackTypeMethode : "TRANSFERT";
        LegCompileRequest compileReq = new LegCompileRequest(
                sourceCodeFse,
                destCodeFse,
                typeMethode,
                api,
                userParams,
                null,
                resolveCas(userParams)
        );
        LegCompileResponse compiled = legCompilerService.compile(compileReq);
        return invokeCompiled(tx, null, compiled);
    }

    private SchemaLegResult invokeCompiled(
            PaymentTransaction tx,
            LigneSchemaEcriture line,
            LegCompileResponse compiled
    ) {
        PaymentProvider provider = providerRepository.findById(compiled.providerId())
                .orElseThrow(() -> new BusinessException(
                        "PROVIDER_NOT_FOUND",
                        "Fournisseur introuvable pour jambe FSE " + compiled.codeFse(),
                        HttpStatus.NOT_FOUND
                ));

        String operationCode = compiled.providerOperationCode();
        if (!StringUtils.hasText(operationCode)) {
            throw new BusinessException(
                    "OPERATION_NOT_RESOLVED",
                    "Opération non résolue pour " + compiled.typeMethode() + " / FSE " + compiled.codeFse()
            );
        }

        Map<String, Object> context = new HashMap<>();
        if (compiled.resolvedParams() != null) {
            compiled.resolvedParams().forEach(context::put);
        }
        context.put("codeFse", compiled.codeFse());
        context.put("typeMethode", compiled.typeMethode());

        String stepCode = line != null && StringUtils.hasText(line.getStepId())
                ? line.getStepId()
                : compiled.typeMethode();

        ProviderExecutionResult result = providerInvocationPort.callForProvider(
                tx,
                provider.getCode(),
                stepCode,
                operationCode,
                Map.of(),
                context
        );

        return new SchemaLegResult(
                line != null ? line.getId() : null,
                stepCode,
                line != null ? line.getOrdre() : null,
                compiled.codeFse(),
                compiled.typeMethode(),
                compiled.apiAUtiliser(),
                provider.getCode(),
                operationCode,
                result.isSuccess(),
                result.getProviderStatus(),
                result.getProviderMessage(),
                result.getProviderReference(),
                result.getGatewayStatus() != null ? result.getGatewayStatus() : PaymentStatus.FAILED,
                result.getResponseBody()
        );
    }
}
