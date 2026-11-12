package com.m2i.omnicanal.presentation.service;



import com.m2i.omnicanal.model.AutreParametre;
import com.m2i.omnicanal.presentation.domain.AutreParametreFSE;

import java.util.List;

/**
 * Service Interface for managing AutreParametre.
 */
public interface AutreParametreService {

    /**
     * Save a autreParametre.
     *
     * @param autreParametre the entity to save
     * @return the persisted entity
     */
    AutreParametreFSE save(AutreParametreFSE autreParametre);

    /**
     *  Get all the autreParametres.
     *  
     *  @return the list of entities
     */
    List<AutreParametre> findAll();

    /**
     *  Get the "id" autreParametre.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    AutreParametre findOne(Long id);

    /**
     *  Delete the "id" autreParametre.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    public List<AutreParametre> findByFournisseurService(String fseCode);
}
