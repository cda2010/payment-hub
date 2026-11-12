package com.gateway.payment.configuration.service;

import com.gateway.payment.audit.service.AuditService;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.api.dto.EnvironmentRequest;
import com.gateway.payment.configuration.api.dto.EnvironmentResponse;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.domain.ProviderEnvironment;
import com.gateway.payment.configuration.domain.ProviderEnvironmentCode;
import com.gateway.payment.configuration.repository.ProviderEnvironmentRepository;
import com.gateway.payment.omnicanal.external.OmnicanalProviderCatalog;
import com.gateway.payment.orchestration.service.InterfaceUtilisateurService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class EnvironmentService {

    private final ProviderEnvironmentRepository environmentRepository;
    private final ProviderService providerService;
    private final AuditService auditService;
    private final InterfaceUtilisateurService interfaceUtilisateurService;
    private final OmnicanalProviderCatalog omnicanalCatalog;

    public EnvironmentService(
            ProviderEnvironmentRepository environmentRepository,
            ProviderService providerService,
            AuditService auditService,
            InterfaceUtilisateurService interfaceUtilisateurService,
            OmnicanalProviderCatalog omnicanalCatalog
    ) {
        this.environmentRepository = environmentRepository;
        this.providerService = providerService;
        this.auditService = auditService;
        this.interfaceUtilisateurService = interfaceUtilisateurService;
        this.omnicanalCatalog = omnicanalCatalog;
    }

    @Transactional(readOnly = true)
    public List<EnvironmentResponse> listByProvider(UUID providerId) {
        List<EnvironmentResponse> remote = omnicanalCatalog.environments(providerId);
        if (remote != null) {
            return remote;
        }
        providerService.requireProvider(providerId);
        return environmentRepository.findByProviderId(providerId).stream()
                .sorted(Comparator.comparing(ProviderEnvironment::getCode))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public EnvironmentResponse create(UUID providerId, EnvironmentRequest request, String actor) {
        PaymentProvider provider = providerService.requireProvider(providerId);
        String code = ProviderEnvironmentCode.parse(request.code()).name();
        environmentRepository.findByProviderIdAndCode(providerId, code).ifPresent(e -> {
            throw new BusinessException("ENVIRONMENT_CODE_EXISTS", "Code environnement déjà utilisé");
        });
        if (request.actif()) {
            deactivateOthers(providerId, null);
        }
        ProviderEnvironment env = ProviderEnvironment.builder()
                .provider(provider)
                .code(code)
                .baseUrl(request.baseUrl())
                .timeoutSeconds(request.timeoutSeconds())
                .actif(request.actif())
                .build();
        ProviderEnvironment saved = environmentRepository.save(env);
        if (request.actif()) {
            interfaceUtilisateurService.invalidateInterfaceCache();
        }
        auditService.record(actor, "ENVIRONMENT_CREATE", "ENVIRONMENT", saved.getId().toString(),
                "Création environnement " + saved.getCode() + " pour provider " + providerId);
        return toResponse(saved);
    }

    @Transactional
    public EnvironmentResponse update(UUID providerId, UUID envId, EnvironmentRequest request, String actor) {
        ProviderEnvironment env = requireEnvironment(providerId, envId);
        String code = ProviderEnvironmentCode.parse(request.code()).name();
        environmentRepository.findByProviderIdAndCode(providerId, code)
                .filter(existing -> !existing.getId().equals(envId))
                .ifPresent(e -> {
                    throw new BusinessException("ENVIRONMENT_CODE_EXISTS", "Code environnement déjà utilisé");
                });
        if (request.actif()) {
            deactivateOthers(providerId, envId);
        }
        env.setCode(code);
        env.setBaseUrl(request.baseUrl());
        env.setTimeoutSeconds(request.timeoutSeconds());
        env.setActif(request.actif());
        ProviderEnvironment saved = environmentRepository.save(env);
        interfaceUtilisateurService.invalidateInterfaceCache();
        auditService.record(actor, "ENVIRONMENT_UPDATE", "ENVIRONMENT", envId.toString(),
                "Mise à jour environnement " + saved.getCode());
        return toResponse(saved);
    }

    /**
     * Garantit SANDBOX / DEVELOPPEMENT / PRODUCTION pour un fournisseur.
     * Les manquants sont créés inactifs (sauf SANDBOX s'il n'y a aucun actif).
     */
    @Transactional
    public void ensureStandardEnvironments(UUID providerId, String defaultBaseUrl) {
        PaymentProvider provider = providerService.requireProvider(providerId);
        List<ProviderEnvironment> existing = environmentRepository.findByProviderId(providerId);
        String baseUrl = existing.stream()
                .filter(e -> ProviderEnvironmentCode.SANDBOX.name().equalsIgnoreCase(e.getCode()) || e.isActif())
                .map(ProviderEnvironment::getBaseUrl)
                .findFirst()
                .orElse(defaultBaseUrl != null && !defaultBaseUrl.isBlank()
                        ? defaultBaseUrl
                        : "https://example.invalid");
        Integer timeout = existing.stream()
                .map(ProviderEnvironment::getTimeoutSeconds)
                .filter(t -> t != null)
                .findFirst()
                .orElse(30);
        boolean hasActive = existing.stream().anyMatch(ProviderEnvironment::isActif);

        for (ProviderEnvironmentCode code : ProviderEnvironmentCode.values()) {
            boolean present = existing.stream()
                    .anyMatch(e -> code.name().equalsIgnoreCase(e.getCode())
                            || aliasesMatch(code, e.getCode()));
            if (present) {
                continue;
            }
            boolean actif = !hasActive && code == ProviderEnvironmentCode.SANDBOX;
            environmentRepository.save(ProviderEnvironment.builder()
                    .provider(provider)
                    .code(code.name())
                    .baseUrl(baseUrl)
                    .timeoutSeconds(timeout)
                    .actif(actif)
                    .build());
            if (actif) {
                hasActive = true;
            }
        }
    }

    private static boolean aliasesMatch(ProviderEnvironmentCode code, String raw) {
        if (raw == null) {
            return false;
        }
        try {
            return ProviderEnvironmentCode.parse(raw) == code;
        } catch (BusinessException ex) {
            return false;
        }
    }

    private void deactivateOthers(UUID providerId, UUID keepId) {
        for (ProviderEnvironment env : environmentRepository.findByProviderId(providerId)) {
            if (keepId != null && env.getId().equals(keepId)) {
                continue;
            }
            if (env.isActif()) {
                env.setActif(false);
                environmentRepository.save(env);
            }
        }
    }

    ProviderEnvironment requireEnvironment(UUID providerId, UUID envId) {
        ProviderEnvironment env = environmentRepository.findById(envId)
                .orElseThrow(() -> new BusinessException("ENVIRONMENT_NOT_FOUND", "Environnement introuvable",
                        HttpStatus.NOT_FOUND));
        if (!env.getProvider().getId().equals(providerId)) {
            throw new BusinessException("ENVIRONMENT_NOT_FOUND", "Environnement introuvable pour ce fournisseur",
                    HttpStatus.NOT_FOUND);
        }
        return env;
    }

    private EnvironmentResponse toResponse(ProviderEnvironment env) {
        return new EnvironmentResponse(
                env.getId(),
                env.getProvider().getId(),
                env.getCode(),
                env.getBaseUrl(),
                env.getTimeoutSeconds(),
                env.isActif(),
                env.getCreatedAt(),
                env.getUpdatedAt()
        );
    }
}
