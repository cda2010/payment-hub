package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.LigneChargementCompte;
import com.m2i.pti.repository.LigneChargementCompteRepository;
import com.m2i.pti.service.ChargementCompteService;
import com.m2i.pti.service.LigneChargementCompteService;
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
 * REST controller for managing LigneChargementCompte.
 */
@RestController
@RequestMapping("/api")
public class LigneChargementCompteResource {

    private final Logger log = LoggerFactory.getLogger(LigneChargementCompteResource.class);

    private static final String ENTITY_NAME = "ligneChargementCompte";

    private final LigneChargementCompteService ligneChargementCompteService;

    private final ChargementCompteService chargementCompteService;

    private final LigneChargementCompteRepository ligneChargementCompteRepository;

    public LigneChargementCompteResource(LigneChargementCompteService ligneChargementCompteService, ChargementCompteService chargementCompteService, LigneChargementCompteRepository ligneChargementCompteRepository) {
        this.ligneChargementCompteService = ligneChargementCompteService;
        this.chargementCompteService = chargementCompteService;
        this.ligneChargementCompteRepository = ligneChargementCompteRepository;
    }

    /**
     * POST  /ligne-chargement-comptes : Create a new ligneChargementCompte.
     *
     * @param ligneChargementCompte the ligneChargementCompte to create
     * @return the ResponseEntity with status 201 (Created) and with body the new ligneChargementCompte, or with status 400 (Bad Request) if the ligneChargementCompte has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/ligne-chargement-comptes")
    @Timed
    public ResponseEntity<LigneChargementCompte> createLigneChargementCompte(@RequestBody LigneChargementCompte ligneChargementCompte) throws URISyntaxException {
        log.debug("REST request to save LigneChargementCompte : {}", ligneChargementCompte);
        if (ligneChargementCompte.getId() != null) {
            throw new BadRequestAlertException("A new ligneChargementCompte cannot already have an ID", ENTITY_NAME, "idexists");
        }
        LigneChargementCompte result = ligneChargementCompteService.save(ligneChargementCompte);
        return ResponseEntity.created(new URI("/api/ligne-chargement-comptes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /ligne-chargement-comptes : Updates an existing ligneChargementCompte.
     *
     * @param ligneChargementCompte the ligneChargementCompte to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated ligneChargementCompte,
     * or with status 400 (Bad Request) if the ligneChargementCompte is not valid,
     * or with status 500 (Internal Server Error) if the ligneChargementCompte couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/ligne-chargement-comptes")
    @Timed
    public ResponseEntity<LigneChargementCompte> updateLigneChargementCompte(@RequestBody LigneChargementCompte ligneChargementCompte) throws URISyntaxException {
        log.debug("REST request to update LigneChargementCompte : {}", ligneChargementCompte);
        if (ligneChargementCompte.getId() == null) {
            return createLigneChargementCompte(ligneChargementCompte);
        }
        LigneChargementCompte result = ligneChargementCompteService.save(ligneChargementCompte);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ligneChargementCompte.getId().toString()))
            .body(result);
    }

    /**
     * GET  /ligne-chargement-comptes : get all the ligneChargementComptes.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of ligneChargementComptes in body
     */
    @GetMapping("/ligne-chargement-comptes")
    @Timed
    public List<LigneChargementCompte> getAllLigneChargementComptes() {
        log.debug("REST request to get all LigneChargementComptes");
        return ligneChargementCompteService.findAll();
        }

    /**
     * GET  /ligne-chargement-comptes/:id : get the "id" ligneChargementCompte.
     *
     * @param id the id of the ligneChargementCompte to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the ligneChargementCompte, or with status 404 (Not Found)
     */
    @GetMapping("/ligne-chargement-comptes/{id}")
    @Timed
    public ResponseEntity<LigneChargementCompte> getLigneChargementCompte(@PathVariable Long id) {
        log.debug("REST request to get LigneChargementCompte : {}", id);
        LigneChargementCompte ligneChargementCompte = ligneChargementCompteService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(ligneChargementCompte));
    }

    /**
     * DELETE  /ligne-chargement-comptes/:id : delete the "id" ligneChargementCompte.
     *
     * @param id the id of the ligneChargementCompte to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/ligne-chargement-comptes/{id}")
    @Timed
    public ResponseEntity<Void> deleteLigneChargementCompte(@PathVariable Long id) {
        log.debug("REST request to delete LigneChargementCompte : {}", id);
        ligneChargementCompteService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    @GetMapping("/clientDetail/{id}")
    @Timed
    public ResponseEntity<List<LigneChargementCompte>> queryDetail(@PathVariable Long id, Pageable pageable){
         Page<LigneChargementCompte> page=ligneChargementCompteRepository.queryDetailPage(id,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/chargement-compte");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/clientId/{id}")
    @Timed
    public Integer finById(@PathVariable Long id){
        Integer nbr= ligneChargementCompteService.findById(id);

        if (nbr > 0 )
        {
            ligneChargementCompteService.deleteAll(id);
            return 0;
        }
        else {
            chargementCompteService.delete(id);
            return 1;
        }


    }
}
