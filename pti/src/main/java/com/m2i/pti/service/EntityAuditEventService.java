package com.m2i.pti.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.m2i.pti.domain.EntityAuditEvent;
import com.m2i.pti.mobile.dto.MAuditEvent;
import com.m2i.pti.repository.EntityAuditEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * Service Implementation for managing EntityAuditEvent.
 */
@Service
@Transactional
public class EntityAuditEventService {

    private final Logger log = LoggerFactory.getLogger(EntityAuditEventService.class);

    private final EntityAuditEventRepository entityAuditEventRepository;

    public EntityAuditEventService(EntityAuditEventRepository entityAuditEventRepository) {
        this.entityAuditEventRepository = entityAuditEventRepository;
    }

    /**
     * Save a entityAuditEvent.
     *
     * @param entityAuditEvent the entity to save
     * @return the persisted entity
     */
    public EntityAuditEvent save(EntityAuditEvent entityAuditEvent) {
        log.debug("Request to save EntityAuditEvent : {}", entityAuditEvent);
        return entityAuditEventRepository.save(entityAuditEvent);
    }

    /**
     *  Get all the entityAuditEvents.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<EntityAuditEvent> findAll(String action,String adresse,String modifiedBy,Pageable pageable) {
        log.debug("Request to get all EntityAuditEvents");
        List<EntityAuditEvent> list=new ArrayList<>();
        // return entityAuditEventRepository.findAll(pageable);
        list= entityAuditEventRepository.findAuditByCredential(action,adresse,modifiedBy);
        int start= (int) pageable.getOffset();
        int end=(start+pageable.getPageSize())>list.size()?list.size():(start+pageable.getPageSize());
        Page<EntityAuditEvent> retour= new PageImpl<>(list.subList(start, end), pageable, list.size());
        return retour;
    }

    /**
     *  Get one entityAuditEvent by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public EntityAuditEvent findOne(Long id) {
        log.debug("Request to get EntityAuditEvent : {}", id);
        return entityAuditEventRepository.findById(id).orElse(null);
    }

    /**
     *  Delete the  entityAuditEvent by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete EntityAuditEvent : {}", id);
        entityAuditEventRepository.deleteById(id);
    }

    public   List<EntityAuditEvent> findEntityValuejson(Long id,String entite) {
         return entityAuditEventRepository.findEntityValuejson(id,entite.toUpperCase());
    }

    public   Page<EntityAuditEvent> findEntityValuejsonPage(Long id,String entite,Pageable pageable) {
        return entityAuditEventRepository.findEntityValuejsonPage(id,entite.toUpperCase(),pageable);
    }


    public   List<MAuditEvent> findEntityValuejsonTrait(Page<EntityAuditEvent> page ) throws IOException {
        List<MAuditEvent> listObjet = new ArrayList();
        ObjectMapper objectMapper = new ObjectMapper();
        for (EntityAuditEvent e : page) {
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
        }

        return listObjet;
    }
}
