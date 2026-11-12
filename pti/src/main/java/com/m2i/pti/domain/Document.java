package com.m2i.pti.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A Document.
 */
@Entity
@Table(name = "document")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Document extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //@NotNull
    @Lob
    @Column(name = "valeur")
    private byte[] valeur;

    @Column(name = "valeur_content_type")
    private String valeurContentType;

    @Column(name = "image_folder")
    private String imageFolder;


    @ManyToOne
    private TypeDocument typeDocument;


    @ManyToOne
    private Client titulaire;

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getValeur() {
        return valeur;
    }

    public Document valeur(byte[] valeur) {
        this.valeur = valeur;
        return this;
    }

    public void setValeur(byte[] valeur) {
        this.valeur = valeur;
    }

    public String getValeurContentType() {
        return valeurContentType;
    }

    public Document valeurContentType(String valeurContentType) {
        this.valeurContentType = valeurContentType;
        return this;
    }

    public void setValeurContentType(String valeurContentType) {
        this.valeurContentType = valeurContentType;
    }

    public TypeDocument getTypeDocument() {
        return typeDocument;
    }

    public Document typeDocument(TypeDocument typeDocument) {
        this.typeDocument = typeDocument;
        return this;
    }

    public void setTypeDocument(TypeDocument typeDocument) {
        this.typeDocument = typeDocument;
    }

    public Client getTitulaire() {
        return titulaire;
    }

    public void setTitulaire(Client titulaire) {
        this.titulaire = titulaire;
    }

    public String getImageFolder() {
        return imageFolder;
    }

    public void setImageFolder(String imageFolder) {
        this.imageFolder = imageFolder;
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
        Document document = (Document) o;
        if (document.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), document.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Document{" +
            "id=" + getId() +
            ", valeur='" + getValeur() + "'" +
            ", valeurContentType='" + valeurContentType + "'" +
            "}";
    }
}
