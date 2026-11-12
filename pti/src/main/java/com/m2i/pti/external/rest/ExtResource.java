/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.external.rest;

import com.m2i.pti.domain.enumeration.EtiquetteCompteCode;
import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.CompteAutre;
import com.m2i.pti.domain.CompteBanque;
import com.m2i.pti.domain.PointVente;

import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.enumeration.TypeUO;
import com.m2i.pti.service.CompteService;
import com.m2i.pti.service.UniteOrganisationService;
import com.m2i.pti.external.domain.ExtBwCustomer;
import com.m2i.pti.external.service.ExtService;
import com.m2i.pti.mobile.dto.MInfos;
import com.m2i.pti.mobile.dto.MOperation;
import com.m2i.pti.mobile.dto.MResponse;
import com.m2i.pti.mobile.dto.MResponseOperation;
import com.m2i.pti.mobile.service.MobileService;
import com.m2i.pti.repository.BwAccountRepository;
import com.m2i.pti.repository.CompteAutreRepository;
import com.m2i.pti.repository.CompteBanqueRepository;
import com.m2i.pti.repository.CompteRepository;
import com.m2i.pti.repository.UniteOrganisationRepository;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Optional;
import com.m2i.pti.service.dto.Balance;
import com.m2i.pti.service.dto.Mouvement;
import com.m2i.pti.service.dto.UniteOrganisationDTO;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import jakarta.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;
import com.m2i.pti.domain.enumeration.TypeServiceLibelle;
import com.m2i.pti.service.PointVenteService;

/**
 *
 * @author cda
 */
@RestController
@RequestMapping("/api/v1")
public class ExtResource {

    ExtService extService;
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private final CompteRepository compteRepository;
    private final BwAccountRepository bwAccountRepository;
    private final CompteAutreRepository compteAutreRepository;
    private final CompteBanqueRepository compteBanqueRepository;
    private final UniteOrganisationRepository uniteOrganisationRepository;
    private final Logger log = LoggerFactory.getLogger(ExtResource.class);
    private final CompteService compteService;

    private final MobileService mobileService;
    private final UniteOrganisationService uniteOrganisationService;
    PointVenteService pointVenteService;

    public ExtResource(PointVenteService pointVenteService, MobileService mobileService, UniteOrganisationService uniteOrganisationService, CompteService compteService, ExtService extService, CompteRepository compteRepository, BwAccountRepository bwAccountRepository, CompteAutreRepository compteAutreRepository, CompteBanqueRepository compteBanqueRepository, UniteOrganisationRepository uniteOrganisationRepository) {
        this.extService = extService;
        this.compteRepository = compteRepository;
        this.bwAccountRepository = bwAccountRepository;
        this.compteAutreRepository = compteAutreRepository;
        this.compteBanqueRepository = compteBanqueRepository;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.compteService = compteService;
        this.uniteOrganisationService = uniteOrganisationService;
        this.mobileService = mobileService;
        this.pointVenteService = pointVenteService;

    }

    @PostMapping("/operations/link")
    @Timed
    public MResponse doLink(@RequestBody ExtBwCustomer extBwCustomer) throws IOException, NoSuchAlgorithmException {
        log.debug("REST request to save Operation : {}", extBwCustomer);
        // MResponse mResponse = extService.verifyLink(extBwCustomer.getNumeroCompte(), extBwCustomer.getMsisdn());
        // if (mResponse.getCode().equals("-11")) {
        boolean immediat = false;
        if (extBwCustomer.getActiveImmediat() != null && Boolean.TRUE.equals(extBwCustomer.getActiveImmediat())) {
            immediat = extBwCustomer.getActiveImmediat();
        }
        MResponse mResponse = extService.createBwCustomer(extBwCustomer, extBwCustomer.getCodeFse(), immediat);
        return mResponse;
        // }
        // return mResponse;
    }

    @PostMapping("/operations/delink")
    @Timed
    public MResponse doDelink(@RequestBody ExtBwCustomer extBwCustomer) throws IOException, NoSuchAlgorithmException {
        log.debug("REST request to save Operation : {}", extBwCustomer.getNumeroCompte());

        MResponse mResponse = extService.delinkBwCustomer(extBwCustomer, extBwCustomer.getCodeFse());
        return mResponse;

    }

