package com.m2i.omnicanal.application.service.impl;


import com.m2i.omnicanal.application.domain.TypeCpt;
import com.m2i.omnicanal.application.repository.TypeCompteRepository;
import com.m2i.omnicanal.application.service.TypeCompteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing TypeCompte.
 */
@Service
@Transactional
public class TypeCompteServiceImpl implements TypeCompteService{

    private final Logger log = LoggerFactory.getLogger(TypeCompteServiceImpl.class);
    
    private final TypeCompteRepository typeCompteRepository;

    public TypeCompteServiceImpl(TypeCompteRepository typeCompteRepository) {
        this.typeCompteRepository = typeCompteRepository;
    }

    /**
     * Save a typeCompte.
     *
     * @param typeCompte the entity to save
     * @return the persisted entity
     */
    @Override
    public TypeCpt save(TypeCpt typeCompte) {
        log.debug("Request to save TypeCompte : {}", typeCompte);
        TypeCpt result = typeCompteRepository.save(typeCompte);
        return result;
    }

    /**
     *  Get all the typeComptes.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<TypeCpt> findAll() {
        log.debug("Request to get all TypeComptes");
        List<TypeCpt> result = typeCompteRepository.findAll();

        return result;
    }

    /**
     *  Get one typeCompte by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public TypeCpt findOne(Long id) {
        log.debug("Request to get TypeCompte : {}", id);
        TypeCpt typeCompte = typeCompteRepository.findOne(id);
        return typeCompte;
    }

    /**
     *  Delete the  typeCompte by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete TypeCompte : {}", id);
        typeCompteRepository.deleteById(id);
    }
}
