package com.m2i.pti.service;

import com.m2i.pti.domain.Devise;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.enumeration.TypeUO;
import com.m2i.pti.repository.DeviseRepository;
import com.m2i.pti.service.dto.DeviseDTO;
import com.m2i.pti.service.mapper.DeviseMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


/**
 * Service Implementation for managing Devise.
 */
@Service
@Transactional
public class DeviseService {

    private final Logger log = LoggerFactory.getLogger(DeviseService.class);

    private final DeviseRepository deviseRepository;

    private final DeviseMapper deviseMapper;

    public DeviseService(DeviseRepository deviseRepository, DeviseMapper deviseMapper) {
        this.deviseRepository = deviseRepository;
        this.deviseMapper = deviseMapper;
    }

    /**
     * Save a devise.
     *
     * @param deviseDTO the entity to save
     * @return the persisted entity
     */
    public DeviseDTO save(DeviseDTO deviseDTO) {
        log.debug("Request to save Devise : {}", deviseDTO);

        Optional<Devise> deviseExistWithCode = deviseRepository.findByCode(deviseDTO.getCode());
        if (deviseExistWithCode.isPresent() && (!deviseExistWithCode.get().getId().equals(deviseDTO.getId()))) {
            throw new CustomParameterizedException("Le code est déjà utilisé !");
        }
        Optional<Devise> deviseExistWithLibelle = deviseRepository.findByLibelle(deviseDTO.getLibelle());
        if (deviseExistWithLibelle.isPresent() && !deviseExistWithLibelle.get().getId().equals(deviseDTO.getId())) {
            throw new CustomParameterizedException("Le libelle est déjà utilisé !");
        }
        Devise devise = deviseMapper.toEntity(deviseDTO);
        devise = deviseRepository.save(devise);
        return deviseMapper.toDto(devise);
    }

    /**
     * Get all the devises.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<DeviseDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Devises");
        return deviseRepository.findAll(pageable)
            .map(deviseMapper::toDto);
    }

    /**
     * Get one devise by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public DeviseDTO findOne(Long id) {
        log.debug("Request to get Devise : {}", id);
        Devise devise = deviseRepository.findById(id).orElse(null);
        return deviseMapper.toDto(devise);
    }

    /**
     * Delete the devise by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Devise : {}", id);
        deviseRepository.deleteById(id);
    }

    @Transactional
    public UniteOrganisation findAgenceUOByType(UniteOrganisation uniteOrganisation, TypeUO typeUO) {
        if (uniteOrganisation.getTypeUO().getLibelleTypeUO().equals(String.valueOf(typeUO))) {
            return uniteOrganisation;
        }
        if (uniteOrganisation.getPereUO() == null) return null;
        else return findAgenceUOByType(uniteOrganisation.getPereUO(), typeUO);
    }

    public Devise findByCode(String code) {
        Optional<Devise> devise = deviseRepository.findByCode(code);
        if (devise.isPresent())
       return devise.get();
        else
            return null;
    }
}
