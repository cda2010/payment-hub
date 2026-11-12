package com.m2i.pti.external.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.*;
import com.m2i.pti.domain.enumeration.SecteurActivite;
import com.m2i.pti.external.domain.ExtBwCustomer;
import com.m2i.pti.external.domain.ExtCallData;
import com.m2i.pti.external.domain.ExternalResponse;
import com.m2i.pti.external.domain.GrData;
import com.m2i.pti.external.domain.MFraisResponse;
import com.m2i.pti.external.service.ExtService;
import com.m2i.pti.external.service.ExternalService;
import com.m2i.pti.mobile.dto.*;
import com.m2i.pti.mobile.enumeration.MTypeCompte;
import com.m2i.pti.mobile.service.MobileService;
import com.m2i.pti.repository.*;
import com.m2i.pti.service.ClientService;
import com.m2i.pti.service.CompteService;
import com.m2i.pti.service.PointVenteService;
import com.m2i.pti.service.UniteOrganisationService;
import com.m2i.pti.service.PersonneService;
import com.m2i.pti.service.dto.Message;
import com.m2i.pti.service.util.Util;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import com.m2i.pti.web.rest.errors.CustomSettlementException;
import com.m2i.pti.service.dto.FactureFournisseur;
import com.m2i.pti.service.ApiService;
import com.m2i.pti.service.dto.CollectionFacture;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.m2i.pti.mobile.dto.MResponse;
import com.m2i.pti.service.dto.ClientDTO;
import com.m2i.pti.service.dto.Customers;
import com.m2i.pti.service.dto.CustomersInfo;
import com.m2i.pti.web.rest.util.PaginationUtil;

import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/external")
public class ExternalRessource {

    private final ExternalService externalService;
    private final PersonneRepository personneRepository;
    private final ClientRepository clientRepository;
    private final CompteRepository compteRepository;
    private final ParametreAppRepository parametreAppRepository;
    private final Logger log = LoggerFactory.getLogger(ClientService.class);
    private final PointVenteService pointVenteService;
    private final UniteOrganisationRepository uniteOrganisationRepository;
    private final UniteOrganisationService uniteOrganisationService;
    private final MobileService mobileService;
    private final FacturierRepository facturierRepository;
    private final OauthClientDetailsRepository oauthClientDetailsRepository;
    private final ProduitRepository produitRepository;
    private final ApiService apiService;
    private final CompteService compteService;
    private final ExtService extService;
    private final PersonneService personneService;

    public ExternalRessource(PersonneService personneService, ExtService extService, CompteService compteService, ApiService apiService, OauthClientDetailsRepository oauthClientDetailsRepository, ProduitRepository produitRepository, FacturierRepository facturierRepository, UniteOrganisationService uniteOrganisationService, ExternalService externalService, PersonneRepository personneRepository, ClientRepository clientRepository, CompteRepository compteRepository, ParametreAppRepository parametreAppRepository, PointVenteService pointVenteService, UniteOrganisationRepository uniteOrganisationRepository, MobileService mobileService) {
        this.externalService = externalService;
        this.personneRepository = personneRepository;
        this.clientRepository = clientRepository;
        this.compteRepository = compteRepository;
        this.parametreAppRepository = parametreAppRepository;
        this.pointVenteService = pointVenteService;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.mobileService = mobileService;
        this.uniteOrganisationService = uniteOrganisationService;
        this.oauthClientDetailsRepository = oauthClientDetailsRepository;
        this.facturierRepository = facturierRepository;
        this.produitRepository = produitRepository;
        this.apiService = apiService;
        this.compteService = compteService;
        this.extService = extService;
        this.personneService = personneService;
    }
//    @GetMapping("/verification")
//    public Boolean verifyOTP(@RequestParam String codePays, @RequestParam String telephone,@RequestParam String otp) {
//       return externalService.verifyOTP(codePays,telephone,otp);
//    }

    @GetMapping("/solde")
    public Message getSolde(@RequestParam String codePays,
            @RequestParam String telephone) throws IOException, JAXBException {
        log.info("Debut d'execution de la methode getSolde de external ressource");
        return externalService.getSolde(codePays, telephone);
    }

