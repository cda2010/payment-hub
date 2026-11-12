package com.m2i.omnicanal.application.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.application.domain.AgenceFse;
import com.m2i.omnicanal.application.domain.CompteFse;
import com.m2i.omnicanal.application.service.CompteService;
import com.m2i.omnicanal.application.web.rest.util.HeaderUtil;
import com.m2i.omnicanal.application.web.rest.util.PaginationUtil;

import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

/**
 * REST controller for managing Compte.
 */
@RestController
@RequestMapping("/api")
public class CompteResource {

    private final Logger log = LoggerFactory.getLogger(CompteResource.class);

    private static final String ENTITY_NAME = "compte";
        
    private final CompteService compteService;

    public CompteResource(CompteService compteService) {
        this.compteService = compteService;
    }

    /**
     * POST  /comptes : Create a new compte.
     *
     * @param compte the compte to create
     * @return the ResponseEntity with status 201 (Created) and with body the new compte, or with status 400 (Bad Request) if the compte has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/comptes")
    @Timed
    public ResponseEntity<CompteFse> createCompte(@RequestBody CompteFse compte) throws URISyntaxException {
        log.debug("REST request to save Compte : {}", compte);
        if (compte.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new compte cannot already have an ID")).body(null);
        }
        CompteFse result = compteService.save(compte);
        return ResponseEntity.created(new URI("/api/comptes/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /comptes : Updates an existing compte.
     *
     * @param compte the compte to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated compte,
     * or with status 400 (Bad Request) if the compte is not valid,
     * or with status 500 (Internal Server Error) if the compte couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/comptes")
    @Timed
    public ResponseEntity<CompteFse> updateCompte(@RequestBody CompteFse compte) throws URISyntaxException {
        log.debug("REST request to update Compte : {}", compte);
        if (compte.getId() == null) {
            return createCompte(compte);
        }
        CompteFse result = compteService.save(compte);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, compte.getId().toString()))
            .body(result);
    }
    
  
 @GetMapping("/comptes/{teneur}/{titulaire}/{type}")
    @Timed
    public ResponseEntity<List<CompteFse>> getAllComptes(@ApiParam Pageable pageable,@PathVariable Long teneur, @PathVariable Long titulaire,@PathVariable Long type) {
        log.debug("REST request to get a page of comptes");
        Long ptitulaire;
        Long pteneur;
        Long ptype;
        if (titulaire!=0){
            log.debug("titulaore noteIs null"+titulaire);
           ptitulaire=titulaire; 
        }else{
            log.debug("titu Is null");
            ptitulaire=null;
        }
            
        
         if (teneur!=0){
            pteneur=teneur;
         }else{
              log.debug("Teneur Is null");
               pteneur=null;
         }
         
         if (type!=0){
             
               ptype=type;
         }else{
             log.debug("Type Is null");
               ptype=null;
         }
          
         
         
        Page<CompteFse> page = compteService.findAll(pageable, pteneur,ptitulaire,ptype  );
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/comptes");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
    /**
     * GET  /comptes : get all the comptes.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of comptes in body
     */
   
    /**
     * GET  /comptes : get all the comptes.
     *
     * @param idrelation
     * @return the ResponseEntity with status 200 (OK) and the list of comptes in body
     */
    

    @GetMapping("/comptes/relation/{idrelation}")
    @Timed
    public List<CompteFse> getComptesByRelation(@PathVariable Long idrelation) {
        log.debug("REST request to get all Comptes");
        return compteService.findByRelation(idrelation);
    }
      @GetMapping("/comptes/mirroir/{idCompte}")
    @Timed
    public List<CompteFse> getCompteMirroir(@PathVariable Long idCompte) {
        log.debug("REST request to get all Comptes");
        
        return compteService.findCompteMorroir(idCompte);
    }
    /**
     * GET  /comptes/:id : get the "id" compte.
     *
     * @param id the id of the compte to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the compte, or with status 404 (Not Found)
     */
    @GetMapping("/comptes/{id}")
    @Timed
    public ResponseEntity<CompteFse> getCompte(@PathVariable Long id) {
        log.debug("REST request to get Compte : {}", id);
        CompteFse compte = compteService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(compte));
    }

    /**
     * DELETE  /comptes/:id : delete the "id" compte.
     *
     * @param id the id of the compte to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/comptes/{id}")
    @Timed
    public ResponseEntity<Void> deleteCompte(@PathVariable Long id) {
        log.debug("REST request to delete Compte : {}", id);
        compteService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
