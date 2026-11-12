package com.m2i.omnicanal.application.service;

import com.m2i.omnicanal.application.domain.TypeUniteOrg;
import java.util.List;

/**
 * Service Interface for managing TypeUniteOrganisation.
 */
public interface TypeUniteOrganisationService {

    /**
     * Save a typeUniteOrganisation.
     *
     * @param typeUniteOrganisation the entity to save
     * @return the persisted entity
     */
    TypeUniteOrg save(TypeUniteOrg typeUniteOrganisation);

    /**
     *  Get all the typeUniteOrganisations.
     *  
     *  @return the list of entities
     */
    List<TypeUniteOrg> findAll();

    /**
     *  Get the "id" typeUniteOrganisation.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    TypeUniteOrg findOne(Long id);

    /**
     *  Delete the "id" typeUniteOrganisation.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);
}
