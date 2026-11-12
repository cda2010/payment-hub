package com.m2i.pti.service;

import com.m2i.pti.domain.Devise;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.UoDevise;
import com.m2i.pti.domain.enumeration.TypeUO;
import com.m2i.pti.repository.UniteOrganisationRepository;
import com.m2i.pti.repository.UoDeviseRepository;
import com.m2i.pti.service.dto.UoDeviseDTO;
import com.m2i.pti.service.mapper.UoDeviseMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Service Implementation for managing UoDevise.
 */
@Service
@Transactional
public class UoDeviseService {

    private final Logger log = LoggerFactory.getLogger(UoDeviseService.class);

    private final UoDeviseRepository uoDeviseRepository;

    private final UoDeviseMapper uoDeviseMapper;

    private final UniteOrganisationService uniteOrganisationService;

    private final UniteOrganisationRepository uniteOrganisationRepository;

    public UoDeviseService(UoDeviseRepository uoDeviseRepository, UoDeviseMapper uoDeviseMapper, UniteOrganisationService uniteOrganisationService, UniteOrganisationRepository uniteOrganisationRepository) {
        this.uoDeviseRepository = uoDeviseRepository;
        this.uoDeviseMapper = uoDeviseMapper;
        this.uniteOrganisationService = uniteOrganisationService;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
    }

    /**
     * Save a uoDevise.
     *
     * @param uoDeviseDTO the entity to save
     * @return the persisted entity
     */
    public UoDeviseDTO save(UoDeviseDTO uoDeviseDTO) {
        UoDevise uoDevise = uoDeviseMapper.toEntity(uoDeviseDTO);
        verifDefaultDevise(uoDeviseDTO);
        verifUnicite(uoDeviseDTO);
        uoDevise = uoDeviseRepository.save(uoDevise);
        return uoDeviseMapper.toDto(uoDevise);
    }

    private void verifUnicite(UoDeviseDTO uoDeviseDTO) {
        UoDevise uoDeviseExist = uoDeviseRepository.findByUniteOrganisationIdAndDeviseId(uoDeviseDTO.getUniteOrganisationId(), uoDeviseDTO.getDeviseId());
        if (uoDeviseExist != null) {
            if (uoDeviseDTO.getId() == null) {
                throw new CustomParameterizedException("Ce pays est dejà configuré avec cette devise!");
            } else {
                UoDevise oldUoDevise = uoDeviseRepository.findById(uoDeviseDTO.getId()).orElse(null);
                if (uoDeviseDTO.getDeviseId() != oldUoDevise.getDevise().getId()
                        || uoDeviseDTO.getUniteOrganisationId() != oldUoDevise.getUniteOrganisation().getId()) {
                    throw new CustomParameterizedException("Ce pays est dejà configuré avec cette devise!");
                }
            }

        }
    }

    public void verifDefaultDevise(UoDeviseDTO uoDeviseDTO) {
        UoDevise uoDeviseExist = uoDeviseRepository.findByUniteOrganisationIdAndDefaultDevise(uoDeviseDTO.getUniteOrganisationId(), Boolean.TRUE);
        if (uoDeviseExist != null) {
            if (uoDeviseDTO.getId() == null) {
                if (uoDeviseDTO.getDefaultDevise() == Boolean.TRUE)
                    throw new CustomParameterizedException("Ce pays dispose dejà d'une devise par defaut !");
            } else {
                UoDevise oldUoDevise = uoDeviseRepository.findById(uoDeviseDTO.getId()).orElse(null);
                if ((uoDeviseDTO.getDefaultDevise() != oldUoDevise.getDefaultDevise())
                        || uoDeviseDTO.getUniteOrganisationId() != oldUoDevise.getUniteOrganisation().getId()) {
                    if (uoDeviseDTO.getDefaultDevise() == Boolean.TRUE)
                        throw new CustomParameterizedException("Ce pays dispose dejà d'une devise par defaut !");
                }
            }
        }
    }

    public List<UoDeviseDTO> findByActeur(Long uoId) {
        UniteOrganisation uo = uniteOrganisationRepository.findById(uoId).orElse(null);
        log.debug("DefaultByOrganisation" + uoId);
        if (!uo.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.PAYS))) {
            UniteOrganisation uoPays = uniteOrganisationService.findUoParentByType(uo, TypeUO.PAYS);
            return uoDeviseMapper.toDto(uoDeviseRepository.findByUniteOrganisationId(uoPays.getId()));
        }
        return uoDeviseMapper.toDto(uoDeviseRepository.findByUniteOrganisationId(uoId));
    }

    /**
     * Default devise of Organisation Pays
     *
     * @param uoId
     * @return
     */
    public UoDeviseDTO findByActeurDefault(Long uoId) {
        UniteOrganisation uo = uniteOrganisationRepository.findById(uoId).orElse(null);
        log.debug("DefaultByOrganisation" + uoId);
        if (!uo.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.PAYS))) {
            UniteOrganisation uoPays = uniteOrganisationService.findUoParentByType(uo, TypeUO.PAYS);
            return uoDeviseMapper.toDto(uoDeviseRepository.findByUniteOrganisationIdAndDefaultDevise(uoPays.getId(), Boolean.TRUE));
        }
        return uoDeviseMapper.toDto(uoDeviseRepository.findByUniteOrganisationIdAndDefaultDevise(uoId, Boolean.TRUE));
    }

    /**
     * Recherche de la devise par defaut de l'organisation connecté à travers le pays de son reseau
     *
     * @return
     */
    public UoDeviseDTO findByActeurConnecte() {
        UniteOrganisation uoConnecte = uniteOrganisationService.getUoOfUserConnecte();
        UniteOrganisation uoPays = uniteOrganisationService.findUoParentByType(uoConnecte, TypeUO.PAYS);
        if (uoPays != null) {
            UoDeviseDTO uoDevise = this.findByActeurDefault(uoPays.getId());
            if (uoDevise != null) {
                return uoDevise;
            } else throw new CustomParameterizedException("La devise par defaut de votre pays n'est pas configure!");
        } else {
            throw new CustomParameterizedException("Le pays de votre organisation n'est pas configure !");
        }

    }


    /**
     * Get all the uoDevises.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<UoDeviseDTO> findAll(Pageable pageable) {
        log.debug("Request to get all UoDevises");
        return uoDeviseRepository.findAll(pageable)
                .map(uoDeviseMapper::toDto);
    }

    /**
     * Get one uoDevise by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public UoDeviseDTO findOne(Long id) {
        log.debug("Request to get UoDevise : {}", id);
        UoDevise uoDevise = uoDeviseRepository.findById(id).orElse(null);
        return uoDeviseMapper.toDto(uoDevise);
    }

    /**
     * Delete the  uoDevise by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete UoDevise : {}", id);
        uoDeviseRepository.deleteById(id);
    }
}
