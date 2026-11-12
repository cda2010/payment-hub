package com.m2i.pti.service;

import com.m2i.pti.domain.Nationalite;
import com.m2i.pti.repository.NationaliteRepository;
import com.m2i.pti.service.dto.NationaliteDTO;
import com.m2i.pti.service.mapper.NationaliteMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


/**
 * Service Implementation for managing Nationalite.
 */
@Service
@Transactional
public class NationaliteService {

    private final Logger log = LoggerFactory.getLogger(NationaliteService.class);

    private final NationaliteRepository nationaliteRepository;

    private final NationaliteMapper nationaliteMapper;

    public NationaliteService(NationaliteRepository nationaliteRepository, NationaliteMapper nationaliteMapper) {
        this.nationaliteRepository = nationaliteRepository;
        this.nationaliteMapper = nationaliteMapper;
    }

    /**
     * Save a nationalite.
     *
     * @param nationaliteDTO the entity to save
     * @return the persisted entity
     */
    public NationaliteDTO save(NationaliteDTO nationaliteDTO) {
        log.info("Request to save Nationalite : {}", nationaliteDTO);
        Optional<Nationalite> nationaliteExistWithCode = nationaliteRepository.findByCode(nationaliteDTO.getCode());
        if (nationaliteExistWithCode.isPresent() && (!nationaliteExistWithCode.get().getId().equals(nationaliteDTO.getId()))) {
            throw new CustomParameterizedException("Le code est déjà utilisé !");
        }

        Optional<Nationalite> typeCompteExistWithLibelle = nationaliteRepository.findByLibelle(nationaliteDTO.getLibelle());
        if (typeCompteExistWithLibelle.isPresent() && !typeCompteExistWithLibelle.get().getId().equals(nationaliteDTO.getId())) {
            throw new CustomParameterizedException("Le libelle est déjà utilisé !");
        }

        Nationalite nationalite = nationaliteMapper.toEntity(nationaliteDTO);
        nationalite = nationaliteRepository.save(nationalite);
        return nationaliteMapper.toDto(nationalite);
    }

    /**
     * Get all the nationalites.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<NationaliteDTO> findAll(Pageable pageable) {
        log.info("Request to get all Nationalites");
        return nationaliteRepository.findAll(pageable)
            .map(nationaliteMapper::toDto);
    }

    /**
     * Get one nationalite by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public NationaliteDTO findOne(Long id) {
        log.info("Request to get Nationalite : {}", id);
        Nationalite nationalite = nationaliteRepository.findById(id).orElse(null);
        return nationaliteMapper.toDto(nationalite);
    }

    /**
     * Delete the nationalite by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.info("Request to delete Nationalite : {}", id);
        nationaliteRepository.deleteById(id);
    }
    @Transactional(readOnly = true)
    public Nationalite getByCode(String code) {
        return nationaliteRepository.findNationaliteByCode(code);
    }

    @Transactional(readOnly = true)
    public String getLibelleById(Long id){
        return nationaliteRepository.getLibelleById(id);
    }

    public List<Nationalite> findAllNationalite() {
        return nationaliteRepository.findAllNationalite();
    }


}
