package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.ParametreExport;
import com.m2i.pti.service.PrametreExportService;
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
import com.m2i.pti.repository.PrametreExportRepository;
import com.m2i.pti.service.dto.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing PrametreExport.
 */
@RestController
@RequestMapping("/api")
public class PrametreExportResource {

    private final Logger log = LoggerFactory.getLogger(PrametreExportResource.class);

    private static final String ENTITY_NAME = "prametreExport";

    private final PrametreExportService prametreExportService;
    private final PrametreExportRepository prametreExportRepository;

    public PrametreExportResource(PrametreExportService prametreExportService, PrametreExportRepository prametreExportRepository) {
        this.prametreExportService = prametreExportService;
        this.prametreExportRepository = prametreExportRepository;
    }

    /**
     * POST  /prametre-exports : Create a new prametreExport.
     *
     * @param parametreExport the prametreExport to create
     * @return the ResponseEntity with status 201 (Created) and with body the new prametreExport, or with status 400 (Bad Request) if the prametreExport has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/prametre-exports")
    @Timed
    public ResponseEntity<ParametreExport> createPrametreExport(@RequestBody ParametreExport parametreExport) throws URISyntaxException {
        log.debug("----------------------------------------------------------------REST request to save PrametreExport : {}", parametreExport);
        if (parametreExport.getId() != null) {
            throw new BadRequestAlertException("A new prametreExport cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ParametreExport result = prametreExportService.save(parametreExport);
        return ResponseEntity.created(new URI("/api/prametre-exports/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /prametre-exports : Updates an existing prametreExport.
     *
     * @param parametreExport the prametreExport to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated prametreExport,
     * or with status 400 (Bad Request) if the prametreExport is not valid,
     * or with status 500 (Internal Server Error) if the prametreExport couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/prametre-exports")
    @Timed
    public ResponseEntity<ParametreExport> updatePrametreExport(@RequestBody ParametreExport parametreExport) throws URISyntaxException {
        log.debug("------------------------------------------------------------REST request to update PrametreExport : {}", parametreExport);
        if (parametreExport.getId() == null) {
            System.out.println("====================Create =========================================");
            return createPrametreExport(parametreExport);
        }else{
            System.out.println("====================Modification =========================================");
            ParametreExport result = prametreExportService.save(parametreExport);
            return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, parametreExport.getId().toString())).body(result);
        }
    }

    /**
     * GET  /prametre-exports : get all the prametreExports.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of prametreExports in body
     */
    @GetMapping("/prametre-exports")
    @Timed
    public ResponseEntity<List<ParametreExport>> getAllPrametreExports(Pageable pageable) {
        log.debug("REST request to get a page of PrametreExports");
        Page<ParametreExport> page = prametreExportService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/prametre-exports");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /prametre-exports/:id : get the "id" prametreExport.
     *
     * @param id the id of the prametreExport to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the prametreExport, or with status 404 (Not Found)
     */
    @GetMapping("/prametre-exports/{id}")
    @Timed
    public ResponseEntity<ParametreExport> getPrametreExport(@PathVariable Long id) {
        log.debug("REST request to get PrametreExport : {}", id);
        ParametreExport parametreExport = prametreExportService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(parametreExport));
    }

    /**
     * DELETE  /prametre-exports/:id : delete the "id" prametreExport.
     *
     * @param id the id of the prametreExport to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/prametre-exports/{id}")
    @Timed
    public ResponseEntity<Void> deletePrametreExport(@PathVariable Long id) {
        log.debug("REST request to delete PrametreExport : {}", id);
        prametreExportService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }


    @GetMapping("/prametre-exports/of-exporttable/{id}")
    @Timed
    public ResponseEntity<List<ParametreExport>> getAllParametreExportByExportId(@PathVariable Long id, Pageable pageable) {
        Page<ParametreExport> page = prametreExportRepository.findParamExportByExportId(id, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/prametre-exports");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
