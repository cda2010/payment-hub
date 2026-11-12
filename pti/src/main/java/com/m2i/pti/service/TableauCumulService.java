package com.m2i.pti.service;

import com.m2i.pti.domain.TableauCumul;
import com.m2i.pti.domain.enumeration.Action;
import com.m2i.pti.domain.enumeration.Temps;
import com.m2i.pti.domain.enumeration.TypeRegle;
import com.m2i.pti.repository.TableauCumulRepository;
import com.m2i.pti.service.dto.TableauCumulDTO;
import com.m2i.pti.service.mapper.TableauCumulMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service Implementation for managing TableauCumul.
 */
@Service

public class TableauCumulService {

    private final Logger log = LoggerFactory.getLogger(TableauCumulService.class);

    private final TableauCumulRepository tableauCumulRepository;

    private final TableauCumulMapper tableauCumulMapper;

    private final UniteOrganisationService uniteOrganisationService;

    public TableauCumulService(TableauCumulRepository tableauCumulRepository, TableauCumulMapper tableauCumulMapper, UniteOrganisationService uniteOrganisationService) {
        this.tableauCumulRepository = tableauCumulRepository;
        this.tableauCumulMapper = tableauCumulMapper;
        this.uniteOrganisationService = uniteOrganisationService;
    }

    /**
     * Save a tableauCumul.
     *
     * @param tableauCumulDTO the entity to save
     * @return the persisted entity
     */
    @Transactional
    public TableauCumulDTO save(TableauCumulDTO tableauCumulDTO) {
        log.debug("Request to save TableauCumul : {}", tableauCumulDTO);

        Long orgEmId = tableauCumulDTO.getOrganisationEmId();
        Long orgAcqId = tableauCumulDTO.getOrganisationAcqId();
        String poids = "";
        // Le poids correspond à la concatenation des niveaux des UO
        if (orgAcqId != null) {
            poids += uniteOrganisationService.getUONiveau(orgAcqId);
        }
        if (orgEmId != null) {
            poids += uniteOrganisationService.getUONiveau(orgEmId);
        }
        if (tableauCumulDTO.getAction() == Action.BLOQUE) {
            poids = "2" + poids;
        } else {
            poids = "1" + poids;
        }
        TableauCumul tableauCumul = tableauCumulMapper.toEntity(tableauCumulDTO);
        tableauCumul.poidsNum(!poids.equals("") ? Integer.parseInt(poids) : 0);
        tableauCumul = tableauCumulRepository.save(tableauCumul);
        return tableauCumulMapper.toDto(tableauCumul);
    }

    /**
     * Get all the tableauCumuls.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<TableauCumulDTO> findAll(Pageable pageable) {
        log.debug("Request to get all TableauCumuls");
        return tableauCumulRepository.findAll(pageable)
                .map(tableauCumulMapper::toDto);
    }

    public List<TableauCumulDTO> findAll() {
        log.debug("Request to get all TableauCumuls");
        return tableauCumulMapper.toDto(tableauCumulRepository.findAll());
    }

    public List<TableauCumulDTO> findByTypeRegle(TypeRegle typeRegle) {
        return tableauCumulMapper.toDto(tableauCumulRepository.findByTypeRegle(typeRegle));
    }

    public Page<TableauCumulDTO> findAllByCriteria(Pageable pageable, Long typeServiceId, Long canalId, Long clientBeneficiaireId, Long clientEmetteurId,
            Long uoEmId, Long uoAcqId) {
        ArrayList<Long> uoEmIds = new ArrayList<>();
        ArrayList<Long> uoAcqIds = new ArrayList<>();
        log.debug("type Service" + typeServiceId);
        if (uoEmId != null) {
            uoEmIds.add(uoEmId);
        } else {
            uoEmIds = null;
        }
        if (uoAcqId != null) {
            uoAcqIds.add(uoAcqId);
        } else {
            uoAcqIds = null;
        }
        return tableauCumulRepository.findFraisByMultiCriteria(typeServiceId, canalId, clientBeneficiaireId, clientEmetteurId, uoEmIds, uoAcqIds, pageable).map(tableauCumulMapper::toDto);
    }


    public List<TableauCumulDTO> findByCriteria(Long typeServiceId, Long canalId, Long clientBeneficiaireId, Long clientEmetteurId,
            Long uoEmetteurId, Long uoAcqId, TypeRegle typeRegle, Temps temps, Long profilId) {
        log.debug("Request to get all TableauFrais");
        if (profilId != null) {
            return tableauCumulMapper.toDto(tableauCumulRepository.findFraisByMultiParamsWithProfil(typeRegle, typeServiceId, canalId, clientBeneficiaireId, clientEmetteurId, uoEmetteurId, uoAcqId, temps, profilId));
        } else {
            return tableauCumulMapper.toDto(tableauCumulRepository.findFraisByMultiParams(typeRegle, typeServiceId, canalId, clientBeneficiaireId, clientEmetteurId, uoEmetteurId, uoAcqId, temps));
        }
    }

    /**
     * Get one tableauCumul by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    
    public TableauCumulDTO findOne(Long id) {
        log.debug("Request to get TableauCumul : {}", id);
        TableauCumul tableauCumul = tableauCumulRepository.findById(id).orElse(null);
        return tableauCumulMapper.toDto(tableauCumul);
    }

    /**
     * Delete the tableauCumul by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete TableauCumul : {}", id);
        tableauCumulRepository.deleteById(id);
    }
}
