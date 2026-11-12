package com.m2i.pti.service;

import com.m2i.pti.domain.ChargementTransfert;
import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.LigneChargement;

import java.util.List;

/**
 * Service Interface for managing ChargementTransfert.
 */
public interface ChargementTransfertService {

    /**
     * Save a chargementTransfert.
     *
     * @param chargementTransfert the entity to save
     * @return the persisted entity
     */
    ChargementTransfert save(ChargementTransfert chargementTransfert);

    /**
     *  Get all the chargementTransferts.
     *
     *  @return the list of entities
     */
    List<ChargementTransfert> findAll();

    /**
     *  Get the "id" chargementTransfert.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    ChargementTransfert findOne(Long id);

    /**
     *  Delete the "id" chargementTransfert.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);
    public Integer findByHashFichier(String hash);
    public Integer findByNomFichier(String toUpperCase);

    public List<ChargementTransfert> findAllOrderByDate();
      public List<ChargementTransfert> findByClient(Client c, String statut);
}
