package com.m2i.pti.service;

import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.ServiceProfil;
import com.m2i.pti.domain.enumeration.TypeProfilService;
import com.m2i.pti.repository.ClientRepository;
import com.m2i.pti.repository.ServiceProfilRepository;
import com.m2i.pti.service.dto.ServiceProfilDTO;
import com.m2i.pti.service.mapper.ServiceProfilMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Service Implementation for managing ServiceProfil.
 */
@Service
@Transactional
public class ServiceProfilService {

    private final Logger log = LoggerFactory.getLogger(ServiceProfilService.class);

    private final ServiceProfilRepository serviceProfilRepository;

    private final ServiceProfilMapper serviceProfilMapper;

    private final ClientRepository clientRepository;

    private final AutorisationService autorisationService;

    public ServiceProfilService(ServiceProfilRepository serviceProfilRepository, ServiceProfilMapper serviceProfilMapper, ClientRepository clientRepository, AutorisationService autorisationService) {
        this.serviceProfilRepository = serviceProfilRepository;
        this.serviceProfilMapper = serviceProfilMapper;
        this.clientRepository = clientRepository;
        this.autorisationService = autorisationService;
    }

    /**
     * Save a serviceProfil.
     *
     * @param serviceProfilDTO the entity to save
     * @return the persisted entity
     */
    public ServiceProfilDTO save(ServiceProfilDTO serviceProfilDTO) {
        log.debug("Request to save Service Profil : {}", serviceProfilDTO);
        ServiceProfil serviceProfil = serviceProfilMapper.toEntity(serviceProfilDTO);
        /*if (serviceProfil.getId() != null) {//Mise à jour automatique des autorisations de tous les clients
            List<Client> clients = clientRepository.findByProfilIdAndSupprimeIsFalse(serviceProfil.getId());
            for (Client client : clients) {
                autorisationService.saveAutoriastions(client, serviceProfil.getTypeServices().stream().map(typeService -> typeService.getId()).collect(Collectors.toList()));
            }
        }*/
        serviceProfil = serviceProfilRepository.save(serviceProfil);
        return serviceProfilMapper.toDto(serviceProfil);
    }

    /**
     * Get all the serviceProfils.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<ServiceProfilDTO> findAll(Pageable pageable) {
        log.debug("Request to get all ServiceProfils");
        return serviceProfilRepository.findAll(pageable)
                .map(serviceProfilMapper::toDto);
    }
    @Transactional(readOnly = true)
    public List<ServiceProfilDTO> findByType(TypeProfilService typeProfilService) {
        log.debug("Request to get all ServiceProfils");
        List<ServiceProfil> list= serviceProfilRepository.findAllByTypeProfilService(typeProfilService);
        List<ServiceProfilDTO> result=new ArrayList<ServiceProfilDTO>();
        list.forEach(item->{
            result.add(serviceProfilMapper.toDto(item));
        });
        return result;
    }

    /**
     * Get one serviceProfil by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public ServiceProfilDTO findOne(Long id) {
        log.debug("Request to get ServiceProfil : {}", id);
        ServiceProfil serviceProfil = serviceProfilRepository.findOneWithEagerRelationships(id);
        return serviceProfilMapper.toDto(serviceProfil);
    }

    /**
     * Delete the  serviceProfil by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete ServiceProfil : {}", id);
        serviceProfilRepository.deleteById(id);
    }
}
