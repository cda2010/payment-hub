package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Produit;

import com.m2i.pti.repository.ProduitRepository;
import com.m2i.pti.service.ProduitService;
import com.m2i.pti.service.dto.ProduitDTO;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
 * REST controller for managing Produit.
 */
@RestController
@RequestMapping("/api")
public class ProduitResource {

    private final Logger log = LoggerFactory.getLogger(ProduitResource.class);

    private static final String ENTITY_NAME = "produit";

    private final ProduitRepository produitRepository;

    private final ProduitService produitService;

    public ProduitResource(ProduitRepository produitRepository, ProduitService produitService) {
        this.produitRepository = produitRepository;
        this.produitService = produitService;
    }

    /**
     * POST  /produits : Create a new produit.
     *
     * @param produit the produit to create
     * @return the ResponseEntity with status 201 (Created) and with body the new produit, or with status 400 (Bad Request) if the produit has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/produits")
    @Timed
    public ResponseEntity<Produit> createProduit(@RequestBody ProduitDTO produit) throws URISyntaxException {
        log.debug("REST request to save Produit : {}", produit);
       
        if (produit.getId() != null) {
            throw new BadRequestAlertException("A new produit cannot already have an ID", ENTITY_NAME, "idexists");
        }
       
        Produit result = produitService.save(produit);
        return ResponseEntity.created(new URI("/api/produits/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /produits : Updates an existing produit.
     *
     * @param produit the produit to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated produit,
     * or with status 400 (Bad Request) if the produit is not valid,
     * or with status 500 (Internal Server Error) if the produit couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/produits")
    @Timed
    public ResponseEntity<Produit> updateProduit(@RequestBody ProduitDTO produit) throws URISyntaxException {
        log.debug("REST request to update Produit : {}", produit);
        if (produit.getId() == null) {
            return createProduit(produit);
        }
       //Produit result = produitRepository.save(produit);
        Produit result = produitService.upDate(produit);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, produit.getId().toString()))
            .body(result);
    }

    /**
     * GET  /produits : get all the produits.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of produits in body
     */
    @GetMapping("/produits")
    @Timed
    public ResponseEntity<List<Produit>> getAllProduits(Pageable pageable) {
        log.debug("REST request to get all Produits");
        Page<Produit> page=produitRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/produit");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
        }

    @GetMapping("/produits/liste")
    @Timed
    public ResponseEntity<List<ProduitDTO>> getProduitListe(Pageable pageable) {
        List<ProduitDTO> all = produitRepository.findWithFacturier();
        int start = Math.min((int) pageable.getOffset(), all.size());
        int end = Math.min(start + pageable.getPageSize(), all.size());
        Page<ProduitDTO> page = new PageImpl<>(all.subList(start, end), pageable, all.size());
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/produits/liste");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/produits/libelles")
    @Timed
    public List<ProduitDTO> getProduitLabels() {
        return produitRepository.findLabels();
    }

    @GetMapping("/produits/ofFacturier/{id}")
    @Timed
    public List<Produit> getAllProduitsOfFacturier(@PathVariable Long id) {
        log.debug("REST request to get all Produits");
        return produitRepository.findProduitByFacturierIdOrderByNumeroOrdreAsc(id);
    }

    /*@GetMapping("/produits/ofFacturier/{id}")
    @Timed
    public ResponseEntity<List<Produit>> getAllProduitsOfFacturier(@PathVariable Long id, Pageable pageable) {
        log.debug("REST request to get all Produits");
        Page<Produit>  page=produitRepository.findPageProduitByFacturierIdOrderByNumeroOrdreAsc(id,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/produit");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }*/

    /**
     * GET  /produits/:id : get the "id" produit.
     *
     * @param id the id of the produit to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the produit, or with status 404 (Not Found)
     */
    @GetMapping("/produits/{id}")
    @Timed
    public ResponseEntity<ProduitDTO> getProduit(@PathVariable Long id) {
        log.debug("REST request to get Produit : {}", id);
        //Produit produit = produitRepository.findById(id).orElse(null);
        ProduitDTO produit =produitService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(produit));
    }

    /**
     * DELETE  /produits/:id : delete the "id" produit.
     *
     * @param id the id of the produit to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/produits/{id}")
    @Timed
    public ResponseEntity<Void> deleteProduit(@PathVariable Long id) {
        log.debug("REST request to delete Produit : {}", id);
        produitRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
