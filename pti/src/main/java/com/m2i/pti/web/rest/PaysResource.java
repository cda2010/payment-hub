package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.PaysService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.PaysDTO;
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
 * REST controller for managing Pays.
 */
@RestController
@RequestMapping("/api")
public class PaysResource {

    private final Logger log = LoggerFactory.getLogger(PaysResource.class);

    private static final String ENTITY_NAME = "pays";

    private final PaysService paysService;

    public PaysResource(PaysService paysService) {
        this.paysService = paysService;
    }

    /**
     * POST  /pays : Create a new pays.
     *
     * @param paysDTO the paysDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new paysDTO, or with status 400 (Bad Request) if the pays has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/pays")
    @Timed
    public ResponseEntity<PaysDTO> createPays(@Valid @RequestBody PaysDTO paysDTO) throws URISyntaxException {
        log.debug("REST request to save Pays : {}", paysDTO);
        if (paysDTO.getId() != null) {
            throw new BadRequestAlertException("A new pays cannot already have an ID", ENTITY_NAME, "idexists");
        }
        PaysDTO result = paysService.save(paysDTO);
        return ResponseEntity.created(new URI("/api/pays/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /pays : Updates an existing pays.
     *
     * @param paysDTO the paysDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated paysDTO,
     * or with status 400 (Bad Request) if the paysDTO is not valid,
     * or with status 500 (Internal Server Error) if the paysDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/pays")
    @Timed
    public ResponseEntity<PaysDTO> updatePays(@Valid @RequestBody PaysDTO paysDTO) throws URISyntaxException {
        log.debug("REST request to update Pays : {}", paysDTO);
        if (paysDTO.getId() == null) {
            return createPays(paysDTO);
        }
        PaysDTO result = paysService.save(paysDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, paysDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /pays : get all the pays.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of pays in body
     */
    @GetMapping("/pays")
    @Timed
    public ResponseEntity<List<PaysDTO>> getAllPays(Pageable pageable) {
        log.debug("REST request to get a page of Pays");
        Page<PaysDTO> page = paysService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/pays");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /pays/:id : get the "id" pays.
     *
     * @param id the id of the paysDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the paysDTO, or with status 404 (Not Found)
     */
    @GetMapping("/pays/{id}")
    @Timed
    public ResponseEntity<PaysDTO> getPays(@PathVariable Long id) {
        log.debug("REST request to get Pays : {}", id);
        PaysDTO paysDTO = paysService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(paysDTO));
    }

    /**
     * DELETE  /pays/:id : delete the "id" pays.
     *
     * @param id the id of the paysDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/pays/{id}")
    @Timed
    public ResponseEntity<Void> deletePays(@PathVariable Long id) {
        log.debug("REST request to delete Pays : {}", id);
        paysService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
