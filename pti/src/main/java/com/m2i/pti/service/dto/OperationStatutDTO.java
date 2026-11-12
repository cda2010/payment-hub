package com.m2i.pti.service.dto;

import com.m2i.pti.domain.enumeration.ActionService;
import com.m2i.pti.domain.enumeration.Etape;

public class OperationStatutDTO {
    private ActionService etapeService;
    private Etape statutEtape;
    public ActionService getEtapeService() {
        return etapeService;
    }
    public void setEtapeService(ActionService etapeService) {
        this.etapeService = etapeService;
    }
    public Etape getStatutEtape() {
        return statutEtape;
    }
    public void setStatutEtape(Etape statutEtape) {  
        this.statutEtape = statutEtape;
    }
    public OperationStatutDTO(String etapeService, String statutEtape) {
        this.etapeService = etapeService!=null? ActionService.valueOf(etapeService):null;
        this.statutEtape = statutEtape!=null? Etape.valueOf(statutEtape):null;
    }
 
    
}
