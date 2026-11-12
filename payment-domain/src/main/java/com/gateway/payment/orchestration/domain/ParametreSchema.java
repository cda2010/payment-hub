package com.gateway.payment.orchestration.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/** Binding paramètre utilisateur → paramètre méthode pour une ligne de schéma. */
@Entity
@Table(name = "parametre_schema")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametreSchema {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ligne_schema_ecriture_id", nullable = false)
    private LigneSchemaEcriture ligneSchemaEcriture;

    @Column(name = "nom_parametre", length = 150)
    private String nomParametre;

    @Column(name = "code_parametre", nullable = false, length = 100)
    private String codeParametre;

    @Column(name = "code_parametre_methode", length = 100)
    private String codeParametreMethode;

    @Column(name = "nom_externe", length = 100)
    private String nomExterne;

    @Column(name = "type_parametre", length = 50)
    private String typeParametre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private ParametreCategorie categorie = ParametreCategorie.USER;

    @Column(name = "valeur_defaut", length = 500)
    private String valeurDefaut;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (categorie == null) {
            categorie = ParametreCategorie.USER;
        }
    }
}
