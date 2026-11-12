package com.m2i.pti.service;

import com.m2i.pti.domain.TableauFrais;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.repository.TableauFraisRepository;
import com.m2i.pti.repository.UniteOrganisationRepository;
import com.m2i.pti.service.dto.TableauFraisDTO;
import com.m2i.pti.service.mapper.TableauFraisMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Service Implementation for managing TableauFrais.
 */
@Service
@Transactional
public class TableauFraisService {
    private final Logger log = LoggerFactory.getLogger(TableauFraisService.class);
    private final TableauFraisRepository tableauFraisRepository;
    private final TableauFraisMapper tableauFraisMapper;
    private final UniteOrganisationService uniteOrganisationService;
    private final UniteOrganisationRepository uniteOrganisationRepository;
    public TableauFraisService(TableauFraisRepository tableauFraisRepository, TableauFraisMapper tableauFraisMapper, UniteOrganisationService uniteOrganisationService, UniteOrganisationRepository uniteOrganisationRepository) {
        this.tableauFraisRepository = tableauFraisRepository;
        this.tableauFraisMapper = tableauFraisMapper;
        this.uniteOrganisationService = uniteOrganisationService;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
    }

    /**
     * Save a tableauFrais.
     *
     * @param tableauFraisDTO the entity to save
     * @return the persisted entity
     */
    public TableauFraisDTO save(TableauFraisDTO tableauFraisDTO) {
        log.debug("Enregistrement d'un paramètre de tableauFrais", tableauFraisDTO);
      //  verifBeforeSave(tableauFraisDTO);
        Long orgEmId = tableauFraisDTO.getOrganisationEmId();
        Long orgAcqId = tableauFraisDTO.getOrganisationAcqId();
        String poids = "";
        // Le poids correspond à la concatenation des niveaux des UO
        if (orgEmId != null) {
            poids += uniteOrganisationService.getUONiveau(orgEmId);
        }

        if (orgAcqId != null) {
            poids += uniteOrganisationService.getUONiveau(orgAcqId);
        }

        TableauFrais tableauFrais = tableauFraisMapper.toEntity(tableauFraisDTO);
        tableauFrais.poidsNum(!poids.equals("") ? Integer.parseInt(poids) : 0);
        tableauFrais = tableauFraisRepository.save(tableauFrais);
        return tableauFraisMapper.toDto(tableauFrais);
    }

    /**
     * Verification de l'existence du paramétrage en cours d'enregistrement
     * @param tableauFraisDTO
     */
    public void verifBeforeSave(TableauFraisDTO tableauFraisDTO) {
        if (tableauFraisDTO.getId() == null) {
            if (!tableauFraisRepository.findFraisByMultiParams(tableauFraisDTO.getTypeServiceId(),
                    tableauFraisDTO.getCanalId(),
                    tableauFraisDTO.getBeneficiaireId(), tableauFraisDTO.getEmetteurId(),
                    Arrays.asList(tableauFraisDTO.getOrganisationEmId()),
                    Arrays.asList(tableauFraisDTO.getOrganisationAcqId())).isEmpty()) {
                throw new CustomParameterizedException("Ce parametrage existe dejà");
            }
        }
    }

    /**
     * Deduire le tableau de frais
     *
     * @param typeServiceId
     * @param canalId
     * @param clientBeneficiaireId
     * @param clientEmetteurId
     * @param uoEmId
     * @param uoAcqId
     * @return
     */
    public List<TableauFraisDTO> deduireFrais(Long typeServiceId, Long canalId, Long clientBeneficiaireId, Long clientEmetteurId,
                                              Long uoEmId, Long uoAcqId, Long profilId) {
        ArrayList<Long> uoEmIds = new ArrayList<>();
        ArrayList<Long> uoAcqIds = new ArrayList<>();
        getListUoId(uoEmId, uoEmIds);//Liste des uos de la chaine emetteur
        getListUoId(uoAcqId, uoAcqIds);//Liste des uos de la chaine acquereur
        if (uoAcqIds.isEmpty()) uoAcqIds.add(-1L);
        if (uoEmIds.isEmpty()) uoEmIds.add(-1L);
        log.debug("Parametres");
        log.debug("TYPESERVICE",typeServiceId);
        log.debug("PROFIL ID",profilId);
        return tableauFraisMapper.toDto(tableauFraisRepository.findFraisByMultiParamsWithProfil(typeServiceId, canalId, clientBeneficiaireId, clientEmetteurId, uoEmIds, uoAcqIds, profilId));
    }

    private void getListUoId(Long uoId, ArrayList<Long> uoEmIds) {
        if (uoId != null) {
            log.debug("Informations sur la liste les uos de la chaine");
            UniteOrganisation uoEm = uniteOrganisationService.findOneById(uoId);
            uniteOrganisationService.findAllUoParentsIds(uoEm, uoEmIds);
        }
    }

    /**
     * Get all the tableauFrais.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<TableauFraisDTO> findAll(Pageable pageable) {
        log.debug("Request to get all TableauFrais");
        return tableauFraisRepository.findAll(pageable)
                .map(tableauFraisMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<TableauFraisDTO> findAll() {
        log.debug("Request to get all TableauFrais");
        return tableauFraisMapper.toDto(tableauFraisRepository.findAll());
    }


    public Page<TableauFraisDTO> findAllByCriteria(Pageable pageable, Long typeServiceId, Long canalId, Long clientBeneficiaireId, Long clientEmetteurId,
                                                   Long uoEmId, Long uoAcqId) {
        ArrayList<Long> uoEmIds = new ArrayList<>();
        ArrayList<Long> uoAcqIds = new ArrayList<>();
        if (uoEmId != null) {
            uoEmIds.add(uoEmId);
        } else uoEmIds = null;
        if (uoAcqId != null) {
            uoAcqIds.add(uoAcqId);
        } else uoAcqIds = null;
        return tableauFraisRepository.findFraisByMultiCriteria(typeServiceId, canalId, clientBeneficiaireId, clientEmetteurId, uoEmIds, uoAcqIds, pageable).map(tableauFraisMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<TableauFraisDTO> findAllByMultiC(Long typeServiceId, Long canalId, Long clientBeneficiaireId, Long clientEmetteurId,
                                                 List<Long> uoEmetteurIds, List<Long> uOAcqIds) {
        log.debug("Request to get all TableauFrais");
        return tableauFraisMapper.toDto(tableauFraisRepository.findFraisByMultiParams(typeServiceId, canalId, clientBeneficiaireId, clientEmetteurId, uoEmetteurIds, uOAcqIds));
    }

    /**
     * Get one tableauFrais by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public TableauFraisDTO findOne(Long id) {
        log.debug("Request to get TableauFrais : {}", id);
        TableauFrais tableauFrais = tableauFraisRepository.findById(id).orElse(null);
        return tableauFraisMapper.toDto(tableauFrais);
    }

    /**
     * Delete the tableauFrais by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete TableauFrais : {}", id);
        tableauFraisRepository.deleteById(id);
    }
}
