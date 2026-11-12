package com.m2i.pti.service;

import com.m2i.pti.domain.ProduitCanal;
import com.m2i.pti.repository.ProduitCanalRepository;
import com.m2i.pti.service.dto.ProduitCanalDTO;
import com.m2i.pti.service.mapper.ProduitCanalMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing ProduitCanal.
 */
@Service
@Transactional
public class ProduitCanalService {

    private final Logger log = LoggerFactory.getLogger(ProduitCanalService.class);

    private final ProduitCanalRepository produitCanalRepository;

    private final ProduitCanalMapper produitCanalMapper;

    public ProduitCanalService(ProduitCanalRepository produitCanalRepository, ProduitCanalMapper produitCanalMapper) {
        this.produitCanalRepository = produitCanalRepository;
        this.produitCanalMapper = produitCanalMapper;
    }

    /**
     * Save a produitCanal.
     *
     * @param produitCanalDTO the entity to save
     * @return the persisted entity
     */
    public ProduitCanalDTO save(ProduitCanalDTO produitCanalDTO) {
        log.debug("Request to save ProduitCanal : {}", produitCanalDTO);
        ProduitCanal produitCanal = produitCanalMapper.toEntity(produitCanalDTO);
        produitCanal = produitCanalRepository.save(produitCanal);
        return produitCanalMapper.toDto(produitCanal);
    }

    /**
     *  Get all the produitCanals.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<ProduitCanalDTO> findAll(Pageable pageable) {
        log.debug("Request to get all ProduitCanals");
        return produitCanalRepository.findAll(pageable)
            .map(produitCanalMapper::toDto);
    }

    /**
     *  Get one produitCanal by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public ProduitCanalDTO findOne(Long id) {
        log.debug("Request to get ProduitCanal : {}", id);
        ProduitCanal produitCanal = produitCanalRepository.findById(id).orElse(null);
        return produitCanalMapper.toDto(produitCanal);
    }

    /**
     *  Delete the  produitCanal by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete ProduitCanal : {}", id);
        produitCanalRepository.deleteById(id);
    }
}