    @PostMapping("/send-code-otp")
    public ResponseEntity<Message> sendCodeOtp(@RequestParam String codePays,
            @RequestParam String telephone
    ) {
        Message message = externalService.generateAndSendCodeOTP(codePays, telephone);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/send-code")
    public ResponseEntity<Message> sendCodeOtp(@RequestParam String codePays,
            @RequestParam String telephone, @RequestParam String identifiant
    ) {
        Message message = externalService.generateAndSendCodeOTP(codePays, telephone, identifiant);
        return ResponseEntity.ok(message);
    }

    @GetMapping("/historique")
    public Message getHistorique(@RequestParam String codePays,
            @RequestParam String telephone,
            @RequestParam String debutPeriode,
            @RequestParam String finPeriode
    ) throws IOException, JAXBException, NoSuchAlgorithmException {
        return externalService.getHistorique(codePays, telephone, debutPeriode, finPeriode);
    }

    @GetMapping("/verification")
    public CredentialApi verifieHashParam(@RequestParam String params, @RequestHeader String hashParam, @RequestHeader String client_id) {
        return externalService.verifieHashParam(params, hashParam, client_id);
    }

    @GetMapping("/infos-client")
    public Message getInfosClient(@RequestParam String codePays,
            @RequestParam String telephone
    ) throws IOException, JAXBException {
        return externalService.getInfosClient(codePays, telephone);
    }

    @PostMapping("/operations/status")
    @Timed
    public ExternalResponse getStatus(@RequestParam String codeOperation) throws IOException, JAXBException {
        log.info("Service getstatus opération .........................");
        ExternalResponse reponse = mobileService.getStatus(codeOperation, "INTERNE");
        return reponse;
    }

    @PostMapping("/operations/status/externe")
    @Timed
    public ExternalResponse getStatusExterne(@RequestParam String codeOperation) throws IOException, JAXBException {
        log.info("Service getstatus opération .........................");
        ExternalResponse reponse = mobileService.getStatus(codeOperation, "EXTERNE");
        return reponse;
    }

    @GetMapping("/operations/calcul-frais")
    @Timed
    public MFrais calculFrais(@RequestParam String typeService,
            @RequestParam String codePv,
            @RequestParam(required = false) String codeUoDest,
            @RequestParam(required = false) String codeUoSrc,
            @RequestParam BigDecimal montant) {
        log.info("Debut d'execution de la methode calculFrais de external ressource");
        String canal = "MOBILE";
        return externalService.calculFrais(new MParamFrais(codePv, typeService, canal, codeUoDest, codeUoSrc, montant, null));
    }

    @PostMapping("/operations/paiement-bien")
    @Timed
    public ExternalResponse paiementBien(@RequestParam String codePays,
            @RequestParam String telephone,
            @RequestParam String codePv,
            @RequestParam BigDecimal montant,
            @RequestParam String codeOTP
    //            ,@RequestParam(required = false) String codeExterne
    ) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.info("Debut d'execution de la methode paiementBien de external ressource");
        String canal = "MOBILE";
        try {
            if (externalService.verifyOTP(codePays, telephone, codeOTP)) {
                Optional<PointVente> pointVenteExist = pointVenteService.findByCode(codePv);
                String codeFseSrc = parametreAppRepository.findByTypeAndNomAndCodeFse("FOURNISSEUR_MONNAIE", codePays).getValeur();
                UniteOrganisation uoSrc = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(codeFseSrc);
                if (pointVenteExist.isPresent() && uoSrc != null) {
                    if (pointVenteExist.get().getUniteOrganisation() != null) {
                        MOperation mOperation = new MOperation();
                        mOperation.setTypeService("PAIEMENT_BIEN");
                        mOperation.setCanal(canal);
                        mOperation.setCodePv(codePv);
                        mOperation.setMontant(montant);
                        mOperation.setActeur("ORGANISATION");
                        mOperation.setMotif("M2");
                        mOperation.setUserConnecte("external");
                        mOperation.setEmetteur(getMPersonne(codePays, telephone));
                        mOperation.setCodeUoEm(codeOTP);
                        /* Calcul des frais*/
                        String codeUoDest = pointVenteExist.get().getUniteOrganisation().getCodeUO();
                        mOperation.setCodeUoEm(uoSrc.getCodeUO());
                        mOperation.setCodeUoAcq(codeUoDest);
                        MFrais mFrais = externalService.calculFrais(new MParamFrais(codePv, mOperation.getTypeService(), canal, uoSrc.getCodeUO(), codeUoDest, montant, null));
                        mOperation.setMontantFrais(Util.getZeroIfNull(mFrais.getFraisOperation()));
                        mOperation.setMontantTaxe(Util.getZeroIfNull(mFrais.getTaxeOPeration()));
                        return externalService.doOperationRetraitCompte(mOperation);
                    } else {
                        return new ExternalResponse("-1", "Impossible de trouver l'unité d'organisation correspondant au point de vente.", null, null);
                        //throw new CustomParameterizedException("Impossible de trouver l'unité d'organisation correspondant au point de vente.");
                    }
                } else {
                    return new ExternalResponse("-1", "Impossible de trouver l'unité d'organisation correspondant au point de vente.", null, null);
                }
            } else {
                return new ExternalResponse("-1", "OTP Incorrect.", null, null);
            }
        } catch (CustomSettlementException e) {

            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ExternalResponse("-1", e.getMessage(), null, null);
        } catch (CustomParameterizedException e) {
            return new ExternalResponse("-1", e.getMessage(), null, null);
        }
    }

