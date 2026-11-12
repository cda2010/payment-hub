package com.m2i.omnicanal.application.service;

import com.m2i.omnicanal.application.domain.ParametreSchem;

import java.util.List;

/**
 * Service Interface for managing ParametreMethode.
 */
public interface ParametreSchemaService {

    /**
     * Save a parametreMethode.
     *
     * @param parametreMethode the entity to save
     * @return the persisted entity
     */
    ParametreSchem save(ParametreSchem parametreShema);

    /**
     *  Get all the parametreMethodes.
     *  
     *  @return the list of entities
     */
    List<ParametreSchem> findAll();

    /**
     *  Get the "id" parametreMethode.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    ParametreSchem findOne(Long id);

    /**
     *  Delete the "id" parametreMethode.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    public List<ParametreSchem> findByLigne(Long ligne);
}
