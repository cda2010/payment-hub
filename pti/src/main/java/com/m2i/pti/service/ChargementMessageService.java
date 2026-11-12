package com.m2i.pti.service;

import com.m2i.pti.domain.ChargementMessage;
import com.m2i.pti.repository.ChargementMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Service Implementation for managing ChargementMessage.
 */
@Service
@Transactional
public class ChargementMessageService {

    private final Logger log = LoggerFactory.getLogger(ChargementMessageService.class);

    private final ChargementMessageRepository chargementMessageRepository;

    public ChargementMessageService(ChargementMessageRepository chargementMessageRepository) {
        this.chargementMessageRepository = chargementMessageRepository;
    }

    /**
     * Save a chargementMessage.
     *
     * @param chargementMessage the entity to save
     * @return the persisted entity
     */
    public ChargementMessage save(ChargementMessage chargementMessage) {
        log.debug("Request to save ChargementMessage : {}", chargementMessage);
        return chargementMessageRepository.save(chargementMessage);
    }

    /**
     *  Get all the chargementMessages.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<ChargementMessage> findAll() {
        log.debug("Request to get all ChargementMessages");
        return chargementMessageRepository.findAllMessage();
    }

    /**
     *  Get one chargementMessage by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public ChargementMessage findOne(Long id) {
        log.debug("Request to get ChargementMessage : {}", id);
        return chargementMessageRepository.findById(id).orElse(null);
    }

    /**
     *  Delete the  chargementMessage by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete ChargementMessage : {}", id);
        chargementMessageRepository.deleteById(id);
    }
    public Integer findByHashFichier(String hash) {
        return chargementMessageRepository.findByHashFichier(hash);
        };
    public Integer findByNomFichier(String toUpperCase) {
        return chargementMessageRepository.findByNomFichier(toUpperCase);

    };
}
