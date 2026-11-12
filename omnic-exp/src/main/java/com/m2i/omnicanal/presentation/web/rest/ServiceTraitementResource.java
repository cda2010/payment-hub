package com.m2i.omnicanal.presentation.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.presentation.domain.Service;
import com.m2i.omnicanal.presentation.service.ServiceTraitementService;
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
 * REST controller for managing ServiceTraitement.
 */
@RestController
@RequestMapping("/api")
public class ServiceTraitementResource {

    private final Logger log = LoggerFactory.getLogger(ServiceTraitementResource.class);

    private static final String ENTITY_NAME = "serviceTraitement";
        
    private final ServiceTraitementService serviceTraitementService;

    public ServiceTraitementResource(ServiceTraitementService serviceTraitementService) {
        this.serviceTraitementService = serviceTraitementService;
    }

    /**
     * POST  /service-traitements : Create a new serviceTraitement.
     *
     * @param serviceTraitement the serviceTraitement to create
     * @return the ResponseEntity with status 201 (Created) and with body the new serviceTraitement, or with status 400 (Bad Request) if the serviceTraitement has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/service-traitements")
    @Timed
    public ResponseEntity<Service> createServiceTraitement(@RequestBody Service serviceTraitement) throws URISyntaxException {
        log.debug("REST request to save ServiceTraitement : {}", serviceTraitement);
        if (serviceTraitement.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new serviceTraitement cannot already have an ID")).body(null);
        }
        Service result = serviceTraitementService.save(serviceTraitement);
        return ResponseEntity.created(new URI("/api/service-traitements/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /service-traitements : Updates an existing serviceTraitement.
     *
     * @param serviceTraitement the serviceTraitement to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated serviceTraitement,
     * or with status 400 (Bad Request) if the serviceTraitement is not valid,
     * or with status 500 (Internal Server Error) if the serviceTraitement couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/service-traitements")
    @Timed
    public ResponseEntity<Service> updateServiceTraitement(@RequestBody Service serviceTraitement) throws URISyntaxException {
        log.debug("REST request to update ServiceTraitement : {}", serviceTraitement);
        if (serviceTraitement.getId() == null) {
            return createServiceTraitement(serviceTraitement);
        }
        Service result = serviceTraitementService.save(serviceTraitement);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, serviceTraitement.getId().toString()))
            .body(result);
    }

    /**
     * GET  /service-traitements : get all the serviceTraitements.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of serviceTraitements in body
     */
    @GetMapping("/service-traitements")
    @Timed
    public List<Service> getAllServiceTraitements() {
        log.debug("REST request to get all ServiceTraitements");
        return serviceTraitementService.findAll();
    }

    /**
     * GET  /service-traitements/:id : get the "id" serviceTraitement.
     *
     * @param id the id of the serviceTraitement to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the serviceTraitement, or with status 404 (Not Found)
     */
    @GetMapping("/service-traitements/{id}")
    @Timed
    public ResponseEntity<Service> getServiceTraitement(@PathVariable Long id) {
        log.debug("REST request to get ServiceTraitement : {}", id);
        Service serviceTraitement = serviceTraitementService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(serviceTraitement));
    }

    /**
     * DELETE  /service-traitements/:id : delete the "id" serviceTraitement.
     *
     * @param id the id of the serviceTraitement to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/service-traitements/{id}")
    @Timed
    public ResponseEntity<Void> deleteServiceTraitement(@PathVariable Long id) {
        log.debug("REST request to delete ServiceTraitement : {}", id);
        serviceTraitementService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
