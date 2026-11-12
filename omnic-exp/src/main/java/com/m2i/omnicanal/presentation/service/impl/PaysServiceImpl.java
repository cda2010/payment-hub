package com.m2i.omnicanal.presentation.service.impl;

import com.m2i.omnicanal.presentation.domain.PaysFse;
import com.m2i.omnicanal.presentation.repository.PaysRepository;
import com.m2i.omnicanal.presentation.service.PaysService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing Pays.
 */
@Service
@Transactional
public class PaysServiceImpl implements PaysService{

    private final Logger log = LoggerFactory.getLogger(PaysServiceImpl.class);
    
    private final PaysRepository paysRepository;

    public PaysServiceImpl(PaysRepository paysRepository) {
        this.paysRepository = paysRepository;
    }

    /**
     * Save a pays.
     *
     * @param pays the entity to save
     * @return the persisted entity
     */
    @Override
    public PaysFse save(PaysFse pays) {
        log.debug("Request to save Pays : {}", pays);
        PaysFse result = paysRepository.save(pays);
        return result;
    }

    /**
     *  Get all the pays.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<PaysFse> findAll() {
        log.debug("Request to get all Pays");
        List<PaysFse> result = paysRepository.findAll();

        return result;
    }

    /**
     *  Get one pays by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public PaysFse findOne(Long id) {
        log.debug("Request to get Pays : {}", id);
        PaysFse pays = paysRepository.findOne(id);
        return pays;
    }

    /**
     *  Delete the  pays by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Pays : {}", id);
        paysRepository.deleteById(id);
    }
}
