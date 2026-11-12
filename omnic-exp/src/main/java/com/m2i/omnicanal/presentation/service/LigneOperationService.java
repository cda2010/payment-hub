package com.m2i.omnicanal.presentation.service;


import com.m2i.omnicanal.model.LigneOperation;
import com.m2i.omnicanal.presentation.domain.LigneOper;
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
    LigneOper save(LigneOper ligneOperation);

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
    void delete(Long id);
}
