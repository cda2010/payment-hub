package com.m2i.pti.service;

import com.m2i.pti.domain.PointVente;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.User;
import com.m2i.pti.domain.enumeration.TypeUO;
import com.m2i.pti.repository.PointVenteRepository;
import com.m2i.pti.repository.UniteOrganisationRepository;
import com.m2i.pti.repository.UserRepository;
import com.m2i.pti.service.dto.PointVenteDTO;
import com.m2i.pti.service.dto.PointVenteUserDTO;
import com.m2i.pti.service.dto.UniteOrganisationDTO;
import com.m2i.pti.service.mapper.PointVenteMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing PointVente.
 */
@Service
@Transactional
public class PointVenteService {

    private final Logger log = LoggerFactory.getLogger(PointVenteService.class);

    private final PointVenteRepository pointVenteRepository;

    private final PointVenteMapper pointVenteMapper;

    private final UniteOrganisationRepository uniteOrganisationRepository;

    private final UserRepository userRepository;

    private final UniteOrganisationService uniteOrganisationService;

    private UserService userService;

    public PointVenteService(PointVenteRepository pointVenteRepository, PointVenteMapper pointVenteMapper, UniteOrganisationRepository uniteOrganisationRepository, UserRepository userRepository, UniteOrganisationService uniteOrganisationService, UserService userService) {
        this.pointVenteRepository = pointVenteRepository;
        this.pointVenteMapper = pointVenteMapper;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.userRepository = userRepository;
        this.uniteOrganisationService = uniteOrganisationService;
        this.userService = userService;
    }

    /**
     * Save a pointVente.
     *
     * @param pointVenteDTO the entity to save
     * @return the persisted entity
     */
    public PointVenteDTO save(PointVenteDTO pointVenteDTO) {
        log.debug("Request to save PointVente : {}", pointVenteDTO);
        Optional<PointVente> pointVenteExistWithLibelle = pointVenteRepository.findByLibellePointVenteAndUniteOrganisationId(pointVenteDTO.getLibellePointVente(), pointVenteDTO.getUniteOrganisationId());
        if (pointVenteExistWithLibelle.isPresent() && !pointVenteExistWithLibelle.get().getId().equals(pointVenteDTO.getId())) {
            throw new CustomParameterizedException("Le libelle est déjà utilisé !");
        }
        if (pointVenteDTO.getId() == null) {

        }
        PointVente pointVente = pointVenteMapper.toEntity(pointVenteDTO);
        pointVente = pointVenteRepository.save(pointVente);
        return pointVenteMapper.toDto(pointVente);
    }

    public PointVente save(PointVente pointVente) {
        log.debug("Request to save PointVente : {}", pointVente);

        PointVente pointV = pointVenteRepository.save(pointVente);
        return pointV;
    }

    @Cacheable(value = "findPointByCode", key = "#p0", sync = true)
    @Transactional(readOnly = true)
    public PointVente findPointByCode(String codePv) {
        return pointVenteMapper.toEntity(pointVenteRepository.findByPointVente(codePv));

    }

    /**
     * @param codePv
     * @return
     */
    public Optional<PointVente> findByCode(String codePv) {
        return pointVenteRepository.findTopByCodePointVente(codePv);
    }

    /**
     * Get all the pointVentes.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    /**
     * Liste des point de ventes de l'organisation
     */
    @Transactional(readOnly = true)
    public Page<PointVenteDTO> findAll(Pageable pageable) {
        log.debug("Request to get all PointVentes");
        UniteOrganisation uniteOrganisation = userService.getUserWithAuthorities().getUniteOrganisation();
        if (uniteOrganisation != null) {
            log.debug(uniteOrganisation.getId() + "*****************************************************************");
            return findByUO(uniteOrganisation.getId(), pageable);
        } else {
            throw new CustomParameterizedException("UO introuvable");
        }
    }

    public List<PointVente> findAllPv() {
        return pointVenteRepository.findAll();
    }

