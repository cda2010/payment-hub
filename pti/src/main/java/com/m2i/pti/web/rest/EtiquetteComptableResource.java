package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.EtiquetteComptableService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.EtiquetteComptableDTO;
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
 * REST controller for managing EtiquetteComptable.
 */
@RestController
@RequestMapping("/api")
public class EtiquetteComptableResource {

    private final Logger log = LoggerFactory.getLogger(EtiquetteComptableResource.class);

    private static final String ENTITY_NAME = "etiquetteComptable";

    private final EtiquetteComptableService etiquetteComptableService;

    public EtiquetteComptableResource(EtiquetteComptableService etiquetteComptableService) {
        this.etiquetteComptableService = etiquetteComptableService;
    }

    /**
     * POST  /etiquette-comptables : Create a new etiquetteComptable.
     *
     * @param etiquetteComptableDTO the etiquetteComptableDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new etiquetteComptableDTO, or with status 400 (Bad Request) if the etiquetteComptable has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/etiquette-comptables")
    @Timed
    public ResponseEntity<EtiquetteComptableDTO> createEtiquetteComptable(@RequestBody EtiquetteComptableDTO etiquetteComptableDTO) throws URISyntaxException {
        log.debug("REST request to save EtiquetteComptable : {}", etiquetteComptableDTO);
        if (etiquetteComptableDTO.getId() != null) {
            throw new BadRequestAlertException("A new etiquetteComptable cannot already have an ID", ENTITY_NAME, "idexists");
        }
        EtiquetteComptableDTO result = etiquetteComptableService.save(etiquetteComptableDTO);
        return ResponseEntity.created(new URI("/api/etiquette-comptables/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /etiquette-comptables : Updates an existing etiquetteComptable.
     *
     * @param etiquetteComptableDTO the etiquetteComptableDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated etiquetteComptableDTO,
     * or with status 400 (Bad Request) if the etiquetteComptableDTO is not valid,
     * or with status 500 (Internal Server Error) if the etiquetteComptableDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/etiquette-comptables")
    @Timed
    public ResponseEntity<EtiquetteComptableDTO> updateEtiquetteComptable(@RequestBody EtiquetteComptableDTO etiquetteComptableDTO) throws URISyntaxException {
        log.debug("REST request to update EtiquetteComptable : {}", etiquetteComptableDTO);
        if (etiquetteComptableDTO.getId() == null) {
            return createEtiquetteComptable(etiquetteComptableDTO);
        }
        EtiquetteComptableDTO result = etiquetteComptableService.save(etiquetteComptableDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, etiquetteComptableDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /etiquette-comptables : get all the etiquetteComptables.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of etiquetteComptables in body
     */
    @GetMapping("/etiquette-comptables")
    @Timed
    public ResponseEntity<List<EtiquetteComptableDTO>> getAllEtiquetteComptables(Pageable pageable) {
        log.debug("REST request to get a page of EtiquetteComptables");
        Page<EtiquetteComptableDTO> page = etiquetteComptableService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/etiquette-comptables");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /etiquette-comptables/:id : get the "id" etiquetteComptable.
     *
     * @param id the id of the etiquetteComptableDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the etiquetteComptableDTO, or with status 404 (Not Found)
     */
    @GetMapping("/etiquette-comptables/{id}")
    @Timed
    public ResponseEntity<EtiquetteComptableDTO> getEtiquetteComptable(@PathVariable Long id) {
        log.debug("REST request to get EtiquetteComptable : {}", id);
        EtiquetteComptableDTO etiquetteComptableDTO = etiquetteComptableService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(etiquetteComptableDTO));
    }

    /**
     * DELETE  /etiquette-comptables/:id : delete the "id" etiquetteComptable.
     *
     * @param id the id of the etiquetteComptableDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/etiquette-comptables/{id}")
    @Timed
    public ResponseEntity<Void> deleteEtiquetteComptable(@PathVariable Long id) {
        log.debug("REST request to delete EtiquetteComptable : {}", id);
        etiquetteComptableService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
