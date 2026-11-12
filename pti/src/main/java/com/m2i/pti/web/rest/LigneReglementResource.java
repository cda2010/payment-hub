package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.LigneReglement;

import com.m2i.pti.repository.LigneReglementRepository;
import com.m2i.pti.service.LigneReglementDTO;
import com.m2i.pti.service.mapper.LigneReglementMapper;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
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
 * REST controller for managing LigneReglement.
 */
@RestController
@RequestMapping("/api")
public class LigneReglementResource {

    private final Logger log = LoggerFactory.getLogger(LigneReglementResource.class);

    private static final String ENTITY_NAME = "ligneReglement";

    private final LigneReglementRepository ligneReglementRepository;

    private final LigneReglementMapper ligneReglementMapper;

    public LigneReglementResource(LigneReglementRepository ligneReglementRepository, LigneReglementMapper ligneReglementMapper) {
        this.ligneReglementRepository = ligneReglementRepository;
        this.ligneReglementMapper = ligneReglementMapper;
    }

    /**
     * POST  /ligne-reglements : Create a new ligneReglement.
     *
     * @param ligneReglement the ligneReglement to create
     * @return the ResponseEntity with status 201 (Created) and with body the new ligneReglement, or with status 400 (Bad Request) if the ligneReglement has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/ligne-reglements")
    @Timed
    public ResponseEntity<LigneReglement> createLigneReglement(@RequestBody LigneReglement ligneReglement) throws URISyntaxException {
        log.debug("REST request to save LigneReglement : {}", ligneReglement);
        if (ligneReglement.getId() != null) {
            throw new BadRequestAlertException("A new ligneReglement cannot already have an ID", ENTITY_NAME, "idexists");
        }
        LigneReglement result = ligneReglementRepository.save(ligneReglement);
        return ResponseEntity.created(new URI("/api/ligne-reglements/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /ligne-reglements : Updates an existing ligneReglement.
     *
     * @param ligneReglement the ligneReglement to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated ligneReglement,
     * or with status 400 (Bad Request) if the ligneReglement is not valid,
     * or with status 500 (Internal Server Error) if the ligneReglement couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/ligne-reglements")
    @Timed
    public ResponseEntity<LigneReglement> updateLigneReglement(@RequestBody LigneReglement ligneReglement) throws URISyntaxException {
        log.debug("REST request to update LigneReglement : {}", ligneReglement);
        if (ligneReglement.getId() == null) {
            return createLigneReglement(ligneReglement);
        }
        LigneReglement result = ligneReglementRepository.save(ligneReglement);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ligneReglement.getId().toString()))
            .body(result);
    }

    /**
     * GET  /ligne-reglements : get all the ligneReglements.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of ligneReglements in body
     */
    @GetMapping("/ligne-reglements")
    @Timed
    public ResponseEntity<List<LigneReglementDTO>> getAllLigneReglements(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of LigneReglements");
        Page<LigneReglementDTO> page = ligneReglementRepository.findAll(pageable).map(ligneReglementMapper::toDto);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/ligne-reglements");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /ligne-reglements/:id : get the "id" ligneReglement.
     *
     * @param id the id of the ligneReglement to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the ligneReglement, or with status 404 (Not Found)
     */
    @GetMapping("/ligne-reglements/{id}")
    @Timed
    public ResponseEntity<LigneReglement> getLigneReglement(@PathVariable Long id) {
        log.debug("REST request to get LigneReglement : {}", id);
        LigneReglement ligneReglement = ligneReglementRepository.findById(id).orElse(null);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(ligneReglement));
    }

    /**
     * DELETE  /ligne-reglements/:id : delete the "id" ligneReglement.
     *
     * @param id the id of the ligneReglement to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/ligne-reglements/{id}")
    @Timed
    public ResponseEntity<Void> deleteLigneReglement(@PathVariable Long id) {
        log.debug("REST request to delete LigneReglement : {}", id);
        ligneReglementRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
