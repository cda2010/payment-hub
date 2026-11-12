package com.m2i.omnicanal.presentation.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.presentation.domain.RelationFse;
import com.m2i.omnicanal.presentation.service.RelationService;
import com.m2i.omnicanal.presentation.service.dto.FournisseurServiceDto;
import com.m2i.omnicanal.presentation.service.dto.RelationDto;
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

/**
 * REST controller for managing Relation.
 */
@RestController
@RequestMapping("/api")
public class RelationResource {

    private final Logger log = LoggerFactory.getLogger(RelationResource.class);

    private static final String ENTITY_NAME = "relation";
        
    private final RelationService relationService;

    public RelationResource(RelationService relationService) {
        this.relationService = relationService;
    }

    /**
     * POST  /relations : Create a new relation.
     *
     * @param relation the relation to create
     * @return the ResponseEntity with status 201 (Created) and with body the new relation, or with status 400 (Bad Request) if the relation has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/relations")
    @Timed
    public ResponseEntity<RelationFse> createRelation(@RequestBody RelationFse relation) throws URISyntaxException {
        log.debug("REST request to save Relation : {}", relation);
        if (relation.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new relation cannot already have an ID")).body(null);
        }
        RelationFse result = relationService.save(relation);
        return ResponseEntity.created(new URI("/api/relations/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /relations : Updates an existing relation.
     *
     * @param relation the relation to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated relation,
     * or with status 400 (Bad Request) if the relation is not valid,
     * or with status 500 (Internal Server Error) if the relation couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/relations")
    @Timed
    public ResponseEntity<RelationFse> updateRelation(@RequestBody RelationFse relation) throws URISyntaxException {
        log.debug("REST request to update Relation : {}", relation);
        if (relation.getId() == null) {
            return createRelation(relation);
        }
        RelationFse result = relationService.save(relation);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, relation.getId().toString()))
            .body(result);
    }

    /**
     * GET  /relations : get all the relations.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of relations in body
     */
    @GetMapping("/relations")
    @Timed
    public List<RelationFse> getAllRelations() {
        log.debug("REST request to get all Relations");
        return relationService.findAll();
    }
    @GetMapping("/listrelations")
    @Timed
    public List<RelationDto> getAllRelationsDto() {
        log.debug("REST request to get all Relations");
         List<RelationDto> listRelDto=  new ArrayList();
      List<RelationFse> listRel=  relationService.findAll();
      Iterator it= listRel.iterator();
      while (it.hasNext()){
          RelationFse relFse= (RelationFse)it.next();
          FournisseurServiceDto fourDtoS= new FournisseurServiceDto();
          FournisseurServiceDto fourDtoD= new FournisseurServiceDto();
          fourDtoS.setCodeFse(relFse.getFseSource().getCodeFse());
          fourDtoS.setLibelleFse(relFse.getFseSource().getNom());
          fourDtoD.setCodeFse(relFse.getFseDestination().getCodeFse());
          fourDtoD.setLibelleFse(relFse.getFseDestination().getNom());
          
          RelationDto relDto = new  RelationDto();
          relDto.setFseSource(fourDtoS);
         relDto.setFseDestination(fourDtoD);
         listRelDto.add(relDto);
         
      }
        return listRelDto;
    }

    /**
     * GET  /relations/:id : get the "id" relation.
     *
     * @param id the id of the relation to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the relation, or with status 404 (Not Found)
     */
    @GetMapping("/relations/{id}")
    @Timed
    public ResponseEntity<RelationFse> getRelation(@PathVariable Long id) {
        log.debug("REST request to get Relation : {}", id);
        RelationFse relation = relationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(relation));
    }

    /**
     * DELETE  /relations/:id : delete the "id" relation.
     *
     * @param id the id of the relation to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/relations/{id}")
    @Timed
    public ResponseEntity<Void> deleteRelation(@PathVariable Long id) {
        log.debug("REST request to delete Relation : {}", id);
        relationService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
