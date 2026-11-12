package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.outils.restclient.AuthTokenInfo;
import com.m2i.pti.domain.ChargementMessage;
import com.m2i.pti.domain.LigneMessage;
import com.m2i.pti.domain.enumeration.StatutLigneChargement;
import com.m2i.pti.repository.ChargementMessageRepository;
import com.m2i.pti.service.ApiService;
import com.m2i.pti.service.ChargementMessageService;
import com.m2i.pti.service.LigneMessageService;
import com.m2i.pti.service.easyBatch.CsvMessage;
import com.m2i.pti.service.easyBatch.MessageAssembleur;
import com.m2i.pti.service.easyBatch.Read;
import com.m2i.pti.service.easyBatch.ReaderMessageService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.easybatch.core.job.Job;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.record.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing ChargementMessage.
 */
@RestController
@RequestMapping("/api")
public class ChargementMessageResource {

    private final Logger log = LoggerFactory.getLogger(ChargementMessageResource.class);

    private static final String ENTITY_NAME = "chargementMessage";

    private final ChargementMessageService chargementMessageService;

    private final ChargementMessageRepository chargementMessageRepository;

    ReaderMessageService readerMessageService;

    private static final String CHARGEMENT_PATTERN = "^(?:[0-9]+;?[0-9]+;?(.+)@(.+)+;[a-zA-Z]+$)";

    private final Read readService;

    private final LigneMessageService ligneMessageService;

    final ApiService apiService;


    public ChargementMessageResource(ChargementMessageService chargementMessageService, ChargementMessageRepository chargementMessageRepository, ReaderMessageService readerMessageService, Read readService, LigneMessageService ligneMessageService, ApiService apiService) {
        this.chargementMessageService = chargementMessageService;
        this.chargementMessageRepository = chargementMessageRepository;
        this.readerMessageService = readerMessageService;
        this.readService = readService;
        this.ligneMessageService = ligneMessageService;
        this.apiService = apiService;
    }

