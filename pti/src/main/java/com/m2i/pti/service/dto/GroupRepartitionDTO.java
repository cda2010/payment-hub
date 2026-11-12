package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the GroupRepartition entity.
 */
public class GroupRepartitionDTO implements Serializable {

    private Long id;

    private String libelleGroup;
    private Long typeServiceId;
    private  String typeServiceLibelle;

    public GroupRepartitionDTO() {
    }

    public GroupRepartitionDTO(Long id, String libelleGroup, Long typeServiceId, String typeServiceLibelle) {
        this.id = id;
        this.libelleGroup = libelleGroup;
        this.typeServiceId = typeServiceId;
        this.typeServiceLibelle = typeServiceLibelle;
    }

    public GroupRepartitionDTO(Long id, String libelleGroup, Long typeServiceId) {
        this.id = id;
        this.libelleGroup = libelleGroup;
        this.typeServiceId = typeServiceId;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelleGroup() {
        return libelleGroup;
    }

    public void setLibelleGroup(String libelleGroup) {
        this.libelleGroup = libelleGroup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GroupRepartitionDTO groupRepartitionDTO = (GroupRepartitionDTO) o;
        if(groupRepartitionDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), groupRepartitionDTO.getId());
    }

    public Long getTypeServiceId() {
        return typeServiceId;
    }

    public void setTypeServiceId(Long typeServiceId) {
        this.typeServiceId = typeServiceId;
    }

    public String getTypeServiceLibelle() {
        return typeServiceLibelle;
    }

    public void setTypeServiceLibelle(String typeServiceLibelle) {
        this.typeServiceLibelle = typeServiceLibelle;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "GroupRepartitionDTO{" +
            "id=" + getId() +
            ", libelleGroup='" + getLibelleGroup() + "'" +
            "}";
    }
}
