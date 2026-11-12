package com.m2i.omnicanal.application.service.impl;

import com.m2i.omnicanal.application.domain.AutreParametreFSE;
import com.m2i.omnicanal.application.service.AutreParametreService;

import com.m2i.omnicanal.application.repository.AutreParametreRepository;
import com.m2i.omnicanal.model.AutreParametre;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing AutreParametre.
 */
@Service
@Transactional
public class AutreParametreServiceImpl implements AutreParametreService{

    private final Logger log = LoggerFactory.getLogger(AutreParametreServiceImpl.class);
    
    private final AutreParametreRepository autreParametreRepository;

    public AutreParametreServiceImpl(AutreParametreRepository autreParametreRepository) {
        this.autreParametreRepository = autreParametreRepository;
    }

    /**
     * Save a autreParametre.
     *
     * @param autreParametre the entity to save
     * @return the persisted entity
     */
    @Override
    public AutreParametreFSE save(AutreParametreFSE autreParametre) {
        log.debug("Request to save AutreParametre : {}", autreParametre);
        AutreParametreFSE result = autreParametreRepository.save(autreParametre);
        return result;
    }

    /**
     *  Get all the autreParametres.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<AutreParametre> findAll() {
        log.debug("Request to get all AutreParametres");
        List<AutreParametre> result = autreParametreRepository.findAll();

        return result;
    }

    /**
     *  Get one autreParametre by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public AutreParametre findOne(Long id) {
        log.debug("Request to get AutreParametre : {}", id);
        AutreParametre autreParametre = autreParametreRepository.findOne(id);
        return autreParametre;
    }

    /**
     *  Delete the  autreParametre by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete AutreParametre : {}", id);
        autreParametreRepository.deleteById(id);
    }

  
}
