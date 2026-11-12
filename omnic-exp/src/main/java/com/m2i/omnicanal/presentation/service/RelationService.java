package com.m2i.omnicanal.presentation.service;


import com.m2i.omnicanal.presentation.domain.RelationFse;
import java.util.List;

/**
 * Service Interface for managing Relation.
 */
public interface RelationService {

    /**
     * Save a relation.
     *
     * @param relation the entity to save
     * @return the persisted entity
     */
    RelationFse save(RelationFse relation);

    /**
     *  Get all the relations.
     *  
     *  @return the list of entities
     */
    List<RelationFse> findAll();

    /**
     *  Get the "id" relation.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    RelationFse findOne(Long id);

    /**
     *  Delete the "id" relation.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);
}
