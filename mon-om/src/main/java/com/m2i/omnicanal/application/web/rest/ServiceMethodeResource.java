package com.m2i.omnicanal.application.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.application.domain.ServiceMethod;

import com.m2i.omnicanal.application.service.ServiceMethodeService;
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
 * REST controller for managing ServiceMethode.
 */
@RestController
@RequestMapping("/api")
public class ServiceMethodeResource {

    private final Logger log = LoggerFactory.getLogger(ServiceMethodeResource.class);

    private static final String ENTITY_NAME = "serviceMethode";
        
    private final ServiceMethodeService serviceMethodeService;

    public ServiceMethodeResource(ServiceMethodeService serviceMethodeService) {
        this.serviceMethodeService = serviceMethodeService;
    }

    /**
     * POST  /service-methodes : Create a new serviceMethode.
     *
     * @param serviceMethode the serviceMethode to create
     * @return the ResponseEntity with status 201 (Created) and with body the new serviceMethode, or with status 400 (Bad Request) if the serviceMethode has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/service-methodes")
    @Timed
    public ResponseEntity<ServiceMethod> createServiceMethode(@RequestBody ServiceMethod serviceMethode) throws URISyntaxException {
        log.debug("REST request to save ServiceMethode : {}", serviceMethode);
        if (serviceMethode.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new serviceMethode cannot already have an ID")).body(null);
        }
        ServiceMethod result = serviceMethodeService.save(serviceMethode);
        return ResponseEntity.created(new URI("/api/service-methodes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /service-methodes : Updates an existing serviceMethode.
     *
     * @param serviceMethode the serviceMethode to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated serviceMethode,
     * or with status 400 (Bad Request) if the serviceMethode is not valid,
     * or with status 500 (Internal Server Error) if the serviceMethode couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/service-methodes")
    @Timed
    public ResponseEntity<ServiceMethod> updateServiceMethode(@RequestBody ServiceMethod serviceMethode) throws URISyntaxException {
        log.debug("REST request to update ServiceMethode : {}", serviceMethode);
        if (serviceMethode.getId() == null) {
            return createServiceMethode(serviceMethode);
        }
        ServiceMethod result = serviceMethodeService.save(serviceMethode);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, serviceMethode.getId().toString()))
            .body(result);
    }

    /**
     * GET  /service-methodes : get all the serviceMethodes.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of serviceMethodes in body
     */
    @GetMapping("/service-methodes")
    @Timed
    public List<ServiceMethod> getAllServiceMethodes() {
        log.debug("REST request to get all ServiceMethodes");
        return serviceMethodeService.findAll();
    }

    
     @GetMapping("/service-methodes/service/{idService}")
    @Timed
    public List<ServiceMethod> getServiceMethodeSelect(@PathVariable Long idService) {
        log.debug("REST request to get all ServiceMethodes");
        return serviceMethodeService.findByService(idService);
    }
    
    /**
     * GET  /service-methodes/:id : get the "id" serviceMethode.
     *
     * @param id the id of the serviceMethode to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the serviceMethode, or with status 404 (Not Found)
     */
    @GetMapping("/service-methodes/{id}")
    @Timed
    public ResponseEntity<ServiceMethod> getServiceMethode(@PathVariable Long id) {
        log.debug("REST request to get ServiceMethode : {}", id);
        ServiceMethod serviceMethode = serviceMethodeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(serviceMethode));
    }

    /**
     * DELETE  /service-methodes/:id : delete the "id" serviceMethode.
     *
     * @param id the id of the serviceMethode to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/service-methodes/{id}")
    @Timed
    public ResponseEntity<Void> deleteServiceMethode(@PathVariable Long id) {
        log.debug("REST request to delete ServiceMethode : {}", id);
        serviceMethodeService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
