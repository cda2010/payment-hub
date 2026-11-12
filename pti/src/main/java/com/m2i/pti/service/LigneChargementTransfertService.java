package com.m2i.pti.service;

import com.m2i.pti.domain.LigneChargementTransfert;
import com.m2i.pti.domain.enumeration.StatutLigneChargement;
import org.springframework.data.domain.Page;

import jakarta.persistence.Enumerated;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
/**
 * Service Interface for managing LigneChargementTransfert.
 */
public interface LigneChargementTransfertService {

    /**
     * Save a ligneChargementTransfert.
     *
     * @param ligneChargementTransfert the entity to save
     * @return the persisted entity
     */
    LigneChargementTransfert save(LigneChargementTransfert ligneChargementTransfert);

    /**
     *  Get all the ligneChargementTransferts.
     *
     *  @return the list of entities
     */
    List<LigneChargementTransfert> findAll();

    /**
     *  Get the "id" ligneChargementTransfert.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    LigneChargementTransfert findOne(Long id);
 Integer  findNombre(Long id);
    /**
     *  Delete the "id" ligneChargementTransfert.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);
    List<LigneChargementTransfert>  queryDetail(Long id);
    public Integer findById(Long id);

    void deleteAll(Long id);

    List<LigneChargementTransfert> findByChargementTransfertIdAndStatutLigneChargement(Long id);

    List<LigneChargementTransfert> findByChargementTransfertIdAndStatutLigneChargement(Pageable page, Long id);
      public Integer findCompteByChargementTransfertIdAndStatutLigneChargement(Long idChargement, StatutLigneChargement valueOf);
    public Double findSommeByChargementTransfertIdAndStatutLigneChargement(Long idChargement, StatutLigneChargement valueOf);
}
