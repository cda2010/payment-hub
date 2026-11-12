package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.ReleveCompteService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.ReleveCompteDTO;
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
 * REST controller for managing ReleveCompte.
 */
@RestController
@RequestMapping("/api")
public class ReleveCompteResource {

    private final Logger log = LoggerFactory.getLogger(ReleveCompteResource.class);

    private static final String ENTITY_NAME = "releveCompte";

    private final ReleveCompteService releveCompteService;

    public ReleveCompteResource(ReleveCompteService releveCompteService) {
        this.releveCompteService = releveCompteService;
    }

    /**
     * POST  /releve-comptes : Create a new releveCompte.
     *
     * @param releveCompteDTO the releveCompteDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new releveCompteDTO, or with status 400 (Bad Request) if the releveCompte has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/releve-comptes")
    @Timed
    public void createReleveCompte(@RequestBody ReleveCompteDTO releveCompteDTO) throws URISyntaxException {

      releveCompteService.generateReleveOfPastMonth();

    }

    /**
     * PUT  /releve-comptes : Updates an existing releveCompte.
     *
     * @param releveCompteDTO the releveCompteDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated releveCompteDTO,
     * or with status 400 (Bad Request) if the releveCompteDTO is not valid,
     * or with status 500 (Internal Server Error) if the releveCompteDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
//    @PutMapping("/releve-comptes")
//    @Timed
//    public ResponseEntity<ReleveCompteDTO> updateReleveCompte(@RequestBody ReleveCompteDTO releveCompteDTO) throws URISyntaxException {
//        log.debug("REST request to update ReleveCompte : {}", releveCompteDTO);
//        if (releveCompteDTO.getId() == null) {
//            return createReleveCompte(releveCompteDTO);
//        }
//        ReleveCompteDTO result = releveCompteService.save(releveCompteDTO);
//        return ResponseEntity.ok()
//            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, releveCompteDTO.getId().toString()))
//            .body(result);
//    }

    /**
     * GET  /releve-comptes : get all the releveComptes.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of releveComptes in body
     */
    @GetMapping("/releve-comptes")
    @Timed
    public ResponseEntity<List<ReleveCompteDTO>> getAllReleveComptes(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of ReleveComptes");
        Page<ReleveCompteDTO> page = releveCompteService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/releve-comptes");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /releve-comptes/:id : get the "id" releveCompte.
     *
     * @param id the id of the releveCompteDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the releveCompteDTO, or with status 404 (Not Found)
     */
    @GetMapping("/releve-comptes/{id}")
    @Timed
    public ResponseEntity<ReleveCompteDTO> getReleveCompte(@PathVariable Long id) {
        log.debug("REST request to get ReleveCompte : {}", id);
        ReleveCompteDTO releveCompteDTO = releveCompteService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(releveCompteDTO));
    }

    /**
     * DELETE  /releve-comptes/:id : delete the "id" releveCompte.
     *
     * @param id the id of the releveCompteDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/releve-comptes/{id}")
    @Timed
    public ResponseEntity<Void> deleteReleveCompte(@PathVariable Long id) {
        log.debug("REST request to delete ReleveCompte : {}", id);
        releveCompteService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
