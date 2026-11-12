package com.m2i.omnicanal.application.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.application.repository.FournisseurServiceRepository;
import com.m2i.omnicanal.application.domain.FournisseurFSE;
import com.m2i.omnicanal.application.service.FournisseurServiceService;
import com.m2i.omnicanal.application.web.rest.util.HeaderUtil;

import com.m2i.omnicanal.application.web.rest.util.PaginationUtil;
import com.m2i.omnicanal.model.Api;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing FournisseurService.
 */
@RestController
@RequestMapping("/api")
public class FournisseurServiceResource {

    private final Logger log = LoggerFactory.getLogger(FournisseurServiceResource.class);

    private static final String ENTITY_NAME = "fournisseurService";

    private final FournisseurServiceService fournisseurServiceService;

    private final FournisseurServiceRepository FSRepository;

    public FournisseurServiceResource(FournisseurServiceService fournisseurServiceService, FournisseurServiceRepository fsRepository) {
        this.fournisseurServiceService = fournisseurServiceService;
        FSRepository = fsRepository;
    }

    /**
     * POST  /fournisseur-services : Create a new fournisseurService.
     *
     * @param fournisseurService the fournisseurService to create
     * @return the ResponseEntity with status 201 (Created) and with body the new fournisseurService, or with status 400 (Bad Request) if the fournisseurService has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/fournisseur-services")
    @Timed
    public ResponseEntity<FournisseurFSE> createFournisseurService(@Valid @RequestBody FournisseurFSE fournisseurService) throws URISyntaxException {
        log.debug("REST request to save FournisseurService : {}", fournisseurService);
        if (fournisseurService.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new fournisseurService cannot already have an ID")).body(null);
        }
        FournisseurFSE result = fournisseurServiceService.save(fournisseurService);
        return ResponseEntity.created(new URI("/api/fournisseur-services/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /fournisseur-services : Updates an existing fournisseurService.
     *
     * @param fournisseurService the fournisseurService to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated fournisseurService,
     * or with status 400 (Bad Request) if the fournisseurService is not valid,
     * or with status 500 (Internal Server Error) if the fournisseurService couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/fournisseur-services")
    @Timed
    public ResponseEntity<FournisseurFSE> updateFournisseurService(@Valid @RequestBody FournisseurFSE fournisseurService) throws URISyntaxException {
        log.debug("REST request to update FournisseurService : {}", fournisseurService);
        if (fournisseurService.getId() == null) {
            return createFournisseurService(fournisseurService);
        }
        FournisseurFSE result = fournisseurServiceService.save(fournisseurService);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, fournisseurService.getId().toString()))
            .body(result);
    }

    /**
     * GET  /fournisseur-services : get all the fournisseurServices.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of fournisseurServices in body
     */
    @GetMapping("/fournisseur-services")
    @Timed
//    public List<FournisseurFSE> getAllFournisseurServices() {
//        log.debug("REST request to get all FournisseurServices");
//        return fournisseurServiceService.findAll();
//    }
    public ResponseEntity<List<FournisseurFSE>> getAllFournisseurServices(Pageable pageable) {
        log.debug("REST request to get all FournisseurServices");
        Page<FournisseurFSE> page = FSRepository.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/apis");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);

    }
    
    
     @GetMapping("/fournisseur-services/list")
    @Timed
//    public List<FournisseurFSE> getAllFournisseurServices() {
//        log.debug("REST request to get all FournisseurServices");
//        return fournisseurServiceService.findAll();
//    }
    public List<FournisseurFSE> getAllFournisseurServices() {
        log.debug("REST request to get all FournisseurServices");
        List <FournisseurFSE> page = FSRepository.findAll();
  
        return page;

    }
    
    
 @GetMapping("/fournisseur-services/compense/{idFse}")
    @Timed
    public List<FournisseurFSE> getAllFournisseurCompenseServices(@PathVariable Long idFse) {
        log.debug("REST request to get all FournisseurServices");
        return fournisseurServiceService.findCompense(idFse);
    }

    /**
     * GET  /fournisseur-services/:id : get the "id" fournisseurService.
     *
     * @param id the id of the fournisseurService to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the fournisseurService, or with status 404 (Not Found)
     */
    @GetMapping("/fournisseur-services/{id}")
    @Timed
    public ResponseEntity<FournisseurFSE> getFournisseurService(@PathVariable Long id) {
        log.debug("REST request to get FournisseurService : {}", id);
        FournisseurFSE fournisseurService = fournisseurServiceService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(fournisseurService));
    }

    /**
     * DELETE  /fournisseur-services/:id : delete the "id" fournisseurService.
     *
     * @param id the id of the fournisseurService to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/fournisseur-services/{id}")
    @Timed
    public ResponseEntity<Void> deleteFournisseurService(@PathVariable Long id) {
        log.debug("REST request to delete FournisseurService : {}", id);
        fournisseurServiceService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
