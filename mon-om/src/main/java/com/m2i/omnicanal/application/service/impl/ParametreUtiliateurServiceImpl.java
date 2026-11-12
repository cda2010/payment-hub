package com.m2i.omnicanal.application.service.impl;


import com.m2i.omnicanal.application.domain.ParametreUser;
import com.m2i.omnicanal.application.repository.ParametreUtilisateurRepository;
import com.m2i.omnicanal.application.service.ParametreUtiliateurService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing ParametreUtiliateur.
 */
@Service
@Transactional
public class ParametreUtiliateurServiceImpl implements ParametreUtiliateurService{

    private final Logger log = LoggerFactory.getLogger(ParametreUtiliateurServiceImpl.class);
    
    private final ParametreUtilisateurRepository parametreUtiliateurRepository;

    public ParametreUtiliateurServiceImpl(ParametreUtilisateurRepository parametreUtiliateurRepository) {
        this.parametreUtiliateurRepository = parametreUtiliateurRepository;
    }

    /**
     * Save a parametreUtiliateur.
     *
     * @param parametreUtiliateur the entity to save
     * @return the persisted entity
     */
    @Override
    public ParametreUser save(ParametreUser parametreUtiliateur) {
        log.debug("Request to save ParametreUtiliateur : {}", parametreUtiliateur);
        ParametreUser result = parametreUtiliateurRepository.save(parametreUtiliateur);
        return result;
    }

    /**
     *  Get all the parametreUtiliateurs.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<ParametreUser> findAll() {
        log.debug("Request to get all ParametreUtiliateurs");
        List<ParametreUser> result = parametreUtiliateurRepository.findAll();

        return result;
    }

    /**
     *  Get one parametreUtiliateur by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public ParametreUser findOne(Long id) {
        log.debug("Request to get ParametreUtiliateur : {}", id);
        ParametreUser parametreUtiliateur = parametreUtiliateurRepository.findOne(id);
        return parametreUtiliateur;
    }

    /**
     *  Delete the  parametreUtiliateur by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete ParametreUtiliateur : {}", id);
        parametreUtiliateurRepository.deleteById(id);
    }

    @Override
    public List<ParametreUser> findByService(Long idService) {
     List<ParametreUser>   result = parametreUtiliateurRepository.findByService(idService);
     return result;
    }

    @Override
    public List<ParametreUser> findByInterface(Long idInterface) {
     List<ParametreUser>   result = parametreUtiliateurRepository.findByInterface(idInterface);
     return result;
    }
}
