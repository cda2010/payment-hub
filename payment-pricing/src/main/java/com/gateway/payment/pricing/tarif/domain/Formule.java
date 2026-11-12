package com.gateway.payment.pricing.tarif.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Version d'une formule du catalogue et son schéma de paramètres. Une version utilisée n'est jamais modifiée. */
@Entity
@Table(name = "formule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Formule {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 150)
    private String libelle;

    @Column(nullable = false)
    private int version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TypeFormule type;

    @Column(length = 500)
    private String expression;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StatutFormule statut;

    @OneToMany(mappedBy = "formule", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("ordre ASC")
    @Builder.Default
    private List<FormuleParametre> parametres = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public boolean estActive() {
        return statut == StatutFormule.ACTIVE;
    }

    public String libelleVersion() {
        return code + " v" + version;
    }
}
