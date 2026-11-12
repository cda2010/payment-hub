package com.m2i.omnicanal.presentation.service.impl;


import com.m2i.omnicanal.presentation.domain.RelationFse;
import com.m2i.omnicanal.presentation.repository.RelationRepository;
import com.m2i.omnicanal.presentation.service.RelationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing Relation.
 */
@Service
@Transactional
public class RelationServiceImpl implements RelationService{

    private final Logger log = LoggerFactory.getLogger(RelationServiceImpl.class);
    
    private final RelationRepository relationRepository;

    public RelationServiceImpl(RelationRepository relationRepository) {
        this.relationRepository = relationRepository;
    }

    /**
     * Save a relation.
     *
     * @param relation the entity to save
     * @return the persisted entity
     */
    @Override
    public RelationFse save(RelationFse relation) {
        log.debug("Request to save Relation : {}", relation);
        RelationFse result = relationRepository.save(relation);
        return result;
    }

    /**
     *  Get all the relations.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<RelationFse> findAll() {
        log.debug("Request to get all Relations");
        List<RelationFse> result = relationRepository.findAll();

        return result;
    }

    /**
     *  Get one relation by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public RelationFse findOne(Long id) {
        log.debug("Request to get Relation : {}", id);
        RelationFse relation = relationRepository.findOne(id);
        return relation;
    }

    /**
     *  Delete the  relation by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Relation : {}", id);
        relationRepository.deleteById(id);
    }
}
