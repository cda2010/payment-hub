package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.StructureOrganisationFacturier;

import com.m2i.pti.repository.StructureOrganisationFacturierRepository;
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
 * REST controller for managing StructureOrganisationFacturier.
 */
@RestController
@RequestMapping("/api")
public class StructureOrganisationFacturierResource {

    private final Logger log = LoggerFactory.getLogger(StructureOrganisationFacturierResource.class);

    private static final String ENTITY_NAME = "structureOrganisationFacturier";

    private final StructureOrganisationFacturierRepository structureOrganisationFacturierRepository;

    public StructureOrganisationFacturierResource(StructureOrganisationFacturierRepository structureOrganisationFacturierRepository) {
        this.structureOrganisationFacturierRepository = structureOrganisationFacturierRepository;
    }

    /**
     * POST  /structure-organisation-facturiers : Create a new structureOrganisationFacturier.
     *
     * @param structureOrganisationFacturier the structureOrganisationFacturier to create
     * @return the ResponseEntity with status 201 (Created) and with body the new structureOrganisationFacturier, or with status 400 (Bad Request) if the structureOrganisationFacturier has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/structure-organisation-facturiers")
    @Timed
    public ResponseEntity<StructureOrganisationFacturier> createStructureOrganisationFacturier(@RequestBody StructureOrganisationFacturier structureOrganisationFacturier) throws URISyntaxException {
        log.debug("REST request to save StructureOrganisationFacturier : {}", structureOrganisationFacturier);
        if (structureOrganisationFacturier.getId() != null) {
            throw new BadRequestAlertException("A new structureOrganisationFacturier cannot already have an ID", ENTITY_NAME, "idexists");
        }
        StructureOrganisationFacturier result = structureOrganisationFacturierRepository.save(structureOrganisationFacturier);
        return ResponseEntity.created(new URI("/api/structure-organisation-facturiers/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /structure-organisation-facturiers : Updates an existing structureOrganisationFacturier.
     *
     * @param structureOrganisationFacturier the structureOrganisationFacturier to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated structureOrganisationFacturier,
     * or with status 400 (Bad Request) if the structureOrganisationFacturier is not valid,
     * or with status 500 (Internal Server Error) if the structureOrganisationFacturier couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/structure-organisation-facturiers")
    @Timed
    public ResponseEntity<StructureOrganisationFacturier> updateStructureOrganisationFacturier(@RequestBody StructureOrganisationFacturier structureOrganisationFacturier) throws URISyntaxException {
        log.debug("REST request to update StructureOrganisationFacturier : {}", structureOrganisationFacturier);
        if (structureOrganisationFacturier.getId() == null) {
            return createStructureOrganisationFacturier(structureOrganisationFacturier);
        }
        StructureOrganisationFacturier result = structureOrganisationFacturierRepository.save(structureOrganisationFacturier);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, structureOrganisationFacturier.getId().toString()))
            .body(result);
    }

    /**
     * GET  /structure-organisation-facturiers : get all the structureOrganisationFacturiers.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of structureOrganisationFacturiers in body
     */
    @GetMapping("/structure-organisation-facturiers")
    @Timed
    public List<StructureOrganisationFacturier> getAllStructureOrganisationFacturiers() {
        log.debug("REST request to get all StructureOrganisationFacturiers");
        return structureOrganisationFacturierRepository.findAll();
        }

    /**
     * GET  /structure-organisation-facturiers/:id : get the "id" structureOrganisationFacturier.
     *
     * @param id the id of the structureOrganisationFacturier to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the structureOrganisationFacturier, or with status 404 (Not Found)
     */
    @GetMapping("/structure-organisation-facturiers/{id}")
    @Timed
    public ResponseEntity<StructureOrganisationFacturier> getStructureOrganisationFacturier(@PathVariable Long id) {
        log.debug("REST request to get StructureOrganisationFacturier : {}", id);
        StructureOrganisationFacturier structureOrganisationFacturier = structureOrganisationFacturierRepository.findById(id).orElse(null);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(structureOrganisationFacturier));
    }

    /**
     * DELETE  /structure-organisation-facturiers/:id : delete the "id" structureOrganisationFacturier.
     *
     * @param id the id of the structureOrganisationFacturier to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/structure-organisation-facturiers/{id}")
    @Timed
    public ResponseEntity<Void> deleteStructureOrganisationFacturier(@PathVariable Long id) {
        log.debug("REST request to delete StructureOrganisationFacturier : {}", id);
        structureOrganisationFacturierRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
