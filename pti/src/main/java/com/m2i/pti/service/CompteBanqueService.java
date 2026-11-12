package com.m2i.pti.service;

import com.m2i.pti.domain.CompteBanque;
import com.m2i.pti.domain.enumeration.EtiquetteCompteCode;
import com.m2i.pti.repository.CompteBanqueRepository;
import com.m2i.pti.service.dto.CompteBanqueDTO;
import com.m2i.pti.service.mapper.CompteBanqueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Service Implementation for managing CompteBanque.
 */
@Service
@Transactional
public class CompteBanqueService {

    private final Logger log = LoggerFactory.getLogger(CompteBanqueService.class);

    private final CompteBanqueRepository compteBanqueRepository;

    private final CompteBanqueMapper compteBanqueMapper;

    public CompteBanqueService(CompteBanqueRepository compteBanqueRepository, CompteBanqueMapper compteBanqueMapper) {
        this.compteBanqueRepository = compteBanqueRepository;
        this.compteBanqueMapper = compteBanqueMapper;
    }

    /**
     * Save a compteBanque.
     *
     * @param compteBanqueDTO the entity to save
     * @return the persisted entity
     */
    public CompteBanqueDTO save(CompteBanqueDTO compteBanqueDTO) {
        log.info("Request to save CompteBanque : {}", compteBanqueDTO);
        CompteBanque compteBanque = compteBanqueMapper.toEntity(compteBanqueDTO);
        compteBanque = compteBanqueRepository.save(compteBanque);
        return compteBanqueMapper.toDto(compteBanque);
    }

    /**
     * Get all the compteBanques.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<CompteBanqueDTO> findAll(Pageable pageable) {
        log.info("Request to get all CompteBanques");
        return compteBanqueRepository.findAll(pageable)
                .map(compteBanqueMapper::toDto);
    }

    public List<CompteBanque> findByCriteria(String codeBanque, String codeAgence, String rib, String valeurCompte) {
        return compteBanqueRepository.finbyCriteria(codeAgence, codeBanque, rib, valeurCompte, EtiquetteCompteCode.CPTECLIENT.toString());
    }

    public List<CompteBanque> findByCriteriaStrict(String codeBanque, String codeAgence, String rib, String valeurCompte) {
        return compteBanqueRepository.finbyCriteria(codeAgence, codeBanque, rib, valeurCompte, EtiquetteCompteCode.CPTECLIENT.toString());
    }

    /**
     * Get one compteBanque by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public CompteBanqueDTO findOne(Long id) {
        log.info("Request to get CompteBanque : {}", id);
        CompteBanque compteBanque = compteBanqueRepository.findById(id).orElse(null);
        return compteBanqueMapper.toDto(compteBanque);
    }
    

    public CompteBanque findByCompteId(Long id) {
        log.info("Request to get CompteBanque : {}", id);
        CompteBanque compteBanque = compteBanqueRepository.findByCompteId(id);
        return compteBanque;
    }

    /**
     * Delete the compteBanque by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.info("Request to delete CompteBanque : {}", id);
        compteBanqueRepository.deleteById(id);
    }
}
