package com.gateway.payment.identity.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/** Seuil d'une politique : montant, nombre de lignes, ou règle finale. */
@Entity
@Table(name = "identity_approval_rule", uniqueConstraints = {
        @UniqueConstraint(name = "uq_identity_approval_rule", columnNames = {"policy_id", "code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdentityApprovalRule {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "policy_id", nullable = false)
    private UUID policyId;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 160)
    private String label;

    @Column(nullable = false)
    private int position;

    @Column(name = "total_amount_lte")
    private Long totalAmountLte;

    @Column(name = "line_count_lte")
    private Integer lineCountLte;

    @Column(name = "max_line_amount_lte")
    private Long maxLineAmountLte;

    @Column(name = "else_rule", nullable = false)
    @Builder.Default
    private boolean elseRule = false;

    @Column(nullable = false, length = 32)
    private String profile;
}
