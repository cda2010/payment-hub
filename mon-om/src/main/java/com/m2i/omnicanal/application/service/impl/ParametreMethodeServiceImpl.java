package com.m2i.omnicanal.application.service.impl;


import com.m2i.omnicanal.application.domain.ParametreMethod;
import com.m2i.omnicanal.application.repository.ParametreMethodeRepository;
import com.m2i.omnicanal.application.service.ParametreMethodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing ParametreMethode.
 */
@Service
@Transactional
public class ParametreMethodeServiceImpl implements ParametreMethodeService{

    private final Logger log = LoggerFactory.getLogger(ParametreMethodeServiceImpl.class);
    
    private final ParametreMethodeRepository parametreMethodeRepository;

    public ParametreMethodeServiceImpl(ParametreMethodeRepository parametreMethodeRepository) {
        this.parametreMethodeRepository = parametreMethodeRepository;
    }

    /**
     * Save a parametreMethode.
     *
     * @param parametreMethode the entity to save
     * @return the persisted entity
     */
    @Override
    public ParametreMethod save(ParametreMethod parametreMethode) {
        log.debug("Request to save ParametreMethode : {}", parametreMethode);
        ParametreMethod result = parametreMethodeRepository.save(parametreMethode);
        return result;
    }

    /**
     *  Get all the parametreMethodes.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<ParametreMethod> findAll() {
        log.debug("Request to get all ParametreMethodes");
        List<ParametreMethod> result = parametreMethodeRepository.findAll();

        return result;
    }

    /**
     *  Get one parametreMethode by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public ParametreMethod findOne(Long id) {
        log.debug("Request to get ParametreMethode : {}", id);
        ParametreMethod parametreMethode = parametreMethodeRepository.findOne(id);
        return parametreMethode;
    }

    /**
     *  Delete the  parametreMethode by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete ParametreMethode : {}", id);
        parametreMethodeRepository.deleteById(id);
    }

    @Override
    public List<ParametreMethod> findByMethode(Long idmethode) {
         log.debug("Request to get all ParametreMethodes by methode");
        List<ParametreMethod> result = parametreMethodeRepository.findByMethode(idmethode);

        return result;
    }
}
