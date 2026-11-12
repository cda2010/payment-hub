package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Bouton;

import com.m2i.pti.repository.BoutonRepository;
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
 * REST controller for managing Bouton.
 */
@RestController
@RequestMapping("/api")
public class BoutonResource {

    private final Logger log = LoggerFactory.getLogger(BoutonResource.class);

    private static final String ENTITY_NAME = "bouton";

    private final BoutonRepository boutonRepository;

    public BoutonResource(BoutonRepository boutonRepository) {
        this.boutonRepository = boutonRepository;
    }

    /**
     * POST  /boutons : Create a new bouton.
     *
     * @param bouton the bouton to create
     * @return the ResponseEntity with status 201 (Created) and with body the new bouton, or with status 400 (Bad Request) if the bouton has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/boutons")
    @Timed
    public ResponseEntity<Bouton> createBouton(@RequestBody Bouton bouton) throws URISyntaxException {
        log.debug("REST request to save Bouton : {}", bouton);
        if (bouton.getId() != null) {
            throw new BadRequestAlertException("A new bouton cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Bouton result = boutonRepository.save(bouton);
        return ResponseEntity.created(new URI("/api/boutons/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /boutons : Updates an existing bouton.
     *
     * @param bouton the bouton to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated bouton,
     * or with status 400 (Bad Request) if the bouton is not valid,
     * or with status 500 (Internal Server Error) if the bouton couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/boutons")
    @Timed
    public ResponseEntity<Bouton> updateBouton(@RequestBody Bouton bouton) throws URISyntaxException {
        log.debug("REST request to update Bouton : {}", bouton);
        if (bouton.getId() == null) {
            return createBouton(bouton);
        }
        Bouton result = boutonRepository.save(bouton);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, bouton.getId().toString()))
            .body(result);
    }

    /**
     * GET  /boutons : get all the boutons.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of boutons in body
     */
    @GetMapping("/boutons")
    @Timed
    public List<Bouton> getAllBoutons() {
        log.debug("REST request to get all Boutons");
        return boutonRepository.findAll();
        }

    /**
     * GET  /boutons/:id : get the "id" bouton.
     *
     * @param id the id of the bouton to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the bouton, or with status 404 (Not Found)
     */
    @GetMapping("/boutons/{id}")
    @Timed
    public ResponseEntity<Bouton> getBouton(@PathVariable Long id) {
        log.debug("REST request to get Bouton : {}", id);
        Bouton bouton = boutonRepository.findById(id).orElse(null);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(bouton));
    }

    /**
     * DELETE  /boutons/:id : delete the "id" bouton.
     *
     * @param id the id of the bouton to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/boutons/{id}")
    @Timed
    public ResponseEntity<Void> deleteBouton(@PathVariable Long id) {
        log.debug("REST request to delete Bouton : {}", id);
        boutonRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
