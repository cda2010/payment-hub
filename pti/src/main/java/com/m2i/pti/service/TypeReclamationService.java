package com.m2i.pti.service;

import com.m2i.pti.service.TypeReclamationService;
import com.m2i.pti.domain.TypeReclamation;
import com.m2i.pti.repository.TypeReclamationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link TypeReclamation}.
 */
@Service
@Transactional
public class TypeReclamationService {

    private final Logger log = LoggerFactory.getLogger(TypeReclamationService.class);

    private final TypeReclamationRepository typeReclamationRepository;

    public TypeReclamationService(TypeReclamationRepository typeReclamationRepository) {
        this.typeReclamationRepository = typeReclamationRepository;
    }

    /**
     * Save a typeReclamation.
     *
     * @param typeReclamation the entity to save.
     * @return the persisted entity.
     */
    public TypeReclamation save(TypeReclamation typeReclamation) {
        log.debug("Request to save TypeReclamation : {}", typeReclamation);
        return typeReclamationRepository.save(typeReclamation);
    }

    /**
     * Get all the typeReclamations.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<TypeReclamation> findAll() {
        log.debug("Request to get all TypeReclamations");
        return typeReclamationRepository.findAll();
    }

    /**
     * Get one typeReclamation by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<TypeReclamation> findOne(Long id) {
        log.debug("Request to get TypeReclamation : {}", id);
        return typeReclamationRepository.findById(id);
    }

    /**
     * Delete the typeReclamation by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete TypeReclamation : {}", id);
        typeReclamationRepository.deleteById(id);
    }
}
