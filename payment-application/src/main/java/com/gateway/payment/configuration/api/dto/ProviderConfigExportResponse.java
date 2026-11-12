package com.gateway.payment.configuration.api.dto;

import com.gateway.payment.common.domain.AuthType;
import com.gateway.payment.common.domain.ExecutionMode;
import com.gateway.payment.common.domain.PaymentMethodType;
import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.common.domain.StepType;
import com.gateway.payment.configuration.domain.FseParticipantKind;
import com.gateway.payment.configuration.domain.FseStatut;

import java.util.List;
import java.util.Map;

public record ProviderConfigExportResponse(
        int version,
        String exportedAt,
        ProviderExport provider,
        List<EnvironmentExport> environments,
        List<AuthenticationExport> authentications,
        List<FseApiExport> fseApis,
        List<OperationExport> operations,
        List<ApiExport> apis,
        List<FlowExport> flows,
        List<StatusMappingExport> statusMappings,
        OtpConfigExport otpConfig,
        List<WebhookExport> webhooks
) {
    public record ProviderExport(
            String code,
            String libelle,
            String description,
            String logoUrl,
            boolean actif,
            Integer ordreAffichage,
            PaymentMethodType paymentMethodType,
            ExecutionMode modeExecution,
            List<ServiceTypeCode> serviceTypes,
            String codeFse,
            String codeExterne,
            String groupCode,
            String countryCode,
            FseParticipantKind participantKind,
            FseStatut fseStatut
    ) {}

    public record EnvironmentExport(
            String code,
            String baseUrl,
            Integer timeoutSeconds,
            boolean actif
    ) {}

    public record AuthenticationExport(
            String environmentCode,
            AuthType authType,
            String clientId,
            String tokenUrl,
            String customHeaderName,
            String extraConfigJson
    ) {}

    public record FseApiExport(
            String code,
            String nom,
            String typeApi,
            String baseUrl,
            String host,
            String port,
            boolean useSsl,
            boolean useProxy,
            String environmentCode,
            boolean actif,
            AuthType authType,
            String clientId,
            String tokenUrl,
            String customHeaderName,
            String extraConfigJson
    ) {}

    public record OperationExport(
            String code,
            String nom,
            String description,
            boolean idempotent,
            boolean actif
    ) {}

    public record ApiExport(
            String code,
            String nom,
            String environmentCode,
            String fseApiCode,
            String operationCode,
            String httpMethod,
            String path,
            String contentType,
            Integer timeoutSeconds,
            String requestMappingJson,
            String responseMappingJson,
            boolean retryEnabled,
            Integer maxAttempts,
            Long retryDelayMs,
            Double backoffMultiplier,
            String retryOn,
            boolean actif
    ) {}

    public record FlowExport(
            String code,
            String nom,
            ServiceTypeCode serviceType,
            String bpmProcessKey,
            String description,
            boolean actif,
            List<FlowStepExport> steps
    ) {}

    public record FlowStepExport(
            String code,
            StepType stepType,
            int ordre,
            String apiOperationCode,
            String nextStepSuccess,
            String nextStepFailure,
            String configurationJson,
            boolean actif
    ) {}

    public record StatusMappingExport(
            String providerStatus,
            PaymentStatus gatewayStatus,
            Map<String, String> messages
    ) {}

    public record OtpConfigExport(
            boolean otpRequired,
            Integer length,
            Integer validitySeconds,
            Integer maxAttempts,
            boolean resendAllowed,
            Integer resendDelaySeconds,
            String generateOperation,
            String validateOperation
    ) {}

    public record WebhookExport(
            String pathSuffix,
            String authMode,
            String allowedIps,
            boolean actif
    ) {}
}
