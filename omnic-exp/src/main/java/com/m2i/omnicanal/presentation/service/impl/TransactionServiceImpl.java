/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service.impl;

import com.m2i.omnicanal.presentation.domain.MethodeFonction;
import com.m2i.omnicanal.presentation.domain.Parameters;
import com.m2i.omnicanal.presentation.domain.ParametreMethod;

import com.m2i.omnicanal.presentation.domain.model.Transaction;
import com.m2i.omnicanal.model.LigneTransaction;
import com.m2i.omnicanal.model.Methode;
import com.m2i.omnicanal.presentation.service.ParametreMethodeService;
import com.m2i.omnicanal.presentation.service.TransactionService;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import jakarta.inject.Inject;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author cda
 */
@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {
    @Inject 
    ParametreMethodeService paramMethode;
    @Override
    public ResponseEntity<Object> getMethodeFonction(Transaction trans) {
        /*
     ResponseEntity<Object> resultat=null;
      Methode methode = new MethodeFonction(); 
      Set<Parameters> listparam;
      Set<LigneTransaction> ligneTrans = trans.getLigneTransaction();
      Iterator it = ligneTrans.iterator();
      LigneTransaction lg ;
      RestClient resClient = new RestClient();
        while(it.hasNext()){
            lg=  (LigneTransaction)it.next();
            methode= lg.getMethodeApi();
            listparam= lg.getParametreService();
            switch (methode.getApi().getType()) {
                case REST:
                    HashMap  <String, Object> mapParametre ;
                    resClient.setRestServerUri("http://"+methode.getApi().getHost()+":"+methode.getApi().getPort()+"/"+methode.getApi().getExternalName());   
                   //   resClient.setRestServerUri("http://jsonplaceholder.typicode.com/posts");
                      List<ParametreMethod> result=paramMethode.findByMethode(methode.getId());
                      mapParametre=getParametres( lg.getParametreService(),result);   
                     // resultat=   resClient.getRessource( mapParametre);
                      
                      /*  Traitement du resultat****/

/** voir comment caster * le resultat ***********************************/
                      
 


                      /*****/
        /*          
                    break;
                case WEBSERVICE:
                            
                    
                    break;
                default:
                    break;
            }
            
            
            
        }
        return resultat;*/
        return null;
    }
    private  HashMap  <String, Object> getParametres(Set<Parameters> listparam,List<ParametreMethod> listparamethode){ 
         HashMap  <String, Object> mapParametre = new HashMap<>();
         ParametreMethod  elementParam =null;
         String valeur=null;
         Iterator itParam = listparamethode.iterator();
         while(itParam.hasNext()) {
               elementParam=(ParametreMethod) itParam.next();
               valeur= rechercheParamMthode(listparam,  elementParam.getCodeParametre());
               if (valeur!=null){
                         mapParametre.put(elementParam.getNomExterne(), valeur);
                   }
                     
         }
        
        return mapParametre;
    }
   
    private String rechercheParamMthode(  Set<Parameters> listparam, String code){
       String valeur= null;
       Iterator it = listparam.iterator();
       Parameters  param;
       while(it.hasNext()) {
           param=(Parameters) it.next();
           if (param.getCodeParametre().equals(code)){
                     return param.getValeur();
           }       
       }
    return valeur;
    }
}
