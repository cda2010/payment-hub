package com.gateway.payment.common.i18n;

import com.gateway.payment.i18n.domain.I18nTranslation;
import com.gateway.payment.i18n.repository.I18nTranslationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Lecture / écriture des traductions centralisées ({@code i18n_translation}).
 * <p>
 * Le texte porté par la colonne d'origine (ex. {@code catalog_de_service.name}) reste la
 * valeur de repli : une traduction absente pour la langue demandée ne masque jamais le libellé.
 */
@Service
public class TranslationService {

    private final I18nTranslationRepository repository;
    private final GatewayI18nService i18nService;

    public TranslationService(I18nTranslationRepository repository, GatewayI18nService i18nService) {
        this.repository = repository;
        this.i18nService = i18nService;
    }

    public String defaultLanguage() {
        return i18nService.properties().normalizeOrDefault(null);
    }

    public List<String> supportedLanguages() {
        return i18nService.properties().supportedCodes();
    }

    /** Toutes les traductions d'une entité, résolues pour la langue de la requête courante. */
    @Transactional(readOnly = true)
    public Bundle bundle(String entityType, UUID entityId) {
        if (entityId == null) {
            return Bundle.empty(i18nService.currentLanguage());
        }
        return toBundle(repository.findByEntityTypeAndEntityId(entityType, entityId));
    }

    /** Chargement groupé — évite un N+1 sur les endpoints de liste. */
    @Transactional(readOnly = true)
    public Map<UUID, Bundle> bundles(String entityType, Collection<UUID> entityIds) {
        String language = i18nService.currentLanguage();
        Map<UUID, Bundle> out = new LinkedHashMap<>();
        if (entityIds == null || entityIds.isEmpty()) {
            return out;
        }
        Map<UUID, List<I18nTranslation>> grouped = new LinkedHashMap<>();
        for (I18nTranslation row : repository.findByEntityTypeAndEntityIdIn(entityType, entityIds)) {
            grouped.computeIfAbsent(row.getEntityId(), k -> new ArrayList<>()).add(row);
        }
        for (UUID id : entityIds) {
            List<I18nTranslation> rows = grouped.get(id);
            out.put(id, rows == null ? Bundle.empty(language) : toBundle(rows));
        }
        return out;
    }

    /**
     * Enregistre les libellés d'un champ. Les langues absentes de {@code byLang} sont supprimées,
     * les langues hors {@code gateway.i18n.supported-languages} sont ignorées.
     */
    @Transactional
    public void save(String entityType, UUID entityId, String fieldPath, Map<String, String> byLang) {
        Objects.requireNonNull(entityId, "entityId");
        Map<String, String> wanted = i18nService.filterSupportedMessages(byLang);
        List<I18nTranslation> existing =
                repository.findByEntityTypeAndEntityIdAndFieldPath(entityType, entityId, fieldPath);
        if (wanted == null) {
            repository.deleteAll(existing);
            return;
        }
        Map<String, I18nTranslation> byLangExisting = new LinkedHashMap<>();
        for (I18nTranslation row : existing) {
            byLangExisting.put(row.getLang(), row);
        }
        List<I18nTranslation> toSave = new ArrayList<>();
        wanted.forEach((lang, text) -> {
            I18nTranslation row = byLangExisting.remove(lang);
            if (row == null) {
                row = I18nTranslation.builder()
                        .entityType(entityType)
                        .entityId(entityId)
                        .fieldPath(fieldPath)
                        .lang(lang)
                        .textValue(text)
                        .build();
            } else {
                row.setTextValue(text);
            }
            toSave.add(row);
        });
        repository.saveAll(toSave);
        repository.deleteAll(byLangExisting.values());
    }

    /** Enregistre plusieurs champs d'un coup : {@code fieldPath → {lang → texte}}. */
    @Transactional
    public void saveAll(String entityType, UUID entityId, Map<String, Map<String, String>> byPath) {
        if (byPath == null || byPath.isEmpty()) {
            return;
        }
        byPath.forEach((fieldPath, byLang) -> {
            if (fieldPath != null && !fieldPath.isBlank()) {
                save(entityType, entityId, fieldPath.trim(), byLang);
            }
        });
    }

    @Transactional
    public void deleteScope(String entityType, UUID entityId) {
        if (entityId != null) {
            repository.deleteByEntityTypeAndEntityId(entityType, entityId);
        }
    }

    /**
     * Complète la langue par défaut avec le libellé de la colonne d'origine, de sorte
     * qu'un produit créé sans {@code nameI18n} reste cohérent en base.
     */
    public Map<String, String> withDefaultLanguage(Map<String, String> byLang, String defaultText) {
        Map<String, String> normalized = i18nService.filterSupportedMessages(byLang);
        Map<String, String> out = normalized == null ? new LinkedHashMap<>() : new LinkedHashMap<>(normalized);
        if (defaultText != null && !defaultText.isBlank()) {
            out.putIfAbsent(defaultLanguage(), defaultText.trim());
        }
        return out.isEmpty() ? null : out;
    }

    private Bundle toBundle(List<I18nTranslation> rows) {
        Map<String, Map<String, String>> byPath = new LinkedHashMap<>();
        for (I18nTranslation row : rows) {
            byPath.computeIfAbsent(row.getFieldPath(), k -> new LinkedHashMap<>())
                    .put(row.getLang(), row.getTextValue());
        }
        return new Bundle(i18nService.currentLanguage(), byPath);
    }

    /** Traductions d'une entité + langue de la requête courante. */
    public record Bundle(String language, Map<String, Map<String, String>> byPath) {

        public static Bundle empty(String language) {
            return new Bundle(language, Map.of());
        }

        /** Texte pour la langue courante, ou {@code fallback} si aucune traduction. */
        public String text(String fieldPath, String fallback) {
            String resolved = LocalizedMessages.resolve(byPath.get(fieldPath), language);
            return resolved != null ? resolved : fallback;
        }

        /** Map complète {@code {lang → texte}} pour l'admin, {@code null} si aucune traduction. */
        public Map<String, String> messages(String fieldPath) {
            Map<String, String> messages = byPath.get(fieldPath);
            return messages == null || messages.isEmpty() ? null : Map.copyOf(messages);
        }
    }
}