    @PostMapping("/operations/mini-statement")
    @Timed
    public List<Mouvement> getMinStatement(@RequestParam String agence, @RequestParam String numeroCompte,
            @RequestParam String msisdn, @RequestParam String codeOperateur,
            @RequestParam String bankCible, @RequestParam String alias) throws Exception {
        ExtBwCustomer ec = setExtBwCustomer(numeroCompte, msisdn, alias);
        MResponse mResponse = extService.verifyLink(ec, msisdn, bankCible);

        if (mResponse.getCode().equals("0")) {
            UniteOrganisation fse = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(bankCible);
            if (fse != null) {
                String dateDebut = LocalDate.now().minusDays(90).format(dateTimeFormatter);
                String dateFin = LocalDate.now().format(dateTimeFormatter);
                return extService.getMiniStatement(agence, numeroCompte, fse.getCodeFse(), dateDebut, dateFin);
            } else {
                throw new Exception("La banque cible fournie n'existe pas");
            }
        } else {
            throw new Exception(mResponse.getMessage());
        }

    }

    @PostMapping("/operations/annulation")
    @Timed
    public MResponse setAnnulation(@RequestParam String codeOperation, @RequestParam String codePv
    ) throws IOException, NoSuchAlgorithmException, JAXBException {
        MResponseOperation mResponse = extService.doAnnulation(codeOperation, codePv);
        return new MResponse(mResponse.getCode(), mResponse.getMessage());
    }

    @PostMapping("/operations/solde")
    @Timed
    public Balance getSolde(@RequestParam String numeroCompte,
            @RequestParam String agence,
            @RequestParam String msisdn,
            @RequestParam String codeOperateur,
            @RequestParam String bankCible, @RequestParam String alias) throws Exception {

        ExtBwCustomer ec = setExtBwCustomer(numeroCompte, msisdn, alias);

        MResponse mResponse = extService.verifyLink(ec, msisdn, bankCible);
        if (mResponse.getCode().equals("0")) {
            UniteOrganisation fse = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(bankCible);
            if (fse != null) {
                return extService.getSolde(numeroCompte, agence, fse.getCodeFse());
            } else {
                throw new Exception("La banque cible fournie n'existe pas");
            }
        } else {
            throw new Exception(mResponse.getMessage());
        }
    }

    private Compte getCompteClient(ExtBwCustomer accountClient, String valeurCompte, Compte compteRef) {
        Compte compteDest = new Compte();
        CompteBanque cptbq = new CompteBanque();
        CompteAutre cptbqautre = new CompteAutre();
        if (accountClient.getCodeAgence().equals("0")) {
            cptbqautre.setCodeFse(accountClient.getCodeFse());
            compteDest.setValeurCompte(accountClient.getNumeroCompte());
            compteDest.setCompteAutre(cptbqautre);

        } else {
            cptbq.setCodeAgence(accountClient.getCodeAgence());
            cptbq.setCodeBanque(accountClient.getCodeBanque());
            compteDest.setValeurCompte(accountClient.getNumeroCompte());
            cptbq.setCodeFse(accountClient.getCodeFse());
            //     compteDest.setCodeFseProprietaire(codeOperateur);
            compteDest.setCompteBanque(cptbq);
        }

        return compteDest;
    }

    private UniteOrganisation returnUniteOrganisation(Compte cpt) {
        UniteOrganisation fseCompte = null;
        if (cpt.getCompteAutre() != null) {
            fseCompte = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(cpt.getCompteAutre().getCodeFse());

        } else if (cpt.getCompteBanque() != null) {
            fseCompte = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(cpt.getCompteBanque().getCodeFse());
        }
        return fseCompte;
    }

    private ExtBwCustomer setExtBwCustomer(String numeroCompte, String msisdn, String alias) {
        ExtBwCustomer ec = new ExtBwCustomer();
        ec.setNumeroCompte(numeroCompte);
        ec.setMsisdn(msisdn);
        if (alias != null) {
            ec.setAlias(alias);
        }

        return ec;
    }

