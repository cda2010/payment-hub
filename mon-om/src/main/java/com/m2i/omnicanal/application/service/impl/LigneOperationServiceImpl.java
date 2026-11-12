package com.m2i.omnicanal.application.service.impl;

import com.m2i.omnicanal.application.domain.LigneOper;
import com.m2i.omnicanal.application.repository.LigneOperationRepository;
import com.m2i.omnicanal.application.service.LigneOperationService;
import com.m2i.omnicanal.model.LigneOperation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing LigneOperation.
 */
@Service
@Transactional
public class LigneOperationServiceImpl implements LigneOperationService{

    private final Logger log = LoggerFactory.getLogger(LigneOperationServiceImpl.class);
    
    private final LigneOperationRepository ligneOperationRepository;

    public LigneOperationServiceImpl(LigneOperationRepository ligneOperationRepository) {
        this.ligneOperationRepository = ligneOperationRepository;
    }

    /**
     * Save a ligneOperation.
     *
     * @param ligneOperation the entity to save
     * @return the persisted entity
     */


    /**
     *  Get all the ligneOperations.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<LigneOper> findAll() {
        log.debug("Request to get all LigneOperations");
        List<LigneOper> result = ligneOperationRepository.findAll();

        return result;
    }

    /**
     *  Get one ligneOperation by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public LigneOper findOne(Long id) {
        log.debug("Request to get LigneOperation : {}", id);
        LigneOper ligneOperation = ligneOperationRepository.findOne(id);
        return ligneOperation;
    }

    /**
     *  Delete the  ligneOperation by id.
     *
     *  @param id the id of the entity
     */


    @Override
    public List<LigneOper> findByOperationId(Long id) {
        log.debug("Request to get LigneOperation : {}", id);
        List<LigneOper> ligneOperation = ligneOperationRepository.findByOperationId(id);
        
        return ligneOperation;
    }
}
