/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.external.rest;

import com.codahale.metrics.annotation.Timed;
import com.google.common.hash.Hashing;
import com.m2i.pti.mobile.service.MobileService;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.m2i.pti.external.service.ExtService;
import com.m2i.pti.mobile.dto.MClient;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import jakarta.xml.bind.JAXBException;
import org.json.JSONException;
import org.springframework.web.bind.annotation.RequestParam;
import com.m2i.pti.mobile.dto.MParametre;
import com.m2i.pti.mobile.dto.SettlementResponse;
import com.pti.emeraude.gateway.PtiSetup;
import com.pti.emeraude.gateway.domain.CustomerAccount;
import com.pti.emeraude.gateway.domain.CustomerInformation;
import com.pti.emeraude.gateway.domain.ElementFacture;
import com.m2i.pti.external.domain.ExtBwCustomer;
import com.m2i.pti.service.util.Util;
import com.pti.emeraude.gateway.domain.FactureFournisseur;
import com.pti.emeraude.gateway.domain.MResponse;
import com.pti.emeraude.gateway.domain.RegleFacturier;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.m2i.pti.service.CompteService;

/**
 *
 * @author cda
 */
@RestController
@RequestMapping("/api/external-facturier/omnnicanal/v1")
public class ExLinkageResource {

    CompteService compteService;
    MobileService mobileService;
    ExtService extService;
    private final Logger log = LoggerFactory.getLogger(ExLinkageResource.class);

    public ExLinkageResource(MobileService mobileService, ExtService extService, CompteService compteService) {
        this.mobileService = mobileService;
        this.compteService = compteService;
        this.extService = extService;
    }
    
    @PostMapping("/getregle")
    @Timed
    public com.m2i.omnicanal.outils.restclient.model.Message getRegle(
            @RequestParam("reference") String reference,
            @RequestParam("montant") String montant
    )
            throws JAXBException {
        RegleFacturier reglFact = new RegleFacturier();
        FactureFournisseur fs = new FactureFournisseur();
        reglFact.setMessage("OK");
        reglFact.setAction("OK");
        fs.setReference(reference);
        fs.setMontant(montant);
        reglFact.setFacture(fs);
        String str = (String) Util.marshall(reglFact, String.class);
        com.m2i.omnicanal.outils.restclient.model.Message msg = new com.m2i.omnicanal.outils.restclient.model.Message();
        msg.setMsg("ok");
        msg.setCodeErr("0");
        
        return msg;
    }

    /**
     *
     * @param montant
     * @param nombre
     * @return
     * @throws JAXBException
     */
    @PostMapping("/getmontant")
    @Timed
    public com.m2i.omnicanal.outils.restclient.model.Message getMontant(
            @RequestParam("montant") String montant,
            @RequestParam("nombre") String nombre
    )
            throws JAXBException {
        ElementFacture elmfact = new ElementFacture();
        Double mt = 0D;
        Long nbre = Long.parseLong(nombre);
        try {
            mt = Double.parseDouble(montant);
        } catch (Exception ex) {
            mt = Double.parseDouble(montant.substring(0, montant.indexOf(".")));
        }
        mt = mt.longValue() * 1D;
        com.m2i.omnicanal.outils.restclient.model.Message msg = new com.m2i.omnicanal.outils.restclient.model.Message();
        msg.setMsg("ok");
        msg.setCodeErr("0");
        Double total = nbre * mt * 1D;
        elmfact.setMontant(mt);
        elmfact.setNombre(nbre);
        elmfact.setTotal(total);
        String str = (String) Util.marshall(elmfact, String.class);
        msg.setText(str);
        return msg;
    }
    
       
    @PostMapping("/link")
    @Timed
    public SettlementResponse link(@RequestParam String codeFse,
            @RequestParam String codeOperateur, @RequestParam String agence,
            @RequestParam String numeroCompte, @RequestParam String codeTelephoneClient,
            @RequestParam String telephoneClient, @RequestParam String telephoneVerif) {
        SettlementResponse settlementResponse = new SettlementResponse();
        //boolean bool = apiGTPService.verifOtp(codeTelephoneVerif, telephoneVerif, codeFacturier, otp, identifiant);

        //     if (bool) {
        List<com.pti.emeraude.gateway.domain.MParametre> mParametres = new ArrayList<>();
        try {
            MClient mclient = mobileService.findInfosClientExterne(numeroCompte, agence, codeFse);
            String alias = compteService.getCRC32Checksum(agence, numeroCompte, telephoneClient, codeOperateur);
            ExtBwCustomer extBwCustomer = new ExtBwCustomer();
            extBwCustomer.setCodePays(codeTelephoneClient);
            extBwCustomer.setMsisdn(telephoneClient);
            extBwCustomer.setNomCustomer(mclient.getPersonne().getNom());
            extBwCustomer.setPrenomCustomer(mclient.getPersonne().getPrenom());
            extBwCustomer.setNumeroCompte(mclient.getCompte().getNumeroCompte());
            extBwCustomer.setCodeOperateur(codeOperateur);
            extBwCustomer.setTypeCompte(mclient.getCompte().getTypeCompte().name());
            extBwCustomer.setLast4Digit(agence);
            extBwCustomer.setAlias(alias);
            extBwCustomer.setCodeAgence(mclient.getCompte().getCodeAgence());
            extBwCustomer.setCodeFse(codeFse);
            /* le codeFseApplication Source est le code l'application appelante
             */
            com.m2i.pti.mobile.dto.MResponse rep = extService.createBwCustomer(extBwCustomer, codeFse, true);
            if (rep.getCode() != null && rep.getCode().equals("0")) {
                settlementResponse.setCodeMsg("0");
                settlementResponse.setSendMsg(Boolean.FALSE);
            } else {
                settlementResponse.setCodeMsg("-1");
                if (rep.getMessage() != null) {
                    settlementResponse.setMsg(rep.getMessage());
                }
                
            }
            
        } catch (Exception e) {
            settlementResponse.setCodeMsg("-1");
            settlementResponse.setMsg(e.getMessage());
        }

//                String alias = walletApiService.getCRC32Checksum(brancheCode, accountNumber, msisdn, bankCible);
        // String alias = walletApiService.getCRC32Checksum(brancheCode, accountNumber, msisdn, codeOperateur);
        //   }
        return settlementResponse;
    }
    
