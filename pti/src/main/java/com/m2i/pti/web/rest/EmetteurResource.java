package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.EmetteurService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.EmetteurDTO;
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
 * REST controller for managing Emetteur.
 */
@RestController
@RequestMapping("/api")
public class EmetteurResource {

    private final Logger log = LoggerFactory.getLogger(EmetteurResource.class);

    private static final String ENTITY_NAME = "emetteur";

    private final EmetteurService emetteurService;

    public EmetteurResource(EmetteurService emetteurService) {
        this.emetteurService = emetteurService;
    }

    /**
     * POST  /emetteurs : Create a new emetteur.
     *
     * @param emetteurDTO the emetteurDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new emetteurDTO, or with status 400 (Bad Request) if the emetteur has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/emetteurs")
    @Timed
    public ResponseEntity<EmetteurDTO> createEmetteur(@RequestBody EmetteurDTO emetteurDTO) throws URISyntaxException {
        log.debug("REST request to save Emetteur : {}", emetteurDTO);
        if (emetteurDTO.getId() != null) {
            throw new BadRequestAlertException("A new emetteur cannot already have an ID", ENTITY_NAME, "idexists");
        }
        EmetteurDTO result = emetteurService.save(emetteurDTO);
        return ResponseEntity.created(new URI("/api/emetteurs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /emetteurs : Updates an existing emetteur.
     *
     * @param emetteurDTO the emetteurDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated emetteurDTO,
     * or with status 400 (Bad Request) if the emetteurDTO is not valid,
     * or with status 500 (Internal Server Error) if the emetteurDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/emetteurs")
    @Timed
    public ResponseEntity<EmetteurDTO> updateEmetteur(@RequestBody EmetteurDTO emetteurDTO) throws URISyntaxException {
        log.debug("REST request to update Emetteur : {}", emetteurDTO);
        if (emetteurDTO.getId() == null) {
            return createEmetteur(emetteurDTO);
        }
        EmetteurDTO result = emetteurService.save(emetteurDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, emetteurDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /emetteurs : get all the emetteurs.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of emetteurs in body
     */
    @GetMapping("/emetteurs")
    @Timed
    public ResponseEntity<List<EmetteurDTO>> getAllEmetteurs(Pageable pageable) {
        log.debug("REST request to get a page of Emetteurs");
        Page<EmetteurDTO> page = emetteurService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/emetteurs");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /emetteurs/:id : get the "id" emetteur.
     *
     * @param id the id of the emetteurDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the emetteurDTO, or with status 404 (Not Found)
     */
    @GetMapping("/emetteurs/{id}")
    @Timed
    public ResponseEntity<EmetteurDTO> getEmetteur(@PathVariable Long id) {
        log.debug("REST request to get Emetteur : {}", id);
        EmetteurDTO emetteurDTO = emetteurService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(emetteurDTO));
    }

    /**
     * DELETE  /emetteurs/:id : delete the "id" emetteur.
     *
     * @param id the id of the emetteurDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/emetteurs/{id}")
    @Timed
    public ResponseEntity<Void> deleteEmetteur(@PathVariable Long id) {
        log.debug("REST request to delete Emetteur : {}", id);
        emetteurService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
