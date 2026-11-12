package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.FournisseurServiceService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.FournisseurServiceDTO;
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
 * REST controller for managing FournisseurService.
 */
@RestController
@RequestMapping("/api")
public class FournisseurServiceResource {

    private final Logger log = LoggerFactory.getLogger(FournisseurServiceResource.class);

    private static final String ENTITY_NAME = "fournisseurService";

    private final FournisseurServiceService fournisseurServiceService;

    public FournisseurServiceResource(FournisseurServiceService fournisseurServiceService) {
        this.fournisseurServiceService = fournisseurServiceService;
    }

    /**
     * POST  /fournisseur-services : Create a new fournisseurService.
     *
     * @param fournisseurServiceDTO the fournisseurServiceDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new fournisseurServiceDTO, or with status 400 (Bad Request) if the fournisseurService has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/fournisseur-services")
    @Timed
    public ResponseEntity<FournisseurServiceDTO> createFournisseurService(@Valid @RequestBody FournisseurServiceDTO fournisseurServiceDTO) throws URISyntaxException {
        log.debug("REST request to save FournisseurService : {}", fournisseurServiceDTO);
        if (fournisseurServiceDTO.getId() != null) {
            throw new BadRequestAlertException("A new fournisseurService cannot already have an ID", ENTITY_NAME, "idexists");
        }
        FournisseurServiceDTO result = fournisseurServiceService.save(fournisseurServiceDTO);
        return ResponseEntity.created(new URI("/api/fournisseur-services/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /fournisseur-services : Updates an existing fournisseurService.
     *
     * @param fournisseurServiceDTO the fournisseurServiceDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated fournisseurServiceDTO,
     * or with status 400 (Bad Request) if the fournisseurServiceDTO is not valid,
     * or with status 500 (Internal Server Error) if the fournisseurServiceDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/fournisseur-services")
    @Timed
    public ResponseEntity<FournisseurServiceDTO> updateFournisseurService(@Valid @RequestBody FournisseurServiceDTO fournisseurServiceDTO) throws URISyntaxException {
        log.debug("REST request to update FournisseurService : {}", fournisseurServiceDTO);
        if (fournisseurServiceDTO.getId() == null) {
            return createFournisseurService(fournisseurServiceDTO);
        }
        FournisseurServiceDTO result = fournisseurServiceService.save(fournisseurServiceDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, fournisseurServiceDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /fournisseur-services : get all the fournisseurServices.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of fournisseurServices in body
     */
    @GetMapping("/fournisseur-services")
    @Timed
    public ResponseEntity<List<FournisseurServiceDTO>> getAllFournisseurServices(Pageable pageable) {
        log.debug("REST request to get a page of FournisseurServices");
        Page<FournisseurServiceDTO> page = fournisseurServiceService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/fournisseur-services");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /fournisseur-services/:id : get the "id" fournisseurService.
     *
     * @param id the id of the fournisseurServiceDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the fournisseurServiceDTO, or with status 404 (Not Found)
     */
    @GetMapping("/fournisseur-services/{id}")
    @Timed
    public ResponseEntity<FournisseurServiceDTO> getFournisseurService(@PathVariable Long id) {
        log.debug("REST request to get FournisseurService : {}", id);
        FournisseurServiceDTO fournisseurServiceDTO = fournisseurServiceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(fournisseurServiceDTO));
    }

    /**
     * DELETE  /fournisseur-services/:id : delete the "id" fournisseurService.
     *
     * @param id the id of the fournisseurServiceDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/fournisseur-services/{id}")
    @Timed
    public ResponseEntity<Void> deleteFournisseurService(@PathVariable Long id) {
        log.debug("REST request to delete FournisseurService : {}", id);
        fournisseurServiceService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
