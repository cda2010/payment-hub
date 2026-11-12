/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.external.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.PointVente;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.external.domain.ExtActeurTransfert;
import com.m2i.pti.external.domain.ExtBwCustomer;
import com.m2i.pti.external.domain.ExternalResponse;
import com.m2i.pti.external.service.CarteService;
import com.m2i.pti.external.service.ExtService;
import com.m2i.pti.external.service.ExternalService;
import com.m2i.pti.mobile.dto.MClient;
import com.m2i.pti.mobile.dto.MCompte;
import com.m2i.pti.mobile.dto.MFrais;
import com.m2i.pti.mobile.dto.MInfos;
import com.m2i.pti.mobile.dto.MOperation;
import com.m2i.pti.mobile.dto.MParamFrais;
import com.m2i.pti.mobile.dto.MPersonne;
import com.m2i.pti.mobile.dto.MResponse;
import com.m2i.pti.mobile.enumeration.MSexe;
import com.m2i.pti.mobile.enumeration.MTypeCompte;
import com.m2i.pti.service.CompteService;
import com.m2i.pti.service.PointVenteService;
import com.m2i.pti.service.UniteOrganisationService;
import com.m2i.pti.service.dto.CustomerInformation;
import com.m2i.pti.service.dto.Message;
import com.m2i.pti.service.util.Util;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import com.m2i.pti.web.rest.errors.CustomSettlementException;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.logging.Logger;
import jakarta.xml.bind.JAXBException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author cda
 */
@RestController
@RequestMapping("/api/external/card/v1")
public class ExternalCardResource {

    PointVenteService pointVenteService;
    UniteOrganisationService uniteOrganisationService;
    ExternalService externalService;
    CarteService carteService;
    String codeFse = "0054";
    ExtService extService;
    CompteService compteService;
    private static final Logger log = Logger.getLogger(ExternalCardResource.class.getName());

    public ExternalCardResource(PointVenteService pointVenteService, CarteService carteService, ExternalService externalService, CompteService compteService, ExtService extService) {
        this.carteService = carteService;
        this.externalService = externalService;
        this.compteService = compteService;
        this.extService = extService;
        this.pointVenteService = pointVenteService;
    }

    @GetMapping("/getInfos")
    @Timed
    public MClient getInfos(@RequestParam String accountId, @RequestParam String last4Digit) throws IOException, JAXBException {
        log.info("Debut d'execution de la methode getSolde de external ressource");
        MClient cus = carteService.getInfoCarte(accountId, last4Digit, codeFse);

        return cus;
    }

    @GetMapping("/getSolde")
    @Timed
    public com.m2i.pti.service.dto.Balance getSolde(@RequestParam String accountId, @RequestParam String last4Digit) throws IOException, JAXBException {
        log.info("Debut d'execution de la methode getSolde de external ressource");
        com.m2i.pti.service.dto.Balance balance = carteService.getSolde(accountId, last4Digit, codeFse);

        return balance;
    }

    @GetMapping("/genereOTP")
    @Timed
    public Message genereOTP(@RequestParam String codePays, @RequestParam String telephone, @RequestParam String identifiant) throws IOException, JAXBException {
        log.info("Debut d'execution de la methode getSolde de external ressource");
        return externalService.generateAndSendCodeOTP(codePays, telephone, identifiant);
    }

