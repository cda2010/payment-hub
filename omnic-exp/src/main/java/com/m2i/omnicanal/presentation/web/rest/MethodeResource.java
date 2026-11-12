package com.m2i.omnicanal.presentation.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.presentation.domain.MethodeFonction;
import com.m2i.omnicanal.presentation.service.MethodeService;
import com.m2i.omnicanal.presentation.web.rest.util.HeaderUtil;

import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Methode.
 */
@RestController
@RequestMapping("/api")
public class MethodeResource {

    private final Logger log = LoggerFactory.getLogger(MethodeResource.class);

    private static final String ENTITY_NAME = "methode";
        
    private final MethodeService methodeService;

    public MethodeResource(MethodeService methodeService) {
        this.methodeService = methodeService;
    }

    /**
     * POST  /methodes : Create a new methode.
     *
     * @param methode the methode to create
     * @return the ResponseEntity with status 201 (Created) and with body the new methode, or with status 400 (Bad Request) if the methode has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/methodes")
    @Timed
    public ResponseEntity<MethodeFonction> createMethode(@Valid @RequestBody MethodeFonction methode) throws URISyntaxException {
        log.debug("REST request to save Methode : {}", methode);
        if (methode.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new methode cannot already have an ID")).body(null);
        }
        MethodeFonction result = methodeService.save(methode);
        return ResponseEntity.created(new URI("/api/methodes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /methodes : Updates an existing methode.
     *
     * @param methode the methode to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated methode,
     * or with status 400 (Bad Request) if the methode is not valid,
     * or with status 500 (Internal Server Error) if the methode couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/methodes")
    @Timed
    public ResponseEntity<MethodeFonction> updateMethode(@Valid @RequestBody MethodeFonction methode) throws URISyntaxException {
        log.debug("REST request to update Methode : {}", methode);
        if (methode.getId() == null) {
            return createMethode(methode);
        }
        MethodeFonction result = methodeService.save(methode);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, methode.getId().toString()))
            .body(result);
    }

    /**
     * GET  /methodes : get all the methodes.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of methodes in body
     */
    @GetMapping("/methodes")
    @Timed
    public List<MethodeFonction> getAllMethodes() {
        log.debug("REST request to get all Methodes");
        return methodeService.findAll();
    }
@GetMapping("/methodes/api/{idApi}")
    @Timed
    public List<MethodeFonction> getMethodesApi(@PathVariable Long idApi) {
        log.debug("REST request to  Methodes by API");
        return methodeService.findByApi(idApi);
    }
    /**
     * GET  /methodes/:id : get the "id" methode.
     *
     * @param id the id of the methode to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the methode, or with status 404 (Not Found)
     */
    @GetMapping("/methodes/{id}")
    @Timed
    public ResponseEntity<MethodeFonction> getMethode(@PathVariable Long id) {
        log.debug("REST request to get Methode : {}", id);
        MethodeFonction methode = methodeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(methode));
    }

    /**
     * DELETE  /methodes/:id : delete the "id" methode.
     *
     * @param id the id of the methode to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/methodes/{id}")
    @Timed
    public ResponseEntity<Void> deleteMethode(@PathVariable Long id) {
        log.debug("REST request to delete Methode : {}", id);
        methodeService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
