package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.CompteBanqueService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.CompteBanqueDTO;
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
 * REST controller for managing CompteBanque.
 */
@RestController
@RequestMapping("/api")
public class CompteBanqueResource {

    private final Logger log = LoggerFactory.getLogger(CompteBanqueResource.class);

    private static final String ENTITY_NAME = "compteBanque";

    private final CompteBanqueService compteBanqueService;

    public CompteBanqueResource(CompteBanqueService compteBanqueService) {
        this.compteBanqueService = compteBanqueService;
    }

    /**
     * POST  /compte-banques : Create a new compteBanque.
     *
     * @param compteBanqueDTO the compteBanqueDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new compteBanqueDTO, or with status 400 (Bad Request) if the compteBanque has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/compte-banques")
    @Timed
    public ResponseEntity<CompteBanqueDTO> createCompteBanque(@Valid @RequestBody CompteBanqueDTO compteBanqueDTO) throws URISyntaxException {
        log.debug("REST request to save CompteBanque : {}", compteBanqueDTO);
        if (compteBanqueDTO.getId() != null) {
            throw new BadRequestAlertException("A new compteBanque cannot already have an ID", ENTITY_NAME, "idexists");
        }
        CompteBanqueDTO result = compteBanqueService.save(compteBanqueDTO);
        return ResponseEntity.created(new URI("/api/compte-banques/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /compte-banques : Updates an existing compteBanque.
     *
     * @param compteBanqueDTO the compteBanqueDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated compteBanqueDTO,
     * or with status 400 (Bad Request) if the compteBanqueDTO is not valid,
     * or with status 500 (Internal Server Error) if the compteBanqueDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/compte-banques")
    @Timed
    public ResponseEntity<CompteBanqueDTO> updateCompteBanque(@Valid @RequestBody CompteBanqueDTO compteBanqueDTO) throws URISyntaxException {
        log.debug("REST request to update CompteBanque : {}", compteBanqueDTO);
        if (compteBanqueDTO.getId() == null) {
            return createCompteBanque(compteBanqueDTO);
        }
        CompteBanqueDTO result = compteBanqueService.save(compteBanqueDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, compteBanqueDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /compte-banques : get all the compteBanques.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of compteBanques in body
     */
    @GetMapping("/compte-banques")
    @Timed
    public ResponseEntity<List<CompteBanqueDTO>> getAllCompteBanques(Pageable pageable) {
        log.debug("REST request to get a page of CompteBanques");
        Page<CompteBanqueDTO> page = compteBanqueService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/compte-banques");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }


    /**
     * GET  /compte-banques/:id : get the "id" compteBanque.
     *
     * @param id the id of the compteBanqueDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the compteBanqueDTO, or with status 404 (Not Found)
     */
    @GetMapping("/compte-banques/{id}")
    @Timed
    public ResponseEntity<CompteBanqueDTO> getCompteBanque(@PathVariable Long id) {
        log.debug("REST request to get CompteBanque : {}", id);
        CompteBanqueDTO compteBanqueDTO = compteBanqueService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(compteBanqueDTO));
    }

    /**
     * DELETE  /compte-banques/:id : delete the "id" compteBanque.
     *
     * @param id the id of the compteBanqueDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/compte-banques/{id}")
    @Timed
    public ResponseEntity<Void> deleteCompteBanque(@PathVariable Long id) {
        log.debug("REST request to delete CompteBanque : {}", id);
        compteBanqueService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
