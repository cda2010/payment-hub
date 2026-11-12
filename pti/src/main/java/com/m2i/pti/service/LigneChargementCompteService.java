package com.m2i.pti.service;

import com.m2i.pti.domain.LigneChargementCompte;
import com.m2i.pti.domain.enumeration.StatutLigneChargement;

import java.util.List;

/**
 * Service Interface for managing LigneChargementCompte.
 */
public interface LigneChargementCompteService {

    /**
     * Save a ligneChargementCompte.
     *
     * @param ligneChargementCompte the entity to save
     * @return the persisted entity
     */
    LigneChargementCompte save(LigneChargementCompte ligneChargementCompte);

    /**
     *  Get all the ligneChargementComptes.
     *
     *  @return the list of entities
     */
    List<LigneChargementCompte> findAll();

    /**
     *  Get the "id" ligneChargementCompte.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    LigneChargementCompte findOne(Long id);

    /**
     *  Delete the "id" ligneChargementCompte.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);
    List<LigneChargementCompte>  queryDetail(Long id);
    public Integer findById(Long id);

    void deleteAll(Long id);

    public List<LigneChargementCompte> findByChargementCompteIdAndStatutLigneChargement(Long id, StatutLigneChargement statut);
}
