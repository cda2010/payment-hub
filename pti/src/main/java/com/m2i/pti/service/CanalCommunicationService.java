package com.m2i.pti.service;

import com.m2i.pti.domain.CanalCommunication;
import com.m2i.pti.repository.CanalCommunicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing CanalCommunication.
 */
@Service
@Transactional
public class CanalCommunicationService {

    private final Logger log = LoggerFactory.getLogger(CanalCommunicationService.class);

    private final CanalCommunicationRepository canalCommunicationRepository;

    public CanalCommunicationService(CanalCommunicationRepository canalCommunicationRepository) {
        this.canalCommunicationRepository = canalCommunicationRepository;
    }

    /**
     * Save a canalCommunication.
     *
     * @param canalCommunication the entity to save
     * @return the persisted entity
     */
    public CanalCommunication save(CanalCommunication canalCommunication) {
        log.debug("Request to save CanalCommunication : {}", canalCommunication);
        return canalCommunicationRepository.save(canalCommunication);
    }

    /**
     * Get all the canalCommunications.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<CanalCommunication> findAll() {
        log.debug("Request to get all CanalCommunications");
        return canalCommunicationRepository.findAll();
    }

    /**
     * Get one canalCommunication by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public CanalCommunication findOne(Long id) {
        log.debug("Request to get CanalCommunication : {}", id);
        return canalCommunicationRepository.findById(id).orElse(null);
    }

    /**
     * Delete the canalCommunication by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete CanalCommunication : {}", id);
        canalCommunicationRepository.deleteById(id);
    }
}
