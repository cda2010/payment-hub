package com.gateway.payment.orchestration.service;

import com.gateway.payment.audit.service.AuditService;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.domain.ProviderApi;
import com.gateway.payment.configuration.domain.ProviderOperation;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.configuration.repository.ProviderApiRepository;
import com.gateway.payment.configuration.repository.ProviderOperationRepository;
import com.gateway.payment.orchestration.api.dto.InterfaceTemplatePreviewResponse;
import com.gateway.payment.orchestration.api.dto.InterfaceUtilisateurRequest;
import com.gateway.payment.orchestration.api.dto.InterfaceUtilisateurResponse;
import com.gateway.payment.orchestration.api.dto.ParametreUtilisateurRequest;
import com.gateway.payment.orchestration.api.dto.ParametreUtilisateurResponse;
import com.gateway.payment.orchestration.domain.InterfaceUtilisateur;
import com.gateway.payment.orchestration.domain.ParametreUtilisateur;
import com.gateway.payment.orchestration.domain.ProprieteApi;
import com.gateway.payment.orchestration.domain.TypeEnvoi;
import com.gateway.payment.orchestration.repository.InterfaceUtilisateurRepository;
import com.gateway.payment.orchestration.repository.ParametreUtilisateurRepository;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class InterfaceUtilisateurService {

    private final InterfaceUtilisateurRepository interfaceRepository;
    private final OrchestrationInterfaceCache interfaceCache;
    private final ParametreUtilisateurRepository parametreRepository;
    private final PaymentProviderRepository providerRepository;
    private final ProviderOperationRepository operationRepository;
    private final ProviderApiRepository apiRepository;
    private final OrchestrationMapper mapper;
    private final InterfaceParamTemplateCatalog paramTemplateCatalog;
    private final AuditService auditService;
    private final ObjectProvider<OmnicanalInterfaceSource> interfaceSource;

    @PersistenceContext
    private EntityManager entityManager;

    public InterfaceUtilisateurService(
            InterfaceUtilisateurRepository interfaceRepository,
            ParametreUtilisateurRepository parametreRepository,
            PaymentProviderRepository providerRepository,
            ProviderOperationRepository operationRepository,
            ProviderApiRepository apiRepository,
            OrchestrationMapper mapper,
            OrchestrationInterfaceCache interfaceCache,
            InterfaceParamTemplateCatalog paramTemplateCatalog,
            AuditService auditService,
            ObjectProvider<OmnicanalInterfaceSource> interfaceSource
    ) {
        this.interfaceRepository = interfaceRepository;
        this.parametreRepository = parametreRepository;
        this.providerRepository = providerRepository;
        this.operationRepository = operationRepository;
        this.apiRepository = apiRepository;
        this.mapper = mapper;
        this.interfaceCache = interfaceCache;
        this.paramTemplateCatalog = paramTemplateCatalog;
        this.auditService = auditService;
        this.interfaceSource = interfaceSource;
    }

    @Transactional(readOnly = true)
    public List<InterfaceUtilisateurResponse> listByProvider(UUID providerId) {
        List<InterfaceUtilisateurResponse> remote = remoteInterfaces(providerId);
        if (remote != null) {
            return remote;
        }
        requireProvider(providerId);
        return interfaceRepository.findByProviderIdWithParametres(providerId).stream()
                .sorted(java.util.Comparator.comparing(InterfaceUtilisateur::getTypeMethode))
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<InterfaceUtilisateurResponse> listByProvider(UUID providerId, Pageable pageable) {
        List<InterfaceUtilisateurResponse> remote = remoteInterfaces(providerId);
        if (remote != null) {
            int start = (int) Math.min(pageable.getOffset(), remote.size());
            int end = Math.min(start + pageable.getPageSize(), remote.size());
            return new PageImpl<>(remote.subList(start, end), pageable, remote.size());
        }
        requireProvider(providerId);
        Pageable effective = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by(Sort.Direction.ASC, "typeMethode")
                );
        Page<InterfaceUtilisateur> page = interfaceRepository.findByProvider_Id(providerId, effective);
        List<UUID> ids = page.getContent().stream().map(InterfaceUtilisateur::getId).toList();
        if (ids.isEmpty()) {
            return page.map(mapper::toResponse);
        }
        Map<UUID, InterfaceUtilisateur> hydrated = interfaceRepository.findAllByIdInWithParametres(ids).stream()
                .collect(Collectors.toMap(InterfaceUtilisateur::getId, Function.identity(), (a, b) -> a));
        return page.map(entity -> mapper.toResponse(hydrated.getOrDefault(entity.getId(), entity)));
    }

    @Transactional(readOnly = true)
    public InterfaceUtilisateurResponse getById(UUID id) {
        return mapper.toResponse(requireInterfaceWithParams(id));
    }

    @Transactional(readOnly = true)
    public InterfaceTemplatePreviewResponse previewTemplate(UUID providerId, String typeMethode) {
        requireProvider(providerId);
        String normalized = InterfaceParamTemplateCatalog.normalize(typeMethode);
        ProviderOperation operation = resolveOperationByCode(providerId, normalized).orElse(null);
        ProviderApi api = null;
        if (operation != null) {
            api = apiRepository.findByOperationId(operation.getId()).stream()
                    .filter(ProviderApi::isActif)
                    .findFirst()
                    .orElse(null);
        }
        return new InterfaceTemplatePreviewResponse(
                normalized,
                paramTemplateCatalog.serviceTypeFor(normalized),
                operation != null ? operation.getId() : null,
                operation != null ? operation.getCode() : null,
                api != null ? api.getId() : null,
                api != null ? api.getCode() : null,
                paramTemplateCatalog.previewParametres(normalized)
        );
    }

    @Transactional
    public InterfaceUtilisateurResponse create(UUID providerId, InterfaceUtilisateurRequest request, String actor) {
        PaymentProvider provider = requireProvider(providerId);
        // IU RESPONSE abandonné — contrat BillResponse fixe ; ignorer toute valeur client.
        TypeEnvoi typeEnvoi = TypeEnvoi.REQUEST;
        String typeMethode = request.typeMethode().trim().toUpperCase();
        interfaceRepository.findByCodeFseAndServiceTypeAndTypeMethodeAndTypeEnvoi(
                request.codeFse(), request.serviceType(), typeMethode, typeEnvoi
        ).ifPresent(existing -> {
            throw new BusinessException(
                    "INTERFACE_EXISTS",
                    "Interface déjà définie pour " + request.codeFse() + " / " + typeMethode
            );
        });

        ProviderOperation operation = resolveOperation(providerId, request.providerOperationId());
        if (operation == null) {
            operation = resolveOperationByCode(providerId, typeMethode).orElse(null);
        }
        ProviderApi api = resolveApi(providerId, request.providerApiId());
        if (api == null && operation != null) {
            api = apiRepository.findByOperationId(operation.getId()).stream()
                    .filter(ProviderApi::isActif)
                    .findFirst()
                    .orElse(null);
        }

        InterfaceUtilisateur entity = InterfaceUtilisateur.builder()
                .provider(provider)
                .codeFse(request.codeFse())
                .serviceType(request.serviceType())
                .typeMethode(typeMethode)
                .typeEnvoi(typeEnvoi)
                .providerOperation(operation)
                .providerApi(api)
                .codeInterface(StringUtils.hasText(request.codeInterface()) ? request.codeInterface() : typeMethode)
                .nomInterface(StringUtils.hasText(request.nomInterface())
                        ? request.nomInterface()
                        : provider.getLibelle() + " / " + typeMethode)
                .actif(false)
                .build();
        InterfaceUtilisateur saved = interfaceRepository.save(entity);
        paramTemplateCatalog.seedDefaultParametres(saved, parametreRepository);
        interfaceCache.invalidate(saved.getCodeFse(), saved.getTypeMethode(), saved.getTypeEnvoi());
        InterfaceUtilisateur hydrated = reloadInterfaceWithParams(saved.getId());
        if (request.actif()) {
            assertPublishable(hydrated);
            hydrated.setActif(true);
            hydrated = interfaceRepository.save(hydrated);
            auditService.record(actor, "INTERFACE_PUBLISH", "INTERFACE_UTILISATEUR", hydrated.getId().toString(),
                    hydrated.getCodeFse() + " / " + hydrated.getTypeMethode());
        } else {
            auditService.record(actor, "INTERFACE_CREATE", "INTERFACE_UTILISATEUR", hydrated.getId().toString(),
                    hydrated.getCodeFse() + " / " + hydrated.getTypeMethode());
        }
        return mapper.toResponse(reloadInterfaceWithParams(hydrated.getId()));
    }

    @Transactional
    public InterfaceUtilisateurResponse update(UUID providerId, UUID id, InterfaceUtilisateurRequest request, String actor) {
        InterfaceUtilisateur entity = requireInterfaceForProvider(providerId, id);
        String oldFse = entity.getCodeFse();
        String oldMethode = entity.getTypeMethode();
        TypeEnvoi oldEnvoi = entity.getTypeEnvoi();
        boolean wasActif = entity.isActif();
        // IU RESPONSE abandonné — contrat BillResponse fixe ; ignorer toute valeur client.
        TypeEnvoi typeEnvoi = TypeEnvoi.REQUEST;
        String typeMethode = request.typeMethode().trim().toUpperCase();
        interfaceRepository.findByCodeFseAndServiceTypeAndTypeMethodeAndTypeEnvoi(
                request.codeFse(), request.serviceType(), typeMethode, typeEnvoi
        ).filter(existing -> !existing.getId().equals(id)).ifPresent(existing -> {
            throw new BusinessException(
                    "INTERFACE_EXISTS",
                    "Interface déjà définie pour " + request.codeFse() + " / " + typeMethode
            );
        });
        entity.setCodeFse(request.codeFse());
        entity.setServiceType(request.serviceType());
        entity.setTypeMethode(typeMethode);
        entity.setTypeEnvoi(typeEnvoi);
        entity.setProviderOperation(resolveOperation(providerId, request.providerOperationId()));
        entity.setProviderApi(resolveApi(providerId, request.providerApiId()));
        entity.setCodeInterface(request.codeInterface());
        entity.setNomInterface(request.nomInterface());
        if (request.actif()) {
            assertPublishable(
                    entity.getProviderOperation(),
                    entity.getProviderApi(),
                    parametreRepository.findByInterfaceUtilisateurIdOrderByOrdreAscCodeParametreAsc(id)
            );
            entity.setActif(true);
        } else {
            entity.setActif(false);
        }
        InterfaceUtilisateur saved = interfaceRepository.save(entity);
        interfaceCache.invalidate(oldFse, oldMethode, oldEnvoi);
        interfaceCache.invalidate(saved.getCodeFse(), saved.getTypeMethode(), saved.getTypeEnvoi());
        String action = !wasActif && saved.isActif() ? "INTERFACE_PUBLISH"
                : wasActif && !saved.isActif() ? "INTERFACE_UNPUBLISH"
                : "INTERFACE_UPDATE";
        auditService.record(actor, action, "INTERFACE_UTILISATEUR", saved.getId().toString(),
                saved.getCodeFse() + " / " + saved.getTypeMethode() + " / actif=" + saved.isActif());
        return mapper.toResponse(saved);
    }

    @Transactional
    public InterfaceUtilisateurResponse publish(UUID providerId, UUID id, String actor) {
        InterfaceUtilisateur entity = requireInterfaceWithParams(
                requireInterfaceForProvider(providerId, id).getId());
        assertPublishable(entity);
        entity.setActif(true);
        InterfaceUtilisateur saved = interfaceRepository.save(entity);
        interfaceCache.invalidate(saved.getCodeFse(), saved.getTypeMethode(), saved.getTypeEnvoi());
        auditService.record(actor, "INTERFACE_PUBLISH", "INTERFACE_UTILISATEUR", id.toString(),
                saved.getCodeFse() + " / " + saved.getTypeMethode());
        return mapper.toResponse(reloadInterfaceWithParams(id));
    }

    @Transactional
    public InterfaceUtilisateurResponse unpublish(UUID providerId, UUID id, String actor) {
        InterfaceUtilisateur entity = requireInterfaceForProvider(providerId, id);
        entity.setActif(false);
        InterfaceUtilisateur saved = interfaceRepository.save(entity);
        interfaceCache.invalidate(saved.getCodeFse(), saved.getTypeMethode(), saved.getTypeEnvoi());
        auditService.record(actor, "INTERFACE_UNPUBLISH", "INTERFACE_UTILISATEUR", id.toString(),
                saved.getCodeFse() + " / " + saved.getTypeMethode());
        return mapper.toResponse(saved);
    }

    @Transactional
    public void delete(UUID providerId, UUID id, String actor) {
        InterfaceUtilisateur entity = requireInterfaceForProvider(providerId, id);
        interfaceCache.invalidate(entity.getCodeFse(), entity.getTypeMethode(), entity.getTypeEnvoi());
        auditService.record(actor, "INTERFACE_DELETE", "INTERFACE_UTILISATEUR", id.toString(),
                entity.getCodeFse() + " / " + entity.getTypeMethode());
        interfaceRepository.delete(entity);
    }

    private void assertPublishable(InterfaceUtilisateur entity) {
        List<ParametreUtilisateur> params = entity.getParametres() != null && !entity.getParametres().isEmpty()
                ? entity.getParametres()
                : parametreRepository.findByInterfaceUtilisateurIdOrderByOrdreAscCodeParametreAsc(entity.getId());
        assertPublishable(entity.getProviderOperation(), entity.getProviderApi(), params);
    }

    /** Opération ou API liée + ≥1 paramètre avec code. */
    private void assertPublishable(
            ProviderOperation operation,
            ProviderApi api,
            List<ParametreUtilisateur> params
    ) {
        List<String> errors = new ArrayList<>();
        if (operation == null && api == null) {
            errors.add("opération ou API requise");
        }
        if (params == null || params.isEmpty()) {
            errors.add("au moins un ParametreUtilisateur est requis");
        } else {
            for (ParametreUtilisateur p : params) {
                if (!StringUtils.hasText(p.getCodeParametre())) {
                    errors.add("paramètre sans codeParametre");
                    break;
                }
            }
        }
        if (!errors.isEmpty()) {
            throw new BusinessException(
                    "INTERFACE_NOT_PUBLISHABLE",
                    "Interface incomplète : " + String.join(" ; ", errors),
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    @Transactional(readOnly = true)
    public List<ParametreUtilisateurResponse> listParametres(UUID providerId, UUID interfaceId) {
        requireInterfaceForProvider(providerId, interfaceId);
        return parametreRepository.findByInterfaceUtilisateurIdOrderByOrdreAscCodeParametreAsc(interfaceId).stream()
                .map(mapper::toParametreUtilisateurResponse)
                .toList();
    }

    @Transactional
    public InterfaceUtilisateurResponse seedDefaultParametres(UUID providerId, UUID interfaceId) {
        InterfaceUtilisateur entity = requireInterfaceForProvider(providerId, interfaceId);
        paramTemplateCatalog.seedDefaultParametres(entity, parametreRepository);
        interfaceCache.invalidate(entity.getCodeFse(), entity.getTypeMethode(), entity.getTypeEnvoi());
        return mapper.toResponse(reloadInterfaceWithParams(interfaceId));
    }

    /**
     * Génère une InterfaceUtilisateur REQUEST depuis une méthode (ProviderApi).
     * Paramètres : mapping de la méthode, puis complément catalogue. Bloque si déjà existante.
     */
    @Transactional
    public InterfaceUtilisateurResponse generateFromProviderApi(UUID providerId, UUID providerApiId) {
        PaymentProvider provider = requireProvider(providerId);
        if (!StringUtils.hasText(provider.getCodeFse())) {
            throw new BusinessException(
                    "PROVIDER_FSE_REQUIRED",
                    "Le fournisseur doit avoir un code FSE pour générer une interface"
            );
        }
        ProviderApi api = resolveApi(providerId, providerApiId);
        if (api == null) {
            throw new BusinessException("API_NOT_FOUND", "Méthode introuvable", HttpStatus.NOT_FOUND);
        }

        ProviderOperation operation = api.getOperation();
        String typeMethode = InterfaceParamTemplateCatalog.normalize(
                StringUtils.hasText(api.getCode()) ? api.getCode()
                        : (operation != null ? operation.getCode() : "")
        );
        if (!StringUtils.hasText(typeMethode)) {
            throw new BusinessException("TYPE_METHODE_REQUIRED", "Impossible de déterminer le typeMethode");
        }

        var serviceType = paramTemplateCatalog.serviceTypeFor(typeMethode);
        boolean exists = interfaceRepository
                .findByCodeFseAndServiceTypeAndTypeMethodeAndTypeEnvoi(
                        provider.getCodeFse(), serviceType, typeMethode, TypeEnvoi.REQUEST)
                .isPresent()
                || interfaceRepository.findByProviderIdOrderByTypeMethodeAsc(providerId).stream()
                .anyMatch(i -> typeMethode.equalsIgnoreCase(i.getTypeMethode())
                        && i.getTypeEnvoi() == TypeEnvoi.REQUEST);

        if (exists) {
            throw new BusinessException(
                    "INTERFACE_EXISTS",
                    "Interface déjà définie pour " + provider.getCodeFse() + " / " + typeMethode + " (REQUEST)",
                    HttpStatus.CONFLICT
            );
        }

        InterfaceUtilisateur entity = InterfaceUtilisateur.builder()
                .provider(provider)
                .codeFse(provider.getCodeFse())
                .serviceType(serviceType)
                .typeMethode(typeMethode)
                .typeEnvoi(TypeEnvoi.REQUEST)
                .providerOperation(operation)
                .providerApi(api)
                .codeInterface(typeMethode)
                .nomInterface(provider.getLibelle() + " / " + typeMethode)
                .actif(true)
                .build();
        InterfaceUtilisateur saved = interfaceRepository.save(entity);
        seedParametresFromMethodMapping(saved, api.getRequestMappingJson());
        paramTemplateCatalog.seedDefaultParametres(saved, parametreRepository);
        interfaceCache.invalidate(saved.getCodeFse(), saved.getTypeMethode(), saved.getTypeEnvoi());
        return mapper.toResponse(reloadInterfaceWithParams(saved.getId()));
    }

    /** Crée des ParametreUtilisateur depuis requestMappingJson (clé externe → ${codeParametre} ou littéral). */
    private void seedParametresFromMethodMapping(InterfaceUtilisateur iface, String requestMappingJson) {
        if (!StringUtils.hasText(requestMappingJson)) {
            return;
        }
        Map<String, Object> map;
        try {
            map = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(requestMappingJson, new com.fasterxml.jackson.core.type.TypeReference<>() {});
        } catch (Exception ex) {
            return;
        }
        int ordre = 1;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String nomExterne = entry.getKey();
            if (!StringUtils.hasText(nomExterne)) {
                continue;
            }
            String raw = entry.getValue() == null ? "" : String.valueOf(entry.getValue()).trim();
            String codeParametre;
            String valeurFixe = null;
            if (raw.startsWith("${") && raw.endsWith("}") && raw.length() > 3) {
                codeParametre = raw.substring(2, raw.length() - 1).trim();
            } else {
                // Littéral (ex. "codeFse":"0011") → code = clé, valeur fixe
                codeParametre = nomExterne.trim();
                valeurFixe = StringUtils.hasText(raw) ? raw : null;
            }
            if (!StringUtils.hasText(codeParametre)) {
                continue;
            }
            if (parametreRepository.findByInterfaceUtilisateurIdAndCodeParametre(iface.getId(), codeParametre).isPresent()) {
                ordre++;
                continue;
            }
            parametreRepository.save(ParametreUtilisateur.builder()
                    .interfaceUtilisateur(iface)
                    .nomParametre(codeParametre)
                    .codeParametre(codeParametre)
                    .codeParametreMethode(null)
                    .typeParametre("STRING")
                    .valeur(valeurFixe)
                    .ordre(ordre++)
                    .build());
        }
    }

    private InterfaceUtilisateur reloadInterfaceWithParams(UUID id) {
        entityManager.flush();
        entityManager.clear();
        return requireInterfaceWithParams(id);
    }

    @Transactional
    public ParametreUtilisateurResponse addParametre(
            UUID providerId,
            UUID interfaceId,
            ParametreUtilisateurRequest request
    ) {
        InterfaceUtilisateur iface = requireInterfaceForProvider(providerId, interfaceId);
        String code = request.codeParametre().trim();
        parametreRepository.findByInterfaceUtilisateurIdAndCodeParametre(interfaceId, code)
                .ifPresent(existing -> {
                    throw new BusinessException(
                            "PARAM_EXISTS",
                            "Paramètre déjà défini: " + code
                    );
                });
            ParametreUtilisateur entity = ParametreUtilisateur.builder()
                .interfaceUtilisateur(iface)
                .nomParametre(request.nomParametre())
                .codeParametre(code)
                .codeParametreMethode(blankToNull(request.codeParametreMethode()))
                .typeParametre(StringUtils.hasText(request.typeParametre()) ? request.typeParametre() : "STRING")
                .valeur(request.valeur())
                .parametreApi(request.parametreApi())
                .attribut(blankToNull(request.attribut()))
                .sourceParametre(blankToNull(request.sourceParametre()))
                .extractScript(blankToNull(request.extractScript()))
                .labelKey(blankToNull(request.labelKey()))
                .labelsJson(blankToNull(request.labelsJson()))
                .affiche(request.affiche() == null || request.affiche())
                .ordre(request.ordre() != null ? request.ordre() : 0)
                .build();
        ParametreUtilisateur saved = parametreRepository.save(entity);
        interfaceCache.invalidate(iface.getCodeFse(), iface.getTypeMethode(), iface.getTypeEnvoi());
        return mapper.toParametreUtilisateurResponse(saved);
    }

    @Transactional
    public ParametreUtilisateurResponse updateParametre(
            UUID providerId,
            UUID interfaceId,
            UUID paramId,
            ParametreUtilisateurRequest request
    ) {
        ParametreUtilisateur entity = requireParamForInterface(providerId, interfaceId, paramId);
        InterfaceUtilisateur iface = entity.getInterfaceUtilisateur();
        entity.setNomParametre(request.nomParametre());
        entity.setCodeParametre(request.codeParametre().trim());
        entity.setCodeParametreMethode(blankToNull(request.codeParametreMethode()));
        entity.setTypeParametre(StringUtils.hasText(request.typeParametre()) ? request.typeParametre() : "STRING");
        entity.setValeur(request.valeur());
        entity.setParametreApi(request.parametreApi());
        entity.setAttribut(blankToNull(request.attribut()));
        entity.setSourceParametre(blankToNull(request.sourceParametre()));
        entity.setExtractScript(blankToNull(request.extractScript()));
        entity.setLabelKey(blankToNull(request.labelKey()));
        entity.setLabelsJson(blankToNull(request.labelsJson()));
        if (request.affiche() != null) {
            entity.setAffiche(request.affiche());
        }
        if (request.ordre() != null) {
            entity.setOrdre(request.ordre());
        }
        ParametreUtilisateur saved = parametreRepository.save(entity);
        interfaceCache.invalidate(iface.getCodeFse(), iface.getTypeMethode(), iface.getTypeEnvoi());
        return mapper.toParametreUtilisateurResponse(saved);
    }

    @Transactional
    public void deleteParametre(UUID providerId, UUID interfaceId, UUID paramId) {
        ParametreUtilisateur entity = requireParamForInterface(providerId, interfaceId, paramId);
        InterfaceUtilisateur iface = entity.getInterfaceUtilisateur();
        parametreRepository.delete(entity);
        interfaceCache.invalidate(iface.getCodeFse(), iface.getTypeMethode(), iface.getTypeEnvoi());
    }

    @Transactional(readOnly = true)
    public InterfaceUtilisateur resolveForLeg(String codeFse, String typeMethode, TypeEnvoi typeEnvoi) {
        return interfaceCache.resolve(codeFse, typeMethode, typeEnvoi);
    }

    public String resolveCodeFse(String sourceCodeFse, String destCodeFse, ProprieteApi apiAUtiliser) {
        return switch (apiAUtiliser) {
            case FSESOURCE -> sourceCodeFse;
            case FSEDESTINATAIRE, FSEPARTENAIRE -> destCodeFse;
        };
    }

    private List<InterfaceUtilisateurResponse> remoteInterfaces(UUID providerId) {
        OmnicanalInterfaceSource source = interfaceSource.getIfAvailable();
        if (source == null || !source.applies(providerId)) {
            return null;
        }
        return source.list(providerId);
    }

    private PaymentProvider requireProvider(UUID providerId) {
        return providerRepository.findById(providerId)
                .orElseThrow(() -> new BusinessException("PROVIDER_NOT_FOUND", "Fournisseur introuvable", HttpStatus.NOT_FOUND));
    }

    private InterfaceUtilisateur requireInterfaceWithParams(UUID id) {
        return interfaceRepository.findByIdWithParametres(id)
                .orElseThrow(() -> new BusinessException("INTERFACE_NOT_FOUND", "Interface introuvable", HttpStatus.NOT_FOUND));
    }

    private InterfaceUtilisateur requireInterfaceForProvider(UUID providerId, UUID id) {
        InterfaceUtilisateur entity = requireInterfaceWithParams(id);
        if (!entity.getProvider().getId().equals(providerId)) {
            throw new BusinessException("INTERFACE_NOT_FOUND", "Interface introuvable pour ce fournisseur", HttpStatus.NOT_FOUND);
        }
        return entity;
    }

    private ParametreUtilisateur requireParamForInterface(UUID providerId, UUID interfaceId, UUID paramId) {
        requireInterfaceForProvider(providerId, interfaceId);
        ParametreUtilisateur param = parametreRepository.findById(paramId)
                .orElseThrow(() -> new BusinessException("PARAM_NOT_FOUND", "Paramètre introuvable", HttpStatus.NOT_FOUND));
        if (!param.getInterfaceUtilisateur().getId().equals(interfaceId)) {
            throw new BusinessException("PARAM_NOT_FOUND", "Paramètre introuvable pour cette interface", HttpStatus.NOT_FOUND);
        }
        return param;
    }

    private ProviderOperation resolveOperation(UUID providerId, UUID operationId) {
        if (operationId == null) {
            return null;
        }
        ProviderOperation op = operationRepository.findById(operationId)
                .orElseThrow(() -> new BusinessException("OPERATION_NOT_FOUND", "Opération introuvable", HttpStatus.NOT_FOUND));
        if (!op.getProvider().getId().equals(providerId)) {
            throw new BusinessException("OPERATION_NOT_FOUND", "Opération introuvable pour ce fournisseur", HttpStatus.NOT_FOUND);
        }
        return op;
    }

    private ProviderApi resolveApi(UUID providerId, UUID apiId) {
        if (apiId == null) {
            return null;
        }
        ProviderApi api = apiRepository.findById(apiId)
                .orElseThrow(() -> new BusinessException("API_NOT_FOUND", "Endpoint introuvable", HttpStatus.NOT_FOUND));
        if (!api.getProvider().getId().equals(providerId)) {
            throw new BusinessException("API_NOT_FOUND", "Endpoint introuvable pour ce fournisseur", HttpStatus.NOT_FOUND);
        }
        return api;
    }

    private java.util.Optional<ProviderOperation> resolveOperationByCode(UUID providerId, String typeMethode) {
        return operationRepository.findByProviderIdAndCode(providerId, typeMethode)
                .or(() -> operationRepository.findByProviderIdAndCode(
                        providerId, InterfaceParamTemplateCatalog.normalize(typeMethode)));
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    /** Invalide tout le cache interfaces (changement d'env actif, etc.). */
    public void invalidateInterfaceCache() {
        interfaceCache.invalidateAll();
    }
}
