package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.BrancheActiviteService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.BrancheActiviteDTO;
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
 * REST controller for managing BrancheActivite.
 */
@RestController
@RequestMapping("/api")
public class BrancheActiviteResource {

    private final Logger log = LoggerFactory.getLogger(BrancheActiviteResource.class);

    private static final String ENTITY_NAME = "brancheActivite";

    private BrancheActiviteService brancheActiviteService;

    public BrancheActiviteResource(BrancheActiviteService brancheActiviteService) {
        this.brancheActiviteService = brancheActiviteService;
    }

    /**
     * POST  /branche-activites : Create a new brancheActivite.
     *
     * @param brancheActiviteDTO the brancheActiviteDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new brancheActiviteDTO, or with status 400 (Bad Request) if the brancheActivite has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/branche-activites")
    @Timed
    public ResponseEntity<BrancheActiviteDTO> createBrancheActivite(@Valid @RequestBody BrancheActiviteDTO brancheActiviteDTO) throws URISyntaxException {
        log.debug("REST request to save BrancheActivite : {}", brancheActiviteDTO);
        if (brancheActiviteDTO.getId() != null) {
            throw new BadRequestAlertException("A new brancheActivite cannot already have an ID", ENTITY_NAME, "idexists");
        }
        BrancheActiviteDTO result = brancheActiviteService.save(brancheActiviteDTO);
        return ResponseEntity.created(new URI("/api/branche-activites/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /branche-activites : Updates an existing brancheActivite.
     *
     * @param brancheActiviteDTO the brancheActiviteDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated brancheActiviteDTO,
     * or with status 400 (Bad Request) if the brancheActiviteDTO is not valid,
     * or with status 500 (Internal Server Error) if the brancheActiviteDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/branche-activites")
    @Timed
    public ResponseEntity<BrancheActiviteDTO> updateBrancheActivite(@Valid @RequestBody BrancheActiviteDTO brancheActiviteDTO) throws URISyntaxException {
        log.debug("REST request to update BrancheActivite : {}", brancheActiviteDTO);
        if (brancheActiviteDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        BrancheActiviteDTO result = brancheActiviteService.save(brancheActiviteDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, brancheActiviteDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /branche-activites : get all the brancheActivites.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of brancheActivites in body
     */
    @GetMapping("/branche-activites")
    @Timed
    public ResponseEntity<List<BrancheActiviteDTO>> getAllBrancheActivites(Pageable pageable) {
        log.debug("REST request to get a page of BrancheActivites");
        Page<BrancheActiviteDTO> page = brancheActiviteService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/branche-activites");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /branche-activites/:id : get the "id" brancheActivite.
     *
     * @param id the id of the brancheActiviteDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the brancheActiviteDTO, or with status 404 (Not Found)
     */
    @GetMapping("/branche-activites/{id}")
    @Timed
    public ResponseEntity<BrancheActiviteDTO> getBrancheActivite(@PathVariable Long id) {
        log.debug("REST request to get BrancheActivite : {}", id);
       BrancheActiviteDTO brancheActiviteDTO = brancheActiviteService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(brancheActiviteDTO));
    }

    /**
     * DELETE  /branche-activites/:id : delete the "id" brancheActivite.
     *
     * @param id the id of the brancheActiviteDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/branche-activites/{id}")
    @Timed
    public ResponseEntity<Void> deleteBrancheActivite(@PathVariable Long id) {
        log.debug("REST request to delete BrancheActivite : {}", id);
        brancheActiviteService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
