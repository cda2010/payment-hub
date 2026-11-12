package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.outils.restclient.AuthTokenInfo;
import com.m2i.omnicanal.outils.restclient.model.MessageInt;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.m2i.pti.batch.reader.JobService;
import com.m2i.pti.domain.Canal;
import com.m2i.pti.domain.enumeration.*;
import com.m2i.pti.repository.ChargementTransfertRepository;
import com.m2i.pti.service.*;
import com.m2i.pti.service.easyBatch.CSVTransfert;
import com.m2i.pti.domain.ChargementTransfert;
import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.CompteAutre;
import com.m2i.pti.domain.LigneChargement;
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
import com.m2i.pti.mobile.dto.ChargementTransfertMessageDTO;

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
import com.m2i.pti.service.dto.Balance;
import com.m2i.pti.service.dto.CompteAutreDTO;
import com.m2i.pti.service.easyBatch.ReaderChargementService;
import com.m2i.pti.service.easyBatch.Read;
import com.m2i.pti.service.easyBatch.TransfertAssembler;
import com.m2i.pti.service.mapper.CompteAutreMapper;
import com.m2i.pti.service.pentaho.TransformationChargement;
import com.m2i.pti.service.util.RandomUtil;
import com.m2i.pti.service.util.Util;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import com.m2i.pti.web.rest.errors.ChargementException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import com.m2i.pti.service.kafka.MessageProducer;
import org.easybatch.core.job.Job;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.record.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.xml.bind.JAXBException;
import org.easybatch.core.filter.RecordFilter;
import org.easybatch.core.job.JobParameters;
import org.easybatch.core.job.JobReport;
import org.easybatch.core.listener.JobListener;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.trans.TransMeta;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StopWatch;
import com.m2i.pti.service.mapper.LigneChargementMapper;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.springframework.scheduling.annotation.Async;

/**
 * REST controller for managing ChargementTransfert.
 */
@RestController
@RequestMapping("/api")
public class ChargementTransfertResource {

    private final Logger log = LoggerFactory.getLogger(ChargementTransfertResource.class);

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
    RecepteurAlarmeService recepteurAlarmeService;
    PassiveExpiringMap<String, Compte> compteDebitMapDto = new PassiveExpiringMap<>(600000);
    PassiveExpiringMap<String, Operation> operationMapDto = new PassiveExpiringMap<>(600000);
    JobService jobService;

    public ChargementTransfertResource(JobService jobService, RecepteurAlarmeService recepteurAlarmeService, LigneChargementMapper ligneChargementMapper, MessageProducer messageProducer, CompteAutreRepository compteAutreRepository, CompteAutreMapper compteAutreMapper, MotifOperationService motifOperationService, TransformationChargement transformationChargement, @Qualifier("taskExecutor") TaskExecutor taskExecutor, ChargementTransfertRepository chargementTransfertRepository, UniteOrganisationService uniteOrganisationService, PointVenteService pointVenteService, MobileService mobileService, CompteRepository compteRepository, ClientService clientService, CompteService compteService, OperationRepository operationRepository, UserService userService, CanalService canalService, TypeServiceService typeServiceService, TransactionComptePtiService transactionComptePtiService, ChargementTransfertService chargementTransfertService, ReaderChargementService readerChargementService, LigneChargementTransfertService ligneChargementTransfertService, ApiService apiService, Read readService) {
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
        this.messageProducer = messageProducer;
        this.ligneChargementMapper = ligneChargementMapper;
        this.recepteurAlarmeService = recepteurAlarmeService;
        this.jobService = jobService;
    }

    /**
     * POST /chargement-transferts : Create a new chargementTransfert.
     *
     * @param chargementTransfert the chargementTransfert to create
     * @return the ResponseEntity with status 201 (Created) and with body the
     * new chargementTransfert, or with status 400 (Bad Request) if the
     * chargementTransfert has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/chargement-transferts")
    @Timed
    public ResponseEntity<ChargementTransfert> createChargementTransfert(@RequestBody ChargementTransfert chargementTransfert) throws URISyntaxException {
        log.debug("REST request to save ChargementTransfert : {}", chargementTransfert);
        if (chargementTransfert.getId() != null) {
            throw new BadRequestAlertException("A new chargementTransfert cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ChargementTransfert result = chargementTransfertService.save(chargementTransfert);
        return ResponseEntity.created(new URI("/api/chargement-transferts/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT /chargement-transferts : Updates an existing chargementTransfert.
     *
     * @param chargementTransfert the chargementTransfert to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated
     * chargementTransfert, or with status 400 (Bad Request) if the
     * chargementTransfert is not valid, or with status 500 (Internal Server
     * Error) if the chargementTransfert couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/chargement-transferts")
    @Timed
    public ResponseEntity<ChargementTransfert> updateChargementTransfert(@RequestBody ChargementTransfert chargementTransfert) throws URISyntaxException {
        log.debug("REST request to update ChargementTransfert : {}", chargementTransfert);
        if (chargementTransfert.getId() == null) {
            return createChargementTransfert(chargementTransfert);
        }
        ChargementTransfert result = chargementTransfertService.save(chargementTransfert);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, chargementTransfert.getId().toString()))
                .body(result);
    }

    /**
     * GET /chargement-transferts : get all the chargementTransferts.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of
     * chargementTransferts in body
     */
    @GetMapping("/chargement-transferts")
    @Timed
    public List<ChargementTransfert> getAllChargementTransferts() {
        log.debug("REST request to get all ChargementTransferts");
        return chargementTransfertService.findAllOrderByDate();
    }