    @PostMapping("/operations/retrait-compte")
    @Timed
    public ExternalResponse retraitCompte(@RequestParam String codePays,
            @RequestParam String telephone,
            @RequestParam String codePv,
            @RequestParam BigDecimal montant,
            @RequestParam String codeOTP
    ) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.info("Debut d'execution de la methode paiementBien de external ressource");
        String canal = "MOBILE";
        try {
            if (externalService.verifyOTP(codePays, telephone, codeOTP)) {
                Optional<PointVente> pointVenteExist = pointVenteService.findByCode(codePv);
                String codeFseSrc = parametreAppRepository.findByTypeAndNomAndCodeFse("FOURNISSEUR_MONNAIE", codePays).getValeur();
                UniteOrganisation uoSrc = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(codeFseSrc);
                if (pointVenteExist.isPresent() && uoSrc != null) {
                    if (pointVenteExist.get().getUniteOrganisation() != null) {
                        MOperation mOperation = new MOperation();
                        mOperation.setTypeService("RETRAIT_COMPTE");
                        mOperation.setCanal(canal);
                        mOperation.setCodePv(codePv);
                        mOperation.setMontant(montant);
                        mOperation.setActeur("ORGANISATION");
                        mOperation.setMotif("M2");
                        mOperation.setUserConnecte("external");
                        mOperation.setEmetteur(getMPersonne(codePays, telephone));
                        mOperation.setCodeUoEm(codeOTP);
                        /* Calcul des frais*/
                        String codeUoDest = pointVenteExist.get().getUniteOrganisation().getCodeUO();
                        mOperation.setCodeUoEm(uoSrc.getCodeUO());
                        mOperation.setCodeUoAcq(codeUoDest);
                        MFrais mFrais = externalService.calculFrais(new MParamFrais(codePv, mOperation.getTypeService(), canal, uoSrc.getCodeUO(), codeUoDest, montant, null));
                        mOperation.setMontantFrais(Util.getZeroIfNull(mFrais.getFraisOperation()));
                        mOperation.setMontantTaxe(Util.getZeroIfNull(mFrais.getTaxeOPeration()));
                        return externalService.doOperationRetraitCompte(mOperation);
                    } else {
                        return new ExternalResponse("-1", "Impossible de trouver l'unité d'organisation correspondant au point de vente.", null, null);
                        //throw new CustomParameterizedException("Impossible de trouver l'unité d'organisation correspondant au point de vente.");
                    }
                } else {
                    return new ExternalResponse("-1", "Impossible de trouver l'unité d'organisation correspondant au point de vente.", null, null);
                }
            } else {
                return new ExternalResponse("-1", "OTP Incorrect.", null, null);
            }
        } catch (CustomSettlementException e) {

            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ExternalResponse("-1", e.getMessage(), null, null);
        } catch (CustomParameterizedException e) {
            return new ExternalResponse("-1", e.getMessage(), null, null);
        }
    }

    @PostMapping("/operations/deposit")
    @Timed
    public ExternalResponse depositToAccount(
            @RequestParam String codePaysRecepteur,
            @RequestParam String telephoneRecepteur,
            @RequestParam String codePv,
            @RequestParam BigDecimal montant,
            @RequestParam String clientId
    ) throws IOException, NoSuchAlgorithmException, JAXBException {
        try {
            log.info("Debut d'execution de la methode deposit de external ressource");
            String canal = "MOBILE";
            MOperation mOperation = new MOperation();
            mOperation.setTypeService("CASH_ACCOUNT");
            mOperation.setCanal(canal);
            mOperation.setCodePv(codePv);
            mOperation.setActeur("ORGANISATION");
            mOperation.setMontant(montant);
            mOperation.setMotif("M13");
            Optional<PointVente> pointVenteExist = pointVenteService.findByCode(codePv);
            //   mOperation.setEmetteur(getMPersonne(codePaysEmetteur, telephoneEmetteur));
            mOperation.setBeneficiaire(getMPersonne(codePaysRecepteur, telephoneRecepteur));
            //Calcul frais
            //String codeFseSrc = parametreAppRepository.findByTypeAndNomAndCodeFse("FOURNISSEUR_MONNAIE", codePaysEmetteur).getValeur();
            String codeFseDest = parametreAppRepository.findByTypeAndNomAndCodeFse("FOURNISSEUR_MONNAIE", codePaysRecepteur).getValeur();

            OauthClientDetails oauthClientDetails = oauthClientDetailsRepository.findClient_id(clientId);

            if (pointVenteExist.isPresent() && pointVenteExist.get().getUniteOrganisation() != null) {
                UniteOrganisation uoSrc = uniteOrganisationService.findOneById(pointVenteExist.get().getUniteOrganisation().getId());
                if (oauthClientDetails != null && oauthClientDetails.getUniteOrganisation() != null) {
                    UniteOrganisation uo = oauthClientDetails.getUniteOrganisation();
                    if (!uo.getCodeUO().equals(uoSrc.getCodeUO())) {
                        return new ExternalResponse("-1", "Transaction impossible pour cet agent!!!", null, null);
                    }
                } else {
                    return new ExternalResponse("-1", "Transaction impossible pour cet agent!!!", null, null);
                }
                mOperation.setIdentifiant(uoSrc.getCodeUO());
                mOperation.setCodeUo(uoSrc.getCodeUO());
                UniteOrganisation uoDest = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(codeFseDest);
                mOperation.setCodeUoEm(uoDest.getCodeUO());
                if (uoDest != null) {
                    MFrais mFrais = externalService.calculFrais(new MParamFrais(codePv, mOperation.getTypeService(), canal, uoSrc.getCodeUO(), uoDest.getCodeUO(), montant, null));
                    mOperation.setMontantTaxe(mFrais.getTaxeOPeration());
                    mOperation.setMontantFrais(mFrais.getFraisOperation());
                    log.info("Api account to accout  MOperation {}", mOperation);
                    return externalService.doOperationDepotCompte(mOperation);
                } else {
                    return new ExternalResponse("-1", "Impossible de trouver les unités d'organisation", null, null);
                }
            } else {
                return new ExternalResponse("-1", "Impossible de trouver les unités d'organisation", null, null);
            }
        } catch (CustomSettlementException e) {
            return new ExternalResponse("-1", e.getMessage(), null, null);
        } catch (CustomParameterizedException e) {
            return new ExternalResponse("-1", e.getMessage(), null, null);
        }

    }

    @PostMapping("/operations/account-account")
    @Timed
    public ExternalResponse accountToAccount(@RequestParam String codePaysEmetteur,
            @RequestParam String telephoneEmetteur,
            @RequestParam String codePaysRecepteur,
            @RequestParam String telephoneRecepteur,
            @RequestParam String codePv,
            @RequestParam BigDecimal montant,
            @RequestParam String codeOTP) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.info("Debut d'execution de la methode acountToAccount de external ressource");
        String canal = "MOBILE";
        if (externalService.verifyOTP(codePaysEmetteur, telephoneEmetteur, codeOTP)) {
            MOperation mOperation = new MOperation();
            mOperation.setTypeService("ACCOUNT_ACCOUNT");
            mOperation.setCanal(canal);
            mOperation.setCodePv(codePv);
            mOperation.setActeur("ORGANISATION");
            mOperation.setMontant(montant);
            mOperation.setMotif("M13");
            mOperation.setEmetteur(getMPersonne(codePaysEmetteur, telephoneEmetteur));
            mOperation.setBeneficiaire(getMPersonne(codePaysRecepteur, telephoneRecepteur));
            //Calcul frais
            String codeFseSrc = parametreAppRepository.findByTypeAndNomAndCodeFse("FOURNISSEUR_MONNAIE", codePaysEmetteur).getValeur();
            String codeFseDest = parametreAppRepository.findByTypeAndNomAndCodeFse("FOURNISSEUR_MONNAIE", codePaysRecepteur).getValeur();

            UniteOrganisation uoSrc = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(codeFseSrc);
            UniteOrganisation uoDest = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(codeFseDest);
            mOperation.setCodeUoEm(uoDest.getCodeUO());
            if (uoSrc != null && uoDest != null) {
                MFrais mFrais = externalService.calculFrais(new MParamFrais(codePv, mOperation.getTypeService(), canal, uoSrc.getCodeUO(), uoDest.getCodeUO(), montant, null));
                mOperation.setMontantTaxe(mFrais.getTaxeOPeration());
                mOperation.setMontantFrais(mFrais.getFraisOperation());
                log.info("Api account to accout  MOperation {}", mOperation);
                return externalService.doOperationCompteACompte(mOperation);
            } else {
                throw new CustomParameterizedException("Impossible de trouver les unités d'organisation !");
            }
        } else {
            throw new CustomParameterizedException("Code OTP invalide !");
        }
    }

    @PostMapping("/operations/paiement-internet")
    @Timed
    public ExternalResponse paiementInternet(@RequestParam String codePays,
            @RequestParam String telephone,
            @RequestParam String codePv,
            @RequestParam String codeRetrait,
            @RequestParam String montant,
            @RequestParam String isMontant) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.info("Debut d'execution de la methode retraitCash de external ressource");
        String canal = "MOBILE";
        boolean exe = false; // a executer
        try {
            Optional<PointVente> pointVenteExist = pointVenteService.findByCode(codePv);
            String codeFseSrc = parametreAppRepository.findByTypeAndNomAndCodeFse("FOURNISSEUR_MONNAIE", codePays).getValeur();
            if (pointVenteExist.isPresent()) {
                if (pointVenteExist.get().getUniteOrganisation() != null) {
                    MOperation forVerication = new MOperation();
                    forVerication.setCodePv(codePv);
                    forVerication.setCanal(canal);
                    forVerication.setCodeRetrait(codeRetrait);
                    MRetraitResponse detailRetrait = mobileService.getDetailRetrait(forVerication);
                    if (detailRetrait.getReponse().getCode().equals("0")) {
                        if (isMontant.equals("1")) {
                            if (detailRetrait.getMontant().equals(new BigDecimal(montant))) {
                                exe = true;
                            } else {
                                exe = false;
                            }
                        } else if (isMontant.equals("0")) {
                            exe = true;
                        }
                        if (exe == true) {
                            MOperation mOperation = new MOperation();
                            mOperation.setTypeService("RETRAIT");
                            mOperation.setCodeRetrait(codeRetrait);
                            mOperation.setCanal(canal);
                            mOperation.setCodeRetrait(codeRetrait);
                            mOperation.setCodePv(codePv);
                            mOperation.setMontant(detailRetrait.getMontant());
                            mOperation.setMotif("M13");
                            //mOperation.setEmetteur();
                            mOperation.setBeneficiaire(getMPersonne(codePays, telephone));
                            /* Calcul des frais*/
                            String codeUoDest = pointVenteExist.get().getUniteOrganisation().getCodeUO();
                            //MFrais mFrais=externalService.calculFrais(new MParamFrais(codePv,mOperation.getTypeService(),canal,codeFseSrc,codeUoDest,detailRetrait.getMontant(),null));
                            mOperation.setMontantFrais(BigDecimal.ZERO);
                            mOperation.setMontantTaxe(BigDecimal.ZERO);
                            MResponseOperation mResponse = mobileService.doOperationRetraitCash(mOperation);
                            ExternalResponse externalResponse = new ExternalResponse(mResponse.getCode(), mResponse.getMessage(), mResponse.getOperation().getCodeOperation(), detailRetrait.getMontant().toString());
                            return externalResponse;
                        } else {
                            return new ExternalResponse("-1", "le montant du paiement ne correspond pas à celui fixé" + montant, null, null);

                        }

                    } else {

                        return new ExternalResponse("-1", detailRetrait.getReponse().getMessage(), null, null);

                    }

                } else {
                    return new ExternalResponse("-1", "Impossible de trouver l'unité d'organisation correspondant au point de vente.", null, null);

                }
            } else {
                return new ExternalResponse("-1", "Point de vente introuvable", null, null);

            }

        } catch (CustomSettlementException e) {

            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ExternalResponse("-1", e.getMessage(), null, null);
        }

    }

    private MInfos getMPersonne(String codePays, String telephone) {
        Personne personne = personneRepository.findTopByTelephoneAndCodePaysTelephoneAndActivatedIsTrue(telephone, codePays);
        if (personne != null) {
            Client client = clientRepository.findTop1ByPersonneIdAndSupprimeIsFalse(personne.getId());
            if (client != null) {
                Compte compte = mobileService.findComptePersonne(telephone, codePays);
                // Compte compte = compteRepository.findByClientAndValeurCompte(client, telephone);
                if (compte != null) {
                    MInfos mInfos = new MInfos();
                    //Remplissage de MInfo
                    MCompte mCompte = new MCompte();
                    MPersonne mPersonne = new MPersonne();
                    //Remplissage de MCompte
                    mCompte.setNumeroCompte(compte.getValeurCompte());
                    mCompte.setTypeCompte(MTypeCompte.AUTRE);
                    ParametreApp parametreApp = parametreAppRepository.findByTypeAndNomAndCodeFse("FOURNISSEUR_MONNAIE", codePays);
                    if (parametreApp != null) {
                        mCompte.setTeneurCompte(parametreApp.getValeur());
                        //Remplissage de l'objet MPersonne
                        mPersonne.setCodeTelephone(codePays);
                        mPersonne.setTelephone(personne.getTelephone());
                        mPersonne.setEmail(personne.getEmail());
                        mInfos.setCompte(mCompte);
                        mInfos.setTitulaire(mPersonne);
                        return mInfos;
                    } else {
                        throw new CustomParameterizedException("Une erreur de paramtrage s'est produite!");
                    }
                } else {
                    throw new CustomParameterizedException("Compte introuvable !");
                }
            } else {
                throw new CustomParameterizedException("Client introuvable !");
            }
        } else {
            throw new CustomParameterizedException("Client introuvable !");
        }
    }

    private MInfos getMPersonne(ExtBwCustomer extCustomer, String codeOperateur) {
        String codeBanque = "0";
        String codeAgence = "0";
        String numeroTelephone = "";
        String teneur;
        String numeroCompte = "";
        MInfos minfos = null;
        numeroTelephone = extCustomer.getMsisdn();
        teneur = extCustomer.getCodeFse();
        numeroCompte = extCustomer.getNumeroCompte();
        if (extCustomer.getTypeCompte().equals("BANQUE")) {
            codeBanque = extCustomer.getCodeBanque();
            codeAgence = extCustomer.getCodeAgence();
        } else if (extCustomer.getTypeCompte().equals("CARTE")) {
            codeAgence = extCustomer.getLast4Digit();
        }
        String malias = compteService.getCRC32Checksum(codeAgence, numeroCompte, numeroTelephone, codeOperateur);
        minfos = compteService.findByalias(malias);
        //   Compte compte = mobileService.findComptePersonne(telephone, codePays);
        // Compte compte = compteRepository.findByClientAndValeurCompte(client, telephone);
        if (minfos != null) {
            return minfos;
        } else {
            throw new CustomParameterizedException("Compte introuvable !");
        }

    }

    @GetMapping("/facturiers/data/of-uo")
    @Timed
    public GrData getFacturierAndAttribute(@RequestParam String codeUo, @RequestParam String secteur) throws IOException {
        log.info("---------codeUo :{}", codeUo);
        log.info("---------secteur :{}", secteur);
        //WALLET, AUTRE, AIRTIME 

        GrData grData = mobileService.getFacturierAndAttribute(codeUo, secteur);
        return grData;
    }

    @PostMapping("/facturier/list-facture")
    public List<FactureFournisseur> getListeFacture(@RequestBody MListParametreProduit mlistparametre) {
        log.debug("REST request to save getFacture : {}", mlistparametre);
        try {
            Produit prod = mobileService.getFacturierProduit(mlistparametre.getIdProduit());
            String codeFse = mobileService.getFacturierFSE(prod.getFacturier().getId());
            if (mlistparametre.getMparametre() != null) {
                CollectionFacture col = apiService.getFacture(mlistparametre.getMparametre(), codeFse);
                if (col != null) {
                    return col.getListeFacture();
                }
            }
            return Collections.emptyList();
        } catch (Exception e) {

        }
        return Collections.emptyList();
    }

    @PostMapping("/facturier/get-info")
    public List<MParametre> getInfosFacture(@RequestBody MListParametreProduit mlistparametre) {
        log.debug("REST request to save getFacture : {}", mlistparametre);
        List<MParametre> listInfos = new ArrayList();
        try {
            Produit prod = mobileService.getFacturierProduit(mlistparametre.getIdProduit());
            String codeFse = mobileService.getFacturierFSE(prod.getFacturier().getId());
            if (mlistparametre.getMparametre() != null) {
                listInfos = apiService.getInfos(mlistparametre.getMparametre(), codeFse);

            }

        } catch (Exception e) {

        }
        return listInfos;
    }

    @GetMapping("/facturier/get-liste-paiement-facture")
    public List<MfactureMarchand> getListePaiementFacture(@RequestParam String codePays,
            @RequestParam String telephone, @RequestParam String codeFaturier, @RequestParam Long taille, @RequestParam Long page) throws JAXBException {
        List<MfactureMarchand> listeMarchand = new ArrayList();
        try {

            Optional<Personne> searchPersonne = personneService.findPersonneByPhoneAndCodePays(telephone, codePays);//Inclure le code
            if (searchPersonne.isPresent()) {
                Facturier facturier = this.facturierRepository.findByCodeFacturier(codeFaturier);
                //Code fact
                if (facturier != null) {
                    listeMarchand = mobileService.getFactureByFacturierAndPersonne(searchPersonne.get().getId(), facturier.getId(), taille, page);
                }
            }
        } catch (Exception e) {

        }

        return listeMarchand;
    }

    @PostMapping("/operations/getFees")
    @Timed
    public MFraisResponse calculFraisExternal(@RequestBody ExtCallData extCallData) {
        log.info("Debut d'execution de la methode calculFrais de external ressource");
        MFraisResponse mFraisResponse = new MFraisResponse();
        String numeroTelephone;
        String codeBanque = "0";
        String codeAgence = "0";
        String numeroCompte;
        String codePays;
        String teneur;
        String codeOperateur;

        MInfos minfos = null;
        if (extCallData != null && extCallData.getCompteInfo() != null) {
            Facturier facturier = facturierRepository.findByCodeFacturier(extCallData.getCodeFacturier());

            numeroTelephone = extCallData.getCompteInfo().getMsisdn();
            teneur = extCallData.getCompteInfo().getCodeFse();
            codeOperateur = extCallData.getCodeOperateur();
            numeroCompte = extCallData.getCompteInfo().getNumeroCompte();
            if (extCallData.getCompteInfo().getTypeCompte().equals("BANQUE")) {
                codeBanque = extCallData.getCompteInfo().getCodeBanque();
                codeAgence = extCallData.getCompteInfo().getCodeAgence();
            } else if (extCallData.getCompteInfo().getTypeCompte().equals("CARTE")) {
                codeBanque = "0";
                codeAgence = "0";
            }
            String malias = compteService.getCRC32Checksum(codeAgence, numeroCompte, numeroTelephone, codeOperateur);
            ExtBwCustomer extBwCustomer = extCallData.getCompteInfo();
            extBwCustomer.setAlias(malias);
            minfos = compteService.findByalias(malias);
            MResponse mResp = new MResponse();
            if (minfos == null) {
                MResponse mResponse = extService.createBwCustomer(extBwCustomer, extBwCustomer.getCodeFse(), true);
                if (mResponse != null && mResponse.getCode().equals("0")) {
                    minfos = compteService.findByalias(malias);

                } else {
                    mResp.setCode("-100");

                }
            }
            MFrais mFrais = externalService.calculFrais(new MParamFrais(extCallData.getCodePv(), extCallData.getTypeService(), extCallData.getCanal(), facturier.getUniteOrganisation().getCodeUO(), extCallData.getCodeUoSrc(), extCallData.getMontant(), minfos));
            if (mFrais != null) {
                mFraisResponse.setFees(mFrais);
                mFraisResponse.setCode("0");
            } else {
                mResp.setCode("-100");

            }
        }

        return mFraisResponse;
    }

    @PostMapping("/operations/paiement-facture")
    @Timed
    public MResponse paiementFacture(@RequestParam String codePays,
            @RequestParam String telephone,
            @RequestParam String codePv,
            @RequestParam BigDecimal montant,
            @RequestParam String codeFacturier,
            @RequestParam String fees,
            @RequestParam Long produitId,
            @RequestParam String canal,
            @RequestParam String referenceFacture,
            @RequestParam(required = false) String codeFseSrc, @RequestParam String typeService,
            @RequestBody MParametrePaiementFacture mlistparametre
    ) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.info("Debut d'execution de la methode paiement facture de external ressource");
        try {
            Optional<PointVente> pointVenteExist = pointVenteService.findByCode(codePv);
            /*if (!(codeFseSrc != null)) {
                codeFseSrc = parametreAppRepository.findByTypeAndNomAndCodeFse("FOURNISSEUR_MONNAIE", codePays).getValeur();
            }
             */
            boolean airtimePaiement = false;
            UniteOrganisation uoSrc = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(mlistparametre.getCompteInfo().getCodeFse());
            if (pointVenteExist.isPresent() && uoSrc != null) {
                if (pointVenteExist.get().getUniteOrganisation() != null) {
                    MOperationFacture mOperation = new MOperationFacture();
                    mOperation.setTypeService(typeService.toUpperCase());
                    mOperation.setCanal(canal);
                    mOperation.setCodePv(codePv);
                    mOperation.setMontant(montant);
                    mOperation.setActeur("ORGANISATION");
                    mOperation.setApplication("AUTRE");
                    mOperation.setMotif("M2");
                    mOperation.setUserConnecte("external");
                    mOperation.setEmetteur(getMPersonne(mlistparametre.getCompteInfo(), mlistparametre.getCodeOperateur()));
                    Facturier facturier = this.facturierRepository.findByCodeFacturier(codeFacturier);
                    //Code fact
                    if (facturier != null) {
                        if (facturier.getSecteurActivite().equals(SecteurActivite.TELEPHONIE)) {
                            mOperation.setTypeService("PAIEMENT_AIRTIME");
                            airtimePaiement = true;
                        }
                        MFacturier mFacturier = new MFacturier();
                        mFacturier.setNomFacturier(facturier.getNomFacturier());
                        mFacturier.setId(facturier.getId());
                        mOperation.setFacturier(mFacturier);
                        mOperation.setListParam(mlistparametre.getListParametreProduit().getMparametre());
                        Produit produit = produitRepository.findByProduitId(produitId);
                        if (produit != null) {
                            MProduit mProduit = new MProduit();
                            mProduit.setId(produit.getId());
                            mOperation.setProduit(mProduit);
                            mOperation.setReferenceFacture(referenceFacture);
                            /* Calcul des frais*/
                            String codeUoDest = facturier.getUniteOrganisation().getCodeUO();
                            MFrais mFrais = externalService.calculFrais(new MParamFrais(codePv, mOperation.getTypeService(), canal, uoSrc.getCodeUO(), codeUoDest, montant, null));
                            mOperation.setMontantFrais(Util.getZeroIfNull(mFrais.getFraisOperation()));
                            mOperation.setMontantTaxe(Util.getZeroIfNull(mFrais.getTaxeOPeration()));
                            if (!airtimePaiement) {
                                return externalService.doOperationPaiementFacture(mOperation);
                            } else {
                                return externalService.doOperationPaiementCreditAirtime(mOperation);
                            }

                        } else {
                            return new MResponse("-1", "Produit introuvable");
                        }
                    } else {
                        return new MResponse("-1", "Facturier introuvable");
                    }

                } else {
                    return new MResponse("-1", "Impossible de trouver l'unité d'organisation correspondant au point de vente.");
                    //throw new CustomParameterizedException("Impossible de trouver l'unité d'organisation correspondant au point de vente.");
                }
            } else {
                return new MResponse("-1", "Impossible de trouver l'unité d'organisation correspondant au point de vente.");
            }
        } catch (CustomSettlementException e) {
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new MResponse("-1", e.getMessage());
        } catch (CustomParameterizedException e) {
            return new MResponse("-1", e.getMessage());
        }
    }

    @GetMapping("/coris-money/clients")
    @Timed
    public ResponseEntity<CustomersInfo> getClients(@RequestParam(required = false) String phoneNumber, @RequestParam(required = false) String lastName, @RequestParam(required = false) String firstName, @RequestParam(required = false) String walletNumber, Pageable pageable) {

        log.debug("REST request to get a page of Clients");

        CustomersInfo customersInfo = new CustomersInfo();

        Page<ClientDTO> page = externalService.getAllClientPageOptimise(phoneNumber, lastName, firstName, walletNumber, null, "COMO", pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/coris-money/clients");

        List<Customers> p = externalService.getAllDTOClientExternal(page, walletNumber, "COMO");

        customersInfo.setCustomerAccounts(p);

        return new ResponseEntity<>(customersInfo, headers, HttpStatus.OK);

    }

    @GetMapping("/coris-cash/clients")
    @Timed
    public ResponseEntity<CustomersInfo> getClientsCorisCash(@RequestParam(required = false) String customerNumber,
            @RequestParam(required = false) String cardNumber,
            @RequestParam(required = false) String last4Digit,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String identityDocumentNumber,
            Pageable pageable) {
        log.debug("REST request to get a page of Clients Coris Cash");
        customerNumber = blankToNull(customerNumber);
        cardNumber = blankToNull(cardNumber);
        last4Digit = blankToNull(last4Digit);
        lastName = blankToNull(lastName);
        firstName = blankToNull(firstName);
        identityDocumentNumber = blankToNull(identityDocumentNumber);
        log.debug("customerNumber:{}, cardNumber:{}, last4Digit:{}, lastName:{}, firstName:{}, identityDocumentNumber:{}",
                customerNumber, cardNumber, last4Digit, lastName, firstName, identityDocumentNumber);
        CustomersInfo customersInfo = new CustomersInfo();
        // customerNumber = téléphone du client
        Page<ClientDTO> page = externalService.getAllClientPageOptimise(customerNumber, lastName, firstName, cardNumber, last4Digit, "GTP", pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/coris-cash/clients");
        List<Customers> p = externalService.getAllDTOClientExternal(page, identityDocumentNumber, "GTP");
        customersInfo.setCustomerAccounts(p);
        return new ResponseEntity<>(customersInfo, headers, HttpStatus.OK);
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
