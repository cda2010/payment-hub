package com.m2i.omnicanal.application.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.application.domain.ParametreUser;
import com.m2i.omnicanal.application.service.ParametreUtiliateurService;
import com.m2i.omnicanal.application.web.rest.util.HeaderUtil;
import com.m2i.omnicanal.model.enumeration.ProprieteApi;

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
 * REST controller for managing ParametreUtiliateur.
 */
@RestController
@RequestMapping("/api")
public class ParametreUtiliateurResource {

    private final Logger log = LoggerFactory.getLogger(ParametreUtiliateurResource.class);

    private static final String ENTITY_NAME = "parametreUtiliateur";
        
    private final ParametreUtiliateurService parametreUtiliateurService;

    public ParametreUtiliateurResource(ParametreUtiliateurService parametreUtiliateurService) {
        this.parametreUtiliateurService = parametreUtiliateurService;
    }

    /**
     * POST  /parametre-utiliateurs : Create a new parametreUtiliateur.
     *
     * @param parametreUtiliateur the parametreUtiliateur to create
     * @return the ResponseEntity with status 201 (Created) and with body the new parametreUtiliateur, or with status 400 (Bad Request) if the parametreUtiliateur has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/parametre-utiliateurs")
    @Timed
    public ResponseEntity<ParametreUser> createParametreUtiliateur(@RequestBody ParametreUser parametreUtiliateur) throws URISyntaxException {
        log.debug("REST request to save ParametreUtiliateur : {}", parametreUtiliateur);
        
        
        
        if (parametreUtiliateur.getParametreApi().equals(ProprieteApi.EMPTY)){
                parametreUtiliateur.setParametreApi(null);
        }
        if (parametreUtiliateur.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new parametreUtiliateur cannot already have an ID")).body(null);
        }
        ParametreUser result = parametreUtiliateurService.save(parametreUtiliateur);
        return ResponseEntity.created(new URI("/api/parametre-utiliateurs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /parametre-utiliateurs : Updates an existing parametreUtiliateur.
     *
     * @param parametreUtiliateur the parametreUtiliateur to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated parametreUtiliateur,
     * or with status 400 (Bad Request) if the parametreUtiliateur is not valid,
     * or with status 500 (Internal Server Error) if the parametreUtiliateur couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/parametre-utiliateurs")
    @Timed
    public ResponseEntity<ParametreUser> updateParametreUtiliateur(@RequestBody ParametreUser parametreUtiliateur) throws URISyntaxException {
        log.debug("REST request to update ParametreUtiliateur : {}", parametreUtiliateur);
        if (parametreUtiliateur.getId() == null) {
            return createParametreUtiliateur(parametreUtiliateur);
        }
        ParametreUser result = parametreUtiliateurService.save(parametreUtiliateur);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, parametreUtiliateur.getId().toString()))
            .body(result);
    }

    /**
     * GET  /parametre-utiliateurs : get all the parametreUtiliateurs.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of parametreUtiliateurs in body
     */
    @GetMapping("/parametre-utiliateurs")
    @Timed
    public List<ParametreUser> getAllParametreUtiliateurs() {
        log.debug("REST request to get all ParametreUtiliateurs");
        return parametreUtiliateurService.findAll();
    }
    @GetMapping("/parametre-utiliateurs/service/{idService}")
    @Timed
    public List<ParametreUser> getServiceParametreUtiliateurs(@PathVariable Long idService) {
        log.debug("REST request to get all ParametreUtiliateurs");
        return parametreUtiliateurService.findByService(idService);
    }
    
     @GetMapping("/parametre-utiliateurs/interface/{idInterface}")
    @Timed
    public List<ParametreUser> getInterfaceParametreUtiliateurs(@PathVariable Long idInterface) {
        log.debug("REST request to get all ParametreUtiliateurs");
        return parametreUtiliateurService.findByInterface(idInterface);
    }
    
    
    /**
     * GET  /parametre-utiliateurs/:id : get the "id" parametreUtiliateur.
     *
     * @param id the id of the parametreUtiliateur to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the parametreUtiliateur, or with status 404 (Not Found)
     */
    @GetMapping("/parametre-utiliateurs/{id}")
    @Timed
    public ResponseEntity<ParametreUser> getParametreUtiliateur(@PathVariable Long id) {
        log.debug("REST request to get ParametreUtiliateur : {}", id);
        ParametreUser parametreUtiliateur = parametreUtiliateurService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(parametreUtiliateur));
    }

    /**
     * DELETE  /parametre-utiliateurs/:id : delete the "id" parametreUtiliateur.
     *
     * @param id the id of the parametreUtiliateur to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/parametre-utiliateurs/{id}")
    @Timed
    public ResponseEntity<Void> deleteParametreUtiliateur(@PathVariable Long id) {
        log.debug("REST request to delete ParametreUtiliateur : {}", id);
        parametreUtiliateurService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
