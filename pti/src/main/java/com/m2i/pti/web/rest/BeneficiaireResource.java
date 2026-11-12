package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.BeneficiaireService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.BeneficiaireDTO;
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
 * REST controller for managing Beneficiaire.
 */
@RestController
@RequestMapping("/api")
public class BeneficiaireResource {

    private final Logger log = LoggerFactory.getLogger(BeneficiaireResource.class);

    private static final String ENTITY_NAME = "beneficiaire";

    private final BeneficiaireService beneficiaireService;

    public BeneficiaireResource(BeneficiaireService beneficiaireService) {
        this.beneficiaireService = beneficiaireService;
    }

    /**
     * POST  /beneficiaires : Create a new beneficiaire.
     *
     * @param beneficiaireDTO the beneficiaireDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new beneficiaireDTO, or with status 400 (Bad Request) if the beneficiaire has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/beneficiaires")
    @Timed
    public ResponseEntity<BeneficiaireDTO> createBeneficiaire(@RequestBody BeneficiaireDTO beneficiaireDTO) throws URISyntaxException {
        log.debug("REST request to save Beneficiaire : {}", beneficiaireDTO);
        if (beneficiaireDTO.getId() != null) {
            throw new BadRequestAlertException("A new beneficiaire cannot already have an ID", ENTITY_NAME, "idexists");
        }
        BeneficiaireDTO result = beneficiaireService.save(beneficiaireDTO);
        return ResponseEntity.created(new URI("/api/beneficiaires/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /beneficiaires : Updates an existing beneficiaire.
     *
     * @param beneficiaireDTO the beneficiaireDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated beneficiaireDTO,
     * or with status 400 (Bad Request) if the beneficiaireDTO is not valid,
     * or with status 500 (Internal Server Error) if the beneficiaireDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/beneficiaires")
    @Timed
    public ResponseEntity<BeneficiaireDTO> updateBeneficiaire(@RequestBody BeneficiaireDTO beneficiaireDTO) throws URISyntaxException {
        log.debug("REST request to update Beneficiaire : {}", beneficiaireDTO);
        if (beneficiaireDTO.getId() == null) {
            return createBeneficiaire(beneficiaireDTO);
        }
        BeneficiaireDTO result = beneficiaireService.save(beneficiaireDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, beneficiaireDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /beneficiaires : get all the beneficiaires.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of beneficiaires in body
     */
    @GetMapping("/beneficiaires")
    @Timed
    public ResponseEntity<List<BeneficiaireDTO>> getAllBeneficiaires(Pageable pageable) {
        log.debug("REST request to get a page of Beneficiaires");
        Page<BeneficiaireDTO> page = beneficiaireService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/beneficiaires");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /beneficiaires/:id : get the "id" beneficiaire.
     *
     * @param id the id of the beneficiaireDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the beneficiaireDTO, or with status 404 (Not Found)
     */
    @GetMapping("/beneficiaires/{id}")
    @Timed
    public ResponseEntity<BeneficiaireDTO> getBeneficiaire(@PathVariable Long id) {
        log.debug("REST request to get Beneficiaire : {}", id);
        BeneficiaireDTO beneficiaireDTO = beneficiaireService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(beneficiaireDTO));
    }

    /**
     * DELETE  /beneficiaires/:id : delete the "id" beneficiaire.
     *
     * @param id the id of the beneficiaireDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/beneficiaires/{id}")
    @Timed
    public ResponseEntity<Void> deleteBeneficiaire(@PathVariable Long id) {
        log.debug("REST request to delete Beneficiaire : {}", id);
        beneficiaireService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
