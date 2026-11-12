package com.m2i.omnicanal.presentation.service;


import com.m2i.omnicanal.presentation.domain.TypeCpt;
import java.util.List;

/**
 * Service Interface for managing TypeCompte.
 */
public interface TypeCompteService {

    /**
     * Save a typeCompte.
     *
     * @param typeCompte the entity to save
     * @return the persisted entity
     */
    TypeCpt save(TypeCpt typeCompte);

    /**
     *  Get all the typeComptes.
     *  
     *  @return the list of entities
     */
    List<TypeCpt> findAll();

    /**
     *  Get the "id" typeCompte.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    TypeCpt findOne(Long id);

    /**
     *  Delete the "id" typeCompte.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);
}
