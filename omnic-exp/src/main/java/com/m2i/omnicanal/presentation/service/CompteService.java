package com.m2i.omnicanal.presentation.service;


import com.m2i.omnicanal.model.Agence;
import com.m2i.omnicanal.presentation.domain.CompteFse;
import java.util.List;

/**
 * Service Interface for managing Compte.
 */
public interface CompteService {

    /**
     * Save a compte.
     *
     * @param compte the entity to save
     * @return the persisted entity
     */
    CompteFse save(CompteFse compte);

    /**
     *  Get all the comptes.
     *  
     *  @return the list of entities
     */
    List<CompteFse> findAll();

    /**
     *  Get the "id" compte.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    CompteFse findOne(Long id);

    /**
     *  Delete the "id" compte.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    public CompteFse findCompteByEtiquetteAndFse(String codeFse, String etiquette);
 public   CompteFse findCompteFse(String codeParametre,Agence agence, Long typeServiceId, Long relationId, String codeFseTeneur);
}
