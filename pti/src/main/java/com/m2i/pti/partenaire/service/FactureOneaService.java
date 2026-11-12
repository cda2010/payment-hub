package com.m2i.pti.partenaire.service;

import com.m2i.pti.partenaire.domain.FactureOnea;
import com.m2i.pti.partenaire.repository.FactureOneaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing FactureOnea.
 */
@Service
@Transactional
public class FactureOneaService {

    private final Logger log = LoggerFactory.getLogger(FactureOneaService.class);

    private final FactureOneaRepository factureOneaRepository;

    public FactureOneaService(FactureOneaRepository factureOneaRepository) {
        this.factureOneaRepository = factureOneaRepository;
    }

    /**
     * Save a factureOnea.
     *
     * @param factureOnea the entity to save
     * @return the persisted entity
     */
    public FactureOnea save(FactureOnea factureOnea) {
        log.debug("Request to save FactureOnea : {}", factureOnea);
        return factureOneaRepository.save(factureOnea);
    }

    /**
     *  Get all the factureOneas.
     *
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<FactureOnea> findAll() {
        log.debug("Request to get all FactureOneas");
        return factureOneaRepository.findAll();
    }

    /**
     *  Get one factureOnea by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public FactureOnea findOne(Long id) {
        log.debug("Request to get FactureOnea : {}", id);
        return factureOneaRepository.findById(id).orElse(null);
    }

    /**
     *  Delete the  factureOnea by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete FactureOnea : {}", id);
        factureOneaRepository.deleteById(id);
    }
}
