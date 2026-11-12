package com.m2i.omnicanal.presentation.service;


import com.m2i.omnicanal.model.SchemaEcriture;
import com.m2i.omnicanal.model.TypeService;

import com.m2i.omnicanal.presentation.domain.SchemaEcrit;
import java.util.List;

/**
 * Service Interface for managing SchemaEcriture.
 */
public interface SchemaEcritureService {

    /**
     * Save a schemaEcriture.
     *
     * @param schemaEcriture the entity to save
     * @return the persisted entity
     */
    SchemaEcrit save(SchemaEcrit schemaEcriture);

    /**
     *  Get all the schemaEcritures.
     *  
     *  @return the list of entities
     */
    List<SchemaEcrit> findAll();

    /**
     *  Get the "id" schemaEcriture.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    SchemaEcrit findOne(Long id);

    /**
     *  Delete the "id" schemaEcriture.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    public List<SchemaEcriture> findByService(TypeService typeService);
    public List<SchemaEcriture> findByService(Long  typeServiceId);
}
