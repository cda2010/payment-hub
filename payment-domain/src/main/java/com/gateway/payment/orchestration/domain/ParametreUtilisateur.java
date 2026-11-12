package com.gateway.payment.orchestration.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

/**
 * Paramètre attendu / template d'une {@link InterfaceUtilisateur} (constitues Omnicanal).
 * Sert au binding canal → codes méthode avant application de {@link ParametreSchema}.
 */
@Entity
@Table(
        name = "parametre_utilisateur",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_parametre_utilisateur_iface_code",
                columnNames = {"interface_utilisateur_id", "code_parametre"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametreUtilisateur {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "interface_utilisateur_id", nullable = false)
    private InterfaceUtilisateur interfaceUtilisateur;

    @Column(name = "nom_parametre", length = 150)
    private String nomParametre;

    @Column(name = "code_parametre", nullable = false, length = 100)
    private String codeParametre;

    /** Code attendu côté méthode FSE (ex. CPTSOURCE, MONTANT). */
    @Column(name = "code_parametre_methode", length = 100)
    private String codeParametreMethode;

    @Column(name = "type_parametre", length = 50)
    private String typeParametre;

    /** Valeur par défaut / fixe (optionnelle). */
    @Column(length = 500)
    private String valeur;

    @Enumerated(EnumType.STRING)
    @Column(name = "parametre_api", length = 30)
    private ProprieteApi parametreApi;

    /**
     * Règle d'extraction :
     * <ul>
     *   <li>{@code REGEX:pattern} ou {@code REGEX:pattern:group} — défaut payload</li>
     *   <li>codes compte ({@code NUMERO_COMPTE}, …) — lookup {@link CompteFse}</li>
     *   <li>{@code SCRIPT} — voir {@link #extractScript}</li>
     * </ul>
     */
    @Column(length = 500)
    private String attribut;

    /**
     * Clé du champ composé dans le payload.
     * En pratique préférer {@link #codeParametreMethode} comme source ;
     * ce champ reste un override optionnel.
     */
    @Column(name = "source_parametre", length = 100)
    private String sourceParametre;

    /**
     * Script Groovy (règles complexes). Bindings : {@code source}, {@code params}.
     * Prioritaire sur REGEX lorsque renseigné.
     */
    @Column(name = "extract_script", columnDefinition = "TEXT")
    private String extractScript;

    /**
     * Clé i18n (ex. {@code bill.amount}). Utilisée si {@link #labelsJson} absente.
     */
    @Column(name = "label_key", length = 150)
    private String labelKey;

    /**
     * Libellés inline JSON {@code {"fr":"...","en":"..."}} (prioritaire sur {@link #labelKey}).
     */
    @Column(name = "labels_json", columnDefinition = "TEXT")
    private String labelsJson;

    /** Exposé UI / canal (aligné {@code MParam.affiche}). Défaut {@code true}. */
    @Column(nullable = false)
    @Builder.Default
    private boolean affiche = true;

    @Column(nullable = false)
    @Builder.Default
    private int ordre = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
