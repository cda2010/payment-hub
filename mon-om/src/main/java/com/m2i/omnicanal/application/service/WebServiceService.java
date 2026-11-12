package com.m2i.omnicanal.application.service;


import com.m2i.omnicanal.application.domain.ApplicationApi;
import com.m2i.omnicanal.application.domain.WebServiceUser;
import java.util.List;

/**
 * Service Interface for managing Api.
 */
public interface WebServiceService {

    /**
     * Save a api.
     *
     * @param api the entity to save
     * @return the persisted entity
     */
    WebServiceUser save(WebServiceUser api);

    /**
     *  Get all the apis.
     *  
     *  @return the list of entities
     */
    List<WebServiceUser> findAll();

    /**
     *  Get the "id" api.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    WebServiceUser findOne(Long id);

    /**
     *  Delete the "id" api.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);
}
