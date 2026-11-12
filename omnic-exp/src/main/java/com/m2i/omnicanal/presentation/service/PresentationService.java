/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service;

import com.google.gson.JsonElement;
import com.m2i.omnicanal.model.FournisseurService;
import com.m2i.omnicanal.model.InterfaceUtilisateur;
import com.m2i.omnicanal.model.LigneSchemaEcriture;
import com.m2i.omnicanal.model.ParametreUtilisateur;
import com.m2i.omnicanal.model.Transaction;
import com.m2i.omnicanal.model.TypeService;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.json.simple.JSONObject;



/**
 *
 * @author cda
 */
public interface PresentationService {
   ResultMessage findByNumeroCouche(List<ResultMessage>  rMsg,String numeroCouche);
    List<ResultMessage>   findResponseEntity(List<ResultMessage>  rMsg,String numeroCouche);
     JSONObject  findResponseJson(List<ResultMessage>  rMsg,String numeroCouche);
      JSONObject  getJsonObject(String numCouche , int errCode, String msg,String responseType, Object responseEntity);
       ResultMessage  getResultMessage(String numCouche , int errCode, String msg,String responseType, Object responseEntity);
      
      List<String> check(String key, JsonElement jsonElement,List<String> list );
      Object  setJsonResponseEntity(List<ResultMessage>  rMsg);
      boolean rechercheSucess (JsonElement jsonElement,List<String> list );
    public  Set<LigneSchemaEcriture>   findSchema(TypeService tpService, FournisseurService fournisseurServiceSource, FournisseurService fournisseurServiceDest);
     public Transaction findTransaction(  String codeUo,TypeService typeService,List<ParametreUtilisateur>  listparamUser, String reference,String canal);
}
