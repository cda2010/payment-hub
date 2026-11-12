package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.service.ProduitCanalService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.service.dto.ProduitCanalDTO;
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
 * REST controller for managing ProduitCanal.
 */
@RestController
@RequestMapping("/api")
public class ProduitCanalResource {

    private final Logger log = LoggerFactory.getLogger(ProduitCanalResource.class);

    private static final String ENTITY_NAME = "produitCanal";

    private final ProduitCanalService produitCanalService;

    public ProduitCanalResource(ProduitCanalService produitCanalService) {
        this.produitCanalService = produitCanalService;
    }

    /**
     * POST  /produit-canals : Create a new produitCanal.
     *
     * @param produitCanalDTO the produitCanalDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new produitCanalDTO, or with status 400 (Bad Request) if the produitCanal has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/produit-canals")
    @Timed
    public ResponseEntity<ProduitCanalDTO> createProduitCanal(@RequestBody ProduitCanalDTO produitCanalDTO) throws URISyntaxException {
        log.debug("REST request to save ProduitCanal : {}", produitCanalDTO);
        if (produitCanalDTO.getId() != null) {
            throw new BadRequestAlertException("A new produitCanal cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ProduitCanalDTO result = produitCanalService.save(produitCanalDTO);
        return ResponseEntity.created(new URI("/api/produit-canals/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /produit-canals : Updates an existing produitCanal.
     *
     * @param produitCanalDTO the produitCanalDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated produitCanalDTO,
     * or with status 400 (Bad Request) if the produitCanalDTO is not valid,
     * or with status 500 (Internal Server Error) if the produitCanalDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/produit-canals")
    @Timed
    public ResponseEntity<ProduitCanalDTO> updateProduitCanal(@RequestBody ProduitCanalDTO produitCanalDTO) throws URISyntaxException {
        log.debug("REST request to update ProduitCanal : {}", produitCanalDTO);
        if (produitCanalDTO.getId() == null) {
            return createProduitCanal(produitCanalDTO);
        }
        ProduitCanalDTO result = produitCanalService.save(produitCanalDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, produitCanalDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /produit-canals : get all the produitCanals.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of produitCanals in body
     */
    @GetMapping("/produit-canals")
    @Timed
    public ResponseEntity<List<ProduitCanalDTO>> getAllProduitCanals(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of ProduitCanals");
        Page<ProduitCanalDTO> page = produitCanalService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/produit-canals");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /produit-canals/:id : get the "id" produitCanal.
     *
     * @param id the id of the produitCanalDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the produitCanalDTO, or with status 404 (Not Found)
     */
    @GetMapping("/produit-canals/{id}")
    @Timed
    public ResponseEntity<ProduitCanalDTO> getProduitCanal(@PathVariable Long id) {
        log.debug("REST request to get ProduitCanal : {}", id);
        ProduitCanalDTO produitCanalDTO = produitCanalService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(produitCanalDTO));
    }

    /**
     * DELETE  /produit-canals/:id : delete the "id" produitCanal.
     *
     * @param id the id of the produitCanalDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/produit-canals/{id}")
    @Timed
    public ResponseEntity<Void> deleteProduitCanal(@PathVariable Long id) {
        log.debug("REST request to delete ProduitCanal : {}", id);
        produitCanalService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
