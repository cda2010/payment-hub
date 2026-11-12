package com.m2i.pti.service.dto;


import com.m2i.pti.domain.enumeration.TypePersonne;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A DTO for the Lien entity.
 */
public class LienDTO implements Serializable {

    private Long id;

    private Long lienPotentiel;

    private Boolean valider;

    private Long personneId;

    private String personneNom;

    private String personnePrenom;

    private TypePersonne personneType;

    private String personneRaisonSocial;

    private String personneIfu;

    private String personneTelephone;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLienPotentiel() {
        return lienPotentiel;
    }

    public void setLienPotentiel(Long lienPotentiel) {
        this.lienPotentiel = lienPotentiel;
    }

    public Boolean isValider() {
        return valider;
    }

    public void setValider(Boolean valider) {
        this.valider = valider;
    }

    public Long getPersonneId() {
        return personneId;
    }

    public void setPersonneId(Long personneId) {
        this.personneId = personneId;
    }

    public String getPersonneNom() {
        return personneNom;
    }

    public void setPersonneNom(String personneNom) {
        this.personneNom = personneNom;
    }

    public String getPersonnePrenom() {
        return personnePrenom;
    }

    public void setPersonnePrenom(String personnePrenom) {
        this.personnePrenom = personnePrenom;
    }

    public TypePersonne getPersonneType() {
        return personneType;
    }

    public void setPersonneType(TypePersonne personneType) {
        this.personneType = personneType;
    }

    public String getPersonneRaisonSocial() {
        return personneRaisonSocial;
    }

    public void setPersonneRaisonSocial(String personneRaisonSocial) {
        this.personneRaisonSocial = personneRaisonSocial;
    }

    public String getPersonneIfu() {
        return personneIfu;
    }

    public void setPersonneIfu(String personneIfu) {
        this.personneIfu = personneIfu;
    }

    public String getPersonneTelephone() {
        return personneTelephone;
    }

    public void setPersonneTelephone(String personneTelephone) {
        this.personneTelephone = personneTelephone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LienDTO lienDTO = (LienDTO) o;
        if(lienDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), lienDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "LienDTO{" +
            "id=" + getId() +
            ", lienPotentiel='" + getLienPotentiel() + "'" +
                ",personneId='"+getPersonneId()+"'"+
            ", valider='" + isValider() + "'" +
            "}";
    }
}
