package com.m2i.pti.service;

import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.RegleCommission;
import com.m2i.pti.domain.enumeration.CategorieCompte;
import com.m2i.pti.domain.enumeration.EtiquetteCompteCode;
import com.m2i.pti.domain.enumeration.TypeOperateur;
import com.m2i.pti.repository.CompteRepository;
import com.m2i.pti.repository.RegleCommissionRepository;
import com.m2i.pti.service.dto.RegleCommissionDTO;
import com.m2i.pti.service.mapper.CompteAutreMapper;
import com.m2i.pti.service.mapper.CompteBanqueMapper;
import com.m2i.pti.service.mapper.CompteMapper;
import com.m2i.pti.service.mapper.RegleCommissionMapper;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


/**
 * Service Implementation for managing RegleCommission.
 */
@Service
@Transactional
public class RegleCommissionService {

    private final Logger log = LoggerFactory.getLogger(RegleCommissionService.class);

    private final RegleCommissionRepository regleCommissionRepository;

    private final RegleCommissionMapper regleCommissionMapper;

    private final UniteOrganisationService uniteOrganisationService;

    private final CompteRepository compteRepository;

    private final CompteMapper compteMapper;

    private final CompteBanqueMapper compteBanqueMapper;

    private final CompteAutreMapper compteAutreMapper;

    public RegleCommissionService(RegleCommissionRepository regleCommissionRepository, RegleCommissionMapper regleCommissionMapper, UniteOrganisationService uniteOrganisationService, CompteRepository compteRepository, CompteMapper compteMapper, CompteBanqueMapper compteBanqueMapper, CompteAutreMapper compteAutreMapper) {
        this.regleCommissionRepository = regleCommissionRepository;
        this.regleCommissionMapper = regleCommissionMapper;
        this.uniteOrganisationService = uniteOrganisationService;
        this.compteRepository = compteRepository;
        this.compteMapper = compteMapper;
        this.compteBanqueMapper = compteBanqueMapper;
        this.compteAutreMapper = compteAutreMapper;
    }

    /**
     * Save a regleCommission.
     *
     * @param regleCommissionDTO the entity to save
     * @return the persisted entity
     */
    public RegleCommissionDTO save(RegleCommissionDTO regleCommissionDTO) {
        log.info("Request to save RegleCommission : {}", regleCommissionDTO);
        Long teneurSrcId = regleCommissionDTO.getTeneurSrcId();
        Long teneurDestId = regleCommissionDTO.getTeneurDestId();
        String poids = "";
        if (regleCommissionDTO.getTypeOperateur() != null) {
            poids += "1";
        } else poids += "0";
        if (teneurSrcId != null) {
            poids += uniteOrganisationService.getUONiveau(teneurSrcId);
        }
        if (teneurDestId != null) {
            poids += uniteOrganisationService.getUONiveau(teneurDestId);
        }

        if (regleCommissionDTO.getCategorieCompte().equals(CategorieCompte.AUTRE)) {
            regleCommissionDTO.setCodeAgence("");
            regleCommissionDTO.setCodeBanque("");
            regleCommissionDTO.setRib("");
        }
        regleCommissionDTO.setEtiquette(EtiquetteCompteCode.CPTESRCFRAIS.toString());
        RegleCommission regleCommission = regleCommissionMapper.toEntity(regleCommissionDTO);
        regleCommission = regleCommissionRepository.save(regleCommission);
        regleCommission.setPoidsNum(!poids.equals("") ? Integer.parseInt(poids) : 0);
        return regleCommissionMapper.toDto(regleCommission);
    }

    /**
     * Get all the regleCommissions.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<RegleCommissionDTO> findAll(Long teneurSrcId, Long teneurDestId, Long typeServiceId, Pageable pageable) {
        return regleCommissionRepository.findByMultiCriteria(typeServiceId, teneurSrcId, teneurDestId, pageable)
                .map(regleCommissionMapper::toDto);
    }

    /**
     * @param teneurSrcId
     * @param teneurDestId
     * @param typeOperateur
     * @return
     */
    public List<RegleCommission> findByCriteria(Long uoSrcId, Long uoDestId, Long teneurSrcId, Long teneurDestId, Long typeServiceId, TypeOperateur typeOperateur) {
        log.info("Recherche des regles de repartition de l'operation");
        return regleCommissionRepository.findByCriteria(typeServiceId, teneurSrcId, teneurDestId, uoDestId, uoSrcId, typeOperateur);
    }

 public List<RegleCommission> findByCriteria(Long uoSrcId, Long uoDestId, Long teneurSrcId, Long teneurDestId, Long typeServiceId, TypeOperateur typeOperateur, BigDecimal montant) {
        log.info("Recherche des regles de repartition de l'operation");
        return regleCommissionRepository.findByCriteria(typeServiceId, teneurSrcId, teneurDestId, uoDestId, uoSrcId, typeOperateur,montant);
    }

    /**
     * Get one regleCommission by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public RegleCommissionDTO findOne(Long id) {
        log.info("Request to get RegleCommission : {}", id);
        RegleCommission regleCommission = regleCommissionRepository.findById(id).orElse(null);
        return regleCommissionMapper.toDto(regleCommission);
    }

    /**
     * Delete the  regleCommission by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.info("Request to delete RegleCommission : {}", id);
        regleCommissionRepository.deleteById(id);
    }


    /**
     * @param teneurSrc
     * @param teneurDest
     * @param typeOperateur
     */
    public RegleCommission verifRegleCommission(Long teneurSrc, Long teneurDest, TypeOperateur typeOperateur, Long typeService, Compte compteOperation, Long uoSource, Long uoDest, BigDecimal montant) {
        List<RegleCommission> regleCommissions = findByCriteria(uoSource, uoDest, teneurSrc, teneurDest, typeService, typeOperateur,montant);
        if (regleCommissions != null && !regleCommissions.isEmpty()) {
            Optional<RegleCommission> regleCommissionExist = regleCommissions.stream().findFirst();
            RegleCommission regleEnCours;
            if (regleCommissionExist.isPresent()) {
                regleEnCours = regleCommissionExist.get();
            } else {
                regleEnCours = null;
            }
            return regleEnCours;
        }
        return null;
    }


    public RegleCommission verifRegleCommission(TypeOperateur typeOperateur, Long typeServiceId, Compte compteOperation, Long uoSourceId, Long uoDestId) {
        List<RegleCommission> regleCommissions = findByCriteria(uoSourceId, uoDestId, null, null, typeServiceId, typeOperateur);
        if (regleCommissions != null && !regleCommissions.isEmpty()) {
            Optional<RegleCommission> regleCommissionExist = regleCommissions.stream().findFirst();
            RegleCommission regleEnCours;
            if (regleCommissionExist.isPresent()) {
                regleEnCours = regleCommissionExist.get();
            } else {
                regleEnCours = null;
            }
            return regleEnCours;
        }
        return null;
    }
}
