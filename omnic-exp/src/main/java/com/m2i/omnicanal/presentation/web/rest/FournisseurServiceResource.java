package com.m2i.omnicanal.presentation.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.model.FournisseurService;
import com.m2i.omnicanal.presentation.domain.FournisseurFSE;

import com.m2i.omnicanal.presentation.domain.dto.ListFournisseurDto;
import com.m2i.omnicanal.presentation.domain.dto.ListFournisseurServiceDto;
import com.m2i.omnicanal.presentation.service.FournisseurServiceService;
import com.m2i.omnicanal.presentation.service.dto.FournisseurServiceDto;
import com.m2i.omnicanal.presentation.web.rest.util.HeaderUtil;

import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
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

    public FournisseurServiceResource(FournisseurServiceService fournisseurServiceService) {
        this.fournisseurServiceService = fournisseurServiceService;
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
    public List<FournisseurFSE> getAllFournisseurServices() {
        log.debug("REST request to get all FournisseurServices");
        return fournisseurServiceService.findAll();
    }
 @GetMapping("/fournisseur-services-ext")
    @Timed
    public   List<ListFournisseurServiceDto>  getFournisseurDtoServices() {
        log.debug("REST request to get all FournisseurServices");
        List<FournisseurFSE> listfse= fournisseurServiceService.findAll();
         List<ListFournisseurServiceDto> listFseDto = new ArrayList();
        Iterator it = listfse.iterator();
        ListFournisseurDto listFournisseurDto=new ListFournisseurDto();
        while (it.hasNext()){
            FournisseurFSE fournisseurFSE= (FournisseurFSE)it.next();
            ListFournisseurServiceDto fseDto= new ListFournisseurServiceDto();
            fseDto.setCodeFse(fournisseurFSE.getCodeFse());
            fseDto.setLibelleFse(fournisseurFSE.getNom());
            fseDto.setCodeExterne(fournisseurFSE.getCodeExterne());
            listFseDto.add(fseDto);
        }
        listFournisseurDto.setFournisseurServiceDto(listFseDto);
        return listFseDto;
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

    
     @GetMapping("/fournisseur-services/compense/{codeFse}")
    @Timed
    public FournisseurServiceDto getFournisseurService(@PathVariable String codeFse) {
      
        FournisseurFSE fournisseurService =  (FournisseurFSE) fournisseurServiceService.findByCodeFse(codeFse);
        
        FournisseurServiceDto fseDto= new FournisseurServiceDto();
        if (fournisseurService.getFseCompense()!=null){
             fseDto.setCodeFse(fournisseurService.getFseCompense().getCodeFse());
                fseDto.setLibelleFse(fournisseurService.getFseCompense().getNom());
        }
       
        return fseDto;
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
