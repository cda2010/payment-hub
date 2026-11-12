package com.m2i.pti.service;

import com.m2i.pti.domain.Emetteur;
import com.m2i.pti.repository.EmetteurRepository;
import com.m2i.pti.service.dto.EmetteurDTO;
import com.m2i.pti.service.mapper.EmetteurMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing Emetteur.
 */
@Service
@Transactional
public class EmetteurService {

    private final Logger log = LoggerFactory.getLogger(EmetteurService.class);

    private final EmetteurRepository emetteurRepository;

    private final EmetteurMapper emetteurMapper;

    public EmetteurService(EmetteurRepository emetteurRepository, EmetteurMapper emetteurMapper) {
        this.emetteurRepository = emetteurRepository;
        this.emetteurMapper = emetteurMapper;
    }

    /**
     * Save a emetteur.
     *
     * @param emetteurDTO the entity to save
     * @return the persisted entity
     */
    public EmetteurDTO save(EmetteurDTO emetteurDTO) {
        log.debug("Request to save Emetteur : {}", emetteurDTO);
        Emetteur emetteur = emetteurMapper.toEntity(emetteurDTO);
        emetteur = emetteurRepository.save(emetteur);
        return emetteurMapper.toDto(emetteur);
    }

    /**
     * Get all the emetteurs.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<EmetteurDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Emetteurs");
        return emetteurRepository.findAll(pageable)
            .map(emetteurMapper::toDto);
    }

    /**
     * Get one emetteur by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public EmetteurDTO findOne(Long id) {
        log.debug("Request to get Emetteur : {}", id);
        Emetteur emetteur = emetteurRepository.findById(id).orElse(null);
        return emetteurMapper.toDto(emetteur);
    }

    /**
     * Delete the emetteur by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Emetteur : {}", id);
        emetteurRepository.deleteById(id);
    }
}
