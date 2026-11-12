package com.m2i.omnicanal.application.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Etiquette.
 */
@Entity
@Table(name = "etiquette")

public class Etiquette implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_etiquette")
    private String codeEtiquette;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodeEtiquette() {
        return codeEtiquette;
    }

    public Etiquette codeEtiquette(String codeEtiquette) {
        this.codeEtiquette = codeEtiquette;
        return this;
    }

    public void setCodeEtiquette(String codeEtiquette) {
        this.codeEtiquette = codeEtiquette;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Etiquette etiquette = (Etiquette) o;
        if (etiquette.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, etiquette.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Etiquette{" +
            "id=" + id +
            ", codeEtiquette='" + codeEtiquette + "'" +
            '}';
    }
}
