package com.m2i.omnicanal.application.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.application.domain.SchemaEcrit;
import com.m2i.omnicanal.application.domain.Service;
import com.m2i.omnicanal.application.repository.SchemaEcritureRepository;
import com.m2i.omnicanal.application.service.TypeServiceService;
import com.m2i.omnicanal.application.service.SchemaEcritureService;
import com.m2i.omnicanal.application.web.rest.util.HeaderUtil;
import com.m2i.omnicanal.application.web.rest.util.PaginationUtil;
import com.m2i.omnicanal.model.SchemaEcriture;
import com.m2i.omnicanal.model.TypeService;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import io.github.jhipster.web.util.ResponseUtil;
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
 * REST controller for managing SchemaEcriture.
 */
@RestController
@RequestMapping("/api")
public class SchemaEcritureResource {

    private final Logger log = LoggerFactory.getLogger(SchemaEcritureResource.class);

    private static final String ENTITY_NAME = "schemaEcriture";

    private final SchemaEcritureService schemaEcritureService;
    private final SchemaEcritureRepository schemaEcritureRepository;
    private final TypeServiceService typeServiceService;
    public SchemaEcritureResource(SchemaEcritureService schemaEcritureService, SchemaEcritureRepository schemaEcritureRepository, TypeServiceService typeServiceService) {
        this.schemaEcritureService = schemaEcritureService;
        this.schemaEcritureRepository = schemaEcritureRepository;
        this.typeServiceService=typeServiceService;
    }

    /**
     * POST  /schema-ecritures : Create a new schemaEcriture.
     *
     * @param schemaEcriture the schemaEcriture to create
     * @return the ResponseEntity with status 201 (Created) and with body the new schemaEcriture, or with status 400 (Bad Request) if the schemaEcriture has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/schema-ecritures")
    @Timed
    public ResponseEntity<SchemaEcrit> createSchemaEcriture(@RequestBody SchemaEcrit schemaEcriture) throws URISyntaxException {
        log.debug("REST request to save SchemaEcriture : {}", schemaEcriture);
        if (schemaEcriture.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new schemaEcriture cannot already have an ID")).body(null);
        }
       if (schemaEcriture.getCodeGroovy()!= null) {
            GroovyShell shell = new GroovyShell();
            Binding binding = new Binding();
            Script script;
            try {
            script = shell.parse(schemaEcriture.getCodeGroovy());
            }catch (Exception e) {

                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("schemaEcriture", "idexists", "le code groovy est incorrect")).body(null);

            }

       }
        SchemaEcrit result = schemaEcritureService.save(schemaEcriture);
        return ResponseEntity.created(new URI("/api/schema-ecritures/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /schema-ecritures : Updates an existing schemaEcriture.
     *
     * @param schemaEcriture the schemaEcriture to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated schemaEcriture,
     * or with status 400 (Bad Request) if the schemaEcriture is not valid,
     * or with status 500 (Internal Server Error) if the schemaEcriture couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/schema-ecritures")
    @Timed
    public ResponseEntity<SchemaEcrit> updateSchemaEcriture(@RequestBody SchemaEcrit schemaEcriture) throws URISyntaxException {
        log.debug("REST request to update SchemaEcriture : {}", schemaEcriture);
        if (schemaEcriture.getId() == null) {
            return createSchemaEcriture(schemaEcriture);
        }
        SchemaEcrit result = schemaEcritureService.save(schemaEcriture);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, schemaEcriture.getId().toString()))
            .body(result);
    }

    /**
     * GET  /schema-ecritures : get all the schemaEcritures.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of schemaEcritures in body
     */
    @GetMapping("/schema-ecritures")
    @Timed
//    public List<SchemaEcrit> getAllSchemaEcritures() {
//        log.debug("REST request to get all SchemaEcritures");
//        return schemaEcritureService.findAll();
//    }

    public ResponseEntity<List<SchemaEcrit>> getAllSchemaEcritures(Pageable pageable) {
        log.debug("REST request to get all SchemaEcritures");
        Page<SchemaEcrit> page = schemaEcritureRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/apis");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);

    }
        @GetMapping("/schema-ecritures/typeService/{typeService}")
    @Timed
    public List<SchemaEcriture> getAllServiceSchemaEcritures(@PathVariable Long typeService) {
        log.debug("REST request to get all SchemaEcritures");

      TypeService  ts= typeServiceService.findOne(typeService);
        return schemaEcritureService.findByService(ts);
    }

    /**
     * GET  /schema-ecritures/:id : get the "id" schemaEcriture.
     *
     * @param id the id of the schemaEcriture to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the schemaEcriture, or with status 404 (Not Found)
     */
    @GetMapping("/schema-ecritures/{id}")
    @Timed
    public ResponseEntity<SchemaEcrit> getSchemaEcriture(@PathVariable Long id) {
        log.debug("REST request to get SchemaEcriture : {}", id);
        SchemaEcrit schemaEcriture = schemaEcritureService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(schemaEcriture));
    }

    /**
     * DELETE  /schema-ecritures/:id : delete the "id" schemaEcriture.
     *
     * @param id the id of the schemaEcriture to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/schema-ecritures/{id}")
    @Timed
    public ResponseEntity<Void> deleteSchemaEcriture(@PathVariable Long id) {
        log.debug("REST request to delete SchemaEcriture : {}", id);
        schemaEcritureService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
