package com.gateway.payment.network.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Unité du réseau. Dans l'arbre, {@code chemin} liste les identifiants depuis le pays, l'unité comprise
 * ({@code /idPays/idFse/idAgence/}) : les descendants d'une unité sont les lignes dont le chemin commence par le sien.
 * Un regroupement se rattache à une unité de n'importe quel niveau comme les autres, mais n'a jamais d'enfant : il a
 * des membres, pris dans la branche de son parent.
 */
@Entity
@Table(name = "unite_organisation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniteOrganisation {

    /** Attribué à la création pour calculer le chemin avant l'insertion. */
    @Id
    private UUID id;

    @Column(nullable = false, length = 40, updatable = false)
    private String code;

    @Column(nullable = false, length = 150)
    private String libelle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeUnite type;

    /**
     * Code FSE Omnicanal (ex. {@code 0011} Coris Money). Renseigné uniquement si {@link TypeUnite#FSE} ;
     * fixé à la création.
     */
    @Column(name = "code_fse", length = 20, updatable = false)
    private String codeFse;

    /** Mobile money ou banque. Renseigné pour un FSE que l'enrôlement peut proposer. */
    @Enumerated(EnumType.STRING)
    @Column(name = "famille_fse", length = 16)
    private FamilleFse familleFse;

    /** Code de la {@link Zone}. */
    @Column(nullable = false, length = 10, updatable = false)
    private String zone;

    @Column(name = "parent_id")
    private UUID parentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatutUnite statut;

    @Column(nullable = false)
    private int niveau;

    @Column(nullable = false, length = 400)
    private String chemin;

    /** Null tant que l'unité n'est pas enregistrée : Spring Data fait alors un persist, pas un merge. */
    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "created_by", nullable = false, length = 100, updatable = false)
    private String createdBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by", nullable = false, length = 100)
    private String updatedBy;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public boolean estActive() {
        return statut == StatutUnite.ACTIF;
    }

    /** Vrai si {@code autre} est dans la branche de cette unité, elle-même exclue. */
    public boolean estAscendantDe(UniteOrganisation autre) {
        return estAscendantDe(autre.getChemin());
    }

    public boolean estAscendantDe(String cheminAutre) {
        return !type.estRegroupement() && cheminAutre.startsWith(chemin) && !cheminAutre.equals(chemin);
    }

    /** Chemin du parent ; vide pour un pays. */
    public String cheminParent() {
        return chemin.substring(0, chemin.length() - id.toString().length() - 1);
    }

    /** Identifiants des ascendants, du pays au parent direct. */
    public List<UUID> idsAscendants() {
        List<UUID> ids = Arrays.stream(chemin.split("/"))
                .filter(s -> !s.isEmpty())
                .map(UUID::fromString)
                .toList();
        return ids.subList(0, ids.size() - 1);
    }

    public String libelleCourt() {
        return type.libelle() + " " + code;
    }
}
