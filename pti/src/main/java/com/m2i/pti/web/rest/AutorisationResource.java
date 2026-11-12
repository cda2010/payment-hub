package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.AutorisationService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.AutorisationDTO;
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
 * REST controller for managing Autorisation.
 */
@RestController
@RequestMapping("/api")
public class AutorisationResource {

    private final Logger log = LoggerFactory.getLogger(AutorisationResource.class);

    private static final String ENTITY_NAME = "autorisation";

    private final AutorisationService autorisationService;

    public AutorisationResource(AutorisationService autorisationService) {
        this.autorisationService = autorisationService;
    }

    /**
     * POST  /autorisations : Create a new autorisation.
     *
     * @param autorisationDTO the autorisationDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new autorisationDTO, or with status 400 (Bad Request) if the autorisation has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/autorisations")
    @Timed
    public ResponseEntity<AutorisationDTO> createAutorisation(@RequestBody AutorisationDTO autorisationDTO) throws URISyntaxException {
        log.debug("REST request to save Autorisation : {}", autorisationDTO);
        if (autorisationDTO.getId() != null) {
            throw new BadRequestAlertException("A new autorisation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        AutorisationDTO result = autorisationService.save(autorisationDTO);
        return ResponseEntity.created(new URI("/api/autorisations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /autorisations : Updates an existing autorisation.
     *
     * @param autorisationDTO the autorisationDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated autorisationDTO,
     * or with status 400 (Bad Request) if the autorisationDTO is not valid,
     * or with status 500 (Internal Server Error) if the autorisationDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/autorisations")
    @Timed
    public ResponseEntity<AutorisationDTO> updateAutorisation(@RequestBody AutorisationDTO autorisationDTO) throws URISyntaxException {
        log.debug("REST request to update Autorisation : {}", autorisationDTO);
        if (autorisationDTO.getId() == null) {
            return createAutorisation(autorisationDTO);
        }
        AutorisationDTO result = autorisationService.save(autorisationDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, autorisationDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /autorisations : get  the autorisations.
     *
     * @param uoId  information
     * @return the ResponseEntity with status 200 (OK) and the list of autorisations in body
     */
    @GetMapping("/autorisationsByUO/{uoId}")
    @Timed
    public List<AutorisationDTO> getAutorisationsByUO(@PathVariable Long uoId) {
        log.debug("REST request to get a page of Autorisations");
      List<AutorisationDTO> autorisationDTOS = autorisationService.findByUO(uoId);

        return autorisationDTOS;
    }

    /**
     * Liste des autorisation d'un client donnné
     * @param clientId
     * @return
     */
    @GetMapping("/autorisations/byClient")
    @Timed
    public List<AutorisationDTO> getAutorisationsByClient(@RequestParam Long clientId) {
        log.debug("REST request to get a page of Autorisations");
        List<AutorisationDTO> autorisationDTOS = autorisationService.findByClient(clientId);

        return autorisationDTOS;
    }

    /**
     * GET  /autorisations : get all the autorisations.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of autorisations in body
     */
    @GetMapping("/autorisations")
    @Timed
    public ResponseEntity<List<AutorisationDTO>> getAllAutorisations(Pageable pageable) {
        log.debug("REST request to get a page of Autorisations");
        Page<AutorisationDTO> page = autorisationService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/autorisations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /autorisations/:id : get the "id" autorisation.
     *
     * @param id the id of the autorisationDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the autorisationDTO, or with status 404 (Not Found)
     */
    @GetMapping("/autorisations/{id}")
    @Timed
    public ResponseEntity<AutorisationDTO> getAutorisation(@PathVariable Long id) {
        log.debug("REST request to get Autorisation : {}", id);
        AutorisationDTO autorisationDTO = autorisationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(autorisationDTO));
    }

    /**
     * DELETE  /autorisations/:id : delete the "id" autorisation.
     *
     * @param id the id of the autorisationDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/autorisations/{id}")
    @Timed
    public ResponseEntity<Void> deleteAutorisation(@PathVariable Long id) {
        log.debug("REST request to delete Autorisation : {}", id);
        autorisationService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
