package com.gateway.payment.divers.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categorie_depense")
@Getter
@Setter
@NoArgsConstructor
public class CategorieDepense {

    @Id
    @Column(nullable = false, length = 40)
    private String code;

    @Column(nullable = false, length = 80)
    private String label;

    @Column(nullable = false, length = 64)
    private String icon;

    @Column(nullable = false)
    private int priority;
}
