package com.m2i.omnicanal.presentation.service;


import com.m2i.omnicanal.model.LigneSchemaEcriture;
import com.m2i.omnicanal.presentation.domain.LigneSchemaEcrit;
import java.util.List;
import java.util.Set;

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
    public Set<LigneSchemaEcriture> findLigneBySchema(Long idSchema);
}
