package com.m2i.omnicanal.application.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Etiquette.
 */
@Entity
@Table(name = "etiquette_methode")

public class EtiquetteMethode implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "libelle")
    private String libelle;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public String toString() {
        return "EtiquetteMethode{" +
            "id=" + id +
            ", code='" + code + '\'' +
            ", libelle='" + libelle + '\'' +
            '}';
    }
}
