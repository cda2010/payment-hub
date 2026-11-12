package com.m2i.pti.external.service;


import com.m2i.pti.domain.BwCustomer;
import com.m2i.pti.repository.BwCustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Implementation for managing {@link BwCustomer}.
 */
@Service
@Transactional
public class BwCustomerService {
    private final Logger log = LoggerFactory.getLogger(BwCustomerService.class);
    private final BwCustomerRepository bwCustomerRepository;
    public BwCustomerService(BwCustomerRepository bwCustomerRepository) {
        this.bwCustomerRepository = bwCustomerRepository;
    }

    /**
     * Save a bwCustomer.
     *
     * @param bwCustomer the entity to save.
     * @return the persisted entity.
     */
 
    public BwCustomer save(BwCustomer bwCustomer) {
        log.debug("Request to save BwCustomer : {}", bwCustomer);
        return bwCustomerRepository.save(bwCustomer);
    }

    /**
     * Get all the bwCustomers.
     *
     * @return the list of entities.
     */

    @Transactional(readOnly = true)
    public List<BwCustomer> findAll() {
        log.debug("Request to get all BwCustomers");
        return bwCustomerRepository.findAll();
    }

    /**
     * Get one bwCustomer by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
   
    @Transactional(readOnly = true)
    public BwCustomer findOne(Long id) {
        log.debug("Request to get BwCustomer : {}", id);
        return bwCustomerRepository.findById(id).orElse(null);
    }

    /**
     * Delete the bwCustomer by id.
     *
     * @param id the id of the entity.
     */
 
    public void delete(Long id) {
        log.debug("Request to delete BwCustomer : {}", id);
        bwCustomerRepository.deleteById(id);
    }


    public Page<BwCustomer> findAll(Pageable pageable) {
        return bwCustomerRepository.findAll(pageable);
    }


    public Page<BwCustomer> findAll(Pageable pageable, String nom, String prenom, String telephone,boolean valide) {
        return bwCustomerRepository.findAll(pageable, nom, prenom, telephone,valide);
    }


    public BwCustomer findByTelephone(String msisdn) {
        return bwCustomerRepository.findByTelephone(msisdn);
    }

    public Page<BwCustomer> findAll(Pageable pageable, String nom, String prenom, String telephone, boolean valide, Long uoId) {
           return bwCustomerRepository.findAll(pageable, nom, prenom, telephone,valide,uoId);
    }
}
