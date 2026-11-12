package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Commerciale;

import com.m2i.pti.repository.CommercialeRepository;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
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
 * REST controller for managing Commerciale.
 */
@RestController
@RequestMapping("/api")
public class CommercialeResource {

    private final Logger log = LoggerFactory.getLogger(CommercialeResource.class);

    private static final String ENTITY_NAME = "commerciale";

    private final CommercialeRepository commercialeRepository;

    public CommercialeResource(CommercialeRepository commercialeRepository) {
        this.commercialeRepository = commercialeRepository;
    }

    /**
     * POST  /commerciales : Create a new commerciale.
     *
     * @param commerciale the commerciale to create
     * @return the ResponseEntity with status 201 (Created) and with body the new commerciale, or with status 400 (Bad Request) if the commerciale has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/commerciales")
    @Timed
    public ResponseEntity<Commerciale> createCommerciale(@RequestBody Commerciale commerciale) throws URISyntaxException {
        log.debug("REST request to save Commerciale : {}", commerciale);
        if (commerciale.getId() != null) {
            throw new BadRequestAlertException("A new commerciale cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Commerciale result = commercialeRepository.save(commerciale);
        return ResponseEntity.created(new URI("/api/commerciales/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /commerciales : Updates an existing commerciale.
     *
     * @param commerciale the commerciale to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated commerciale,
     * or with status 400 (Bad Request) if the commerciale is not valid,
     * or with status 500 (Internal Server Error) if the commerciale couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/commerciales")
    @Timed
    public ResponseEntity<Commerciale> updateCommerciale(@RequestBody Commerciale commerciale) throws URISyntaxException {
        log.debug("REST request to update Commerciale : {}", commerciale);
        if (commerciale.getId() == null) {
            return createCommerciale(commerciale);
        }
        Commerciale result = commercialeRepository.save(commerciale);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, commerciale.getId().toString()))
            .body(result);
    }

    /**
     * GET  /commerciales : get all the commerciales.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of commerciales in body
     */
    @GetMapping("/commerciales")
    @Timed
    public List<Commerciale> getAllCommerciales() {
        log.debug("REST request to get all Commerciales");
        return commercialeRepository.findAll();
        }

    /**
     * GET  /commerciales/:id : get the "id" commerciale.
     *
     * @param id the id of the commerciale to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the commerciale, or with status 404 (Not Found)
     */
    @GetMapping("/commerciales/{id}")
    @Timed
    public ResponseEntity<Commerciale> getCommerciale(@PathVariable Long id) {
        log.debug("REST request to get Commerciale : {}", id);
        Commerciale commerciale = commercialeRepository.findById(id).orElse(null);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(commerciale));
    }

    /**
     * DELETE  /commerciales/:id : delete the "id" commerciale.
     *
     * @param id the id of the commerciale to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/commerciales/{id}")
    @Timed
    public ResponseEntity<Void> deleteCommerciale(@PathVariable Long id) {
        log.debug("REST request to delete Commerciale : {}", id);
        commercialeRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
