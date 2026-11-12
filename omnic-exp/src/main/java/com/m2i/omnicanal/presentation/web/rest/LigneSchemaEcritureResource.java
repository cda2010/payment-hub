package com.m2i.omnicanal.presentation.web.rest;

import com.codahale.metrics.annotation.Timed;

import com.m2i.omnicanal.presentation.domain.LigneSchemaEcrit;
import com.m2i.omnicanal.presentation.service.LigneSchemaEcritureService;
import com.m2i.omnicanal.presentation.web.rest.util.HeaderUtil;

import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing LigneSchemaEcriture.
 */
@RestController
@RequestMapping("/api")
public class LigneSchemaEcritureResource {

    private final Logger log = LoggerFactory.getLogger(LigneSchemaEcritureResource.class);

    private static final String ENTITY_NAME = "ligneSchemaEcriture";
        
    private final LigneSchemaEcritureService ligneSchemaEcritureService;

    public LigneSchemaEcritureResource(LigneSchemaEcritureService ligneSchemaEcritureService) {
        this.ligneSchemaEcritureService = ligneSchemaEcritureService;
    }

    /**
     * POST  /ligne-schema-ecritures : Create a new ligneSchemaEcriture.
     *
     * @param ligneSchemaEcriture the ligneSchemaEcriture to create
     * @return the ResponseEntity with status 201 (Created) and with body the new ligneSchemaEcriture, or with status 400 (Bad Request) if the ligneSchemaEcriture has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/ligne-schema-ecritures")
    @Timed
    public ResponseEntity<LigneSchemaEcrit> createLigneSchemaEcriture(@Valid @RequestBody LigneSchemaEcrit ligneSchemaEcriture) throws URISyntaxException {
        log.debug("REST request to save LigneSchemaEcriture : {}", ligneSchemaEcriture);
        if (ligneSchemaEcriture.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new ligneSchemaEcriture cannot already have an ID")).body(null);
        }
        LigneSchemaEcrit result = ligneSchemaEcritureService.save(ligneSchemaEcriture);
        return ResponseEntity.created(new URI("/api/ligne-schema-ecritures/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /ligne-schema-ecritures : Updates an existing ligneSchemaEcriture.
     *
     * @param ligneSchemaEcriture the ligneSchemaEcriture to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated ligneSchemaEcriture,
     * or with status 400 (Bad Request) if the ligneSchemaEcriture is not valid,
     * or with status 500 (Internal Server Error) if the ligneSchemaEcriture couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/ligne-schema-ecritures")
    @Timed
    public ResponseEntity<LigneSchemaEcrit> updateLigneSchemaEcriture(@Valid @RequestBody LigneSchemaEcrit ligneSchemaEcriture) throws URISyntaxException {
        log.debug("REST request to update LigneSchemaEcriture : {}", ligneSchemaEcriture);
        if (ligneSchemaEcriture.getId() == null) {
            return createLigneSchemaEcriture(ligneSchemaEcriture);
        }
        LigneSchemaEcrit result = ligneSchemaEcritureService.save(ligneSchemaEcriture);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ligneSchemaEcriture.getId().toString()))
            .body(result);
    }

    /**
     * GET  /ligne-schema-ecritures : get all the ligneSchemaEcritures.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of ligneSchemaEcritures in body
     */
    @GetMapping("/ligne-schema-ecritures")
    @Timed
    public List<LigneSchemaEcrit> getAllLigneSchemaEcritures() {
        log.debug("REST request to get all LigneSchemaEcritures");
        return ligneSchemaEcritureService.findAll();
    }
 /**
     * GET  /ligne-schema-ecritures : get all the ligneSchemaEcritures.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of ligneSchemaEcritures in body
     */
    @GetMapping("/ligne-schema-ecritures/schema/{idschema}")
    @Timed
    public List<LigneSchemaEcrit> getLigneSchemaEcrituresBySchema(@PathVariable Long idschema) {
        log.debug("REST request to get all LigneSchemaEcritures");
        return ligneSchemaEcritureService.findBySchema(idschema);
    }
    /**
     * GET  /ligne-schema-ecritures/:id : get the "id" ligneSchemaEcriture.
     *
     * @param id the id of the ligneSchemaEcriture to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the ligneSchemaEcriture, or with status 404 (Not Found)
     */
    @GetMapping("/ligne-schema-ecritures/{id}")
    @Timed
    public ResponseEntity<LigneSchemaEcrit> getLigneSchemaEcriture(@PathVariable Long id) {
        log.debug("REST request to get LigneSchemaEcriture : {}", id);
        LigneSchemaEcrit ligneSchemaEcriture = ligneSchemaEcritureService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(ligneSchemaEcriture));
    }

    /**
     * DELETE  /ligne-schema-ecritures/:id : delete the "id" ligneSchemaEcriture.
     *
     * @param id the id of the ligneSchemaEcriture to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/ligne-schema-ecritures/{id}")
    @Timed
    public ResponseEntity<Void> deleteLigneSchemaEcriture(@PathVariable Long id) {
        log.debug("REST request to delete LigneSchemaEcriture : {}", id);
        ligneSchemaEcritureService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
