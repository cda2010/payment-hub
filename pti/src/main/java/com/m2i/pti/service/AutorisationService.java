package com.m2i.pti.service;

import com.m2i.pti.domain.Autorisation;
import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.TypeService;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.repository.AutorisationRepository;
import com.m2i.pti.repository.UniteOrganisationRepository;
import com.m2i.pti.service.dto.AutorisationDTO;
import com.m2i.pti.service.dto.TypeServiceDTO;
import com.m2i.pti.service.mapper.AutorisationMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing Autorisation.
 */
@Service
@Transactional
public class AutorisationService {

    private final Logger log = LoggerFactory.getLogger(AutorisationService.class);

    private final AutorisationRepository autorisationRepository;

    private final UniteOrganisationRepository uniteOrganisationRepository;

    private final AutorisationMapper autorisationMapper;

    private final TypeServiceService typeServiceService;

    public AutorisationService(AutorisationRepository autorisationRepository, UniteOrganisationRepository uniteOrganisationRepository, AutorisationMapper autorisationMapper, TypeServiceService typeServiceService) {
        this.autorisationRepository = autorisationRepository;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.autorisationMapper = autorisationMapper;
        this.typeServiceService = typeServiceService;
    }

    /**
     * Save a autorisation.
     *
     * @param autorisationDTO the entity to save
     * @return the persisted entity
     */
    public AutorisationDTO save(AutorisationDTO autorisationDTO) {
        log.debug("Request to save Autorisation : {}", autorisationDTO);
        Autorisation autorisation = autorisationMapper.toEntity(autorisationDTO);
        autorisation = autorisationRepository.save(autorisation);
        return autorisationMapper.toDto(autorisation);
    }

    /*
        Verifier si une organisation à l'autorisation d'effectue une operation donnée
     */
       
    public Boolean hasAutorisation(UniteOrganisation uo, TypeService typeService) {

        
        String condition = "unite_organisation_id=" + uo.getId() + " and type_service_id=" + typeService.getId();
        log.debug("CONDITION : {}", condition);
        AutorisationDTO aut = autorisationRepository.findByCondition(uo.getId(),typeService.getId() );
        Autorisation autorisationExistant = autorisationMapper.toEntity(aut);
        // Autorisation autorisationExistant= autorisationRepository.findTopByUniteOrganisationAndTypeService(uo,typeService);
        return autorisationExistant != null ? true : false;
    }

    /**
     * Get all the autorisations.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<AutorisationDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Autorisations");
        return autorisationRepository.findAll(pageable)
                .map(autorisationMapper::toDto);
    }

    /**
     * Get the autorisations.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<AutorisationDTO> findByUO(Long id, Pageable pageable) {
        log.debug("Request to get Autorisations");
        return autorisationRepository.findByUniteOrganisation(uniteOrganisationRepository.findById(id).orElse(null), pageable)
                .map(autorisationMapper::toDto);
    }

    /**
     * Get the autorisations.
     *
     * @param id information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<AutorisationDTO> findByUO(Long id) {
        log.debug("Request to get Autorisations");
        String condition = "unite_organisation_id=" + id ;
    //    AutorisationDTO aut = autorisationRepository.findByCondition(condition);
        return autorisationMapper.toDto(autorisationRepository.findByUniteOrganisation(uniteOrganisationRepository.findById(id).orElse(null)));
    }

    /**
     * Get the autorisations.
     *
     * @param id information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<Autorisation> findByCodeUO(String code) {
        log.debug("Request to get Autorisations");
        return autorisationRepository.findByUniteOrganisation(uniteOrganisationRepository.findByCodeUO(code));
    }

    @Transactional(readOnly = true)
    public List<AutorisationDTO> findByClient(Long clientId) {
        log.debug("Request to get Autorisations");
        return autorisationMapper.toDto(autorisationRepository.findByClientId(clientId));
    }

    /**
     * Enregistrement des autorisations d'un client
     *
     * @param client
     * @param serviceIds
     */
    public void saveAutoriastions(Client client, List<Long> serviceIds) {
        List<TypeServiceDTO> typeServices = typeServiceService.findServiceClient();
        for (TypeServiceDTO service : typeServices) {
            if (client.getId() != null) {
                List<Autorisation> exist = autorisationRepository.findByTypeServiceIdAndClientId(service.getId(), client.getId());
                if (exist.isEmpty()) {
                    if (serviceIds.contains(service.getId())) {
                        AutorisationDTO autorisationDTO = new AutorisationDTO();
                        autorisationDTO.setTypeServiceId(service.getId());
                        autorisationDTO.setClientId(client.getId());
                        this.save(autorisationDTO);
                    }
                } else if (!serviceIds.contains(service.getId())) {
                    autorisationRepository.deleteById(exist.get(0).getId());
                }
            } else {
                throw new CustomParameterizedException("Client introvable......");
            }
        }
    }

    /**
     *
     * @param clientId
     * @param typeServiceId
     * @return
     */
    public List<Autorisation> findByClientIdAndTypeServiceId(Long clientId, Long typeServiceId) {
        return autorisationRepository.findTop1ByTypeServiceIdAndClientId(typeServiceId, clientId);
    }

    /**
     * Get one autorisation by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public AutorisationDTO findOne(Long id) {
        log.debug("Request to get Autorisation : {}", id);
        Autorisation autorisation = autorisationRepository.findById(id).orElse(null);
        return autorisationMapper.toDto(autorisation);
    }

    /**
     * Delete the autorisation by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Autorisation : {}", id);
        autorisationRepository.deleteById(id);
    }
}
