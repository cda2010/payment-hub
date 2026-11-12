package com.m2i.omnicanal.application.service.impl;


import com.m2i.omnicanal.model.SchemaEcriture;
import com.m2i.omnicanal.model.TypeService;
import com.m2i.omnicanal.application.domain.SchemaEcrit;
import com.m2i.omnicanal.application.repository.SchemaEcritureRepository;
import com.m2i.omnicanal.application.service.SchemaEcritureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing SchemaEcriture.
 */
@Service
@Transactional
public class SchemaEcritureServiceImpl implements SchemaEcritureService{

    private final Logger log = LoggerFactory.getLogger(SchemaEcritureServiceImpl.class);
    
    private final SchemaEcritureRepository schemaEcritureRepository;

    public SchemaEcritureServiceImpl(SchemaEcritureRepository schemaEcritureRepository) {
        this.schemaEcritureRepository = schemaEcritureRepository;
    }

    /**
     * Save a schemaEcriture.
     *
     * @param schemaEcriture the entity to save
     * @return the persisted entity
     */
    @Override
    public SchemaEcrit save(SchemaEcrit schemaEcriture) {
        log.debug("Request to save SchemaEcriture : {}", schemaEcriture);
        SchemaEcrit result = schemaEcritureRepository.save(schemaEcriture);
        return result;
    }

    /**
     *  Get all the schemaEcritures.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<SchemaEcrit> findAll() {
        log.debug("Request to get all SchemaEcritures");
        List<SchemaEcrit> result = schemaEcritureRepository.findAll();

        return result;
    }

    /**
     *  Get one schemaEcriture by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public SchemaEcrit findOne(Long id) {
        log.debug("Request to get SchemaEcriture : {}", id);
        SchemaEcrit schemaEcriture = schemaEcritureRepository.findOne(id);
        return schemaEcriture;
    }

    /**
     *  Delete the  schemaEcriture by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete SchemaEcriture : {}", id);
        schemaEcritureRepository.deleteById(id);
    }

    @Override
    public List<SchemaEcriture> findByService(TypeService typeService) {
        List<SchemaEcriture> result= schemaEcritureRepository.findByService(typeService);
        return result;
    }

   
}
