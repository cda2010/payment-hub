package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import com.m2i.pti.domain.CategorieService;
import com.m2i.pti.domain.enumeration.EtiquetteCompteCode;

/**
 * A DTO for the TypeService entity.
 */
public class TypeServiceDTO implements Serializable {

    private Long id;

    private String libelle;

    private Boolean cmpteClientSournce;

    private Boolean cmpteClientDest;

    private Boolean operationClient;

    private Boolean fraisApplicable;

    private String code;

    private String description;
 
    private EtiquetteCompteCode etiquetteCompteCode;

    private CategorieService categorieService;

    private Long catId;

    private String catCode;

    private String catLibelle;


    
 private List<String> action;
    public TypeServiceDTO() {
        
    }
           
    public TypeServiceDTO(Long id, String code, String libelle,  String description, 
            String etiquetteCompteCode, Boolean fraisApplicable,
            Boolean operationClient,Boolean cmpteClientSournce, Boolean cmpteClientDest , Long catId, String catCode, String catLibelle
            ) {
        this.id = id;
        this.libelle = libelle;
        this.cmpteClientSournce = cmpteClientSournce;
        this.cmpteClientDest = cmpteClientDest;
        this.operationClient = operationClient;
        this.fraisApplicable = fraisApplicable;
        this.code = code;
        this.description = description;
        if (etiquetteCompteCode!=null){
             this.etiquetteCompteCode = EtiquetteCompteCode.valueOf(etiquetteCompteCode);
        }
        this.catId = catId;
        this.catCode = catCode;
        this.catLibelle = catLibelle;
       
    }
    public TypeServiceDTO(Long id, String code, String libelle,  String description, 
            String etiquetteCompteCode, Boolean fraisApplicable,
            Boolean operationClient,Boolean cmpteClientSournce, Boolean cmpteClientDest 
            ) {
        this.id = id;
        this.libelle = libelle;
        this.cmpteClientSournce = cmpteClientSournce;
        this.cmpteClientDest = cmpteClientDest;
        this.operationClient = operationClient;
        this.fraisApplicable = fraisApplicable;
        this.code = code;
        this.description = description;
        if (etiquetteCompteCode!=null){
             this.etiquetteCompteCode = EtiquetteCompteCode.valueOf(etiquetteCompteCode);
        }
        
       
    }


    public TypeServiceDTO(Long id, String libelle, String code, String etiquetteCompteCode,Boolean operationClient) {
        this.id = id;
        this.libelle = libelle;
        this.code = code;
        this.etiquetteCompteCode = etiquetteCompteCode!=null? EtiquetteCompteCode.valueOf(etiquetteCompteCode):null;
        this.operationClient = operationClient;
    }





    public TypeServiceDTO(Long id, String code) {
        this.id = id;
        this.code = code;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EtiquetteCompteCode getEtiquetteCompteCode() {
        return etiquetteCompteCode;
    }

    public void setEtiquetteCompteCode(EtiquetteCompteCode etiquetteCompteCode) {
        this.etiquetteCompteCode = etiquetteCompteCode;
    }

    public Boolean getCmpteClientSournce() {
        return cmpteClientSournce;
    }

    public Boolean getCmpteClientDest() {
        return cmpteClientDest;
    }

    public Boolean getOperationClient() {
        return operationClient;
    }

    public Boolean getFraisApplicable() {
        return fraisApplicable;
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

    public Boolean isCmpteClientSournce() {
        return cmpteClientSournce;
    }

    public void setCmpteClientSournce(Boolean cmpteClientSournce) {
        this.cmpteClientSournce = cmpteClientSournce;
    }

    public Boolean isCmpteClientDest() {
        return cmpteClientDest;
    }

    public void setCmpteClientDest(Boolean cmpteClientDest) {
        this.cmpteClientDest = cmpteClientDest;
    }

    public Boolean isOperationClient() {
        return operationClient;
    }

    public void setOperationClient(Boolean operationClient) {
        this.operationClient = operationClient;
    }

    public Boolean isFraisApplicable() {
        return fraisApplicable;
    }

    public void setFraisApplicable(Boolean fraisApplicable) {
        this.fraisApplicable = fraisApplicable;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TypeServiceDTO typeServiceDTO = (TypeServiceDTO) o;
        if(typeServiceDTO.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), typeServiceDTO.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "TypeServiceDTO{" +
            "id=" + getId() +
            ", libelle='" + getLibelle() + "'" +
            ", cmpteClientSournce='" + isCmpteClientSournce() + "'" +
            ", cmpteClientDest='" + isCmpteClientDest() + "'" +
            ", operationClient='" + isOperationClient() + "'" +
            ", fraisApplicable='" + isFraisApplicable() + "'" +
            "}";
    }

    public List<String> getAction() {
        return action;
    }

    public void setAction(List<String> action) {
        this.action = action;
    }

    public CategorieService getCategorieService() {
        return categorieService;
    }

    public void setCategorieService(CategorieService categorieService) {
        this.categorieService = categorieService;
    }

    public Long getCatId() {
        return catId;
    }

    public void setCatId(Long catId) {
        this.catId = catId;
    }

    public String getCatCode() {
        return catCode;
    }

    public void setCatCode(String catCode) {
        this.catCode = catCode;
    }

    public String getCatLibelle() {
        return catLibelle;
    }

    public void setCatLibelle(String catLibelle) {
        this.catLibelle = catLibelle;
    }

    
}
