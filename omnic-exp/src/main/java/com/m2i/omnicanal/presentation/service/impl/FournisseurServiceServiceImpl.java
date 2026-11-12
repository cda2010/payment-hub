package com.m2i.omnicanal.presentation.service.impl;


import com.m2i.omnicanal.model.FournisseurService;
import com.m2i.omnicanal.presentation.domain.AgenceFse;
import com.m2i.omnicanal.presentation.domain.FournisseurFSE;
import com.m2i.omnicanal.presentation.repository.FournisseurServiceRepository;
import com.m2i.omnicanal.presentation.service.FournisseurServiceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.cache.annotation.Cacheable;

/**
 * Service Implementation for managing FournisseurService.
 */
@Service
@Transactional
public class FournisseurServiceServiceImpl implements FournisseurServiceService{

    private final Logger log = LoggerFactory.getLogger(FournisseurServiceServiceImpl.class);
    
    private final FournisseurServiceRepository fournisseurServiceRepository;

    public FournisseurServiceServiceImpl(FournisseurServiceRepository fournisseurServiceRepository) {
        this.fournisseurServiceRepository = fournisseurServiceRepository;
    }

    /**
     * Save a fournisseurService.
     *
     * @param fournisseurService the entity to save
     * @return the persisted entity
     */
    @Override
    public FournisseurFSE save(FournisseurFSE fournisseurService) {
        log.debug("Request to save FournisseurService : {}", fournisseurService);
        FournisseurFSE result = fournisseurServiceRepository.save(fournisseurService);
        return result;
    }

    /**
     *  Get all the fournisseurServices.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<FournisseurFSE> findAll() {
        log.debug("Request to get all FournisseurServices");
        List<FournisseurFSE> result = fournisseurServiceRepository.findAll();

        return result;
    }

    /**
     *  Get one fournisseurService by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public FournisseurFSE findOne(Long id) {
        log.debug("Request to get FournisseurService : {}", id);
        FournisseurFSE fournisseurService = fournisseurServiceRepository.findOne(id);
        return fournisseurService;
    }

    /**
     *  Delete the  fournisseurService by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete FournisseurService : {}", id);
        fournisseurServiceRepository.deleteById(id);
    }

    @Override
        @Transactional(readOnly = true)
    public FournisseurService findByCodeFse(String fseSource) {
     
        FournisseurFSE fournisseurService = fournisseurServiceRepository.findByCodeFse(fseSource);
        return fournisseurService;
    }

    
    @Override
        @Transactional(readOnly = true)
    @Cacheable(value = "findUoByCodeFse", key = "#p0", sync = true)
    public FournisseurFSE findUoByCodeFse(String fseSource) {
        FournisseurFSE fournisseurService = fournisseurServiceRepository.findByCodeFse(fseSource);
        return fournisseurService;
    }
    
    
    @Override
    @Transactional(readOnly = true)
    
@Cacheable(value = "fseCompense", key = "#p0", sync = true)
    public FournisseurFSE findByFseCompense(FournisseurService FseDest) {
          FournisseurFSE fournisseurService = fournisseurServiceRepository.findByFseCompense(FseDest);
        return fournisseurService;
    }

   
}
