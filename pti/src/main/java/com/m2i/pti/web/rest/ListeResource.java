package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Liste;

import com.m2i.pti.repository.ListeRepository;
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
 * REST controller for managing Liste.
 */
@RestController
@RequestMapping("/api")
public class ListeResource {

    private final Logger log = LoggerFactory.getLogger(ListeResource.class);

    private static final String ENTITY_NAME = "liste";

    private final ListeRepository listeRepository;

    public ListeResource(ListeRepository listeRepository) {
        this.listeRepository = listeRepository;
    }

    /**
     * POST  /listes : Create a new liste.
     *
     * @param liste the liste to create
     * @return the ResponseEntity with status 201 (Created) and with body the new liste, or with status 400 (Bad Request) if the liste has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/listes")
    @Timed
    public ResponseEntity<Liste> createListe(@RequestBody Liste liste) throws URISyntaxException {
        log.debug("REST request to save Liste : {}", liste);
        if (liste.getId() != null) {
            throw new BadRequestAlertException("A new liste cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Liste result = listeRepository.save(liste);
        return ResponseEntity.created(new URI("/api/listes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /listes : Updates an existing liste.
     *
     * @param liste the liste to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated liste,
     * or with status 400 (Bad Request) if the liste is not valid,
     * or with status 500 (Internal Server Error) if the liste couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/listes")
    @Timed
    public ResponseEntity<Liste> updateListe(@RequestBody Liste liste) throws URISyntaxException {
        log.debug("REST request to update Liste : {}", liste);
        if (liste.getId() == null) {
            return createListe(liste);
        }
        Liste result = listeRepository.save(liste);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, liste.getId().toString()))
            .body(result);
    }

    /**
     * GET  /listes : get all the listes.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of listes in body
     */
    @GetMapping("/listes")
    @Timed
    public List<Liste> getAllListes() {
        log.debug("REST request to get all Listes");
        return listeRepository.findAll();
        }

    /**
     * GET  /listes/:id : get the "id" liste.
     *
     * @param id the id of the liste to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the liste, or with status 404 (Not Found)
     */
    @GetMapping("/listes/{id}")
    @Timed
    public ResponseEntity<Liste> getListe(@PathVariable Long id) {
        log.debug("REST request to get Liste : {}", id);
        Liste liste = listeRepository.findById(id).orElse(null);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(liste));
    }

    /**
     * DELETE  /listes/:id : delete the "id" liste.
     *
     * @param id the id of the liste to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/listes/{id}")
    @Timed
    public ResponseEntity<Void> deleteListe(@PathVariable Long id) {
        log.debug("REST request to delete Liste : {}", id);
        listeRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
