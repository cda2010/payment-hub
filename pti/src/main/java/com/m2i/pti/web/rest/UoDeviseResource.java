package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.UoDeviseService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.UoDeviseDTO;
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
 * REST controller for managing UoDevise.
 */
@RestController
@RequestMapping("/api")
public class UoDeviseResource {

    private final Logger log = LoggerFactory.getLogger(UoDeviseResource.class);

    private static final String ENTITY_NAME = "uoDevise";

    private final UoDeviseService uoDeviseService;

    public UoDeviseResource(UoDeviseService uoDeviseService) {
        this.uoDeviseService = uoDeviseService;
    }

    /**
     * POST  /uo-devises : Create a new uoDevise.
     *
     * @param uoDeviseDTO the uoDeviseDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new uoDeviseDTO, or with status 400 (Bad Request) if the uoDevise has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/uo-devises")
    @Timed
    public ResponseEntity<UoDeviseDTO> createUoDevise(@RequestBody UoDeviseDTO uoDeviseDTO) throws URISyntaxException {
        log.debug("REST request to save UoDevise : {}", uoDeviseDTO);
        if (uoDeviseDTO.getId() != null) {
            throw new BadRequestAlertException("A new uoDevise cannot already have an ID", ENTITY_NAME, "idexists");
        }
        UoDeviseDTO result = uoDeviseService.save(uoDeviseDTO);
        return ResponseEntity.created(new URI("/api/uo-devises/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT  /uo-devises : Updates an existing uoDevise.
     *
     * @param uoDeviseDTO the uoDeviseDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated uoDeviseDTO,
     * or with status 400 (Bad Request) if the uoDeviseDTO is not valid,
     * or with status 500 (Internal Server Error) if the uoDeviseDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/uo-devises")
    @Timed
    public ResponseEntity<UoDeviseDTO> updateUoDevise(@RequestBody UoDeviseDTO uoDeviseDTO) throws URISyntaxException {
        log.debug("REST request to update UoDevise : {}", uoDeviseDTO);
        if (uoDeviseDTO.getId() == null) {
            return createUoDevise(uoDeviseDTO);
        }
        UoDeviseDTO result = uoDeviseService.save(uoDeviseDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, uoDeviseDTO.getId().toString()))
                .body(result);
    }

    /**
     * GET  /uo-devises : get all the uoDevises.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of uoDevises in body
     */
    @GetMapping("/uo-devises")
    @Timed
    public ResponseEntity<List<UoDeviseDTO>> getAllUoDevises(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of UoDevises");
        Page<UoDeviseDTO> page = uoDeviseService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/uo-devises");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/uo-devises/byOrganisation")
    @Timed
    public List<UoDeviseDTO> getAllUoDevisesByOrganisation(@RequestParam Long organisationId) {
        log.debug("REST request to get a page of UoDevises");
        return uoDeviseService.findByActeur(organisationId);
    }


    @GetMapping("/uo-devises/defaultSrc")
    @Timed
    public UoDeviseDTO getDefaultSrcUoDevice() {
        log.debug("REST request to get a page of UoDevises");
        return uoDeviseService.findByActeurConnecte();
    }


    @GetMapping("/uo-devises/defaultByOrg")
    @Timed
    public UoDeviseDTO getDefaultUoDevice(@RequestParam Long organisationId) {
        log.debug("REST request to get a page of UoDevises");
        log.debug("Default|||");
        return uoDeviseService.findByActeurDefault(organisationId);
    }

    /**
     * GET  /uo-devises/:id : get the "id" uoDevise.
     *
     * @param id the id of the uoDeviseDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the uoDeviseDTO, or with status 404 (Not Found)
     */
    @GetMapping("/uo-devises/{id}")
    @Timed
    public ResponseEntity<UoDeviseDTO> getUoDevise(@PathVariable Long id) {
        log.debug("REST request to get UoDevise : {}", id);
        UoDeviseDTO uoDeviseDTO = uoDeviseService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(uoDeviseDTO));
    }

    /**
     * DELETE  /uo-devises/:id : delete the "id" uoDevise.
     *
     * @param id the id of the uoDeviseDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/uo-devises/{id}")
    @Timed
    public ResponseEntity<Void> deleteUoDevise(@PathVariable Long id) {
        log.debug("REST request to delete UoDevise : {}", id);
        uoDeviseService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
