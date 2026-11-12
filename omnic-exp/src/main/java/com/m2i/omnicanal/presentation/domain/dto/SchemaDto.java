/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.domain.dto;

import com.m2i.omnicanal.model.TypeService;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

/**
 *
 * @author cda
 */
public class SchemaDto {
     private Long id;
  
  private String nomSchema;
 
  private String codeGroovy;
  
  private Long typeServiceId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomSchema() {
        return nomSchema;
    }

    public void setNomSchema(String nomSchema) {
        this.nomSchema = nomSchema;
    }

    public String getCodeGroovy() {
        return codeGroovy;
    }

    public void setCodeGroovy(String codeGroovy) {
        this.codeGroovy = codeGroovy;
    }

    public Long getTypeServiceId() {
        return typeServiceId;
    }

    public void setTypeServiceId(Long typeServiceId) {
        this.typeServiceId = typeServiceId;
    }
  
  
  
}
