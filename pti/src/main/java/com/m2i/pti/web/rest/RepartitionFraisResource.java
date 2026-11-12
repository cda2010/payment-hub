package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.RepartitionFrais;
import com.m2i.pti.domain.enumeration.Acteur;
import com.m2i.pti.service.RepartitionFraisService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.RepartitionFraisDTO;
import io.swagger.annotations.ApiParam;
import io.github.jhipster.web.util.ResponseUtil;
import java.math.BigDecimal;
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
 * REST controller for managing RepartitionFrais.
 */
@RestController
@RequestMapping("/api")
public class RepartitionFraisResource {

    private final Logger log = LoggerFactory.getLogger(RepartitionFraisResource.class);

    private static final String ENTITY_NAME = "repartitionFrais";

    private final RepartitionFraisService repartitionFraisService;

    public RepartitionFraisResource(RepartitionFraisService repartitionFraisService) {
        this.repartitionFraisService = repartitionFraisService;
    }

    /**
     * POST  /repartition-frais : Create a new repartitionFrais.
     *
     * @param repartitionFraisDTO the repartitionFraisDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new repartitionFraisDTO, or with status 400 (Bad Request) if the repartitionFrais has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/repartition-frais")
    @Timed
    public ResponseEntity<RepartitionFraisDTO> createRepartitionFrais(@Valid @RequestBody RepartitionFraisDTO repartitionFraisDTO) throws URISyntaxException {
        log.debug("REST request to save RepartitionFrais : {}", repartitionFraisDTO);
        if (repartitionFraisDTO.getId() != null) {
            throw new BadRequestAlertException("A new repartitionFrais cannot already have an ID", ENTITY_NAME, "idexists");
        }
        RepartitionFraisDTO result = repartitionFraisService.save(repartitionFraisDTO);
        return ResponseEntity.created(new URI("/api/repartition-frais/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT  /repartition-frais : Updates an existing repartitionFrais.
     *
     * @param repartitionFraisDTO the repartitionFraisDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated repartitionFraisDTO,
     * or with status 400 (Bad Request) if the repartitionFraisDTO is not valid,
     * or with status 500 (Internal Server Error) if the repartitionFraisDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/repartition-frais")
    @Timed
    public ResponseEntity<RepartitionFraisDTO> updateRepartitionFrais(@Valid @RequestBody RepartitionFraisDTO repartitionFraisDTO) throws URISyntaxException {
        log.debug("REST request to update RepartitionFrais : {}", repartitionFraisDTO);
        if (repartitionFraisDTO.getId() == null) {
            return createRepartitionFrais(repartitionFraisDTO);
        }
        RepartitionFraisDTO result = repartitionFraisService.save(repartitionFraisDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, repartitionFraisDTO.getId().toString()))
                .body(result);
    }

    /**
     * GET  /repartition-frais : get all the repartitionFrais.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of repartitionFrais in body
     */
    @GetMapping("/repartition-frais")
    @Timed
    public ResponseEntity<List<RepartitionFraisDTO>> getAllRepartitionFrais(@ApiParam Pageable pageable, @RequestParam(required = false) Long typeServiceId, @RequestParam(required = false) Long fseSourceId, @RequestParam(required = false) Long fseDestId, @RequestParam(required = false)Acteur acteur) {
        log.debug("REST request to get a page of RepartitionFrais");
        Page<RepartitionFraisDTO> page = repartitionFraisService.findAll(pageable,  typeServiceId, fseSourceId,fseDestId,acteur);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/repartition-frais");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /repartition-frais/:id : get the "id" repartitionFrais.
     *
     * @param id the id of the repartitionFraisDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the repartitionFraisDTO, or with status 404 (Not Found)
     */
    @GetMapping("/repartition-frais/{id}")
    @Timed
    public ResponseEntity<RepartitionFraisDTO> getRepartitionFrais(@PathVariable Long id) {
        log.debug("REST request to get RepartitionFrais : {}", id);
        RepartitionFraisDTO repartitionFraisDTO = repartitionFraisService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(repartitionFraisDTO));
    }

    /**
     * DELETE  /repartition-frais/:id : delete the "id" repartitionFrais.
     *
     * @param id the id of the repartitionFraisDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/repartition-frais/{id}")
    @Timed
    public ResponseEntity<Void> deleteRepartitionFrais(@PathVariable Long id) {
        log.debug("REST request to delete RepartitionFrais : {}", id);
        repartitionFraisService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
    
    
    @GetMapping("/repartition-fraisx")
    @Timed
    public RepartitionFrais getAllRepartitionFraisX( @RequestParam(required = false) Long typeServiceId, @RequestParam(required = false) Long fseSourceId, @RequestParam(required = false) Long fseDestId, @RequestParam(required = false)Acteur acteur, @RequestParam(required = false)Double montantop, @RequestParam(required = false)Double montantf) {
        log.debug("REST request to get a page of RepartitionFrais");
        RepartitionFrais page = repartitionFraisService.findByCriteriaAndFse( typeServiceId, acteur,  BigDecimal.valueOf(montantf),  BigDecimal.valueOf(montantop),  fseSourceId,  fseDestId);
        
        return page;
    }
}
