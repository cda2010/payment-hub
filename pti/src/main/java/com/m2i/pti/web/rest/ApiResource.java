package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.outils.util.StandardResponse;
import com.m2i.pti.domain.Compte;
import com.m2i.pti.mobile.dto.MtypeDoc;
import com.m2i.pti.service.ApiService;
import com.m2i.pti.service.CommissionService;
import com.m2i.pti.service.dto.*;
import com.m2i.pti.service.kafka.MessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import com.m2i.pti.service.BackService;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import com.m2i.omnicanal.outils.util.ResponseStatus;
import com.m2i.pti.mobile.dto.MParametre;
import java.util.ArrayList;
import jakarta.xml.bind.JAXBException;

@RestController
@RequestMapping("/api")
public class ApiResource {

    private final Logger log = LoggerFactory.getLogger(ApiResource.class);
    private ApiService apiService;
    private final CommissionService commissionService;
    private BackService backService;

    private final MessageProducer messageProducer;

    public ApiResource(ApiService apiService, CommissionService commissionService, MessageProducer messageProducer, BackService backService) {
        this.apiService = apiService;
        this.commissionService = commissionService;
        this.messageProducer = messageProducer;
        this.backService = backService;

    }

    @GetMapping("/omnnicanal/fournisseur")
    @Timed
    public List<FseDTO> getList() throws IOException {
        return apiService.getListeFournisseur();
    }

    @GetMapping("/omnnicanal/listRelation")
    @Timed
    public List<RelationDto> getListRelationsFse() throws IOException {
        return apiService.getListeRelationFournisseur();
    }

    @GetMapping("/omnnicanal/comptes_fse")
    @Timed
    public ResponseEntity getListRelationsFse(@RequestParam String etiquette, @RequestParam String codeFse) throws IOException {
        return apiService.getCompteOfFse(codeFse, etiquette);
    }

    /**
     * Envoi d'sms
     *
     * @param numero
     * @param message
     * @return
     * @throws IOException
     */
    @GetMapping("/omnnicanal/sendSms")
    @Timed
    public ResponseEntity sendSMS(@RequestParam String numero, @RequestParam String message) throws IOException {
        apiService.sendMessage(numero, message, "");
        return ResponseEntity.ok().body("OK");
    }

    @GetMapping("/omnnicanal/mail")
    @Timed
    public ResponseEntity sendMail(@RequestParam String mail, @RequestParam String message) throws IOException {

        apiService.sendMail(mail, message);
        return ResponseEntity.ok().body("OK");
    }

    @GetMapping("/omnnicanal/codeRetrait/cardLess")
    @Timed
    public ResponseEntity getCodeRetraitCardLess(@RequestParam String telephone, @RequestParam String montant, @RequestParam String reference) throws IOException {
        return apiService.getCodeRetraitGab(telephone, montant, reference);
    }

    @PostMapping("/omnnicanal/virementSimple")
    @Timed
    public ResponseEntity virementSimple(@RequestBody Compte compteSrc, @RequestBody Compte compteDest, @RequestParam BigDecimal montant) throws IOException {
        return null;
        // return apiService.virementSimple(compteSrc, compteDest, montant);
    }

    /**
     * RecupÃ¨re les informations d'un compte CBI
     *
     * @param compte
     * @param cle
     * @param agence
     * @return
     * @throws IOException
     */
    @GetMapping("/omnnicanal/infoClient")
    @Timed
    public PersonneAllDTO getInfosClient(@RequestParam String compte, @RequestParam String cle, @RequestParam String agence, @RequestParam Long codeFse) throws IOException, JAXBException {
        return apiService.getClientInfo(compte, agence, cle, codeFse);
    }

    @GetMapping("/omnnicanal/compense")
    @Timed
    public FseDTO getCompense(@RequestParam String codeFse) throws IOException {
        return apiService.getFseCompense(codeFse);
    }

    @GetMapping("/omnnicanal/get_solde")
    @Timed
    public Balance getSolde(@RequestParam(required = false) String agence, @RequestParam String numeroCompte, @RequestParam String codeFse) throws IOException, JAXBException {
        return apiService.getSolde(numeroCompte, agence, codeFse);
    }

       @GetMapping("/omnnicanal/getStoppage")
    @Timed
    public StoppageData getStoppage(@RequestParam(required = false) String agence, @RequestParam String numeroCompte, @RequestParam String codeFse, @RequestParam String sens, @RequestParam String isInit, @RequestParam(required = false) String eventId,@RequestParam(required = false) String codeBanque, @RequestParam String montant) throws IOException, JAXBException {
        
     
            return apiService.getStoppage(numeroCompte, agence, codeFse, sens, eventId, isInit,montant);
        
        
        
    }

    
    
