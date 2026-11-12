package com.m2i.pti.service;

import com.m2i.pti.domain.Authority;
import com.m2i.pti.domain.Profil;
import com.m2i.pti.domain.User;
import com.m2i.pti.repository.AuthorityRepository;
import com.m2i.pti.repository.ProfilRepository;
import com.m2i.pti.repository.TypeUniteOrganisationRepository;
import com.m2i.pti.repository.UserRepository;
import com.m2i.pti.security.AuthoritiesConstants;
import com.m2i.pti.service.dto.ProfilDTO;
import com.m2i.pti.service.dto.TypeUniteOrganisationDTO;
import com.m2i.pti.service.mapper.ProfilMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Service Implementation for managing Profil.
 */
@Service
@Transactional
public class ProfilService {

    private final Logger log = LoggerFactory.getLogger(ProfilService.class);

    private final ProfilRepository profilRepository;

    private final ProfilMapper profilMapper;

    private final AuthorityRepository authorityRepository;

    private final TypeUniteOrganisationRepository typeUniteOrganisationRepository;


    private final UserRepository userRepository;

    public ProfilService(ProfilRepository profilRepository, ProfilMapper profilMapper, AuthorityRepository authorityRepository, TypeUniteOrganisationRepository typeUniteOrganisationRepository, UserRepository userRepository) {
        this.profilRepository = profilRepository;
        this.profilMapper = profilMapper;
        this.authorityRepository = authorityRepository;
        this.typeUniteOrganisationRepository = typeUniteOrganisationRepository;
        this.userRepository = userRepository;
    }

    /**
     * Save a profil.
     *
     * @param profilDTO the entity to save
     * @return the persisted entity
     */
    public ProfilDTO save(ProfilDTO profilDTO) {
        log.debug("Request to save Profil : {}", profilDTO);
        Profil profil = profilMapper.toEntity(profilDTO);
        if (profilDTO.getAuthorities() != null) {
            Set<String> auths = profilDTO.getAuthorities();
            //Ajout du role USER par default
            auths.add(AuthoritiesConstants.USER);
            Set<Authority> authorities = profilDTO.getAuthorities().stream()
                    .map(authorityRepository::findById)
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .collect(Collectors.toSet());
            profil.setRoles(authorities);

         /*
            Mise à jour des roles des utilisateurs ayant ce profil
            Pour verifier  que les roles de l'utilisateur ont changes
            on utilise l'attribut profilChange
         */
            if (profilDTO.getProfilsChange() != null) {
                if (profilDTO.getProfilsChange().equals(Boolean.TRUE)) {
                    List<User> user = userRepository.findByProfilId(profilDTO.getId());
                    user.forEach(user1 -> {
                        user1.setAuthorities(authorities);
                        userRepository.save(user1);
                    });
                }
            }
        }
        profil = profilRepository.save(profil);
        return profilMapper.toDto(profil);
    }

    /**
     * Get all the profils.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<ProfilDTO> findAll1(Pageable pageable) {
        log.debug("Request to get all TypeUniteOrganisations");
        Page<ProfilDTO> profils = profilRepository.findAll(pageable).map(profilMapper::toDto);
        for (ProfilDTO profilDTO : profils) {
            ProfilDTO profilDTO1 = this.findOne(profilDTO.getId());
            profilDTO.setAuthorities(profilDTO1.getAuthorities());
        }
        return profils;

    }

    /**
     * Get all the profils.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<ProfilDTO> findAll(Pageable pageable) {
        return profilRepository.findAll(pageable)
                .map(ProfilDTO::new);
    }

    /**
     * Liste des profils par typeUO
     * @param typeId
     * @return
     */
    @Transactional(readOnly = true)
    public List<ProfilDTO> findByTypeUO(Long typeId) {
        log.debug("Request to get all Profils");
        return profilMapper.toDto(profilRepository.findByTypeUO(typeUniteOrganisationRepository.findById(typeId).orElse(null)));
    }

    /**
     * Liste des profils par typeUO
     * @param typeId
     * @return
     */
    @Transactional(readOnly = true)
    public List<ProfilDTO> findProfilsAndRolesByTypeUO(Long typeId) {
        log.debug("Request to get all Profils");
        List<ProfilDTO> profilDTOS = profilMapper.toDto(profilRepository.findByTypeUO(typeUniteOrganisationRepository.findById(typeId).orElse(null)));
        for (ProfilDTO profilDTO : profilDTOS) {
            Set<String> auts = profilRepository.findRoleByProfilId(profilDTO.getId());
            log.debug("--------------------------Profils : {}", auts);
            log.debug("--------------------------size : {}", auts.size());
            profilDTO.setAuthorities(auts);
        }
        return profilDTOS;
    }

    public Set<Authority> findProfilRoles(Long profilId) {
        Profil profil = profilRepository.findById(profilId).orElse(null);
        return profil.getRoles();
    }

    /**
     * Get one profil by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public ProfilDTO findOne(Long id) {
        log.debug("Request to get Profil : {}", id);
        Profil profil = profilRepository.findOneWithEagerRelationships(id);
        log.debug("ceci est les roles+++++++++++++++++++++++: " + profil.getRoles());
        ProfilDTO profilDTO = profilMapper.toDto(profil);
        profilDTO.setAuthorities(profil.getRoles().stream().map(Authority::getName).collect(Collectors.toSet()));
        return profilDTO;
    }

    /**
     * Delete the  profil by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Profil : {}", id);
        if (userRepository.findByProfilId(id).isEmpty()) {
            profilRepository.deleteById(id);
        } else {
            throw new CustomParameterizedException("Impossible de supprimer,Ce profil est associé à des utilisateurs");
        }
    }
    @Transactional(readOnly = true)
    public List<ProfilDTO> findByTypeUOOptimise(Long typeId) {
        log.debug("Request to get all Profils");
        TypeUniteOrganisationDTO tuo = typeUniteOrganisationRepository.findDtoById(typeId);
        return profilRepository.findByTuoId(tuo!=null?tuo.getId():null);
    }

    @Transactional(readOnly = true)
    public Page<ProfilDTO> findAll1Optimise(Pageable pageable) {

        Long taille = new Long(pageable.getPageSize());
        Long page = new Long(pageable.getPageSize()*pageable.getPageNumber());
        int totalProfil;
        Pageable pageable1= new Pageable() {
            @Override
            public int getPageNumber() {
                return 0;
            }
            @Override
            public int getPageSize() {
                return 0;
            }
            @Override
            public long getOffset() {
                return 0;
            }

            @Override
            public Pageable withPage(int pageNumber) {
                return this;
            }
            @Override
            public Sort getSort() {
                return null;
            }
            @Override
            public Pageable next() {
                return null;
            }
            @Override
            public Pageable previousOrFirst() {
                return null;
            }
            @Override
            public Pageable first() {
                return null;
            }
            @Override
            public boolean hasPrevious() {
                return false;
            }
        };
        log.debug("Request to get all TypeUniteOrganisations");
        totalProfil = profilRepository.countAllProfil();
        List<ProfilDTO> profils = profilRepository.findAllProfil(taille,page);
        for (ProfilDTO profilDTO : profils) {
            profilDTO.setAuthorities(profilRepository.findRoleByProfilId(profilDTO.getId()));
        }
        Page<ProfilDTO> retour = new PageImpl<>(profils, pageable1, totalProfil);
        return retour;
    }


}
