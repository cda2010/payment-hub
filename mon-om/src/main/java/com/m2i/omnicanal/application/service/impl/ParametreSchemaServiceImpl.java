package com.m2i.omnicanal.application.service.impl;


import com.m2i.omnicanal.application.domain.ParametreSchem;
import com.m2i.omnicanal.application.repository.ParametreSchemaRepository;
import com.m2i.omnicanal.application.service.ParametreSchemaService;
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
public class ParametreSchemaServiceImpl implements ParametreSchemaService{

    private final Logger log = LoggerFactory.getLogger(ParametreSchemaServiceImpl.class);
    
    private final ParametreSchemaRepository parametreSchemaRepository;

    public ParametreSchemaServiceImpl(ParametreSchemaRepository parametreSchemaRepository) {
        this.parametreSchemaRepository = parametreSchemaRepository;
    }

    /**
     * Save a parametreMethode.
     *
     * @param parametreMethode the entity to save
     * @return the persisted entity
     */
    @Override
    public ParametreSchem save(ParametreSchem parametreSchema) {
        log.debug("Request to save ParametreMethode : {}", parametreSchema);
        ParametreSchem result = parametreSchemaRepository.save(parametreSchema);
        return result;
    }

    /**
     *  Get all the ParametreSchem.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<ParametreSchem> findAll() {
        log.debug("Request to get all ParametreSchem");
        List<ParametreSchem> result = parametreSchemaRepository.findAll();

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
    public ParametreSchem findOne(Long id) {
        log.debug("Request to get ParametreMethode : {}", id);
        ParametreSchem parametreMethode = parametreSchemaRepository.findOne(id);
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
        parametreSchemaRepository.deleteById(id);
    }

    @Override
    public List<ParametreSchem> findByLigne(Long idligne) {
         log.debug("Request to get all ParametreSchem by ligne");
        List<ParametreSchem> result = parametreSchemaRepository.findByLigne(idligne);

        return result;
    }
}
