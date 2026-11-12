package com.m2i.pti.partenaire.ressource;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Facture;
import com.m2i.pti.partenaire.domain.FactureOnea;
import com.m2i.pti.partenaire.service.FactureOneaService;
import com.m2i.pti.service.easyBatch.CSVFacture;
import com.m2i.pti.service.easyBatch.FactureAssembler;
import com.m2i.pti.service.easyBatch.Read;
import com.m2i.pti.service.easyBatch.ReaderFactureService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.easybatch.core.job.Job;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.record.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.*;

/**
 * REST controller for managing FactureOnea.
 */
@RestController
@RequestMapping("/api")
public class FactureOneaResource {

    private final Logger log = LoggerFactory.getLogger(FactureOneaResource.class);

    private static final String ENTITY_NAME = "factureOnea";

    private final FactureOneaService factureOneaService;

    private final ReaderFactureService readerFactureService;

    private final Read readService;

    public FactureOneaResource(FactureOneaService factureOneaService, ReaderFactureService readerFactureService, Read readService) {
        this.factureOneaService = factureOneaService;
        this.readerFactureService = readerFactureService;
        this.readService = readService;
    }

    /**
     * POST  /facture-oneas : Create a new factureOnea.
     *
     * @param factureOnea the factureOnea to create
     * @return the ResponseEntity with status 201 (Created) and with body the new factureOnea, or with status 400 (Bad Request) if the factureOnea has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/facture-oneas")
    @Timed
    public ResponseEntity<FactureOnea> createFactureOnea(@RequestBody FactureOnea factureOnea) throws URISyntaxException {
        log.debug("REST request to save FactureOnea : {}", factureOnea);
        if (factureOnea.getId() != null) {
            throw new BadRequestAlertException("A new factureOnea cannot already have an ID", ENTITY_NAME, "idexists");
        }
        FactureOnea result = factureOneaService.save(factureOnea);
        return ResponseEntity.created(new URI("/api/facture-oneas/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /facture-oneas : Updates an existing factureOnea.
     *
     * @param factureOnea the factureOnea to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated factureOnea,
     * or with status 400 (Bad Request) if the factureOnea is not valid,
     * or with status 500 (Internal Server Error) if the factureOnea couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/facture-oneas")
    @Timed
    public ResponseEntity<FactureOnea> updateFactureOnea(@RequestBody FactureOnea factureOnea) throws URISyntaxException {
        log.debug("REST request to update FactureOnea : {}", factureOnea);
        if (factureOnea.getId() == null) {
            return createFactureOnea(factureOnea);
        }
        FactureOnea result = factureOneaService.save(factureOnea);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, factureOnea.getId().toString()))
            .body(result);
    }

    /**
     * GET  /facture-oneas : get all the factureOneas.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of factureOneas in body
     */
    @GetMapping("/facture-oneas")
    @Timed
    public List<FactureOnea> getAllFactureOneas() {
        log.debug("REST request to get all FactureOneas");
        return factureOneaService.findAll();
        }

    /**
     * GET  /facture-oneas/:id : get the "id" factureOnea.
     *
     * @param id the id of the factureOnea to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the factureOnea, or with status 404 (Not Found)
     */
    @GetMapping("/facture-oneas/{id}")
    @Timed
    public ResponseEntity<FactureOnea> getFactureOnea(@PathVariable Long id) {
        log.debug("REST request to get FactureOnea : {}", id);
        FactureOnea factureOnea = factureOneaService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(factureOnea));
    }

    /**
     * DELETE  /facture-oneas/:id : delete the "id" factureOnea.
     *
     * @param id the id of the factureOnea to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/facture-oneas/{id}")
    @Timed
    public ResponseEntity<Void> deleteFactureOnea(@PathVariable Long id) {
        log.debug("REST request to delete FactureOnea : {}", id);
        factureOneaService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
    @PostMapping("upload/facture/fichier")
    public ResponseEntity<Void> singleFileUpload(@RequestParam("file") MultipartFile file) {
        Map<String, byte[]> mapSign = new HashMap();
        List<CSVFacture> ligneFacture= new ArrayList();
        List<CSVFacture> ligneFacture1= new ArrayList();
        List<FactureOnea> factureOneaList= new ArrayList<>();
        factureOneaList=factureOneaService.findAll();
        FactureAssembler assembler;
        try {
            File fichier = readerFactureService.convert(file);
            assembler = new FactureAssembler();
            Facture facture=new Facture();
            int i,j;
            int tail,min=0;
            ligneFacture = readerFactureService.read(file.getInputStream(),assembler,"");
            tail = Math.max(ligneFacture.size(), factureOneaList.size());
            //min = Math.min(ligneFacture.size(), factureOneaList.size());
                for (i = 0; i < ligneFacture.size(); i++) {
                        if ((factureOneaList.size()>i&&factureOneaList.get(i)!=null) &&(ligneFacture.get(i).getRefAbonne().contains(factureOneaList.get(i).getRefAbonne()) && ligneFacture.get(i).getTypePiece().contains(factureOneaList.get(i).getTypePiece()) && ligneFacture.get(i).getAnneFacture().contains(factureOneaList.get(i).getAnneeFacture()) && ligneFacture.get(i).getMoisFacture().contains(factureOneaList.get(i).getMoisFacture()))) {


                        } else {

                            ligneFacture1.add(ligneFacture.get(i));
                        }
                }


            Job job = (Job) readService.getBatch(ligneFacture1, new ReadFileProcessor(facture,factureOneaService ));
            return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, "OK")).build();

        } catch (IOException e) {
            log.debug(e.getMessage());
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", e.getMessage())).body(null);
        }
    }
    @Transactional
    public static class ReadFileProcessor implements RecordProcessor {


        private final FactureOneaService factureOneaService;
        Facture facture;

        public ReadFileProcessor( Facture facture, FactureOneaService factureOneaService) {

            this.factureOneaService = factureOneaService;
        }

        @Override
        public Record processRecord(Record record) throws Exception {
            CSVFacture csvFacture = (CSVFacture) record.getPayload();
            FactureOnea factureOnea = new FactureOnea();

            //factureOnea.setFacture(this.chargement);
            factureOnea.setRefAbonne(csvFacture.getRefAbonne());
            factureOnea.setTypePiece(csvFacture.getTypePiece());
            factureOnea.setNumeroPiece(csvFacture.getNumeroPiece());
            factureOnea.setSoldePiece(csvFacture.getSoldePiece());
            factureOnea.setDateLimitePiece(csvFacture.getDateLimitePiece());
            factureOnea.setDateAppliPenalite(csvFacture.getDateAppliPenalite());
            factureOnea.setDateLettrage(csvFacture.getDateLettrage());
            factureOnea.setCategorieComAbonne(csvFacture.getCategorieComAbonne());
            factureOnea.setNomAbonne(csvFacture.getNomAbonne());
            factureOnea.setAnneeFacture(csvFacture.getAnneFacture());
            factureOnea.setMoisFacture(csvFacture.getMoisFacture());
            factureOneaService.save(factureOnea);

            return record;
        }

    }
}
