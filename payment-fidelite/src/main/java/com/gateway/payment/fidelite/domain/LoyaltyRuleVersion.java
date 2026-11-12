package com.gateway.payment.fidelite.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/** Version publiée. Une ligne écrite ne se réécrit pas : un changement crée la version suivante. */
@Entity
@Table(
        name = "loyalty_rule_version",
        uniqueConstraints = @UniqueConstraint(name = "loyalty_rule_version_uk", columnNames = {"rule_id", "version_no"})
)
@Getter
@Setter
@NoArgsConstructor
public class LoyaltyRuleVersion {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rule_id", nullable = false)
    private LoyaltyRule rule;

    @Column(name = "version_no", nullable = false)
    private int versionNo;

    @Column(nullable = false)
    private int priority;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String definition;

    @Column(name = "published_at", nullable = false)
    private Instant publishedAt;
}
