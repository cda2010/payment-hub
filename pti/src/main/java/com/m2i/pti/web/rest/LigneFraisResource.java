package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Preconditions;
import com.m2i.pti.service.LigneFraisService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.LigneFraisDTO;
import io.github.jhipster.web.util.ResponseUtil;
import liquibase.precondition.Precondition;
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
 * REST controller for managing LigneFrais.
 */
@RestController
@RequestMapping("/api")
public class LigneFraisResource {

    private final Logger log = LoggerFactory.getLogger(LigneFraisResource.class);

    private static final String ENTITY_NAME = "ligneFrais";

    private final LigneFraisService ligneFraisService;

    public LigneFraisResource(LigneFraisService ligneFraisService) {
        this.ligneFraisService = ligneFraisService;
    }

    /**
     * POST  /ligne-frais : Create a new ligneFrais.
     *
     * @param ligneFraisDTO the ligneFraisDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new ligneFraisDTO, or with status 400 (Bad Request) if the ligneFrais has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/ligne-frais")
    @Timed
    public ResponseEntity<LigneFraisDTO> createLigneFrais(@RequestBody LigneFraisDTO ligneFraisDTO) throws URISyntaxException {
        log.debug("REST request to save LigneFrais : {}", ligneFraisDTO);
        Preconditions.checkNotNull(ligneFraisDTO,ligneFraisDTO);
        if (ligneFraisDTO.getId() != null) {
            throw new BadRequestAlertException("A new ligneFrais cannot already have an ID", ENTITY_NAME, "idexists");
        }
        LigneFraisDTO result = ligneFraisService.save(ligneFraisDTO);
        return ResponseEntity.created(new URI("/api/ligne-frais/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /ligne-frais : Updates an existing ligneFrais.
     *
     * @param ligneFraisDTO the ligneFraisDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated ligneFraisDTO,
     * or with status 400 (Bad Request) if the ligneFraisDTO is not valid,
     * or with status 500 (Internal Server Error) if the ligneFraisDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/ligne-frais")
    @Timed
    public ResponseEntity<LigneFraisDTO> updateLigneFrais(@RequestBody LigneFraisDTO ligneFraisDTO) throws URISyntaxException {
        log.debug("REST request to update LigneFrais : {}", ligneFraisDTO);
        if (ligneFraisDTO.getId() == null) {
            return createLigneFrais(ligneFraisDTO);
        }
        LigneFraisDTO result = ligneFraisService.save(ligneFraisDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, ligneFraisDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /ligne-frais : get all the ligneFrais.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of ligneFrais in body
     */
    @GetMapping("/ligne-frais")
    @Timed
    public ResponseEntity<List<LigneFraisDTO>> getAllLigneFrais(Pageable pageable) {
        log.debug("REST request to get a page of LigneFrais");
        Page<LigneFraisDTO> page = ligneFraisService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/ligne-frais");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /ligne-frais : get all the ligneFrais by tableauFrais.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of ligneFrais in body
     */
/*    @GetMapping("/ligne-frais/{id}/tableauFrais")
    @Timed
    public ResponseEntity<List<LigneFraisDTO>> getAllLigneFrais(Pageable pageable,@PathVariable long id) {
        log.debug("REST request to get a page of LigneFrais");
        Page<LigneFraisDTO> page = ligneFraisService.findAllByTableauFrais(id,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/ligne-frais");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }*/
    @GetMapping("/ligne-frais/{id}/tableauFrais")
    @Timed
    public List<LigneFraisDTO> getAllLigneFraisList(@PathVariable long id) {
        log.debug("REST request to get a page of LigneFrais");
        //Page<LigneFraisDTO> page = ligneFraisService.findAllByTableauFrais(id,pageable);
        List<LigneFraisDTO> page = ligneFraisService.findAllByTableauFraisList(id);
        //HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/ligne-frais");
        return page;
    }

    /**
     * GET  /ligne-frais/:id : get the "id" ligneFrais.
     *
     * @param id the id of the ligneFraisDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the ligneFraisDTO, or with status 404 (Not Found)
     */
    @GetMapping("/ligne-frais/{id}")
    @Timed
    public ResponseEntity<LigneFraisDTO> getLigneFrais(@PathVariable Long id) {
        log.debug("REST request to get LigneFrais : {}", id);
        LigneFraisDTO ligneFraisDTO = ligneFraisService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(ligneFraisDTO));
    }

    /**
     * DELETE  /ligne-frais/:id : delete the "id" ligneFrais.
     *
     * @param id the id of the ligneFraisDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/ligne-frais/{id}")
    @Timed
    public ResponseEntity<Void> deleteLigneFrais(@PathVariable Long id) {
        log.debug("REST request to delete LigneFrais : {}", id);
        ligneFraisService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
