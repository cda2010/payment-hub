package com.gateway.payment.fidelite.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/** Récompense du catalogue, indépendante des programmes qui la proposent. */
@Entity
@Table(
        name = "catalogue_recompense",
        uniqueConstraints = @UniqueConstraint(
                name = "catalogue_recompense_tenant_code_uk",
                columnNames = {"tenant_id", "code"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class CatalogueRecompense {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "tenant_id", nullable = false, length = 64)
    private String tenantId;

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

    @Column(name = "type_recompense", nullable = false, length = 30)
    private String typeRecompense = "CATALOGUE";
}
