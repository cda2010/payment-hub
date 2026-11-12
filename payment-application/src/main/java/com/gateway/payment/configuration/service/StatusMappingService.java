package com.gateway.payment.configuration.service;

import com.gateway.payment.audit.service.AuditService;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.common.i18n.GatewayI18nService;
import com.gateway.payment.common.i18n.LocalizedMessages;
import com.gateway.payment.configuration.api.dto.StatusMappingRequest;
import com.gateway.payment.configuration.api.dto.StatusMappingResponse;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.domain.ProviderStatusMapping;
import com.gateway.payment.configuration.repository.ProviderStatusMappingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class StatusMappingService {

    private final ProviderStatusMappingRepository statusMappingRepository;
    private final ProviderService providerService;
    private final AuditService auditService;
    private final GatewayI18nService i18nService;

    public StatusMappingService(
            ProviderStatusMappingRepository statusMappingRepository,
            ProviderService providerService,
            AuditService auditService,
            GatewayI18nService i18nService
    ) {
        this.statusMappingRepository = statusMappingRepository;
        this.providerService = providerService;
        this.auditService = auditService;
        this.i18nService = i18nService;
    }

    @Transactional(readOnly = true)
    public List<StatusMappingResponse> listByProvider(UUID providerId) {
        providerService.requireProvider(providerId);
        return statusMappingRepository.findByProviderId(providerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public StatusMappingResponse create(UUID providerId, StatusMappingRequest request, String actor) {
        PaymentProvider provider = providerService.requireProvider(providerId);
        statusMappingRepository.findByProviderIdAndProviderStatus(providerId, request.providerStatus())
                .ifPresent(m -> {
                    throw new BusinessException("STATUS_MAPPING_EXISTS",
                            "Mapping déjà défini pour le statut " + request.providerStatus());
                });
        Map<String, String> messages = LocalizedMessages.normalize(request.messages());
        messages = i18nService.filterSupportedMessages(messages);
        ProviderStatusMapping mapping = ProviderStatusMapping.builder()
                .provider(provider)
                .providerStatus(request.providerStatus())
                .gatewayStatus(request.gatewayStatus())
                .messages(messages)
                .build();
        ProviderStatusMapping saved = statusMappingRepository.save(mapping);
        auditService.record(actor, "STATUS_MAPPING_CREATE", "STATUS_MAPPING", saved.getId().toString(),
                request.providerStatus() + " -> " + request.gatewayStatus());
        return toResponse(saved);
    }

    @Transactional
    public StatusMappingResponse update(UUID providerId, UUID mappingId, StatusMappingRequest request, String actor) {
        ProviderStatusMapping mapping = requireMapping(providerId, mappingId);
        statusMappingRepository.findByProviderIdAndProviderStatus(providerId, request.providerStatus())
                .filter(existing -> !existing.getId().equals(mappingId))
                .ifPresent(m -> {
                    throw new BusinessException("STATUS_MAPPING_EXISTS",
                            "Mapping déjà défini pour le statut " + request.providerStatus());
                });
        mapping.setProviderStatus(request.providerStatus());
        mapping.setGatewayStatus(request.gatewayStatus());
        mapping.setMessages(i18nService.filterSupportedMessages(LocalizedMessages.normalize(request.messages())));
        ProviderStatusMapping saved = statusMappingRepository.save(mapping);
        auditService.record(actor, "STATUS_MAPPING_UPDATE", "STATUS_MAPPING", mappingId.toString(),
                request.providerStatus() + " -> " + request.gatewayStatus());
        return toResponse(saved);
    }

    private ProviderStatusMapping requireMapping(UUID providerId, UUID mappingId) {
        providerService.requireProvider(providerId);
        ProviderStatusMapping mapping = statusMappingRepository.findById(mappingId)
                .orElseThrow(() -> new BusinessException("STATUS_MAPPING_NOT_FOUND",
                        "Mapping introuvable", HttpStatus.NOT_FOUND));
        if (!mapping.getProvider().getId().equals(providerId)) {
            throw new BusinessException("STATUS_MAPPING_NOT_FOUND",
                    "Mapping introuvable pour ce fournisseur", HttpStatus.NOT_FOUND);
        }
        return mapping;
    }

    private StatusMappingResponse toResponse(ProviderStatusMapping mapping) {
        return new StatusMappingResponse(
                mapping.getId(),
                mapping.getProvider().getId(),
                mapping.getProviderStatus(),
                mapping.getGatewayStatus(),
                mapping.getMessages()
        );
    }
}
