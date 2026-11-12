package com.m2i.pti.service;

import com.m2i.pti.domain.CompteAutre;
import com.m2i.pti.repository.CompteAutreRepository;
import com.m2i.pti.service.dto.CompteAutreDTO;
import com.m2i.pti.service.mapper.CompteAutreMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing CompteAutre.
 */
@Service
@Transactional
public class CompteAutreService {

    private final Logger log = LoggerFactory.getLogger(CompteAutreService.class);

    private final CompteAutreRepository compteAutreRepository;

    private final CompteAutreMapper compteAutreMapper;

    public CompteAutreService(CompteAutreRepository compteAutreRepository, CompteAutreMapper compteAutreMapper) {
        this.compteAutreRepository = compteAutreRepository;
        this.compteAutreMapper = compteAutreMapper;
    }

    /**
     * Save a compteAutre.
     *
     * @param compteAutreDTO the entity to save
     * @param compteAutreDTO the entity to save
     * @return the persisted entity
     */
    public CompteAutreDTO save(CompteAutreDTO compteAutreDTO) {
        log.debug("Request to save CompteAutre : {}", compteAutreDTO);
        CompteAutre compteAutre = compteAutreMapper.toEntity(compteAutreDTO);
        compteAutre = compteAutreRepository.saveAndFlush(compteAutre);
        return compteAutreMapper.toDto(compteAutre);
    }

    public CompteAutre save(CompteAutre compteAutre) {
        log.debug("Request to save CompteAutre : {}", compteAutre);
      
        compteAutre = compteAutreRepository.saveAndFlush(compteAutre);
        return compteAutre;
    }
    
    /**
     * Get all the compteAutres.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<CompteAutreDTO> findAll(Pageable pageable) {
        log.debug("Request to get all CompteAutres");
        return compteAutreRepository.findAll(pageable)
            .map(compteAutreMapper::toDto);
    }

    /**
     * Get one compteAutre by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public CompteAutreDTO findOne(Long id) {
        log.debug("Request to get CompteAutre : {}", id);
        CompteAutre compteAutre = compteAutreRepository.findById(id).orElse(null);
        return compteAutreMapper.toDto(compteAutre);
    }
    
     @Transactional(readOnly = true)
    public CompteAutre findCompteAutreById(Long id) {
        log.debug("Request to get CompteAutre : {}", id);
        CompteAutre compteAutre = compteAutreRepository.findCompteAutreById(id);
        return compteAutre;
    }

    /**
     * Delete the compteAutre by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete CompteAutre : {}", id);
        compteAutreRepository.deleteById(id);
    }
}