    private Compte getCompteOperation(UniteOrganisation uoDestAgence, String etiquette) {

        Compte compte = compteService.getCompteOfOrganisationEtiquette(uoDestAgence, EtiquetteCompteCode.valueOf(etiquette));
        Compte compteOperation = compteService.getCompteOperationActeur(uoDestAgence, EtiquetteCompteCode.CPTEOPERATIONACTEUR.name());
        if (compte != null) {
            return compte;
        }

        return compteOperation;

    }

    @PostMapping("/operations/transfert")
    @Timed
    public MResponse doTransfert(@RequestParam String codePv, @RequestParam String referenceExterne, @RequestParam String numeroCompte, @RequestParam String msisdn,
            @RequestParam String montantOp, @RequestParam String codeOperateur, @RequestParam String api, @RequestParam String bankCible, @RequestParam String alias, @RequestParam(required = false) String canal) throws Exception {

        log.debug("DEBUT DU TRANSFERT PI");
        log.info("codePv" + codePv);
        log.info("msisdn  {}", msisdn);
        log.info("codeOperateur  {}", codeOperateur);
        log.info("codePv" + codePv);
        log.debug("numeroCompte {}", numeroCompte);
        log.debug("DEBUT DU TRANSFERT PI");

        String pcanal;
        Compte compteSrc;
        Compte compteComm;
        Compte compteDest = null;
        ExtBwCustomer ec = new ExtBwCustomer();
        ec.setNumeroCompte(numeroCompte);
        ec.setMsisdn(msisdn);

        pcanal = canal != null ? canal : "MOBILE";
        if (alias != null && alias.length() > 0) {
            ec.setAlias(alias);
        }
        MResponse mResponse = new MResponse();

        mResponse = extService.verifyLink(ec, msisdn, bankCible);

        String donneurOrdre;
        String beneficiaire;
        String description;
        UniteOrganisation fseCompte;
        MOperation mOperation = new MOperation();
        try {
            log.info("DEBUT DU TRANSFERT PI");
            if (mResponse != null && mResponse.getCode().equals("0")) {
                /*le code opérateur correspond au code fse de l'opérateur Mobile, Microfinance...**/
                UniteOrganisation fse = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(codeOperateur);
                if (fse == null) {
                    throw new Exception("Fournisseur inexistant");
                }
                UniteOrganisation uoDestAgence;
                UniteOrganisation agencesSiege;

                if (codeOperateur.equals(bankCible)) {
                    log.info("CAS DE PI ");
                    agencesSiege = uniteOrganisationService.getFseAgenceSiege(fse);
                    uoDestAgence = uniteOrganisationService.findByCodePv(codePv);
                } else {
                    uoDestAgence = uniteOrganisationService.getFseAgenceSiege(fse);
                    agencesSiege = uoDestAgence;
                }

                UniteOrganisationDTO uoPays = uniteOrganisationService.findPereByType(fse.getId(), TypeUO.PAYS);
                if (uoPays == null || uoDestAgence == null) {
                    return new MResponse("-1", "Erreur lors de l'operation");
                }
                ExtBwCustomer compteClient = extService.returnCustomer(numeroCompte, msisdn, bankCible, alias);

                if (compteClient != null) {
                    donneurOrdre = compteClient.getNomCustomer() + " " + compteClient.getPrenomCustomer();
                    beneficiaire = compteClient.getNomCustomer() + " " + compteClient.getPrenomCustomer();
                    description = api + " " + donneurOrdre + " " + beneficiaire;
                    //     BwAccount compteClient = bwAccountRepository.findByNumeroCompteAndBwCustomerMsisdn(numeroCompte, msisdn);
                    if (api.equals("WalletToBank") || api.equals("PiToAccount")) {
                        log.info("---------------------------Recuperation du compte de ----------------------: {}", bankCible);
                        //*** A REVOIR ******/
                        //  compteSrc = compteService.getCompteOperationActeur(uoDestAgence, EtiquetteCompteCode.CPTEOPERATIONACTEUR.name());
                        compteSrc = getCompteOperation(uoDestAgence, EtiquetteCompteCode.CPTEOPERATIONRETRAIT.name());

                        compteComm = compteService.getCompteOperationActeur(agencesSiege, EtiquetteCompteCode.CPTEATTENTECOMM.name());
                        //compteSrc = compteRepository.findCompteByCodeFseProprietaire(fse.getCodeFse());
                        log.info("---------------------------Recuperation du compte  du client----------------------: {}", numeroCompte);
                        if (compteClient != null) {
                            compteDest = getCompteClient(compteClient, numeroCompte, compteSrc);
                            MInfos mInfo = extService.getMPersonne(numeroCompte, msisdn, compteDest, uoPays.getCodePays(), alias);
                            fseCompte = returnUniteOrganisation(compteDest);
                            if (api.equals("PiToAccount")) {

                                mOperation.setEmetteur(null);
                            } else {

                                mOperation.setEmetteur(mInfo);
                            }
                            mOperation.setBeneficiaire(mInfo);
                            mOperation.setCanal(pcanal);
                            mOperation.setCodeUoAcq(fseCompte.getCodeUO());
                            mOperation.setCodeUoEm(fse.getCodeUO());
                            log.info("---------------------------ENTREE TRANSFERT----------------------: {}", numeroCompte);
                            MResponseOperation mresp = extService.doTransfert(mOperation, codePv, compteSrc, compteDest, compteComm, new BigDecimal(montantOp), referenceExterne, api, description, donneurOrdre, beneficiaire, alias);
                            log.info("---------------------------FIN TRANSFERT----------------------: {}", numeroCompte);
                            if (mresp != null) {
                                if (mresp.getOperation() != null) {
                                    //   if (!rep.getOperation().getTypeService().getCode().equals(TypeServiceLibelle.ACCOUNT_CASH.toString())) {
                                    mobileService.envoiDesMessage(mresp.getOperation(), null);
                                    // }               
                                    ///mobileService.repartirCommission(mresp.getOperation());
                                    return new MResponse(mresp.getCode(), mresp.getMessage(), mresp.getOperation().getCodeOperation());
                                } else {
                                    return new MResponse(mresp.getCode(), mresp.getMessage(), null);
                                }
                            }

                        } else {
                            throw new CustomParameterizedException("Compte inexistant");
                        }
                    } else if (api.equals("BankToWallet") || api.equals("AccountToPi")) {
                        log.debug("---------------------------Recuperation du compte de ----------------------: {}", codeOperateur);
                        // compteDest = compteService.getCompteOperationActeur(uoDestAgence, EtiquetteCompteCode.CPTEOPERATIONACTEUR.name());

                        compteComm = compteService.getCompteOperationActeur(agencesSiege, EtiquetteCompteCode.CPTEATTENTECOMM.name());
                        compteDest = getCompteOperation(uoDestAgence, EtiquetteCompteCode.CPTEOPERATIONENVOI.name());

//compteDest = compteRepository.findCompteByCodeFseProprietaire(fse.getCodeFse());
                        if (compteClient != null) {
                            compteSrc = getCompteClient(compteClient, numeroCompte, compteDest);
                            MInfos mInfo = extService.getMPersonne(numeroCompte, msisdn, compteSrc, uoPays.getCodePays(), alias);
                            mOperation.setBeneficiaire(mInfo);
                            mOperation.setIdentifiant(msisdn);

                            if (api.equals("AccountToPi")) {
                                mOperation.setBeneficiaire(null);

                            } else {
                                mOperation.setBeneficiaire(mInfo);

                            }
                            mOperation.setEmetteur(mInfo);
                            fseCompte = returnUniteOrganisation(compteSrc);
                            mOperation.setCodeUoEm(fseCompte.getCodeUO());
                            mOperation.setCodeUoAcq(fse.getCodeUO());
                            mOperation.setMontant(new BigDecimal(montantOp));
                            mOperation.setCanal(pcanal);
                            MResponseOperation mresp = extService.doTransfert(mOperation, codePv, compteSrc, compteDest, compteComm, new BigDecimal(montantOp), referenceExterne, api, description, donneurOrdre, beneficiaire, alias);
                            MResponse m = new MResponse();
                            if (mresp != null) {
                                if (mresp.getOperation() != null) {
                                    mobileService.envoiDesMessage(mresp.getOperation(), null);
                                    // }               
                                    //   mobileService.repartirCommission(mresp.getOperation());
                                    return new MResponse(mresp.getCode(), mresp.getMessage(), mresp.getOperation().getCodeOperation());
                                } else {
                                    return new MResponse(mresp.getCode(), mresp.getMessage(), null);
                                }

                            }
                        }

                    }
                } else {
                    throw new CustomParameterizedException("Compte inexistant");
                }
            } else {
                throw new CustomParameterizedException(mResponse.getMessage());
            }
        } catch (CustomParameterizedException e) {

            return new MResponse("-1", e.getMessage(), null);

        }

        return new MResponse("-1", "Erreur lors du traitement", null);
    }

