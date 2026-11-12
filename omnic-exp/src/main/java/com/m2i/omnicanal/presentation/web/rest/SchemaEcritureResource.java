package com.m2i.omnicanal.presentation.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.model.ParametreUtilisateur;
import com.m2i.omnicanal.presentation.domain.ParametreUser;
import com.m2i.omnicanal.presentation.domain.SchemaEcrit;
import com.m2i.omnicanal.presentation.service.SchemaEcritureService;
import com.m2i.omnicanal.presentation.service.SchemaMotif;
import com.m2i.omnicanal.presentation.web.rest.util.HeaderUtil;

import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing SchemaEcriture.
 */
@RestController
@RequestMapping("/api")
public class SchemaEcritureResource {

    private final Logger log = LoggerFactory.getLogger(SchemaEcritureResource.class);

    private static final String ENTITY_NAME = "schemaEcriture";
        
    private final SchemaEcritureService schemaEcritureService;

    public SchemaEcritureResource(SchemaEcritureService schemaEcritureService) {
        this.schemaEcritureService = schemaEcritureService;
    }

    /**
     * POST  /schema-ecritures : Create a new schemaEcriture.
     *
     * @param schemaEcriture the schemaEcriture to create
     * @return the ResponseEntity with status 201 (Created) and with body the new schemaEcriture, or with status 400 (Bad Request) if the schemaEcriture has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/schema-ecritures")
    @Timed
    public ResponseEntity<SchemaEcrit> createSchemaEcriture(@RequestBody SchemaEcrit schemaEcriture) throws URISyntaxException {
        log.debug("REST request to save SchemaEcriture : {}", schemaEcriture);
        if (schemaEcriture.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new schemaEcriture cannot already have an ID")).body(null);
        }
        if (!SchemaMotif.connu(schemaEcriture.getPatternCode())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "motif", "le motif du schema est inconnu")).body(null);
        }
        SchemaEcrit result = schemaEcritureService.save(schemaEcriture);
        return ResponseEntity.created(new URI("/api/schema-ecritures/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /schema-ecritures : Updates an existing schemaEcriture.
     *
     * @param schemaEcriture the schemaEcriture to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated schemaEcriture,
     * or with status 400 (Bad Request) if the schemaEcriture is not valid,
     * or with status 500 (Internal Server Error) if the schemaEcriture couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/schema-ecritures")
    @Timed
    public ResponseEntity<SchemaEcrit> updateSchemaEcriture(@RequestBody SchemaEcrit schemaEcriture) throws URISyntaxException {
        log.debug("REST request to update SchemaEcriture : {}", schemaEcriture);
        if (schemaEcriture.getId() == null) {
            return createSchemaEcriture(schemaEcriture);
        }
        if (!SchemaMotif.connu(schemaEcriture.getPatternCode())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "motif", "le motif du schema est inconnu")).body(null);
        }
        SchemaEcrit result = schemaEcritureService.save(schemaEcriture);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, schemaEcriture.getId().toString()))
            .body(result);
    }

    /**
     * GET  /schema-ecritures : get all the schemaEcritures.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of schemaEcritures in body
     */
    @GetMapping("/schema-ecritures")
    @Timed
    public List<SchemaEcrit> getAllSchemaEcritures() {
        log.debug("REST request to get all SchemaEcritures");
        return schemaEcritureService.findAll();
    }

    /**
     * GET  /schema-ecritures/:id : get the "id" schemaEcriture.
     *
     * @param id the id of the schemaEcriture to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the schemaEcriture, or with status 404 (Not Found)
     */
    @GetMapping("/schema-ecritures/{id}")
    @Timed
    public ResponseEntity<SchemaEcrit> getSchemaEcriture(@PathVariable Long id) {
        log.debug("REST request to get SchemaEcriture : {}", id);
        SchemaEcrit schemaEcriture = schemaEcritureService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(schemaEcriture));
    }

    /**
     * DELETE  /schema-ecritures/:id : delete the "id" schemaEcriture.
     *
     * @param id the id of the schemaEcriture to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/schema-ecritures/{id}")
    @Timed
    public ResponseEntity<Void> deleteSchemaEcriture(@PathVariable Long id) {
        log.debug("REST request to delete SchemaEcriture : {}", id);
        schemaEcritureService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
