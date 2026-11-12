package com.m2i.pti.service;

import com.m2i.pti.domain.RepartionFraisEmAcq;
import com.m2i.pti.repository.RepartionFraisEmAcqRepository;
import com.m2i.pti.service.dto.RepartionFraisEmAcqDTO;
import com.m2i.pti.service.mapper.RepartionFraisEmAcqMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


/**
 * Service Implementation for managing RepartionFraisEmAcq.
 */
@Service
@Transactional
public class RepartionFraisEmAcqService {

    private final Logger log = LoggerFactory.getLogger(RepartionFraisEmAcqService.class);

    private final RepartionFraisEmAcqRepository repartionFraisEmAcqRepository;

    private final RepartionFraisEmAcqMapper repartionFraisEmAcqMapper;

    public RepartionFraisEmAcqService(RepartionFraisEmAcqRepository repartionFraisEmAcqRepository, RepartionFraisEmAcqMapper repartionFraisEmAcqMapper) {
        this.repartionFraisEmAcqRepository = repartionFraisEmAcqRepository;
        this.repartionFraisEmAcqMapper = repartionFraisEmAcqMapper;
    }

    /**
     * Save a repartionFraisEmAcq.
     *
     * @param repartionFraisEmAcqDTO the entity to save
     * @return the persisted entity
     */
    public RepartionFraisEmAcqDTO save(RepartionFraisEmAcqDTO repartionFraisEmAcqDTO) {
        log.debug("Request to save RepartionFraisEmAcq : {}", repartionFraisEmAcqDTO);
        Optional<RepartionFraisEmAcq> repartionFraisEmAcqExistWithTypeService = repartionFraisEmAcqRepository.findByTypeServiceId(repartionFraisEmAcqDTO.getTypeServiceId());
        if (repartionFraisEmAcqExistWithTypeService.isPresent() && (!repartionFraisEmAcqExistWithTypeService.get().getId().equals(repartionFraisEmAcqDTO.getId()))) {
            throw new CustomParameterizedException("Le parametrage pour ce type de service existe dejà !");
        } else {
            RepartionFraisEmAcq repartionFraisEmAcq = repartionFraisEmAcqMapper.toEntity(repartionFraisEmAcqDTO);
            repartionFraisEmAcq = repartionFraisEmAcqRepository.save(repartionFraisEmAcq);
            return repartionFraisEmAcqMapper.toDto(repartionFraisEmAcq);
        }
    }

    /**
     * Get all the repartionFraisEmAcqs.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<RepartionFraisEmAcqDTO> findAll(Pageable pageable) {
        log.debug("Request to get all RepartionFraisEmAcqs");
        return repartionFraisEmAcqRepository.findAll(pageable)
                .map(repartionFraisEmAcqMapper::toDto);
    }

    /**
     * Get one repartionFraisEmAcq by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public RepartionFraisEmAcqDTO findOne(Long id) {
        log.debug("Request to get RepartionFraisEmAcq : {}", id);
        RepartionFraisEmAcq repartionFraisEmAcq = repartionFraisEmAcqRepository.findById(id).orElse(null);
        return repartionFraisEmAcqMapper.toDto(repartionFraisEmAcq);
    }

    /**
     * Delete the repartionFraisEmAcq by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete RepartionFraisEmAcq : {}", id);
        repartionFraisEmAcqRepository.deleteById(id);
    }
}
