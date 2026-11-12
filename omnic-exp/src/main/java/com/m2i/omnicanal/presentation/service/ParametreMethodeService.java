package com.m2i.omnicanal.presentation.service;

import com.m2i.omnicanal.presentation.domain.ParametreMethod;

import java.util.List;

/**
 * Service Interface for managing ParametreMethode.
 */
public interface ParametreMethodeService {

    /**
     * Save a parametreMethode.
     *
     * @param parametreMethode the entity to save
     * @return the persisted entity
     */
    ParametreMethod save(ParametreMethod parametreMethode);

    /**
     *  Get all the parametreMethodes.
     *  
     *  @return the list of entities
     */
    List<ParametreMethod> findAll();

    /**
     *  Get the "id" parametreMethode.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    ParametreMethod findOne(Long id);

    /**
     *  Delete the "id" parametreMethode.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    public List<ParametreMethod> findByMethode(Long idmethode);
}
