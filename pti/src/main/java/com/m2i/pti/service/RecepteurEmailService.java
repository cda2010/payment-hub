package com.m2i.pti.service;

import com.m2i.pti.domain.RecepteurEmail;
import com.m2i.pti.repository.RecepteurEmailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing RecepteurEmail.
 */
@Service
@Transactional
public class RecepteurEmailService {

    private final Logger log = LoggerFactory.getLogger(RecepteurEmailService.class);

    private final RecepteurEmailRepository recepteurEmailRepository;

    public RecepteurEmailService(RecepteurEmailRepository recepteurEmailRepository) {
        this.recepteurEmailRepository = recepteurEmailRepository;
    }

    /**
     * Save a recepteurEmail.
     *
     * @param recepteurEmail the entity to save
     * @return the persisted entity
     */
    public RecepteurEmail save(RecepteurEmail recepteurEmail) {
        log.debug("Request to save RecepteurEmail : {}", recepteurEmail);
        return recepteurEmailRepository.save(recepteurEmail);
    }

    /**
     * Get all the recepteurEmails.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<RecepteurEmail> findAll() {
        log.debug("Request to get all RecepteurEmails");
        return recepteurEmailRepository.findAll();
    }

    /**
     * Get one recepteurEmail by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public RecepteurEmail findOne(Long id) {
        log.debug("Request to get RecepteurEmail : {}", id);
        return recepteurEmailRepository.findById(id).orElse(null);
    }

    /**
     * Delete the recepteurEmail by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete RecepteurEmail : {}", id);
        recepteurEmailRepository.deleteById(id);
    }
}
