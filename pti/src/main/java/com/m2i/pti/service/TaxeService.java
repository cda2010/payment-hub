package com.m2i.pti.service;


import com.m2i.pti.domain.Taxe;
import com.m2i.pti.domain.enumeration.EtiquetteCompteCode;
import com.m2i.pti.repository.TaxeRepository;
import com.m2i.pti.service.dto.TaxeDTO;
import com.m2i.pti.service.mapper.TaxeMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing Taxe.
 */
@Service
@Transactional
public class TaxeService {

    private final Logger log = LoggerFactory.getLogger(TaxeService.class);

    private TaxeRepository taxeRepository;

    private TaxeMapper taxeMapper;

    public TaxeService(TaxeRepository taxeRepository, TaxeMapper taxeMapper) {
        this.taxeRepository = taxeRepository;
        this.taxeMapper = taxeMapper;
    }

    /**
     * Save a taxe.
     *
     * @param taxeDTO the entity to save
     * @return the persisted entity
     */
    public TaxeDTO save(TaxeDTO taxeDTO) {
        log.debug("Request to save Taxe : {}", taxeDTO);
        if (taxeDTO.getId() == null) {
            verifCode(taxeDTO);
            verifNom(taxeDTO);
        } else {
            Taxe taxeExistant = taxeRepository.findById(taxeDTO.getId()).orElse(null);
            if (!taxeExistant.getCode().equals(taxeDTO.getCode())) {
                verifCode(taxeDTO);
            }
            if (!taxeExistant.getNom().equals(taxeDTO.getNom())) {
                verifNom(taxeDTO);
            }
        }
        if(taxeDTO.getId()==null)
            taxeDTO.setEtiquette(EtiquetteCompteCode.CPTETAXE.toString());
        Taxe taxe = taxeMapper.toEntity(taxeDTO);

        taxe = taxeRepository.save(taxe);
        return taxeMapper.toDto(taxe);
    }


    private void verifNom(TaxeDTO taxeDTO) {
        if (taxeRepository.existsByNom(taxeDTO.getNom())) {
            throw new CustomParameterizedException("Le nom existe déjà !");
        }
    }

    private void verifCode(TaxeDTO taxeDTO) {
        if (taxeRepository.existsByCode(taxeDTO.getCode())) {
            throw new CustomParameterizedException("Le code existe déjà !");
        }
    }

    /**
     * Get all the taxes.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<TaxeDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Taxes");
        return taxeRepository.findAll(pageable)
                .map(taxeMapper::toDto);
    }


    /**
     * Get one taxe by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public TaxeDTO findOne(Long id) {
        log.debug("Request to get Taxe : {}", id);
        return taxeMapper.toDto(taxeRepository.findById(id).orElse(null));
    }

    /**
     * Delete the taxe by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Taxe : {}", id);
        taxeRepository.deleteById(id);
    }
}
