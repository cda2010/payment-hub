package com.m2i.pti.service.dto;

import com.m2i.pti.domain.ClassificationFacturier;

import java.io.Serializable;

public class ClassificationFacturierDTO implements Serializable {


    private Long id;

    private String libelleClassification;

    private UniteOrganisationDTO uniteOrganisation ;

    private Long niveauSuperieurId ;

    public ClassificationFacturierDTO(ClassificationFacturier c){
        this.id=c.getId();
        this.libelleClassification=c.getLibelleClassification();
        UniteOrganisationDTO uo=new UniteOrganisationDTO();
        uo.setId(c.getUniteOrganisation().getId());
        uo.setLibelleUniteOrganisation(c.getUniteOrganisation().getLibelleUniteOrganisation());
        this.uniteOrganisation=uo;
        if(c.getNiveauSuperieur()!=null){
            this.niveauSuperieurId =c.getNiveauSuperieur().getId();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLibelleClassification() {
        return libelleClassification;
    }

    public void setLibelleClassification(String libelleClassification) {
        this.libelleClassification = libelleClassification;
    }

    public UniteOrganisationDTO getUniteOrganisation() {
        return uniteOrganisation;
    }

    public void setUniteOrganisation(UniteOrganisationDTO uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
    }

    public Long getNiveauSuperieurId() {
        return niveauSuperieurId;
    }

    public void setNiveauSuperieurId(Long niveauSuperieurId) {
        this.niveauSuperieurId = niveauSuperieurId;
    }
}
