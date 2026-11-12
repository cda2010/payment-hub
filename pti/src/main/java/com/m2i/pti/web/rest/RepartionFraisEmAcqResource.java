package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.RepartionFraisEmAcqService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.RepartionFraisEmAcqDTO;
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
 * REST controller for managing RepartionFraisEmAcq.
 */
@RestController
@RequestMapping("/api")
public class RepartionFraisEmAcqResource {

    private final Logger log = LoggerFactory.getLogger(RepartionFraisEmAcqResource.class);

    private static final String ENTITY_NAME = "repartionFraisEmAcq";

    private final RepartionFraisEmAcqService repartionFraisEmAcqService;

    public RepartionFraisEmAcqResource(RepartionFraisEmAcqService repartionFraisEmAcqService) {
        this.repartionFraisEmAcqService = repartionFraisEmAcqService;
    }

    /**
     * POST  /repartion-frais-em-acqs : Create a new repartionFraisEmAcq.
     *
     * @param repartionFraisEmAcqDTO the repartionFraisEmAcqDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new repartionFraisEmAcqDTO, or with status 400 (Bad Request) if the repartionFraisEmAcq has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/repartion-frais-em-acqs")
    @Timed
    public ResponseEntity<RepartionFraisEmAcqDTO> createRepartionFraisEmAcq(@RequestBody RepartionFraisEmAcqDTO repartionFraisEmAcqDTO) throws URISyntaxException {
        log.debug("REST request to save RepartionFraisEmAcq : {}", repartionFraisEmAcqDTO);
        if (repartionFraisEmAcqDTO.getId() != null) {
            throw new BadRequestAlertException("A new repartionFraisEmAcq cannot already have an ID", ENTITY_NAME, "idexists");
        }
        RepartionFraisEmAcqDTO result = repartionFraisEmAcqService.save(repartionFraisEmAcqDTO);
        return ResponseEntity.created(new URI("/api/repartion-frais-em-acqs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /repartion-frais-em-acqs : Updates an existing repartionFraisEmAcq.
     *
     * @param repartionFraisEmAcqDTO the repartionFraisEmAcqDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated repartionFraisEmAcqDTO,
     * or with status 400 (Bad Request) if the repartionFraisEmAcqDTO is not valid,
     * or with status 500 (Internal Server Error) if the repartionFraisEmAcqDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/repartion-frais-em-acqs")
    @Timed
    public ResponseEntity<RepartionFraisEmAcqDTO> updateRepartionFraisEmAcq(@RequestBody RepartionFraisEmAcqDTO repartionFraisEmAcqDTO) throws URISyntaxException {
        log.debug("REST request to update RepartionFraisEmAcq : {}", repartionFraisEmAcqDTO);
        if (repartionFraisEmAcqDTO.getId() == null) {
            return createRepartionFraisEmAcq(repartionFraisEmAcqDTO);
        }
        RepartionFraisEmAcqDTO result = repartionFraisEmAcqService.save(repartionFraisEmAcqDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, repartionFraisEmAcqDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /repartion-frais-em-acqs : get all the repartionFraisEmAcqs.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of repartionFraisEmAcqs in body
     */
    @GetMapping("/repartion-frais-em-acqs")
    @Timed
    public ResponseEntity<List<RepartionFraisEmAcqDTO>> getAllRepartionFraisEmAcqs(Pageable pageable) {
        log.debug("REST request to get a page of RepartionFraisEmAcqs");
        Page<RepartionFraisEmAcqDTO> page = repartionFraisEmAcqService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/repartion-frais-em-acqs");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /repartion-frais-em-acqs/:id : get the "id" repartionFraisEmAcq.
     *
     * @param id the id of the repartionFraisEmAcqDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the repartionFraisEmAcqDTO, or with status 404 (Not Found)
     */
    @GetMapping("/repartion-frais-em-acqs/{id}")
    @Timed
    public ResponseEntity<RepartionFraisEmAcqDTO> getRepartionFraisEmAcq(@PathVariable Long id) {
        log.debug("REST request to get RepartionFraisEmAcq : {}", id);
        RepartionFraisEmAcqDTO repartionFraisEmAcqDTO = repartionFraisEmAcqService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(repartionFraisEmAcqDTO));
    }

    /**
     * DELETE  /repartion-frais-em-acqs/:id : delete the "id" repartionFraisEmAcq.
     *
     * @param id the id of the repartionFraisEmAcqDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/repartion-frais-em-acqs/{id}")
    @Timed
    public ResponseEntity<Void> deleteRepartionFraisEmAcq(@PathVariable Long id) {
        log.debug("REST request to delete RepartionFraisEmAcq : {}", id);
        repartionFraisEmAcqService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
