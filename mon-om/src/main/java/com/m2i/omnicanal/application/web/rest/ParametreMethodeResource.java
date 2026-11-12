package com.m2i.omnicanal.application.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.application.domain.ParametreMethod;

import com.m2i.omnicanal.application.service.ParametreMethodeService;
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
 * REST controller for managing ParametreMethode.
 */
@RestController
@RequestMapping("/api")
public class ParametreMethodeResource {

    private final Logger log = LoggerFactory.getLogger(ParametreMethodeResource.class);

    private static final String ENTITY_NAME = "parametreMethode";
        
    private final ParametreMethodeService parametreMethodeService;

    public ParametreMethodeResource(ParametreMethodeService parametreMethodeService) {
        this.parametreMethodeService = parametreMethodeService;
    }

    /**
     * POST  /parametre-methodes : Create a new parametreMethode.
     *
     * @param parametreMethode the parametreMethode to create
     * @return the ResponseEntity with status 201 (Created) and with body the new parametreMethode, or with status 400 (Bad Request) if the parametreMethode has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/parametre-methodes")
    @Timed
    public ResponseEntity<ParametreMethod> createParametreMethode(@RequestBody ParametreMethod parametreMethode) throws URISyntaxException {
        log.debug("REST request to save ParametreMethode : {}", parametreMethode);
        if (parametreMethode.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new parametreMethode cannot already have an ID")).body(null);
        }
        ParametreMethod result = parametreMethodeService.save(parametreMethode);
        return ResponseEntity.created(new URI("/api/parametre-methodes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /parametre-methodes : Updates an existing parametreMethode.
     *
     * @param parametreMethode the parametreMethode to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated parametreMethode,
     * or with status 400 (Bad Request) if the parametreMethode is not valid,
     * or with status 500 (Internal Server Error) if the parametreMethode couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/parametre-methodes")
    @Timed
    public ResponseEntity<ParametreMethod> updateParametreMethode(@RequestBody ParametreMethod parametreMethode) throws URISyntaxException {
        log.debug("REST request to update ParametreMethode : {}", parametreMethode);
        if (parametreMethode.getId() == null) {
            return createParametreMethode(parametreMethode);
        }
        ParametreMethod result = parametreMethodeService.save(parametreMethode);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, parametreMethode.getId().toString()))
            .body(result);
    }

    /**
     * GET  /parametre-methodes : get all the parametreMethodes.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of parametreMethodes in body
     */
    @GetMapping("/parametre-methodes")
    @Timed
    public List<ParametreMethod> getAllParametreMethodes() {
        log.debug("REST request to get all ParametreMethodes");
        return parametreMethodeService.findAll();
    }
    
     @GetMapping("/parametre-methodes/methode/{idmethode}")
    @Timed
    public List<ParametreMethod> getParame(@PathVariable Long idmethode) {
        log.debug("REST request to get all ParametreMethodes");
        return parametreMethodeService.findByMethode(idmethode);
    }
    

    /**
     * GET  /parametre-methodes/:id : get the "id" parametreMethode.
     *
     * @param id the id of the parametreMethode to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the parametreMethode, or with status 404 (Not Found)
     */
    @GetMapping("/parametre-methodes/{id}")
    @Timed
    public ResponseEntity<ParametreMethod> getParametreMethode(@PathVariable Long id) {
        log.debug("REST request to get ParametreMethode : {}", id);
        ParametreMethod parametreMethode = parametreMethodeService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(parametreMethode));
    }

    /**
     * DELETE  /parametre-methodes/:id : delete the "id" parametreMethode.
     *
     * @param id the id of the parametreMethode to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/parametre-methodes/{id}")
    @Timed
    public ResponseEntity<Void> deleteParametreMethode(@PathVariable Long id) {
        log.debug("REST request to delete ParametreMethode : {}", id);
        parametreMethodeService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
