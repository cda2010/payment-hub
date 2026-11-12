package com.m2i.pti.service;

import com.m2i.pti.domain.VerifOtp;
import com.m2i.pti.repository.VerifOtpRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing VerifOtp.
 */
@Service
@Transactional
public class VerifOtpService {

    private final Logger log = LoggerFactory.getLogger(VerifOtpService.class);

    private final VerifOtpRepository verifOtpRepository;

    public VerifOtpService(VerifOtpRepository verifOtpRepository) {
        this.verifOtpRepository = verifOtpRepository;
    }

    /**
     * Save a verifOtp.
     *
     * @param verifOtp the entity to save
     * @return the persisted entity
     */
    public VerifOtp save(VerifOtp verifOtp) {
        log.debug("Request to save VerifOtp : {}", verifOtp);
        return verifOtpRepository.save(verifOtp);
    }

    /**
     * Get all the verifOtps.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<VerifOtp> findAll(Pageable pageable) {
        log.debug("Request to get all VerifOtps");
        return verifOtpRepository.findAll(pageable);
    }

    /**
     * Get one verifOtp by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public VerifOtp findOne(Long id) {
        log.debug("Request to get VerifOtp : {}", id);
        return verifOtpRepository.findById(id).orElse(null);
    }

    /**
     * Delete the verifOtp by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete VerifOtp : {}", id);
        verifOtpRepository.deleteById(id);
    }
}
