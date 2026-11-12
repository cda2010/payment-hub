package com.m2i.pti.service;

import com.m2i.pti.domain.TauxChange;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.enumeration.TypeChange;
import com.m2i.pti.domain.enumeration.TypeUO;
import com.m2i.pti.repository.TauxChangeRepository;
import com.m2i.pti.service.dto.TauxChangeDTO;
import com.m2i.pti.service.dto.UoDeviseDTO;
import com.m2i.pti.service.mapper.TauxChangeMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


/**
 * Service Implementation for managing TauxChange.
 */
@Service
@Transactional
public class TauxChangeService {

    private final Logger log = LoggerFactory.getLogger(TauxChangeService.class);

    private final TauxChangeRepository tauxChangeRepository;

    private final TauxChangeMapper tauxChangeMapper;

    private final UniteOrganisationService uniteOrganisationService;

    private final UoDeviseService uoDeviseService;

    public TauxChangeService(TauxChangeRepository tauxChangeRepository, TauxChangeMapper tauxChangeMapper, UniteOrganisationService uniteOrganisationService, UoDeviseService uoDeviseService) {
        this.tauxChangeRepository = tauxChangeRepository;
        this.tauxChangeMapper = tauxChangeMapper;
        this.uniteOrganisationService = uniteOrganisationService;
        this.uoDeviseService = uoDeviseService;
    }

    /**
     * Save a tauxChange.
     *
     * @param tauxChangeDTO the entity to save
     * @return the persisted entity
     */
    public TauxChangeDTO save(TauxChangeDTO tauxChangeDTO) {
        log.debug("Request to save TauxChange : {}", tauxChangeDTO);
        TauxChange tauxChange = tauxChangeMapper.toEntity(tauxChangeDTO);
        tauxChange.dateTaux(LocalDate.now());
        tauxChange = tauxChangeRepository.save(tauxChange);
        return tauxChangeMapper.toDto(tauxChange);
    }

    /**
     * Update taux existants
     */
    public void updateAll() {
        List<TauxChange> tauxChanges = tauxChangeRepository.findAll();
        tauxChanges.forEach(tauxChange -> {
            log.debug("mise à jour");
            save(tauxChangeMapper.toDto(tauxChange));
        });
    }

    /**
     * Get all the tauxChanges.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<TauxChangeDTO> findAll(Pageable pageable) {
        log.debug("Request to get all TauxChanges");
        return tauxChangeRepository.findAll(pageable)
                .map(tauxChangeMapper::toDto);
    }

    /**
     * Recherche en fonction de la devise de depart et de la devise d'arrive
     *
     * @param departId
     * @param arriveeId
     * @param dateTaux
     * @param typeChange
     * @return
     */
    public TauxChangeDTO findByCriteria(Long departId, Long arriveeId, LocalDate dateTaux, TypeChange typeChange) {
        return tauxChangeMapper.toDto(tauxChangeRepository.findTopByDeviseDepartIdAndDeviseArriveeIdAndDateTauxAndTypeChange(departId, arriveeId, dateTaux, typeChange));
    }

    /**
     * @param arriveeId
     * @param dateTaux
     * @param typeChange
     * @return
     */
    public TauxChangeDTO findByCriteriaOfUserConnecte(Long departId, Long arriveeId, LocalDate dateTaux, TypeChange typeChange) {
        UniteOrganisation uoConnecte = uniteOrganisationService.getUoOfUserConnecte();
        UniteOrganisation uoPays = uniteOrganisationService.findUoParentByType(uoConnecte, TypeUO.PAYS);
        if (departId == null) {//Recherche de la devise source
            if (uoPays != null) {
                UoDeviseDTO uoDevise = uoDeviseService.findByActeurDefault(uoPays.getId());
                if (uoDevise != null) {
                    departId = uoDevise.getDeviseId();
                } else
                    throw new CustomParameterizedException("La devise par defaut de votre pays n'est pas configure!");
            } else {
                throw new CustomParameterizedException("Le pays de votre organisation n'est pas configure !");
            }
        }
        return tauxChangeMapper.toDto(tauxChangeRepository.findTopByDeviseDepartIdAndDeviseArriveeIdAndDateTauxAndTypeChange(departId, arriveeId, dateTaux, typeChange));
    }

    /**
     * Get one tauxChange by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public TauxChangeDTO findOne(Long id) {
        log.debug("Request to get TauxChange : {}", id);
        TauxChange tauxChange = tauxChangeRepository.findById(id).orElse(null);
        return tauxChangeMapper.toDto(tauxChange);
    }

    /**
     * Delete the  tauxChange by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete TauxChange : {}", id);
        tauxChangeRepository.deleteById(id);
    }
}
