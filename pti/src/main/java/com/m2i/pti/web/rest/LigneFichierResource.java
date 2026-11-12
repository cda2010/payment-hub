package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.LigneChargement;
import com.m2i.pti.domain.LigneChargementFichier;
import com.m2i.pti.domain.enumeration.StatutLigneChargement;
import com.m2i.pti.service.fichier.LigneFichierService;
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

import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing LigneChargement.
 */
@RestController
@RequestMapping("/api/ligne-fichier")
public class LigneFichierResource {

    private final Logger log = LoggerFactory.getLogger(LigneFichierResource.class);

    private static final String ENTITY_NAME = "ligneChargement";
    private final LigneFichierService ligneFichierService;


    public LigneFichierResource(LigneFichierService ligneFichierService) {
        this.ligneFichierService = ligneFichierService;
    }

    /**
     * POST  /ligne-chargements : Create a new ligneChargement.
     *
     * @param ligneChargement the ligneChargement to create
     * @return the ResponseEntity with status 201 (Created) and with body the new ligneChargement, or with status 400 (Bad Request) if the ligneChargement has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */


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
    @GetMapping("/{id}")
    @Timed
    public ResponseEntity<LigneChargementFichier> getLigneChargement(@PathVariable Long id) {
        log.debug("REST request to get LigneChargement : {}", id);
        LigneChargementFichier ligneChargementFichier = ligneFichierService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(ligneChargementFichier));
    }

    /**
     * DELETE  /ligne-chargements/:id : delete the "id" ligneChargement.
     *
     * @param id the id of the ligneChargement to delete
     * @return the ResponseEntity with status 200 (OK)
     */

    @GetMapping("/requete")
    @Timed
    public ResponseEntity<List<LigneChargementFichier>> queryDetailByStatut(@RequestParam("id") Long id, @RequestParam("statut") String statut, Pageable pageable){
        log.debug("---------------------------------------------------{}", statut);
        log.debug("---------------------------------------------------{}", id);
        StatutLigneChargement statutLigne = StatutLigneChargement.NC;
        Page<LigneChargementFichier> page=null;
        if (statut.equals("ACCT")){
            statutLigne = StatutLigneChargement.OC;
            page = ligneFichierService.queryDetailPageByStatut( id,statutLigne,pageable);
        }else {
            StatutLigneChargement statutLigne1= StatutLigneChargement.IC;
            page = ligneFichierService.queryDetailPageByStatut1(id,statutLigne,statutLigne1,pageable);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/chargement-fichier");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    @GetMapping("/organisation")
    @Timed
    public ResponseEntity<List<LigneChargementFichier>> queryDetailByOrganisation(@RequestParam(value = "organisationId",required = false) Long organisationId, Pageable pageable){
        log.debug("---------------{}", organisationId);
        Page<LigneChargementFichier> page = ligneFichierService.queryDetailPageByOrganisation( organisationId,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/chargement-fichier");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
