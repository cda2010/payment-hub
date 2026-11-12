package com.m2i.pti.service;

import com.m2i.pti.domain.Localite;
import com.m2i.pti.repository.LocaliteRepository;
import com.m2i.pti.service.dto.LocaliteDTO;
import com.m2i.pti.service.mapper.LocaliteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing Localite.
 */
@Service
@Transactional
public class LocaliteService {

    private final Logger log = LoggerFactory.getLogger(LocaliteService.class);

    private final LocaliteRepository localiteRepository;

    private final LocaliteMapper localiteMapper;

    public LocaliteService(LocaliteRepository localiteRepository, LocaliteMapper localiteMapper) {
        this.localiteRepository = localiteRepository;
        this.localiteMapper = localiteMapper;
    }

    /**
     * Save a localite.
     *
     * @param localiteDTO the entity to save
     * @return the persisted entity
     */
    public LocaliteDTO save(LocaliteDTO localiteDTO) {
        log.debug("Request to save Localite : {}", localiteDTO);
        Localite localite = localiteMapper.toEntity(localiteDTO);
        localite = localiteRepository.save(localite);
        return localiteMapper.toDto(localite);
    }

    /**
     *  Get all the localites.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<LocaliteDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Localites");
        return localiteRepository.findAll(pageable)
            .map(localiteMapper::toDto);
    }

    /**
     *  Get one localite by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public LocaliteDTO findOne(Long id) {
        log.debug("Request to get Localite : {}", id);
        Localite localite = localiteRepository.findById(id).orElse(null);
        return localiteMapper.toDto(localite);
    }

    /**
     *  Delete the  localite by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Localite : {}", id);
        localiteRepository.deleteById(id);
    }
}
