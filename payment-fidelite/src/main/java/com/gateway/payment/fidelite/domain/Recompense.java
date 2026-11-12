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

import java.util.UUID;

/** Récompense échangeable contre des points, dans un programme. */
@Entity
@Table(
        name = "recompense",
        uniqueConstraints = @UniqueConstraint(name = "recompense_programme_code_uk", columnNames = {"programme_id", "code"})
)
@Getter
@Setter
@NoArgsConstructor
public class Recompense {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programme_id", nullable = false)
    private Programme programme;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 120)
    private String titre;

    @Column(nullable = false, length = 240)
    private String description;

    @Column(nullable = false)
    private int cout;

    @Column(nullable = false, length = 80)
    private String icone;

    @Column(nullable = false, length = 24)
    private String categorie;

    @Column(nullable = false)
    private int position;
}
