package com.m2i.omnicanal.presentation.service;

import com.m2i.omnicanal.model.InterfaceUtilisateur;
import com.m2i.omnicanal.model.enumeration.TypeEnvoi;
import com.m2i.omnicanal.model.enumeration.TypeMethode;
import com.m2i.omnicanal.presentation.domain.InterfaceUser;

import java.util.List;

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

    public InterfaceUser findByService(String codeService);



    public List<InterfaceUser> findByServiceFse(String fse, Long typeService);
public InterfaceUser getInterfaceUser(String codeFse, TypeMethode typeMethode, TypeEnvoi typeEnvoi);
 
}
