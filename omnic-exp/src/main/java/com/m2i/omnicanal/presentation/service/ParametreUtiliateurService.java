package com.m2i.omnicanal.presentation.service;


import com.m2i.omnicanal.presentation.domain.ParametreUser;
import java.util.List;

/**
 * Service Interface for managing ParametreUtiliateur.
 */
public interface ParametreUtiliateurService {

    /**
     * Save a parametreUtiliateur.
     *
     * @param parametreUtiliateur the entity to save
     * @return the persisted entity
     */
    ParametreUser save(ParametreUser parametreUtiliateur);

    /**
     *  Get all the parametreUtiliateurs.
     *  
     *  @return the list of entities
     */
    List<ParametreUser> findAll();

    /**
     *  Get the "id" parametreUtiliateur.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    ParametreUser findOne(Long id);

    /**
     *  Delete the "id" parametreUtiliateur.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    public List<ParametreUser> findByService(Long idService);

    public List<ParametreUser> findByInterface(Long idInterface);

    public List<ParametreUser> findByServiceFse(String fse, Long typeService);
}
