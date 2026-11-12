package com.m2i.pti.service.dto;


import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the Document entity.
 */
public class DocumentDTO implements Serializable {

    private Long id;

    @NotNull
    @Lob
    private byte[] valeur;
    private String valeurContentType;

    private Long typeDocumentId;

    private String typeDocumentLibelle;

    private String typeDocumentCode;

    private String imageFolder;

    private Long titulaireId;

    public Long getTitulaireId() {
        return titulaireId;
    }

    public void setTitulaireId(Long titulaireId) {
        this.titulaireId = titulaireId;
    }

    private String clientCodeClient;

    public String getTypeDocumentLibelle() {
        return typeDocumentLibelle;
    }

    public void setTypeDocumentLibelle(String typeDocumentLibelle) {
        this.typeDocumentLibelle = typeDocumentLibelle;
    }

    public String getClientCodeClient() {
        return clientCodeClient;
    }

    public void setClientCodeClient(String clientCodeClient) {
        this.clientCodeClient = clientCodeClient;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getValeur() {
        return valeur;
    }

    public void setValeur(byte[] valeur) {
        this.valeur = valeur;
    }

    public String getValeurContentType() {
        return valeurContentType;
    }

    public void setValeurContentType(String valeurContentType) {
        this.valeurContentType = valeurContentType;
    }

    public Long getTypeDocumentId() {
        return typeDocumentId;
    }

    public void setTypeDocumentId(Long typeDocumentId) {
        this.typeDocumentId = typeDocumentId;
    }

    public String getTypeDocumentCode() {
        return typeDocumentCode;
    }

    public void setTypeDocumentCode(String typeDocumentCode) {
        this.typeDocumentCode = typeDocumentCode;
    }

    public String getImageFolder() {
        return imageFolder;
    }

    public void setImageFolder(String imageFolder) {
        this.imageFolder = imageFolder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DocumentDTO documentDTO = (DocumentDTO) o;
        if(documentDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), documentDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "DocumentDTO{" +
            "id=" + getId() +
            ", valeur='" + getValeur() + "'" +
            "}";
    }
}
