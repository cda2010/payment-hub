package com.m2i.omnicanal.application.service;


import com.m2i.omnicanal.application.domain.LigneOper;

import java.util.List;

/**
 * Service Interface for managing LigneOperation.
 */
public interface LigneOperationService {

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
    List<LigneOper> findAll();

    /**
     *  Get the "id" ligneOperation.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    LigneOper findOne(Long id);

    /**
     *  Delete the "id" ligneOperation.
     *
     *  @param id the id of the entity
     */


    public List<LigneOper> findByOperationId(Long id);
}
