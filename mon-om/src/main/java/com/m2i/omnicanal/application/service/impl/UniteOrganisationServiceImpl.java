package com.m2i.omnicanal.application.service.impl;

import com.m2i.omnicanal.application.service.UniteOrganisationService;
import com.m2i.omnicanal.application.domain.UniteOrg;
import com.m2i.omnicanal.application.repository.UniteOrganisationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;


/**
 * Service Implementation for managing UniteOrganisation.
 */
@Service
@Transactional
public class UniteOrganisationServiceImpl implements UniteOrganisationService{

    private final Logger log = LoggerFactory.getLogger(UniteOrganisationServiceImpl.class);
    
    private final UniteOrganisationRepository uniteOrganisationRepository;

    public UniteOrganisationServiceImpl(UniteOrganisationRepository uniteOrganisationRepository) {
        this.uniteOrganisationRepository = uniteOrganisationRepository;
    }

    /**
     * Save a uniteOrganisation.
     *
     * @param uniteOrganisation the entity to save
     * @return the persisted entity
     */
    @Override
    public UniteOrg save(UniteOrg uniteOrganisation) {
        log.debug("Request to save UniteOrganisation : {}", uniteOrganisation);
        UniteOrg result = uniteOrganisationRepository.save(uniteOrganisation);
        return result;
    }

    /**
     *  Get all the uniteOrganisations.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<UniteOrg> findAll(Pageable pageable) {
        log.debug("Request to get all UniteOrganisations");
        Page<UniteOrg> result = uniteOrganisationRepository.findAll(pageable);
        return result;
    }

    /**
     *  Get one uniteOrganisation by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public UniteOrg findOne(Long id) {
        log.debug("Request to get UniteOrganisation : {}", id);
        UniteOrg uniteOrganisation = uniteOrganisationRepository.findOne(id);
        return uniteOrganisation;
    }

    /**
     *  Delete the  uniteOrganisation by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete UniteOrganisation : {}", id);
        uniteOrganisationRepository.deleteById(id);
    }
}
