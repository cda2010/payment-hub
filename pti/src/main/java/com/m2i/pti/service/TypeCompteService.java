package com.m2i.pti.service;

import com.m2i.pti.domain.TypeCompte;
import com.m2i.pti.repository.TypeCompteRepository;
import com.m2i.pti.service.dto.TypeCompteDTO;
import com.m2i.pti.service.mapper.TypeCompteMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


/**
 * Service Implementation for managing TypeCompte.
 */
@Service
@Transactional
public class TypeCompteService {

    private final Logger log = LoggerFactory.getLogger(TypeCompteService.class);

    private final TypeCompteRepository typeCompteRepository;

    private final TypeCompteMapper typeCompteMapper;

    public TypeCompteService(TypeCompteRepository typeCompteRepository, TypeCompteMapper typeCompteMapper) {
        this.typeCompteRepository = typeCompteRepository;
        this.typeCompteMapper = typeCompteMapper;
    }

    /**
     * Save a typeCompte.
     *
     * @param typeCompteDTO the entity to save
     * @return the persisted entity
     */
    public TypeCompteDTO save(TypeCompteDTO typeCompteDTO) {

        Optional<TypeCompte> typeCompteExistWithCode = typeCompteRepository.findByCodeTypeCompte(typeCompteDTO.getCodeTypeCompte());
        if (typeCompteExistWithCode.isPresent() && (!typeCompteExistWithCode.get().getId().equals(typeCompteDTO.getId()))) {
            throw new CustomParameterizedException("Le code est déjà utilisé !");
        }

        Optional<TypeCompte> typeCompteExistWithLibelle = typeCompteRepository.findByLibelleTypeCompte(typeCompteDTO.getLibelleTypeCompte());
        if (typeCompteExistWithLibelle.isPresent() && !typeCompteExistWithLibelle.get().getId().equals(typeCompteDTO.getId())) {
            throw new CustomParameterizedException("Le libelle est déjà utilisé !");
        }

        log.debug("Request to save TypeCompte : {}", typeCompteDTO);
        TypeCompte typeCompte = typeCompteMapper.toEntity(typeCompteDTO);
        typeCompte = typeCompteRepository.save(typeCompte);
        return typeCompteMapper.toDto(typeCompte);
    }

    /**
     * Get all the typeComptes.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<TypeCompteDTO> findAll(Pageable pageable) {
        log.debug("Request to get all TypeComptes");
        return typeCompteRepository.findAll(pageable)
            .map(typeCompteMapper::toDto);
    }

    /**
     * Get one typeCompte by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public TypeCompteDTO findOne(Long id) {
        log.debug("Request to get TypeCompte : {}", id);
        TypeCompte typeCompte = typeCompteRepository.findById(id).orElse(null);
        return typeCompteMapper.toDto(typeCompte);
    }

    /**
     * Delete the typeCompte by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete TypeCompte : {}", id);
        typeCompteRepository.deleteById(id);
    }
}