    @GetMapping("/omnnicanal/consultation_historique")
    @Timed
    public Statement getHistoriqueCompte(@RequestParam(required = false) String agence, @RequestParam String numeroCompte, @RequestParam String codeFse,
            @RequestParam(required = false) String debutPeriode, @RequestParam(required = false) String finPeriode) throws IOException, JAXBException {
        return apiService.getHistorique(numeroCompte, agence, codeFse, debutPeriode, finPeriode);
    }

    @GetMapping("/omnnicanal/mini-releve")
    @Timed
    public Statement getMiniReleveCompte(@RequestParam(required = false) String agence, @RequestParam String numeroCompte, @RequestParam String codeFse) throws IOException, JAXBException {
        return apiService.getMiniReleve(numeroCompte, agence, codeFse);
    }

    @GetMapping("/omnnicanal/envoi_kafka")
    @Timed
    public void getKafkaMessage() throws IOException, JAXBException {
        apiService.sendMessage("22676477115", "sms", null);
    }

    @GetMapping("/backend/typedoc")
    @Timed
    public List<MtypeDoc> getTypeDoc() throws IOException {
        return backService.getTypeDoc();
    }

    @GetMapping("/backend/resetPassord")
    @Timed
    public ResponseEntity resetPwd(@RequestParam("login") String login) throws IOException {
        return backService.resetPwd(login);
    }

    @GetMapping("/backend/userDetail")
    @Timed
    public ResponseEntity userDetail(@RequestParam("telephone") String telephone) throws IOException {
        return backService.checkuser(telephone);
    }

    @GetMapping("/allow-machine")
    @Timed
    public void allow(@RequestParam("adresseIp") String adresseIp) {
        log.debug("allow machine" + adresseIp);
        apiService.allowMachine(adresseIp);
    }

    @GetMapping("/activeMachine")
    @Timed
    public void activeMachine(@RequestParam("uuid") String uuid) throws IOException {
        log.debug("Active machine");
        apiService.activeMachine(uuid);
    }

    @PostMapping("/sendCardless")
    @Timed
    //@PostAuthorize("hasIpAddress('192.168.0.136')")
    public StandardResponse sendCardless(
            @RequestParam("telephone") String telephone,
            @RequestParam("montant") String montant,
            @RequestParam("reference") String reference,
            @RequestParam("entityCode") String entityCode
    ) throws UnsupportedEncodingException, NoSuchAlgorithmException, KeyManagementException, JAXBException, IOException {
        StandardResponse result = apiService.setCardLessTransactionCrypted(telephone,
                montant,
                reference,
                entityCode);
        //return new ResponseEntity(msg,HttpStatus.CREATED);
        return result;

    }

    @PostMapping("/getCardlessStatus")
    @Timed
    //@PostAuthorize("hasIpAddress('192.168.0.136')")
    public StandardResponse getCardlessStatus(
            @RequestParam("entityCode") String entityCode,
            @RequestParam("reference") String reference,
            @RequestParam("codeFse") String codeFse
    ) throws UnsupportedEncodingException, NoSuchAlgorithmException, KeyManagementException, JAXBException, IOException {
        List<MParametre> listMParametre = new ArrayList();
        MParametre mp = new MParametre("ENTITYCODE", entityCode);
        listMParametre.add(mp);
        mp = new MParametre("REFERENCE", reference);
        listMParametre.add(mp);
        Message msgStatut = apiService.checkStatus(codeFse, listMParametre);
        log.debug("FIN DE LA VERIFICATION  DU STATUT DE REQUETE  .." + reference);
        StandardResponse sResponse = new StandardResponse();
        if (msgStatut != null) {
            sResponse.setResponseStatus(new ResponseStatus(msgStatut.getCodeErr(), msgStatut.getMsg()));
        }

        //return new ResponseEntity(msg,HttpStatus.CREATED);
        return sResponse;

    }

    @PostMapping("/getVoucherCardLess")
    @Timed
    //@PostAuthorize("hasIpAddress('192.168.0.136')")
    public StandardResponse getVoucher(
            @RequestParam("telephone") String telephone,
            @RequestParam("montant") String montant,
            @RequestParam("reference") String reference,
            @RequestParam("entityCode") String entityCode, @RequestParam("codeFse") String codeFse
    ) throws UnsupportedEncodingException, NoSuchAlgorithmException, KeyManagementException, JAXBException, IOException {
        com.m2i.omnicanal.outils.restclient.model.Message msg = apiService.setCardLessTransaction(
                telephone,
                telephone,
                montant,
                reference,
                entityCode,
                codeFse);
        StandardResponse sResponse = new StandardResponse();
         if (msg != null) {
              sResponse.setResponseStatus(new ResponseStatus(msg.getCodeErr(), msg.getMsg()));
         }
       
        //return new ResponseEntity(msg,HttpStatus.CREATED);
        return sResponse;

    }

}
