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

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "coalition_membre",
        uniqueConstraints = @UniqueConstraint(
                name = "coalition_membre_pk",
                columnNames = {"programme_id", "marchand_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class CoalitionMembre {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programme_id", nullable = false)
    private Programme programme;

    @Column(name = "marchand_id", nullable = false)
    private UUID marchandId;

    @Column(name = "part_pct", nullable = false, precision = 6, scale = 2)
    private BigDecimal partPct;
}
