package com.gateway.payment.catalog.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Version d'un service catalogue (ex. ACCOUNT_TRANSFER v1.0 / v1.1 / v2.0).
 * Toute modification importante crée une nouvelle version ; une transaction
 * conserve la référence exacte de la version utilisée.
 */
@Entity
@Table(
        name = "catalog_de_service_version",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_catalog_service_version",
                columnNames = {"catalog_de_service_id", "version"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogDeServiceVersion {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "catalog_de_service_id", nullable = false)
    private CatalogDeService catalogDeService;

    /** Numéro de version sémantique, ex. {@code 1.0}, {@code 1.1}, {@code 2.0}. */
    @Column(nullable = false, length = 20)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private CatalogDeServiceStatus status = CatalogDeServiceStatus.DRAFT;

    @Column(name = "effective_from")
    private Instant effectiveFrom;

    @Column(name = "effective_to")
    private Instant effectiveTo;

    /** Clé BPMN / workflow associée à cette version. */
    @Column(name = "workflow_key", length = 120)
    private String workflowKey;

    /** Configuration produit versionnée (JSON libre). */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> configuration = new LinkedHashMap<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = CatalogDeServiceStatus.DRAFT;
        }
        if (configuration == null) {
            configuration = new LinkedHashMap<>();
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
