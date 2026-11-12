package com.m2i.pti.service;

import com.m2i.pti.domain.FournisseurService;
import com.m2i.pti.repository.FournisseurServiceRepository;
import com.m2i.pti.service.dto.FournisseurServiceDTO;
import com.m2i.pti.service.mapper.FournisseurServiceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing FournisseurService.
 */
@Service
@Transactional
public class FournisseurServiceService {

    private final Logger log = LoggerFactory.getLogger(FournisseurServiceService.class);

    private final FournisseurServiceRepository fournisseurServiceRepository;

    private final FournisseurServiceMapper fournisseurServiceMapper;

    public FournisseurServiceService(FournisseurServiceRepository fournisseurServiceRepository, FournisseurServiceMapper fournisseurServiceMapper) {
        this.fournisseurServiceRepository = fournisseurServiceRepository;
        this.fournisseurServiceMapper = fournisseurServiceMapper;
    }

    /**
     * Save a fournisseurService.
     *
     * @param fournisseurServiceDTO the entity to save
     * @return the persisted entity
     */
    public FournisseurServiceDTO save(FournisseurServiceDTO fournisseurServiceDTO) {
        FournisseurService fournisseurService = fournisseurServiceMapper.toEntity(fournisseurServiceDTO);
        fournisseurService = fournisseurServiceRepository.save(fournisseurService);
        return fournisseurServiceMapper.toDto(fournisseurService);
    }

    /**
     * Get all the fournisseurServices.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<FournisseurServiceDTO> findAll(Pageable pageable) {
        return fournisseurServiceRepository.findAll(pageable)
            .map(fournisseurServiceMapper::toDto);
    }

    /**
     * Get one fournisseurService by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public FournisseurServiceDTO findOne(Long id) {
        log.debug("Request to get FournisseurService : {}", id);
        FournisseurService fournisseurService = fournisseurServiceRepository.findById(id).orElse(null);
        return fournisseurServiceMapper.toDto(fournisseurService);
    }

    @Transactional(readOnly = true)
    public FournisseurService findByCode(String code) {
        FournisseurService fournisseurService = fournisseurServiceRepository.findByCodeFse(code);
        return fournisseurService;
    }

    /**
     * Delete the fournisseurService by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete FournisseurService : {}", id);
        fournisseurServiceRepository.deleteById(id);
    }
}
