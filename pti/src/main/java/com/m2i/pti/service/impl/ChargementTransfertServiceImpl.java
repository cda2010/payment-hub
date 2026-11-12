package com.m2i.pti.service.impl;

import com.m2i.pti.domain.LigneChargement;
import com.m2i.pti.service.ChargementTransfertService;
import com.m2i.pti.domain.ChargementTransfert;
import com.m2i.pti.domain.Client;
import com.m2i.pti.repository.ChargementTransfertRepository;
import org.hibernate.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing ChargementTransfert.
 */
@Service
@Transactional
public class ChargementTransfertServiceImpl implements ChargementTransfertService {

    private final Logger log = LoggerFactory.getLogger(ChargementTransfertServiceImpl.class);

    private final ChargementTransfertRepository chargementTransfertRepository;

    public ChargementTransfertServiceImpl(ChargementTransfertRepository chargementTransfertRepository) {
        this.chargementTransfertRepository = chargementTransfertRepository;
    }

    /**
     * Save a chargementTransfert.
     *
     * @param chargementTransfert the entity to save
     * @return the persisted entity
     */
    @Override
    public ChargementTransfert save(ChargementTransfert chargementTransfert) {
        log.debug("Request to save ChargementTransfert : {}", chargementTransfert);
        return chargementTransfertRepository.save(chargementTransfert);
    }

    /**
     * Get all the chargementTransferts.
     *
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<ChargementTransfert> findAll() {
        log.debug("Request to get all ChargementTransferts");
        return chargementTransfertRepository.findAll();
    }

    /**
     * Get one chargementTransfert by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public ChargementTransfert findOne(Long id) {
        log.debug("Request to get ChargementTransfert : {}", id);
        return chargementTransfertRepository.findById(id).orElse(null);
    }

    /**
     * Delete the chargementTransfert by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete ChargementTransfert : {}", id);
        chargementTransfertRepository.deleteById(id);
    }

    @Override
    public Integer findByHashFichier(String hash) {
        Integer nombre = chargementTransfertRepository.findByHashFichier(hash);
        return nombre;
    }

    @Override
    public Integer findByNomFichier(String nomFichier) {
        Integer nombre = chargementTransfertRepository.findByNomFichier(nomFichier);
        return nombre;

    }

    @Override
    public List<ChargementTransfert> findAllOrderByDate() {
        List<ChargementTransfert> result = chargementTransfertRepository.findAllOrderByDate();
        return result;
    }

    @Override
    public List<ChargementTransfert> findByClient(Client c, String statut) {
        List<ChargementTransfert> result = chargementTransfertRepository.findByClient(c, statut);
        return result;
    }    
}
