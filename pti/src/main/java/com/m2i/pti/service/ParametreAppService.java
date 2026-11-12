package com.m2i.pti.service;

import com.m2i.pti.domain.ParametreApp;
import com.m2i.pti.domain.enumeration.TypeParametre;
import com.m2i.pti.repository.ParametreAppRepository;
import com.m2i.pti.service.dto.ParametreAppDTO;
import com.m2i.pti.service.mapper.ParametreAppMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing ParametreApp.
 */
@Service
@Transactional
public class ParametreAppService {

    private final Logger log = LoggerFactory.getLogger(ParametreAppService.class);

    private ParametreAppRepository parametreAppRepository;

    private ParametreAppMapper parametreAppMapper;

    public ParametreAppService(ParametreAppRepository parametreAppRepository, ParametreAppMapper parametreAppMapper) {
        this.parametreAppRepository = parametreAppRepository;
        this.parametreAppMapper = parametreAppMapper;
    }

    /**
     * Save a parametreApp.
     *
     * @param parametreAppDTO the entity to save
     * @return the persisted entity
     */
    public ParametreAppDTO save(ParametreAppDTO parametreAppDTO) {
        log.debug("Request to save ParametreApp : {}", parametreAppDTO);

        ParametreApp parametreApp = parametreAppMapper.toEntity(parametreAppDTO);
        parametreApp = parametreAppRepository.save(parametreApp);
        return parametreAppMapper.toDto(parametreApp);
    }

    /**
     * Get all the parametreApps.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<ParametreAppDTO> findAll(Pageable pageable) {
        log.debug("Request to get all ParametreApps");
        return parametreAppRepository.findAllOrderbyIdDesc(pageable)
                .map(parametreAppMapper::toDto);
    }


    /**
     * Get one parametreApp by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public ParametreAppDTO findOne(Long id) {
        log.debug("Request to get ParametreApp : {}", id);
        return parametreAppMapper.toDto(parametreAppRepository.findById(id).orElse(null));
    }

    /**
     * Delete the parametreApp by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete ParametreApp : {}", id);
        parametreAppRepository.deleteById(id);
    }
     @Transactional(readOnly = true)
    public ParametreApp findByTypeAndNom(TypeParametre typeParametre,  String nom) {
      ParametreApp param=   parametreAppRepository.findByTypeAndNom(typeParametre, nom);
        return param;
    }
     @Transactional(readOnly = true)
    public ParametreApp findByNomAndCodeFSE( String nom, String codeFSE) {
        return parametreAppRepository.findByTypeAndNomAndCodeFse(nom,codeFSE);
    }

}
