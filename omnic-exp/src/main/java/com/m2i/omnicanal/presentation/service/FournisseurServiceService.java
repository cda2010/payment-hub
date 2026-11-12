package com.m2i.omnicanal.presentation.service;

import com.m2i.omnicanal.model.FournisseurService;
import com.m2i.omnicanal.presentation.domain.AgenceFse;
import com.m2i.omnicanal.presentation.domain.FournisseurFSE;
import java.util.List;

/**
 * Service Interface for managing FournisseurService.
 */
public interface FournisseurServiceService {

    /**
     * Save a fournisseurService.
     *
     * @param fournisseurService the entity to save
     * @return the persisted entity
     */
    FournisseurFSE save(FournisseurFSE fournisseurService);

    /**
     *  Get all the fournisseurServices.
     *  
     *  @return the list of entities
     */
    List<FournisseurFSE> findAll();

    /**
     *  Get the "id" fournisseurService.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    FournisseurFSE findOne(Long id);

    /**
     *  Delete the "id" fournisseurService.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    public FournisseurService findByCodeFse(String fseSource);

    public FournisseurFSE findByFseCompense(FournisseurService FseDest);
    public FournisseurFSE findUoByCodeFse(String fseSource) ;
  
}
