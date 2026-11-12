package com.gateway.payment.catalog.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entrée du catalogue de services produit (intention métier),
 * indépendante des fournisseurs d'exécution.
 * Regroupée dans une {@link CategorieService}.
 */
@Entity
@Table(name = "catalog_de_service")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogDeService {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true, length = 60)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categorie_id", nullable = false)
    private CategorieService categorie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private CatalogDeServiceStatus status = CatalogDeServiceStatus.DRAFT;

    /**
     * Version courante (typiquement la dernière PUBLISHED effective).
     * Les transactions pointent vers une version précise, pas vers ce pointeur.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_version_id")
    private CatalogDeServiceVersion currentVersion;

    @OneToMany(mappedBy = "catalogDeService", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<CatalogDeServiceVersion> versions = new ArrayList<>();

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
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addVersion(CatalogDeServiceVersion version) {
        versions.add(version);
        version.setCatalogDeService(this);
    }
}
