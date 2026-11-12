package com.m2i.pti.service;

import com.m2i.pti.domain.PersonnePhysique;
import com.m2i.pti.repository.PersonnePhysiqueRepository;
import com.m2i.pti.service.dto.PersonnePhysiqueDTO;
import com.m2i.pti.service.mapper.PersonnePhysiqueMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


/**
 * Service Implementation for managing PersonnePhysique.
 */
@Service
@Transactional
public class PersonnePhysiqueService {

    private final Logger log = LoggerFactory.getLogger(PersonnePhysiqueService.class);

    private final PersonnePhysiqueRepository personnePhysiqueRepository;

    private final PersonnePhysiqueMapper personnePhysiqueMapper;

    public PersonnePhysiqueService(PersonnePhysiqueRepository personnePhysiqueRepository, PersonnePhysiqueMapper personnePhysiqueMapper) {
        this.personnePhysiqueRepository = personnePhysiqueRepository;
        this.personnePhysiqueMapper = personnePhysiqueMapper;
    }

    /**
     * Save a personnePhysique.
     *
     * @param personnePhysiqueDTO the entity to save
     * @return the persisted entity
     */
    public PersonnePhysiqueDTO save(PersonnePhysiqueDTO personnePhysiqueDTO) {
        log.debug("Request to save PersonnePhysique : {}", personnePhysiqueDTO);
        PersonnePhysique personnePhysique = personnePhysiqueMapper.toEntity(personnePhysiqueDTO);
        personnePhysique = personnePhysiqueRepository.save(personnePhysique);
        return personnePhysiqueMapper.toDto(personnePhysique);
    }

    /**
     * Get all the personnePhysiques.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<PersonnePhysiqueDTO> findAll(Pageable pageable) {
        log.debug("Request to get all PersonnePhysiques");
        return personnePhysiqueRepository.findAll(pageable)
                .map(personnePhysiqueMapper::toDto);
    }

    /**
     * Get one personnePhysique by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public PersonnePhysiqueDTO findOne(Long id) {
        log.debug("Request to get PersonnePhysique : {}", id);
        PersonnePhysique personnePhysique = personnePhysiqueRepository.findById(id).orElse(null);
        return personnePhysiqueMapper.toDto(personnePhysique);
    }

    /**
     * Delete the personnePhysique by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete PersonnePhysique : {}", id);
        personnePhysiqueRepository.deleteById(id);
    }


    /**
     * Recherche en fonction de plusieurs critères
     * @param nom
     * @param prenom
     * @param dateNaissance
     * @return
     */
    public List<PersonnePhysique> findByMultiCriteria(String nom, String prenom, LocalDate dateNaissance) {
        return personnePhysiqueRepository.findPersonnePhysiqueByMultiCriteria(nom, prenom, dateNaissance);
    }
}
