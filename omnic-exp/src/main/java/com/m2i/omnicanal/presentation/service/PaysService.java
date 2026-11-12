package com.m2i.omnicanal.presentation.service;


import com.m2i.omnicanal.presentation.domain.PaysFse;
import java.util.List;

/**
 * Service Interface for managing Pays.
 */
public interface PaysService {

    /**
     * Save a pays.
     *
     * @param pays the entity to save
     * @return the persisted entity
     */
    PaysFse save(PaysFse pays);

    /**
     *  Get all the pays.
     *  
     *  @return the list of entities
     */
    List<PaysFse> findAll();

    /**
     *  Get the "id" pays.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    PaysFse findOne(Long id);

    /**
     *  Delete the "id" pays.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);
}
