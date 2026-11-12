package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A ExportTable.
 */
@Entity
@Table(name = "export_table")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ExportTable implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "description")
    private String description;

    @Column(name = "nom_fichier")
    private String nomFichier;
    
    
    @Column(name = "output_filename")
    private String outputFilename;
    
    @Column(name = "libelle")
    private String libelle;
   

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNomFichier() {
        return this.nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public String getOutputFilename() {
        return this.outputFilename;
    }

    public void setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename;
    }

    public String getLibelle() {
        return this.libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
    

    @Override
    public String toString() {
        return "{" +
            " id='" + getId() + "'" +
            ", description='" + getDescription() + "'" +
            ", nomFichier='" + getNomFichier() + "'" +
            ", outputFilename='" + getOutputFilename() + "'" +
            ", libelle='" + getLibelle() + "'" +
            "}";
    }

   

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExportTable exportTable = (ExportTable) o;
        if (exportTable.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), exportTable.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

}
