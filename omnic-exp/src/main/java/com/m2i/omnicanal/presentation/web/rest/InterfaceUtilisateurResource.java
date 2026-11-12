package com.m2i.omnicanal.presentation.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.presentation.domain.InterfaceUser;
import com.m2i.omnicanal.presentation.service.InterfaceUtilisateurService;
import com.m2i.omnicanal.presentation.service.PresentationService;
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
 * REST controller for managing InterfaceUtilisateur.
 */
@RestController
@RequestMapping("/api")
public class InterfaceUtilisateurResource {

    private final Logger log = LoggerFactory.getLogger(InterfaceUtilisateurResource.class);

    private static final String ENTITY_NAME = "interfaceUtilisateur";
        
    private final InterfaceUtilisateurService interfaceUtilisateurService;

    public InterfaceUtilisateurResource(InterfaceUtilisateurService interfaceUtilisateurService) {
        this.interfaceUtilisateurService = interfaceUtilisateurService;
       
    }

    /**
     * POST  /interface-utilisateurs : Create a new interfaceUtilisateur.
     *
     * @param interfaceUtilisateur the interfaceUtilisateur to create
     * @return the ResponseEntity with status 201 (Created) and with body the new interfaceUtilisateur, or with status 400 (Bad Request) if the interfaceUtilisateur has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/interface-utilisateurs")
    @Timed
    public ResponseEntity<InterfaceUser> createInterfaceUtilisateur(@RequestBody InterfaceUser interfaceUtilisateur) throws URISyntaxException {
        log.debug("REST request to save InterfaceUtilisateur : {}", interfaceUtilisateur);
        if (interfaceUtilisateur.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new interfaceUtilisateur cannot already have an ID")).body(null);
        }
        InterfaceUser result = interfaceUtilisateurService.save(interfaceUtilisateur);
        return ResponseEntity.created(new URI("/api/interface-utilisateurs/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /interface-utilisateurs : Updates an existing interfaceUtilisateur.
     *
     * @param interfaceUtilisateur the interfaceUtilisateur to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated interfaceUtilisateur,
     * or with status 400 (Bad Request) if the interfaceUtilisateur is not valid,
     * or with status 500 (Internal Server Error) if the interfaceUtilisateur couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/interface-utilisateurs")
    @Timed
    public ResponseEntity<InterfaceUser> updateInterfaceUtilisateur(@RequestBody InterfaceUser interfaceUtilisateur) throws URISyntaxException {
        log.debug("REST request to update InterfaceUtilisateur : {}", interfaceUtilisateur);
        if (interfaceUtilisateur.getId() == null) {
            return createInterfaceUtilisateur(interfaceUtilisateur);
        }
        InterfaceUser result = interfaceUtilisateurService.save(interfaceUtilisateur);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, interfaceUtilisateur.getId().toString()))
            .body(result);
    }

    /**
     * GET  /interface-utilisateurs : get all the interfaceUtilisateurs.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of interfaceUtilisateurs in body
     */
    @GetMapping("/interface-utilisateurs")
    @Timed
    public List<InterfaceUser> getAllInterfaceUtilisateurs() {
        log.debug("REST request to get all InterfaceUtilisateurs");
        return interfaceUtilisateurService.findAll();
    }

    
    
     /**
     * GET  /interface-utilisateurs : get all the interfaceUtilisateurs.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of interfaceUtilisateurs in body
     */
    @GetMapping("/interface-utilisateurs/fse/{fse}/{typeService}")
    @Timed
    public List<InterfaceUser> getListeParametreUtilisateur(@PathVariable String fse,@PathVariable Long typeService  ) {
        log.debug("REST request to get all InterfaceUtilisateurs");
        return interfaceUtilisateurService.findByServiceFse(fse,typeService);
    }
    
   

    /**
     * GET  /interface-utilisateurs/:id : get the "id" interfaceUtilisateur.
     *
     * @param id the id of the interfaceUtilisateur to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the interfaceUtilisateur, or with status 404 (Not Found)
     */
    @GetMapping("/interface-utilisateurs/{id}")
    @Timed
    public ResponseEntity<InterfaceUser> getInterfaceUtilisateur(@PathVariable Long id) {
        log.debug("REST request to get InterfaceUtilisateur : {}", id);
        InterfaceUser interfaceUtilisateur = interfaceUtilisateurService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(interfaceUtilisateur));
    }

    /**
     * DELETE  /interface-utilisateurs/:id : delete the "id" interfaceUtilisateur.
     *
     * @param id the id of the interfaceUtilisateur to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/interface-utilisateurs/{id}")
    @Timed
    public ResponseEntity<Void> deleteInterfaceUtilisateur(@PathVariable Long id) {
        log.debug("REST request to delete InterfaceUtilisateur : {}", id);
        interfaceUtilisateurService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
