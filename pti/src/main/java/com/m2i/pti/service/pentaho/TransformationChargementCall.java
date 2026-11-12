package com.m2i.pti.service.pentaho;

import com.m2i.omnicanal.outils.restclient.model.MessageInt;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.m2i.pti.domain.Canal;
import com.m2i.pti.domain.enumeration.*;
import com.m2i.pti.repository.ChargementTransfertRepository;
import com.m2i.pti.service.*;
import com.m2i.pti.domain.ChargementTransfert;
import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.LigneChargementTransfert;
import com.m2i.pti.domain.MotifOperation;
import com.m2i.pti.domain.Operation;
import com.m2i.pti.domain.Personne;
import com.m2i.pti.domain.PointVente;
import com.m2i.pti.domain.TypeService;
import com.m2i.pti.domain.User;
import com.m2i.pti.domain.UniteOrganisation;

import com.m2i.pti.mobile.dto.MCompte;
import com.m2i.pti.mobile.dto.MInfos;

import com.m2i.pti.mobile.dto.MLigneChargement;
import com.m2i.pti.mobile.dto.MOperation;
import com.m2i.pti.mobile.dto.MPersonne;
import com.m2i.pti.mobile.dto.MResponseOperation;
import com.m2i.pti.mobile.dto.MchargementTransfert;
import com.m2i.pti.mobile.enumeration.MTypeCompte;
import com.m2i.pti.mobile.service.MobileService;
import com.m2i.pti.repository.CompteAutreRepository;
import com.m2i.pti.repository.CompteRepository;
import com.m2i.pti.repository.OperationRepository;
import com.m2i.pti.service.ApiService;
import com.m2i.pti.service.CanalService;
import com.m2i.pti.service.ChargementTransfertService;
import com.m2i.pti.service.ClientService;
import com.m2i.pti.service.CompteService;
import com.m2i.pti.service.LigneChargementTransfertService;
import com.m2i.pti.service.PointVenteService;
import com.m2i.pti.service.TransactionComptePtiService;
import com.m2i.pti.service.TypeServiceService;
import com.m2i.pti.service.UserService;
import com.m2i.pti.service.UniteOrganisationService;
import com.m2i.pti.service.easyBatch.ReaderChargementService;
import com.m2i.pti.service.easyBatch.Read;
import com.m2i.pti.service.mapper.CompteAutreMapper;
import com.m2i.pti.service.pentaho.TransformationChargement;
import com.m2i.pti.service.util.RandomUtil;
import com.m2i.pti.web.rest.errors.ChargementException;
import io.github.jhipster.web.util.ResponseUtil;
import com.m2i.pti.service.kafka.MessageProducer;
import com.m2i.pti.service.mapper.LigneChargementMapper;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.record.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

/**
 * REST controller for managing ChargementTransfert.
 */
@Service
public class TransformationChargementCall {

    private final Logger log = LoggerFactory.getLogger(TransformationChargementCall.class);

    private static final String ENTITY_NAME = "chargementTransfert";

    private final ChargementTransfertService chargementTransfertService;

    private final ReaderChargementService readerChargementService;

    private final LigneChargementTransfertService ligneChargementTransfertService;

    private static final String CHARGEMENT_PATTERN = "^(?:[a-zA-Z]+;[a-zA-Z]+;[0-9]+;[0-9]+$)";

    private final TransactionComptePtiService transactionComptePtiService;
    private final CompteService compteService;
    private final TypeServiceService typeServiceService;
    private final CanalService canalService;
    private final ClientService clientService;
    private final ChargementTransfertRepository chargementTransfertRepository;
    private static final String PCHARGEMENT_CARTE = "^(?:(503578|507992)[0-9]{10})";
    private final UserService userService;
    final ApiService apiService;
    final MobileService mobileService;
    final OperationRepository operationRepository;
    private final Read readService;
    private Integer nombreLigneCharge = 0;
    private Integer nombreLigneErreur = 0;
    CompteRepository compteRepository;
    CompteAutreRepository compteAutreRepository;
    MotifOperationService motifOperationService;
    PointVenteService pointVenteService;
    UniteOrganisationService uniteOrganisationService;
    TaskExecutor taskExecutor;
    TransformationChargement transformationChargement;
    CompteAutreMapper compteAutreMapper;
    MessageProducer messageProducer;
    LigneChargementMapper ligneChargementMapper;

