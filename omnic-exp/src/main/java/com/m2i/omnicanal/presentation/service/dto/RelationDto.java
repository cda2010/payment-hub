/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service.dto;

/**
 *
 * @author cda
 */
public class RelationDto {
     private Long id;
  private FournisseurServiceDto fseSource;
  
  private FournisseurServiceDto fseDestination;

  

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FournisseurServiceDto getFseSource() {
        return fseSource;
    }

    public void setFseSource(FournisseurServiceDto fseSource) {
        this.fseSource = fseSource;
    }

    public FournisseurServiceDto getFseDestination() {
        return fseDestination;
    }

    public void setFseDestination(FournisseurServiceDto fseDestination) {
        this.fseDestination = fseDestination;
    }
  
}