    /**
     * POST  /chargement-messages : Create a new chargementMessage.
     *
     * @param chargementMessage the chargementMessage to create
     * @return the ResponseEntity with status 201 (Created) and with body the new chargementMessage, or with status 400 (Bad Request) if the chargementMessage has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/chargement-messages")
    @Timed
    public ResponseEntity<ChargementMessage> createChargementMessage(@RequestBody ChargementMessage chargementMessage) throws URISyntaxException {
        log.debug("REST request to save ChargementMessage : {}", chargementMessage);
        if (chargementMessage.getId() != null) {
            throw new BadRequestAlertException("A new chargementMessage cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ChargementMessage result = chargementMessageService.save(chargementMessage);
        return ResponseEntity.created(new URI("/api/chargement-messages/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /chargement-messages : Updates an existing chargementMessage.
     *
     * @param chargementMessage the chargementMessage to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated chargementMessage,
     * or with status 400 (Bad Request) if the chargementMessage is not valid,
     * or with status 500 (Internal Server Error) if the chargementMessage couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/chargement-messages")
    @Timed
    public ResponseEntity<ChargementMessage> updateChargementMessage(@RequestBody ChargementMessage chargementMessage) throws URISyntaxException {
        log.debug("REST request to update ChargementMessage : {}", chargementMessage);
        if (chargementMessage.getId() == null) {
            return createChargementMessage(chargementMessage);
        }
        ChargementMessage result = chargementMessageService.save(chargementMessage);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, chargementMessage.getId().toString()))
            .body(result);
    }

    /**
     * GET  /chargement-messages : get all the chargementMessages.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of chargementMessages in body
     */
    @GetMapping("/chargement-messages")
    @Timed
    public ResponseEntity<List<ChargementMessage>> getAllChargementMessages(Pageable pageable) {
        log.debug("REST request to get a page of ChargementMessages");
        Page<ChargementMessage> page=chargementMessageRepository.findAllMessagePage(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/chargement-message");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /chargement-messages/:id : get the "id" chargementMessage.
     *
     * @param id the id of the chargementMessage to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the chargementMessage, or with status 404 (Not Found)
     */
    @GetMapping("/chargement-messages/{id}")
    @Timed
    public ResponseEntity<ChargementMessage> getChargementMessage(@PathVariable Long id) {
        log.debug("REST request to get ChargementMessage : {}", id);
        ChargementMessage chargementMessage = chargementMessageService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(chargementMessage));
    }

    /**
     * DELETE  /chargement-messages/:id : delete the "id" chargementMessage.
     *
     * @param id the id of the chargementMessage to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/chargement-messages/{id}")
    @Timed
    public ResponseEntity<Void> deleteChargementMessage(@PathVariable Long id) {
        log.debug("REST request to delete ChargementMessage : {}", id);
        chargementMessageService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @PostMapping("/upload/chargement_message/fichier")
    public ResponseEntity<Void> singleFileUpload(@RequestParam("file") MultipartFile file) throws IOException {
        MessageAssembleur messageAssembleur;
        List<CsvMessage> listChargement= new ArrayList<>();
        List<LigneMessage> ligneMessages= new ArrayList<>();
        try{
            String type = file.getContentType();
            if (type.equals("text/plain") || type.equals("text/csv") || type.equals("application/vnd.ms-excel")) {
                /**
                 * rechercher dans la table des chargements si le nom existe
                 * déjà si oui retourner les informations procéder à
                 * vérification de la signature du fichier
                 */
                String hash = readerMessageService.hashFile(file.getInputStream());
                Integer nbreHash = chargementMessageService.findByHashFichier(hash);
                Integer nbreHashNomFile = chargementMessageService.findByNomFichier(file.getOriginalFilename().toUpperCase());
                /*
                Enregistrement sur le disque
                */
                File fichier = readerMessageService.convert(file);
                if ((nbreHash > 0) && (nbreHashNomFile > 0)) {
                    log.debug("Fichier existe");
                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "Le fichier a ete deja charge")).body(null);
                } else  {
                    log.debug("debut traitement");
                    ChargementMessage chargementMessage=new ChargementMessage();
                    chargementMessage.setDateChargement(ZonedDateTime.now().toLocalDate());
                    chargementMessage.setDateFichier(ZonedDateTime.now());
                    messageAssembleur= new MessageAssembleur();
                    listChargement = readerMessageService.read(file.getInputStream(), messageAssembleur,CHARGEMENT_PATTERN);
                    chargementMessage.setNomFichier(file.getOriginalFilename().toUpperCase());
                    chargementMessage.setHashFichier(hash);
                    chargementMessage.setStatut("N");
                    chargementMessage.setNombreLigne(listChargement.size());
                    chargementMessage.setNombreLigneCharge(listChargement.size());
                    chargementMessage.setNombreLigneErreur(0);
                    ChargementMessage result = chargementMessageService.save(chargementMessage);
                    if(result.getId() >0 ) {
                        ReportBatch rpBatch = new ReportBatch();
                        Job job = (Job) readService.getBatch(listChargement, new ReadFileProcessor(result, ligneMessageService));
                        result.setReference(ChargementMessageResource.padLeftZeros(result.getId().toString(), 10));
                        chargementMessageService.save(result);
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
        public ReportBatch(Integer nombreLigneCharge, Integer nombreLigneErreur) {
            this.nombreLigneCharge = nombreLigneCharge;
            this.nombreLigneErreur = nombreLigneErreur;
        }

        public ReportBatch() {
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


    }
    @Transactional
    public static class ReadFileProcessor implements RecordProcessor {
        AuthTokenInfo tokeinfo;
        ReportBatch rpBatch;
        private final LigneMessageService ligneMessageService;
        ChargementMessage chargement;


        public ReadFileProcessor(ChargementMessage chargement, LigneMessageService ligneMessageService) {


            this.ligneMessageService = ligneMessageService;
            //this.chargement = chargement;
            this.chargement = new ChargementMessage();
            this.chargement = chargement;
        }

        @Override
        public Record processRecord(Record record) throws Exception {
            CsvMessage csvMessage = (CsvMessage) record.getPayload();
            LigneMessage ligneChargementMessage = new LigneMessage();

            ligneChargementMessage.setChargementMessage(this.chargement);
            ligneChargementMessage.setStatutLigne(StatutLigneChargement.NC);
            ligneChargementMessage.setCodeIndicatif(csvMessage.getCodeIndicatif());
            if(csvMessage.getNumeroTelephone()!=null){
                ligneChargementMessage.setNumeroTelephone(csvMessage.getNumeroTelephone());
            }
            if(csvMessage.getEmail()!=null){
                ligneChargementMessage.setEmail(csvMessage.getEmail());
            }


            ligneChargementMessage.setMessage(csvMessage.getMessage());
            LigneMessage lignC = ligneMessageService.save(ligneChargementMessage);
            lignC.setRrn(padLeftZeros("C" + lignC.getId().toString(), 10));
            /**
             * Mise à jour du rrn
             */
           // lignC.setRrn(padLeftZeros("C" + lignC.getId().toString(), 10));
            ligneMessageService.save(lignC);

            return record;
        }
    }
    private static String padLeftZeros(String str, int n) {
        return String.format("%1$" + n + "s", str).replace(' ', '0');
    }

    @GetMapping("/valider-chargement")
    public ResponseEntity<Void> validerChargement(@RequestParam Long chargementId) {
        log.debug("Début du traitement");
        ChargementMessage chargementMessage = chargementMessageService.findOne(chargementId);
        chargementMessage.setStatut("I");
        chargementMessageService.save(chargementMessage);
        List<LigneMessage> ligneMessages = null;
        ChargementMessageResource.ReportBatch reportBatch = new ChargementMessageResource.ReportBatch(chargementMessage.getNombreLigneCharge(),chargementMessage.getNombreLigneErreur());
        ligneMessages = ligneMessageService.getLigneMessage(chargementId);

        Job job = (Job) readService.getBatch(ligneMessages, new ChargementMessageResource.MessageProcessor(apiService,ligneMessageService,reportBatch));

        chargementMessage.setNombreLigneCharge(reportBatch.getNombreLigneCharge());
        chargementMessage.setNombreLigneErreur(reportBatch.getNombreLigneErreur());
        chargementMessage.setStatut("V");
        chargementMessageService.save(chargementMessage);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, "Validation effectuée")).build();

    }

    private static class MessageProcessor implements RecordProcessor {
        LigneMessageService ligneMessageService;
        ApiService apiService;
        Boolean error;
        Integer nombreLigneCharge = 0;
        Integer nombreLigneErreur = 0;
        ChargementMessageResource.ReportBatch rpBatch;
        public MessageProcessor(ApiService apiService,LigneMessageService ligneMessageService,ChargementMessageResource.ReportBatch rpBatch) {
            nombreLigneCharge = rpBatch.getNombreLigneCharge();
            nombreLigneErreur = rpBatch.getNombreLigneErreur();
            this.apiService = apiService;
            this.rpBatch = rpBatch;
            this.ligneMessageService = ligneMessageService;
        }

        @Override
        public Record processRecord(Record record) throws Exception {
            LigneMessage ligneMessage = (LigneMessage) record.getPayload();

            if(ligneMessage.getStatutLigne().equals(StatutLigneChargement.NC)) {
                ligneMessage.setStatutLigne(StatutLigneChargement.IC);
            }
            error = false;
            ligneMessageService.save(ligneMessage);
            try {
                if(ligneMessage.getStatutLigne().equals(StatutLigneChargement.IC)) {
                    if(ligneMessage.getNumeroTelephone()!=null){
                        apiService.sendMessage("+"+ligneMessage.getCodeIndicatif()+ligneMessage.getNumeroTelephone(),ligneMessage.getMessage(),ligneMessage.getCodeIndicatif());
                    }else {
                        ligneMessage.setError("Message sur le téléphone non envoyé, téléphone non renseigné");
                    }
                    if(ligneMessage.getEmail()!=null){
                        apiService.sendMail(ligneMessage.getEmail(),ligneMessage.getMessage());
                    } else {
                        ligneMessage.setError("Message sur le email non envoyé, email non renseigné");
                    }
                } else {
                    error= true;
                    ligneMessage.setError("Message non envoyé");
                }

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            if (error) {
                nombreLigneErreur = nombreLigneErreur + 1;
            }

            rpBatch.setNombreLigneCharge(nombreLigneCharge);
            rpBatch.setNombreLigneErreur(nombreLigneErreur);
            ligneMessageService.save(ligneMessage);
            return record;
        }
    }
}
