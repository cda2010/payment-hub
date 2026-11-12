package com.m2i.pti.service;

import com.m2i.pti.domain.*;
import com.m2i.pti.repository.AuthorityRepository;
import com.m2i.pti.repository.TypeUniteOrganisationRepository;
import com.m2i.pti.repository.UniteOrganisationRepository;
import com.m2i.pti.service.dto.TypeUniteOrganisationDTO;
import com.m2i.pti.service.mapper.TypeUniteOrganisationMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.collections4.map.PassiveExpiringMap;

/**
 * Service Implementation for managing TypeUniteOrganisation.
 */
@Service
@Transactional
public class TypeUniteOrganisationService {

    private final Logger log = LoggerFactory.getLogger(TypeUniteOrganisationService.class);

    private final TypeUniteOrganisationRepository typeUniteOrganisationRepository;

    private final UniteOrganisationRepository uniteOrganisationRepository;

    private final UserService userService;

    private final TypeUniteOrganisationMapper typeUniteOrganisationMapper;
    private final AuthorityRepository authorityRepository;
    PassiveExpiringMap<String, TypeUniteOrganisationDTO> typeUniteOrganisationMapByID = new PassiveExpiringMap<>(6400000);

    public TypeUniteOrganisationService(TypeUniteOrganisationRepository typeUniteOrganisationRepository, UniteOrganisationRepository uniteOrganisationRepository, UserService userService, TypeUniteOrganisationMapper typeUniteOrganisationMapper, AuthorityRepository authorityRepository) {
        this.typeUniteOrganisationRepository = typeUniteOrganisationRepository;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.userService = userService;
        this.typeUniteOrganisationMapper = typeUniteOrganisationMapper;
        this.authorityRepository = authorityRepository;
    }

    /**
     * Save a typeUniteOrganisation.
     *
     * @param typeUniteOrganisationDTO the entity to save
     * @return the persisted entity
     */
    public TypeUniteOrganisationDTO save(TypeUniteOrganisationDTO typeUniteOrganisationDTO) {
        log.debug("Request to save TypeUniteOrganisation : {}", typeUniteOrganisationDTO);
        TypeUniteOrganisation typeUniteOrganisation = typeUniteOrganisationMapper.toEntity(typeUniteOrganisationDTO);
        Set<Authority> roles = new HashSet<>();
        for (String str : typeUniteOrganisationDTO.getAuthorities()) {
            roles.add(authorityRepository.findAllByName(str));
        }
        typeUniteOrganisation.setRoles(roles);
        Optional<TypeUniteOrganisation> typeUoExistWithCode = typeUniteOrganisationRepository.findByCodeTypeUO(typeUniteOrganisationDTO.getCodeTypeUO());
        if (typeUoExistWithCode.isPresent() && (!typeUoExistWithCode.get().getId().equals(typeUniteOrganisationDTO.getId()))) {
            throw new CustomParameterizedException("Le libelle est déjà utilisé !");
        }

        TypeUniteOrganisation typeUoWithLibelle = typeUniteOrganisationRepository.findByLibelleTypeUO(typeUniteOrganisationDTO.getLibelleTypeUO());
        if (typeUoWithLibelle != null && !typeUoWithLibelle.getId().equals(typeUniteOrganisationDTO.getId())) {
            throw new CustomParameterizedException("Le code est déjà utilisé !");
        }
        typeUniteOrganisation = typeUniteOrganisationRepository.save(typeUniteOrganisation);
        TypeUniteOrganisationDTO dto = typeUniteOrganisationMapper.toDto(typeUniteOrganisation);
        Set<String> rolesDto = new HashSet<>();
        for (Authority authority : typeUniteOrganisation.getRoles()) {
            rolesDto.add(authority.getName());
        }
        dto.setAuthorities(rolesDto);
        return dto;
    }

