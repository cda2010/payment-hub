package com.m2i.pti.service.impl;

import com.m2i.pti.domain.enumeration.StatutLigneChargement;
import com.m2i.pti.service.LigneChargementTransfertService;
import com.m2i.pti.domain.LigneChargementTransfert;
import com.m2i.pti.repository.LigneChargementTransfertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Service Implementation for managing LigneChargementTransfert.
 */
@Service
@Transactional
public class LigneChargementTransfertServiceImpl implements LigneChargementTransfertService {

    private final Logger log = LoggerFactory.getLogger(LigneChargementTransfertServiceImpl.class);

    private final LigneChargementTransfertRepository ligneChargementTransfertRepository;

    public LigneChargementTransfertServiceImpl(LigneChargementTransfertRepository ligneChargementTransfertRepository) {
        this.ligneChargementTransfertRepository = ligneChargementTransfertRepository;
    }

    /**
     * Save a ligneChargementTransfert.
     *
     * @param ligneChargementTransfert the entity to save
     * @return the persisted entity
     */
    @Override
    public LigneChargementTransfert save(LigneChargementTransfert ligneChargementTransfert) {
        log.debug("Request to save LigneChargementTransfert : {}", ligneChargementTransfert);
        return ligneChargementTransfertRepository.save(ligneChargementTransfert);
    }

    /**
     * Get all the ligneChargementTransferts.
     *
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<LigneChargementTransfert> findAll() {
        log.debug("Request to get all LigneChargementTransferts");
        return ligneChargementTransfertRepository.findAll();
    }

    /**
     * Get one ligneChargementTransfert by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public LigneChargementTransfert findOne(Long id) {
        log.debug("Request to get LigneChargementTransfert : {}", id);
        return ligneChargementTransfertRepository.findById(id).orElse(null);
    }

    /**
     * Delete the ligneChargementTransfert by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete LigneChargementTransfert : {}", id);
        ligneChargementTransfertRepository.deleteById(id);
    }

    @Override
    public List<LigneChargementTransfert> queryDetail(Long id) {
        return ligneChargementTransfertRepository.queryDetail(id);
    }

    @Override
    public Integer findById(Long id) {
        Integer nombre = ligneChargementTransfertRepository.countByChargementTransfert(id);
        return nombre;

    }

    @Override
    public void deleteAll(Long id) {
        log.debug("Request to delete LigneChargement : {}", id);
        ligneChargementTransfertRepository.deleteAlls(id);
    }

    @Override
    public Integer findNombre(Long id) {
        Integer nombre = ligneChargementTransfertRepository.countByChargementTransfert(id);
        return nombre;
    }

    @Override
    public List<LigneChargementTransfert> findByChargementTransfertIdAndStatutLigneChargement(Long id) {
        return ligneChargementTransfertRepository.findByChargementTransfertIdAndStatutLigneChargement(id);
    }

    @Override
    public List<LigneChargementTransfert> findByChargementTransfertIdAndStatutLigneChargement(Pageable page, Long id) {
        return ligneChargementTransfertRepository.findByChargementTransfertIdAndStatutLigneChargement(page, id).getContent();
    }
      @Override
    public Integer findCompteByChargementTransfertIdAndStatutLigneChargement(Long idChargement, StatutLigneChargement valueOf) {
        return ligneChargementTransfertRepository.findCompteByChargementTransfertIdAndStatutLigneChargement(idChargement,valueOf);
    }

    @Override
    public Double findSommeByChargementTransfertIdAndStatutLigneChargement(Long idChargement, StatutLigneChargement valueOf) {
      return ligneChargementTransfertRepository.findSumByChargementTransfertIdAndStatutLigneChargement(idChargement,valueOf);
    }
}
