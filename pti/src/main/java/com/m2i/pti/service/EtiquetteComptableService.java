package com.m2i.pti.service;

import com.m2i.pti.domain.EtiquetteComptable;
import com.m2i.pti.repository.EtiquetteComptableRepository;
import com.m2i.pti.service.dto.EtiquetteComptableDTO;
import com.m2i.pti.service.mapper.EtiquetteComptableMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing EtiquetteComptable.
 */
@Service
@Transactional
public class EtiquetteComptableService {

    private final Logger log = LoggerFactory.getLogger(EtiquetteComptableService.class);

    private final EtiquetteComptableRepository etiquetteComptableRepository;

    private final EtiquetteComptableMapper etiquetteComptableMapper;

    public EtiquetteComptableService(EtiquetteComptableRepository etiquetteComptableRepository, EtiquetteComptableMapper etiquetteComptableMapper) {
        this.etiquetteComptableRepository = etiquetteComptableRepository;
        this.etiquetteComptableMapper = etiquetteComptableMapper;
    }

    /**
     * Save a etiquetteComptable.
     *
     * @param etiquetteComptableDTO the entity to save
     * @return the persisted entity
     */
    public EtiquetteComptableDTO save(EtiquetteComptableDTO etiquetteComptableDTO) {
        log.debug("Request to save EtiquetteComptable : {}", etiquetteComptableDTO);
        EtiquetteComptable etiquetteComptable = etiquetteComptableMapper.toEntity(etiquetteComptableDTO);
        etiquetteComptable = etiquetteComptableRepository.save(etiquetteComptable);
        return etiquetteComptableMapper.toDto(etiquetteComptable);
    }

    /**
     * Get all the etiquetteComptables.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<EtiquetteComptableDTO> findAll(Pageable pageable) {
        log.debug("Request to get all EtiquetteComptables");
        return etiquetteComptableRepository.findAll(pageable)
            .map(etiquetteComptableMapper::toDto);
    }

    /**
     * Get one etiquetteComptable by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public EtiquetteComptableDTO findOne(Long id) {
        log.debug("Request to get EtiquetteComptable : {}", id);
        EtiquetteComptable etiquetteComptable = etiquetteComptableRepository.findById(id).orElse(null);
        return etiquetteComptableMapper.toDto(etiquetteComptable);
    }

    /**
     * Delete the etiquetteComptable by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete EtiquetteComptable : {}", id);
        etiquetteComptableRepository.deleteById(id);
    }
}