    @PostMapping("/operations/paiementMarchand")
    @Timed
    public MResponse doOperationPaiementMarchand(@RequestParam String codePvSrc, @RequestParam String codePvDest, @RequestParam String referenceExterne, @RequestParam String numeroCompte, @RequestParam String codeOperateur, @RequestParam String bankCible, @RequestParam String montantOp, @RequestParam String donneurOrdre, @RequestParam String beneficiaire, @RequestParam(required = false) String canal) throws Exception {
        MOperation mOperation = new MOperation();
        String pcanal = canal != null ? canal : "MOBILE";
        mOperation.setCodePv(codePvDest);
        mOperation.setTypeService(TypeServiceLibelle.PI_PAIEMENT_MARCHAND.name());
        UniteOrganisation fse = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(codeOperateur);
        if (fse == null) {
            throw new Exception("Fournisseur inexistant");
        }
        UniteOrganisation uoSrcAgence;
        UniteOrganisation agencesSiege;
        if (codeOperateur.equals(bankCible)) {
            log.info("CAS DE PI ");
            agencesSiege = uniteOrganisationService.getFseAgenceSiege(fse);
            uoSrcAgence = uniteOrganisationService.findByCodePv(codePvSrc);
        } else {
            uoSrcAgence = uniteOrganisationService.getFseAgenceSiege(fse);
            agencesSiege = uoSrcAgence;
        }
        UniteOrganisationDTO uoPays = uniteOrganisationService.findPereByType(fse.getId(), TypeUO.PAYS);
        if (uoPays == null || uoSrcAgence == null) {
            //return new MResponse("-1", "Erreur lors de l'operation");
        }
        PointVente opt = pointVenteService.findPointByCode(codePvDest);
        Optional<PointVente> pointVenteExist = Optional.of(opt);
        if (pointVenteExist.isPresent()) {

            UniteOrganisation uoDest = pointVenteExist.get().getUniteOrganisation();
            uoDest = uniteOrganisationService.findOneById(uoDest.getId());
            Compte compteDest = getCompteOperation(uoDest, EtiquetteCompteCode.CPTEOPERATIONACTEUR.name());
            Compte compteSrc = getCompteOperation(uoSrcAgence, EtiquetteCompteCode.CPTEOPERATIONRETRAIT.name());
            Compte compteComm = compteService.getCompteOperationActeur(agencesSiege, EtiquetteCompteCode.CPTEATTENTECOMM.name());
            //   if (compteDest != null && numeroCompte.equals(compteDest.getValeurCompte())) {
            mOperation.setCanal(pcanal);
            mOperation.setCodeUoAcq(uoDest.getCodeUO());
            mOperation.setCodeUoEm(uoSrcAgence.getCodeUO());
            mOperation.setMontant(new BigDecimal(montantOp));
            MResponseOperation mresp = mobileService.doOperationPaiementMarchandPI(pointVenteExist.get(), uoSrcAgence, uoDest, mOperation, compteSrc, compteDest, donneurOrdre, beneficiaire);
            if (mresp != null) {
                if (mresp.getOperation() != null) {
                    mobileService.envoiDesMessage(mresp.getOperation(), null);

                    return new MResponse(mresp.getCode(), mresp.getMessage(), mresp.getOperation().getCodeOperation());
                } else {
                    return new MResponse(mresp.getCode(), mresp.getMessage(), null);
                }

            }
            //  } else {
            // return new MResponse("-1", "le compte d'operation est incorrect", null);
            //   }

        } else {
            return new MResponse("-1", "Le point de vente est inexistant", null);
        }
        return new MResponse("-1", "Erreur lors du traitement", null);
    }
}
