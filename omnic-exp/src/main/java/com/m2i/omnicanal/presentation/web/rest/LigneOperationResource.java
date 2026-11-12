package com.m2i.omnicanal.presentation.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.model.LigneOperation;
import com.m2i.omnicanal.presentation.domain.LigneOper;

import com.m2i.omnicanal.presentation.service.LigneOperationService;
import com.m2i.omnicanal.presentation.web.rest.util.HeaderUtil;
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
 * REST controller for managing LigneOperation.
 */
@RestController
@RequestMapping("/api")
public class LigneOperationResource {

    private final Logger log = LoggerFactory.getLogger(LigneOperationResource.class);

    private static final String ENTITY_NAME = "ligneOperation";
        
    private final LigneOperationService ligneOperationService;

    public LigneOperationResource(LigneOperationService ligneOperationService) {
        this.ligneOperationService = ligneOperationService;
    }

    /**
     * POST  /ligne-operations : Create a new ligneOperation.
     *
     * @param ligneOperation the ligneOperation to create
     * @return the ResponseEntity with status 201 (Created) and with body the new ligneOperation, or with status 400 (Bad Request) if the ligneOperation has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/ligne-operations")
    @Timed
    public ResponseEntity<LigneOperation> createLigneOperation(@RequestBody LigneOper ligneOperation) throws URISyntaxException {
        log.debug("REST request to save LigneOperation : {}", ligneOperation);
        if (ligneOperation.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new ligneOperation cannot already have an ID")).body(null);
        }
        LigneOper result = ligneOperationService.save(ligneOperation);
        return ResponseEntity.created(new URI("/api/ligne-operations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /ligne-operations : Updates an existing ligneOperation.
     *
     * @param ligneOperation the ligneOperation to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated ligneOperation,
     * or with status 400 (Bad Request) if the ligneOperation is not valid,
     * or with status 500 (Internal Server Error) if the ligneOperation couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/ligne-operations")
    @Timed
    public ResponseEntity<LigneOperation> updateLigneOperation(@RequestBody LigneOper ligneOperation) throws URISyntaxException {
        log.debug("REST request to update LigneOperation : {}", ligneOperation);
        if (ligneOperation.getId() == null) {
            return createLigneOperation(ligneOperation);
        }
        LigneOper result = ligneOperationService.save(ligneOperation);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ligneOperation.getId().toString()))
            .body(result);
    }

    /**
     * GET  /ligne-operations : get all the ligneOperations.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of ligneOperations in body
     */
    @GetMapping("/ligne-operations")
    @Timed
    public List<LigneOper> getAllLigneOperations() {
        log.debug("REST request to get all LigneOperations");
        return ligneOperationService.findAll();
    }

    /**
     * GET  /ligne-operations/:id : get the "id" ligneOperation.
     *
     * @param id the id of the ligneOperation to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the ligneOperation, or with status 404 (Not Found)
     */
    @GetMapping("/ligne-operations/{id}")
    @Timed
    public ResponseEntity<LigneOper> getLigneOperation(@PathVariable Long id) {
        log.debug("REST request to get LigneOperation : {}", id);
        LigneOper ligneOperation = ligneOperationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(ligneOperation));
    }

    /**
     * DELETE  /ligne-operations/:id : delete the "id" ligneOperation.
     *
     * @param id the id of the ligneOperation to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/ligne-operations/{id}")
    @Timed
    public ResponseEntity<Void> deleteLigneOperation(@PathVariable Long id) {
        log.debug("REST request to delete LigneOperation : {}", id);
        ligneOperationService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
