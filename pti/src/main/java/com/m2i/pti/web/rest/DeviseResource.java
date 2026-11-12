package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.DeviseService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.DeviseDTO;
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
 * REST controller for managing Devise.
 */
@RestController
@RequestMapping("/api")
public class DeviseResource {

    private final Logger log = LoggerFactory.getLogger(DeviseResource.class);

    private static final String ENTITY_NAME = "devise";

    private final DeviseService deviseService;

    public DeviseResource(DeviseService deviseService) {
        this.deviseService = deviseService;
    }

    /**
     * POST  /devises : Create a new devise.
     *
     * @param deviseDTO the deviseDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new deviseDTO, or with status 400 (Bad Request) if the devise has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/devises")
    @Timed
    public ResponseEntity<DeviseDTO> createDevise(@Valid @RequestBody DeviseDTO deviseDTO) throws URISyntaxException {
        log.debug("REST request to save Devise : {}", deviseDTO);
        if (deviseDTO.getId() != null) {
            throw new BadRequestAlertException("A new devise cannot already have an ID", ENTITY_NAME, "idexists");
        }
        DeviseDTO result = deviseService.save(deviseDTO);
        return ResponseEntity.created(new URI("/api/devises/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /devises : Updates an existing devise.
     *
     * @param deviseDTO the deviseDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated deviseDTO,
     * or with status 400 (Bad Request) if the deviseDTO is not valid,
     * or with status 500 (Internal Server Error) if the deviseDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/devises")
    @Timed
    public ResponseEntity<DeviseDTO> updateDevise(@Valid @RequestBody DeviseDTO deviseDTO) throws URISyntaxException {
        log.debug("REST request to update Devise : {}", deviseDTO);
        if (deviseDTO.getId() == null) {
            return createDevise(deviseDTO);
        }
        DeviseDTO result = deviseService.save(deviseDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, deviseDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /devises : get all the devises.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of devises in body
     */
    @GetMapping("/devises")
    @Timed
    public ResponseEntity<List<DeviseDTO>> getAllDevises(Pageable pageable) {
        log.debug("REST request to get a page of Devises");
        Page<DeviseDTO> page = deviseService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/devises");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /devises/:id : get the "id" devise.
     *
     * @param id the id of the deviseDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the deviseDTO, or with status 404 (Not Found)
     */
    @GetMapping("/devises/{id}")
    @Timed
    public ResponseEntity<DeviseDTO> getDevise(@PathVariable Long id) {
        log.debug("REST request to get Devise : {}", id);
        DeviseDTO deviseDTO = deviseService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(deviseDTO));
    }

    /**
     * DELETE  /devises/:id : delete the "id" devise.
     *
     * @param id the id of the deviseDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/devises/{id}")
    @Timed
    public ResponseEntity<Void> deleteDevise(@PathVariable Long id) {
        log.debug("REST request to delete Devise : {}", id);
        deviseService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
