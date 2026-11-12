package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.CanalService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.CanalDTO;
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
 * REST controller for managing Canal.
 */
@RestController
@RequestMapping("/api")
public class CanalResource {

    private final Logger log = LoggerFactory.getLogger(CanalResource.class);

    private static final String ENTITY_NAME = "canal";

    private final CanalService canalService;

    public CanalResource(CanalService canalService) {
        this.canalService = canalService;
    }

    /**
     * POST  /canals : Create a new canal.
     *
     * @param canalDTO the canalDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new canalDTO, or with status 400 (Bad Request) if the canal has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/canals")
    @Timed
    public ResponseEntity<CanalDTO> createCanal(@Valid @RequestBody CanalDTO canalDTO) throws URISyntaxException {
        log.debug("REST request to save Canal : {}", canalDTO);
        if (canalDTO.getId() != null) {
            throw new BadRequestAlertException("A new canal cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CanalDTO result = canalService.save(canalDTO);
        return ResponseEntity.created(new URI("/api/canals/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /canals : Updates an existing canal.
     *
     * @param canalDTO the canalDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated canalDTO,
     * or with status 400 (Bad Request) if the canalDTO is not valid,
     * or with status 500 (Internal Server Error) if the canalDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/canals")
    @Timed
    public ResponseEntity<CanalDTO> updateCanal(@Valid @RequestBody CanalDTO canalDTO) throws URISyntaxException {
        log.debug("REST request to update Canal : {}", canalDTO);
        if (canalDTO.getId() == null) {
            return createCanal(canalDTO);
        }
        CanalDTO result = canalService.save(canalDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, canalDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /canals : get all the canals.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of canals in body
     */
    @GetMapping("/canals")
    @Timed
    public ResponseEntity<List<CanalDTO>> getAllCanals(Pageable pageable) {
        log.debug("REST request to get a page of Canals");
        Page<CanalDTO> page = canalService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/canals");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /canals/:id : get the "id" canal.
     *
     * @param id the id of the canalDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the canalDTO, or with status 404 (Not Found)
     */
    @GetMapping("/canals/{id}")
    @Timed
    public ResponseEntity<CanalDTO> getCanal(@PathVariable Long id) {
        log.debug("REST request to get Canal : {}", id);
        CanalDTO canalDTO = canalService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(canalDTO));
    }

    /**
     * DELETE  /canals/:id : delete the "id" canal.
     *
     * @param id the id of the canalDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/canals/{id}")
    @Timed
    public ResponseEntity<Void> deleteCanal(@PathVariable Long id) {
        log.debug("REST request to delete Canal : {}", id);
        canalService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
