package com.m2i.omnicanal.presentation.service;


import com.m2i.omnicanal.presentation.domain.Parameters;
import java.util.List;

/**
 * Service Interface for managing Parametre.
 */
public interface ParametreService {

    /**
     * Save a parametre.
     *
     * @param parametre the entity to save
     * @return the persisted entity
     */
    Parameters save(Parameters parametre);

    /**
     *  Get all the parametres.
     *  
     *  @return the list of entities
     */
    List<Parameters> findAll();

    /**
     *  Get the "id" parametre.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    Parameters findOne(Long id);

    /**
     *  Delete the "id" parametre.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    public List<Parameters> findByService(Long idservice);
}