    /**
     * Get pointVentes by unite organisation.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<PointVenteDTO> findByUO(Long uoId, Pageable pageable) {
        log.debug("Request to get all PointVentes");
        return pointVenteRepository.findByUniteOrganisation(uniteOrganisationRepository.findById(uoId).orElse(null), pageable)
                .map(pointVenteMapper::toDto);
    }

    /**
     * Get pointVentes by unite organisation.
     *
     * @param uoId information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<PointVenteDTO> findByUO(Long uoId) {
        log.debug("Request to get all PointVentes");
        //return pointVenteMapper.toDto(pointVenteRepository.findByUniteOrganisation(uniteOrganisationRepository.findById(uoId).orElse(null)));
        return pointVenteRepository.findByUniteOrganisationIdOptimise(uniteOrganisationRepository.findById(uoId).orElse(null).getId());
    }

    @Transactional(readOnly = true)
    public List<PointVenteDTO> findByUOAndCord(String longitudeMin, String longitudeMax, String latitudeMin, String latitudeMax) {
        log.debug("Request to get all PointVentes longitudeMin" + longitudeMin);
        log.debug("Request to get all PointVentes longitudeMax" + longitudeMax);
        log.debug("Request to get all PointVentes latitudeMin" + latitudeMin);
        log.debug("Request to get all PointVentes latitudeMax" + latitudeMax);
        //return pointVenteMapper.toDto(pointVenteRepository.findByUniteOrganisation(uniteOrganisationRepository.findById(uoId).orElse(null)));
        return pointVenteRepository.findByUoIdAndCordOptimise(longitudeMin, longitudeMax, latitudeMin, latitudeMax);
    }

    @Transactional(readOnly = true)
    public List<PointVenteDTO> findAllByFSE(UniteOrganisation uo, String longitudeMin, String longitudeMax, String latitudeMin, String latitudeMax) {

        if (uo != null) {
            List<PointVenteDTO> pvs = new ArrayList<>();
            if (uo.getTypeUO().getLibelleTypeUO().equals(TypeUO.FSE.name())) {
                ArrayList<UniteOrganisation> listAll = new ArrayList<>();

                listAll.addAll(uniteOrganisationService.findAllUoFils(uo));
                listAll.forEach(u -> {
                    pvs.addAll(findByUO(u.getId()));
                });
                return pvs;
            }
        }
        log.debug("Request to get all PointVentes");
        return pointVenteMapper.toDto(pointVenteRepository.findByUniteOrganisation(uniteOrganisationRepository.findById(uo.getId()).orElse(null)));
    }

    @Transactional(readOnly = true)
    public List<PointVenteDTO> findAllCordByFSE(UniteOrganisation uo, String longitudeMin, String longitudeMax, String latitudeMin, String latitudeMax) {

        if (uo != null) {
            List<PointVenteDTO> pvs = new ArrayList<>();
            if (uo.getTypeUO().getLibelleTypeUO().equals(TypeUO.FSE.name())) {
                // ArrayList<UniteOrganisation> listAll = new ArrayList<>();

                // listAll.addAll(uniteOrganisationService.findAllUoFils(uo));
                pvs = findByUOAndCord(longitudeMin, longitudeMax, latitudeMin, latitudeMax);

                /* listAll.forEach(u -> {
                });*/
                return pvs;
            }
        }
        log.debug("Request to get all PointVentes");
        return pointVenteMapper.toDto(pointVenteRepository.findByUniteOrganisation(uniteOrganisationRepository.findById(uo.getId()).orElse(null)));
    }

    @Transactional(readOnly = true)
    public List<PointVenteDTO> findAllByUO(Long uoId) {
        UniteOrganisation uo = uniteOrganisationRepository.findByUoId(uoId);
        if (uo != null) {
            List<PointVenteDTO> pvs = new ArrayList<>();
            if (uo.getTypeUO().getLibelleTypeUO().equals(TypeUO.FSE.name())) {
                ArrayList<UniteOrganisation> listAll = new ArrayList<>();
                ArrayList<UniteOrganisation> listFils = new ArrayList<>();
                listAll.addAll(uniteOrganisationService.findAllUoFils(uo, listAll, listFils));
                listAll.forEach(u -> {
                    pvs.addAll(findByUO(u.getId()));
                });
                return pvs;
            }
        }
        log.debug("Request to get all PointVentes");
        return pointVenteMapper.toDto(pointVenteRepository.findByUniteOrganisation(uniteOrganisationRepository.findById(uoId).orElse(null)));
    }

    /*

     */
    /**
     * Get one pointVente by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public PointVenteDTO findOne(Long id) {
        log.debug("Request to get PointVente : {}", id);
        PointVente pointVente = pointVenteRepository.findById(id).orElse(null);
        return pointVenteMapper.toDto(pointVente);
    }

    /**
     * Get one pointVente by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public PointVente findByOne(Long id) {
        log.debug("Request to get PointVente : {}", id);
        PointVente pointVente = pointVenteRepository.findById(id).orElse(null);
        return (pointVente);
    }

    /**
     * Delete the pointVente by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete PointVente : {}", id);
        pointVenteRepository.deleteById(id);
    }


    /*
        L'unite d'organisation de l'utilisateur connecte
     */
    public PointVente getPointVenteOfUserConnecte() {
        User userConnecte = userService.getUserWithAuthorities();
        if (userConnecte != null) {
            PointVente pointVente = userConnecte.getPointVente();
            log.debug("Point de vente de l'utilisateur connecte!");
            if (pointVente != null) {
                return pointVente;
            }
        }
        return null;
    }

    public PointVenteDTO getCodePointVente(Long uoId) {
        UniteOrganisationDTO uniteOrganisationDTO = uniteOrganisationService.findOne(uoId);
        String pv = null;
        if (uniteOrganisationDTO != null) {
            String codeUO = uniteOrganisationDTO.getCodeUO();
            pv = codeUO + RandomStringUtils.randomNumeric(3);
            pv = pv.toUpperCase();
        }
        Long max = pointVenteRepository.getMaxId();
        String code;
        if (max > 0) {
            PointVente lastPointVente = pointVenteRepository.findById(max).orElse(null);
            Integer lastId = Integer.parseInt(lastPointVente.getCodePointVente().substring(3));
            code = String.format("%03d", lastId + 1);
            code = "PV" + code;
        } else {
            code = String.format("%03d", 1);
            code = "PV" + code;
        }
        PointVenteDTO pointVenteDTO = new PointVenteDTO();
        pointVenteDTO.setCodePointVente(pv);
        return pointVenteDTO;
    }

    /*
        Mise à jour des utilisateurs chargé de la gestion du point vente
     */
    public PointVenteDTO gestionUserPointVente(PointVenteUserDTO pointVenteUserDTO) {
        if (pointVenteUserDTO != null) {
            List<Long> userIds = pointVenteUserDTO.getUserIds();
            if (userIds.size() > 1) {
                throw new CustomParameterizedException("Un utilisateur par pointVente!");
            }
            PointVente pv = pointVenteRepository.findById(pointVenteUserDTO.getPointVenteId()).orElse(null);
            userService.findAllUserByPointVente(pointVenteUserDTO.getPointVenteId()).forEach(user -> {
                Long id = user.getId();
                if (!userIds.contains(id)) {
                    user.setPointVente(null);
                    userRepository.save(user);
                }
            });

            userIds.forEach(id -> {
                User user = userRepository.findById(id).orElse(null);
                log.debug("utilisateur du point de vente identifie ");
                user.setPointVente(pv);
                userRepository.save(user);
            });
            return pointVenteMapper.toDto(pv);
        }
        return null;
    }

    public PointVente getPointVenteOfUserConnecteOptimise(User userConnecte) {
        if (userConnecte != null) {
            PointVente pointVente = userConnecte.getPointVente();
            log.debug("Point de vente de l'utilisateur connecte!");
            if (pointVente != null) {
                return pointVente;
            }
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<PointVenteDTO> findAllByUOOptimise(Long uoId) {
        UniteOrganisationDTO uo = uniteOrganisationRepository.findDtoById(uoId);
        if (uo != null) {
            List<PointVenteDTO> pvs = new ArrayList<>();
            if (uo.getTypeUOLibelleTypeUO().equals(TypeUO.FSE.name())) {
                ArrayList<UniteOrganisationDTO> listAll = new ArrayList<>();
                ArrayList<UniteOrganisationDTO> listFils = new ArrayList<>();
                listAll.addAll(uniteOrganisationService.findAllUoFilsOptimise(uo, listAll, listFils));
                listAll.forEach(u -> {
                    pvs.addAll(findByUOOptimise(u.getId()));
                });
                return pvs;
            }
        }
        log.debug("Request to get all PointVentes");
        return pointVenteRepository.findByUniteOrganisationIdOptimise(uoId);
    }

    @Transactional(readOnly = true)
    public List<PointVenteDTO> findByUOOptimise(Long uoId) {
        log.debug("Request to get all PointVentes");
        return pointVenteRepository.findByUniteOrganisationIdOptimise(uoId);
    }

    public List<PointVente> findPointVenteByOrganisation(Long uoId) {
        log.debug("Request to get all PointVentes");

        List<PointVente> listPv = pointVenteRepository.findByUniteOrganisationId(uoId);
        if (listPv != null) {
            return listPv;
        }
        return null;
    }

    public List<Long> findAllIdOptimise() {
        return pointVenteRepository.findAllIdOptimise();
    }

}
