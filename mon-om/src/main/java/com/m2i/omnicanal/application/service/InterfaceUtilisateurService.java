package com.m2i.omnicanal.application.service;

import java.util.List;

import com.m2i.omnicanal.application.domain.InterfaceUser;

/**
 * Service Interface for managing InterfaceUtilisateur.
 */
public interface InterfaceUtilisateurService {

    /**
     * Save a interfaceUtilisateur.
     *
     * @param interfaceUtilisateur the entity to save
     * @return the persisted entity
     */
    InterfaceUser save(InterfaceUser interfaceUtilisateur);

    /**
     *  Get all the interfaceUtilisateurs.
     *  
     *  @return the list of entities
     */
    List<InterfaceUser> findAll();

    /**
     *  Get the "id" interfaceUtilisateur.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    InterfaceUser findOne(Long id);

    /**
     *  Delete the "id" interfaceUtilisateur.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    /**
     * Duplicate an interfaceUtilisateur and its parameters.
     *
     * @param interfaceUtilisateur the entity to duplicate
     * @return the duplicated entity
     */
    InterfaceUser duplicate(InterfaceUser interfaceUtilisateur, InterfaceUser newInterfaceUtilisateur);
}
