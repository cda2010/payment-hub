package com.gateway.payment.i18n.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Traduction d'un texte visible utilisateur, adressée par
 * {@code (entityType, entityId, fieldPath, lang)}.
 * <p>
 * {@code entityId} null = clé globale (non rattachée à une ligne), ex. libellés partagés.
 * Les colonnes d'origine ({@code catalog_de_service.name}…) restent la valeur de repli
 * quand aucune traduction n'existe pour la langue demandée.
 */
@Entity
@Table(name = "i18n_translation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class I18nTranslation {

    @Id
    @UuidGenerator
    private UUID id;

    /** Portée : {@code CATALOG_DE_SERVICE}, {@code CATEGORIE_SERVICE}, {@code CATALOG_DE_SERVICE_VERSION}… */
    @Column(name = "entity_type", nullable = false, length = 60)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    /**
     * Chemin du texte traduit, ex. {@code name}, {@code description},
     * {@code configuration.roles[SOURCE].label}, {@code configuration.recap.fields[amount].label}.
     */
    @Column(name = "field_path", nullable = false, length = 200)
    private String fieldPath;

    /** Code langue normalisé (minuscules, sans région) : {@code fr}, {@code en}. */
    @Column(nullable = false, length = 10)
    private String lang;

    @Column(name = "text_value", nullable = false, columnDefinition = "TEXT")
    private String textValue;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
