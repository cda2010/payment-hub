package com.m2i.omnicanal.presentation.service.impl;


import com.m2i.omnicanal.model.TypeService;
import com.m2i.omnicanal.presentation.domain.CompteFse;
import com.m2i.omnicanal.presentation.domain.TypeServ;
import com.m2i.omnicanal.presentation.repository.CompteRepository;
import com.m2i.omnicanal.presentation.repository.TypeServiceRepository;
import com.m2i.omnicanal.presentation.service.CompteService;
import com.m2i.omnicanal.presentation.service.TypeServiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing Compte.
 */
@Service
@Transactional
public class TypeServiceServiceImpl implements TypeServiceService{

    private final Logger log = LoggerFactory.getLogger(TypeServiceServiceImpl.class);
    
    private final TypeServiceRepository typeServiceRepository;

    public TypeServiceServiceImpl(TypeServiceRepository typeServiceRepository) {
        this.typeServiceRepository = typeServiceRepository;
    }

    /**
     * Save a compte.
     *
     * @param typeServ
     * @param compte the entity to save
     * @return the persisted entity
     */
    @Override
    public TypeServ save(TypeServ typeServ) {
        log.debug("Request to save Compte : {}", typeServ);
        TypeServ result = typeServiceRepository.save(typeServ);
        return result;
    }

    /**
     *  Get all the comptes.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<TypeServ> findAll() {
        log.debug("Request to get all Comptes");
        List<TypeServ> result = typeServiceRepository.findAll();

        return result;
    }

    /**
     *  Get one compte by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public TypeServ findOne(Long id) {
        log.debug("Request to get Compte : {}", id);
        TypeServ typeServ = typeServiceRepository.findOne(id);
        return typeServ;
    }

    /**
     *  Delete the  compte by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Compte : {}", id);
        typeServiceRepository.deleteById(id);
    }

    @Override
    public TypeService findByCodeTypeService(String codeService) {
    
        TypeServ typeServ = typeServiceRepository.findByCodeService(codeService);
        return typeServ;
    }
}
