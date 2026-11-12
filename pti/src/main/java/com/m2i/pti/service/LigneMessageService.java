package com.m2i.pti.service;

import com.m2i.pti.domain.LigneMessage;
import com.m2i.pti.repository.LigneMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Service Implementation for managing LigneMessage.
 */
@Service
@Transactional
public class LigneMessageService {

    private final Logger log = LoggerFactory.getLogger(LigneMessageService.class);

    private final LigneMessageRepository ligneMessageRepository;

    public LigneMessageService(LigneMessageRepository ligneMessageRepository) {
        this.ligneMessageRepository = ligneMessageRepository;
    }

    /**
     * Save a ligneMessage.
     *
     * @param ligneMessage the entity to save
     * @return the persisted entity
     */

    public LigneMessage save(LigneMessage ligneMessage) {
        log.debug("Request to save LigneMessage : {}", ligneMessage);
        return ligneMessageRepository.save(ligneMessage);
    }

    /**
     *  Get all the ligneMessages.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<LigneMessage> findAll(Pageable pageable) {
        log.debug("Request to get all LigneMessages");
        return ligneMessageRepository.findAll(pageable);
    }

    /**
     *  Get one ligneMessage by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public LigneMessage findOne(Long id) {
        log.debug("Request to get LigneMessage : {}", id);
        return ligneMessageRepository.findById(id).orElse(null);
    }

    /**
     *  Delete the  ligneMessage by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete LigneMessage : {}", id);
        ligneMessageRepository.deleteById(id);
    }

    public void deleteAll(Long id) {
        ligneMessageRepository.deleteLigne(id);
    }
    public Integer findById(Long id) {
       return ligneMessageRepository.countByChargementMessageId(id);
    }

    @Transactional(readOnly = true)
    public List<LigneMessage> getLigneMessage(Long chargementId) {
        return ligneMessageRepository.findByChargementMessageId(chargementId);
    }

    @Transactional(readOnly = true)
    public Page<LigneMessage> getLigneMessagePage(Long chargementId,Pageable pageable){
        return ligneMessageRepository.findAllPageByMessage(chargementId,pageable);
    }
}