    public TransformationChargementCall(LigneChargementMapper ligneChargementMapper, CompteAutreRepository compteAutreRepository, CompteAutreMapper compteAutreMapper, MotifOperationService motifOperationService, TransformationChargement transformationChargement, TaskExecutor taskExecutor, ChargementTransfertRepository chargementTransfertRepository, UniteOrganisationService uniteOrganisationService, PointVenteService pointVenteService, MobileService mobileService, CompteRepository compteRepository, ClientService clientService, CompteService compteService, OperationRepository operationRepository, UserService userService, CanalService canalService, TypeServiceService typeServiceService, TransactionComptePtiService transactionComptePtiService, ChargementTransfertService chargementTransfertService, ReaderChargementService readerChargementService, LigneChargementTransfertService ligneChargementTransfertService, ApiService apiService, Read readService) {
        this.chargementTransfertService = chargementTransfertService;
        this.readerChargementService = readerChargementService;
        this.ligneChargementTransfertService = ligneChargementTransfertService;
        this.chargementTransfertRepository = chargementTransfertRepository;
        this.apiService = apiService;
        this.readService = readService;
        this.transactionComptePtiService = transactionComptePtiService;
        this.canalService = canalService;
        this.typeServiceService = typeServiceService;
        this.userService = userService;
        this.operationRepository = operationRepository;
        this.compteService = compteService;
        this.clientService = clientService;
        this.compteRepository = compteRepository;
        this.mobileService = mobileService;
        this.pointVenteService = pointVenteService;
        this.uniteOrganisationService = uniteOrganisationService;
        this.taskExecutor = taskExecutor;
        this.transformationChargement = transformationChargement;
        this.motifOperationService = motifOperationService;
        this.compteAutreRepository = compteAutreRepository;
        this.compteAutreMapper = compteAutreMapper;
        this.ligneChargementMapper = ligneChargementMapper;

    }

    public Operation doTransfertEnMasse(MOperation mOperation, String codeExterne, String service) {
        Operation operation = new Operation();
        ZonedDateTime dateTime = ZonedDateTime.now();
        User user = userService.findeByLogin(mOperation.getUserConnecte());
        log.debug("************Op********");
        log.debug("********************" + mOperation.getCodePv());
        TypeService typeService = typeServiceService.findByCodeService(service);
        MotifOperation motifOperation = motifOperationService.findByCode(mOperation.getMotif());
        operation.setMotifOperation(motifOperation);
        String codeUoAcq;
        operation = mobileService.saveInformationPersonne(mOperation.getEmetteur(), Boolean.FALSE, operation);
        Optional<PointVente> pointVenteExist = pointVenteService.findByCode(mOperation.getCodePv());
        if (mOperation.getCodeUoAcq() == null) {
            codeUoAcq = pointVenteExist.get().getUniteOrganisation().getCodeUO();
        } else {
            codeUoAcq = mOperation.getCodeUoAcq();
        }
        UniteOrganisation paysPaiement = uniteOrganisationService.findByCodeUOAndSupprimeIsFalse(mOperation.getCodePays(), false);
        if (paysPaiement != null) {
            operation.uoAcquereur(paysPaiement);
        }
        UniteOrganisation uoEmetteur = uniteOrganisationService.findByCodeUOAndSupprimeIsFalse(mOperation.getCodeUo(), false);
        if (uoEmetteur != null) {
            operation.uoEmetteur(uoEmetteur);
        }
        ////operationService.verifAutorisationOfTypeService(typeService, organisation);
        mOperation.setTypeService(service);
        mOperation.setCanal("MOBILE");
        operation.compteSrc(mobileService.getCompte(mOperation.getEmetteur().getCompte(), operation.getEmetteur().getPersonne(), mOperation.getApplication(),Boolean.FALSE));
        Canal canal = canalService.findByCodeCanal(mOperation.getCanal());
        mOperation = mobileService.getMontantCalculFrais(mOperation, mOperation.getCodePv(), mOperation.getCanal(), mOperation.getCodeUoEm(), codeUoAcq, mOperation.getMontant());
        operation.montantOp(mOperation.getMontant())
                .montantFrais(mOperation.getMontantFrais())
                .montantTaxe(mOperation.getMontantTaxe())
                .typeService(typeService)
                .statut(StatutOperation.VALIDE)
                .canal(canal)
                .description("PAIEMENT EN MASSE REF: " + codeExterne)
                .montantOp(mOperation.getMontant())
                .codeExterne(codeExterne)
                .codeOperation(RandomUtil.getCodeOperation(dateTime))
                .dateOperation(dateTime)
                .pointVente(pointVenteExist.get())
                .montantOpEnDevise(mOperation.getMontant())
                .user(user);
        //operation = mobileService.saveInformationPersonne(mOperation.getEmetteur(), Boolean.FALSE, operation);
        operation = operationRepository.save(operation);
        return operation;
    }

