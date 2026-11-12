package com.m2i.omnicanal.presentation.service.impl;

import com.m2i.omnicanal.model.LigneOperation;
import com.m2i.omnicanal.presentation.domain.LigneOper;
import com.m2i.omnicanal.presentation.service.LigneOperationService;

import com.m2i.omnicanal.presentation.repository.LigneOperationRepository;
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
    @Override
    public LigneOper save(LigneOper ligneOperation) {
        log.debug("Request to save LigneOperation : {}", ligneOperation);
        LigneOper result = ligneOperationRepository.save(ligneOperation);
        return result;
    }

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
    public void delete(Long id) {
        log.debug("Request to delete LigneOperation : {}", id);
        ligneOperationRepository.deleteById(id);
    }
}
