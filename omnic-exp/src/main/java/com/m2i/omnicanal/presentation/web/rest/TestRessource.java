/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.model.InterfaceUtilisateur;
import com.m2i.omnicanal.model.enumeration.ParamatreType;
import com.m2i.omnicanal.model.enumeration.ProprieteApi;
import com.m2i.omnicanal.outils.restclient.AuthTokenInfo;
import com.m2i.omnicanal.presentation.http.PartnerHttpClient;
import com.m2i.omnicanal.presentation.domain.ParametreDTO;
import com.m2i.omnicanal.presentation.domain.ParametreUser;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author cda
 */
@RestController
@RequestMapping("/api")
public class TestRessource {
     private final String login;
    private final String password;
    private final String url;
    private final  String clientId;
    private final String secretWord;
    public TestRessource() {
        this.login = "admin";
        this.password = "admin";
        this.url = "http://10.214.0.190:5980/";
        this.clientId = "Omnicanalapp";
        this.secretWord = "my-secret-token-to-change-in-production";
    }
    
 private ParametreUser addParametre(Long id ,  String codeParam, String valeur){
      InterfaceUtilisateur interfaceUtilisateur =new InterfaceUtilisateur();
  ParametreUser p=    new ParametreUser();
  p.setId(id);
  p.setCodeParametre(codeParam);
  p.setInterfaceUtilisateur(interfaceUtilisateur);
  p.setParametreApi(ProprieteApi.FSESOURCE);
  p.setNomParametre("");
  p.setTypeParametre(ParamatreType.STRING);
  p.setValeur(valeur);
  
  return p;
  
     
 }
 @PostMapping("/test-omni")
    @Timed
     public ResponseEntity sendParamsApprovisionnementToOmnicanal()  {
           
        String grant = PartnerHttpClient.passwordGrant(login, password, "password", secretWord, clientId);
        AuthTokenInfo tokeinfo = PartnerHttpClient.current().requestToken(this.url + "/oauth/token", grant, clientId, secretWord);
            ParametreDTO param = new ParametreDTO();
       
                    
            List<ParametreUser> parametres = new ArrayList<>();
            
            
           
            
            
            
             parametres.add(addParametre(2L, "MONTANTFRAISACQ", String.valueOf("500")));
            parametres.add(addParametre(3L, "MONTANTFRAISEMT", String.valueOf("400")));
            parametres.add(addParametre(4L, "MONTANTOP", String.valueOf("50000")));
            parametres.add(addParametre(5L, "CODEFSEDEST", "0004"));//A recuperer à partir du compte reseau master
            parametres.add(addParametre(6L, "CODEFSESOURCE", "0004"));

            parametres.add(addParametre(7L,"CPTEOPERATIONACTEUR","44454445454"));
          //  parametres.add(addParametre(8L, "CODEBANQUE", compteOperationActeur.getCompteBanque().getCodeBanque()));
           // parametres.add(addParametre(9L, "CODEAGENCE", compteOperationActeur.getCompteBanque().getCodeAgence()));


            parametres.add(addParametre(10L, "SIEGE","1"));

            param.setCodeService("001"); //code du type de service
            param.setCodeUniteOrganisation("01");//code UO
            param.setFse("0004"); //code FSE CBI
            param.setProprieteApi(ProprieteApi.FSESOURCE);
            param.setParam(parametres);
            param.setCodeUniteOrganisation("01");//code UO
            param.setFse("0004"); //code FSE CBI
            param.setProprieteApi(ProprieteApi.FSESOURCE);
            param.setParam(parametres);
         
            ResponseEntity<Object> result = PartnerHttpClient.current().postEntity(this.url + "/api/omnicanalPresentation", tokeinfo, param);
            return result;
        
    }
}
