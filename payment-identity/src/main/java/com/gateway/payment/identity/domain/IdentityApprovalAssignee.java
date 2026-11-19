package com.gateway.payment.identity.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/** Qui valide un niveau (L1, L2) d'une politique. */
@Entity
@Table(name = "identity_approval_assignee", uniqueConstraints = {
        @UniqueConstraint(name = "uq_identity_approval_assignee", columnNames = {"policy_id", "level"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdentityApprovalAssignee {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "policy_id", nullable = false)
    private UUID policyId;

    @Column(nullable = false, length = 8)
    private String level;

    @Column(name = "assignee_type", nullable = false, length = 16)
    private String assigneeType;

    @Column(name = "assignee_value", nullable = false, length = 64)
    private String assigneeValue;

    @Column(name = "assignee_scope", nullable = false, length = 16)
    private String assigneeScope;
}
