package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.LocaliteService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.LocaliteDTO;
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
 * REST controller for managing Localite.
 */
@RestController
@RequestMapping("/api")
public class LocaliteResource {

    private final Logger log = LoggerFactory.getLogger(LocaliteResource.class);

    private static final String ENTITY_NAME = "localite";

    private final LocaliteService localiteService;

    public LocaliteResource(LocaliteService localiteService) {
        this.localiteService = localiteService;
    }

    /**
     * POST  /localites : Create a new localite.
     *
     * @param localiteDTO the localiteDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new localiteDTO, or with status 400 (Bad Request) if the localite has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/localites")
    @Timed
    public ResponseEntity<LocaliteDTO> createLocalite(@RequestBody LocaliteDTO localiteDTO) throws URISyntaxException {
        log.debug("REST request to save Localite : {}", localiteDTO);
        if (localiteDTO.getId() != null) {
            throw new BadRequestAlertException("A new localite cannot already have an ID", ENTITY_NAME, "idexists");
        }
        LocaliteDTO result = localiteService.save(localiteDTO);
        return ResponseEntity.created(new URI("/api/localites/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /localites : Updates an existing localite.
     *
     * @param localiteDTO the localiteDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated localiteDTO,
     * or with status 400 (Bad Request) if the localiteDTO is not valid,
     * or with status 500 (Internal Server Error) if the localiteDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/localites")
    @Timed
    public ResponseEntity<LocaliteDTO> updateLocalite(@RequestBody LocaliteDTO localiteDTO) throws URISyntaxException {
        log.debug("REST request to update Localite : {}", localiteDTO);
        if (localiteDTO.getId() == null) {
            return createLocalite(localiteDTO);
        }
        LocaliteDTO result = localiteService.save(localiteDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, localiteDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /localites : get all the localites.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of localites in body
     */
    @GetMapping("/localites")
    @Timed
    public ResponseEntity<List<LocaliteDTO>> getAllLocalites(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of Localites");
        Page<LocaliteDTO> page = localiteService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/localites");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /localites/:id : get the "id" localite.
     *
     * @param id the id of the localiteDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the localiteDTO, or with status 404 (Not Found)
     */
    @GetMapping("/localites/{id}")
    @Timed
    public ResponseEntity<LocaliteDTO> getLocalite(@PathVariable Long id) {
        log.debug("REST request to get Localite : {}", id);
        LocaliteDTO localiteDTO = localiteService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(localiteDTO));
    }

    /**
     * DELETE  /localites/:id : delete the "id" localite.
     *
     * @param id the id of the localiteDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/localites/{id}")
    @Timed
    public ResponseEntity<Void> deleteLocalite(@PathVariable Long id) {
        log.debug("REST request to delete Localite : {}", id);
        localiteService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
