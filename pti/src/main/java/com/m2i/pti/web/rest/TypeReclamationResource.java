package com.m2i.pti.web.rest;

import com.m2i.pti.domain.TypeReclamation;
import com.m2i.pti.service.TypeReclamationService;
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
 * REST controller for managing {@link com.m2i.pti.domain.TypeReclamation}.
 */
@RestController
@RequestMapping("/api")
public class TypeReclamationResource {

    private final Logger log = LoggerFactory.getLogger(TypeReclamationResource.class);

    private static final String ENTITY_NAME = "typeReclamation";
    private final TypeReclamationService typeReclamationService;

    public TypeReclamationResource(TypeReclamationService typeReclamationService) {
        this.typeReclamationService = typeReclamationService;
    }

    /**
     * {@code POST  /type-reclamations} : Create a new typeReclamation.
     *
     * @param typeReclamation the typeReclamation to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new typeReclamation, or with status {@code 400 (Bad Request)} if the typeReclamation has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/type-reclamations")
    public ResponseEntity<TypeReclamation> createTypeReclamation(@RequestBody TypeReclamation typeReclamation) throws URISyntaxException {
        log.debug("REST request to save TypeReclamation : {}", typeReclamation);
        if (typeReclamation.getId() != null) {
            throw new BadRequestAlertException("A new typeReclamation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        TypeReclamation result = typeReclamationService.save(typeReclamation);
        return ResponseEntity.created(new URI("/api/type-reclamations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /type-reclamations} : Updates an existing typeReclamation.
     *
     * @param typeReclamation the typeReclamation to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated typeReclamation,
     * or with status {@code 400 (Bad Request)} if the typeReclamation is not valid,
     * or with status {@code 500 (Internal Server Error)} if the typeReclamation couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/type-reclamations")
    public ResponseEntity<TypeReclamation> updateTypeReclamation(@RequestBody TypeReclamation typeReclamation) throws URISyntaxException {
        log.debug("REST request to update TypeReclamation : {}", typeReclamation);
        if (typeReclamation.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        TypeReclamation result = typeReclamationService.save(typeReclamation);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert( ENTITY_NAME, typeReclamation.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /type-reclamations} : get all the typeReclamations.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of typeReclamations in body.
     */
    @GetMapping("/type-reclamations")
    public List<TypeReclamation> getAllTypeReclamations() {
        log.debug("REST request to get all TypeReclamations");
        return typeReclamationService.findAll();
    }

    /**
     * {@code GET  /type-reclamations/:id} : get the "id" typeReclamation.
     *
     * @param id the id of the typeReclamation to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the typeReclamation, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/type-reclamations/{id}")
    public ResponseEntity<TypeReclamation> getTypeReclamation(@PathVariable Long id) {
        log.debug("REST request to get TypeReclamation : {}", id);
        Optional<TypeReclamation> typeReclamation = typeReclamationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(typeReclamation);
    }

    /**
     * {@code DELETE  /type-reclamations/:id} : delete the "id" typeReclamation.
     *
     * @param id the id of the typeReclamation to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/type-reclamations/{id}")
    public ResponseEntity<Void> deleteTypeReclamation(@PathVariable Long id) {
        log.debug("REST request to delete TypeReclamation : {}", id);
        typeReclamationService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
