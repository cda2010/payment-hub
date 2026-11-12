package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.BilanOperation;
import com.m2i.pti.service.BilanOperationService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.BilanOperationDTO;
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

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing BilanOperation.
 */
@RestController
@RequestMapping("/api")
public class BilanOperationResource {

    private final Logger log = LoggerFactory.getLogger(BilanOperationResource.class);

    private static final String ENTITY_NAME = "bilanOperation";

    private final BilanOperationService bilanOperationService;

    public BilanOperationResource(BilanOperationService bilanOperationService) {
        this.bilanOperationService = bilanOperationService;
    }

    /**
     * POST  /bilan-operations : Create a new bilanOperation.
     *
     * @param bilanOperationDTO the bilanOperationDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new bilanOperationDTO, or with status 400 (Bad Request) if the bilanOperation has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/bilan-operations")
    @Timed
    public ResponseEntity<BilanOperationDTO> createBilanOperation(@RequestBody BilanOperationDTO bilanOperationDTO) throws URISyntaxException {
        log.debug("REST request to save BilanOperation : {}", bilanOperationDTO);
        if (bilanOperationDTO.getId() != null) {
            throw new BadRequestAlertException("A new bilanOperation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        BilanOperationDTO result = bilanOperationService.save(bilanOperationDTO);
        return ResponseEntity.created(new URI("/api/bilan-operations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /bilan-operations : Updates an existing bilanOperation.
     *
     * @param bilanOperationDTO the bilanOperationDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated bilanOperationDTO,
     * or with status 400 (Bad Request) if the bilanOperationDTO is not valid,
     * or with status 500 (Internal Server Error) if the bilanOperationDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/bilan-operations")
    @Timed
    public ResponseEntity<BilanOperationDTO> updateBilanOperation(@RequestBody BilanOperationDTO bilanOperationDTO) throws URISyntaxException {
        log.debug("REST request to update BilanOperation : {}", bilanOperationDTO);
        if (bilanOperationDTO.getId() == null) {
            return createBilanOperation(bilanOperationDTO);
        }
        BilanOperationDTO result = bilanOperationService.save(bilanOperationDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, bilanOperationDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /bilan-operations : get all the bilanOperations.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of bilanOperations in body
     */
    @GetMapping("/bilan-operations")
    @Timed
    public ResponseEntity<List<BilanOperationDTO>> getAllBilanOperations(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of BilanOperations");
        Page<BilanOperationDTO> page = bilanOperationService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/bilan-operations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /bilan-operations/:id : get the "id" bilanOperation.
     *
     * @param id the id of the bilanOperationDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the bilanOperationDTO, or with status 404 (Not Found)
     */
    @GetMapping("/bilan-operations/{id}")
    @Timed
    public ResponseEntity<BilanOperationDTO> getBilanOperation(@PathVariable Long id) {
        log.debug("REST request to get BilanOperation : {}", id);
        BilanOperationDTO bilanOperationDTO = bilanOperationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(bilanOperationDTO));
    }

    /**
     * DELETE  /bilan-operations/:id : delete the "id" bilanOperation.
     *
     * @param id the id of the bilanOperationDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/bilan-operations/{id}")
    @Timed
    public ResponseEntity<Void> deleteBilanOperation(@PathVariable Long id) {
        log.debug("REST request to delete BilanOperation : {}", id);
        bilanOperationService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
    
       @GetMapping("/bilan-operations/xxx/{id}")
    @Timed
    public   List<BilanOperation>  getBilanOperationxx(@PathVariable Long id) {
        log.debug("REST request to get BilanOperation : {}", id);
        List<BilanOperation> v= bilanOperationService.findAllByActeurAndMultiCriteria(null, 6L, 2L, 1373L, 1373L, 293L, 294L, java.time.LocalDate.now());
        return v;
    }

    @GetMapping("/bilan-operation/by-personne")
    @Timed
    public ResponseEntity<List<BilanOperationDTO>> getBilanOperationByPersonne(@RequestParam(required = true) Long personneId, @RequestParam(required = false) ZonedDateTime dateDebut, @RequestParam(required = false) ZonedDateTime dateFin, Pageable pageable ){
        log.debug("Request to get bilan of operation.......");
        Page<BilanOperationDTO> page=bilanOperationService.findallByPersonneoptimiser(personneId,dateDebut,dateFin,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/bilan-operations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);

    }

    
    
}
