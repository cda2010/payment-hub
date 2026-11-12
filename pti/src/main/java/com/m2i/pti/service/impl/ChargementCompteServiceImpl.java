package com.m2i.pti.service.impl;

import com.m2i.pti.service.ChargementCompteService;
import com.m2i.pti.domain.ChargementCompte;
import com.m2i.pti.repository.ChargementCompteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing ChargementCompte.
 */
@Service
@Transactional
public class ChargementCompteServiceImpl implements ChargementCompteService{

    private final Logger log = LoggerFactory.getLogger(ChargementCompteServiceImpl.class);

    private final ChargementCompteRepository chargementCompteRepository;

    public ChargementCompteServiceImpl(ChargementCompteRepository chargementCompteRepository) {
        this.chargementCompteRepository = chargementCompteRepository;
    }

    /**
     * Save a chargementCompte.
     *
     * @param chargementCompte the entity to save
     * @return the persisted entity
     */
    @Override
    public ChargementCompte save(ChargementCompte chargementCompte) {
        log.debug("Request to save ChargementCompte : {}", chargementCompte);
        return chargementCompteRepository.save(chargementCompte);
    }

    /**
     *  Get all the chargementComptes.
     *
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<ChargementCompte> findAll() {
        log.debug("Request to get all ChargementComptes");
        return chargementCompteRepository.findAll();
    }

    /**
     *  Get one chargementCompte by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public ChargementCompte findOne(Long id) {
        log.debug("Request to get ChargementCompte : {}", id);
        return chargementCompteRepository.findById(id).orElse(null);
    }

    /**
     *  Delete the  chargementCompte by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete ChargementCompte : {}", id);
        chargementCompteRepository.deleteById(id);
    }
    @Override
    public Integer findByHashFichier(String hash) {
        Integer nombre = chargementCompteRepository.findByHashFichier(hash);
        return nombre;
    }
    @Override
    public Integer findByNomFichier(String nomFichier) {
        Integer nombre = chargementCompteRepository.findByNomFichier(nomFichier);
        return nombre;

    }

    @Override
    public List<ChargementCompte> findAllOrderByDateChargement() {
         log.debug("Request to get all ChargementComptes");
        return chargementCompteRepository.findAllOrderByDateChargement();
    }
}
