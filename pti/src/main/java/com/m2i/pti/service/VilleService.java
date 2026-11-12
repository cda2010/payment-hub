package com.m2i.pti.service;

import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.Ville;
import com.m2i.pti.domain.enumeration.TypeUO;
import com.m2i.pti.repository.UniteOrganisationRepository;
import com.m2i.pti.repository.VilleRepository;
import com.m2i.pti.service.dto.VilleDTO;
import com.m2i.pti.service.mapper.VilleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;



/**
 * Service Implementation for managing Ville.
 */
@Service
@Transactional
public class VilleService {

    private final Logger log = LoggerFactory.getLogger(VilleService.class);

    private final VilleRepository villeRepository;

    private final VilleMapper villeMapper;

    private final UniteOrganisationRepository uniteOrganisationRepository;

    private final UniteOrganisationService uniteOrganisationService;

    public VilleService(VilleRepository villeRepository, VilleMapper villeMapper, UniteOrganisationRepository uniteOrganisationRepository, UniteOrganisationService uniteOrganisationService) {
        this.villeRepository = villeRepository;
        this.villeMapper = villeMapper;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.uniteOrganisationService = uniteOrganisationService;
    }
    /*Test de taches programmée*/
//    @Scheduled(cron="*/10 * * * * *")
//    public void doScheduledWork() {
//        System.out.println("Tache programmée");
//    }

    /**
     * Save a ville.
     *
     * @param villeDTO the entity to save
     * @return the persisted entity
     */

    public VilleDTO save(VilleDTO villeDTO) {
        log.debug("Request to save Ville : {}", villeDTO);
        Ville ville = villeMapper.toEntity(villeDTO);
        ville = villeRepository.save(ville);
        return villeMapper.toDto(ville);
    }

    /**
     *  Get all the villes.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<VilleDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Villes");
        return villeRepository.findAll(pageable)
            .map(villeMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<VilleDTO> findAllByCountry(Long uoId) {
        UniteOrganisation uo = uniteOrganisationRepository.findById(uoId).orElse(null);
        log.debug("DefaultByOrganisation"+uoId);
        if(!uo.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.PAYS))){
            UniteOrganisation uoPays = uniteOrganisationService.findUoParentByType(uo, TypeUO.PAYS);
            return villeMapper.toDto(villeRepository.findByPaysId(uoPays.getId()));
        }
        return villeMapper.toDto(villeRepository.findByPaysId(uoId));
    }

    /**
     *  Get one ville by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public VilleDTO findOne(Long id) {
        log.debug("Request to get Ville : {}", id);
        Ville ville = villeRepository.findById(id).orElse(null);
        return villeMapper.toDto(ville);
    }

    /**
     *  Delete the  ville by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Ville : {}", id);
        villeRepository.deleteById(id);
    }
}
