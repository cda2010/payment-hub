package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.BilanTaxeService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.service.dto.BilanTaxeDTO;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing BilanTaxe.
 */
@RestController
@RequestMapping("/api")
public class BilanTaxeResource {

    private final Logger log = LoggerFactory.getLogger(BilanTaxeResource.class);

    private static final String ENTITY_NAME = "bilanTaxe";

    private BilanTaxeService bilanTaxeService;

    public BilanTaxeResource(BilanTaxeService bilanTaxeService) {
        this.bilanTaxeService = bilanTaxeService;
    }

    /**
     * POST  /bilan-taxes : Create a new bilanTaxe.
     *
     * @param bilanTaxeDTO the bilanTaxeDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new bilanTaxeDTO, or with status 400 (Bad Request) if the bilanTaxe has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/bilan-taxes")
    @Timed
    public ResponseEntity<BilanTaxeDTO> createBilanTaxe(@RequestBody BilanTaxeDTO bilanTaxeDTO) throws URISyntaxException {
        log.debug("REST request to save BilanTaxe : {}", bilanTaxeDTO);
        if (bilanTaxeDTO.getId() != null) {
            throw new BadRequestAlertException("A new bilanTaxe cannot already have an ID", ENTITY_NAME, "idexists");
        }
        BilanTaxeDTO result = bilanTaxeService.save(bilanTaxeDTO);
        return ResponseEntity.created(new URI("/api/bilan-taxes/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT  /bilan-taxes : Updates an existing bilanTaxe.
     *
     * @param bilanTaxeDTO the bilanTaxeDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated bilanTaxeDTO,
     * or with status 400 (Bad Request) if the bilanTaxeDTO is not valid,
     * or with status 500 (Internal Server Error) if the bilanTaxeDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/bilan-taxes")
    @Timed
    public ResponseEntity<BilanTaxeDTO> updateBilanTaxe(@RequestBody BilanTaxeDTO bilanTaxeDTO) throws URISyntaxException {
        log.debug("REST request to update BilanTaxe : {}", bilanTaxeDTO);
        if (bilanTaxeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        BilanTaxeDTO result = bilanTaxeService.save(bilanTaxeDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, bilanTaxeDTO.getId().toString()))
                .body(result);
    }

    /**
     * GET  /bilan-taxes : get all the bilanTaxes.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of bilanTaxes in body
     */
    @GetMapping("/bilan-taxes")
    @Timed
    public List<BilanTaxeDTO> getAllBilanTaxes() {
        log.debug("REST request to get all BilanTaxes");
        return bilanTaxeService.findAll();
    }

    /**
     * GET  /bilan-taxes/:id : get the "id" bilanTaxe.
     *
     * @param id the id of the bilanTaxeDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the bilanTaxeDTO, or with status 404 (Not Found)
     */
    @GetMapping("/bilan-taxes/{id}")
    @Timed
    public ResponseEntity<BilanTaxeDTO> getBilanTaxe(@PathVariable Long id) {
        log.debug("REST request to get BilanTaxe : {}", id);
        BilanTaxeDTO bilanTaxeDTO = bilanTaxeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(bilanTaxeDTO));
    }

    /**
     * DELETE  /bilan-taxes/:id : delete the "id" bilanTaxe.
     *
     * @param id the id of the bilanTaxeDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/bilan-taxes/{id}")
    @Timed
    public ResponseEntity<Void> deleteBilanTaxe(@PathVariable Long id) {
        log.debug("REST request to delete BilanTaxe : {}", id);
        bilanTaxeService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
