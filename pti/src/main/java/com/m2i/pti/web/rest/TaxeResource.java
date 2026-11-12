package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.TaxeService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.TaxeDTO;
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
 * REST controller for managing Taxe.
 */
@RestController
@RequestMapping("/api")
public class TaxeResource {

    private final Logger log = LoggerFactory.getLogger(TaxeResource.class);

    private static final String ENTITY_NAME = "taxe";

    private TaxeService taxeService;

    public TaxeResource(TaxeService taxeService) {
        this.taxeService = taxeService;
    }

    /**
     * POST  /taxes : Create a new taxe.
     *
     * @param taxeDTO the taxeDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new taxeDTO, or with status 400 (Bad Request) if the taxe has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/taxes")
    @Timed
    public ResponseEntity<TaxeDTO> createTaxe(@Valid @RequestBody TaxeDTO taxeDTO) throws URISyntaxException {
        log.debug("REST request to save Taxe : {}", taxeDTO);
        if (taxeDTO.getId() != null) {
            throw new BadRequestAlertException("A new taxe cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TaxeDTO result = taxeService.save(taxeDTO);
        return ResponseEntity.created(new URI("/api/taxes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /taxes : Updates an existing taxe.
     *
     * @param taxeDTO the taxeDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated taxeDTO,
     * or with status 400 (Bad Request) if the taxeDTO is not valid,
     * or with status 500 (Internal Server Error) if the taxeDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/taxes")
    @Timed
    public ResponseEntity<TaxeDTO> updateTaxe(@Valid @RequestBody TaxeDTO taxeDTO) throws URISyntaxException {
        log.debug("REST request to update Taxe : {}", taxeDTO);
        if (taxeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        TaxeDTO result = taxeService.save(taxeDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, taxeDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /taxes : get all the taxes.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of taxes in body
     */
    @GetMapping("/taxes")
    @Timed
    public ResponseEntity<List<TaxeDTO>> getAllTaxes(Pageable pageable) {
        log.debug("REST request to get a page of Taxes");
        Page<TaxeDTO> page = taxeService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/taxes");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /taxes/:id : get the "id" taxe.
     *
     * @param id the id of the taxeDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the taxeDTO, or with status 404 (Not Found)
     */
    @GetMapping("/taxes/{id}")
    @Timed
    public ResponseEntity<TaxeDTO> getTaxe(@PathVariable Long id) {
        log.debug("REST request to get Taxe : {}", id);
        TaxeDTO taxeDTO = taxeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(taxeDTO));
    }

    /**
     * DELETE  /taxes/:id : delete the "id" taxe.
     *
     * @param id the id of the taxeDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/taxes/{id}")
    @Timed
    public ResponseEntity<Void> deleteTaxe(@PathVariable Long id) {
        log.debug("REST request to delete Taxe : {}", id);
        taxeService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