    public ResponseEntity<List<MchargementTransfert>> getLigneChargementTransfert(@RequestParam String telephone, @RequestParam String code, @RequestParam String statut) {
        Client cl = mobileService.getCustomer(telephone, code);
        List<ChargementTransfert> listchargementTransfert = chargementTransfertService.findByClient(cl, statut);
        List<MchargementTransfert> mlistChargement = new ArrayList();
        listchargementTransfert.forEach((ChargementTransfert chargementTransfert) -> {
            MchargementTransfert mchargement = new MchargementTransfert();
            mchargement.setDateChargement(chargementTransfert.getDateChargement());
            mchargement.setIdChargement(chargementTransfert.getId());
            mchargement.setNomFichier(chargementTransfert.getNomFichier());
            mchargement.setReference(chargementTransfert.getReference());
            mchargement.setNombreLigne(chargementTransfert.getNombreLigne());
            mchargement.setNombreLigneCharge(chargementTransfert.getNombreLigneCharge());
            mchargement.setNombreLigneErreur(chargementTransfert.getNombreLigneErreur());
            mchargement.setStatut(chargementTransfert.getStatut());
            mchargement.setDescription(chargementTransfert.getDescription());
            List<MLigneChargement> mlistLigneChargement = new ArrayList();
            List<LigneChargementTransfert> listLigneChargementTransfert = ligneChargementTransfertService.queryDetail(chargementTransfert.getId());
            listLigneChargementTransfert.forEach((LigneChargementTransfert ligneChargementTransfert) -> {
                MLigneChargement mligneChargement = new MLigneChargement();
                mligneChargement.setMontant(ligneChargementTransfert.getMontant());
                mligneChargement.setNom(ligneChargementTransfert.getNom());
                mligneChargement.setPrenom(ligneChargementTransfert.getPrenom());
                mligneChargement.setError(ligneChargementTransfert.getError());
                mligneChargement.setNumeroCompte(ligneChargementTransfert.getNumeroCompte());
                mligneChargement.setNumeroTelephone(ligneChargementTransfert.getNumeroTelephone());
                mlistLigneChargement.add(mligneChargement);
            });
            mchargement.setmLigneChargement(mlistLigneChargement);
            mlistChargement.add(mchargement);
        });

        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(mlistChargement));
    }

    private static MInfos getBeneficiaire(LigneChargementTransfert lgCharge, Compte compteCredit) {

        MInfos benef = new MInfos();
        //Compte compteCredit = compteService.findByCompteFse(ligneChargementTransfert.getNumeroTelephone(), this.fournisseurMonnaie);

        MPersonne persE = new MPersonne();

        // MPersonne Emetteur
        persE.setNom(lgCharge.getNom());
        persE.setPrenom(lgCharge.getPrenom());
        persE.setTelephone(lgCharge.getNumeroTelephone());
        persE.setCodeTelephone(lgCharge.getChargementTransfert().getCodePays());

        benef.setTitulaire(persE);
        if (compteCredit != null) {
            MCompte cptE = new MCompte();
            cptE.setCodeAgence("");
            cptE.setCodeBanque("");
            cptE.setNumeroCompte(compteCredit.getValeurCompte());
            cptE.setTeneurCompte(compteCredit.getCompteAutre().getCodeFse());
            cptE.setTypeCompte(MTypeCompte.AUTRE);
            benef.setCompte(cptE);
        }

        return benef;
    }

    private static MInfos getBeneficiaire(MLigneChargement lgCharge, Compte compteCredit) {

        MInfos benef = new MInfos();
        //Compte compteCredit = compteService.findByCompteFse(ligneChargementTransfert.getNumeroTelephone(), this.fournisseurMonnaie);

        MPersonne persE = new MPersonne();

        // MPersonne Emetteur
        persE.setNom(lgCharge.getNom());
        persE.setPrenom(lgCharge.getPrenom());
        persE.setTelephone(lgCharge.getNumeroTelephone());
        persE.setCodeTelephone(lgCharge.getCodePays());

        benef.setTitulaire(persE);
        if (compteCredit != null) {
            MCompte cptE = new MCompte();
            cptE.setCodeAgence("");
            cptE.setCodeBanque("");
            cptE.setNumeroCompte(compteCredit.getValeurCompte());
            cptE.setTeneurCompte(compteCredit.getCompteAutre().getCodeFse());
            cptE.setTypeCompte(MTypeCompte.AUTRE);
            benef.setCompte(cptE);
        }

        return benef;
    }

    private static MInfos getEmeteur(Compte compteDebit) {
        Personne pers = compteDebit.getClient().getPersonne();
        MInfos emetteur = new MInfos();
        if (pers.getTypePersonne().equals(TypePersonne.MORALE)) {
            MPersonne persE = new MPersonne();
            MCompte cptE = new MCompte();
            // MPersonne Emetteur
            persE.setNom(pers.getPersonneMorale().getRaisonSociale());
            persE.setPrenom(pers.getPersonneMorale().getRaisonSociale());
            persE.setTelephone(pers.getTelephone());
            persE.setCodeTelephone(pers.getCodePaysTelephone());
            cptE.setCodeAgence("");
            cptE.setCodeBanque("");
            cptE.setNumeroCompte(compteDebit.getValeurCompte());
            cptE.setTeneurCompte(compteDebit.getCompteAutre().getCodeFse());
            // if (compteDebit.getTypeCompte().equals(CategorieCompte.BANQUE)) {
            //   cptE.setTypeCompte(MTypeCompte.BANQUE);
            // } else {
            cptE.setTypeCompte(MTypeCompte.AUTRE);
            //}
            emetteur.setTitulaire(persE);
            emetteur.setCompte(cptE);
        }
        return emetteur;
    }

    public void chargementLigne(MLigneChargement ligneChargementTransfert, Long compteDebit, Long operationId) {
        MResponseOperation response = null;
        Compte cptDebit = compteService.findCompteOne(compteDebit);
        Operation operation = operationRepository.findByOperationId(operationId);
        StopWatch stopWatch = new StopWatch("Measure Code Execution");
        LigneChargementTransfert ligneTrans = ligneChargementMapper.toEntity(ligneChargementTransfert);
        ligneTrans.setId(ligneChargementTransfert.getLigneChargementTransfertId());
        try {
            if (ligneChargementTransfert != null && !ligneChargementTransfert.getStatutLigne().equals(StatutLigneChargement.OC.name())) {

                ResultMessage resMessage;  //  (ResultMessage)apiService.convert(result.getBody(),ResultMessage.class);
                ResultMessage resMessageEnvoiMsg;
                String typeService;
                DecimalFormat df = new DecimalFormat("###");
                // new BigDecimal(ligneChargementTransfert.getMontant())

                Compte compteCredit = null;

                BigDecimal montantApres = cptDebit.getCompteAutre().getSolde().subtract(BigDecimal.valueOf(Double.parseDouble(ligneChargementTransfert.getMontant().toString())));
                if (ligneChargementTransfert.getTypeTransfert().equals(0)) { //transert vers compte
                    typeService = "TRANSFERT_MASSE";
                    stopWatch.start("1. Recherche de la personnne");
                    if (ligneChargementTransfert.getCompteId() != null) {
                        compteCredit = compteService.findCompteOne(ligneChargementTransfert.getCompteId());
                    } else {
                        ligneChargementTransfert.setError("Compte introuvable");
                        throw new ChargementException(ligneChargementTransfert.getError());
                    }
                    // compteCredit = mobileService.findComptePersonne(ligneChargementTransfert.getNumeroTelephone(), ligneChargementTransfert.getChargementTransfert().getCodePays());
                    stopWatch.stop();
                    if (compteCredit != null) {
                        if (compteCredit.getEtatCompte().equals(EtatCompte.FERME) || compteCredit.getStatutCompte().equals(StatutCompte.DESACTIVE)) {
                            ligneTrans.setError("Compte bloqué");
                            throw new ChargementException(ligneTrans.getError());
                        }
                    } else {
                        ligneTrans.setError("Compte introuvable");
                        throw new ChargementException(ligneTrans.getError());
                    }
                } else {
                    typeService = "TRANSFERT_MASSE_CASH";
                }
                /**
                 * Transfert vers compte
                 */
                if (montantApres.doubleValue() >= 0) {
                    String narration = "";
                    MOperation mOperation = new MOperation();
                    mOperation.setMontant(BigDecimal.valueOf(ligneChargementTransfert.getMontant()));
                    mOperation.setMotif(ligneChargementTransfert.getMotif());
                    mOperation.setCodePv(operation.getPointVente().getCodePointVente());
                    mOperation.setTypeService(typeService);
                    mOperation.setCanal("MOBILE");
                    mOperation.setDescription(typeService + " REF: " + ligneChargementTransfert.getReference());
                    mOperation.setCodeUo(operation.getUoEmetteur().getCodeUO());
                    mOperation.setCodePays(operation.getUoAcquereur().getCodeUO());
                    mOperation.setBeneficiaire(getBeneficiaire(ligneChargementTransfert, compteCredit));
                    if (ligneChargementTransfert.getNarration() != null) {
                        narration = ligneChargementTransfert.getNarration();
                    }
                    stopWatch.start("2. Appel de la fonction chargement PTI");
                    response = mobileService.doOperationChargement(mOperation, operation, narration, nombreLigneCharge,compteCredit,ligneChargementTransfert.getLigneChargementTransfertId());
                    stopWatch.stop();
                    if (response != null && response.getCode().equals("0")) {
                        ligneChargementTransfert.setSoldeApres(0D);
                        ligneTrans.setStatutLigne(StatutLigneChargement.OC);
                        nombreLigneCharge = nombreLigneCharge + 1;
                        operation.setMontantFrais(BigDecimal.ZERO);
                        operation.setMontantTaxe(BigDecimal.ZERO);
                        if (response.getOperation() != null) {
                            mobileService.envoiDesMessage(response.getOperation(),null);
                        }
                    }
                } else {
                    nombreLigneErreur = nombreLigneErreur + 1;
                    nombreLigneCharge = nombreLigneCharge + 1;
                    ligneTrans.setError("Solde insuffisant");
                }

                /**
                 *
                 * Faire le chargement
                 */
            } else {
                nombreLigneErreur = nombreLigneErreur + 1;
                ligneTrans.setError("Compte déjà crédité");
            }
            stopWatch.start("3.Enregistrement dans la table");
              ligneChargementTransfertService.save(ligneTrans);
            stopWatch.stop();
            System.out.println(stopWatch.prettyPrint());
        } catch (Exception e) {
            ligneTrans.setError(e.getMessage());
             ligneChargementTransfertService.save(ligneTrans);
        }
    }

    private static class ChargementProcessor implements RecordProcessor {

        boolean error = false;
        boolean errorT = false;
        MessageInt resultat = null;
        Integer isValide = null;
        ResponseEntity result;
        ResponseEntity resultatC;
        Integer nombreLigneCharge = 0;
        Integer nombreLigneErreur = 0;
        Compte cptDebit;
        MobileService mobileService;
        Operation operation;
        ReportBatch rpBatch;
        CompteRepository compteRepository;
        CompteService compteService;
        UniteOrganisationService uniteOrganisationService;
        private final LigneChargementTransfertService ligneChargementTransfertService;
        String fournisseurMonnaie;
        static TaskExecutor taskExecutor;

        public ChargementProcessor(String fournisseurMonnaie, CompteService compteService, UniteOrganisationService uniteOrganisationService, MobileService mobileService, CompteRepository compteRepository, Operation operation, Compte cptDebit, LigneChargementTransfertService ligneChargementTransfertService, ReportBatch rpBatch, TaskExecutor taskExecutor) {
            this.cptDebit = cptDebit;
            this.mobileService = mobileService;
            this.uniteOrganisationService = uniteOrganisationService;
            this.operation = operation;
            this.ligneChargementTransfertService = ligneChargementTransfertService;
            this.rpBatch = rpBatch;
            this.compteRepository = compteRepository;
            nombreLigneCharge = rpBatch.getNombreLigneCharge();
            nombreLigneErreur = rpBatch.getNombreLigneErreur();
            resultatC = null;
            result = null;
            this.compteService = compteService;
            this.fournisseurMonnaie = fournisseurMonnaie;
            this.taskExecutor = taskExecutor;
        }

        @Override
        public Record processRecord(Record record) throws Exception {

            LigneChargementTransfert ligneChargementTransfert = (LigneChargementTransfert) record.getPayload();

            Pattern p = Pattern.compile(PCHARGEMENT_CARTE);

            LigneChargementTransfert lgCharge = ligneChargementTransfert;
            if (ligneChargementTransfert.getStatutLigne().equals(StatutLigneChargement.NC)) {
                ligneChargementTransfert.setStatutLigne(StatutLigneChargement.IC);
            }
            error = false;
            ligneChargementTransfertService.save(lgCharge);
            /**
             * vérification du numéro de la carte après décryptage
             */
            Matcher m;
            boolean bool = false;

            try {

                if (ligneChargementTransfert.getStatutLigne().equals(StatutLigneChargement.IC)) {

                    ResultMessage resMessage;  //  (ResultMessage)apiService.convert(result.getBody(),ResultMessage.class);
                    ResultMessage resMessageEnvoiMsg;
                    String typeService;
                    DecimalFormat df = new DecimalFormat("###");
                    // new BigDecimal(ligneChargementTransfert.getMontant())
                    ligneChargementTransfert.setSoldeAvant(0D);
                    Compte compteCredit = null;
                    //il s'agit d'un transfert de carte à carte
                    /*
                                                         le débit est constaté et la carte désactivé
                     */
                    BigDecimal montantApres = cptDebit.getCompteAutre().getSolde().subtract(BigDecimal.valueOf(Double.parseDouble(ligneChargementTransfert.getMontant().toString())));
                    if (ligneChargementTransfert.getChargementTransfert().getTypeTransfert().equals(0)) { //transert vers compte
                        typeService = "TRANSFERT_MASSE";

                        //compteCredit = compteService.findByCompteFse(ligneChargementTransfert.getNumeroTelephone(), this.fournisseurMonnaie);
                        //     compteCredit = mobileService.findComptePersonne(ligneChargementTransfert.getNumeroTelephone(), ligneChargementTransfert.getChargementTransfert().getCodePays());
                        compteCredit = compteService.findCompteOne(ligneChargementTransfert.getCompteId());
                        if (compteCredit != null) {
                            if (compteCredit.getEtatCompte().equals(EtatCompte.FERME) || compteCredit.getStatutCompte().equals(StatutCompte.DESACTIVE)) {
                                ligneChargementTransfert.setError("Compte bloqué");
                            }
                        } else {
                            ligneChargementTransfert.setError("Compte introuvable");
                        }
                    } else {
                        typeService = "TRANSFERT_MASSE_CASH";
                    }
                    /**
                     * Transfert vers compte
                     */

                    if (montantApres.doubleValue() >= 0) {

                        String codeFse;
                        if (cptDebit.getCompteAutre() != null) {
                            codeFse = cptDebit.getCompteAutre().getCodeFse();
                        } else {
                            codeFse = cptDebit.getCompteBanque().getCodeFse();
                        }
                        String narration = "";
                        MInfos emeteur = getEmeteur(cptDebit);
                        UniteOrganisation fseSrc = uniteOrganisationService.findFseByCode(codeFse);
                        UniteOrganisation fsePays = uniteOrganisationService.findUoParentByType(fseSrc, TypeUO.PAYS);
                        // MFrais mFrais = mobileService.calculFrais(new MParamFrais(operation.getPointVente().getCodePointVente(), typeService, "MOBILE", fseSrc.getCodeUO(), fsePays.getCodeUO(), BigDecimal.valueOf(ligneChargementTransfert.getMontant()), emeteur));
                        MOperation mOperation = new MOperation();
                        mOperation.setMontant(BigDecimal.valueOf(ligneChargementTransfert.getMontant()));

                        mOperation.setMotif(ligneChargementTransfert.getChargementTransfert().getMotifOperation());

                        mOperation.setCodePv(operation.getPointVente().getCodePointVente());
                        mOperation.setTypeService(typeService);
                        mOperation.setCanal("MOBILE");
                        mOperation.setDescription(typeService + " REF: " + ligneChargementTransfert.getChargementTransfert().getReference());
                        mOperation.setCodePays(fsePays.getCodeUO());
                        mOperation.setCodeUo(fseSrc.getCodeUO());
                        mOperation.setEmetteur(emeteur);
                        mOperation.setBeneficiaire(getBeneficiaire(ligneChargementTransfert, compteCredit));
                        if (ligneChargementTransfert.getNarration() != null) {
                            narration = ligneChargementTransfert.getNarration();
                        }
                        MResponseOperation response = mobileService.doOperationChargement(mOperation, operation, narration, nombreLigneCharge,compteCredit,ligneChargementTransfert.getId());
                        if (response.getCode().equals("0")) {
                            ligneChargementTransfert.setSoldeApres(0D);
                            ligneChargementTransfert.setStatutLigne(StatutLigneChargement.OC);
                            nombreLigneCharge = nombreLigneCharge + 1;
                            operation.setMontantFrais(BigDecimal.ZERO);
                            operation.setMontantTaxe(BigDecimal.ZERO);
                            if (response.getOperation() != null) {
                                // if (!response.getOperation().getTypeService().getCode().equals(TypeServiceLibelle.TRANSFERT_MASSE.toString())) {
                                //mobileService.envoiDesMessage(response.getOperation());

                                //  mobileService.repartirCommission(response.getOperation());
                                //  taskExecutor.execute(new MessageMsgSender(response.getOperation()));
                                // }
                            }
                        }

                        //     ligneChargementCarteService.save(ligneChargementCarte);
                        //} else {
                        //  error = true;
                        //ligneChargementTransfert.setError("Erreur de chargement du compte");
                    } else {
                        error = true;
                        nombreLigneErreur = nombreLigneErreur + 1;
                        ligneChargementTransfert.setError("Solde insuffisant");
                    }

                    /**
                     *
                     * Faire le chargement
                     */
                } else {
                    error = true;
                    nombreLigneErreur = nombreLigneErreur + 1;
                    ligneChargementTransfert.setError("Compte déjà crédité");
                }

            } catch (Exception e) {
                nombreLigneErreur = nombreLigneErreur + 1;
                ligneChargementTransfert.setError(e.getMessage());
                ligneChargementTransfertService.save(ligneChargementTransfert);
                System.out.println(e.getMessage());
            }

            if (error) {
                nombreLigneErreur = nombreLigneErreur + 1;
            }

            rpBatch.setNombreLigneCharge(nombreLigneCharge);

            rpBatch.setNombreLigneErreur(nombreLigneErreur);

            ligneChargementTransfertService.save(ligneChargementTransfert);

            return record;
        }

        
    }

    private class ReportBatch {

        Integer nombreLigneCharge;
        Integer nombreLigneErreur;
        Double montant;

        public ReportBatch(Integer nombreLigneCharge, Integer nombreLigneErreur) {
            this.nombreLigneCharge = nombreLigneCharge;
            this.nombreLigneErreur = nombreLigneErreur;
        }

        public ReportBatch(Double montant) {
            this.montant = montant;
        }

        public Integer getNombreLigneCharge() {
            return nombreLigneCharge;
        }

        public void setNombreLigneCharge(Integer nombreLigneCharge) {
            this.nombreLigneCharge = nombreLigneCharge;
        }

        public Integer getNombreLigneErreur() {
            return nombreLigneErreur;
        }

        public void setNombreLigneErreur(Integer nombreLigneErreur) {
            this.nombreLigneErreur = nombreLigneErreur;
        }

        public Double getMontant() {
            return montant;
        }

        public void setMontant(Double montant) {
            this.montant = montant;
        }

    }

}
