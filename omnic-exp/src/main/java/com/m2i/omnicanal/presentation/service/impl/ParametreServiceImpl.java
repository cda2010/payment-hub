package com.m2i.omnicanal.presentation.service.impl;


import com.m2i.omnicanal.presentation.domain.Parameters;
import com.m2i.omnicanal.presentation.repository.ParametreRepository;
import com.m2i.omnicanal.presentation.service.ParametreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing Parametre.
 */
@Service
@Transactional
public class ParametreServiceImpl implements ParametreService{

    private final Logger log = LoggerFactory.getLogger(ParametreServiceImpl.class);
    
    private final ParametreRepository parametreRepository;

    public ParametreServiceImpl(ParametreRepository parametreRepository) {
        this.parametreRepository = parametreRepository;
    }

    /**
     * Save a parametre.
     *
     * @param parametre the entity to save
     * @return the persisted entity
     */
    @Override
    public Parameters save(Parameters parametre) {
        log.debug("Request to save Parametre : {}", parametre);
        Parameters result = parametreRepository.save(parametre);
        return result;
    }

    /**
     *  Get all the parametres.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<Parameters> findAll() {
        log.debug("Request to get all Parametres");
        List<Parameters> result = parametreRepository.findAll();

        return result;
    }

    /**
     *  Get one parametre by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public Parameters findOne(Long id) {
        log.debug("Request to get Parametre : {}", id);
        Parameters parametre = parametreRepository.findOne(id);
        return parametre;
    }

    /**
     *  Delete the  parametre by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Parametre : {}", id);
        parametreRepository.deleteById(id);
    }

    @Override
    public List<Parameters> findByService(Long idservice) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
