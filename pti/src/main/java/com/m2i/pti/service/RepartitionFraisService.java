package com.m2i.pti.service;

import com.m2i.pti.domain.RepartitionFrais;
import com.m2i.pti.domain.enumeration.Acteur;
import com.m2i.pti.repository.RepartitionFraisRepository;
import com.m2i.pti.service.dto.RepartitionFraisDTO;
import com.m2i.pti.service.mapper.RepartitionFraisMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;


/**
 * Service Implementation for managing RepartitionFrais.
 */
@Service
@Transactional
public class RepartitionFraisService {

    private final Logger log = LoggerFactory.getLogger(RepartitionFraisService.class);

    private final RepartitionFraisRepository repartitionFraisRepository;

    private final RepartitionFraisMapper repartitionFraisMapper;

    private final UniteOrganisationService uniteOrganisationService;

    public RepartitionFraisService(RepartitionFraisRepository repartitionFraisRepository, RepartitionFraisMapper repartitionFraisMapper, UniteOrganisationService uniteOrganisationService) {
        this.repartitionFraisRepository = repartitionFraisRepository;
        this.repartitionFraisMapper = repartitionFraisMapper;
        this.uniteOrganisationService = uniteOrganisationService;
    }

    /**
     * Save a repartitionFrais.
     *
     * @param repartitionFraisDTO the entity to save
     * @return the persisted entity
     */
    public RepartitionFraisDTO save(RepartitionFraisDTO repartitionFraisDTO) {
        log.debug("Request to save RepartitionFrais : {}", repartitionFraisDTO);
        Long fseSourceId = repartitionFraisDTO.getFseSourceId();
        Long fseDestId = repartitionFraisDTO.getFseDestId();
        String poids = "";
        // Le poids correspond à la concatenation des niveaux des UO
        if (fseSourceId != null) {
            poids += uniteOrganisationService.getUONiveau(fseSourceId);
        }
        if (fseDestId != null) {
            poids += uniteOrganisationService.getUONiveau(fseDestId);
        }
        repartitionFraisDTO.setPoidsNum(!poids.equals("") ? Integer.parseInt(poids) : 0);
        RepartitionFrais repartitionFrais = repartitionFraisMapper.toEntity(repartitionFraisDTO);
        repartitionFrais = repartitionFraisRepository.save(repartitionFrais);
        return repartitionFraisMapper.toDto(repartitionFrais);
    }

    /**
     * Get all the repartitionFrais.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<RepartitionFraisDTO> findAll(Pageable pageable, Long typeServiceId, Long fseSrcId, Long fseDestId, Acteur acteur) {
        return repartitionFraisRepository.findByMultiCriteria(typeServiceId, acteur, fseSrcId, fseDestId, pageable).map(repartitionFraisMapper::toDto);
    }

    /**
     * Get one repartitionFrais by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public RepartitionFraisDTO findOne(Long id) {
        log.debug("Request to get RepartitionFrais : {}", id);
        RepartitionFrais repartitionFrais = repartitionFraisRepository.findById(id).orElse(null);
        return repartitionFraisMapper.toDto(repartitionFrais);
    }

    /**
     * Recherche de la repartion de frais de l'operation en cours d'execution
     *
     * @param typeServiceId
     * @param acteur
     * @param montantFrais
     * @return
     */
    public RepartitionFrais findByCriteria(Long typeServiceId, Acteur acteur, BigDecimal montantFrais, BigDecimal montantOp) {
        RepartitionFrais repartitionFrais = repartitionFraisRepository.findByTypeServiceIdAndActeurAndMinLessThanEqualAndMaxGreaterThanEqualAndActivatedIsTrue(typeServiceId, acteur, montantFrais.doubleValue(), montantFrais.doubleValue(), montantOp.doubleValue(), montantOp.doubleValue());
        if (repartitionFrais == null) {
            return repartitionFraisRepository.findByTypeServiceIdAndActeur(typeServiceId, acteur);
        } else return repartitionFrais;
    }


    public RepartitionFrais findByCriteriaAndFse(Long typeServiceId, Acteur acteur, BigDecimal montantFrais, BigDecimal montantOp, Long fseSrcId, Long fseDestId) {
        List<RepartitionFrais> repartitionFrais = repartitionFraisRepository.findByMultiParams(typeServiceId, acteur, fseSrcId, fseDestId, montantFrais.doubleValue(), montantFrais.doubleValue(), montantOp.doubleValue(), montantOp.doubleValue());
        if (repartitionFrais == null) {
            return repartitionFraisRepository.findByTypeServiceIdAndActeur(typeServiceId, acteur);
        } else {
            if (!repartitionFrais.isEmpty())
                return repartitionFrais.get(0);
        }
        return null;
    }

    /**
     * Delete the  repartitionFrais by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete RepartitionFrais : {}", id);
        repartitionFraisRepository.deleteById(id);
    }
}
