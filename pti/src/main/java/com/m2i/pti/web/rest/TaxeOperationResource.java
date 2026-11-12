package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.TaxeOperationService;
import com.m2i.pti.service.dto.TaxeOperationDTO;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
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
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Taxe.
 */
@RestController
@RequestMapping("/api")
public class TaxeOperationResource {

    private final Logger log = LoggerFactory.getLogger(TaxeResource.class);

    private static final String ENTITY_NAME = "taxe";

    private final TaxeOperationService taxeOperationService;

    public TaxeOperationResource(TaxeOperationService taxeOperationService) {
        this.taxeOperationService = taxeOperationService;
    }

    /**
     * POST  /taxe-operations : Create a new taxeOperation.
     *
     * @param taxeOperationDTO the taxeOperationDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new taxeOperationDTO, or with status 400 (Bad Request) if the taxeOperation has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/taxe-operations")
    @Timed
    public ResponseEntity<TaxeOperationDTO> createTaxeOperation(@Valid @RequestBody TaxeOperationDTO taxeOperationDTO) throws URISyntaxException {
        log.debug("REST request to save TaxeOperation : {}", taxeOperationDTO);
        if (taxeOperationDTO.getId() != null) {
            throw new BadRequestAlertException("A new taxeOperation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TaxeOperationDTO result = taxeOperationService.save(taxeOperationDTO);
        return ResponseEntity.created(new URI("/api/taxe-operations/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }


    /**
     * PUT  /taxe-operations : Updates an existing taxeOperation.
     *
     * @param taxeOperationDTO the taxeOperationDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated taxeOperationDTO,
     * or with status 400 (Bad Request) if the taxeOperationDTO is not valid,
     * or with status 500 (Internal Server Error) if the taxeOperationDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/taxe-operations")
    @Timed
    public ResponseEntity<TaxeOperationDTO> updateTaxeOperation(@Valid @RequestBody TaxeOperationDTO taxeOperationDTO) throws URISyntaxException {
        log.debug("REST request to update TaxeOperation : {}", taxeOperationDTO);
        if (taxeOperationDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        TaxeOperationDTO result = taxeOperationService.save(taxeOperationDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, taxeOperationDTO.getId().toString()))
                .body(result);
    }

    /**
     * GET  /taxe-operations : get all the taxeOperations.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of taxeOperations in body
     */
    @GetMapping("/taxe-operations")
    @Timed
    public ResponseEntity<List<TaxeOperationDTO>> getAllTaxeOperations(Pageable pageable) {
        log.debug("REST request to get a page of TaxeOperations");
        Page<TaxeOperationDTO> page = taxeOperationService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/taxe-operations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * Service de calcul des Taxes
     * @param organisationId
     * @param service
     * @param montantOp
     * @param montantFrais
     * @return
     */
    @GetMapping("/taxe-operations/findByTaxe")
    @Timed
    public List<TaxeOperationDTO> getAllTaxeOperationsByTypeService(@RequestParam()Long organisationId, @RequestParam String service, @RequestParam BigDecimal montantOp,@RequestParam BigDecimal montantFrais) {
        log.debug("REST request to get a page of TaxeOperations");
        return taxeOperationService.searchTaxeOperation(organisationId,service,montantFrais,montantOp,null);
    }

    /**
     * GET  /taxe-operations/:id : get the "id" taxeOperation.
     *
     * @param id the id of the taxeOperationDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the taxeOperationDTO, or with status 404 (Not Found)
     */
    @GetMapping("/taxe-operations/{id}")
    @Timed
    public ResponseEntity<TaxeOperationDTO> getTaxeOperation(@PathVariable Long id) {
        log.debug("REST request to get TaxeOperation : {}", id);
        Optional<TaxeOperationDTO> taxeOperationDTO = taxeOperationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(taxeOperationDTO);
    }

    /**
     * DELETE  /taxe-operations/:id : delete the "id" taxeOperation.
     *
     * @param id the id of the taxeOperationDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/taxe-operations/{id}")
    @Timed
    public ResponseEntity<Void> deleteTaxeOperation(@PathVariable Long id) {
        log.debug("REST request to delete TaxeOperation : {}", id);
        taxeOperationService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}