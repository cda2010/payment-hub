package com.m2i.pti.service;

import com.m2i.pti.domain.Beneficiaire;
import com.m2i.pti.repository.BeneficiaireRepository;
import com.m2i.pti.service.dto.BeneficiaireDTO;
import com.m2i.pti.service.mapper.BeneficiaireMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing Beneficiaire.
 */
@Service
@Transactional
public class BeneficiaireService {

    private final Logger log = LoggerFactory.getLogger(BeneficiaireService.class);

    private final BeneficiaireRepository beneficiaireRepository;

    private final BeneficiaireMapper beneficiaireMapper;

    public BeneficiaireService(BeneficiaireRepository beneficiaireRepository, BeneficiaireMapper beneficiaireMapper) {
        this.beneficiaireRepository = beneficiaireRepository;
        this.beneficiaireMapper = beneficiaireMapper;
    }

    /**
     * Save a beneficiaire.
     *
     * @param beneficiaireDTO the entity to save
     * @return the persisted entity
     */
    public BeneficiaireDTO save(BeneficiaireDTO beneficiaireDTO) {
        log.debug("Request to save Beneficiaire : {}", beneficiaireDTO);
        Beneficiaire beneficiaire = beneficiaireMapper.toEntity(beneficiaireDTO);
        beneficiaire = beneficiaireRepository.save(beneficiaire);
        return beneficiaireMapper.toDto(beneficiaire);
    }

    /**
     * Get all the beneficiaires.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<BeneficiaireDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Beneficiaires");
        return beneficiaireRepository.findAll(pageable)
            .map(beneficiaireMapper::toDto);
    }

    /**
     * Get one beneficiaire by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public BeneficiaireDTO findOne(Long id) {
        log.debug("Request to get Beneficiaire : {}", id);
        Beneficiaire beneficiaire = beneficiaireRepository.findById(id).orElse(null);
        return beneficiaireMapper.toDto(beneficiaire);
    }

    /**
     * Delete the beneficiaire by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Beneficiaire : {}", id);
        beneficiaireRepository.deleteById(id);
    }
}
