package com.m2i.pti.mobile.rest;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.EvenementOperation;
import com.m2i.pti.domain.Operation;
import com.m2i.pti.domain.ParametreApp;
import com.m2i.pti.domain.Produit;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.User;
import com.m2i.pti.domain.enumeration.ParamatreFacturier;
import com.m2i.pti.domain.enumeration.TypeFacturier;
import com.m2i.pti.domain.enumeration.TypeFrais;
import com.m2i.pti.domain.enumeration.TypeParametre;
import com.m2i.pti.domain.enumeration.TypeServiceLibelle;
import com.m2i.pti.domain.util.CacheObject;
import com.m2i.pti.external.domain.GrData;
import com.m2i.pti.mobile.dto.UserExist;
import com.m2i.pti.mobile.dto.*;
import com.m2i.pti.mobile.enumeration.MTypeCompte;
import com.m2i.pti.mobile.enumeration.MTypeOrganisation;
import com.m2i.pti.mobile.service.MobileService;
import com.m2i.pti.mobile.service.TransfertAppService;
import com.m2i.pti.repository.EvenementOperationRepository;
import com.m2i.pti.security.AuthoritiesConstants;
import com.m2i.pti.service.ApiService;
import com.m2i.pti.service.CompteService;
import com.m2i.pti.service.ParametreAppService;
import com.m2i.pti.service.UserControlService;
import com.m2i.pti.service.UserService;
import com.m2i.pti.service.dto.Balance;
import com.m2i.pti.service.dto.CollectionFacture;
import com.m2i.pti.service.dto.CompteDTO;
import com.m2i.pti.service.dto.CustomerInformation;
import com.m2i.pti.service.dto.ElementFacture;
import com.m2i.pti.service.dto.FactureFournisseur;
import com.m2i.pti.service.dto.Message;
import com.m2i.pti.service.dto.Mouvement;
import com.m2i.pti.service.dto.RegleFacturier;
import com.m2i.pti.service.dto.Statement;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.ZoneId;

import java.time.ZonedDateTime;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/api/mobile")
public class MobileResource {

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

