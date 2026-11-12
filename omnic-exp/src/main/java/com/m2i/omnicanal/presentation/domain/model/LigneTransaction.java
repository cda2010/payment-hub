/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.domain.model;


import com.m2i.omnicanal.model.enumeration.PrioriteExec;
import com.m2i.omnicanal.presentation.domain.MethodeFonction;
import com.m2i.omnicanal.presentation.domain.Parameters;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author cda
 */
public class LigneTransaction  implements Serializable,  Comparable<LigneTransaction>{
 
  private PrioriteExec priorite;
  private Integer ordre;
       private Long idSchema;

 private MethodeFonction methodeApi;
    private Set<Parameters> parametreService = new HashSet<>();
    public PrioriteExec getPriorite() {
        return priorite;
    }

    public void setPriorite(PrioriteExec priorite) {
        this.priorite = priorite;
    }

    public Integer getOrdre() {
        return ordre;
    }

    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }
 
  //trions les employés selon leur age dans l'ordre croiddant
  //retroune un entier négative, zéro ou positive si l'age 
  //de cet employé est moins que, égale à ou supérieur à l'objet comparé avec

    public Long getIdSchema() {
        return idSchema;
    }

    public void setIdSchema(Long idSchema) {
        this.idSchema = idSchema;
    }
      

    @Override
    public int compareTo(LigneTransaction o) {
        return (this.ordre - o.ordre);
 }

    public MethodeFonction getMethodeApi() {
        return methodeApi;
    }

    public void setMethodeApi(MethodeFonction methodeApi) {
        this.methodeApi = methodeApi;
    }

    public Set<Parameters> getParametreService() {
        return parametreService;
    }

    public void setParametreService(Set<Parameters> parametreService) {
        this.parametreService = parametreService;
    }
    
}
