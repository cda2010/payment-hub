package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.RepartitionOpService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.RepartitionOpDTO;
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

import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing RepartitionOp.
 */
@RestController
@RequestMapping("/api")
public class RepartitionOpResource {

    private final Logger log = LoggerFactory.getLogger(RepartitionOpResource.class);

    private static final String ENTITY_NAME = "repartitionOp";

    private final RepartitionOpService repartitionOpService;

    public RepartitionOpResource(RepartitionOpService repartitionOpService) {
        this.repartitionOpService = repartitionOpService;
    }

    /**
     * POST  /repartition-ops : Create a new repartitionOp.
     *
     * @param repartitionOpDTO the repartitionOpDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new repartitionOpDTO, or with status 400 (Bad Request) if the repartitionOp has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/repartition-ops")
    @Timed
    public ResponseEntity<RepartitionOpDTO> createRepartitionOp(@Valid @RequestBody RepartitionOpDTO repartitionOpDTO) throws URISyntaxException {
        log.debug("REST request to save RepartitionOp : {}", repartitionOpDTO);
        if (repartitionOpDTO.getId() != null) {
            throw new BadRequestAlertException("A new repartitionOp cannot already have an ID", ENTITY_NAME, "idexists");
        }
        RepartitionOpDTO result = repartitionOpService.save(repartitionOpDTO);
        return ResponseEntity.created(new URI("/api/repartition-ops/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /repartition-ops : Updates an existing repartitionOp.
     *
     * @param repartitionOpDTO the repartitionOpDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated repartitionOpDTO,
     * or with status 400 (Bad Request) if the repartitionOpDTO is not valid,
     * or with status 500 (Internal Server Error) if the repartitionOpDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/repartition-ops")
    @Timed
    public ResponseEntity<RepartitionOpDTO> updateRepartitionOp(@Valid @RequestBody RepartitionOpDTO repartitionOpDTO) throws URISyntaxException {
        log.debug("REST request to update RepartitionOp : {}", repartitionOpDTO);
        if (repartitionOpDTO.getId() == null) {
            return createRepartitionOp(repartitionOpDTO);
        }
        RepartitionOpDTO result = repartitionOpService.save(repartitionOpDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, repartitionOpDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /repartition-ops : get all the repartitionOps.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of repartitionOps in body
     */
    @GetMapping("/repartition-ops")
    @Timed
    public ResponseEntity<List<RepartitionOpDTO>> getAllRepartitionOps(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of RepartitionOps");
        Page<RepartitionOpDTO> page = repartitionOpService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/repartition-ops");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /repartition-ops/:id : get the "id" repartitionOp.
     *
     * @param id the id of the repartitionOpDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the repartitionOpDTO, or with status 404 (Not Found)
     */
    @GetMapping("/repartition-ops/{id}")
    @Timed
    public ResponseEntity<RepartitionOpDTO> getRepartitionOp(@PathVariable Long id) {
        log.debug("REST request to get RepartitionOp : {}", id);
        RepartitionOpDTO repartitionOpDTO = repartitionOpService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(repartitionOpDTO));
    }

    /**
     * DELETE  /repartition-ops/:id : delete the "id" repartitionOp.
     *
     * @param id the id of the repartitionOpDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/repartition-ops/{id}")
    @Timed
    public ResponseEntity<Void> deleteRepartitionOp(@PathVariable Long id) {
        log.debug("REST request to delete RepartitionOp : {}", id);
        repartitionOpService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
