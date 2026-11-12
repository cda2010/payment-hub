package com.gateway.payment.pricing.tarif.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Variable déclarée par une formule : type, obligation, valeur par défaut et contraintes (EF-005). */
@Entity
@Table(name = "formule_parametre")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormuleParametre {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "formule_id", nullable = false)
    private Formule formule;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 150)
    private String libelle;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_donnee", nullable = false, length = 16)
    private TypeDonnee typeDonnee;

    @Column(nullable = false)
    private boolean obligatoire;

    /** Texte converti selon {@link #typeDonnee} ; appliqué seulement si le paramètre est facultatif. */
    @Column(name = "valeur_defaut", length = 100)
    private String valeurDefaut;

    @Column(name = "valeur_min", precision = 24, scale = 8)
    private BigDecimal valeurMin;

    @Column(name = "valeur_max", precision = 24, scale = 8)
    private BigDecimal valeurMax;

    /** Nombre maximal de décimales d'une valeur DECIMAL. */
    @Column(name = "nb_decimales")
    private Integer nbDecimales;

    /** MONTANT (devise de la grille), TAUX (forme décimale, affiché en %), ou unité libre. */
    @Column(length = 16)
    private String unite;

    @Column(nullable = false)
    private int ordre;

    @Column(length = 500)
    private String aide;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "valeurs_autorisees")
    private List<String> valeursAutorisees;

    @Column(name = "longueur_max")
    private Integer longueurMax;

    /** Expression régulière que doit respecter une valeur STRING. */
    @Column(length = 200)
    private String format;
}
