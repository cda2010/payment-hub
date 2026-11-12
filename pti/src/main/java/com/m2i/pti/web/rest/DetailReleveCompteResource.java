package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.DetailReleveCompteService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.DetailReleveCompteDTO;
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
 * REST controller for managing DetailReleveCompte.
 */
@RestController
@RequestMapping("/api")
public class DetailReleveCompteResource {

    private final Logger log = LoggerFactory.getLogger(DetailReleveCompteResource.class);

    private static final String ENTITY_NAME = "detailReleveCompte";

    private final DetailReleveCompteService detailReleveCompteService;

    public DetailReleveCompteResource(DetailReleveCompteService detailReleveCompteService) {
        this.detailReleveCompteService = detailReleveCompteService;
    }

    /**
     * POST  /detail-releve-comptes : Create a new detailReleveCompte.
     *
     * @param detailReleveCompteDTO the detailReleveCompteDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new detailReleveCompteDTO, or with status 400 (Bad Request) if the detailReleveCompte has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/detail-releve-comptes")
    @Timed
    public ResponseEntity<DetailReleveCompteDTO> createDetailReleveCompte(@RequestBody DetailReleveCompteDTO detailReleveCompteDTO) throws URISyntaxException {
        log.debug("REST request to save DetailReleveCompte : {}", detailReleveCompteDTO);
        if (detailReleveCompteDTO.getId() != null) {
            throw new BadRequestAlertException("A new detailReleveCompte cannot already have an ID", ENTITY_NAME, "idexists");
        }
        DetailReleveCompteDTO result = detailReleveCompteService.save(detailReleveCompteDTO);
        return ResponseEntity.created(new URI("/api/detail-releve-comptes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /detail-releve-comptes : Updates an existing detailReleveCompte.
     *
     * @param detailReleveCompteDTO the detailReleveCompteDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated detailReleveCompteDTO,
     * or with status 400 (Bad Request) if the detailReleveCompteDTO is not valid,
     * or with status 500 (Internal Server Error) if the detailReleveCompteDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/detail-releve-comptes")
    @Timed
    public ResponseEntity<DetailReleveCompteDTO> updateDetailReleveCompte(@RequestBody DetailReleveCompteDTO detailReleveCompteDTO) throws URISyntaxException {
        log.debug("REST request to update DetailReleveCompte : {}", detailReleveCompteDTO);
        if (detailReleveCompteDTO.getId() == null) {
            return createDetailReleveCompte(detailReleveCompteDTO);
        }
        DetailReleveCompteDTO result = detailReleveCompteService.save(detailReleveCompteDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, detailReleveCompteDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /detail-releve-comptes : get all the detailReleveComptes.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of detailReleveComptes in body
     */
    @GetMapping("/detail-releve-comptes")
    @Timed
    public ResponseEntity<List<DetailReleveCompteDTO>> getAllDetailReleveComptes(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of DetailReleveComptes");
        Page<DetailReleveCompteDTO> page = detailReleveCompteService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/detail-releve-comptes");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/detail-releve-comptes/by-releve")
    @Timed
    public List<DetailReleveCompteDTO> getAllDetailReleveComptes(@RequestParam Long releveId) {
        log.debug("REST request to get a page of DetailReleveComptes");
        List<DetailReleveCompteDTO> all = detailReleveCompteService.findByReleve(releveId);
        return all;
    }
    /**
     * GET  /detail-releve-comptes/:id : get the "id" detailReleveCompte.
     *
     * @param id the id of the detailReleveCompteDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the detailReleveCompteDTO, or with status 404 (Not Found)
     */
    @GetMapping("/detail-releve-comptes/{id}")
    @Timed
    public ResponseEntity<DetailReleveCompteDTO> getDetailReleveCompte(@PathVariable Long id) {
        log.debug("REST request to get DetailReleveCompte : {}", id);
        DetailReleveCompteDTO detailReleveCompteDTO = detailReleveCompteService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(detailReleveCompteDTO));
    }

    /**
     * DELETE  /detail-releve-comptes/:id : delete the "id" detailReleveCompte.
     *
     * @param id the id of the detailReleveCompteDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/detail-releve-comptes/{id}")
    @Timed
    public ResponseEntity<Void> deleteDetailReleveCompte(@PathVariable Long id) {
        log.debug("REST request to delete DetailReleveCompte : {}", id);
        detailReleveCompteService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
