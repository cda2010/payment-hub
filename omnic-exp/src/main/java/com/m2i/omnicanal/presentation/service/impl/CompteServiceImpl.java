package com.m2i.omnicanal.presentation.service.impl;

import com.m2i.omnicanal.model.Agence;
import com.m2i.omnicanal.presentation.domain.CompteFse;
import com.m2i.omnicanal.presentation.repository.CompteRepository;
import com.m2i.omnicanal.presentation.service.CompteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.cache.annotation.Cacheable;

/**
 * Service Implementation for managing Compte.
 */
@Service
@Transactional
public class CompteServiceImpl implements CompteService {

    private final Logger log = LoggerFactory.getLogger(CompteServiceImpl.class);

    private final CompteRepository compteRepository;

    public CompteServiceImpl(CompteRepository compteRepository) {
        this.compteRepository = compteRepository;
    }

    /**
     * Save a compte.
     *
     * @param compte the entity to save
     * @return the persisted entity
     */
    @Override
    public CompteFse save(CompteFse compte) {
        log.debug("Request to save Compte : {}", compte);
        CompteFse result = compteRepository.save(compte);
        return result;
    }

    /**
     * Get all the comptes.
     *
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<CompteFse> findAll() {
        log.debug("Request to get all Comptes");
        List<CompteFse> result = compteRepository.findAll();

        return result;
    }

    /**
     * Get one compte by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public CompteFse findOne(Long id) {
        log.debug("Request to get Compte : {}", id);
        CompteFse compte = compteRepository.findOne(id);
        return compte;
    }

    /**
     * Delete the compte by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Compte : {}", id);
        compteRepository.deleteById(id);
    }

    @Override
    public CompteFse findCompteByEtiquetteAndFse(String codeFse, String etiquette) {
        CompteFse compte = compteRepository.findCompteByEtiquetteAndFse(codeFse, etiquette);
        return compte;
    }
 @Cacheable(value = "findCompteFse", key = "#p0 + '|' + #p1+ '|' + #p2+ '|' + #p3+ '|' + #p4", sync = true)
    @Override
    
    public CompteFse findCompteFse(String codeParametre, Agence agence, Long typeServiceId, Long relationId, String codeFseTeneur) {
        CompteFse cpte = compteRepository.findCompteFse(codeParametre, agence, typeServiceId, relationId, codeFseTeneur);
        return cpte;
    }

}
