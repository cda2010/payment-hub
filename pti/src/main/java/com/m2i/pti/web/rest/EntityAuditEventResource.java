package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.m2i.pti.domain.EntityAuditEvent;
import com.m2i.pti.mobile.dto.MAuditEvent;
import com.m2i.pti.repository.EntityAuditEventRepository;
import com.m2i.pti.service.EntityAuditEventService;
import com.m2i.pti.service.dto.EntityAuditEventDTO;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing EntityAuditEvent.
 */
@RestController
@RequestMapping("/api")
public class EntityAuditEventResource {
    
    private final Logger log = LoggerFactory.getLogger(EntityAuditEventResource.class);
    
    private static final String ENTITY_NAME = "entityAuditEvent";
    
    private final EntityAuditEventService entityAuditEventService;

    private final EntityAuditEventRepository entityAuditEventRepository;
    
    public EntityAuditEventResource(EntityAuditEventService entityAuditEventService, EntityAuditEventRepository entityAuditEventRepository) {
        this.entityAuditEventService = entityAuditEventService;
        this.entityAuditEventRepository = entityAuditEventRepository;
    }

    /**
     * POST /entity-audit-events : Create a new entityAuditEvent.
     *
     * @param entityAuditEvent the entityAuditEvent to create
     * @return the ResponseEntity with status 201 (Created) and with body the
     * new entityAuditEvent, or with status 400 (Bad Request) if the
     * entityAuditEvent has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/entity-audit-events")
    @Timed
    public ResponseEntity<EntityAuditEvent> createEntityAuditEvent(@RequestBody EntityAuditEvent entityAuditEvent) throws URISyntaxException {
        log.debug("REST request to save EntityAuditEvent : {}", entityAuditEvent);
        if (entityAuditEvent.getId() != null) {
            throw new BadRequestAlertException("A new entityAuditEvent cannot already have an ID", ENTITY_NAME, "idexists");
        }
        EntityAuditEvent result = entityAuditEventService.save(entityAuditEvent);
        return ResponseEntity.created(new URI("/api/entity-audit-events/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT /entity-audit-events : Updates an existing entityAuditEvent.
     *
     * @param entityAuditEvent the entityAuditEvent to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated
     * entityAuditEvent, or with status 400 (Bad Request) if the
     * entityAuditEvent is not valid, or with status 500 (Internal Server Error)
     * if the entityAuditEvent couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/entity-audit-events")
    @Timed
    public ResponseEntity<EntityAuditEvent> updateEntityAuditEvent(@RequestBody EntityAuditEvent entityAuditEvent) throws URISyntaxException {
        log.debug("REST request to update EntityAuditEvent : {}", entityAuditEvent);
        if (entityAuditEvent.getId() == null) {
            return createEntityAuditEvent(entityAuditEvent);
        }
        EntityAuditEvent result = entityAuditEventService.save(entityAuditEvent);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, entityAuditEvent.getId().toString()))
                .body(result);
    }

    /**
     * GET /entity-audit-events : get all the entityAuditEvents.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of
     * entityAuditEvents in body
     */
    @GetMapping("/entity-audit-events")
    @Timed
    public ResponseEntity<List<EntityAuditEvent>> getAllEntityAuditEvents(@RequestParam(required = false) String action, @RequestParam(required = false) String adresse, @RequestParam(required = false) String modifiedBy, Pageable pageable) {
        log.debug("REST request to get a page of EntityAuditEvents");
        Page<EntityAuditEvent> page = entityAuditEventService.findAll(action, adresse, modifiedBy, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/entity-audit-events");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/entity-audit-events-optimiser")
    @Timed
    public Page<EntityAuditEventDTO> findAll(String action,String adresse,String modifiedBy,Pageable pageable) {
        log.debug("Request to get all EntityAuditEvents");
        List<EntityAuditEventDTO> list = entityAuditEventRepository.findAuditByCredentialOptimiser(action,adresse,modifiedBy);
        int start= (int) pageable.getOffset();
        int end=(start+pageable.getPageSize())>list.size()?list.size():(start+pageable.getPageSize());
        Page<EntityAuditEventDTO> retour= new PageImpl<>(list.subList(start, end), pageable, list.size());
        return retour;
    }

    /**
     * GET /entity-audit-events/:id : get the "id" entityAuditEvent.
     *
     * @param id the id of the entityAuditEvent to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the
     * entityAuditEvent, or with status 404 (Not Found)
     */
    @GetMapping("/entity-audit-events/{id}")
    @Timed
    public ResponseEntity<EntityAuditEvent> getEntityAuditEvent(@PathVariable Long id) {
        log.debug("REST request to get EntityAuditEvent : {}", id);
        EntityAuditEvent entityAuditEvent = entityAuditEventService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(entityAuditEvent));
    }

    /**
     * DELETE /entity-audit-events/:id : delete the "id" entityAuditEvent.
     *
     * @param id the id of the entityAuditEvent to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/entity-audit-events/{id}")
    @Timed
    public ResponseEntity<Void> deleteEntityAuditEvent(@PathVariable Long id) {
        log.debug("REST request to delete EntityAuditEvent : {}", id);
        entityAuditEventService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
    
    @GetMapping("/entity-audit-events/entite")
    @Timed
    public ResponseEntity<List<?>> getEntityAuditEventContant(@RequestParam Long id, @RequestParam String entite,Pageable pageable) throws IOException {
        log.debug("REST request to get EntityAuditEvent : {}", id);
        List<MAuditEvent> listObjet = new ArrayList();
        ObjectMapper objectMapper = new ObjectMapper();
        
        Page<EntityAuditEvent> listEvt = entityAuditEventService.findEntityValuejsonPage(id, entite,pageable);
        /*for (EntityAuditEvent e : listEvt) {
            
            MAuditEvent mAuditEvent = new MAuditEvent();
            Object objectValueNew = objectMapper.readValue(e.getEntityValuejson(), Object.class);
            if(e.getEntityOldValue()!=null){
                Object objectValueOld = objectMapper.readValue(e.getEntityOldValue(), Object.class);
                mAuditEvent.setObjetAvantModif(objectValueOld);
            }
            mAuditEvent.setObjetApresModif(objectValueNew);
            mAuditEvent.setAction(e.getAction());
            mAuditEvent.setModifiedBy(e.getModifiedBy());
            mAuditEvent.setModifiedDate(e.getModifiedDate());
            listObjet.add(mAuditEvent);
        }*/
        listObjet=entityAuditEventService.findEntityValuejsonTrait(listEvt);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(listEvt, "/client");
        return new ResponseEntity<>(listObjet, headers, HttpStatus.OK);
        //return ResponseUtil.wrapOrNotFound(Optional.ofNullable(listObjet));
    }
}
