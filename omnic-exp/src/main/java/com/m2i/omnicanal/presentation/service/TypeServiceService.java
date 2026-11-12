package com.m2i.omnicanal.presentation.service;


import com.m2i.omnicanal.model.TypeService;
import com.m2i.omnicanal.presentation.domain.TypeServ;
import java.util.List;

/**
 * Service Interface for managing Compte.
 */
public interface TypeServiceService {

    /**
     * Save a compte.
     *
     * @param compte the entity to save
     * @return the persisted entity
     */
    TypeServ save(TypeServ typeServ);

    /**
     *  Get all the comptes.
     *  
     *  @return the list of entities
     */
    List<TypeServ> findAll();

    /**
     *  Get the "id" compte.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    TypeServ findOne(Long id);

    /**
     *  Delete the "id" compte.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    public TypeService findByCodeTypeService(String codeService);
}
