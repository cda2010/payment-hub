package com.gateway.payment.catalog.service;

import com.gateway.payment.catalog.api.dto.CategorieServiceRequest;
import com.gateway.payment.catalog.api.dto.CategorieServiceResponse;
import com.gateway.payment.catalog.domain.CategorieService;
import com.gateway.payment.catalog.repository.CategorieServiceRepository;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.common.i18n.I18nScope;
import com.gateway.payment.common.i18n.TranslationService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class CategorieServiceService {

    private final CategorieServiceRepository repository;
    private final TranslationService translations;

    public CategorieServiceService(CategorieServiceRepository repository, TranslationService translations) {
        this.repository = repository;
        this.translations = translations;
    }

    @Transactional(readOnly = true)
    public List<CategorieServiceResponse> listAll() {
        return toResponses(repository.findAllByOrderByOrdreAffichageAscNameAsc());
    }

    @Transactional(readOnly = true)
    public List<CategorieServiceResponse> listActive() {
        return toResponses(repository.findByActifTrueOrderByOrdreAffichageAscNameAsc());
    }

    @Transactional(readOnly = true)
    public CategorieServiceResponse get(UUID id) {
        return toResponse(require(id));
    }

    @Transactional
    public CategorieServiceResponse create(CategorieServiceRequest request) {
        String code = normalizeCode(request.code());
        if (repository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("CATEGORIE_SERVICE_EXISTS", "Catégorie déjà existante: " + code);
        }
        CategorieService entity = CategorieService.builder()
                .code(code)
                .name(request.name().trim())
                .description(request.description())
                .ordreAffichage(request.ordreAffichage())
                .actif(request.actif() == null || request.actif())
                .build();
        entity = repository.save(entity);
        saveTranslations(entity, request);
        return toResponse(entity);
    }

    @Transactional
    public CategorieServiceResponse update(UUID id, CategorieServiceRequest request) {
        CategorieService entity = require(id);
        String code = normalizeCode(request.code());
        if (!entity.getCode().equalsIgnoreCase(code) && repository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("CATEGORIE_SERVICE_EXISTS", "Catégorie déjà existante: " + code);
        }
        entity.setCode(code);
        entity.setName(request.name().trim());
        entity.setDescription(request.description());
        if (request.ordreAffichage() != null) {
            entity.setOrdreAffichage(request.ordreAffichage());
        }
        if (request.actif() != null) {
            entity.setActif(request.actif());
        }
        entity = repository.save(entity);
        saveTranslations(entity, request);
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public CategorieService requireByCodeOrId(String categoryCode, UUID categoryId) {
        if (categoryId != null) {
            return require(categoryId);
        }
        if (categoryCode == null || categoryCode.isBlank()) {
            throw new BusinessException(
                    "CATEGORIE_SERVICE_REQUIRED",
                    "categoryCode ou categoryId est obligatoire");
        }
        String code = normalizeCode(categoryCode);
        return repository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new BusinessException(
                        "CATEGORIE_SERVICE_NOT_FOUND",
                        "Catégorie introuvable: " + code,
                        HttpStatus.NOT_FOUND));
    }

    private CategorieService require(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException(
                        "CATEGORIE_SERVICE_NOT_FOUND",
                        "Catégorie introuvable: " + id,
                        HttpStatus.NOT_FOUND));
    }

    private void saveTranslations(CategorieService entity, CategorieServiceRequest request) {
        Map<String, Map<String, String>> byPath = new LinkedHashMap<>();
        byPath.put(I18nScope.FIELD_NAME,
                translations.withDefaultLanguage(request.nameI18n(), entity.getName()));
        byPath.put(I18nScope.FIELD_DESCRIPTION,
                translations.withDefaultLanguage(request.descriptionI18n(), entity.getDescription()));
        translations.saveAll(I18nScope.CATEGORIE_SERVICE, entity.getId(), byPath);
    }

    private List<CategorieServiceResponse> toResponses(List<CategorieService> entities) {
        Map<UUID, TranslationService.Bundle> bundles = translations.bundles(
                I18nScope.CATEGORIE_SERVICE, entities.stream().map(CategorieService::getId).toList());
        return entities.stream()
                .map(c -> toResponse(c, bundles.get(c.getId())))
                .toList();
    }

    private CategorieServiceResponse toResponse(CategorieService c) {
        return toResponse(c, translations.bundle(I18nScope.CATEGORIE_SERVICE, c.getId()));
    }

    private static CategorieServiceResponse toResponse(CategorieService c, TranslationService.Bundle bundle) {
        return new CategorieServiceResponse(
                c.getId(),
                c.getCode(),
                bundle.text(I18nScope.FIELD_NAME, c.getName()),
                bundle.text(I18nScope.FIELD_DESCRIPTION, c.getDescription()),
                bundle.messages(I18nScope.FIELD_NAME),
                bundle.messages(I18nScope.FIELD_DESCRIPTION),
                c.getOrdreAffichage(),
                c.isActif(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }

    private static String normalizeCode(String raw) {
        return raw.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
    }
}
