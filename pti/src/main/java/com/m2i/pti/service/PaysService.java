package com.m2i.pti.service;

import com.m2i.pti.domain.Pays;
import com.m2i.pti.repository.PaysRepository;
import com.m2i.pti.service.dto.PaysDTO;
import com.m2i.pti.service.mapper.PaysMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing Pays.
 */
@Service
@Transactional
public class PaysService {

    private final Logger log = LoggerFactory.getLogger(PaysService.class);

    private final PaysRepository paysRepository;

    private final PaysMapper paysMapper;

    public PaysService(PaysRepository paysRepository, PaysMapper paysMapper) {
        this.paysRepository = paysRepository;
        this.paysMapper = paysMapper;
    }

    /**
     * Save a pays.
     *
     * @param paysDTO the entity to save
     * @return the persisted entity
     */
    public PaysDTO save(PaysDTO paysDTO) {
        log.debug("Request to save Pays : {}", paysDTO);
        Pays pays = paysMapper.toEntity(paysDTO);
        pays = paysRepository.save(pays);
        return paysMapper.toDto(pays);
    }

    /**
     * Get all the pays.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<PaysDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Pays");
        return paysRepository.findAll(pageable)
            .map(paysMapper::toDto);
    }

    /**
     * Get one pays by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public PaysDTO findOne(Long id) {
        log.debug("Request to get Pays : {}", id);
        Pays pays = paysRepository.findById(id).orElse(null);
        return paysMapper.toDto(pays);
    }

    /**
     * Delete the pays by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Pays : {}", id);
        paysRepository.deleteById(id);
    }
}
