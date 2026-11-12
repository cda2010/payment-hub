package com.m2i.pti.service.dto;



import com.m2i.pti.domain.enumeration.Destination;
import com.m2i.pti.domain.enumeration.Langue;
import com.m2i.pti.domain.enumeration.TypeServiceLibelle;

import java.io.Serializable;
import java.util.Objects;


/**
 * A DTO for the TemplateMessage entity.
 */
public class TemplateMessageDTO implements Serializable {

    private Long id;

    private String etiquette;

    private String contenu;

    private Langue langue;
    private Long fseId;
    private String fseLibelleUniteOrganisation;

    private Destination destination;
    private TypeServiceLibelle typeServiceLibelle;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEtiquette() {
        return etiquette;
    }

    public void setEtiquette(String etiquette) {
        this.etiquette = etiquette;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public Langue getLangue() {
        return langue;
    }

    public void setLangue(Langue langue) {
        this.langue = langue;
    }

    public Long getFseId() {
        return fseId;
    }

    public void setFseId(Long fseId) {
        this.fseId = fseId;
    }

    public String getFseLibelleUniteOrganisation() {
        return fseLibelleUniteOrganisation;
    }

    public void setFseLibelleUniteOrganisation(String fseLibelleUniteOrganisation) {
        this.fseLibelleUniteOrganisation = fseLibelleUniteOrganisation;
    }


    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public TypeServiceLibelle getTypeServiceLibelle() {
        return typeServiceLibelle;
    }

    public void setTypeServiceLibelle(TypeServiceLibelle typeServiceLibelle) {
        this.typeServiceLibelle = typeServiceLibelle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TemplateMessageDTO templateMessageDTO = (TemplateMessageDTO) o;
        if(templateMessageDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), templateMessageDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TemplateMessageDTO{" +
            "id=" + getId() +
            ", etiquette='" + getEtiquette() + "'" +
            ", contenu='" + getContenu() + "'" +
            ", langue='" + getLangue() + "'" +
            "}";
    }
}
