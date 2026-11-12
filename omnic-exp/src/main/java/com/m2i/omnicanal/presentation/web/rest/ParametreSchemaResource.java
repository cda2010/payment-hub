package com.m2i.omnicanal.presentation.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.model.FournisseurService;
import com.m2i.omnicanal.model.LigneSchemaEcriture;
import com.m2i.omnicanal.model.TypeService;
import com.m2i.omnicanal.presentation.domain.ParametreSchem;
import com.m2i.omnicanal.presentation.domain.dto.SchemaEcritureDto;
import com.m2i.omnicanal.presentation.service.FournisseurServiceService;

import com.m2i.omnicanal.presentation.service.ParametreSchemaService;
import com.m2i.omnicanal.presentation.service.PresentationService;
import com.m2i.omnicanal.presentation.service.TypeServiceService;
import com.m2i.omnicanal.presentation.web.rest.util.HeaderUtil;

import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * REST controller for managing parametreSchema.
 */
@RestController
@RequestMapping("/api")
public class ParametreSchemaResource {

    private final Logger log = LoggerFactory.getLogger(ParametreSchemaResource.class);

    private static final String ENTITY_NAME = "parametreSchema";
        
    private final ParametreSchemaService parametreSchemaService;
private final PresentationService presentationService;
private final TypeServiceService typeServiceService;
private final FournisseurServiceService fournisseurServiceService;
    public ParametreSchemaResource(TypeServiceService typeServiceService,ParametreSchemaService parametreSchemaService,PresentationService presentationService,FournisseurServiceService fournisseurServiceService) {
        this.parametreSchemaService = parametreSchemaService;
         this.presentationService=presentationService;
         this.fournisseurServiceService=fournisseurServiceService;
         this.typeServiceService=typeServiceService;
    }

    /**
     * POST  /parametre-schemas : Create a new parametreSchema.
     *
     * @param parametreSchema
   
     * @return the ResponseEntity with status 201 (Created) and with body the new parametreSchema, or with status 400 (Bad Request) if the parametreSchema has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/parametre-schemas")
    @Timed
    public ResponseEntity<ParametreSchem> createParametreSchema(@RequestBody ParametreSchem parametreSchema) throws URISyntaxException {
        log.debug("REST request to save parametreSchema : {}", parametreSchema);
        if (parametreSchema.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new parametreSchema cannot already have an ID")).body(null);
        }
        ParametreSchem result = parametreSchemaService.save(parametreSchema);
        return ResponseEntity.created(new URI("/api/parametre-schemas/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /parametre-schemas : Updates an existing parametreMethode.
     *
     * @param parametreSchema
     * @param parametreSchema the parametreSchema to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated parametreSchema,
     * or with status 400 (Bad Request) if the parametreSchema is not valid,
     * or with status 500 (Internal Server Error) if the parametreSchema couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    
      @PutMapping("/parametre-schemas")
    @Timed
    public ResponseEntity<ParametreSchem> updateParametreSchema(@RequestBody ParametreSchem parametreSchema) throws URISyntaxException {
        log.debug("REST request to update ParametreUtiliateur : {}", parametreSchema);
        if (parametreSchema.getId() == null) {
            return createParametreSchema(parametreSchema);
        }
        ParametreSchem result = parametreSchemaService.save(parametreSchema);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, parametreSchema.getId().toString()))
            .body(result);
    }

    @GetMapping("/parametre-schemas/schema/{typeService}/{fseSource}/{fseDest}")
    @Timed
    public List<SchemaEcritureDto>getParametreFromSchemas(@PathVariable Long typeService,@PathVariable String fseSource,@PathVariable String fseDest) {
        
     FournisseurService  fournisseurServiceSource;
        FournisseurService  fournisseurServiceDest;
     fournisseurServiceSource=   fournisseurServiceService.findByCodeFse(fseSource);
      fournisseurServiceDest=   fournisseurServiceService.findByCodeFse(fseDest);
      TypeService tpService=typeServiceService.findOne(typeService);
       Set<LigneSchemaEcriture> listSchema=  presentationService.findSchema(tpService,fournisseurServiceSource,fournisseurServiceDest);
      
        List<SchemaEcritureDto> listSchemadto=new ArrayList();
     if (listSchemadto!=null){
       Iterator it = listSchema.iterator();
       while (it.hasNext()){
           LigneSchemaEcriture ligne = (LigneSchemaEcriture)it.next();
            SchemaEcritureDto schemaDto=new SchemaEcritureDto();
            schemaDto.setLigneSchema(ligne);
            schemaDto.setParametreSchema(ligne.getInscrits());
           listSchemadto.add(schemaDto);
       }
     }
        log.debug("REST request to get all parametreSchema");
        return listSchemadto;
    }
    

    /**
     * GET  /parametre-schemas : get all the parametreSchema.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of parametreSchema in body
     */
    @GetMapping("/parametre-schemas")
    @Timed
    public List<ParametreSchem> getAllParametreSchemas() {
        log.debug("REST request to get all parametreSchema");
        return parametreSchemaService.findAll();
    }
    
     @GetMapping("/parametre-schemas/ligne/{idligne}")
    @Timed
    public List<ParametreSchem> getParametreSchemaByligne(@PathVariable Long idligne) {
        log.debug("REST request to get all parametreSchema");
        return parametreSchemaService.findByLigne(idligne);
    }
    

    /**
     * GET  /parametre-schemas/:id : get the "id" parametreSchema.
     *
     * @param id the id of the parametreSchema to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the parametreSchema, or with status 404 (Not Found)
     */
    @GetMapping("/parametre-schemas/{id}")
    @Timed
    public ResponseEntity<ParametreSchem> getParametreSchema(@PathVariable Long id) {
        log.debug("REST request to get ParametreMethode : {}", id);
        ParametreSchem parametreSchema = parametreSchemaService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(parametreSchema));
    }

    /**
     * DELETE  /parametre-schemas/:id : delete the "id" parametreSchema.
     *
     * @param id the id of the parametreMethode to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/parametre-schemas/{id}")
    @Timed
    public ResponseEntity<Void> deleteParametreSchema(@PathVariable Long id) {
        log.debug("REST request to delete parametreSchema : {}", id);
        parametreSchemaService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

   

}
