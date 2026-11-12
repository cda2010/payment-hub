package com.gateway.payment.orchestration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Étiquette de compte (aligné Omnicanal {@code TypeCompte} / {@code codeParametre}).
 * Ex. {@code CPTELORO}, {@code CPTEMIROIR}.
 */
@Entity
@Table(name = "type_compte")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeCompte {

    @Id
    @Column(name = "code_parametre", length = 64, nullable = false)
    private String codeParametre;

    @Column(nullable = false, length = 150)
    private String libelle;
}
