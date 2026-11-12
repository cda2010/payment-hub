package com.m2i.omnicanal.application.service.impl;


import com.m2i.omnicanal.model.FournisseurService;
import com.m2i.omnicanal.application.domain.FournisseurFSE;
import com.m2i.omnicanal.application.repository.FournisseurServiceRepository;
import com.m2i.omnicanal.application.service.FournisseurServiceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

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
    public List<FournisseurFSE> findCompense(Long idFse) {
           List<FournisseurFSE> result;
        if (idFse==0){
             result = fournisseurServiceRepository.findAll();
        }else{
             result = fournisseurServiceRepository.findButMinusFseEnCours(idFse);
        }
        

        return result;
    }
}
