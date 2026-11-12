/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.external.service;

import com.m2i.pti.domain.ExternalOTP;
import com.m2i.pti.mobile.dto.MClient;
import com.m2i.pti.mobile.dto.MCompte;
import com.m2i.pti.mobile.dto.MPersonne;
import com.m2i.pti.mobile.enumeration.MSexe;
import com.m2i.pti.mobile.enumeration.MTypeCompte;
import com.m2i.pti.repository.ExternalOTPRepository;
import com.m2i.pti.service.ApiService;
import com.m2i.pti.service.dto.CustomerInformation;
import com.m2i.pti.service.dto.Message;
import com.m2i.pti.service.util.RandomUtil;
import java.io.IOException;
import java.util.logging.Logger;
import jakarta.xml.bind.JAXBException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author cda
 */
@Service
public class CarteService {

    private final int DELAIS = 600;
    private ApiService apiService;
    private ExternalOTPRepository externalOTPRepository;
    private static final Logger log = Logger.getLogger(CarteService.class.getName());
    ExternalService externalService;

    public CarteService(ApiService apiService, ExternalService externalService, ExternalOTPRepository externalOTPRepository) {
        this.apiService = apiService;
        this.externalService = externalService;
        this.externalOTPRepository = externalOTPRepository;
    }



    public MClient getInfoCarte(String numeroCompte, String agence, String codeFse) throws IOException, IOException, JAXBException {
        CustomerInformation cust = apiService.getInfoClientCompte(numeroCompte, agence, codeFse);
        if (cust != null) {
            MPersonne infos = new MPersonne();
            MClient mclient = new MClient();
            log.info("---------------#Entrer de la fonction#------------------");
            MCompte mcpt = new MCompte();
            if (cust != null && cust.getListCompte().size() > 0) {
                mcpt.setNumeroCompte(cust.getListCompte().get(0).getNumeroCompte());
                mcpt.setTypeCompte(MTypeCompte.valueOf(cust.getListCompte().get(0).getTypeCompte()));
                mcpt.setTeneurCompte(cust.getListCompte().get(0).getTeneurCompte());
                mclient.setCompte(mcpt);
            }
            log.info("Recherche d'une personne in ......");
            log.info("Recherche des informations d'une personne......");
            infos.setSexe(cust.getSexe() != null ? MSexe.valueOf("MASCULIN") : MSexe.NON_DEFINI);
            infos.setNom(cust.getNom());
            infos.setPrenom(cust.getPrenom());
            infos.setCodeTelephone(cust.getCodeIndicatif());
            infos.setTelephone(cust.getTelephone());
            // infos.setDateNaissance(personnePhysique.getDateNaissance());
            infos.setLieuNaissance(cust.getLieuNaissance());

            //Mise ï¿½ jour des informations du document d'identificatio
            mclient.setPersonne(infos);

            log.info(
                    "Fin de la recherche......");
            return mclient;
        }
        return null;

    }

    public com.m2i.pti.service.dto.Balance getSolde(String numeroCompte, String agence, String codeFse) throws IOException, JAXBException {
        com.m2i.pti.service.dto.Balance cus = apiService.getSolde(numeroCompte, agence, codeFse);
        return cus;
    }
}
