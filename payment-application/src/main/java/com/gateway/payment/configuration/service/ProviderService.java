package com.gateway.payment.configuration.service;

import com.gateway.payment.audit.service.AuditService;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.api.dto.ProviderRequest;
import com.gateway.payment.configuration.api.dto.ProviderResponse;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.domain.ServiceType;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.omnicanal.external.OmnicanalProviderCatalog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProviderService {

    private final PaymentProviderRepository providerRepository;
    private final ServiceTypeService serviceTypeService;
    private final AuditService auditService;
    private final OmnicanalProviderCatalog omnicanalCatalog;

    public ProviderService(
            PaymentProviderRepository providerRepository,
            ServiceTypeService serviceTypeService,
            AuditService auditService,
            OmnicanalProviderCatalog omnicanalCatalog
    ) {
        this.providerRepository = providerRepository;
        this.omnicanalCatalog = omnicanalCatalog;
        this.serviceTypeService = serviceTypeService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Page<ProviderResponse> list(Pageable pageable) {
        return list(null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ProviderResponse> list(String query, Pageable pageable) {
        List<ProviderResponse> remote = omnicanalCatalog.providers();
        if (remote != null) {
            List<ProviderResponse> filtered = filter(remote, query);
            int start = (int) Math.min(pageable.getOffset(), filtered.size());
            int end = Math.min(start + pageable.getPageSize(), filtered.size());
            return new PageImpl<>(new ArrayList<>(filtered.subList(start, end)), pageable, filtered.size());
        }
        Page<PaymentProvider> page = StringUtils.hasText(query)
                ? providerRepository.search(query.trim(), pageable)
                : providerRepository.findAll(pageable);
        List<UUID> ids = page.getContent().stream().map(PaymentProvider::getId).toList();
        if (ids.isEmpty()) {
            return page.map(this::toResponse);
        }
        Map<UUID, PaymentProvider> hydrated = providerRepository.findAllWithServiceTypesByIdIn(ids).stream()
                .collect(Collectors.toMap(
                        PaymentProvider::getId,
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new));
        return page.map(p -> toResponse(hydrated.getOrDefault(p.getId(), p)));
    }

    @Transactional(readOnly = true)
    public List<ProviderResponse> listAll() {
        List<ProviderResponse> remote = omnicanalCatalog.providers();
        if (remote != null) {
            return remote;
        }
        return providerRepository.findAllWithServiceTypes().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ProviderResponse> listActive() {
        List<ProviderResponse> remote = omnicanalCatalog.providers();
        if (remote != null) {
            return remote;
        }
        return providerRepository.findActiveWithServiceTypes().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProviderResponse> listActiveByServiceType(ServiceTypeCode serviceType) {
        List<ProviderResponse> remote = omnicanalCatalog.providers();
        if (remote != null) {
            return remote;
        }
        return providerRepository.findActiveByServiceType(serviceType).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProviderResponse getById(UUID id) {
        return omnicanalCatalog.provider(id)
                .orElseGet(() -> omnicanalCatalog.overlay(toResponse(requireProviderWithTypes(id))));
    }

    @Transactional
    public ProviderResponse create(ProviderRequest request, String actor) {
        if (providerRepository.existsByCode(request.code())) {
            throw new BusinessException("PROVIDER_CODE_EXISTS", "Code fournisseur déjà utilisé");
        }
        assertCodeFseAvailable(null, request.codeFse());
        PaymentProvider provider = PaymentProvider.builder()
                .code(request.code())
                .libelle(request.libelle())
                .description(request.description())
                .logoUrl(request.logoUrl())
                .actif(request.actif())
                .ordreAffichage(request.ordreAffichage())
                .paymentMethodType(request.paymentMethodType())
                .modeExecution(request.modeExecution())
                .build();
        applyFseIdentity(provider, request);
        applyServiceTypes(provider, request.serviceTypes());
        PaymentProvider saved = providerRepository.save(provider);
        auditService.record(actor, "PROVIDER_CREATE", "PROVIDER", saved.getId().toString(),
                "Création fournisseur " + saved.getCode());
        return toResponse(saved);
    }

    @Transactional
    public ProviderResponse update(UUID id, ProviderRequest request, String actor) {
        Optional<ProviderResponse> remote = omnicanalCatalog.provider(id);
        if (remote.isPresent()) {
            ProviderResponse saved = omnicanalCatalog.updateProvider(remote.get().codeFse(), request);
            auditService.record(actor, "PROVIDER_UPDATE", "PROVIDER", saved.id().toString(),
                    "Mise à jour fournisseur omnicanal " + saved.codeFse());
            return saved;
        }
        PaymentProvider provider = requireProviderWithTypes(id);
        if (!provider.getCode().equals(request.code()) && providerRepository.existsByCode(request.code())) {
            throw new BusinessException("PROVIDER_CODE_EXISTS", "Code fournisseur déjà utilisé");
        }
        assertCodeFseAvailable(id, request.codeFse());
        provider.setCode(request.code());
        provider.setLibelle(request.libelle());
        provider.setDescription(request.description());
        provider.setLogoUrl(request.logoUrl());
        provider.setActif(request.actif());
        provider.setOrdreAffichage(request.ordreAffichage());
        provider.setPaymentMethodType(request.paymentMethodType());
        provider.setModeExecution(request.modeExecution());
        applyFseIdentity(provider, request);
        applyServiceTypes(provider, request.serviceTypes());
        PaymentProvider saved = providerRepository.save(provider);
        auditService.record(actor, "PROVIDER_UPDATE", "PROVIDER", id.toString(),
                "Mise à jour fournisseur " + saved.getCode());
        return toResponse(saved);
    }

    @Transactional
    public ProviderResponse setEnabled(UUID id, boolean enabled, String actor) {
        PaymentProvider provider = requireProviderWithTypes(id);
        provider.setActif(enabled);
        PaymentProvider saved = providerRepository.save(provider);
        auditService.record(actor, enabled ? "PROVIDER_ENABLE" : "PROVIDER_DISABLE", "PROVIDER",
                id.toString(), null);
        return toResponse(saved);
    }

    private List<ProviderResponse> filter(List<ProviderResponse> providers, String query) {
        if (!StringUtils.hasText(query)) {
            return providers;
        }
        String needle = query.trim().toLowerCase(Locale.ROOT);
        return providers.stream()
                .filter(provider -> contains(provider.code(), needle)
                        || contains(provider.libelle(), needle)
                        || contains(provider.codeFse(), needle)
                        || contains(provider.codeExterne(), needle)
                        || contains(provider.countryCode(), needle))
                .toList();
    }

    private static boolean contains(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    PaymentProvider requireProvider(UUID id) {
        return providerRepository.findById(id)
                .orElseThrow(() -> new BusinessException("PROVIDER_NOT_FOUND", "Fournisseur introuvable",
                        HttpStatus.NOT_FOUND));
    }

    private PaymentProvider requireProviderWithTypes(UUID id) {
        return providerRepository.findByIdWithServiceTypes(id)
                .orElseThrow(() -> new BusinessException("PROVIDER_NOT_FOUND", "Fournisseur introuvable",
                        HttpStatus.NOT_FOUND));
    }

    private void applyServiceTypes(PaymentProvider provider, List<ServiceTypeCode> codes) {
        Set<ServiceType> resolved = serviceTypeService.resolveEntities(codes);
        provider.getServiceTypes().clear();
        provider.getServiceTypes().addAll(resolved);
    }

    private void applyFseIdentity(PaymentProvider provider, ProviderRequest request) {
        provider.setCodeFse(blankToNull(request.codeFse()));
        provider.setCodeExterne(blankToNull(request.codeExterne()));
        String group = blankToNull(request.groupCode());
        provider.setGroupCode(group != null ? group.toUpperCase(Locale.ROOT) : null);
        String country = blankToNull(request.countryCode());
        if (country != null && country.length() > 5) {
            throw new BusinessException(
                    "COUNTRY_CODE_TOO_LONG",
                    "Le pays d'une fiche locale est limité à 5 caractères.",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
        provider.setCountryCode(country != null ? country.toUpperCase(Locale.ROOT) : null);
        provider.setParticipantKind(request.participantKind());
        provider.setFseStatut(request.fseStatut());
    }

    private void assertCodeFseAvailable(UUID currentId, String codeFse) {
        String normalized = blankToNull(codeFse);
        if (normalized == null) {
            return;
        }
        providerRepository.findByCodeFse(normalized).ifPresent(existing -> {
            if (currentId == null || !existing.getId().equals(currentId)) {
                throw new BusinessException("PROVIDER_CODE_FSE_EXISTS", "Code FSE déjà utilisé");
            }
        });
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    ProviderResponse toResponse(PaymentProvider provider) {
        List<ServiceTypeCode> types = provider.getServiceTypes() == null
                ? List.of()
                : provider.getServiceTypes().stream()
                .map(ServiceType::getCode)
                .sorted(Comparator.comparing(Enum::name))
                .toList();
        return new ProviderResponse(
                provider.getId(),
                provider.getCode(),
                provider.getLibelle(),
                provider.getDescription(),
                provider.getLogoUrl(),
                provider.isActif(),
                provider.getOrdreAffichage(),
                provider.getPaymentMethodType() != null
                        ? provider.getPaymentMethodType()
                        : com.gateway.payment.common.domain.PaymentMethodType.MOBILE_MONEY,
                provider.getModeExecution(),
                types,
                provider.getCodeFse(),
                provider.getCodeExterne(),
                provider.getGroupCode(),
                provider.getCountryCode(),
                provider.getParticipantKind(),
                provider.getFseStatut(),
                provider.getCreatedAt(),
                provider.getUpdatedAt()
        );
    }
}
