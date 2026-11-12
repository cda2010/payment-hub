package com.m2i.omnicanal.application.service;

import com.m2i.omnicanal.application.domain.UniteOrg;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing UniteOrganisation.
 */
public interface UniteOrganisationService {

    /**
     * Save a uniteOrganisation.
     *
     * @param uniteOrganisation the entity to save
     * @return the persisted entity
     */
    UniteOrg save(UniteOrg uniteOrganisation);

    /**
     *  Get all the uniteOrganisations.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    Page<UniteOrg> findAll(Pageable pageable);

    /**
     *  Get the "id" uniteOrganisation.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    UniteOrg findOne(Long id);

    /**
     *  Delete the "id" uniteOrganisation.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);
}
