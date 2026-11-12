package com.m2i.pti.service;

import com.m2i.pti.domain.BrancheActivite;
import com.m2i.pti.repository.BrancheActiviteRepository;
import com.m2i.pti.service.dto.BrancheActiviteDTO;
import com.m2i.pti.service.mapper.BrancheActiviteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing BrancheActivite.
 */
@Service
@Transactional
public class BrancheActiviteService {

    private final Logger log = LoggerFactory.getLogger(BrancheActiviteService.class);

    private BrancheActiviteRepository brancheActiviteRepository;

    private BrancheActiviteMapper brancheActiviteMapper;

    public BrancheActiviteService(BrancheActiviteRepository brancheActiviteRepository, BrancheActiviteMapper brancheActiviteMapper) {
        this.brancheActiviteRepository = brancheActiviteRepository;
        this.brancheActiviteMapper = brancheActiviteMapper;
    }

    /**
     * Save a brancheActivite.
     *
     * @param brancheActiviteDTO the entity to save
     * @return the persisted entity
     */
    public BrancheActiviteDTO save(BrancheActiviteDTO brancheActiviteDTO) {
        log.debug("Request to save BrancheActivite : {}", brancheActiviteDTO);

        BrancheActivite brancheActivite = brancheActiviteMapper.toEntity(brancheActiviteDTO);
        brancheActivite = brancheActiviteRepository.save(brancheActivite);
        return brancheActiviteMapper.toDto(brancheActivite);
    }

    /**
     * Get all the brancheActivites.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<BrancheActiviteDTO> findAll(Pageable pageable) {
        log.debug("Request to get all BrancheActivites");
        return brancheActiviteRepository.findAll(pageable)
            .map(brancheActiviteMapper::toDto);
    }


    /**
     * Get one brancheActivite by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public BrancheActiviteDTO findOne(Long id) {
        log.debug("Request to get Beneficiaire : {}", id);
        BrancheActivite brancheActivite = brancheActiviteRepository.findById(id).orElse(null);
        return brancheActiviteMapper.toDto(brancheActivite);
    }

    /**
     * Delete the brancheActivite by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete BrancheActivite : {}", id);
        brancheActiviteRepository.deleteById(id);
    }
}
