package com.m2i.omnicanal.application.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.application.domain.UniteOrg;

import com.m2i.omnicanal.application.service.UniteOrganisationService;
import com.m2i.omnicanal.application.web.rest.util.HeaderUtil;
import com.m2i.omnicanal.application.web.rest.util.PaginationUtil;
import io.swagger.annotations.ApiParam;
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
 * REST controller for managing UniteOrganisation.
 */
@RestController
@RequestMapping("/api")
public class UniteOrganisationResource {

    private final Logger log = LoggerFactory.getLogger(UniteOrganisationResource.class);

    private static final String ENTITY_NAME = "uniteOrganisation";
        
    private final UniteOrganisationService uniteOrganisationService;

    public UniteOrganisationResource(UniteOrganisationService uniteOrganisationService) {
        this.uniteOrganisationService = uniteOrganisationService;
    }

    /**
     * POST  /unite-organisations : Create a new uniteOrganisation.
     *
     * @param uniteOrganisation the uniteOrganisation to create
     * @return the ResponseEntity with status 201 (Created) and with body the new uniteOrganisation, or with status 400 (Bad Request) if the uniteOrganisation has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/unite-organisations")
    @Timed
    public ResponseEntity<UniteOrg> createUniteOrganisation(@RequestBody UniteOrg uniteOrganisation) throws URISyntaxException {
        log.debug("REST request to save UniteOrganisation : {}", uniteOrganisation);
        if (uniteOrganisation.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new uniteOrganisation cannot already have an ID")).body(null);
        }
        UniteOrg result = uniteOrganisationService.save(uniteOrganisation);
        return ResponseEntity.created(new URI("/api/unite-organisations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /unite-organisations : Updates an existing uniteOrganisation.
     *
     * @param uniteOrganisation the uniteOrganisation to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated uniteOrganisation,
     * or with status 400 (Bad Request) if the uniteOrganisation is not valid,
     * or with status 500 (Internal Server Error) if the uniteOrganisation couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/unite-organisations")
    @Timed
    public ResponseEntity<UniteOrg> updateUniteOrganisation(@RequestBody UniteOrg uniteOrganisation) throws URISyntaxException {
        log.debug("REST request to update UniteOrganisation : {}", uniteOrganisation);
        if (uniteOrganisation.getId() == null) {
            return createUniteOrganisation(uniteOrganisation);
        }
        UniteOrg result = uniteOrganisationService.save(uniteOrganisation);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, uniteOrganisation.getId().toString()))
            .body(result);
    }

    /**
     * GET  /unite-organisations : get all the uniteOrganisations.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of uniteOrganisations in body
     */
    @GetMapping("/unite-organisations")
    @Timed
    public ResponseEntity<List<UniteOrg>> getAllUniteOrganisations(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of UniteOrganisations");
        Page<UniteOrg> page = uniteOrganisationService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/unite-organisations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /unite-organisations/:id : get the "id" uniteOrganisation.
     *
     * @param id the id of the uniteOrganisation to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the uniteOrganisation, or with status 404 (Not Found)
     */
    @GetMapping("/unite-organisations/{id}")
    @Timed
    public ResponseEntity<UniteOrg> getUniteOrganisation(@PathVariable Long id) {
        log.debug("REST request to get UniteOrganisation : {}", id);
        UniteOrg uniteOrganisation = uniteOrganisationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(uniteOrganisation));
    }

    /**
     * DELETE  /unite-organisations/:id : delete the "id" uniteOrganisation.
     *
     * @param id the id of the uniteOrganisation to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/unite-organisations/{id}")
    @Timed
    public ResponseEntity<Void> deleteUniteOrganisation(@PathVariable Long id) {
        log.debug("REST request to delete UniteOrganisation : {}", id);
        uniteOrganisationService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
