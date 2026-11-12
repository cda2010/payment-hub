package com.m2i.omnicanal.application.service.impl;

import com.m2i.omnicanal.application.service.TypeUniteOrganisationService;
import com.m2i.omnicanal.application.domain.TypeUniteOrg;
import com.m2i.omnicanal.application.repository.TypeUniteOrganisationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing TypeUniteOrganisation.
 */
@Service
@Transactional
public class TypeUniteOrganisationServiceImpl implements TypeUniteOrganisationService{

    private final Logger log = LoggerFactory.getLogger(TypeUniteOrganisationServiceImpl.class);
    
    private final TypeUniteOrganisationRepository typeUniteOrganisationRepository;

    public TypeUniteOrganisationServiceImpl(TypeUniteOrganisationRepository typeUniteOrganisationRepository) {
        this.typeUniteOrganisationRepository = typeUniteOrganisationRepository;
    }

    /**
     * Save a typeUniteOrganisation.
     *
     * @param typeUniteOrganisation the entity to save
     * @return the persisted entity
     */
    @Override
    public TypeUniteOrg save(TypeUniteOrg typeUniteOrganisation) {
        log.debug("Request to save TypeUniteOrganisation : {}", typeUniteOrganisation);
        TypeUniteOrg result = typeUniteOrganisationRepository.save(typeUniteOrganisation);
        return result;
    }

    /**
     *  Get all the typeUniteOrganisations.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<TypeUniteOrg> findAll() {
        log.debug("Request to get all TypeUniteOrganisations");
        List<TypeUniteOrg> result = typeUniteOrganisationRepository.findAll();

        return result;
    }

    /**
     *  Get one typeUniteOrganisation by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public TypeUniteOrg findOne(Long id) {
        log.debug("Request to get TypeUniteOrganisation : {}", id);
        TypeUniteOrg typeUniteOrganisation = typeUniteOrganisationRepository.findOne(id);
        return typeUniteOrganisation;
    }

    /**
     *  Delete the  typeUniteOrganisation by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete TypeUniteOrganisation : {}", id);
        typeUniteOrganisationRepository.deleteById(id);
    }
}
