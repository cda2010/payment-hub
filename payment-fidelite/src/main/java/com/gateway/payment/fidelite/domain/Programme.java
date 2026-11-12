package com.gateway.payment.fidelite.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "programme")
@Getter
@Setter
@NoArgsConstructor
public class Programme {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 160)
    private String nom;

    @Column(name = "unit_code", nullable = false, length = 30)
    private String unitCode = "POINT";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ProgrammePortee portee;

    @Column(name = "unite_id")
    private UUID uniteId;

    @Column(name = "unite_nom", length = 160)
    private String uniteNom;

    @Column(name = "points_par_mille", nullable = false)
    private int pointsParMille;

    @Column(name = "points_parrain", nullable = false)
    private int pointsParrain;

    @Column(name = "points_filleul", nullable = false)
    private int pointsFilleul;

    /** La prime attend un KYC validé. Les points restent ceux du programme. */
    @Column(name = "referral_kyc_required", nullable = false)
    private boolean referralKycRequired = true;

    @Column(name = "referral_min_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal referralMinAmount = new BigDecimal("5000");

    @Column(name = "referral_currency", nullable = false, length = 3)
    private String referralCurrency = "XOF";

    /** Délai après la saisie du code. Zéro lève la limite de temps. */
    @Column(name = "referral_qualification_days", nullable = false)
    private int referralQualificationDays = 30;

    @Column(name = "referral_max_per_month", nullable = false)
    private int referralMaxPerMonth = 20;

    /** Produits ou services autorisés, séparés par des virgules. Vide : tous. */
    @Column(name = "referral_products", length = 400)
    private String referralProducts;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ProgrammeStatut statut;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "start_at")
    private Instant startAt;

    @Column(name = "end_at")
    private Instant endAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "programme_profil", joinColumns = @JoinColumn(name = "programme_id"))
    @Column(name = "profile_code", nullable = false, length = 64)
    private Set<String> profils = new HashSet<>();
}
