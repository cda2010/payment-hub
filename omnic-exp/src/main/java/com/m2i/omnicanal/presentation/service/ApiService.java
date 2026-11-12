package com.m2i.omnicanal.presentation.service;


import com.m2i.omnicanal.presentation.domain.ApplicationApi;
import java.util.List;

/**
 * Service Interface for managing Api.
 */
public interface ApiService {

    /**
     * Save a api.
     *
     * @param api the entity to save
     * @return the persisted entity
     */
    ApplicationApi save(ApplicationApi api);

    /**
     *  Get all the apis.
     *  
     *  @return the list of entities
     */
    List<ApplicationApi> findAll();

    /**
     *  Get the "id" api.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    ApplicationApi findOne(Long id);

    /**
     *  Delete the "id" api.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);
}
