package com.m2i.pti.service.dto;

import java.io.Serializable;
import java.util.List;

import com.m2i.pti.domain.TypeService;
import com.m2i.pti.domain.UniteOrganisation;



/**
 *
 * @author kadsuke
 */
public class TypeServiceReseauOrganisationDTO implements Serializable {

    private Long id;

    private String codePays;
      
    private List<TypeService> typeService;

    private UniteOrganisation uniteOrganisation;

    private  String codePv;

    private String libelleTypeService;

    private String libelleUo;

    private Long typeServiceId;

    private Long UoId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodePays() {
        return codePays;
    }

    public void setCodePays(String codePays) {
        this.codePays = codePays;
    }

   

    public String getLibelleTypeService() {
        return libelleTypeService;
    }

    public void setLibelleTypeService(String libelleTypeService) {
        this.libelleTypeService = libelleTypeService;
    }

    public String getLibelleUo() {
        return libelleUo;
    }

    public void setLibelleUo(String libelleUo) {
        this.libelleUo = libelleUo;
    }

    public Long getTypeServiceId() {
        return typeServiceId;
    }

    public void setTypeServiceId(Long typeServiceId) {
        this.typeServiceId = typeServiceId;
    }

    public Long getUoId() {
        return UoId;
    }

    public void setUoId(Long uoId) {
        UoId = uoId;
    }

    public List<TypeService> getTypeService() {
        return typeService;
    }

    public void setTypeService(List<TypeService> typeService) {
        this.typeService = typeService;
    }

    public UniteOrganisation getUniteOrganisation() {
        return uniteOrganisation;
    }

    public void setUniteOrganisation(UniteOrganisation uniteOrganisation) {
        this.uniteOrganisation = uniteOrganisation;
    }

    public String getCodePv() {
        return codePv;
    }

    public void setCodePv(String codePv) {
        this.codePv = codePv;
    }

    public TypeServiceReseauOrganisationDTO() {
    }

    public TypeServiceReseauOrganisationDTO(Long id, String codePays, String codePv,Long typeServiceId,Long uoId,
            String libelleUo,String libelleTypeService  ) {
            this.id = id;
            this.codePays = codePays;
            this.codePv = codePv;
            this.libelleTypeService = libelleTypeService;
            this.libelleUo = libelleUo;
            this.typeServiceId = typeServiceId;
            this.UoId = uoId;
    }

}
