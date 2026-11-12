package com.m2i.omnicanal.application.service;


import com.m2i.omnicanal.application.domain.ServiceMethod;

import java.util.List;

/**
 * Service Interface for managing ServiceMethode.
 */
public interface ServiceMethodeService {

    /**
     * Save a serviceMethode.
     *
     * @param serviceMethode the entity to save
     * @return the persisted entity
     */
    ServiceMethod save(ServiceMethod serviceMethode);

    /**
     *  Get all the serviceMethodes.
     *  
     *  @return the list of entities
     */
    List<ServiceMethod> findAll();

    /**
     *  Get the "id" serviceMethode.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    ServiceMethod findOne(Long id);

    /**
     *  Delete the "id" serviceMethode.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    public List<ServiceMethod> findByService(Long idService);
}
