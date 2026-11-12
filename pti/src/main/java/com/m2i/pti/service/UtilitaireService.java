package com.m2i.pti.service;

import com.m2i.pti.domain.Utilitaire;
import com.m2i.pti.repository.UtilitaireRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing Utilitaire.
 */
@Service
@Transactional
public class UtilitaireService {

    private final Logger log = LoggerFactory.getLogger(UtilitaireService.class);

    private final UtilitaireRepository utilitaireRepository;

    public UtilitaireService(UtilitaireRepository utilitaireRepository) {
        this.utilitaireRepository = utilitaireRepository;
    }

    /**
     * Save a utilitaire.
     *
     * @param utilitaire the entity to save
     * @return the persisted entity
     */
    public Utilitaire save(Utilitaire utilitaire) {
        log.debug("Request to save Utilitaire : {}", utilitaire);
        return utilitaireRepository.save(utilitaire);
    }

    /**
     *  Get all the utilitaires.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Utilitaire> findAll(Pageable pageable) {
        log.debug("Request to get all Utilitaires");
        return utilitaireRepository.findAll(pageable);
    }

    /**
     *  Get one utilitaire by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Utilitaire findOne(Long id) {
        log.debug("Request to get Utilitaire : {}", id);
        return utilitaireRepository.findById(id).orElse(null);
    }

    /**
     *  Delete the  utilitaire by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Utilitaire : {}", id);
        utilitaireRepository.deleteById(id);
    }
}
