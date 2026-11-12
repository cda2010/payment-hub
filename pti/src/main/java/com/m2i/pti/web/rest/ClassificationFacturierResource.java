package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.ClassificationFacturier;

import com.m2i.pti.repository.ClassificationFacturierRepository;
import com.m2i.pti.service.dto.ClassificationFacturierDTO;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing ClassificationFacturier.
 */
@RestController
@RequestMapping("/api")
public class ClassificationFacturierResource {

    private final Logger log = LoggerFactory.getLogger(ClassificationFacturierResource.class);

    private static final String ENTITY_NAME = "classificationFacturier";

    private final ClassificationFacturierRepository classificationFacturierRepository;

    public ClassificationFacturierResource(ClassificationFacturierRepository classificationFacturierRepository) {
        this.classificationFacturierRepository = classificationFacturierRepository;
    }

    /**
     * POST  /classification-facturiers : Create a new classificationFacturier.
     *
     * @param classificationFacturier the classificationFacturier to create
     * @return the ResponseEntity with status 201 (Created) and with body the new classificationFacturier, or with status 400 (Bad Request) if the classificationFacturier has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/classification-facturiers")
    @Timed
    public ResponseEntity<ClassificationFacturier> createClassificationFacturier(@RequestBody ClassificationFacturier classificationFacturier) throws URISyntaxException {
        log.debug("REST request to save ClassificationFacturier : {}", classificationFacturier);
        if (classificationFacturier.getId() != null) {
            throw new BadRequestAlertException("A new classificationFacturier cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ClassificationFacturier result = classificationFacturierRepository.save(classificationFacturier);
        return ResponseEntity.created(new URI("/api/classification-facturiers/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /classification-facturiers : Updates an existing classificationFacturier.
     *
     * @param classificationFacturier the classificationFacturier to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated classificationFacturier,
     * or with status 400 (Bad Request) if the classificationFacturier is not valid,
     * or with status 500 (Internal Server Error) if the classificationFacturier couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/classification-facturiers")
    @Timed
    public ResponseEntity<ClassificationFacturier> updateClassificationFacturier(@RequestBody ClassificationFacturier classificationFacturier) throws URISyntaxException {
        log.debug("REST request to update ClassificationFacturier : {}", classificationFacturier);
        if (classificationFacturier.getId() == null) {
            return createClassificationFacturier(classificationFacturier);
        }
        ClassificationFacturier result = classificationFacturierRepository.save(classificationFacturier);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, classificationFacturier.getId().toString()))
            .body(result);
    }

    /**
     * GET  /classification-facturiers : get all the classificationFacturiers.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of classificationFacturiers in body
     */
/*    @GetMapping("/classification-facturiers")
    @Timed
    public List<ClassificationFacturier> getAllClassificationFacturiers() {
        log.debug("REST request to get all ClassificationFacturiers");
        return classificationFacturierRepository.findAll();
    }*/

    @GetMapping("/classification-facturiers")
    @Timed
    public ResponseEntity<List<ClassificationFacturier>> getListypeServices(@ApiParam Pageable pageable) {
        log.debug("REST request to get list of TypeServices");
        Page<ClassificationFacturier> page = classificationFacturierRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/classification-facturiers");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /classification-facturiers/:id : get the "id" classificationFacturier.
     *
     * @param id the id of the classificationFacturier to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the classificationFacturier, or with status 404 (Not Found)
     */
    @GetMapping("/classification-facturiers/{id}")
    @Timed
    public ResponseEntity<ClassificationFacturier> getClassificationFacturier(@PathVariable Long id) {
        log.debug("REST request to get ClassificationFacturier : {}", id);
        ClassificationFacturier classificationFacturier = classificationFacturierRepository.findById(id).orElse(null);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(classificationFacturier));
    }

    @GetMapping("/classification-facturiers-final/ofUo/{id}")
    @Timed
    public List<ClassificationFacturierDTO> getClassificationFacturierFinalOfUo(@PathVariable Long id) {
        List<ClassificationFacturier> l=classificationFacturierRepository.findClassificationFinalOfUo(id);
        int longueur=l.size();
        int i;
        List<ClassificationFacturierDTO> retour=new ArrayList<ClassificationFacturierDTO>();
        for( i=0;i<longueur; i++){
            retour.add(new ClassificationFacturierDTO(l.get(i)));
        }
        return retour;
    }
    @GetMapping("/classification-facturiers/ofUo/{id}")
    @Timed
    public List<ClassificationFacturierDTO> getClassificationFacturierOfUo(@PathVariable Long id) {
        List<ClassificationFacturier> l=classificationFacturierRepository.findClassificationOfUo(id);
        int longueur=l.size();
        int i;
        List<ClassificationFacturierDTO> retour=new ArrayList<ClassificationFacturierDTO>();
        for( i=0;i<longueur; i++){
            retour.add(new ClassificationFacturierDTO(l.get(i)));
        }
        return retour;
    }

    /**
     * DELETE  /classification-facturiers/:id : delete the "id" classificationFacturier.
     *
     * @param id the id of the classificationFacturier to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/classification-facturiers/{id}")
    @Timed
    public ResponseEntity<Void> deleteClassificationFacturier(@PathVariable Long id) {
        log.debug("REST request to delete ClassificationFacturier : {}", id);
        classificationFacturierRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
