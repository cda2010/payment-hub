/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.external.rest;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import jakarta.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.external.domain.ExtBwCustomer;
import com.m2i.pti.external.domain.GrData;
import com.m2i.pti.external.service.ExtService;
import com.m2i.pti.external.service.ExternalService;
import com.m2i.pti.mobile.dto.MClient;
import com.m2i.pti.mobile.dto.MDataTransaction;
import com.m2i.pti.mobile.dto.MOperation;
import com.m2i.pti.mobile.dto.MOperationFacture;
import com.m2i.pti.mobile.dto.MResponse;
import com.m2i.pti.mobile.enumeration.MTypeCompte;
import com.m2i.pti.mobile.rest.MobileResource;
import com.m2i.pti.mobile.service.MobileService;
import com.m2i.pti.mobile.service.TransfertAppService;
import com.m2i.pti.repository.EvenementOperationRepository;
import com.m2i.pti.security.AuthoritiesConstants;
import com.m2i.pti.service.ActiviteClientService;
import com.m2i.pti.service.ApiService;
import com.m2i.pti.service.CompteService;
import com.m2i.pti.service.ParametreAppService;
import com.m2i.pti.service.UserControlService;
import com.m2i.pti.service.UserService;
import com.m2i.pti.service.dto.ActiviteClientDTO;

/**
 *
 * @author cda
 */
@RestController
@RequestMapping("/api/external-gateway/omnnicanal/v1")
public class ExternalGateWayResource {

    ActiviteClientService activiteClientService;
    private final Logger log = LoggerFactory.getLogger(MobileResource.class);
    private final MobileService mobileService;
    private final CompteService compteService;
    private final ApiService apiService;
    private final UserControlService userControlService;
    private final UserService userService;
    private final EvenementOperationRepository evenementOperationRepository;
    private final TransfertAppService transfertAppService;
    private final ApplicationContext appContext;
    private final ParametreAppService parametreAppService;

    private enum ActionOTP {
        GENERER, VERIFIER
    }

    public ExternalGateWayResource(ActiviteClientService activiteClientService, ParametreAppService parametreAppService, EvenementOperationRepository evenementOperationRepository, UserService userService, TransfertAppService transfertAppService, MobileService mobileService, ApiService apiService, CompteService compteService, UserControlService userControlService, ApplicationContext appContext) {
        this.mobileService = mobileService;
        this.apiService = apiService;
        this.compteService = compteService;
        this.userControlService = userControlService;
        this.transfertAppService = transfertAppService;
        this.userService = userService;
        this.evenementOperationRepository = evenementOperationRepository;
        this.appContext = appContext;
        this.parametreAppService = parametreAppService;
        this.activiteClientService = activiteClientService;

    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/operations/doSettlement")
    @Timed
    public MResponse doOperation(@RequestBody MOperation mOperation, @RequestParam String codeFse) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.debug("REST request to save Operation : {}", mOperation);
        MResponse mResponse = mobileService.doOperationExternal(mOperation, codeFse);
        return mResponse;

    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/operations/doLink")
    @Timed
    public MResponse doOperationLINK(@RequestBody MOperationFacture mOperation, @RequestParam String codeOperateur, @RequestBody ExtBwCustomer extBwCustomer) throws IOException, NoSuchAlgorithmException, JAXBException {
        //     log.debug("REST request to save Operation : {}", mOperation);

        MResponse mResponse = null;//extService.doOperationLink(codeOperateur, extBwCustomer);

        return mResponse;

    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/operations/doSettlementFacture")
    @Timed
    public MResponse doOperationFacture(@RequestBody MOperationFacture mOperation, @RequestParam String codeFse) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.debug("REST request to save Operation : {}", mOperation);
        MResponse mResponse = mobileService.doOperationExternalFacture(mOperation, codeFse);

        return mResponse;
    }

    @GetMapping("/facturiers/liste-facturiers")
    @Timed
    public GrData getFacturierAndAttribute(@RequestParam String codeFse, @RequestParam String secteur) throws IOException, JAXBException {
        log.info("---------codeFse :{}", codeFse);
        log.info("---------secteur :{}", secteur);
        //WALLET, AUTRE, AIRTIME 
        GrData grData = mobileService.getListFacturierFromFse(codeFse, secteur);

        return grData;

    }

    @PostMapping("/otp/traiteOTP")
    @Timed
    public com.m2i.pti.service.dto.Message genererOTP(@RequestParam(value = "otp", required = false) String otp,
            @RequestParam String action, @RequestParam(value = "valeurCompte", required = false) String valeurCompte, @RequestParam(value = "codeAgence", required = false) String codeAgence, @RequestParam(value = "teneur", required = false) String teneur) throws IOException, JAXBException {
        com.m2i.pti.service.dto.Message msg = new com.m2i.pti.service.dto.Message();
        if (action.equals(ActionOTP.GENERER.name())) {
            msg = this.mobileService.generateAndSendCodeOTP(valeurCompte, codeAgence, teneur);
        } else if (action.equals(ActionOTP.VERIFIER.name())) {
            msg = this.mobileService.verifyOTP(otp, valeurCompte);
        }

        return msg;
    }

    @GetMapping("/activite/liste-activite")
    @Timed
    public List<ActiviteClientDTO> getActiviteClient(@RequestParam String codeTelephone, @RequestParam String telephone) throws IOException, JAXBException {

        //WALLET, AUTRE, AIRTIME 
        List<ActiviteClientDTO> listactiviteClientDTO = activiteClientService.getActiviteClient(codeTelephone, telephone);

        return listactiviteClientDTO;

    }

    @GetMapping("/operations/dataTransaction")
    @Timed
    public MDataTransaction getDataTransaction(
            @RequestParam String codeFse,
            @RequestParam String telephone,
            @RequestParam String code,
            @RequestParam String canal,
            @RequestParam String typeService,
            @RequestParam String codePv,
            @RequestParam(required = false) String codeUoDest,
            @RequestParam(required = false) String codeUoSrc,
            @RequestParam BigDecimal montant, @RequestParam String codeAgence, @RequestParam String typeCompte,
            @RequestParam String valeurCompte,
            @RequestParam String teneur, @RequestParam(required = false) String codeacteur) throws IOException, JAXBException {

        //  MClient mclient = mobileService.findInfosClient(telephone, code);
        //if (isMonnaieElectronique) {
        // mclient = mobileService.findInfosClient(telephone, code);
        //  } else {
        MClient mclient = new MClient();
        MDataTransaction dataTransaction = mobileService.getInfosDataTransaction(codeFse, telephone, code, canal, typeService, codePv, codeUoDest, codeUoSrc, montant.toString(), codeAgence, typeCompte, valeurCompte, teneur, codeacteur, "0");
        if (mclient == null || (mclient != null && mclient.getCompte() == null)) {
            if (typeCompte.equals(MTypeCompte.BANQUE.name()) || typeCompte.equals(MTypeCompte.CARTE.name())) {
                mclient = mobileService.findInfosClientExterne(valeurCompte, codeAgence, teneur);
            } else if (typeCompte.equals(MTypeCompte.AUTRE.name())) {
                mclient = mobileService.findInfosClientExterne(telephone, code, teneur);
            }
        }

        // }
        if (dataTransaction != null) {
            dataTransaction.setBeneficiaire(mclient);
        }

        return dataTransaction;

    }

}
