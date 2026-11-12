package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Facture;

import com.m2i.pti.repository.FactureRepository;
import com.m2i.pti.service.FactureService;
import com.m2i.pti.service.dto.FactureDTO;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import net.sf.jasperreports.engine.JRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Facture.
 */
@RestController
@RequestMapping("/api")
public class FactureResource {

    private final Logger log = LoggerFactory.getLogger(FactureResource.class);

    private static final String ENTITY_NAME = "facture";

    private final FactureRepository factureRepository;

    private final FactureService factureService;

    public FactureResource(FactureRepository factureRepository, FactureService factureService) {
        this.factureRepository = factureRepository;
        this.factureService = factureService;
    }

    /**
     * POST  /factures : Create a new facture.
     *
     * @param facture the facture to create
     * @return the ResponseEntity with status 201 (Created) and with body the new facture, or with status 400 (Bad Request) if the facture has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/factures")
    @Timed
    public ResponseEntity<Facture> createFacture(@RequestBody Facture facture) throws URISyntaxException {
        log.debug("REST request to save Facture : {}", facture);
        if (facture.getId() != null) {
            throw new BadRequestAlertException("A new facture cannot already have an ID", ENTITY_NAME, "idexists");
        }

        Facture result = factureRepository.save(facture);
        ZonedDateTime dateTime= ZonedDateTime.now();
        result.setReferenceFacature(result.getReferenceFacature()+"_"+result.getId());
        if(result.getDatePaiement()==null) {
            result.setDatePaiement(dateTime.toLocalDateTime().toString());
        }
         result = factureRepository.save(result);
        return ResponseEntity.created(new URI("/api/factures/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /factures : Updates an existing facture.
     *
     * @param facture the facture to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated facture,
     * or with status 400 (Bad Request) if the facture is not valid,
     * or with status 500 (Internal Server Error) if the facture couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/factures")
    @Timed
    public ResponseEntity<Facture> updateFacture(@RequestBody Facture facture) throws URISyntaxException {
        log.debug("REST request to update Facture : {}", facture);
        if (facture.getId() == null) {
            return createFacture(facture);
        }

        Facture result = factureRepository.save(facture);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, facture.getId().toString()))
            .body(result);
    }

    /**
     * GET  /factures : get all the factures.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of factures in body
     */
    @GetMapping("/factures")
    @Timed
    public List<Facture> getAllFactures() {
        log.debug("REST request to get all Factures");
        return factureRepository.findAll();
        }

    @GetMapping("/factures/Alls31")
    @Timed
    public ResponseEntity<List<Facture>> getAllFacturesOptimise(@RequestParam(required = false) String refFacture, @RequestParam(required = false) String refPaiement, @RequestParam(required = false) Long produit, @RequestParam(required = false) String datePaiement , Pageable pageable) {
        log.debug("REST request to get a page of Factures");
        List<Facture> list=new ArrayList<>();
        Page<Facture> page = factureService.findAll(refFacture,refPaiement,produit,datePaiement,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/facture");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/factures/Alls")
    @Timed
    public ResponseEntity<List<FactureDTO>> getAllFactures(@RequestParam(required = false) String refFacture, @RequestParam(required = false) String refPaiement, @RequestParam(required = false) Long produit, @RequestParam(required = false) String datePaiement , Pageable pageable) {
        log.debug("REST request to get a page of Factures");
        List<FactureDTO> list=new ArrayList<>();
        Page<FactureDTO> page = factureService.findAllOptimise(refFacture,refPaiement,produit,datePaiement,pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/facture");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/factures/of-facturier/{id}")
    @Timed
    public List<Facture> getFactureOfFacturier(@PathVariable Long id) {

        return factureRepository.findFacturesOfProduit(id);
    }
    @GetMapping("/factures/of-facturier-optimiser/{id}")
    @Timed
    public List<FactureDTO> getFactureOfFacturierOptimiser(@PathVariable Long id) {

        return factureRepository.findFactureOfProduitOptimise(id);
    }


    /**
     * GET  /factures/:id : get the "id" facture.
     *
     * @param id the id of the facture to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the facture, or with status 404 (Not Found)
     */
    @GetMapping("/factures/{id}")
    @Timed
    public ResponseEntity<Facture> getFacture(@PathVariable Long id) {
        log.debug("REST request to get Facture : {}", id);
        Facture facture = factureRepository.findById(id).orElse(null);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(facture));
    }
  @GetMapping("/factures/by-reference/{reference}")
    @Timed
    public List<Facture>getFactures(@PathVariable String reference) {
       return factureRepository.findByReferenceFacature(reference);
    }
    /**
     * DELETE  /factures/:id : delete the "id" facture.
     *
     * @param id the id of the facture to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/factures/{id}")
    @Timed
    public ResponseEntity<Void> deleteFacture(@PathVariable Long id) {
        log.debug("REST request to delete Facture : {}", id);
        factureRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    //export to csv
/*    @RequestMapping(value = "/factures/download",
            method = RequestMethod.GET,
            produces = MediaType.ALL_VALUE)
    @Timed
    public StringBuilder getFactureCSV(@RequestParam(required = false) String refFacture, @RequestParam(required = false) String refPaiement, @RequestParam(required = false) Long produit, @RequestParam(required = false) String datePaiement, @RequestParam(required = false) Long facturierId ) throws FileNotFoundException, JRException {
        return factureService.getCSVFacture(refFacture,refPaiement,produit,datePaiement, facturierId);
    }*/
}
