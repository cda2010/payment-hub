package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.LigneChargement;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing LigneChargement.
 */
@RestController
@RequestMapping("/api")
public class LigneChargementResource {

    private final Logger log = LoggerFactory.getLogger(LigneChargementResource.class);

    private static final String ENTITY_NAME = "ligneChargement";


    public LigneChargementResource() {
    }

    /**
     * POST  /ligne-chargements : Create a new ligneChargement.
     *
     * @param ligneChargement the ligneChargement to create
     * @return the ResponseEntity with status 201 (Created) and with body the new ligneChargement, or with status 400 (Bad Request) if the ligneChargement has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
//    @PostMapping("/ligne-chargements")
//    @Timed
//    public ResponseEntity<LigneChargement> createLigneChargement(@RequestBody LigneChargement ligneChargement) throws URISyntaxException {
//        log.debug("REST request to save LigneChargement : {}", ligneChargement);
//        if (ligneChargement.getId() != null) {
//            throw new BadRequestAlertException("A new ligneChargement cannot already have an ID", ENTITY_NAME, "idexists");
//        }
//        LigneChargement result = ligneChargementService.save(ligneChargement);
//        return ResponseEntity.created(new URI("/api/ligne-chargements/" + result.getId()))
//            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
//            .body(result);
//    }

    /**
     * PUT  /ligne-chargements : Updates an existing ligneChargement.
     *
     * @param ligneChargement the ligneChargement to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated ligneChargement,
     * or with status 400 (Bad Request) if the ligneChargement is not valid,
     * or with status 500 (Internal Server Error) if the ligneChargement couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
//    @PutMapping("/ligne-chargements")
//    @Timed
//    public ResponseEntity<LigneChargement> updateLigneChargement(@RequestBody LigneChargement ligneChargement) throws URISyntaxException {
//        log.debug("REST request to update LigneChargement : {}", ligneChargement);
//        if (ligneChargement.getId() == null) {
//            return createLigneChargement(ligneChargement);
//        }
//        LigneChargement result = ligneChargementService.save(ligneChargement);
//        return ResponseEntity.ok()
//            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ligneChargement.getId().toString()))
//            .body(result);
//    }

    /**
     * GET  /ligne-chargements : get all the ligneChargements.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of ligneChargements in body
     */
//    @GetMapping("/ligne-chargements")
//    @Timed
//    public List<LigneChargement> getAllLigneChargements() {
//        log.debug("REST request to get all LigneChargements");
//        return ligneChargementService.findAll();
//        }

    /**
     * GET  /ligne-chargements/:id : get the "id" ligneChargement.
     *
     * @param id the id of the ligneChargement to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the ligneChargement, or with status 404 (Not Found)
     */
//    @GetMapping("/ligne-chargements/{id}")
//    @Timed
//    public ResponseEntity<LigneChargement> getLigneChargement(@PathVariable Long id) {
//        log.debug("REST request to get LigneChargement : {}", id);
//        LigneChargement ligneChargement = ligneChargementService.findOne(id);
//        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(ligneChargement));
//    }

    /**
     * DELETE  /ligne-chargements/:id : delete the "id" ligneChargement.
     *
     * @param id the id of the ligneChargement to delete
     * @return the ResponseEntity with status 200 (OK)
     */
//    @DeleteMapping("/ligne-chargements/{id}")
//    @Timed
//    public ResponseEntity<Void> deleteLigneChargement(@PathVariable Long id) {
//        log.debug("REST request to delete LigneChargement : {}", id);
//        ligneChargementService.delete(id);
//        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
//    }

//    @GetMapping("/requete/{id}")
//    @Timed
//    public LigneChargement queryDetail(@PathVariable Long id) {
//        log.debug("REST request to get all LigneChargements");
//        return ligneChargementService.queryDetail(id);
//    }
}
