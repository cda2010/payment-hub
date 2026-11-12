package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.LigneChargementTransfert;
import com.m2i.pti.domain.enumeration.StatutLigneChargement;
import com.m2i.pti.repository.LigneChargementTransfertRepository;
import com.m2i.pti.service.ChargementTransfertService;
import com.m2i.pti.service.LigneChargementTransfertService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing LigneChargementTransfert.
 */
@RestController
@RequestMapping("/api")
public class LigneChargementTransfertResource {

    private final Logger log = LoggerFactory.getLogger(LigneChargementTransfertResource.class);

    private static final String ENTITY_NAME = "ligneChargementTransfert";

    private final LigneChargementTransfertService ligneChargementTransfertService;

    private final LigneChargementTransfertRepository ligneChargementTransfertRepository;

    private final ChargementTransfertService chargementTransfertService;

    public LigneChargementTransfertResource(LigneChargementTransfertService ligneChargementTransfertService, LigneChargementTransfertRepository ligneChargementTransfertRepository, ChargementTransfertService chargementTransfertService) {
        this.ligneChargementTransfertService = ligneChargementTransfertService;
        this.ligneChargementTransfertRepository = ligneChargementTransfertRepository;
        this.chargementTransfertService = chargementTransfertService;
    }

    /**
     * POST  /ligne-chargement-transferts : Create a new ligneChargementTransfert.
     *
     * @param ligneChargementTransfert the ligneChargementTransfert to create
     * @return the ResponseEntity with status 201 (Created) and with body the new ligneChargementTransfert, or with status 400 (Bad Request) if the ligneChargementTransfert has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/ligne-chargement-transferts")
    @Timed
    public ResponseEntity<LigneChargementTransfert> createLigneChargementTransfert(@RequestBody LigneChargementTransfert ligneChargementTransfert) throws URISyntaxException {
        log.debug("REST request to save LigneChargementTransfert : {}", ligneChargementTransfert);
        if (ligneChargementTransfert.getId() != null) {
            throw new BadRequestAlertException("A new ligneChargementTransfert cannot already have an ID", ENTITY_NAME, "idexists");
        }
        LigneChargementTransfert result = ligneChargementTransfertService.save(ligneChargementTransfert);
        return ResponseEntity.created(new URI("/api/ligne-chargement-transferts/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /ligne-chargement-transferts : Updates an existing ligneChargementTransfert.
     *
     * @param ligneChargementTransfert the ligneChargementTransfert to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated ligneChargementTransfert,
     * or with status 400 (Bad Request) if the ligneChargementTransfert is not valid,
     * or with status 500 (Internal Server Error) if the ligneChargementTransfert couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/ligne-chargement-transferts")
    @Timed
    public ResponseEntity<LigneChargementTransfert> updateLigneChargementTransfert(@RequestBody LigneChargementTransfert ligneChargementTransfert) throws URISyntaxException {
        log.debug("REST request to update LigneChargementTransfert : {}", ligneChargementTransfert);
        if (ligneChargementTransfert.getId() == null) {
            return createLigneChargementTransfert(ligneChargementTransfert);
        }
        LigneChargementTransfert result = ligneChargementTransfertService.save(ligneChargementTransfert);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ligneChargementTransfert.getId().toString()))
            .body(result);
    }

    /**
     * GET  /ligne-chargement-transferts : get all the ligneChargementTransferts.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of ligneChargementTransferts in body
     */
    @GetMapping("/ligne-chargement-transferts")
    @Timed
    public List<LigneChargementTransfert> getAllLigneChargementTransferts() {
        log.debug("REST request to get all LigneChargementTransferts");
        return ligneChargementTransfertService.findAll();
        }

    /**
     * GET  /ligne-chargement-transferts/:id : get the "id" ligneChargementTransfert.
     *
     * @param id the id of the ligneChargementTransfert to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the ligneChargementTransfert, or with status 404 (Not Found)
     */
    @GetMapping("/ligne-chargement-transferts/{id}")
    @Timed
    public ResponseEntity<LigneChargementTransfert> getLigneChargementTransfert(@PathVariable Long id) {
        log.debug("REST request to get LigneChargementTransfert : {}", id);
        LigneChargementTransfert ligneChargementTransfert = ligneChargementTransfertService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(ligneChargementTransfert));
    }

    /**
     * DELETE  /ligne-chargement-transferts/:id : delete the "id" ligneChargementTransfert.
     *
     * @param id the id of the ligneChargementTransfert to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/ligne-chargement-transferts/{id}")
    @Timed
    public ResponseEntity<Void> deleteLigneChargementTransfert(@PathVariable Long id) {
        log.debug("REST request to delete LigneChargementTransfert : {}", id);
        ligneChargementTransfertService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @GetMapping("/requete/{id}")
    @Timed
    public ResponseEntity<List<LigneChargementTransfert>> queryDetail(@PathVariable Long id, Pageable pageable){
        Page<LigneChargementTransfert> page= ligneChargementTransfertRepository.queryDetailPage(id,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/chargement-transfert");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/requete/{id}/{statut}")
    @Timed
    public ResponseEntity<List<LigneChargementTransfert>> queryDetailByStatut(@PathVariable Long id, @PathVariable String statut, Pageable pageable){
        log.debug("---------------------------------------------------{}", statut);
        log.debug("---------------------------------------------------{}", id);
        StatutLigneChargement statutLigne = StatutLigneChargement.NC;
        Page<LigneChargementTransfert> page=null;
        if (statut.equals("ACCT")){
            statutLigne = StatutLigneChargement.OC;
            page = ligneChargementTransfertRepository.queryDetailPageByStatut( id,statutLigne,pageable);
        }else {
            StatutLigneChargement statutLigne1= StatutLigneChargement.IC;
            page = ligneChargementTransfertRepository.queryDetailPageByStatut1(id,statutLigne,statutLigne1,pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/chargement-transfert");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/ligne/{id}")
    @Timed
    public Integer finById(@PathVariable Long id){
        Integer nbr= ligneChargementTransfertService.findById(id);

        if (nbr > 0 )
        {
            ligneChargementTransfertService.deleteAll(id);
            return 0;
        }
        else {
            chargementTransfertService.delete(id);
            return 1;
        }


    }
}
