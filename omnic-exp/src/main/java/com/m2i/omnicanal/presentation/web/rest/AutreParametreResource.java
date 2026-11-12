package com.m2i.omnicanal.presentation.web.rest;

import com.codahale.metrics.annotation.Timed;

import com.m2i.omnicanal.model.AutreParametre;
import com.m2i.omnicanal.presentation.domain.AutreParametreFSE;
import com.m2i.omnicanal.presentation.service.AutreParametreService;
import com.m2i.omnicanal.presentation.web.rest.util.HeaderUtil;

import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing AutreParametre.
 */
@RestController
@RequestMapping("/api")
public class AutreParametreResource {

    private final Logger log = LoggerFactory.getLogger(AutreParametreResource.class);

    private static final String ENTITY_NAME = "autreParametre";
        
    private final AutreParametreService autreParametreService;

    public AutreParametreResource(AutreParametreService autreParametreService) {
        this.autreParametreService = autreParametreService;
    }

    
    
    
    @PostMapping("/autre-parametres/fse")
    @Timed
    public List<AutreParametre> getAllOtreParametresFSE( @RequestParam("fseCode") String fseCode) {
        log.debug("REST request to get all AutreParametres");
        return autreParametreService.findByFournisseurService(fseCode);
    }
    
    
    
     @PostMapping("/autre-parametres/valeur")
    @Timed
    public String getAllValeurParametresFSE(
            
              @RequestParam("fseCode") String fseCode,
              @RequestParam("codeParam") String codeParam
        ) {
        log.debug("REST request to get all AutreParametres");
        List<AutreParametre> resultat=  autreParametreService.findByFournisseurService(fseCode);
        String valeur=     rechercheParametre(  resultat,  codeParam);
        return valeur;
    }

    /**
     * GET  /autre-parametres/:id : get the "id" autreParametre.
     *
     * @param id the id of the autreParametre to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the autreParametre, or with status 404 (Not Found)
     */
    @GetMapping("/autre-parametres/{id}")
    @Timed
    public ResponseEntity<AutreParametre> getAutreParametre(@PathVariable Long id) {
        log.debug("REST request to get AutreParametre : {}", id);
        AutreParametre autreParametre = autreParametreService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(autreParametre));
    }

    /**
     * DELETE  /autre-parametres/:id : delete the "id" autreParametre.
     *
     * @param id the id of the autreParametre to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/autre-parametres/{id}")
    @Timed
    public ResponseEntity<Void> deleteAutreParametre(@PathVariable Long id) {
        log.debug("REST request to delete AutreParametre : {}", id);
        autreParametreService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
 
     private String rechercheParametre(  List<AutreParametre> listparam, String code){
       String valeur= null;
       Iterator it = listparam.iterator();
       AutreParametre  param;
       while(it.hasNext()) {
           param=(AutreParametre) it.next();
           if (param.getCodeParametre().equals(code)){
                     return param.getValeur();
           }       
       }
    return valeur;
    }
}
