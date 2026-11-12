package com.m2i.pti.service.dto;

import com.m2i.pti.domain.CanalApplication;
import com.m2i.pti.domain.TypeService;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.enumeration.Application;

/**
 * A Canal APplication
 */
public class ParamCanalDTO {

    private Long id;

    private Application application;

    private TypeService typeService;

    private UniteOrganisation uniteOrganisation;



    private Long typeServiceId;

    private Long uniteOrganisationId;

    private Long canalApplicationId;

    private Long poids;

    private String canal;

    private String sousCanal;

    private String description;

    private String libelleUniteOrganisation;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Long getPoids() {
        return poids;
    }

    public void setPoids(Long poids) {
        this.poids = poids;
    }

    public TypeService getTypeService() {
        return typeService;
    }

    public void setTypeService(TypeService typeService) {
        this.typeService = typeService;
    }

    public UniteOrganisation getUniteOrganisation() {
        return uniteOrganisation;
    }

    public void setUniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
    }

    public Long getTypeServiceId() {
        return typeServiceId;
    }

    public void setTypeServiceId(Long typeServiceId) {
        this.typeServiceId = typeServiceId;
    }

    public Long getUniteOrganisationId() {
        return uniteOrganisationId;
    }

    public void setUniteOrganisationId(Long uniteOrganisationId) {
        this.uniteOrganisationId = uniteOrganisationId;
    }

    public Long getCanalApplicationId() {
        return canalApplicationId;
    }

    public void setCanalApplicationId(Long canalApplicationId) {
        this.canalApplicationId = canalApplicationId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLibelle_unite_organisation() {
        return libelleUniteOrganisation;
    }

    public void setLibelle_unite_organisation(String libelleUniteOrganisation) {
        this.libelleUniteOrganisation = libelleUniteOrganisation;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getSousCanal() {
        return sousCanal;
    }

    public void setSousCanal(String sousCanal) {
        this.sousCanal = sousCanal;
    }

    public String getLibelleUniteOrganisation() {
        return libelleUniteOrganisation;
    }

    public void setLibelleUniteOrganisation(String libelleUniteOrganisation) {
        this.libelleUniteOrganisation = libelleUniteOrganisation;
    }
    
    

    public ParamCanalDTO(Long id, String application, String canal, String sousCanal, String description, String libelleUniteOrganisation) {
        this.id = id;
        this.application = application != null ? Application.valueOf(application) : null;
        this.canal = canal;
        this.sousCanal = sousCanal;
        this.description = description;
        this.libelleUniteOrganisation = libelleUniteOrganisation;
    }


    public ParamCanalDTO(Long id, Application application, TypeService typeService, UniteOrganisation uniteOrganisation,
            CanalApplication canalApplication, Long poids) {
        this.id = id;
        this.application = application;
        this.typeService = typeService;
        this.uniteOrganisation = uniteOrganisation;
      //  this.canalApplication = canalApplication;
        this.poids = poids;
    }

    public ParamCanalDTO() {
    }

}
