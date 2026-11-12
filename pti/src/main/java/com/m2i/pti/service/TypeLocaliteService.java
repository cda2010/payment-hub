package com.m2i.pti.service;

import com.m2i.pti.domain.TypeLocalite;
import com.m2i.pti.repository.TypeLocaliteRepository;
import com.m2i.pti.service.dto.TypeLocaliteDTO;
import com.m2i.pti.service.mapper.TypeLocaliteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing TypeLocalite.
 */
@Service
@Transactional
public class TypeLocaliteService {

    private final Logger log = LoggerFactory.getLogger(TypeLocaliteService.class);

    private final TypeLocaliteRepository typeLocaliteRepository;

    private final TypeLocaliteMapper typeLocaliteMapper;

    public TypeLocaliteService(TypeLocaliteRepository typeLocaliteRepository, TypeLocaliteMapper typeLocaliteMapper) {
        this.typeLocaliteRepository = typeLocaliteRepository;
        this.typeLocaliteMapper = typeLocaliteMapper;
    }

    /**
     * Save a typeLocalite.
     *
     * @param typeLocaliteDTO the entity to save
     * @return the persisted entity
     */
    public TypeLocaliteDTO save(TypeLocaliteDTO typeLocaliteDTO) {
        log.debug("Request to save TypeLocalite : {}", typeLocaliteDTO);
        TypeLocalite typeLocalite = typeLocaliteMapper.toEntity(typeLocaliteDTO);
        typeLocalite = typeLocaliteRepository.save(typeLocalite);
        return typeLocaliteMapper.toDto(typeLocalite);
    }

    /**
     *  Get all the typeLocalites.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<TypeLocaliteDTO> findAll(Pageable pageable) {
        log.debug("Request to get all TypeLocalites");
        return typeLocaliteRepository.findAll(pageable)
            .map(typeLocaliteMapper::toDto);
    }

    /**
     *  Get one typeLocalite by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public TypeLocaliteDTO findOne(Long id) {
        log.debug("Request to get TypeLocalite : {}", id);
        TypeLocalite typeLocalite = typeLocaliteRepository.findById(id).orElse(null);
        return typeLocaliteMapper.toDto(typeLocalite);
    }

    /**
     *  Delete the  typeLocalite by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete TypeLocalite : {}", id);
        typeLocaliteRepository.deleteById(id);
    }
}
