package com.m2i.omnicanal.application.service;


import com.m2i.omnicanal.application.domain.LigneSchemaEcrit;
import java.util.List;

/**
 * Service Interface for managing LigneSchemaEcriture.
 */
public interface LigneSchemaEcritureService {

    /**
     * Save a ligneSchemaEcriture.
     *
     * @param ligneSchemaEcriture the entity to save
     * @return the persisted entity
     */
    LigneSchemaEcrit save(LigneSchemaEcrit ligneSchemaEcriture);

    /**
     *  Get all the ligneSchemaEcritures.
     *  
     *  @return the list of entities
     */
    List<LigneSchemaEcrit> findAll();

    /**
     *  Get the "id" ligneSchemaEcriture.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    LigneSchemaEcrit findOne(Long id);

    /**
     *  Delete the "id" ligneSchemaEcriture.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    public List<LigneSchemaEcrit> findBySchema(Long idSchema);
}
