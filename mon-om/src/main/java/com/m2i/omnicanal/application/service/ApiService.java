package com.m2i.omnicanal.application.service;


import java.util.List;

import com.m2i.omnicanal.application.domain.ApplicationApi;
import com.m2i.omnicanal.model.Api;

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
    List<Api> findAll();

    /**
     *  Get the "id" api.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    Api findOne(Long id);

    /**
     *  Delete the "id" api.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    public Integer findMaxCodeApi();

    ApplicationApi duplicateWithMethodsAndParams(Api originalApi, ApplicationApi newApi);
}