    /**
     * GET /chargement-transferts/:id : get the "id" chargementTransfert.
     *
     * @param id the id of the chargementTransfert to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the
     * chargementTransfert, or with status 404 (Not Found)
     */
    @GetMapping("/chargement-transferts/{id}")
    @Timed
    public ResponseEntity<ChargementTransfert> getChargementTransfert(@PathVariable Long id) {
        log.debug("REST request to get ChargementTransfert : {}", id);
        ChargementTransfert chargementTransfert = chargementTransfertService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(chargementTransfert));
    }

    @GetMapping("/findChargementClient/{id}")
    @Timed
    public ResponseEntity<List<ChargementTransfert>> getChargementTransfertById(@PathVariable Long id, Pageable pageable) {
        log.debug("REST request to get ChargementTransfert : {}", id);
        Page<ChargementTransfert> page = clientService.findChargementPage(id, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/chargement-transfert");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * DELETE /chargement-transferts/:id : delete the "id" chargementTransfert.
     *
     * @param id the id of the chargementTransfert to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/chargement-transferts/{id}")
    @Timed
    public ResponseEntity<Void> deleteChargementTransfert(@PathVariable Long id) {
        log.debug("REST request to delete ChargementTransfert : {}", id);
        chargementTransfertService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    private void saveLigneChargement(CSVTransfert csvTransf) throws Exception {
        LigneChargementTransfert ligneChargementTransf = new LigneChargementTransfert();
        ligneChargementTransf.setMontant(csvTransf.getMontant());
        // ligneChargementCarte.setChargementCarte(chargement);
        LigneChargementTransfert lignC = ligneChargementTransfertService.save(ligneChargementTransf);
        /**
         * Mise à jour du rrn
         */
        // lignC.setRrn(getRrn(lignC));
        ligneChargementTransfertService.save(lignC);
    }

    @Transactional
    public Operation doTransfertEnMasse(MOperation mOperation, String codeExterne, String service) {
        Operation operation = new Operation();
        ZonedDateTime dateTime = ZonedDateTime.now();
        String description = "";
        User user = userService.findeByLogin(mOperation.getUserConnecte());
        log.debug("************Op********");
        log.debug("********************" + mOperation.getCodePv());
        TypeService typeService = typeServiceService.findByCodeService(service);
        MotifOperation motifOperation = motifOperationService.findByCode(mOperation.getMotif());
        operation.setMotifOperation(motifOperation);
        String codeUoAcq;
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
        if (service.equals(TypeServiceLibelle.TRANSFERT_MASSE_DEBIT.name())) {
            operation = mobileService.saveInformationPersonne(mOperation.getEmetteur(), Boolean.TRUE, operation);
            operation.compteDest(mobileService.getCompte(mOperation.getEmetteur().getCompte(), operation.getBeneficaire().getPersonne(), mOperation.getApplication(),Boolean.TRUE));
            description = "REGULATION EN MASSE REF: " + codeExterne;
        } else {
            operation = mobileService.saveInformationPersonne(mOperation.getEmetteur(), Boolean.FALSE, operation);
            operation.compteSrc(mobileService.getCompte(mOperation.getEmetteur().getCompte(), operation.getEmetteur().getPersonne(), mOperation.getApplication(),Boolean.FALSE));
            mOperation = mobileService.getMontantCalculFrais(mOperation, mOperation.getCodePv(), mOperation.getCanal(), mOperation.getCodeUoEm(), codeUoAcq, mOperation.getMontant());
            description = "PAIEMENT EN MASSE REF: " + codeExterne;

        }
        Canal canal = canalService.findByCodeCanal(mOperation.getCanal());

        operation.montantOp(mOperation.getMontant())
                .montantFrais(mOperation.getMontantFrais())
                .montantTaxe(mOperation.getMontantTaxe())
                .typeService(typeService)
                .statut(StatutOperation.VALIDE)
                .canal(canal)
                .description(description)
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

    @GetMapping("/chargement-transferts/liste-charge")
    @Timed
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
                mligneChargement.setStatutLigne(ligneChargementTransfert.getStatutLigne().name());
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

    @GetMapping("/chargement-transferts/valider/mobile")
    @Timed
    public ResponseEntity<Void> valideChargementMobile(@RequestParam Long id, @RequestParam String user, @RequestParam String canal) throws IOException, JAXBException, InterruptedException, ExecutionException, KettleException {
        return setValidateChargement(id, user, canal);
    }

    public void functionchargementLigne(Long idLigne, Long compteDebit, Long operationId, Long min, Long max) throws IOException, NoSuchAlgorithmException, JAXBException {
        MResponseOperation response = null;
        Compte cptDebit;

        if (compteDebitMapDto.containsKey(compteDebit.toString()) && compteDebitMapDto.get(compteDebit.toString()) != null) {
            cptDebit = compteDebitMapDto.get(compteDebit.toString());
        } else {
            cptDebit = compteService.findCompteOne(compteDebit);
            if (cptDebit != null) {
                compteDebitMapDto.put(compteDebit.toString(), cptDebit);
            }

        }

        Operation operation;
        if (operationMapDto.containsKey(operationId.toString()) && operationMapDto.get(operationId.toString()) != null) {
            operation = operationMapDto.get(operationId.toString());
        } else {
            operation = operationRepository.findByOperationId(operationId);
            if (operation != null) {
                operationMapDto.put(operationId.toString(), operation);
            }

        }

        LigneChargementTransfert ligneChargementTransfert = ligneChargementTransfertService.findOne(idLigne);
        //    LigneChargementTransfertDTO lgDto = new LigneChargementTransfertDTO();
        //    MLigneChargement mLigneChargement = ligneChargementMapper.toDto(ligneChargementTransfert);
        //     MChargement mChargement=new MChargement();
        //    mChargement.setCodePays(ligneChargementTransfert.getChargementTransfert().getCodePays());
        //   lgDto.setLigneChargementTransfert(mLigneChargement);
        //    lgDto.setOperId(operationId);
        //   lgDto.setCompteDebit(compteDebit);
        //  messageProducer.sendMessageLigneTransfertOperation(lgDto);
        StopWatch stopWatch = new StopWatch("Measure Code Execution");

        try {
            if (ligneChargementTransfert != null && !ligneChargementTransfert.getStatutLigne().equals(StatutLigneChargement.OC)) {
                ResultMessage resMessage;  //  (ResultMessage)apiService.convert(result.getBody(),ResultMessage.class);
                ResultMessage resMessageEnvoiMsg;
                String typeService = "";
                MOperation mOperation = new MOperation();
                DecimalFormat df = new DecimalFormat("###");
                // new BigDecimal(ligneChargementTransfert.getMontant())
                //   ligneChargementTransfert.setStatutLigne(StatutLigneChargement.IC);
                ligneChargementTransfert.setSoldeAvant(0D);
                Compte compteCredit = null;
                //il s'agit d'un transfert de carte à carte

                //               le débit est constaté et la carte désactivé
                BigDecimal montantApres = BigDecimal.valueOf(ligneChargementTransfert.getMontant());
                if (ligneChargementTransfert.getChargementTransfert().getTypeTransfert() != 2) {// cas des reglations en masse
                    montantApres = cptDebit.getCompteAutre().getSolde().subtract(BigDecimal.valueOf(Double.parseDouble(ligneChargementTransfert.getMontant().toString())));

                }
                //tra

                if (ligneChargementTransfert.getChargementTransfert().getTypeTransfert().equals(0)) { //transert vers compte
                    typeService = "TRANSFERT_MASSE";

                    if (ligneChargementTransfert.getCompteId() != null) {
                        compteCredit = compteService.findCompteOne(ligneChargementTransfert.getCompteId());
                    } else {
                        ligneChargementTransfert.setError("Compte introuvable");
                        throw new ChargementException(ligneChargementTransfert.getError());
                    }
                    mOperation.setBeneficiaire(getBeneficiaire(ligneChargementTransfert, compteCredit));

                    // compteCredit = mobileService.findComptePersonne(ligneChargementTransfert.getNumeroTelephone(), ligneChargementTransfert.getChargementTransfert().getCodePays());
                    if (compteCredit != null) {
                        if (compteCredit.getEtatCompte().equals(EtatCompte.FERME) || compteCredit.getStatutCompte().equals(StatutCompte.DESACTIVE)) {
                            ligneChargementTransfert.setError("Compte bloqué");
                            throw new ChargementException(ligneChargementTransfert.getError());
                        }
                    } else {
                        ligneChargementTransfert.setError("Compte introuvable");
                        throw new ChargementException(ligneChargementTransfert.getError());
                    }
                } else if (ligneChargementTransfert.getChargementTransfert().getTypeTransfert().equals(1)) {
                    typeService = "TRANSFERT_MASSE_CASH";
                } else if (ligneChargementTransfert.getChargementTransfert().getTypeTransfert().equals(2)) {
                    typeService = "TRANSFERT_MASSE_DEBIT";
                    // compteCredit = compteService.findCompteOne(ligneChargementTransfert.getCompteId());;
                    if (ligneChargementTransfert.getCompteId() != null) {
                        compteCredit = compteService.findCompteOne(ligneChargementTransfert.getCompteId());
                    } else {
                        ligneChargementTransfert.setError("Compte introuvable");
                        throw new ChargementException(ligneChargementTransfert.getError());
                    }
                    mOperation.setEmetteur(getBeneficiaire(ligneChargementTransfert, compteCredit));
                    mOperation.setBeneficiaire(getEmeteur(cptDebit));
                    if (compteCredit != null) {

                    } else {
                        ligneChargementTransfert.setError("Compte introuvable");
                        throw new ChargementException(ligneChargementTransfert.getError());
                    }
                }
                if (montantApres.doubleValue() >= 0) {
                    String narration = "";
                    mOperation.setMontant(BigDecimal.valueOf(ligneChargementTransfert.getMontant()));
                    mOperation.setMotif(ligneChargementTransfert.getChargementTransfert().getMotifOperation());
                    mOperation.setCodePv(operation.getPointVente().getCodePointVente());
                    log.debug("POINT VENTE:" + operation.getPointVente().getCodePointVente());
                    mOperation.setTypeService(typeService);
                    mOperation.setCanal("MOBILE");
                    mOperation.setDescription(typeService + " REF: " + ligneChargementTransfert.getChargementTransfert().getReference());
                    mOperation.setCodeUo(operation.getUoEmetteur().getCodeUO());
                    mOperation.setCodePays(operation.getUoAcquereur().getCodeUO());
                    if (ligneChargementTransfert.getNarration() != null) {
                        narration = ligneChargementTransfert.getNarration();
                    }
                    response = mobileService.doOperationChargement(mOperation, operation, narration, nombreLigneCharge, compteCredit, ligneChargementTransfert.getId());
                    log.debug("====XXXXXXXXXX=============TRANSACTION EXISTE=============");
                    if (response != null && response.getCode() != null && response.getCode().equals("0")) {
                        nombreLigneCharge = nombreLigneCharge + 1;
                        operation.setMontantFrais(BigDecimal.ZERO);
                        operation.setMontantTaxe(BigDecimal.ZERO);
                        if (response.getOperation() != null) {
                            log.debug("==============TRANSACTION EXISTE=============");
                            ligneChargementTransfert.setCodeOperation(response.getOperation().getCodeOperation());
                            Long compte = transactionComptePtiService.findExisteOperation(response.getOperation().getCodeOperation(), compteCredit.getCompteAutre().getId());
                            //   TransactionComptePti trans = transactionComptePtiService.findByOperationAndCompteAutre(response.getOperation().getCodeOperation(), compteCredit.getCompteAutre().getId());
                            if (compte > 0) {
                                ligneChargementTransfert.setSoldeApres(0D);
                                ligneChargementTransfert.setStatutLigne(StatutLigneChargement.OC);
                                log.debug("TRANSACTION EXISTE");
                                mobileService.envoiDesMessage(response.getOperation(), null);
                            } else {
                                ligneChargementTransfert.setError("ERREUR LORS DU CHARGEMENT");
                                ligneChargementTransfert.setStatutLigne(StatutLigneChargement.NC);
                                log.debug("ERROR=== DE CHARGEMENT");
                            }
                        }
                        log.debug("MAXX----" + max + "LIGNE TRANS" + ligneChargementTransfert.getId());

                    } else {
                        if (response != null && response.getCode() != null) {
                            ligneChargementTransfert.setError(response.getMessage());
                            ligneChargementTransfert.setStatutLigne(StatutLigneChargement.NC);
                        }
                    }

                } else {
                    nombreLigneErreur = nombreLigneErreur + 1;
                    nombreLigneCharge = nombreLigneCharge + 1;
                    ligneChargementTransfert.setError("Solde insuffisant");
                }

            } else {
                nombreLigneErreur = nombreLigneErreur + 1;
                ligneChargementTransfert.setError("Compte déjà crédité");
            }

            ligneChargementTransfertService.save(ligneChargementTransfert);

        } catch (Exception e) {
            ligneChargementTransfert.setError(e.getMessage());
            ligneChargementTransfertService.save(ligneChargementTransfert);
        }

        if (ligneChargementTransfert.getId() >= max) {
            ChargementTransfert chargement = chargementTransfertService.findOne(ligneChargementTransfert.getChargementTransfert().getId());
            chargement.setEtape(Etape.PROCEED);
            String sms = "LE PAIEMENT EN MASSE REFERENCE:" + ligneChargementTransfert.getChargementTransfert().getReference() + " est termine ";
            recepteurAlarmeService.sendAlarme(sms, "PAIEMENT_MASSE");
            chargementTransfertService.save(chargement);
            log.debug("****************************FIN DU TRANSFERT*******************************");
        }
    }

    @PostMapping("/chargement-transferts/lignechargement/mobile")
    @Timed
    public void chargementLigne(@RequestParam Long idLigne, @RequestParam Long compteDebit, @RequestParam Long operationId, @RequestParam Long min, @RequestParam Long max) throws IOException, NoSuchAlgorithmException, JAXBException {

        //    LigneChargementTransfertDTO lgDto = new LigneChargementTransfertDTO();
        //    MLigneChargement mLigneChargement = ligneChargementMapper.toDto(ligneChargementTransfert);
        //     MChargement mChargement=new MChargement();
        //    mChargement.setCodePays(ligneChargementTransfert.getChargementTransfert().getCodePays());
        //   lgDto.setLigneChargementTransfert(mLigneChargement);
        //    lgDto.setOperId(operationId);
        //   lgDto.setCompteDebit(compteDebit);
        //  messageProducer.sendMessageLigneTransfertOperation(lgDto);
        functionchargementLigne(idLigne, compteDebit, operationId, min, max);
    }

    @Async("taskExecutor")
    private void validchargement(String fseDef, ChargementTransfert chargement, Operation operation, Compte compteDebit) throws KettleException {
        if (fseDef != null) {
            ChargementTransfertMessageDTO lgDto = new ChargementTransfertMessageDTO(chargement.getId(), operation.getId(), compteDebit.getId());
            //messageProducer.sendMessageLigneTransfertOperation(lgDto);
            jobService.startValidation(chargement.getId(), operation.getId(), compteDebit.getId());
        }
        /*
        StopWatch stopWatch = new StopWatch("Measure Code Execution");
        stopWatch.start("1. Validation transfert");
        chargement.setEtape(Etape.INIT);
        Compte cpt = compteService.findIfCompteExisteWithClient(chargement.getCompteDebit(), chargement.getClient());
        if (cpt.getCompteAutre() != null) {
            chargement.setSoldeCompteAvant(compteDebit.getCompteAutre().getSolde());
        }
        chargement = chargementTransfertService.save(chargement);

        if (fseDef != null) {
            KettleEnvironment.init();
            TransMeta transMeta = transformationChargement.generateTrans(chargement.getId().toString(), operation.getId().toString(), compteDebit.getId().toString());
            transformationChargement.runTrans(transMeta);
        }
        cpt = compteService.findIfCompteExisteWithClient(chargement.getCompteDebit(), chargement.getClient());
        if (cpt.getCompteAutre() != null) {
            chargement.setSoldeCompteApres(compteDebit.getCompteAutre().getSolde());
        }
        chargement.setNombreLigneCharge(nombreLigneCharge);
        chargement.setNombreLigneErreur(nombreLigneErreur);
        chargement.setStatut("V");

        chargement.setEtape(Etape.PROCEED);
        chargementTransfertService.save(chargement);
        log.debug("fin du tranfert");
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());
         */
    }

    @GetMapping("/chargement-transferts/valider/{id}/{user}/{canal}")
    @Timed
    public ResponseEntity<Void> setValidateChargement(@PathVariable Long id, @PathVariable String user, @PathVariable String canal) throws IOException, JAXBException, InterruptedException, ExecutionException, KettleException {
        log.debug("debut du tranfert");

        MOperation mOperation = new MOperation();
        ChargementTransfert chargement = chargementTransfertService.findOne(id);
        userCanValideOperations(chargement.getCreatedBy(),user);
        if (chargement.getStatut().equals("I")) {

            return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, "Chargement encours de validation. ")).build();

        }
        /*
        if (chargement.getEtape()!=null && chargement.getEtape().equals(Etape.INIT)){
              return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "Chargement encours de validation.")).body(null);
        }*/
        Double mtTotal = chargement.getMontantTotal();
        Double mtCharge = chargement.getMontantEntrp();
        nombreLigneErreur = 0;
        nombreLigneCharge = 0;
        /*if (!mtCharge.equals(mtTotal)) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "Le montant à charger est inférieur au montant total ")).body(null);
        }*/

        mOperation.setMontant(BigDecimal.valueOf(chargement.getMontantTotal()));
        mOperation.setUserConnecte(user);
        mOperation.setMotif(chargement.getMotifOperation());
        mOperation.setMontantFrais(BigDecimal.ZERO);
        mOperation.setMontantTaxe(BigDecimal.ZERO);
        chargement.setStatut("I");
        String fseDef = mobileService.getFournisseurMonnaieDef(chargement.getCodePays());
        if (fseDef != null) {
            mOperation.setCodePv(mobileService.getCodePointVente(fseDef));
        }

        chargementTransfertService.save(chargement);
        Operation operation = null;
        ReportBatch rpBatch = new ReportBatch(chargement.getNombreLigneCharge(), chargement.getNombreLigneErreur());

        List<LigneChargementTransfert> listligneChargementCarte = null;
        MPersonne pers = new MPersonne();
        MInfos emeteur = new MInfos();
        boolean debitEstPossible = true;
        MCompte cpt = new MCompte();
        Compte compteDebit = compteService.findTopByValeurCompteAndEtiquette(chargement.getCompteDebit(), EtiquetteCompteCode.CPTECLIENT.toString());
        MTypeCompte typeCpte;
        String codeAgence;
        String codebanque;
        CompteAutre compteAutre = new CompteAutre();
        CompteAutreDTO compteAutreDTO = new CompteAutreDTO();
        if (compteDebit != null) {
            //    Integer nbreLigne = ligneChargementTransfertService.findNombre(id);
            String codeFse;
            if (compteDebit.getCompteAutre() != null) {

                //compteAutre = compteAutreRepository.findById(compteDebit.getCompteAutre().getId()).orElse(null);
                compteAutreDTO = compteAutreRepository.findByCompteAutreIdOptimise(compteDebit.getCompteAutre().getId());
                codeFse = compteAutreDTO.getCodeFse();
                typeCpte = MTypeCompte.AUTRE;
                codeAgence = "0";
                codebanque = "0";
            } else {
                codeAgence = compteDebit.getCompteBanque().getCodeAgence();
                codebanque = compteDebit.getCompteBanque().getCodeBanque();
                codeFse = compteDebit.getCompteBanque().getCodeFse();
                typeCpte = MTypeCompte.BANQUE;
            }

            if (chargement.getClient().getPersonne() != null) {
                pers.setNom(chargement.getClient().getPersonne().getPersonneMorale().getRaisonSociale());
                pers.setPrenom(chargement.getClient().getPersonne().getPersonneMorale().getRaisonSociale());
                pers.setTelephone(chargement.getClient().getPersonne().getTelephone());
                pers.setCodeTelephone(chargement.getClient().getPersonne().getCodePaysTelephone());
                emeteur.setTitulaire(pers);
                cpt.setCodeAgence(codeAgence);
                cpt.setTeneurCompte(codeFse);
                cpt.setCodeBanque(codebanque);
                cpt.setTypeCompte(typeCpte);
                if (compteDebit != null) {
                    cpt.setNumeroCompte(compteDebit.getValeurCompte());
                }

                cpt.setRib("");
                emeteur.setCompte(cpt);
                mOperation.setEmetteur(emeteur);
            }

            UniteOrganisation fseSrc = uniteOrganisationService.findFseByCode(codeFse);
            UniteOrganisation fsePays = uniteOrganisationService.findUoParentByType(fseSrc, TypeUO.PAYS);
            mOperation.setCodePays(fsePays.getCodeUO());
            mOperation.setCodeUo(fseSrc.getCodeUO());

            //  int nbrPage = nbreLigne.intValue() / 1000;
            //  for (int i = 0; i <= nbrPage; i++) {
            //   Pageable page = PageRequest.of(i, 1000, Sort.by(new Sort.Order(Sort.Direction.ASC, "id")));
            // listligneChargementCarte = ligneChargementTransfertService.findByChargementTransfertIdAndStatutLigneChargement(page, id);
            if (chargement.getTypeTransfert() == 0) {
                //* chargement vers compte 
                operation = doTransfertEnMasse(mOperation, chargement.getReference(), "TRANSFERT_MASSE");
            } else if (chargement.getTypeTransfert() == 1) {
                operation = doTransfertEnMasse(mOperation, chargement.getReference(), "TRANSFERT_MASSE_CASH");
            } else if (chargement.getTypeTransfert() == 2) {
                operation = doTransfertEnMasse(mOperation, chargement.getReference(), "TRANSFERT_MASSE_DEBIT");
            }
            // TODO: 14/09/2023 Cas du transfert en masse et cash

            if (chargement.getTypeTransfert() != 2) {
                if (compteDebit.getCompteAutre() != null) {
                    debitEstPossible = compteService.peutDebiterCompte(compteDebit, Util.getZeroIfNull(operation.getMontantFrais()).add(operation.getMontantOp()).add(Util.getZeroIfNull(operation.getMontantTaxe())));
                } else if (compteDebit.getCompteBanque() != null) {
                    Balance bal = apiService.getSolde(compteDebit.getValeurCompte(), compteDebit.getCompteBanque().getCodeAgence(), compteDebit.getCompteBanque().getCodeFse());
                    Double solde = bal.getSolde();
                    BigDecimal soldeEnBigDecimal = new BigDecimal(solde);
                    BigDecimal mtop = Util.getZeroIfNull(operation.getMontantFrais()).add(operation.getMontantOp()).add(Util.getZeroIfNull(operation.getMontantTaxe()));
                    if (Util.isBiggerOrEqualThan(soldeEnBigDecimal, mtop)) {
                        debitEstPossible = true;
                    } else {
                        debitEstPossible = false;
                    }
                }

            }

            if (!debitEstPossible) {
                throw new CustomParameterizedException("Solde insuffisant.");
            }
            validchargement(fseDef, chargement, operation, compteDebit);

            //    listligneChargementCarte = ligneChargementTransfertService.findByChargementTransfertIdAndStatutLigneChargement(id);
            //      MInfos emetteur= new MInfos();
            // List<LigneChargementCarte> listligneChargementCarte      = ligneChargementCarteService.decryptLigneChargement(listligneChargementCarteND);
        } else {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "Compte inexistant")).body(null);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, "Chargement encours de validation. Un mail vous sera envoyé en fin de traitement")).build();
    }

    private static class ChargementBatchListener implements JobListener {

        @Override
        public void beforeJobStart(JobParameters jp) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void afterJobEnd(JobReport jr) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

    private static class ChargementFilterResteZ implements RecordFilter {

        @Override
        public Record processRecord(Record record) {
            LigneChargementTransfert ligneChargementTransfert = (LigneChargementTransfert) record.getPayload();
            if (ligneChargementTransfert.getId() % 2 == 0) {
                System.out.println(ligneChargementTransfert.getId());
                return record;
            }

            return null;
        }

    }

    private static class ChargementFilterResteDZ implements RecordFilter {

        @Override
        public Record processRecord(Record record) {
            LigneChargementTransfert ligneChargementTransfert = (LigneChargementTransfert) record.getPayload();

            if (ligneChargementTransfert.getId() % 2 != 0) {
                System.out.println(ligneChargementTransfert.getId());
                return record;
            }
            return null;

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
                        MResponseOperation response = mobileService.doOperationChargement(mOperation, operation, narration, nombreLigneCharge, compteCredit, ligneChargementTransfert.getId());
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

    /**
     * chrgement par lot
     *
     * @param file
     * @param compteDebit
     * @param
     * @return
     * @throws IOException
     * @throws Exception
     */
    @PostMapping("/upload/chargement/fichier")

    public ResponseEntity<Void> singleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam String compteDebit, @RequestParam Integer typeTransfert, @RequestParam(required = false) String codePays, @RequestParam(required = false) String description, @RequestParam(required = false) String motifOperation, @RequestParam(required = false) String numeroTelephone, @RequestParam(required = false) Double montantEntrp, @RequestParam(required = false) boolean retraitGratuit) throws IOException, Exception {

        Map<String, byte[]> mapSign = new HashMap();
        List<CSVTransfert> listChargement = new ArrayList();
        List<MLigneChargement> mlistChargement = new ArrayList();
        Double montant = 0D;
        TransfertAssembler assembler;
        MchargementTransfert mchargement = new MchargementTransfert();
        try {

            String type = file.getContentType();

            System.out.println("----------" + type);
            System.out.println("----------");

            System.out.println("TYPE---------------" + type);
            if (type.equals("text/plain") || type.equals("text/csv") || type.equals("application/vnd.ms-excel")) {
                /**
                 * rechercher dans la table des chargements si le nom existe
                 * déjà si oui retourner les informations procéder à
                 * vérification de la signature du fichier
                 */
                String hash = readerChargementService.hashFile(file.getInputStream());
                Integer nbreHash = chargementTransfertService.findByHashFichier(hash);
                Integer nbreHashNomFile = chargementTransfertService.findByNomFichier(file.getOriginalFilename().toUpperCase());
                /**
                 * Enregistrement sur disque du fichier*
                 */
                File fichier = readerChargementService.convert(file);
                // BasicFileAttributes attr = Files.readAttributes( convFile.toPath(), BasicFileAttributes.class);
                //   log.debug(attr.creationTime().toString());
                if ((nbreHash > 0) && (nbreHashNomFile > 0)) {
                    log.debug("Fichier existe");
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "Le fichier a ete deja charge")).body(null);
                } else {

                    log.debug("debut traitement");
                    //      mapSign= readerChargementService.getSignature(file.getBytes());
                    ChargementTransfert chargementTransfert = new ChargementTransfert();
                    if (compteDebit != null) {
                        Compte compteD = compteService.findTopByValeurCompteAndEtiquette(compteDebit, EtiquetteCompteCode.CPTECLIENT.toString());
                        if (compteD != null) {
                            Client client = compteD.getClient();
                            chargementTransfert.setClient(client);
                        } else {
                            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "Le compte introuvable ")).body(null);
                        }

                    } else {
                        return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "Le compte introuvable")).body(null);
                    }

                    if (codePays != null) {
                        chargementTransfert.setCodePays(codePays);
                    }
                    if (motifOperation != null) {
                        chargementTransfert.setMotifOperation(motifOperation);
                    }
                    if (description != null) {
                        chargementTransfert.setDescription(description);
                    }
                    if (typeTransfert != null) {
                        chargementTransfert.setTypeTransfert(typeTransfert);
                    }
                    if (retraitGratuit) {
                        chargementTransfert.setRetraitGratuit(retraitGratuit);
                    } else {
                        chargementTransfert.setRetraitGratuit(false);
                    }
                    if (montantEntrp != null) {
                        chargementTransfert.setMontantEntrp(montantEntrp);
                    }

                    chargementTransfert.setCompteDebit(compteDebit);
                    chargementTransfert.setDateChargement(ZonedDateTime.now().toLocalDate());
                    //   chargementCarte.setKeyPublic(mapSign.get("publickey").toString());
                    chargementTransfert.setKeyPublic("");
                    chargementTransfert.setKeyPublic("");

                    //hargementCarte.setSignatureFichier(mapSign.get("signature").toString());
                    //    chargementCarte.setDateFichier(dateCreation);
                    assembler = new TransfertAssembler();
                    listChargement = readerChargementService.read(file.getInputStream(), assembler, CHARGEMENT_PATTERN);
                    chargementTransfert.setNombreLigne(listChargement.size());
                    chargementTransfert.setNombreLigneErreur(0);
                    chargementTransfert.setStatut("N");
                    chargementTransfert.setNomFichier(file.getOriginalFilename().toUpperCase());
                    chargementTransfert.setMontantTotal(0D);
                    chargementTransfert.setNombreLigneCharge(0);
                    chargementTransfert.setNombreLigneErreur(0);
                    chargementTransfert.setHashFichier(hash);
                    chargementTransfert.setDateFichier(ZonedDateTime.now());
                    //    Iterator it = listChargement.iterator();
                    // User user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin()).get();
                    ReportBatch rpBatchMontant = new ReportBatch(0D);
                    Job jobMontant = (Job) readService.getBatch(listChargement,
                            new ReadFileProcessorControle(chargementTransfert,
                                    rpBatchMontant)
                    );
                    System.out.println("==============" + rpBatchMontant.getMontant());
                    if (!rpBatchMontant.getMontant().equals(montantEntrp)) {
                        //return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "Echec de l'opération montant Total différent du montant saisi!")).body(null);
                        throw new CustomParameterizedException("Le montant à charger est inférieur au montant total");
                    }

                    ChargementTransfert result = chargementTransfertService.save(chargementTransfert);

                    if (result.getId() > 0) {
                        ReportBatch rpBatch = new ReportBatch(0D);
                        Job job = (Job) readService.getBatch(listChargement,
                                new ReadFileProcessor(mobileService, mlistChargement, result, ligneChargementTransfertService,
                                        rpBatch)
                        );

                        /**
                         * enregistrement du montant total*
                         */
                        result.setReference(ChargementTransfertResource.padLeftZeros(result.getId().toString(), 10));
                        result.setMontantTotal(rpBatch.getMontant());

                        result = chargementTransfertService.save(result);
                        mchargement.setDateChargement(result.getDateChargement());
                        mchargement.setIdChargement(result.getId());
                        mchargement.setNomFichier(result.getNomFichier());
                        mchargement.setReference(result.getReference());
                        mchargement.setNombreLigne(result.getNombreLigne());
                        mchargement.setStatut(result.getStatut());
                        mchargement.setmLigneChargement(mlistChargement);
                    }

                    return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, "OK")).build();
                }

            } else {
                return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "Format de fichier incorrect")).body(null);
            }

        } catch (IOException e) {
            log.debug(e.getMessage());
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", e.getMessage())).body(null);

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

    public static class ReadFileProcessor implements RecordProcessor {

        private Double montant = 0D;
        AuthTokenInfo tokeinfo;
        ReportBatch rpBatch;
        private final LigneChargementTransfertService ligneChargementTransfertService;
        ChargementTransfert chargement;
        List<MLigneChargement> mlistChargement = new ArrayList();
        MobileService mobileService;

        public ReadFileProcessor(MobileService mobileService, List<MLigneChargement> mlistChargement, ChargementTransfert chargement, LigneChargementTransfertService ligneChargementTransfertService, ReportBatch rpBatch) {
            this.rpBatch = rpBatch;
            this.montant = rpBatch.getMontant();

            this.ligneChargementTransfertService = ligneChargementTransfertService;
            //this.chargement = chargement;
            this.mlistChargement = mlistChargement;
            this.mobileService = mobileService;
            this.chargement = chargement;
        }

        @Override
        public Record processRecord(Record record) throws Exception {
            LigneChargementTransfert ligneChargementTransfert = new LigneChargementTransfert();

            CSVTransfert csvTransfert = (CSVTransfert) record.getPayload();

            MLigneChargement mLigneChargement = new MLigneChargement();
            ligneChargementTransfert.setChargementTransfert(this.chargement);
            ligneChargementTransfert.setMontant(csvTransfert.getMontant());
            ligneChargementTransfert.setStatutLigne(StatutLigneChargement.NC);
            ligneChargementTransfert.setNom(csvTransfert.getNom());
            ligneChargementTransfert.setPrenom(csvTransfert.getPrenom());
            ligneChargementTransfert.setNumeroCompte(csvTransfert.getNumeroCompte());
            ligneChargementTransfert.setNumeroTelephone(csvTransfert.getNumeroTelephone());
            ligneChargementTransfert.setNarration(csvTransfert.getNarration());
            try {
                if (ligneChargementTransfert.getNumeroTelephone() != null && ligneChargementTransfert.getNumeroTelephone().trim().length() > 0) {
                    Compte compte = mobileService.findComptePersonne(ligneChargementTransfert.getNumeroTelephone(), chargement.getCodePays());
                    if (compte != null) {
                        ligneChargementTransfert.setNumeroCompte(compte.getValeurCompte());
                        mLigneChargement.setNumeroCompte(compte.getValeurCompte());
                        ligneChargementTransfert.setCompteId(compte.getId());

                        /*Creation de l'opération*/
 /**/
                    } else {
                        ligneChargementTransfert.setError("Compte introuvable");
                        mLigneChargement.setError("Compte introuvable");
                    }
                } else {
                    ligneChargementTransfert.setError("Compte introuvable");
                    mLigneChargement.setError("format de compte incorrect");
                }

            } catch (Exception e) {
                ligneChargementTransfert.setError(e.getMessage());
                ligneChargementTransfertService.save(ligneChargementTransfert);
            }
            LigneChargementTransfert lignC = ligneChargementTransfertService.save(ligneChargementTransfert);
            //  lignC.setRrn(padLeftZeros("C" + lignC.getId().toString(), 10));
            /**
             * Mise à jour du rrn
             */
            //  lignC.setRrn(padLeftZeros("C" + lignC.getId().toString(), 10));
            // ligneChargementTransfertService.save(lignC);
            mLigneChargement.setMontant(lignC.getMontant());
            mLigneChargement.setNom(lignC.getNom());
            mLigneChargement.setPrenom(lignC.getPrenom());
            mLigneChargement.setError(lignC.getError());

            mLigneChargement.setNumeroTelephone(lignC.getNumeroTelephone());
            mlistChargement.add(mLigneChargement);
            //saveLigneChargement(csvCarte,result,TypeTransaction.CREDIT);              
            montant = csvTransfert.getMontant() + montant;
            rpBatch.setMontant(montant);
            return record;
        }

    }

    @Transactional
    public static class ReadFileProcessorControle implements RecordProcessor {

        private Double montant = 0D;
        ReportBatch rpBatch;
        ChargementTransfert chargement;

        public ReadFileProcessorControle(ChargementTransfert chargement, ReportBatch rpBatch) {
            this.rpBatch = rpBatch;
            this.montant = rpBatch.getMontant();
            this.chargement = chargement;
        }

        @Override
        public Record processRecord(Record record) throws Exception {
            CSVTransfert csvTransfert = (CSVTransfert) record.getPayload();
            montant = csvTransfert.getMontant() + montant;
            System.out.println(csvTransfert.getMontant());
            rpBatch.setMontant(montant);
            return record;
        }

    }

    private static String padLeftZeros(String str, int n) {
        return String.format("%1$" + n + "s", str).replace(' ', '0');
    }

    public static void saveLigneChargement(
            Runnable runnable,
            int delay,
            CSVTransfert csvCarte,
            ChargementTransfert chargement,
            MobileService mobileService,
            LigneChargementTransfertService ligneChargementTransfertService,
            ReportBatch rpBatch,
            Double montant
    ) {

        LigneChargementTransfert ligneChargementTransfert = new LigneChargementTransfert();
        ligneChargementTransfert.setMontant(csvCarte.getMontant());
        ligneChargementTransfert.setStatutLigne(StatutLigneChargement.NC);
        ligneChargementTransfert.setChargementTransfert(chargement);
        ligneChargementTransfert.setNom(csvCarte.getNom());
        ligneChargementTransfert.setPrenom(csvCarte.getPrenom());
        ligneChargementTransfert.setNumeroCompte(csvCarte.getNumeroCompte());
        ligneChargementTransfert.setNumeroTelephone(csvCarte.getNumeroTelephone());

        /*rechercher le compte de la personne*/
        Compte compteCredit = mobileService.findComptePersonne(ligneChargementTransfert.getNumeroTelephone(), ligneChargementTransfert.getChargementTransfert().getCodePays());
        if (compteCredit != null) {
            ligneChargementTransfert.setCompteId(compteCredit.getId());
        }
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                runnable.run();
                LigneChargementTransfert lignC = ligneChargementTransfertService.save(ligneChargementTransfert);

                lignC.setRrn(padLeftZeros("C" + lignC.getId().toString(), 10));
                ligneChargementTransfertService.save(lignC);
                final Double nouveauMontant = csvCarte.getMontant() + montant;
                rpBatch.setMontant(nouveauMontant);
            } catch (Exception e) {
                System.err.println(e);
            }
        }).start();
    }

    public void savegneChargement(LigneChargement lc) {

        //lignC.setRrn(padLeftZeros("C" + lignC.getId().toString(), 10));
        //ligneChargementTransfertService.save(lignC);
        //saveLigneChargement(csvCarte,result,TypeTransaction.CREDIT);
        //montant = csvCarte.getMontant() + montant;
        //rpBatch.setMontant(montant);
    }
    
    public void userCanValideOperations(String initiateur, String user) {
        if (user != null) {
            if (user.equals(initiateur)) {
                throw new CustomParameterizedException("Vous ne pouvez pas valider une opération que vous avez initié.");
            }
        }

    }

}
