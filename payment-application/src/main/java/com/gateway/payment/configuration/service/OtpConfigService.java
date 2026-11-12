package com.gateway.payment.configuration.service;

import com.gateway.payment.audit.service.AuditService;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.api.dto.OtpConfigRequest;
import com.gateway.payment.configuration.api.dto.OtpConfigResponse;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.domain.ProviderOtpConfig;
import com.gateway.payment.configuration.repository.ProviderOtpConfigRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OtpConfigService {

    private final ProviderOtpConfigRepository otpConfigRepository;
    private final ProviderService providerService;
    private final AuditService auditService;

    public OtpConfigService(
            ProviderOtpConfigRepository otpConfigRepository,
            ProviderService providerService,
            AuditService auditService
    ) {
        this.otpConfigRepository = otpConfigRepository;
        this.providerService = providerService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public OtpConfigResponse get(UUID providerId) {
        providerService.requireProvider(providerId);
        ProviderOtpConfig config = otpConfigRepository.findByProviderId(providerId)
                .orElseThrow(() -> new BusinessException("OTP_CONFIG_NOT_FOUND",
                        "Configuration OTP introuvable", HttpStatus.NOT_FOUND));
        return toResponse(config);
    }

    @Transactional
    public OtpConfigResponse upsert(UUID providerId, OtpConfigRequest request, String actor) {
        PaymentProvider provider = providerService.requireProvider(providerId);
        ProviderOtpConfig config = otpConfigRepository.findByProviderId(providerId)
                .orElseGet(() -> ProviderOtpConfig.builder().provider(provider).build());

        config.setOtpRequired(request.otpRequired());
        config.setLength(request.length());
        config.setValiditySeconds(request.validitySeconds());
        config.setMaxAttempts(request.maxAttempts());
        config.setResendAllowed(request.resendAllowed());
        config.setResendDelaySeconds(request.resendDelaySeconds());
        config.setGenerateOperation(request.generateOperation());
        config.setValidateOperation(request.validateOperation());

        ProviderOtpConfig saved = otpConfigRepository.save(config);
        auditService.record(actor, "OTP_CONFIG_UPSERT", "OTP_CONFIG", saved.getId().toString(),
                "Upsert OTP config provider=" + providerId);
        return toResponse(saved);
    }

    private OtpConfigResponse toResponse(ProviderOtpConfig config) {
        return new OtpConfigResponse(
                config.getId(),
                config.getProvider().getId(),
                config.isOtpRequired(),
                config.getLength(),
                config.getValiditySeconds(),
                config.getMaxAttempts(),
                config.isResendAllowed(),
                config.getResendDelaySeconds(),
                config.getGenerateOperation(),
                config.getValidateOperation()
        );
    }
}