    public MobileResource(ParametreAppService parametreAppService, EvenementOperationRepository evenementOperationRepository, UserService userService, TransfertAppService transfertAppService, MobileService mobileService, ApiService apiService, CompteService compteService, UserControlService userControlService, ApplicationContext appContext) {
        this.mobileService = mobileService;
        this.apiService = apiService;
        this.compteService = compteService;
        this.userControlService = userControlService;
        this.transfertAppService = transfertAppService;
        this.userService = userService;
        this.evenementOperationRepository = evenementOperationRepository;
        this.appContext = appContext;
        this.parametreAppService = parametreAppService;
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/ecrans/of-produit")
    @Timed
    public List<MEcran> getEcransByProduitId(@RequestParam Long id) {
        return mobileService.getEcransOfProduit(id);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/user/authentificationforte")
    @Timed
    public String isAuthentificationForte() {
        String isAuthen = "0";

        isAuthen = mobileService.isAuthentificationForte();

        return isAuthen;
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/uo/listUoForPickup")
    @Timed
    public List<MOrganisation> getUoList(@RequestParam String codeUo, @RequestParam String type) {
        return transfertAppService.getUoAllFilsByType(codeUo, MTypeOrganisation.valueOf(type));
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/tauxchange/tauxchangedujour")
    @Timed
    public Long gtTauxChange(@RequestParam String devDepart, @RequestParam String devArrivee, @RequestParam String sens) {
        BigDecimal taux = BigDecimal.ONE;
        if (sens.equals("ACHAT")) {
            taux = mobileService.getTauxChangeAchat(devDepart, devArrivee, LocalDate.now());
        } else if (sens.equals("VENTE")) {
            taux = mobileService.getTauxChangeVente(devDepart, devArrivee, LocalDate.now());
        }

        return taux.longValue();
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/produits/of-facturier")
    @Timed
    public List<MProduit> getProduitsOfFacturier(@RequestParam Long id) {
        return mobileService.getPoduitsOfFacturier(id);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/produits/of-facturier-uo")
    @Timed
    public List<MProduit> getProduitsMarchand(@RequestParam String codeUo) {
        return mobileService.getPoduitsOfFacturierFromUo(codeUo);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/facturiers/of-marchand-uo")
    @Timed
    public List<MFacturier> getFacturiersOfMarchand(@RequestParam String codeUo) {
        return mobileService.getFacturierFromUo(codeUo);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/factures/of-facturier-uo")
    @Timed
    public List<MfactureMarchand> getFacturesMarchand(@RequestParam Long id) throws JAXBException {
        return mobileService.getFactureMarchand(id);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/classification-facturiers/of-Uo")
    @Timed
    public List<MClassificationFacturier> getClassificationFacturierOfUo(@RequestParam String code) {
        return mobileService.getClassificationFacturierOfUo(code);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/facturiers/of-classification")
    @Timed
    public List<MFacturier> getFacturierOfClassification(@RequestParam Long id, @RequestParam Optional<String> canal) {
        if (canal.isPresent()) {
            return mobileService.getFacturierOfClassificationAndCanal(id, canal.get());
        } else {
            return mobileService.getFacturierOfClassification(id);
        }

    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/facturiers/telephonie")
    @Timed
    public List<MFacturier> getFacturierOfClassification(@RequestParam String codeFse) {
        return mobileService.getOperateurTelephonie(codeFse);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/publicite/listePublicite")
    @Timed
    public List<MPublicite> getListPublicite(@RequestParam String codeIndicatif) {
        return mobileService.getPublicite(codeIndicatif);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/notifications/listeNotifcation")
    @Timed
    public List<MNotification> getListNotification(@RequestParam String numero, @RequestParam String codeIndicatif) {
        return mobileService.getNotification(numero, codeIndicatif);
    }

    /*
    Opération
     */
    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/operations/do_op")
    @Timed
    public MResponse doOperation(@RequestBody MOperation mOperation) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.debug("REST request to save Operation : {}", mOperation);
        MResponseOperation rep = mobileService.doOperation(mOperation);
        List<MParametre> extraData = null;
        if (rep != null) {
            Compte cpt = null;
            String transactionId = "0";
            if (rep.getOperation() != null) {
                extraData = mobileService.populateMessage(rep.getOperation());
                //   if (!rep.getOperation().getTypeService().getCode().equals(TypeServiceLibelle.ACCOUNT_CASH.toString())) {
                mobileService.envoiDesMessage(rep.getOperation(), extraData);
                // }               
                mobileService.repartirCommission(rep.getOperation());
                transactionId = rep.getOperation().getCodeOperation();
                mobileService.sendCashBack(rep.getOperation());

            }
            return new MResponse(rep.getCode(), rep.getMessage(), transactionId, extraData);
        }
        return new MResponse("-1", "Erreur inconnue");
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/client/verification")
    @Timed
    public UserExist verification(@RequestParam(required = true) String codePays, @RequestParam(required = true) String login, @RequestParam(required = false) String password) {
        return userControlService.verification(login, codePays, password);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/client/deleteentry")
    @Timed
    public MResponse deleteUserControl(@RequestParam Long id) {
        log.debug("REST request to delete UserControl : {}", id);
        if (userControlService.delete(id)) {
            return new MResponse("0", "Opération terminée avec succès");
        }
        return new MResponse("-1", "Erreur !");
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/client/changepassword")
    @Timed
    public MResponse changePasswordUserControl(@RequestParam String login, @RequestParam String password) {
        log.debug("REST request to change pass UserControl : {}", login);
        User user = userService.findeByLogin(login);
        boolean bool = userService.changePassword(user, password);
        if (bool) {

            return new MResponse("0", "Opération terminée avec succès");

        }

        return new MResponse("-1", "Erreur !");
    }

    /*
    Opération
     */
    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/client/infosexist")
    @Timed
    public MClient getClientInfoExit(@RequestParam String telephone, @RequestParam String code) {
        log.debug("REST request to customer {}", telephone + "code--" + code);
        MClient client = mobileService.findInfosClient(telephone, code);
        if (client != null && client.getIdtId() != null && client.getPersonne() != null) {
            mobileService.desactiveCLient(client.getPersonne());
        }
        return client;
    }

    /*
    Opération
     */
    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/client/deactive")
    @Timed
    public MResponse doDesactivation(@RequestBody MPersonne mpersonne) throws IOException, NoSuchAlgorithmException {
        log.debug("REST request to active {}", mpersonne);
        MResponse rep = mobileService.desactiveCLient(mpersonne);
        return rep;
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/agent/deactive")
    @Timed
    public MResponse doDesactivationAgent(@RequestBody MPersonne mpersonne) throws IOException, NoSuchAlgorithmException {
        log.debug("REST request to active {}", mpersonne);
        MResponse rep = mobileService.desactiveAgent(mpersonne);
        return rep;
    }

    /*
    Paiement de facture
     */
    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/operations/do_facturecredit")
    @Timed
    public MResponse doOperationCredit(@RequestBody MOperationFacture mOperation) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.debug("REST request to save Operation : {}", mOperation);
        MResponseOperation rep = mobileService.doOperationPaiementCredit(mOperation);
        log.debug("reponse : {}", rep);
        String transactionId = "0";
        List<MParametre> extraData = null;
        if (rep != null) {
            if (rep.getOperation() != null) {
                extraData = mobileService.populateMessage(rep.getOperation());
                mobileService.envoiDesMessage(rep.getOperation(), extraData);
                mobileService.repartirCommission(rep.getOperation());
                transactionId = rep.getOperation().getCodeOperation();
                   mobileService.sendCashBack(rep.getOperation());
                return new MResponse(rep.getCode(), rep.getMessage(), transactionId, extraData);
            } else {
                return new MResponse(rep.getCode(), rep.getMessage());
            }

            //return new MResponse(rep.getCode(), rep.getMessage());
        }
        return new MResponse("-1", "Erreur inconnue");
    }

    /*
    Paiement de facture
     */
    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/operations/do_facturepaiement")
    @Timed
    public MResponse doOperation(@RequestBody MOperationFacture mOperation) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.debug("REST request to save Operation : {}", mOperation);
        MResponse rep = mobileService.doOperationPaiementFacture(mOperation);
        log.debug("reponse : {}", rep);
        String transactionId = "0";
        List<MParametre> extraData = null;
        if (rep != null && rep.getTansactionId() != null) {

            Operation oper = mobileService.getOperation(Long.parseLong(rep.getTansactionId()));
            if (rep.getExtraData() != null) {
                extraData = rep.getExtraData();
            } else {
                extraData = mobileService.populateMessage(oper);
            }

            mobileService.envoiDesMessage(oper, rep.getExtraData());
            mobileService.repartirCommission(oper);
            transactionId = oper.getCodeOperation();
            mobileService.sendCashBack(oper);
            return new MResponse(rep.getCode(), rep.getMessage(), transactionId, extraData);

        } else if (rep != null) {
            return new MResponse(rep.getCode(), rep.getMessage());
        }
        return new MResponse("-1", "Erreur inconnue");
    }

    /*
    Paiement de facture
     */
    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/operations/do_facturepaiementcash")
    @Timed
    public MResponse doOperationFact(@RequestBody MOperationFacture mOperation) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.debug("REST request to save Operation : {}", mOperation);
        MResponse rep = mobileService.doOperationPaiementFactureDepuisCash(mOperation);
        log.debug("reponse : {}", rep);
        String transactionId = "0";
        List<MParametre> extraData = null;
        if (rep != null && rep.getTansactionId() != null) {
            Operation oper = mobileService.getOperation(Long.parseLong(rep.getTansactionId()));
            if (rep.getExtraData() != null) {
                extraData = rep.getExtraData();
            } else {
                extraData = mobileService.populateMessage(oper);
            }
            mobileService.envoiDesMessage(oper, extraData);
            mobileService.repartirCommission(oper);
            transactionId = oper.getCodeOperation();
            return new MResponse(rep.getCode(), rep.getMessage(), transactionId, extraData);
        } else if (rep != null) {
            return new MResponse(rep.getCode(), rep.getMessage());
        }
        return new MResponse("-1", "Erreur inconnue");
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/users/create")
    @Timed
    public MResponse createUser(@RequestBody MUser muser) throws IOException, NoSuchAlgorithmException {
        log.debug("REST request to save Operation : {}", muser);
        return mobileService.createUser(muser);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/personne/find-personne-physique")
    @Timed
    public MResponse findPersonnePhysique(@RequestParam String telephone, @RequestParam String code, @RequestParam String email) throws IOException, NoSuchAlgorithmException {
        log.debug("REST findpersonneMorale : {}", telephone);
        return mobileService.findPersonnePhysique(code, email, telephone);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/personne/find-personne")
    @Timed
    public MResponse findPersonneMorale(@RequestParam String telephone, @RequestParam String code, @RequestParam String email, @RequestParam String qualif) throws IOException, NoSuchAlgorithmException {
        log.debug("REST findpersonneMorale : {}", telephone);
        return mobileService.findPersonneInscrit(code, email, telephone, qualif);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/compte/verifIntegrite")
    @Timed
    public MResponse findCompteIfCorrect(@RequestParam(required = false) String numeroCompte, @RequestParam String acteur, @RequestParam String identifiant, @RequestParam(required = false) String codeIndicatif) throws IOException, NoSuchAlgorithmException {
        Compte cpt = mobileService.verifIfAutoriser(numeroCompte, acteur, identifiant, codeIndicatif);
        if (cpt != null) {
            return new MResponse("0", "OK");
        } else {
            return new MResponse("-1", "Erreur inconnue!!");
        }

    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/operations/do_op_retrait")
    @Timed
    public MResponse doOperationRetrait(@RequestBody MOperation mOperation) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.debug("REST request to save Operation : {}", mOperation);
        MResponseOperation rep = mobileService.doOperationRetrait(mOperation);
        String transactionId = "0";
        List<MParametre> extraData = null;
        if (rep != null) {
            if (rep.getOperation() != null) {
                extraData = mobileService.populateMessage(rep.getOperation());
                mobileService.envoiDesMessage(rep.getOperation(), extraData);
                mobileService.repartirCommission(rep.getOperation());
                transactionId = rep.getOperation().getCodeOperation();

            }
            return new MResponse(rep.getCode(), rep.getMessage(), transactionId, extraData);
        }
        return new MResponse("-1", "Erreur inconnue!!");
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/operations/do_op_envoi")
    @Timed
    public MResponse doOperationEnvoiCash(@RequestBody MOperation mOperation) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.debug("REST request to save Operation : {}", mOperation);
        List<MParametre> extraData = null;
        MResponseOperation rep = mobileService.doOperationEnvoiCash(mOperation);
        String transactionId = "0";
        if (rep != null) {
            if (rep.getOperation() != null) {
                extraData = mobileService.populateMessage(rep.getOperation());
                mobileService.envoiDesMessage(rep.getOperation(), extraData);
                mobileService.repartirCommission(rep.getOperation());
                transactionId = rep.getOperation().getCodeOperation().toString();

            }

            return new MResponse(rep.getCode(), rep.getMessage(), transactionId);
        }
        return new MResponse("-1", "Erreur inconnue");
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/operations/do_op_transfert_cash_vers_compte")
    @Timed
    public MResponse doOperationTransfertVersCompte(@RequestBody MOperation mOperation) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.debug("REST request to save Operation : {}", mOperation);
        MResponseOperation rep = mobileService.doOperationTransfertVersCompte(mOperation);
        List<MParametre> extraData = null;
        String transactionId = "0";
        if (rep != null) {

            if (rep.getOperation() != null) {
                extraData = mobileService.populateMessage(rep.getOperation());
                mobileService.envoiDesMessage(rep.getOperation(), extraData);
                mobileService.repartirCommission(rep.getOperation());
                transactionId = rep.getOperation().getCodeOperation();
                extraData = mobileService.populateMessage(rep.getOperation());
            }

            return new MResponse(rep.getCode(), rep.getMessage(), transactionId, extraData);
        }
        return new MResponse("-1", "Erreur inconnue");
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/operations/do_op_retrait_compte")
    @Timed
    public MResponse doOperationRetraitCompte(@RequestBody MOperation mOperation) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.debug("REST request to save Operation : {}", mOperation);
        MResponseOperation rep = mobileService.doOperationRetraitCompte(mOperation);
        String transactionId = "0";
        List<MParametre> extraData = null;
        if (rep != null) {
            if (rep.getOperation() != null) {
                mobileService.repartirCommission(rep.getOperation());
                extraData = mobileService.populateMessage(rep.getOperation());
                mobileService.envoiDesMessage(rep.getOperation(), extraData);
                transactionId = rep.getOperation().getCodeOperation();
                extraData = mobileService.populateMessage(rep.getOperation());
            }
            return new MResponse(rep.getCode(), rep.getMessage(), transactionId, extraData);
        }
        return new MResponse("-1", "Erreur inconnue");

    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/operations/do_op_retrait_cash")
    @Timed
    public MResponse doOperationRetraitCash(@RequestBody MOperation mOperation) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.debug("REST request to save Operation : {}", mOperation);
        MResponseOperation rep = mobileService.doOperationRetraitCash(mOperation);
        String transactionId = "0";
        List<MParametre> extraData = null;
        if (rep != null) {
            if (rep.getOperation() != null) {
                mobileService.repartirCommission(rep.getOperation());
                transactionId = rep.getOperation().getCodeOperation();
            }
            return new MResponse(rep.getCode(), rep.getMessage(), transactionId, extraData);
        }
        return new MResponse("-1", "Erreur inconnue");

    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/operations/do_approvisionnement")
    @Timed
    public MResponse doOperationApprovisionnement(@RequestBody MOperation mOperation) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.debug("REST request to save Operation : {}", mOperation);
        log.info("DEBUT APPROVISSIONNEMENT : {}", mOperation);
        MResponseOperation rep = mobileService.doOperationApprovisionnement(mOperation);
        log.info("FIN APPROVISSIONNEMENT : {}", mOperation);
        String transactionId = "0";
        List<MParametre> extraData = null;
        if (rep != null) {
            if (rep.getOperation() != null) {
                log.info("REPARTION DES COMMISSIONS : {}", mOperation);
                mobileService.repartirCommission(rep.getOperation());
                log.info("ENVOI DU SMS : {}", mOperation);
                extraData = mobileService.populateMessage(rep.getOperation());
                mobileService.envoiDesMessage(rep.getOperation(), extraData);

                transactionId = rep.getOperation().getCodeOperation();
            }
            return new MResponse(rep.getCode(), rep.getMessage(), transactionId, extraData);

        }
        return new MResponse("-1", "Erreur inconnue");

    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/operations/reSendMessage")
    @Timed
    public MResponse sendOperationMessage(@RequestParam String codeOperation) throws IOException, JAXBException {
        Operation operation = mobileService.getOperationFromCode(codeOperation);
        if (operation != null) {
            return mobileService.envoiOperationMessage(operation);
        }
        return new MResponse("-1", "Erreur inconnue");
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/operations/do_sweep")
    @Timed
    public MResponse doOperationSweeptoAccount(@RequestBody MOperation mOperation) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.debug("REST request to save Operation : {}", mOperation);
        MResponseOperation rep = mobileService.doDepositToBank(mOperation);
        String transactionId = "0";
        List<MParametre> extraData = null;
        if (rep != null) {
            if (rep.getOperation() != null) {
                extraData = mobileService.populateMessage(rep.getOperation());
                mobileService.envoiDesMessage(rep.getOperation(), extraData);
                mobileService.repartirCommission(rep.getOperation());
                transactionId = rep.getOperation().getId().toString();
            }
            return new MResponse(rep.getCode(), rep.getMessage(), transactionId);
        }
        return new MResponse("-1", "Erreur inconnue");

    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/operations/do_souscription")
    @Timed
    public MResponse doSouscription(@RequestBody MClient client) {
        return mobileService.doSouscription(client);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/operations/do_inscriptionTerminal")
    @Timed
    public MResponseOrga doInscriptionTerminal(@RequestBody MTerminal terminal) {
        return mobileService.doInscriptionTerminal(terminal);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/comptes/all_infos")
    @Timed
    public List<CompteDTO> findComptesByPersonne(@RequestParam String telephone, @RequestParam String code) {
        return mobileService.findComptes(telephone, code);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/organisation/getFromPv")
    @Timed
    public MOrganisation findOrgansiationFromPV(@RequestParam String codePv) {
        return mobileService.findOraganisationByCodePV(codePv);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/autorisation/listAutorisation")
    @Timed
    public List<MAutorisation> findMAutorisation(@RequestParam String codeUo) {
        return mobileService.findAutorisationUoByCode(codeUo);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/personnes/all_infos")
    @Timed
    public MClient findByPersonne(@RequestParam String telephone, @RequestParam String code) {
        log.info("Infos details" + telephone + "#" + code);
        MClient mClient = mobileService.findInfosClient(telephone, code);
        return mClient;
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/client/autorisations")
    @Timed
    public List<MAutorisation> getAutorisationsClient(@RequestParam String telephone, @RequestParam String code) {
        return mobileService.veriAuthClient(code, telephone);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/operations/trans/blocage")
    @Timed
    public MResponse blocageTransaction(@RequestParam String codeoperation) {
        return mobileService.doSuspension(codeoperation);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/operations/trans/detail")
    @Timed
    public MRetraitResponse detailTransaction(@RequestParam String codeoperation) {
        return mobileService.getOperationDetail(codeoperation);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/type-reclamation/list")
    @Timed
    public List<MTypeReclamation> getTypeReclamation() {
        return mobileService.getTypeReclamation();
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/geo-localisation/list")
    @Timed
    public List<MLocalisationOrganisation> getGeoLocalisation(@RequestParam String codeFse, @RequestParam String minLat, @RequestParam String maxLat, @RequestParam String minLon, @RequestParam String maxLon) {
        return mobileService.findLocalisationByType(codeFse, minLat, maxLat, minLon, maxLon);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/organisation/code")
    @Timed
    public MOrganisation getUniteOrganisation(@RequestParam String codeUo) {
        return mobileService.findOraganisationByCode(codeUo);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/type-reclamation/create")
    @Timed
    public MResponse createTypeReclamation(@RequestBody MReclamation mreclamation) throws IOException, NoSuchAlgorithmException {
        log.debug("REST requ²est to save reclation : {}", mreclamation);
        return mobileService.createTypeReclamation(mreclamation);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/operations/calcul_frais")
    @Timed
    public MFrais calculFrais(@RequestParam String canal, @RequestParam String typeService, @RequestParam String codePv,
            @RequestParam(required = false) String codeUoDest, @RequestParam(required = false) String codeUoSrc, @RequestParam BigDecimal montant, @RequestParam(required = false) String codeIndicatif, @RequestParam(required = false) String telephone) {
        MInfos minfo = null;
        if (codeIndicatif != null && telephone != null) {
            minfo = new MInfos();
            MPersonne titulaire = new MPersonne();
            titulaire.setCodeTelephone(codeIndicatif);
            titulaire.setTelephone(telephone);
            minfo.setTitulaire(titulaire);
        }
        log.debug("Requete pour connaitre les frais du service Calcul des frais");

        return mobileService.calculFrais(new MParamFrais(codePv, typeService, canal, codeUoDest, codeUoSrc, montant, minfo), TypeFrais.FRAIS);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/operations/retraitData")
    @Timed
    public MRetraitResponse getRetraitData(@RequestBody MOperation mOperation) throws NoSuchAlgorithmException {
        return mobileService.getDetailRetrait(mOperation);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/operations/motifs")
    @Timed
    public List<MMotif> getMotifs() {
        return mobileService.getMotifs();
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/operations/faq")
    @Timed
    public List<MFaq> getFaq() {
        return mobileService.getFaq();
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/operations/gab/agent")
    @Timed
    public MPointVente getCodeGabUo(@RequestParam String codePays) {
        log.info("Recherche des informations GAB ...");
        return mobileService.getCodeGabUo(codePays);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/comptes/agent")
    @Timed
    public ResponseEntity findAgentCompte(@RequestParam String codeUo) {
        log.info("Recherche des informations de comptes ..." + codeUo);
        MCompte mcpt = null;
        MCompteAgent cptAgent = new MCompteAgent();
        ParametreApp parametreApp = parametreAppService.findByTypeAndNom(TypeParametre.SYSTEM, "AFFICHER_COMPTE_PTI");
        Compte cptOperation;
        Compte cptCommission;
        Compte cptOperationBank = null;
        if (parametreApp != null && parametreApp.getValeur().equals("1")) {
            cptOperation = mobileService.findComptesAgent(codeUo, "CPTEOPERATIONACTEURPTI");
            cptCommission = mobileService.findComptesAgent(codeUo, "CPTECOMACTEURPTI");
        } else {
            cptOperation = mobileService.findComptesAgent(codeUo, "CPTEOPERATIONACTEUR");
            cptCommission = mobileService.findComptesAgent(codeUo, "CPTECOMACTEUR");
        }
        UniteOrganisation uo = mobileService.findUoByCode(codeUo);
        String nomOrganisation = "";
        if (uo != null) {
            nomOrganisation = uo.getLibelleUniteOrganisation();
        }
        if (cptOperation != null) {
            mcpt = new MCompte();
            mcpt.setTeneurCompte(cptOperation.getCompteAutre().getCodeFse());
            mcpt.setTypeCompte(MTypeCompte.AUTRE);
            mcpt.setNumeroCompte(cptOperation.getValeurCompte());
            cptAgent.setSoldeOperation(cptOperation.getCompteAutre().getSolde());
            cptAgent.setCompteOperation(mcpt);
        }
        if (cptCommission != null) {
            mcpt = new MCompte();
            mcpt.setTeneurCompte(cptCommission.getCompteAutre().getCodeFse());
            mcpt.setTypeCompte(MTypeCompte.AUTRE);
            mcpt.setNumeroCompte(cptCommission.getValeurCompte());
            cptAgent.setSoldeCommission(cptCommission.getCompteAutre().getSolde());
            cptAgent.setCompteCommission(mcpt);
        }

        cptAgent.setNomOrganisation(nomOrganisation);
        return new ResponseEntity<>(cptAgent, HttpStatus.OK);
    }

    /**
     * Recuperations des comptes d'un utilisateur en fonction de son telephone
     *
     * @param telephone
     * @param code
     * @return
     */
    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/comptes/infos")
    @Timed
    public ResponseEntity findCompteByPersonne(@RequestParam String telephone, @RequestParam String code) {
        log.info("Recherche des informations de comptes ...");
        List<CompteDTO> compteDTOS = mobileService.findComptes(telephone, code);
        List<MCompte> comptes = new ArrayList<>();
        compteDTOS.forEach(compteDTO -> {
            MCompte compte = new MCompte();
            compte.setNumeroCompte(compteDTO.getValeurCompte());
            if (compteDTO.getCompteBanqueId() != null && compteDTO.getCompteBanqueDTO() != null) {
                compte.setCodeAgence(compteDTO.getCompteBanqueDTO().getCodeAgence());
                compte.setCodeBanque(compteDTO.getCompteBanqueDTO().getCodeBanque());
                compte.setTeneurCompte(compteDTO.getCompteBanqueDTO().getCodeFse());
                compte.setRib(compteDTO.getCompteBanqueDTO().getRib());
                compte.setTypeCompte(MTypeCompte.BANQUE);
                if (compteDTO.getAlias() != null) {
                    compte.setAlias(compteDTO.getAlias());
                }

            } else if (compteDTO.getCompteAutreDTO() != null) {
                compte.setTeneurCompte(compteDTO.getCompteAutreDTO().getCodeFse());
                compte.setTypeCompte(MTypeCompte.AUTRE);
            } else {
                compte.setTeneurCompte(compteDTO.getCarteDTO().getCodeFse());
                compte.setTypeCompte(MTypeCompte.CARTE);
                compte.setCodeAgence(compteDTO.getCarteDTO().getLast4Digit());
            }
            comptes.add(compte);
        });
        return new ResponseEntity<>(comptes, HttpStatus.OK);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/infos/acteur")
    @Timed
    public MPersonne findInfoActeur(@RequestParam(required = false) String telephone, @RequestParam String acteur, @RequestParam(required = false) String codeActeur, @RequestParam(required = false) String code) {
        MPersonne mpers = mobileService.findInfosPersonne(telephone, code, acteur, codeActeur);
        return mpers;
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/infos/orga")
    @Timed
    public MOrganisation findInfoActeur(@RequestParam String codeActeur) {
        MOrganisation morga = mobileService.findInfosOrga(codeActeur);
        return morga;
    }

    /**
     * @return
     */
    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/pays/infos")
    @Timed
    public List<MOrganisation> getPays() {
        return mobileService.findOraganisationByType(MTypeOrganisation.PAYS);
    }

    @GetMapping("/donnee/dataTransaction")
    @Timed
    public MDataTransaction getDataTransaction(
            @RequestParam String telephone,
            @RequestParam String code,
            @RequestParam String canal,
            @RequestParam String typeService,
            @RequestParam String codePv,
            @RequestParam(required = false) String codeUoDest,
            @RequestParam(required = false) String codeUoSrc,
            @RequestParam BigDecimal montant, String codeacteur) throws IOException, JAXBException {
        MInfos minfo = null;
        if (!code.equals(null) && !telephone.equals(null)) {
            minfo = new MInfos();
            MPersonne titulaire = new MPersonne();
            titulaire.setCodeTelephone(code);
            titulaire.setTelephone(telephone);
            minfo.setTitulaire(titulaire);
        }
        MDataTransaction dataTransaction = new MDataTransaction();
        dataTransaction.setMfrais(mobileService.calculFrais(new MParamFrais(codePv, typeService, canal, codeUoSrc, codeUoDest, montant, minfo), TypeFrais.FRAIS));
        if (codeacteur != null && !codeacteur.isEmpty()) {
            UniteOrganisation uo = mobileService.findUoByCode(codeacteur);
            if (uo != null) {
                dataTransaction.setNomUo(uo.getLibelleUniteOrganisation());
            }

        }
        MClient mclient = mobileService.findInfosClient(telephone, code);
        if (mclient == null) {
            MPersonne infos = new MPersonne();
            mclient = new MClient();

            //  mclient = mobileService.findInfosClientExterne(telephone, code, "0021");
        }
        dataTransaction.setBeneficiaire(mclient);
        return dataTransaction;

    }

    @GetMapping("/donnee/dataTransactionXXXXX")
    @Timed
    public MDataTransaction getDataTransaction(
            @RequestParam String telephone,
            @RequestParam String code,
            @RequestParam String canal,
            @RequestParam String typeService,
            @RequestParam String codePv,
            @RequestParam(required = false) String codeUoDest,
            @RequestParam(required = false) String codeUoSrc,
            @RequestParam BigDecimal montant, @RequestParam String codeAgence, @RequestParam String typeCompte,
            @RequestParam String valeurCompte,
            @RequestParam String teneur, @RequestParam String codeacteur) throws IOException, JAXBException {
        MInfos minfo = null;
        if (!code.equals(null) && !telephone.equals(null)) {
            minfo = new MInfos();
            MPersonne titulaire = new MPersonne();
            titulaire.setCodeTelephone(code);
            titulaire.setTelephone(telephone);
            minfo.setTitulaire(titulaire);
        }
        MDataTransaction dataTransaction = new MDataTransaction();
        String fseDef = mobileService.getFournisseurMonnaieDef(code);
        boolean isMonnaieElectronique = mobileService.getIfMonnaieDef(fseDef);

        dataTransaction.setMfrais(mobileService.calculFrais(new MParamFrais(codePv, typeService, canal, codeUoSrc, codeUoDest, montant, minfo), TypeFrais.FRAIS));
        if (codeacteur != null && !codeacteur.isEmpty()) {
            UniteOrganisation uo = mobileService.findUoByCode(codeacteur);
            if (uo != null) {
                dataTransaction.setNomUo(uo.getLibelleUniteOrganisation());
            }
        }
        MClient mclient = new MClient();
        //  MClient mclient = mobileService.findInfosClient(telephone, code);
        if (isMonnaieElectronique) {
            mclient = mobileService.findInfosClient(telephone, code);
        } else {
            if (mclient == null || (mclient != null && mclient.getCompte() == null)) {
                if (typeCompte.equals(MTypeCompte.BANQUE.name())) {
                    mclient = mobileService.findInfosClientExterne(valeurCompte, codeAgence, teneur);
                } else if (typeCompte.equals(MTypeCompte.AUTRE.name())) {
                    mclient = mobileService.findInfosClientExterne(telephone, code, teneur);
                }
            }
        }

        dataTransaction.setBeneficiaire(mclient);
        return dataTransaction;
    }

    @GetMapping("/donnee/dataTransactionExterne")
    @Timed
    public MDataTransaction getDataTransactionExterne(
            @RequestParam(required = false) String telephone,
            @RequestParam(required = false) String code,
            @RequestParam String canal,
            @RequestParam String typeService,
            @RequestParam String codePv,
            @RequestParam(required = false) String codeUoDest,
            @RequestParam(required = false) String codeUoSrc,
            @RequestParam String montant, @RequestParam String codeAgence, @RequestParam String typeCompte,
            @RequestParam String valeurCompte,
            @RequestParam String teneur, @RequestParam(required = false) String codeacteur) throws IOException, JAXBException {
        MInfos minfo = null;
        if (!code.equals(null) && !telephone.equals(null)) {
            minfo = new MInfos();
            MPersonne titulaire = new MPersonne();
            titulaire.setCodeTelephone(code);
            titulaire.setTelephone(telephone);
            minfo.setTitulaire(titulaire);
        }
        MDataTransaction dataTransaction = new MDataTransaction();
        String fseDef = mobileService.getFournisseurMonnaieDef(code);
        //  boolean isMonnaieElectronique = mobileService.getIfMonnaieDef(fseDef);

        dataTransaction.setMfrais(mobileService.calculFrais(new MParamFrais(codePv, typeService, canal, codeUoSrc, codeUoDest, new BigDecimal(montant), minfo), TypeFrais.FRAIS));
        if (codeacteur != null && !codeacteur.isEmpty()) {
            UniteOrganisation uo = mobileService.findUoByCode(codeacteur);
            if (uo != null) {
                dataTransaction.setNomUo(uo.getLibelleUniteOrganisation());
            }
        }

        return dataTransaction;
    }

    @GetMapping("/donnee/dataParameters")
    @Timed
    public MDataParameter getDataParameters(@RequestParam Optional<String> code) {
        MDataParameter dataParameter = new MDataParameter();
        dataParameter.setmListMotif(mobileService.getMotifs());
        dataParameter.setMLbrancheActivite(transfertAppService.findBrancheActivite());
        dataParameter.setMLNationalite(transfertAppService.findNationalite());
        dataParameter.setmListOrganisation(mobileService.findOraganisationByType(MTypeOrganisation.PAYS));
        dataParameter.setmListTypeDoc(mobileService.getTypeDocument());
        dataParameter.setMgroupeUniteOrganisation(mobileService.findGroupeFse());
        dataParameter.setCodeSecretaExclure(mobileService.getCodeSecretAExlure());
        dataParameter.setFournisseurMonnaieDef(null);
        dataParameter.setPointVenteDef(null);
        dataParameter.setmListReseauFse(mobileService.listeReseauFse());
        //    boolean isMonnaieElectronique = mobileService.getIfMonnaieDef(fseDef);

        dataParameter.setIsAuthentificationForte(mobileService.isAuthentificationForte());
        if (code.isPresent()) {
            dataParameter.setmListypePiece(mobileService.getTypePiece(code.get()));
            List<MOrganisation> listOrganisation = mobileService.getAllUniteOrganisationsCodePays(code.get(), MTypeOrganisation.FSE);
            dataParameter.setmListFse(listOrganisation);
            dataParameter.setListeCategorieFse(mobileService.getServiceCategorie(listOrganisation));
            String fseDef = mobileService.getFournisseurMonnaieDef(code.get());

            String fseDigital = mobileService.getFournisseurDigital(code.get());
            dataParameter.setFseDigital(fseDigital);
            if (fseDef != null) {
                String ifCreateCompte = mobileService.getIfCreateAccount(fseDef);
                dataParameter.setIsCreateCompte(ifCreateCompte);
                UniteOrganisation uo = mobileService.findUoBycodeFSE(fseDef);
                if (uo != null) {
                    dataParameter.setCodeUoDef(uo.getCodeUO());
                }
                dataParameter.setPointVenteDef(mobileService.getCodePointVente(fseDef));
                dataParameter.setLongueurNumTelephone(mobileService.getLongueurNumTelephone(fseDef));
                dataParameter.setAgeLimite(mobileService.getAgeLimite(fseDef));
                dataParameter.setDevise(mobileService.getDevise(fseDef));
            }
            dataParameter.setFournisseurMonnaieDef(fseDef);
        } else {
            dataParameter.setmListFse(null);
        }

        return dataParameter;

    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/type-documents/mob-typedocument")
    @Timed
    public List<MtypeDoc> getTypeDoc() {
        return mobileService.getTypeDocument();
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/type-piece/mob-typepiece")
    @Timed
    public List<MTypePiece> getTypePiece() {
        return mobileService.getTypePiece();
    }

    /**
     * @param code
     * @return
     */
    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/pays/list_fse")
    @Timed
    public List<MOrganisation> getListFseOfPays(@RequestParam String code) {
        return mobileService.getAllUniteOrganisationsFilsByType(code, MTypeOrganisation.FSE);
    }

    @GetMapping("/operations/listeOperationAttente")
    @Timed
    public List<MTransaction> getListOperationAttente(@RequestParam String valeurCompte, @RequestParam String typeCompte, @RequestParam String codeAgence, @RequestParam String codeFseTeneur, @RequestParam String typeServ) {
        log.info("Liste des opérations .........................");
        return mobileService.getOpertationEntente(valeurCompte, typeCompte, codeAgence, codeFseTeneur, typeServ);
    }

    @GetMapping("/pointvente/listePointVente")
    @Timed
    public List<MPointVente> getListPointVente(@RequestParam String code) {
        log.info("Liste pointe vente .........................");
        return mobileService.getListePointVente(code);
    }

    @GetMapping("/pointvente/verifPv")
    @Timed
    public MResponse verifIfAffecte(@RequestParam String codeActeur, @RequestParam String codePv) {
        log.info("Verification .........................");
        return mobileService.verifPointVente(codeActeur, codePv);
    }

    @GetMapping("/pointvente/affectation")
    @Timed
    public MResponse doAffectePointVente(@RequestParam String codeActeur, @RequestParam String login, @RequestParam String codePv) {
        log.info("Verification .........................");
        return mobileService.affecterUtilisateurPv(login, codePv, codeActeur);
    }

    @GetMapping("/omnnicanal/sms")
    @Timed
    public MResponse sendSms(@RequestParam String telephone, @RequestParam String message) {
        log.info("Service d'sms vers omnicanal........................." + telephone + " " + message);
        return mobileService.sendSms(telephone, message);
    }

    @GetMapping("/omnnicanal/mail")
    @Timed
    public ResponseEntity sendMail(@RequestParam String mail, @RequestParam String message) throws IOException {

        apiService.sendMail(mail, message);
        return ResponseEntity.ok().body("OK");
    }

    @GetMapping("/operations/annulation")
    @Timed
    public MResponse annulationOper(@RequestParam String codeOperation) throws IOException, JAXBException {
        log.info("Service annulation opération .........................");
        //    return mobileService.annulationOperation(codeOperation);
        return null;
    }

    @GetMapping("/omnnicanal/get_solde")
    @Timed
    public Balance getSolde(@RequestParam String agence, @RequestParam String numeroCompte, @RequestParam String codeFse, @RequestParam String acteur, @RequestParam(required = false) String codeIndicatif, @RequestParam String identifiant) throws IOException, JAXBException {
        mobileService.verifIfAutoriser(numeroCompte, acteur, identifiant, codeIndicatif);
        return apiService.getSolde(numeroCompte, agence, codeFse);
    }

    @PostMapping("/omnnicanal/get_montant")
    @Timed
    public ElementFacture getMontant(@RequestBody MListParametreProduit mlistparametre) throws IOException, JAXBException {
        Produit prod = mobileService.getFacturierProduit(mlistparametre.getIdProduit());
        String codeFse = mobileService.getFacturierFSE(prod.getFacturier().getId());
        List<MParametre> listeMparametre = mlistparametre.getMparametre();
        String codeFseWallet = mobileService.rechercheValeurParam(listeMparametre, ParamatreFacturier.CODEFSEWALLET.name());
        if (codeFseWallet != null) {
            codeFse = codeFseWallet;

        }

        return apiService.getMontant(mlistparametre.getMparametre(), codeFse);
    }

    @PostMapping("/omnnicanal/get_infos")
    @Timed
    public List<MParametre> getRechercheInfos(@RequestBody MListParametreProduit mlistparametre) throws IOException, JAXBException {

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonStr = ow.writeValueAsString(mlistparametre);

        System.out.println(jsonStr);

        Produit prod = mobileService.getFacturierProduit(mlistparametre.getIdProduit());
        String codeFse = mobileService.getFacturierFSE(prod.getFacturier().getId());
        List<MParametre> listeMparametre = mlistparametre.getMparametre();
        listeMparametre.add(new MParametre("CODEFACTURIER", prod.getFacturier().getCodeFacturier()));
        if (prod.getFacturier().getTypeFacturier().equals(TypeFacturier.FSE)) {
            listeMparametre.add(new MParametre("CODEFSEFACTURIER", prod.getFacturier().getUniteOrganisation().getCodeFse()));
        }
        String codeFseWallet = mobileService.rechercheValeurParam(listeMparametre, ParamatreFacturier.CODEFSEWALLET.name());
        if (codeFseWallet != null) {
            codeFse = codeFseWallet;

        }
        return apiService.getInfos(listeMparametre, codeFse);
    }

    @GetMapping("/omnnicanal/genererOtp")
    @Timed
    public Message genererOtp(@RequestParam String codeFse, @RequestParam String numeroCompte, @RequestParam String agence, @RequestParam String teneur) throws IOException, JAXBException {
        List<MParametre> listeParam = new ArrayList();
        MParametre mpam = new MParametre();
        listeParam.add(new MParametre("NUMEROCOMPTE", numeroCompte, 0));
        listeParam.add(new MParametre("AGENCE", agence, 0));
        listeParam.add(new MParametre("TENEUR", teneur, 0));
        Message msg = apiService.genereOtp(listeParam, codeFse);
        return msg;
    }

    @GetMapping("/omnnicanal/otp/generate-code-otp")
    @Timed
    public com.m2i.pti.service.dto.Message genererOTP(@RequestParam String codePays, @RequestParam String telephone, @RequestParam String codeFacturier, @RequestParam String identifiant) throws IOException, JAXBException {
        com.m2i.pti.service.dto.Message msg = new com.m2i.pti.service.dto.Message();

        //   msg = this.mobileService.generateAndSendCodeOTP(codePays, telephone, identifiant, codeFacturier);
        return msg;
    }

    @GetMapping("/omnnicanal/verif-code-otp")
    @Timed
    public com.m2i.pti.service.dto.Message verifOtp(@RequestParam String codeFse, @RequestParam String otp, @RequestParam String identifiant) throws IOException, JAXBException {
        com.m2i.pti.service.dto.Message msg = new com.m2i.pti.service.dto.Message();
        List<MParametre> listeParam = new ArrayList();
        MParametre mpam = new MParametre();
        listeParam.add(new MParametre("OTP", otp, 0));
        listeParam.add(new MParametre("NUMEROCOMPTE", identifiant, 0));
        msg = this.apiService.verifOTP(listeParam, codeFse);

        return msg;
    }

    @PostMapping("/omnnicanal/get_print")
    @Timed
    public ModelAndView getPrint(@RequestBody MListParametreProduit mlistparametre) throws IOException, JAXBException {
        /**
         * rechercher parmi les paramètres le champs reference
         */
        log.info("Service annulation opération .........................");
        String valeurRef = mobileService.rechercheValeurParam(mlistparametre.getMparametre(), "REFERENCE");
        if (valeurRef != null) {
            List<MParametre> listParametre = mobileService.getParametreFacture(valeurRef);
            if (listParametre.size() > 0) {
                return mobileService.getPrint(appContext, listParametre, valeurRef);
            }
        }
        return null;
    }

    @PostMapping("/omnnicanal/get_data")
    @Timed
    public List<MParametre> getListeData(@RequestBody MListParametreProduit mlistparametre) throws IOException, JAXBException {
        Produit prod = mobileService.getFacturierProduit(mlistparametre.getIdProduit());
        String codeFse = mobileService.getFacturierFSE(prod.getFacturier().getId());
        return apiService.getData(mlistparametre.getMparametre(), codeFse);
    }

    @GetMapping("/omnnicanal/getInfosClient2")
    @Timed
    public CustomerInformation getInfoClient2(@RequestParam String agence, @RequestParam String numeroCompte, @RequestParam String codeFse) throws IOException, JAXBException {
        return apiService.getInfoClientCompte(numeroCompte, agence, codeFse);
    }

    @GetMapping("/omnnicanal/getInfosClient")
    @Timed
    public CustomerInformation getInfoClient(@RequestParam String agence, @RequestParam String numeroCompte, @RequestParam String codeFse) throws IOException, JAXBException {
        return apiService.getInfoClientCompte(numeroCompte, agence, codeFse);
    }

    @GetMapping("/omnnicanal/getInfosClientAlias")
    @Timed
    public MInfos getInfoClientAlias(@RequestParam String alias, @RequestParam String codeFse) throws IOException, JAXBException {
        return mobileService.getInfosCompte(alias, codeFse);
    }

    @GetMapping("/omnnicanal/getreglefacturier")
    @Timed
    public RegleFacturier getRegle(@RequestParam String reference, @RequestParam String montant, @RequestParam String idFacturier) throws IOException, JAXBException {

        String codeFse = mobileService.getFacturierFSE(Long.parseLong(idFacturier));

        RegleFacturier regle = apiService.getRegle(reference, montant, codeFse);
        return regle;
    }

    public static String padLeft(String s, int n) {
        return String.format("%" + n + "s", s).replace(' ', '0');
    }

    private ZonedDateTime getZoneDateTimeFromPeriode(String dateRech) {
        ZoneId zoneId = ZoneId.of("UTC");
        String[] paramDate = dateRech.split("-");
        if (paramDate.length == 3) {
            String jj = paramDate[0];
            String mm = paramDate[1];
            /*mois*/
            String aa = paramDate[2];
            /*annee*/
            String jourP = padLeft(jj, 2);/*jour **/
            String mmP = padLeft(mm, 2);
            /**
             * mois pad *
             */
            return ZonedDateTime.of(Integer.parseInt(aa), Integer.parseInt(mmP), Integer.parseInt(jourP), 00, 00, 00, 1234, zoneId);
        }

        return null;

    }

    @GetMapping("/omnnicanal/consultation_historique")
    @Timed
    public List<Mouvement> getHistoriqueCompte(@RequestParam String agence, @RequestParam String numeroCompte, @RequestParam String codeFse, @RequestParam String debutPeriode, @RequestParam String acteur, @RequestParam(required = false) String codeIndicatif, @RequestParam String identifiant, @RequestParam String finPeriode, HttpServletRequest req, HttpServletResponse res) throws IOException, JAXBException {
        mobileService.verifIfAutoriser(numeroCompte, acteur, identifiant, codeIndicatif);;
        Integer estAdmin;
        String codePv;
        Statement st = null;
        if (acteur.equals("ORGANISATION")) {
            if (req.getHeader("POINTVENTE") != null && req.getHeader("ISUOADMIN") != null) {
                codePv = req.getHeader("POINTVENTE");
                if (!codePv.equals("0")) {
                    ZonedDateTime vDatedebut = getZoneDateTimeFromPeriode(debutPeriode);
                    ZonedDateTime vDatefin = getZoneDateTimeFromPeriode(finPeriode);
                    st = mobileService.getAccountHistTransPoint(numeroCompte, codeFse, vDatedebut, vDatefin, codePv);
                } else if (Integer.parseInt(req.getHeader("ISUOADMIN")) == 1) {
                    st = apiService.getHistorique(numeroCompte, agence, codeFse, debutPeriode, finPeriode);
                }

            } else {
                st = apiService.getHistorique(numeroCompte, agence, codeFse, debutPeriode, finPeriode);
            }
        } else {
            st = apiService.getHistorique(numeroCompte, agence, codeFse, debutPeriode, finPeriode);
        }

        if (st != null) {
            return st.getListeMouvement();
        }
        return Collections.emptyList();
    }

    @PostMapping("/xxx/card")
    public void getCard() throws IOException, NoSuchAlgorithmException, JAXBException {
        OperationCardlessDTO message = new OperationCardlessDTO();
        EvenementOperation evenement = null;
        List<EvenementOperation> listeEvt = evenementOperationRepository.findOperationNonLevee();
        for (EvenementOperation evt : listeEvt) {
            Operation oper = evt.getOperation();
            if (oper != null && oper.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.RETRAIT_CARDLESS))) {
                evenement = evenementOperationRepository.findByOperation(oper);
                if (evenement != null) {
                    OperationCardlessDTO operationCardlessDTO = new OperationCardlessDTO();
                    operationCardlessDTO.setEntityCode("CM");
                    operationCardlessDTO.setEvenementId(evenement.getId());
                    operationCardlessDTO.setOperationId(oper.getId());
                    mobileService.cardlessTransactionSetllement(operationCardlessDTO);
                    //    apiService.executionCardless(operationCardlessDTO);
                }
            }
        }

    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/omnnicanal/facture-founisseur/list")
    @Timed
    public List<FactureFournisseur> getFacture(@RequestBody MListParametreProduit mlistparametre) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.debug("REST request to save getFacture : {}", mlistparametre);
        Produit prod = mobileService.getFacturierProduit(mlistparametre.getIdProduit());
        String codeFse = mobileService.getFacturierFSE(prod.getFacturier().getId());
        if (mlistparametre.getMparametre() != null) {
            CollectionFacture col = apiService.getFacture(mlistparametre.getMparametre(), codeFse);
            if (col != null) {
                return col.getListeFacture();
            }
        }

        return Collections.emptyList();
    }

    @GetMapping("/facturiers/liste-facturiers")
    @Timed
    public GrData getFacturierAndAttribute(@RequestParam String codeUo, @RequestParam String secteur) throws IOException, JAXBException {
        log.info("---------codeUo :{}", codeUo);
        log.info("---------secteur :{}", secteur);
        //WALLET, AUTRE, AIRTIME 
        GrData grData = mobileService.getFacturierAndAttribute(codeUo, secteur);

        return grData;

    }

    @GetMapping("/facturiers/facturier")
    @Timed
    public GrData getFacturierAndAttributeUo(@RequestParam String codeUo, @RequestParam String secteur) throws IOException, JAXBException {
        log.info("---------codeUo :{}", codeUo);
        log.info("---------secteur :{}", secteur);
        //WALLET, AUTRE, AIRTIME 
        GrData grData = mobileService.getFacturierAndAttributeUoId(codeUo, secteur);

        return grData;

    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/pointVente/majAlias")
    @Timed
    public void setAliasPointVente(@RequestParam String codePv, @RequestParam String alias) {
        mobileService.updateAliasPoint(codePv, alias);
    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/entreprise/reseaux")
    @Timed
    public List<MOrganisation> getListUoFilsByUoId(@RequestParam String telephone, @RequestParam String codePays) {
        return mobileService.getAllUniteOrganisationsFilsByUoId(telephone, codePays);
    }

    @GetMapping("/omnicanalCheck")
    @Timed
    public Message check(@RequestParam String telephone, @RequestParam String codePays) throws IOException, IOException, JAXBException {
        List<MParametre> listMp = new ArrayList();
        MParametre mp = new MParametre("ENTITYCODE", "BF");
        listMp.add(mp);
        Message msg = apiService.checkPing("0051", listMp);

        if (msg != null) {
            if (msg.getCodeErr() == null || (msg.getCodeErr() != null && msg.getCodeErr().equals("-1"))) {
                throw new CustomParameterizedException(msg.getMsg());
            }
        } else {
            throw new CustomParameterizedException("le système distant est inaccessible!!");
        }
        mp = new MParametre("REFERENCE", telephone);
        listMp.add(mp);
        Message msgStatut = apiService.checkStatus("0051", listMp);
        if (msgStatut != null && msgStatut.getCodeErr().equals("0")) {

        }
        return msgStatut;
    }

    @GetMapping("/omnnicanal/mini-releve")
    @Timed
    public List<Mouvement> miniRele(@RequestParam String agence, @RequestParam String numeroCompte, @RequestParam String codeFse, @RequestParam String acteur, @RequestParam(required = false) String codeIndicatif, @RequestParam String identifiant, HttpServletRequest req, HttpServletResponse res) throws IOException, JAXBException {
        mobileService.verifIfAutoriser(numeroCompte, acteur, identifiant, codeIndicatif);
        Integer estAdmin;
        String codePv;

        Statement st = null;
        if (acteur.equals("ORGANISATION")) {
            if (req.getHeader("POINTVENTE") != null && req.getHeader("ISUOADMIN") != null) {
                codePv = req.getHeader("POINTVENTE");
                if (!codePv.equals("0")) {
                    st = mobileService.getMiniRelevePoint(numeroCompte, codeFse, codePv);
                } else if (Integer.parseInt(req.getHeader("ISUOADMIN")) == 1) {
                    st = apiService.getMiniReleve(numeroCompte, agence, codeFse);
                }

            } else {
                st = apiService.getMiniReleve(numeroCompte, agence, codeFse);
            }
        } else {
            st = apiService.getMiniReleve(numeroCompte, agence, codeFse);
        }

        if (st != null) {
            return st.getListeMouvement();
        }
        return Collections.emptyList();
    }

    /* @PostMapping("/operations/doOperation")
    @Timed
    public MResponse executeOperation(String strMOperation) throws IOException, NoSuchAlgorithmException, JAXBException {

        MOperation mOperation = mobileService.getdoOperationExternalParameter(strMOperation);
        MResponseOperation rep = mobileService.doOperation(mOperation);
        return null;
    }*/
    //=============================================================================================================================================================================================
    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @PostMapping("/operations/external/do_op_retrait_compte")
    @Timed
    public MResponse doExecuteOperationRetraitCompte(@RequestParam String strMOperation) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.debug("REST request to save Operation : {}", strMOperation);
        MOperation mOperation = mobileService.getdoOperationExternalParameter(strMOperation);
        MResponseOperation rep = mobileService.doOperationRetraitCompte(mOperation);
        String transactionId = "0";
        List<MParametre> extraData = null;
        if (rep != null) {
            if (rep.getOperation() != null) {
                mobileService.repartirCommission(rep.getOperation());
                extraData = mobileService.populateMessage(rep.getOperation());
                mobileService.envoiDesMessage(rep.getOperation(), extraData);
                transactionId = rep.getOperation().getCodeOperation();
                extraData = mobileService.populateMessage(rep.getOperation());
            }
            return new MResponse(rep.getCode(), rep.getMessage(), transactionId, extraData);
        }
        return new MResponse("-1", "Erreur inconnue");

    }

    @PostMapping("/operations/external/doOperation")
    @Timed
    public MResponse executeOperation(@RequestParam String strMOperation, @RequestParam String typeService) throws IOException, NoSuchAlgorithmException, JAXBException {
        MResponseOperation rep = null;
        List<MParametre> extraData = null;
        switch (typeService) {
            case "PAIEMENT_AIRTIME": {
                MOperationFacture mOperation = mobileService.getdoOperationFactExternalParameter(strMOperation);
                rep = mobileService.doOperationPaiementCredit(mOperation);
            }
            break;
            case "RETRAIT_COMPTE": {
                MOperation mOperation = mobileService.getdoOperationExternalParameter(strMOperation);
                rep = mobileService.doOperationRetraitCompte(mOperation);
            }
            break;
            case "PAIEMENT_FACTURE": {
                MOperationFacture mOperation = mobileService.getdoOperationFactExternalParameter(strMOperation);
                MResponse reponse = mobileService.doOperationPaiementFacture(mOperation);
                String transactionId = "0";
                if (reponse != null && rep.getTansactionId() != null) {
                    Operation oper = mobileService.getOperation(Long.parseLong(rep.getTansactionId()));
                    if (reponse.getExtraData() != null) {
                        extraData = reponse.getExtraData();
                    } else {
                        extraData = mobileService.populateMessage(oper);
                    }

                    mobileService.envoiDesMessage(oper, reponse.getExtraData());
                    mobileService.repartirCommission(oper);
                    transactionId = oper.getCodeOperation();
                    return new MResponse(rep.getCode(), rep.getMessage(), transactionId, extraData);

                } else if (rep != null) {
                    return new MResponse(rep.getCode(), rep.getMessage());
                }
            }
            break;
            default: {
                MOperation mOperation = mobileService.getdoOperationExternalParameter(strMOperation);
                rep = mobileService.doOperation(mOperation);
            }

        }
        if (rep != null) {
            Compte cpt = null;
            String transactionId = "0";
            if (rep.getOperation() != null) {
                extraData = mobileService.populateMessage(rep.getOperation());
                //   if (!rep.getOperation().getTypeService().getCode().equals(TypeServiceLibelle.ACCOUNT_CASH.toString())) {
                mobileService.envoiDesMessage(rep.getOperation(), extraData);
                // }               
                mobileService.repartirCommission(rep.getOperation());
                transactionId = rep.getOperation().getCodeOperation();

            }
            return new MResponse(rep.getCode(), rep.getMessage(), transactionId, extraData);
        }
        return new MResponse("-1", "Erreur inconnue");

    }

    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.API_AGENT})
    @GetMapping("/operations/action_numero_cardless")
    @Timed
    public MResponse doAjoutNUmeroCardless(@RequestParam String codePv, @RequestParam String codeActeur, @RequestParam String login, @RequestParam String action) {
        log.info("Verification .........................");
        return mobileService.doActionNumeroCardless(login, codePv, codeActeur, action);
    }

}
