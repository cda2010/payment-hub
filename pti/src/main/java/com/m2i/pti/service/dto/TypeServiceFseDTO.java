package com.m2i.pti.service.dto;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.m2i.pti.domain.TypeService;
import com.m2i.pti.domain.UniteOrganisation;

/**
 * A DTO for the TypeService entity.
 */
public class TypeServiceFseDTO implements Serializable {

    private Long id;
    private List<TypeService> typeService = new ArrayList();
    private UniteOrganisation uniteOrganisation;
    private UniteOrganisationDTO uniteOrganisationDTO;
    
    
    private Long type_service_id;
    private Long unite_organisation_id;
    private String code_fse;
    private String code_uo;
    private String libelle_unite_organisation;
    private String code;
    private String description;



    public TypeServiceFseDTO() {
    }
    
    public TypeServiceFseDTO(Long id, Long type_service_id, Long unite_organisation_id, String code_fse, String code_uo,
            String libelle_unite_organisation, String code, String description) {
        this.id = id;
        this.type_service_id = type_service_id;
        this.unite_organisation_id = unite_organisation_id;
        this.code_fse = code_fse;
        this.code_uo = code_uo;
        this.libelle_unite_organisation = libelle_unite_organisation;
        this.code = code;
        this.description = description;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    
    public UniteOrganisation getUniteOrganisation() {
        return uniteOrganisation;
    }
    public void setUniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
    }
    public List<TypeService> getTypeService() {
        return typeService;
    }
    public void setTypeService(List<TypeService> typeService) {
        this.typeService = typeService;
    }
    public UniteOrganisationDTO getUniteOrganisationDTO() {
        return uniteOrganisationDTO;
    }
    public void setUniteOrganisationDTO(UniteOrganisationDTO uniteOrganisationDTO) {
        this.uniteOrganisationDTO = uniteOrganisationDTO;
    }
    public Long getType_service_id() {
        return type_service_id;
    }
    public void setType_service_id(Long type_service_id) {
        this.type_service_id = type_service_id;
    }
    public Long getUnite_organisation_id() {
        return unite_organisation_id;
    }
    public void setUnite_organisation_id(Long unite_organisation_id) {
        this.unite_organisation_id = unite_organisation_id;
    }
    public String getCode_fse() {
        return code_fse;
    }
    public void setCode_fse(String code_fse) {
        this.code_fse = code_fse;
    }
    public String getCode_uo() {
        return code_uo;
    }
    public void setCode_uo(String code_uo) {
        this.code_uo = code_uo;
    }
    public String getLibelle_unite_organisation() {
        return libelle_unite_organisation;
    }
    public void setLibelle_unite_organisation(String libelle_unite_organisation) {
        this.libelle_unite_organisation = libelle_unite_organisation;
    }
    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

     
    
}
