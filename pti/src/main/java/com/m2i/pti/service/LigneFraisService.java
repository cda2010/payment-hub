package com.m2i.pti.service;

import com.m2i.pti.domain.LigneFrais;
import com.m2i.pti.repository.LigneFraisRepository;
import com.m2i.pti.service.dto.LigneFraisDTO;
import com.m2i.pti.service.mapper.LigneFraisMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Service Implementation for managing LigneFrais.
 */
@Service
@Transactional
public class LigneFraisService {

    private final Logger log = LoggerFactory.getLogger(LigneFraisService.class);

    private final LigneFraisRepository ligneFraisRepository;

    private final LigneFraisMapper ligneFraisMapper;

    public LigneFraisService(LigneFraisRepository ligneFraisRepository, LigneFraisMapper ligneFraisMapper) {
        this.ligneFraisRepository = ligneFraisRepository;
        this.ligneFraisMapper = ligneFraisMapper;
    }

    /**
     * Save a ligneFrais.
     *
     * @param ligneFraisDTO the entity to save
     * @return the persisted entity
     */
    public LigneFraisDTO save(LigneFraisDTO ligneFraisDTO) {
        Double minimun = ligneFraisDTO.getMinimun();
        Double maximum = ligneFraisDTO.getMaximun();
        Long tableauFraisId = ligneFraisDTO.getTableauFraisId();
        if (ligneFraisDTO.getId() != null) {//Verification lors d'une mise à jour
            LigneFrais ligneFraisExistant = ligneFraisRepository.findById(ligneFraisDTO.getId()).orElse(null);
            if (!ligneFraisExistant.getMinimun().equals(ligneFraisDTO.getMinimun())) {
                verifNumber(minimun, tableauFraisId);
            }
            if (!ligneFraisExistant.getMaximun().equals(ligneFraisDTO.getMaximun())) {
                verifNumber(maximum, tableauFraisId);
            }
            LigneFrais ligneFrais = ligneFraisMapper.toEntity(ligneFraisDTO);
            ligneFrais = ligneFraisRepository.save(ligneFrais);
            return ligneFraisMapper.toDto(ligneFrais);
        } else {
            verifNumber(maximum, tableauFraisId);
            verifNumber(minimun, tableauFraisId);
            LigneFrais ligneFrais = ligneFraisMapper.toEntity(ligneFraisDTO);
            ligneFrais = ligneFraisRepository.save(ligneFrais);
            return ligneFraisMapper.toDto(ligneFrais);
        }
    }

    private void verifNumber(Double minimun, Long tableauFraisId) {
        LigneFrais ligneFrais=ligneFraisRepository.findByTableauFraisIdAndMinimunLessThanEqualAndMaximunGreaterThanEqual(tableauFraisId,minimun,minimun);
        if(ligneFrais!=null){
            throw new CustomParameterizedException("Le nombre "+minimun+" n'est pas valide!");
        }
    }

    /**
     * Get all the ligneFrais.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<LigneFraisDTO> findAll(Pageable pageable) {
        log.info("Request to get all LigneFrais");
        return ligneFraisRepository.findAll(pageable)
            .map(ligneFraisMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<LigneFraisDTO> findAllByTableauFrais(Long tableauFraisId, Pageable pageable) {
        log.info("Request to get all LigneFrais");
        return ligneFraisRepository.findByTableauFraisIdOrderByMinimun(tableauFraisId, pageable)
            .map(ligneFraisMapper::toDto);
    }
    @Transactional(readOnly = true)
    public List<LigneFraisDTO> findAllByTableauFraisList(Long tableauFraisId) {
        log.info("Request to get all LigneFrais");
        return ligneFraisMapper.toDto(ligneFraisRepository.findByTableauFraisIdOrderByMinimun(tableauFraisId));
    }

    /**
     * Get one ligneFrais by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public LigneFraisDTO findOne(Long id) {
        log.info("Request to get LigneFrais : {}", id);
        LigneFrais ligneFrais = ligneFraisRepository.findById(id).orElse(null);
        return ligneFraisMapper.toDto(ligneFrais);
    }

    /**
     * Delete the ligneFrais by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.info("Request to delete LigneFrais : {}", id);
        ligneFraisRepository.deleteById(id);
    }
}
