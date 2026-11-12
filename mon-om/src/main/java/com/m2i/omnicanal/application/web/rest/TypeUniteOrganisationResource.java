package com.m2i.omnicanal.application.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.application.domain.TypeUniteOrg;
import com.m2i.omnicanal.application.service.TypeUniteOrganisationService;
import com.m2i.omnicanal.application.web.rest.util.HeaderUtil;
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
 * REST controller for managing TypeUniteOrganisation.
 */
@RestController
@RequestMapping("/api")
public class TypeUniteOrganisationResource {

    private final Logger log = LoggerFactory.getLogger(TypeUniteOrganisationResource.class);

    private static final String ENTITY_NAME = "typeUniteOrganisation";
        
    private final TypeUniteOrganisationService typeUniteOrganisationService;

    public TypeUniteOrganisationResource(TypeUniteOrganisationService typeUniteOrganisationService) {
        this.typeUniteOrganisationService = typeUniteOrganisationService;
    }

    /**
     * POST  /type-unite-organisations : Create a new typeUniteOrganisation.
     *
     * @param typeUniteOrganisation the typeUniteOrganisation to create
     * @return the ResponseEntity with status 201 (Created) and with body the new typeUniteOrganisation, or with status 400 (Bad Request) if the typeUniteOrganisation has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/type-unite-organisations")
    @Timed
    public ResponseEntity<TypeUniteOrg> createTypeUniteOrganisation(@RequestBody TypeUniteOrg typeUniteOrganisation) throws URISyntaxException {
        log.debug("REST request to save TypeUniteOrganisation : {}", typeUniteOrganisation);
        if (typeUniteOrganisation.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new typeUniteOrganisation cannot already have an ID")).body(null);
        }
        TypeUniteOrg result = typeUniteOrganisationService.save(typeUniteOrganisation);
        return ResponseEntity.created(new URI("/api/type-unite-organisations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /type-unite-organisations : Updates an existing typeUniteOrganisation.
     *
     * @param typeUniteOrganisation the typeUniteOrganisation to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated typeUniteOrganisation,
     * or with status 400 (Bad Request) if the typeUniteOrganisation is not valid,
     * or with status 500 (Internal Server Error) if the typeUniteOrganisation couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/type-unite-organisations")
    @Timed
    public ResponseEntity<TypeUniteOrg> updateTypeUniteOrganisation(@RequestBody TypeUniteOrg typeUniteOrganisation) throws URISyntaxException {
        log.debug("REST request to update TypeUniteOrganisation : {}", typeUniteOrganisation);
        if (typeUniteOrganisation.getId() == null) {
            return createTypeUniteOrganisation(typeUniteOrganisation);
        }
        TypeUniteOrg result = typeUniteOrganisationService.save(typeUniteOrganisation);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, typeUniteOrganisation.getId().toString()))
            .body(result);
    }

    /**
     * GET  /type-unite-organisations : get all the typeUniteOrganisations.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of typeUniteOrganisations in body
     */
    @GetMapping("/type-unite-organisations")
    @Timed
    public List<TypeUniteOrg> getAllTypeUniteOrganisations() {
        log.debug("REST request to get all TypeUniteOrganisations");
        return typeUniteOrganisationService.findAll();
    }

    /**
     * GET  /type-unite-organisations/:id : get the "id" typeUniteOrganisation.
     *
     * @param id the id of the typeUniteOrganisation to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the typeUniteOrganisation, or with status 404 (Not Found)
     */
    @GetMapping("/type-unite-organisations/{id}")
    @Timed
    public ResponseEntity<TypeUniteOrg> getTypeUniteOrganisation(@PathVariable Long id) {
        log.debug("REST request to get TypeUniteOrganisation : {}", id);
        TypeUniteOrg typeUniteOrganisation = typeUniteOrganisationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(typeUniteOrganisation));
    }

    /**
     * DELETE  /type-unite-organisations/:id : delete the "id" typeUniteOrganisation.
     *
     * @param id the id of the typeUniteOrganisation to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/type-unite-organisations/{id}")
    @Timed
    public ResponseEntity<Void> deleteTypeUniteOrganisation(@PathVariable Long id) {
        log.debug("REST request to delete TypeUniteOrganisation : {}", id);
        typeUniteOrganisationService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
