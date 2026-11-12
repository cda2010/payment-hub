package com.gateway.payment.commission.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "commission_regle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegleCommission {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grille_id", nullable = false)
    private GrilleCommission grille;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 200)
    private String libelle;

    @Column(name = "beneficiary_role", nullable = false, length = 64)
    private String beneficiaryRole;

    @Column(name = "beneficiary_owner_kind", length = 16)
    private String beneficiaryOwnerKind;

    @Column(name = "beneficiary_owner_id")
    private UUID beneficiaryOwnerId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "formule_id", nullable = false)
    private FormuleCommission formule;

    @Enumerated(EnumType.STRING)
    @Column(name = "commission_base", nullable = false, length = 32)
    @Builder.Default
    private SourceCommission commissionBase = SourceCommission.TRANSACTION_FEES;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier_selection_source", length = 32)
    private SourceCommission tierSelectionSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "funding_type", nullable = false, length = 32)
    @Builder.Default
    private FundingType fundingType = FundingType.TRANSACTION_FEES;

    @Column(name = "funder_role", length = 64)
    private String funderRole;

    @Column(name = "params_json", nullable = false, columnDefinition = "TEXT")
    @Builder.Default
    private String paramsJson = "{}";

    @Column(name = "floor_amount", precision = 18, scale = 4)
    private BigDecimal floorAmount;

    @Column(name = "cap_amount", precision = 18, scale = 4)
    private BigDecimal capAmount;

    @Column(nullable = false)
    @Builder.Default
    private int priority = 100;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