    @PostMapping("/getInfos")
    @Timed
    public List<MParametre> getCardInformation(
            @RequestParam String agence,
            @RequestParam String numeroCompte, @RequestParam String codeFse
    )
            throws JAXBException, KeyManagementException, NoSuchAlgorithmException, IOException, JSONException {
        List<MParametre> mParametres = new ArrayList<>();
        MClient mclient = mobileService.findInfosClientExterne(numeroCompte, agence, codeFse);
        log.info("result" + mclient);
        mParametres.add(new MParametre("NOM", mclient.getPersonne().getNom(), 1));
        mParametres.add(new MParametre("PRENOM", mclient.getPersonne().getPrenom(), 1));
        mParametres.add(new MParametre("EMAILADRESS", mclient.getPersonne().getEmail(), 0));
        mParametres.add(new MParametre("AGENCE", mclient.getCompte().getCodeAgence() != null ? mclient.getCompte().getCodeAgence() : agence, 1));
        mParametres.add(new MParametre("NUMEROCOMPTE", mclient.getCompte().getNumeroCompte(), 1));
        mParametres.add(new MParametre("CODETEL", mclient.getPersonne().getCodeTelephone(), 0));
        mParametres.add(new MParametre("CODEOPERATEUR", "0", 0));
        mParametres.add(new MParametre("CODEFSEFACTURIER", codeFse, 0));
        mParametres.add(new MParametre("TEL", mclient.getPersonne().getTelephone(), 1));
        mParametres.add(new MParametre("SENDOTP", "0", 0));
        CustomerInformation customerInformation = new CustomerInformation();
        //    customerInformation.setDateNaissance(mclient.getPersonne().getDateNaissance().toString());
        customerInformation.setNom(mclient.getPersonne().getNom());
        customerInformation.setTelephone(mclient.getPersonne().getTelephone());
        customerInformation.setPrenom(mclient.getPersonne().getPrenom());
        customerInformation.setEmail(mclient.getPersonne().getEmail());
        List<CustomerAccount> listAccount = new ArrayList();
        CustomerAccount acc = new CustomerAccount();
        acc.setTypeCompte(mclient.getCompte().getTypeCompte().name());
        acc.setNumeroCompte(mclient.getCompte().getNumeroCompte());
        acc.setTeneurCompte(codeFse);
        acc.setCodeBanque(mclient.getCompte().getCodeBanque());
        acc.setCodeAgence(mclient.getCompte().getCodeAgence());
        listAccount.add(acc);
        customerInformation.setListCompte(listAccount);
        //   String str = (String) Util.marshall(customerInformation, String.class);
        //mParametres.add(new MParametre("EXTENDEDDATA", str, 0));

        return mParametres;
    }

    /*
    @PostMapping("/link")
    @Timed
    public SettlementResponse link(String codeTelephoneClient, String telephoneClient, String codeTelephoneVerif, String telephoneVerif, String codeFacturier, String otp, String identifiant, String last4Digits, String codeFseApplicationSource) throws JAXBException {
        boolean bool;
        SettlementResponse settlementResponse = new SettlementResponse();
       
            Message msg = mobileService.verifyOTP(codeTelephoneVerif, telephoneVerif, otp, identifiant, codeFacturier);
            if (msg != null && msg.getCodeErr().equals("0")) {
                bool = true;

            } else {
                bool = false;
            }

            if (bool) {
               


                   CustomerInformation cust = apiService.getInfoClientCompte(identifiant, code, codeFse);
              
              
                   // String alias = walletApiService.getCRC32Checksum(last4Digits, identifiant, telephoneClient, codeFseApplicationSource);
                    ExtBwCustomer extBwCustomer = new ExtBwCustomer();
                    extBwCustomer.setCodePays(codeTelephoneClient);
                    extBwCustomer.setMsisdn(telephoneClient);
                    extBwCustomer.setNomCustomer(cust.getNom());
                    extBwCustomer.setPrenomCustomer(cust.getPrenom());
                    extBwCustomer.setNumeroCompte(identifiant);
                    extBwCustomer.setAlias(alias);
                    extBwCustomer.setCodeAgence(code
                    0
                    .3250W<AQ);
                    extBwCustomer.setCodeFse(apiGTPService.getCodeFse());
                    /* le codeFseApplication Source est le code l'application appelante
     */
    //  PtiSetup pticonnect = apiGTPService.returnPtiConnect(codeFseApplicationSource);
    // String hashParam = Hashing.sha256().hashString(telephoneClient + identifiant + pticonnect.getPlainClientCredentials(), StandardCharsets.UTF_8).toString();
    //MResponse mResponse = extService.createBwCustomer(extBwCustomer, extBwCustomer.getCodeFse(), false);
    //}
    //return null;
    //}
}
