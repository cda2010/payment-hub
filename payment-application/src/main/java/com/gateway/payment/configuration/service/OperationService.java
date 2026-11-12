package com.gateway.payment.configuration.service;

import com.gateway.payment.audit.service.AuditService;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.api.dto.OperationRequest;
import com.gateway.payment.configuration.api.dto.OperationResponse;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.domain.ProviderOperation;
import com.gateway.payment.configuration.repository.ProviderOperationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class OperationService {

    private final ProviderOperationRepository operationRepository;
    private final ProviderService providerService;
    private final AuditService auditService;

    public OperationService(
            ProviderOperationRepository operationRepository,
            ProviderService providerService,
            AuditService auditService
    ) {
        this.operationRepository = operationRepository;
        this.providerService = providerService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<OperationResponse> listByProvider(UUID providerId) {
        providerService.requireProvider(providerId);
        return operationRepository.findByProviderId(providerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OperationResponse create(UUID providerId, OperationRequest request, String actor) {
        PaymentProvider provider = providerService.requireProvider(providerId);
        operationRepository.findByProviderIdAndCode(providerId, request.code()).ifPresent(o -> {
            throw new BusinessException("OPERATION_CODE_EXISTS", "Code opération déjà utilisé");
        });
        ProviderOperation operation = ProviderOperation.builder()
                .provider(provider)
                .code(request.code())
                .nom(request.nom())
                .description(request.description())
                .idempotent(request.idempotent())
                .actif(request.actif())
                .build();
        ProviderOperation saved = operationRepository.save(operation);
        auditService.record(actor, "OPERATION_CREATE", "OPERATION", saved.getId().toString(),
                "Création opération " + saved.getCode());
        return toResponse(saved);
    }

    @Transactional
    public OperationResponse update(UUID providerId, UUID opId, OperationRequest request, String actor) {
        ProviderOperation operation = requireOperation(providerId, opId);
        operationRepository.findByProviderIdAndCode(providerId, request.code())
                .filter(existing -> !existing.getId().equals(opId))
                .ifPresent(o -> {
                    throw new BusinessException("OPERATION_CODE_EXISTS", "Code opération déjà utilisé");
                });
        operation.setCode(request.code());
        operation.setNom(request.nom());
        operation.setDescription(request.description());
        operation.setIdempotent(request.idempotent());
        operation.setActif(request.actif());
        ProviderOperation saved = operationRepository.save(operation);
        auditService.record(actor, "OPERATION_UPDATE", "OPERATION", opId.toString(),
                "Mise à jour opération " + saved.getCode());
        return toResponse(saved);
    }

    ProviderOperation requireOperation(UUID providerId, UUID opId) {
        ProviderOperation operation = operationRepository.findById(opId)
                .orElseThrow(() -> new BusinessException("OPERATION_NOT_FOUND", "Opération introuvable",
                        HttpStatus.NOT_FOUND));
        if (!operation.getProvider().getId().equals(providerId)) {
            throw new BusinessException("OPERATION_NOT_FOUND", "Opération introuvable pour ce fournisseur",
                    HttpStatus.NOT_FOUND);
        }
        return operation;
    }

    private OperationResponse toResponse(ProviderOperation operation) {
        return new OperationResponse(
                operation.getId(),
                operation.getProvider().getId(),
                operation.getCode(),
                operation.getNom(),
                operation.getDescription(),
                operation.isIdempotent(),
                operation.isActif(),
                operation.getCreatedAt()
        );
    }
}
