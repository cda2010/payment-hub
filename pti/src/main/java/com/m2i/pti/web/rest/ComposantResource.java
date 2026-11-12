package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.Composant;
import com.m2i.pti.service.dto.ComposantListeDTO;

import com.m2i.pti.repository.ComposantRepository;
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

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Composant.
 */
@RestController
@RequestMapping("/api")
public class ComposantResource {

    private final Logger log = LoggerFactory.getLogger(ComposantResource.class);

    private static final String ENTITY_NAME = "composant";

    private final ComposantRepository composantRepository;

    public ComposantResource(ComposantRepository composantRepository) {
        this.composantRepository = composantRepository;
    }

    /**
     * POST  /composants : Create a new composant.
     *
     * @param composant the composant to create
     * @return the ResponseEntity with status 201 (Created) and with body the new composant, or with status 400 (Bad Request) if the composant has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/composants")
    @Timed
    public ResponseEntity<Composant> createComposant(@RequestBody Composant composant) throws URISyntaxException {
        log.debug("REST request to save Composant : {}", composant);
        if (composant.getId() != null) {
            throw new BadRequestAlertException("A new composant cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Composant result = composantRepository.save(composant);
        return ResponseEntity.created(new URI("/api/composants/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /composants : Updates an existing composant.
     *
     * @param composant the composant to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated composant,
     * or with status 400 (Bad Request) if the composant is not valid,
     * or with status 500 (Internal Server Error) if the composant couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/composants")
    @Timed
    public ResponseEntity<Composant> updateComposant(@RequestBody Composant composant) throws URISyntaxException {
        log.debug("REST request to update Composant : {}", composant);
        if (composant.getId() == null) {
            return createComposant(composant);
        }
        Composant result = composantRepository.save(composant);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, composant.getId().toString()))
            .body(result);
    }

    /**
     * GET  /composants : get all the composants.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of composants in body
     */
    /*@GetMapping("/composants")
    @Timed
    public List<Composant> getAllComposants() {
        log.debug("REST request to get all Composants");
        return composantRepository.findAll();
        }
*/
    /**
     * GET  /composants/:id : get the "id" composant.
     *
     * @param id the id of the composant to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the composant, or with status 404 (Not Found)
     */

    @GetMapping("/composants/libelles")
    @Timed
    public List<ComposantListeDTO> getComposantLabels() {
        return composantRepository.findLabels();
    }

    @GetMapping("/composants")
    @Timed
    public ResponseEntity<List<Composant>> getAllComposants(@ApiParam Pageable pageable) {
        log.debug("REST request to get all Composants");
        Page<Composant> page=composantRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/composants");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/composants/{id}")
    @Timed
    public ResponseEntity<Composant> getComposant(@PathVariable Long id) {
        log.debug("REST request to get Composant : {}", id);
        Composant composant = composantRepository.findById(id).orElse(null);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(composant));
    }

    /**
     * DELETE  /composants/:id : delete the "id" composant.
     *
     * @param id the id of the composant to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/composants/{id}")
    @Timed
    public ResponseEntity<Void> deleteComposant(@PathVariable Long id) {
        log.debug("REST request to delete Composant : {}", id);
        composantRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
    
     @GetMapping("/composants-all")
    @Timed
    public List<Composant> getAllComposants() {
        log.debug("REST request to get all Composants");
        List<Composant> result=composantRepository.findAllListe();
        return result;
    }
}