    @PostMapping("/recharge")
    @Timed
    public ExternalResponse doOperationRecharge(@RequestParam String codePays,
            @RequestParam String telephone,
            @RequestParam String codePv,
            @RequestParam String codeOperateur,
            @RequestParam BigDecimal montant,
            @RequestParam String fees,
            @RequestParam String canal,
            @RequestBody ExtActeurTransfert extBwCustomer
    ) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.info("Debut d'execution de la methode paiement facture de external ressource");
        try {
            Optional<PointVente> pointVenteExist = pointVenteService.findByCode(codePv);
            /*if (!(codeFseSrc != null)) {
                codeFseSrc = parametreAppRepository.findByTypeAndNomAndCodeFse("FOURNISSEUR_MONNAIE", codePays).getValeur();
            }
             */
            UniteOrganisation uoSrc = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(extBwCustomer.getEmeteur().getCodeFse());
            UniteOrganisation uoDest = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(extBwCustomer.getBeneficiaire().getCodeFse());
            if (pointVenteExist.isPresent() && uoSrc != null) {
                if (pointVenteExist.get().getUniteOrganisation() != null) {
                    MOperation mOperation = new MOperation();
                    mOperation.setTypeService("ACCOUNT_ACCOUNT");
                    mOperation.setCanal(canal);
                    mOperation.setCodePv(codePv);
                    mOperation.setMontant(montant);
                    mOperation.setActeur("CLIENT");
                    mOperation.setApplication("AUTRE");
                    mOperation.setMotif("M2");
                    mOperation.setUserConnecte("external");
                    mOperation.setEmetteur(extService.getMPersonne(extBwCustomer.getEmeteur(), codeOperateur));
                    mOperation.setBeneficiaire(extService.getMPersonne(extBwCustomer.getBeneficiaire(), codeOperateur));
                    mOperation.setIdentifiant(extBwCustomer.getEmeteur().getMsisdn());
                    //Code fact
                    /* Calcul des frais*/
                    String codeUoDest = uoDest.getCodeUO();
                    MFrais mFrais = externalService.calculFrais(new MParamFrais(codePv, mOperation.getTypeService(), canal, uoSrc.getCodeUO(), codeUoDest, montant, null));
                    mOperation.setMontantFrais(Util.getZeroIfNull(mFrais.getFraisOperation()));
                    mOperation.setMontantTaxe(Util.getZeroIfNull(mFrais.getTaxeOPeration()));
                    return externalService.doOperationCompteACompte(mOperation);

                } else {
                    return new ExternalResponse("-1", "Impossible de trouver l'unité d'organisation correspondant au point de vente.", null, null);
                    //throw new CustomParameterizedException("Impossible de trouver l'unité d'organisation correspondant au point de vente.");
                }
            } else {
                return new ExternalResponse("-1", "Impossible de trouver l'unité d'organisation correspondant au point de vente.", null, null);
            }
        } catch (CustomSettlementException e) {
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ExternalResponse("-1", e.getMessage(), null, null);
        } catch (CustomParameterizedException e) {
            return new ExternalResponse("-1", e.getMessage(), null, null);
        }
    }

    @PostMapping("/link")
    @Timed
    public ExternalResponse linkCard(@RequestParam String codeOperateur, @RequestParam String codePays,
            @RequestParam String telephone,
            @RequestParam String codeOTP,
            @RequestBody ExtBwCustomer extBwCustomer
    ) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.info("Debut d'execution de la methode paiementBien de external ressource");
        String canal = "MOBILE";
        String numeroTelephone;
        String codeBanque = "0";
        String codeAgence = "0";
        String numeroCompte;
        String teneur;

        try {
            if (extBwCustomer!=null){
                   if (externalService.verifyOTP(codePays, telephone, codeOTP,extBwCustomer.getNumeroCompte())) {
                MInfos minfos = null;
                if (extBwCustomer != null) {
                    numeroTelephone = extBwCustomer.getMsisdn();
                    teneur = extBwCustomer.getCodeFse();
                    numeroCompte = extBwCustomer.getNumeroCompte();
                    if (extBwCustomer.getTypeCompte().equals("BANQUE")) {
                        codeBanque = extBwCustomer.getCodeBanque();
                        codeAgence = extBwCustomer.getCodeAgence();
                    } else if (extBwCustomer.getTypeCompte().equals("CARTE")) {
                        codeBanque = "0";
                        codeAgence = extBwCustomer.getLast4Digit();
                    }
                    String malias = compteService.getCRC32Checksum(codeAgence, numeroCompte, numeroTelephone, codeOperateur);
                    extBwCustomer.setAlias(malias);
                    minfos = compteService.findByalias(malias);
                    MResponse mResp = new MResponse();
                    if (minfos == null) {
                        MResponse mResponse = extService.createBwCustomer(extBwCustomer, codeFse, true);
                        if (mResponse != null && mResponse.getCode().equals("0")) {
                            return new ExternalResponse("0", mResponse.getMessage(), null, null);
                        } else {
                            mResp.setCode("-100");
                            return new ExternalResponse("-1", "une erreur s'est produite lors de l'opération", null, null);
                        }
                    } else {

                        return new ExternalResponse("-1", "le compte  existe déjà", null, null);
                    }

                }

                return new ExternalResponse("-1", "Client existe déjà", null, null);
            } else {
                return new ExternalResponse("-1", "OTP Incorrect.", null, null);
            }
            }else{
                  return new ExternalResponse("-1", "information du client incomplète ", null, null);
            }
           
         
        } catch (CustomSettlementException e) {

            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ExternalResponse("-1", e.getMessage(), null, null);
        } catch (CustomParameterizedException e) {
            return new ExternalResponse("-1", e.getMessage(), null, null);
        }
    }
}
