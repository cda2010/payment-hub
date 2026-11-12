package com.m2i.omnicanal.presentation.service.impl;


import com.m2i.omnicanal.model.LigneSchemaEcriture;
import com.m2i.omnicanal.presentation.domain.LigneSchemaEcrit;
import com.m2i.omnicanal.presentation.repository.LigneSchemaEcritureRepository;
import com.m2i.omnicanal.presentation.service.LigneSchemaEcritureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Service Implementation for managing LigneSchemaEcriture.
 */
@Service
@Transactional
public class LigneSchemaEcritureServiceImpl implements LigneSchemaEcritureService{

    private final Logger log = LoggerFactory.getLogger(LigneSchemaEcritureServiceImpl.class);
    
    private final LigneSchemaEcritureRepository ligneSchemaEcritureRepository;

    public LigneSchemaEcritureServiceImpl(LigneSchemaEcritureRepository ligneSchemaEcritureRepository) {
        this.ligneSchemaEcritureRepository = ligneSchemaEcritureRepository;
    }

    /**
     * Save a ligneSchemaEcriture.
     *
     * @param ligneSchemaEcriture the entity to save
     * @return the persisted entity
     */
    @Override
    public LigneSchemaEcrit save(LigneSchemaEcrit ligneSchemaEcriture) {
        log.debug("Request to save LigneSchemaEcriture : {}", ligneSchemaEcriture);
        LigneSchemaEcrit result = ligneSchemaEcritureRepository.save(ligneSchemaEcriture);
        return result;
    }

    /**
     *  Get all the ligneSchemaEcritures.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<LigneSchemaEcrit> findAll() {
        log.debug("Request to get all LigneSchemaEcritures");
        List<LigneSchemaEcrit> result = ligneSchemaEcritureRepository.findAll();

        return result;
    }

    /**
     *  Get one ligneSchemaEcriture by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public LigneSchemaEcrit findOne(Long id) {
        log.debug("Request to get LigneSchemaEcriture : {}", id);
        LigneSchemaEcrit ligneSchemaEcriture = ligneSchemaEcritureRepository.findOne(id);
        return ligneSchemaEcriture;
    }

    /**
     *  Delete the  ligneSchemaEcriture by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete LigneSchemaEcriture : {}", id);
        ligneSchemaEcritureRepository.deleteById(id);
    }

    @Override
    public List<LigneSchemaEcrit> findBySchema(Long idSchema) {
         log.debug("Request to get all LigneSchemaEcritures");
        List<LigneSchemaEcrit> result = ligneSchemaEcritureRepository.findBySch( idSchema);

        return result;
    }
     @Override
        public Set<LigneSchemaEcriture> findLigneBySchema(Long idSchema){
             log.debug("Request to get all LigneSchemaEcritures");
        Set<LigneSchemaEcriture> result = ligneSchemaEcritureRepository.findLigneBySch( idSchema);

        return result;
        }
}
