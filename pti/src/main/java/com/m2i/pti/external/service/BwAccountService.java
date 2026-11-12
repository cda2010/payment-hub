package com.m2i.pti.external.service;


import com.m2i.pti.domain.BwAccount;
import com.m2i.pti.repository.BwAccountRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service Implementation for managing {@link BwAccount}.
 */
@Service
@Transactional
public class BwAccountService  {

    private final Logger log = LoggerFactory.getLogger(BwAccountService.class);

    private final BwAccountRepository bwAccountRepository;

    public BwAccountService(BwAccountRepository bwAccountRepository) {
        this.bwAccountRepository = bwAccountRepository;
    }

    /**
     * Save a bwAccount.
     *
     * @param bwAccount the entity to save.
     * @return the persisted entity.
     */

    public BwAccount save(BwAccount bwAccount) {
        log.debug("Request to save BwAccount : {}", bwAccount);
        return bwAccountRepository.save(bwAccount);
    }

    /**
     * Get all the bwAccounts.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */

    @Transactional(readOnly = true)
    public Page<BwAccount> findAll(Pageable pageable) {
        log.debug("Request to get all BwAccounts");
        return bwAccountRepository.findAll(pageable);
    }


    /**
     * Get one bwAccount by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
  
    @Transactional(readOnly = true)
    public BwAccount findOne(Long id) {
        log.debug("Request to get BwAccount : {}", id);
        return bwAccountRepository.findById(id).orElse(null);
    }

    /**
     * Delete the bwAccount by id.
     *
     * @param id the id of the entity.
     */

    public void delete(Long id) {
        log.debug("Request to delete BwAccount : {}", id);
        bwAccountRepository.deleteById(id);
    }


    public List<BwAccount> findByBwCustomerId(Long cusId) {
        return bwAccountRepository.findByBwCustomerId(cusId);
    }
}
