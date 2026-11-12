package com.m2i.pti.service;

import com.m2i.pti.domain.Canal;
import com.m2i.pti.domain.TypePiece;
import com.m2i.pti.repository.CanalRepository;
import com.m2i.pti.service.dto.CanalDTO;
import com.m2i.pti.service.mapper.CanalMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import java.util.List;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.collections4.map.PassiveExpiringMap;

/**
 * Service Implementation for managing Canal.
 */
@Service
@Transactional
public class CanalService {

    private final Logger log = LoggerFactory.getLogger(CanalService.class);

    private final CanalRepository canalRepository;

    private final CanalMapper canalMapper;
    PassiveExpiringMap<String, Canal> canalMapDTO = new PassiveExpiringMap<>(6400000);
      PassiveExpiringMap<String, CanalDTO> canalMapDTODto = new PassiveExpiringMap<>(6400000);
    public CanalService(CanalRepository canalRepository, CanalMapper canalMapper) {
        this.canalRepository = canalRepository;
        this.canalMapper = canalMapper;
    }

    /**
     * Save a canal.
     *
     * @param canalDTO the entity to save
     * @return the persisted entity
     */
    public CanalDTO save(CanalDTO canalDTO) {
        log.debug("Request to save Canal : {}", canalDTO);
        if (canalDTO.getId() == null) {
            verifCode(canalDTO);
            verifLibelle(canalDTO);

        } else {
            Canal canalExistant = canalRepository.findById(canalDTO.getId()).orElse(null);
            if (!canalExistant.getCodeCanal().equals(canalDTO.getCodeCanal())) {
                verifCode(canalDTO);
            }
            if (!canalExistant.getLibelleCanal().equals(canalDTO.getLibelleCanal())) {
                verifLibelle(canalDTO);
            }
        }
        Canal canal = canalMapper.toEntity(canalDTO);
        canal = canalRepository.save(canal);
        return canalMapper.toDto(canal);
    }

    private void verifLibelle(CanalDTO canalDTO) {
        if (canalRepository.existsByLibelleCanal(canalDTO.getLibelleCanal())) {
            throw new CustomParameterizedException("Le libelle existe déjà !");
        }
    }

    private void verifCode(CanalDTO canalDTO) {
        if (canalRepository.existsByCodeCanal(canalDTO.getCodeCanal())) {
            throw new CustomParameterizedException("Le code existe déjà !");
        }
    }

    /**
     * Get all the canals.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<CanalDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Canals");
        return canalRepository.findAll(pageable)
                .map(canalMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<Canal> findAll() {
        log.debug("Request to get all Canals");
        return canalRepository.findAll();

    }

    /**
     * Get one canal by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public CanalDTO findOne(Long id) {
        log.debug("Request to get Canal : {}", id);
        Canal canal = canalRepository.findOneWithEagerRelationships(id);
        return canalMapper.toDto(canal);
    }

    public Canal findByCodeCanal(String canalLibelle) {
        Canal canal;
             if (canalMapDTO.containsKey(canalLibelle)) {
                log.info("LE MOTIF EXIST DANS LA MAP");
                canal = canalMapDTO.get(canalLibelle);
            } else {

                canal = canalRepository.findTopByCodeCanal(canalLibelle);
                canalMapDTO.put(canalLibelle,canal);
            }
        
        return canal;
    }

    public Canal findCanal(String canalLibelle) {
         CanalDTO canal;
             if (canalMapDTODto.containsKey(canalLibelle)) {
                log.info("LE MOTIF EXIST DANS LA MAP");
                canal = canalMapDTODto.get(canalLibelle);
            } else {
                 log.info("LE CANAL"+canalLibelle);
                 canal = canalRepository.findByCodeCanal(canalLibelle);
                canalMapDTODto.put(canalLibelle,canal);
            }
        
        
        return canalMapper.toEntity(canal);

    }

    /**
     * Delete the canal by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Canal : {}", id);
        canalRepository.deleteById(id);
    }
}
