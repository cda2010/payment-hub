package com.m2i.pti.service.impl;

import com.m2i.pti.domain.enumeration.StatutLigneChargement;
import com.m2i.pti.service.LigneChargementCompteService;
import com.m2i.pti.domain.LigneChargementCompte;
import com.m2i.pti.repository.LigneChargementCompteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Service Implementation for managing LigneChargementCompte.
 */
@Service
@Transactional
public class LigneChargementCompteServiceImpl implements LigneChargementCompteService{

    private final Logger log = LoggerFactory.getLogger(LigneChargementCompteServiceImpl.class);

    private final LigneChargementCompteRepository ligneChargementCompteRepository;

    public LigneChargementCompteServiceImpl(LigneChargementCompteRepository ligneChargementCompteRepository) {
        this.ligneChargementCompteRepository = ligneChargementCompteRepository;
    }

    /**
     * Save a ligneChargementCompte.
     *
     * @param ligneChargementCompte the entity to save
     * @return the persisted entity
     */
    @Override
    public LigneChargementCompte save(LigneChargementCompte ligneChargementCompte) {
        log.debug("Request to save LigneChargementCompte : {}", ligneChargementCompte);
        return ligneChargementCompteRepository.save(ligneChargementCompte);
    }

    /**
     *  Get all the ligneChargementComptes.
     *
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<LigneChargementCompte> findAll() {
        log.debug("Request to get all LigneChargementComptes");
        return ligneChargementCompteRepository.findAll();
    }

    /**
     *  Get one ligneChargementCompte by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public LigneChargementCompte findOne(Long id) {
        log.debug("Request to get LigneChargementCompte : {}", id);
        return ligneChargementCompteRepository.findById(id).orElse(null);
    }

    /**
     *  Delete the  ligneChargementCompte by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete LigneChargementCompte : {}", id);
        ligneChargementCompteRepository.deleteById(id);
    }

    @Override
    public List<LigneChargementCompte> queryDetail(Long id){
        return ligneChargementCompteRepository.queryDetail(id);
    }

    @Override
    public Integer findById(Long id) {
        Integer nombre = ligneChargementCompteRepository.countByChargementCompte(id);
        return nombre;

    }
    
     
 

    @Override
    public void deleteAll(Long id) {
        log.debug("Request to delete LigneChargement : {}", id);
        ligneChargementCompteRepository.deleteAlls(id);
    }

    @Override
    public List<LigneChargementCompte> findByChargementCompteIdAndStatutLigneChargement(Long id, StatutLigneChargement statut) {
            return ligneChargementCompteRepository.findByChargementCompteIdAndStatutLigneChargement(id,statut);
    }
   
}
