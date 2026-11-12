/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service.dto;

import com.m2i.omnicanal.model.TypeService;
import com.m2i.omnicanal.model.util.JsonTransaction;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;

/**
 *
 * @author cda
 */
public class OperationDTO {
    
    private String reponse;
    private String fseCodeSource;
   
    private String fseCodeDest;
  
    private Long  codeOperation;
  
    private String  typeService;

    public String getFseCodeSource() {
        return fseCodeSource;
    }

    public void setFseCodeSource(String fseCodeSource) {
        this.fseCodeSource = fseCodeSource;
    }

    public String getFseCodeDest() {
        return fseCodeDest;
    }

    public void setFseCodeDest(String fseCodeDest) {
        this.fseCodeDest = fseCodeDest;
    }

    public Long getCodeOperation() {
        return codeOperation;
    }

    public void setCodeOperation(Long codeOperation) {
        this.codeOperation = codeOperation;
    }

    public String getTypeService() {
        return typeService;
    }

    public void setTypeService(String typeService) {
        this.typeService = typeService;
    }
    
    
    
}
