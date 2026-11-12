package com.m2i.pti.service;

import com.m2i.pti.domain.ChargementCompte;
import java.util.List;

/**
 * Service Interface for managing ChargementCompte.
 */
public interface ChargementCompteService {

    /**
     * Save a chargementCompte.
     *
     * @param chargementCompte the entity to save
     * @return the persisted entity
     */
    ChargementCompte save(ChargementCompte chargementCompte);

    /**
     *  Get all the chargementComptes.
     *
     *  @return the list of entities
     */
    List<ChargementCompte> findAll();

    /**
     *  Get the "id" chargementCompte.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    ChargementCompte findOne(Long id);

    /**
     *  Delete the "id" chargementCompte.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);
    public Integer findByHashFichier(String hash);
    public Integer findByNomFichier(String toUpperCase);

    public List<ChargementCompte> findAllOrderByDateChargement();
}
