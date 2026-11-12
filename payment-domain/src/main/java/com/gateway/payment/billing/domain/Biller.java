package com.gateway.payment.billing.domain;

import com.gateway.payment.configuration.domain.PaymentProvider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "biller")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Biller {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(nullable = false)
    private boolean actif;

    @Column(name = "default_currency", nullable = false, length = 10)
    @Builder.Default
    private String defaultCurrency = "XOF";

    @Column(name = "ordre_affichage")
    private Integer ordreAffichage;

    /**
     * Code catégorie navigation ({@link BillerCategory}), ex. {@code ENERGY_WATER}.
     */
    @Column(name = "category_code", length = 40)
    @Builder.Default
    private String categoryCode = BillerCategory.OTHER.name();

    /**
     * Fournisseur de service de type {@code FACTURIER} qui expose les APIs
     * LOOKUP / acquittement pour ce facturier catalogue.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id")
    private PaymentProvider provider;

    /** Code FSE omnicanal ({@code fournisseur_service.code_fse}) quand il n'y a pas de ligne locale. */
    @Column(name = "provider_code", length = 40)
    private String providerCode;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "biller", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordreAffichage ASC, name ASC")
    @Builder.Default
    private List<BillerProduct> products = new ArrayList<>();

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