    /**
     * Get all the typeUniteOrganisations.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<TypeUniteOrganisationDTO> findAll(Pageable pageable) {
        log.debug("Request to get all TypeUniteOrganisations");
        return typeUniteOrganisationRepository.findAll(pageable)
                .map((e) -> {
                    TypeUniteOrganisationDTO dto = typeUniteOrganisationMapper.toDto(e);
                    Set<String> rolesDto = new HashSet<>();
                    for (Authority authority : e.getRoles()) {
                        rolesDto.add(authority.getName());
                    }
                    dto.setAuthorities(rolesDto);
                    return dto;
                });
    }

    /**
     * Get all the typeUniteOrganisations different to unite de regroupement.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<TypeUniteOrganisationDTO> findAllWithoutUR() {
        log.debug("Request to get all TypeUniteOrganisations");
        List<TypeUniteOrganisation> typeUniteOrganisationList = typeUniteOrganisationRepository.findByNiveauDifUR();
        return typeUniteOrganisationMapper.toDto(typeUniteOrganisationList);
    }

    @Transactional(readOnly = true)
    public Set<Authority> findAllRoleByTypeUniteOrganisation(Long typeUniteorganisationId) {
        log.debug("Request to get all Type");
        TypeUniteOrganisation typeUniteOrganisation = typeUniteOrganisationRepository.findById(typeUniteorganisationId).orElse(null);
        if (typeUniteOrganisation != null) {
            if (typeUniteOrganisation != null) {
                return typeUniteOrganisation.getRoles();
            }
        }
        return Collections.emptySet();
    }


    /*
     *
     */
    public boolean hasRole(User user, String role) {
        if (user != null) {
            Set<String> roles = user.getAuthorities().stream().map(Authority::getName)
                    .collect(Collectors.toSet());
            if (roles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    @Transactional(readOnly = true)
    public TypeUniteOrganisation findByOne(Long id) {
        log.debug("Request to get TypeUniteOrganisation : {}", id);
        TypeUniteOrganisationDTO typeUniteOrganisation;
        if (typeUniteOrganisationMapByID.containsKey(id.toString()) && typeUniteOrganisationMapByID.get(id.toString())!=null) {
            typeUniteOrganisation = typeUniteOrganisationMapByID.get(id.toString());
        } else {
            typeUniteOrganisation = typeUniteOrganisationRepository.findDtoById(id);
            if (typeUniteOrganisation!=null){
                 typeUniteOrganisationMapByID.put(id.toString(), typeUniteOrganisation);
            }
           
        }
        TypeUniteOrganisation dto = typeUniteOrganisationMapper.toEntity(typeUniteOrganisation);

        return dto;
    }

    @Transactional(readOnly = true)
    public TypeUniteOrganisation findByCodeType(String code) {
        log.debug("Request to get TypeUniteOrganisation : {}", code);
        TypeUniteOrganisationDTO typeUniteOrganisation = typeUniteOrganisationRepository.findCodetype(code);
        TypeUniteOrganisation dto = typeUniteOrganisationMapper.toEntity(typeUniteOrganisation);

        return dto;
    }

    /**
     * Get one typeUniteOrganisation by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public TypeUniteOrganisationDTO findOne(Long id) {
        log.debug("Request to get TypeUniteOrganisation : {}", id);
        TypeUniteOrganisation typeUniteOrganisation = typeUniteOrganisationRepository.findById(id).orElse(null);
        TypeUniteOrganisationDTO dto = typeUniteOrganisationMapper.toDto(typeUniteOrganisation);
        Set<String> rolesDto = new HashSet<>();
        for (Authority authority : typeUniteOrganisation.getRoles()) {
            rolesDto.add(authority.getName());
        }
        dto.setAuthorities(rolesDto);
        return dto;
    }

    /**
     * Delete the typeUniteOrganisation by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete TypeUniteOrganisation : {}", id);
        TypeUniteOrganisation typeUniteOrganisation = typeUniteOrganisationRepository.findById(id).orElse(null);
        if (!uniteOrganisationRepository.findByTypeUOAndSupprimeIsFalse(typeUniteOrganisation).isEmpty()) {
            typeUniteOrganisationRepository.deleteById(id);
        } else {
            throw new CustomParameterizedException("Impossible de supprimer,Ce type uo est déjà utilisé");
        }
    }

    @Transactional(readOnly = true)
    public Page<TypeUniteOrganisationDTO> findAllOptimise(Pageable pageable) {

        Long taille = new Long(pageable.getPageSize());
        Long page = new Long(pageable.getPageSize() * pageable.getPageNumber());
        int totalTuo = (int) typeUniteOrganisationRepository.count();

        Pageable pageable1 = new Pageable() {
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
        List<TypeUniteOrganisationDTO> typeUniteOrganisationDTOS = typeUniteOrganisationRepository.findAllTUO(taille, page);
        for (TypeUniteOrganisationDTO tuo : typeUniteOrganisationDTOS) {
            tuo.setAuthorities(typeUniteOrganisationRepository.findRoleByTUOId(tuo.getId()).stream().collect(Collectors.toSet()));
        }
        Page<TypeUniteOrganisationDTO> retour = new PageImpl<>(typeUniteOrganisationDTOS, pageable1, totalTuo);
        return retour;

    }

    @Transactional(readOnly = true)
    public List<TypeUniteOrganisationDTO> findAllWithoutUROptimise() {
        log.debug("Request to get all TypeUniteOrganisations");
        return typeUniteOrganisationRepository.findByNiveau();
    }

    @Transactional(readOnly = true)
    public TypeUniteOrganisationDTO findOneOptimise(Long id) {
        log.debug("Request to get TypeUniteOrganisation : {}", id);
        TypeUniteOrganisationDTO typeUniteOrganisation = typeUniteOrganisationRepository.findDtoById(id);

        List<String> role = typeUniteOrganisationRepository.findRoleByTUOId(typeUniteOrganisation.getId());
        Set<String> rolesDto = new HashSet<>();
        for (String authority : role) {
            rolesDto.add(authority);
        }
        typeUniteOrganisation.setAuthorities(rolesDto);
        return typeUniteOrganisation;
    }
  @Transactional(readOnly = true)
    public TypeUniteOrganisationDTO findTypeUniteOrganisationByUo(Long id) {
        log.debug("Request to get TypeUniteOrganisation : {}", id);
        return typeUniteOrganisationMapper.toDto(this.typeUniteOrganisationRepository.findTypeUniteOrganisationByUo(id) ) ;
    }
}
