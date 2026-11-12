package com.m2i.omnicanal.presentation.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.presentation.domain.Etiquette;

import com.m2i.omnicanal.presentation.repository.EtiquetteRepository;
import com.m2i.omnicanal.presentation.web.rest.util.HeaderUtil;
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
 * REST controller for managing Etiquette.
 */
@RestController
@RequestMapping("/api")
public class EtiquetteResource {

    private final Logger log = LoggerFactory.getLogger(EtiquetteResource.class);

    private static final String ENTITY_NAME = "etiquette";
        
    private final EtiquetteRepository etiquetteRepository;

    public EtiquetteResource(EtiquetteRepository etiquetteRepository) {
        this.etiquetteRepository = etiquetteRepository;
    }

    /**
     * POST  /etiquettes : Create a new etiquette.
     *
     * @param etiquette the etiquette to create
     * @return the ResponseEntity with status 201 (Created) and with body the new etiquette, or with status 400 (Bad Request) if the etiquette has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/etiquettes")
    @Timed
    public ResponseEntity<Etiquette> createEtiquette(@RequestBody Etiquette etiquette) throws URISyntaxException {
        log.debug("REST request to save Etiquette : {}", etiquette);
        if (etiquette.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new etiquette cannot already have an ID")).body(null);
        }
        Etiquette result = etiquetteRepository.save(etiquette);
        return ResponseEntity.created(new URI("/api/etiquettes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /etiquettes : Updates an existing etiquette.
     *
     * @param etiquette the etiquette to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated etiquette,
     * or with status 400 (Bad Request) if the etiquette is not valid,
     * or with status 500 (Internal Server Error) if the etiquette couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/etiquettes")
    @Timed
    public ResponseEntity<Etiquette> updateEtiquette(@RequestBody Etiquette etiquette) throws URISyntaxException {
        log.debug("REST request to update Etiquette : {}", etiquette);
        if (etiquette.getId() == null) {
            return createEtiquette(etiquette);
        }
        Etiquette result = etiquetteRepository.save(etiquette);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, etiquette.getId().toString()))
            .body(result);
    }

    /**
     * GET  /etiquettes : get all the etiquettes.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of etiquettes in body
     */
    @GetMapping("/etiquettes")
    @Timed
    public List<Etiquette> getAllEtiquettes() {
        log.debug("REST request to get all Etiquettes");
        List<Etiquette> etiquettes = etiquetteRepository.findAll();
        return etiquettes;
    }

    /**
     * GET  /etiquettes/:id : get the "id" etiquette.
     *
     * @param id the id of the etiquette to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the etiquette, or with status 404 (Not Found)
     */
    @GetMapping("/etiquettes/{id}")
    @Timed
    public ResponseEntity<Etiquette> getEtiquette(@PathVariable Long id) {
        log.debug("REST request to get Etiquette : {}", id);
        Etiquette etiquette = etiquetteRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(etiquette));
    }

    /**
     * DELETE  /etiquettes/:id : delete the "id" etiquette.
     *
     * @param id the id of the etiquette to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/etiquettes/{id}")
    @Timed
    public ResponseEntity<Void> deleteEtiquette(@PathVariable Long id) {
        log.debug("REST request to delete Etiquette : {}", id);
        etiquetteRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
