package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.MotifOperationService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.MotifOperationDTO;
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
 * REST controller for managing MotifOperation.
 */
@RestController
@RequestMapping("/api")
public class MotifOperationResource {

    private final Logger log = LoggerFactory.getLogger(MotifOperationResource.class);

    private static final String ENTITY_NAME = "motifOperation";

    private MotifOperationService motifOperationService;

    public MotifOperationResource(MotifOperationService motifOperationService) {
        this.motifOperationService = motifOperationService;
    }

    /**
     * POST  /motif-operations : Create a new motifOperation.
     *
     * @param motifOperationDTO the motifOperationDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new motifOperationDTO, or with status 400 (Bad Request) if the motifOperation has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/motif-operations")
    @Timed
    public ResponseEntity<MotifOperationDTO> createMotifOperation(@Valid @RequestBody MotifOperationDTO motifOperationDTO) throws URISyntaxException {
        log.debug("REST request to save MotifOperation : {}", motifOperationDTO);
        if (motifOperationDTO.getId() != null) {
            throw new BadRequestAlertException("A new motifOperation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        MotifOperationDTO result = motifOperationService.save(motifOperationDTO);
        return ResponseEntity.created(new URI("/api/motif-operations/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT  /motif-operations : Updates an existing motifOperation.
     *
     * @param motifOperationDTO the motifOperationDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated motifOperationDTO,
     * or with status 400 (Bad Request) if the motifOperationDTO is not valid,
     * or with status 500 (Internal Server Error) if the motifOperationDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/motif-operations")
    @Timed
    public ResponseEntity<MotifOperationDTO> updateMotifOperation(@Valid @RequestBody MotifOperationDTO motifOperationDTO) throws URISyntaxException {
        log.debug("REST request to update MotifOperation : {}", motifOperationDTO);
        if (motifOperationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        MotifOperationDTO result = motifOperationService.save(motifOperationDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, motifOperationDTO.getId().toString()))
                .body(result);
    }

    /**
     * GET  /motif-operations : get all the motifOperations.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of motifOperations in body
     */
    @GetMapping("/motif-operations")
    @Timed
    public ResponseEntity<List<MotifOperationDTO>> getAllMotifOperations(Pageable pageable) {
        log.debug("REST request to get a page of MotifOperations");
        Page<MotifOperationDTO> page = motifOperationService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/motif-operations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /motif-operations/:id : get the "id" motifOperation.
     *
     * @param id the id of the motifOperationDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the motifOperationDTO, or with status 404 (Not Found)
     */
    @GetMapping("/motif-operations/{id}")
    @Timed
    public ResponseEntity<MotifOperationDTO> getMotifOperation(@PathVariable Long id) {
        log.debug("REST request to get MotifOperation : {}", id);
        MotifOperationDTO motifOperationDTO = motifOperationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(motifOperationDTO));
    }

    /**
     * DELETE  /motif-operations/:id : delete the "id" motifOperation.
     *
     * @param id the id of the motifOperationDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/motif-operations/{id}")
    @Timed
    public ResponseEntity<Void> deleteMotifOperation(@PathVariable Long id) {
        log.debug("REST request to delete MotifOperation : {}", id);
        motifOperationService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
