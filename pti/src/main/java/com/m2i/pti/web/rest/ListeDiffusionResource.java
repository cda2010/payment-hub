package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.ListeDiffusionService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.ListeDiffusionDTO;
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
 * REST controller for managing ListeDiffusion.
 */
@RestController
@RequestMapping("/api")
public class ListeDiffusionResource {

    private final Logger log = LoggerFactory.getLogger(ListeDiffusionResource.class);

    private static final String ENTITY_NAME = "listeDiffusion";

    private final ListeDiffusionService listeDiffusionService;

    public ListeDiffusionResource(ListeDiffusionService listeDiffusionService) {
        this.listeDiffusionService = listeDiffusionService;
    }

    /**
     * POST  /liste-diffusions : Create a new listeDiffusion.
     *
     * @param listeDiffusionDTO the listeDiffusionDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new listeDiffusionDTO, or with status 400 (Bad Request) if the listeDiffusion has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/liste-diffusions")
    @Timed
    public ResponseEntity<ListeDiffusionDTO> createListeDiffusion(@RequestBody ListeDiffusionDTO listeDiffusionDTO) throws URISyntaxException {
        log.debug("REST request to save ListeDiffusion : {}", listeDiffusionDTO);
        if (listeDiffusionDTO.getId() != null) {
            throw new BadRequestAlertException("A new listeDiffusion cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ListeDiffusionDTO result = listeDiffusionService.save(listeDiffusionDTO);
        return ResponseEntity.created(new URI("/api/liste-diffusions/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /liste-diffusions : Updates an existing listeDiffusion.
     *
     * @param listeDiffusionDTO the listeDiffusionDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated listeDiffusionDTO,
     * or with status 400 (Bad Request) if the listeDiffusionDTO is not valid,
     * or with status 500 (Internal Server Error) if the listeDiffusionDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/liste-diffusions")
    @Timed
    public ResponseEntity<ListeDiffusionDTO> updateListeDiffusion(@RequestBody ListeDiffusionDTO listeDiffusionDTO) throws URISyntaxException {
        log.debug("REST request to update ListeDiffusion : {}", listeDiffusionDTO);
        if (listeDiffusionDTO.getId() == null) {
            return createListeDiffusion(listeDiffusionDTO);
        }
        ListeDiffusionDTO result = listeDiffusionService.save(listeDiffusionDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, listeDiffusionDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /liste-diffusions : get all the listeDiffusions.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of listeDiffusions in body
     */
    @GetMapping("/liste-diffusions")
    @Timed
    public ResponseEntity<List<ListeDiffusionDTO>> getAllListeDiffusions(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of ListeDiffusions");
        Page<ListeDiffusionDTO> page = listeDiffusionService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/liste-diffusions");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /liste-diffusions/:id : get the "id" listeDiffusion.
     *
     * @param id the id of the listeDiffusionDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the listeDiffusionDTO, or with status 404 (Not Found)
     */
    @GetMapping("/liste-diffusions/{id}")
    @Timed
    public ResponseEntity<ListeDiffusionDTO> getListeDiffusion(@PathVariable Long id) {
        log.debug("REST request to get ListeDiffusion : {}", id);
        ListeDiffusionDTO listeDiffusionDTO = listeDiffusionService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(listeDiffusionDTO));
    }

    /**
     * DELETE  /liste-diffusions/:id : delete the "id" listeDiffusion.
     *
     * @param id the id of the listeDiffusionDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/liste-diffusions/{id}")
    @Timed
    public ResponseEntity<Void> deleteListeDiffusion(@PathVariable Long id) {
        log.debug("REST request to delete ListeDiffusion : {}", id);
        listeDiffusionService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
