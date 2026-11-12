package com.gateway.payment.divers.domain;

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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "budget",
        uniqueConstraints = @UniqueConstraint(name = "budget_party_categorie_uk", columnNames = {"party_id", "categorie_code"})
)
@Getter
@Setter
@NoArgsConstructor
public class Budget {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "party_id", nullable = false)
    private UUID partyId;

    @Column(nullable = false, length = 120)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categorie_code", nullable = false)
    private CategorieDepense categorie;

    @Column(name = "limit_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal limitAmount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
