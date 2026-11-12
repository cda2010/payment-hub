package com.m2i.omnicanal.application.service;


import com.m2i.omnicanal.application.domain.AgenceFse;
import com.m2i.omnicanal.application.repository.AgenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing Agence.
 */
@Service
@Transactional
public class AgenceService {

    private final Logger log = LoggerFactory.getLogger(AgenceService.class);
    
    private final AgenceRepository agenceRepository;

    public AgenceService(AgenceRepository agenceRepository) {
        this.agenceRepository = agenceRepository;
    }

    /**
     * Save a agence.
     *
     * @param agence the entity to save
     * @return the persisted entity
     */
    public AgenceFse save(AgenceFse agence) {
        log.debug("Request to save Agence : {}", agence);
        AgenceFse result = agenceRepository.save(agence);
        return result;
    }

    /**
     *  Get all the agences.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<AgenceFse> findAll(Pageable pageable) {
        log.debug("Request to get all Agences");
        Page<AgenceFse> result = agenceRepository.findAll(pageable);
        return result;
    }

    /**
     *  Get one agence by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public AgenceFse findOne(Long id) {
        log.debug("Request to get Agence : {}", id);
        AgenceFse agence = agenceRepository.findOne(id);
        return agence;
    }

    /**
     *  Delete the  agence by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Agence : {}", id);
        agenceRepository.deleteById(id);
    }

    public AgenceFse findByCodeAgenceAndFournisseurServiceId(String codeAgence, Long id) {
        AgenceFse agence = agenceRepository.findOne(id);
        return agence;
    }

    public List<AgenceFse> findByFournisseurServiceId(Long fseId) {
        List<AgenceFse> result = agenceRepository.findByFournisseurServiceId(fseId);
        return result;
    }

    public Page<AgenceFse> findAll(Pageable pageable, Long fseId) {
         log.debug("Request to get all Agences");
        Page<AgenceFse> result = agenceRepository.findAll(pageable,fseId);
        return result;
    }
}
