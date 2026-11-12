package com.m2i.pti.service;

import com.m2i.pti.domain.GroupeProfilActeur;
import com.m2i.pti.domain.ProfilActeur;
import com.m2i.pti.domain.enumeration.DestinationProfilActeur;
import com.m2i.pti.domain.enumeration.TypeProfilActeur;
import com.m2i.pti.repository.GroupeProfilActeurRepository;
import com.m2i.pti.repository.ProfilActeurRepository;
import com.m2i.pti.service.dto.GroupeProfilActeurDTO;
import com.m2i.pti.service.dto.ProfilActeurDTO;
import com.m2i.pti.service.mapper.GroupeProfilActeurMapper;
import com.m2i.pti.service.mapper.ProfilActeurMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * Service Implementation for managing GroupeProfilActeur.
 */
@Service
@Transactional
public class GroupeProfilActeurService {

    private final Logger log = LoggerFactory.getLogger(GroupeProfilActeurService.class);

    private final GroupeProfilActeurRepository groupeProfilActeurRepository;

    private final GroupeProfilActeurMapper groupeProfilActeurMapper;
    private final ProfilActeurRepository profilActeurRepository;
    private final ProfilActeurMapper profilActeurMapper;

    public GroupeProfilActeurService(GroupeProfilActeurRepository groupeProfilActeurRepository, GroupeProfilActeurMapper groupeProfilActeurMapper, ProfilActeurRepository profilActeurRepository, ProfilActeurMapper profilActeurMapper) {
        this.groupeProfilActeurRepository = groupeProfilActeurRepository;
        this.groupeProfilActeurMapper = groupeProfilActeurMapper;
        this.profilActeurRepository = profilActeurRepository;
        this.profilActeurMapper = profilActeurMapper;
    }

    /**
     * Save a groupeProfilActeur.
     *
     * @param groupeProfilActeurDTO the entity to save
     * @return the persisted entity
     */
    public GroupeProfilActeurDTO save(GroupeProfilActeurDTO groupeProfilActeurDTO) {
        log.debug("Request to save GroupeProfilActeur : {}", groupeProfilActeurDTO);
        GroupeProfilActeur groupeProfilActeur = groupeProfilActeurMapper.toEntity(groupeProfilActeurDTO);
        verifDefaulGroup(groupeProfilActeur);
        groupeProfilActeur = groupeProfilActeurRepository.save(groupeProfilActeur);
        if(groupeProfilActeurDTO.getId()==null){//création automatique de deux profils pour les nouveau groups.
            ProfilActeur profilActeur=new ProfilActeur();
            if(groupeProfilActeur.getType().equals(TypeProfilActeur.CLIENT)){
                profilActeur.setGroupe(groupeProfilActeur);
                profilActeur.setNom("Client Identifié");
                profilActeur.setDescription("Profil des clients identifiés du groupe "+groupeProfilActeur.getNom());
                profilActeur.setType(groupeProfilActeur.getType());
                profilActeur.setDestineA(DestinationProfilActeur.CLIENT_IDENTIFIE);
                profilActeurRepository.save(profilActeur);
                profilActeur=new ProfilActeur();
                profilActeur.setGroupe(groupeProfilActeur);
                profilActeur.setNom("Client Non Identifié");
                profilActeur.setDescription("Profil des clients non identifiés du groupe "+groupeProfilActeur.getNom());
                profilActeur.setType(groupeProfilActeur.getType());
                profilActeur.setDestineA(DestinationProfilActeur.CLIENT_NON_IDENTIFIE);
                profilActeurRepository.save(profilActeur);
            }else {
                profilActeur=new ProfilActeur();
                profilActeur.setGroupe(groupeProfilActeur);
                profilActeur.setNom(groupeProfilActeur.getNom());
                profilActeur.setDescription("Profil des unités d'organisation du groupe "+groupeProfilActeur.getNom());
                profilActeur.setType(groupeProfilActeur.getType());
                profilActeur.setDestineA(DestinationProfilActeur.UNITE_ORGANISATION);
                profilActeurRepository.save(profilActeur);
            }
        }
        groupeProfilActeurDTO=groupeProfilActeurMapper.toDto(groupeProfilActeur);
        List<ProfilActeurDTO> profilActeurDTOS=new ArrayList<>();
        groupeProfilActeur.getProfilActeurs().forEach(profilActeur -> {
            profilActeurDTOS.add(profilActeurMapper.toDto(profilActeur));
        });
        groupeProfilActeurDTO.setProfils(profilActeurDTOS);
        return groupeProfilActeurDTO ;
    }

    public void verifDefaulGroup(GroupeProfilActeur groupeProfilActeur) {
        Optional<GroupeProfilActeur> profilExist = groupeProfilActeurRepository.findByTypeAndActiveTrue(groupeProfilActeur.getType());
        if (profilExist.isPresent()) {
            if (groupeProfilActeur.getId() == null) {
                if (groupeProfilActeur.isActive() == Boolean.TRUE)
                    throw new CustomParameterizedException("Il existe déja un groupe par defaut !");
            } else {
                GroupeProfilActeur oldGroup = groupeProfilActeurRepository.findById(groupeProfilActeur.getId()).orElse(null);
                if ((groupeProfilActeur.isActive() != oldGroup.isActive())
                        || groupeProfilActeur.getType() != oldGroup.getType()) {
                    if (groupeProfilActeur.isActive() == Boolean.TRUE)
                        throw new CustomParameterizedException("Il existe déja un groupe par defaut!");
                }
            }
        }
    }

    /**
     *  Get all the groupeProfilActeurs.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<GroupeProfilActeurDTO> findAll(Pageable pageable) {
        log.debug("Request to get all GroupeProfilActeurs");
        return groupeProfilActeurRepository.findAll(pageable)
            .map(groupeProfilActeur -> {
                GroupeProfilActeurDTO  groupeProfilActeurDTO=groupeProfilActeurMapper.toDto(groupeProfilActeur);
                return groupeProfilActeurDTO;
            });
    }

    public List<GroupeProfilActeur> findAllByType(String acteur) {
        if(acteur.equals("ORGANISATION")){
            return groupeProfilActeurRepository.findAllByType(TypeProfilActeur.ORGANISATION);
        }
        return groupeProfilActeurRepository.findAllByType(TypeProfilActeur.CLIENT);

    }

    /**
     *  Get one groupeProfilActeur by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public GroupeProfilActeurDTO findOne(Long id) {
        log.debug("Request to get GroupeProfilActeur : {}", id);
        GroupeProfilActeur groupeProfilActeur = groupeProfilActeurRepository.findById(id).orElse(null);
        GroupeProfilActeurDTO groupeProfilActeurDTO=groupeProfilActeurMapper.toDto(groupeProfilActeur);
        List<ProfilActeurDTO> profilActeurDTOS=new ArrayList<>();
        groupeProfilActeur.getProfilActeurs().forEach(profilActeur -> {
            profilActeurDTOS.add(profilActeurMapper.toDto(profilActeur));
        });
        groupeProfilActeurDTO.setProfils(profilActeurDTOS);
        return groupeProfilActeurDTO ;
    }

    /**
     *  Delete the  groupeProfilActeur by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete GroupeProfilActeur : {}", id);
        groupeProfilActeurRepository.deleteById(id);
    }
}
