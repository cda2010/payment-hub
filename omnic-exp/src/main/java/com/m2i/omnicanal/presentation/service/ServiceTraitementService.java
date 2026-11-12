package com.m2i.omnicanal.presentation.service;


import com.m2i.omnicanal.presentation.domain.Service;
import java.util.List;

/**
 * Service Interface for managing ServiceTraitement.
 */
public interface ServiceTraitementService {

    /**
     * Save a serviceTraitement.
     *
     * @param serviceTraitement the entity to save
     * @return the persisted entity
     */
    Service save(Service serviceTraitement);

    /**
     *  Get all the serviceTraitements.
     *  
     *  @return the list of entities
     */
    List<Service> findAll();

    /**
     *  Get the "id" serviceTraitement.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    Service findOne(Long id);

    /**
     *  Delete the "id" serviceTraitement.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);
}
