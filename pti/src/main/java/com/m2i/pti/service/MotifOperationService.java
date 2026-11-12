package com.m2i.pti.service;

import com.m2i.pti.domain.Canal;
import com.m2i.pti.domain.MotifOperation;
import com.m2i.pti.repository.MotifOperationRepository;
import com.m2i.pti.service.dto.MotifOperationDTO;
import com.m2i.pti.service.mapper.MotifOperationMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing MotifOperation.
 */
@Service
@Transactional
public class MotifOperationService {

    private final Logger log = LoggerFactory.getLogger(MotifOperationService.class);

    private MotifOperationRepository motifOperationRepository;

    private MotifOperationMapper motifOperationMapper;
    PassiveExpiringMap<String, MotifOperationDTO> motifOperationDTOMapDTO = new PassiveExpiringMap<>(6400000);

    public MotifOperationService(MotifOperationRepository motifOperationRepository, MotifOperationMapper motifOperationMapper) {
        this.motifOperationRepository = motifOperationRepository;
        this.motifOperationMapper = motifOperationMapper;
    }

    /**
     * Save a motifOperation.
     *
     * @param motifOperationDTO the entity to save
     * @return the persisted entity
     */
    public MotifOperationDTO save(MotifOperationDTO motifOperationDTO) {
        log.info("Request to save MotifOperation : {}", motifOperationDTO);
        if (motifOperationDTO.getId() == null) {
            motifOperationDTO.setCode(getCode());
        }

        MotifOperation motifOperationByLibelle = motifOperationRepository.findByLibelle(motifOperationDTO.getLibelle());
        if (motifOperationByLibelle != null && !motifOperationByLibelle.getId().equals(motifOperationDTO.getId())) {
            throw new CustomParameterizedException("Ce motif existe déjà !");
        }
        MotifOperation motifOperation = motifOperationMapper.toEntity(motifOperationDTO);
        motifOperation = motifOperationRepository.save(motifOperation);
        return motifOperationMapper.toDto(motifOperation);
    }

    /**
     * @return
     */
    public String getCode() {
        Long max = motifOperationRepository.getMaxId();
        String code;
        if (max > 0) {
            MotifOperation last = motifOperationRepository.findById(max).orElse(null);
            Integer lastId = Integer.parseInt(last.getCode().substring(1));
            code = String.format("%03d", lastId + 1);
            code = "M" + code;
        } else {
            code = String.format("%03d", 1);
            code = "m" + code;
        }
        return code;
    }

    /**
     * Get all the motifOperations.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<MotifOperationDTO> findAll(Pageable pageable) {
        log.info("Request to get all MotifOperations");
        return motifOperationRepository.findAll(pageable)
                .map(motifOperationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public MotifOperation findByCode(String code) {
        log.info("Request to get MotifOperation : {}", code);
        MotifOperationDTO motifOperation;
        if (motifOperationDTOMapDTO.containsKey(code) && motifOperationDTOMapDTO.get(code) != null) {
            log.info("LE MOTIF EXIST DANS LA MAP");
            motifOperation = motifOperationDTOMapDTO.get(code);
        } else {

            motifOperation = motifOperationRepository.findByCodeMotif(code);
            if (motifOperation != null) {
                motifOperationDTOMapDTO.put(code, motifOperation);
            }

        }

        return motifOperationMapper.toEntity(motifOperation);
    }

    /**
     * Get one motifOperation by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public MotifOperationDTO findOne(Long id) {
        log.info("Request to get MotifOperation : {}", id);
        return motifOperationMapper.toDto(motifOperationRepository.findById(id).orElse(null));
    }

    /**
     * Delete the motifOperation by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.info("Request to delete MotifOperation : {}", id);
        motifOperationRepository.deleteById(id);
    }
}
