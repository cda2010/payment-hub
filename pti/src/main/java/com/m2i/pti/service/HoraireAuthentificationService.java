package com.m2i.pti.service;

import com.m2i.pti.domain.HoraireAuthentification;
import com.m2i.pti.domain.enumeration.Jour;
import com.m2i.pti.repository.HoraireAuthentificationRepository;
import com.m2i.pti.service.dto.HoraireAuthentificationDTO;
import com.m2i.pti.service.mapper.HoraireAuthentificationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Service Implementation for managing HoraireAuthentification.
 */
@Service
@Transactional
public class HoraireAuthentificationService {

    private final Logger log = LoggerFactory.getLogger(HoraireAuthentificationService.class);

    private final HoraireAuthentificationRepository horaireAuthentificationRepository;

    private final HoraireAuthentificationMapper horaireAuthentificationMapper;

    public HoraireAuthentificationService(HoraireAuthentificationRepository horaireAuthentificationRepository, HoraireAuthentificationMapper horaireAuthentificationMapper) {
        this.horaireAuthentificationRepository = horaireAuthentificationRepository;
        this.horaireAuthentificationMapper = horaireAuthentificationMapper;
    }

    /**
     * Save a horaireAuthentification.
     *
     * @param horaireAuthentificationDTO the entity to save
     * @return the persisted entity
     */
    public HoraireAuthentificationDTO save(HoraireAuthentificationDTO horaireAuthentificationDTO) {
        log.debug("Request to save HoraireAuthentification : {}", horaireAuthentificationDTO);
        HoraireAuthentification horaireAuthentification = horaireAuthentificationMapper.toEntity(horaireAuthentificationDTO);
        horaireAuthentification = horaireAuthentificationRepository.save(horaireAuthentification);
        return horaireAuthentificationMapper.toDto(horaireAuthentification);
    }

    /**
     *  Get all the horaireAuthentifications.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<HoraireAuthentificationDTO> findAll(Pageable pageable) {
        log.debug("Request to get all HoraireAuthentifications");
        return horaireAuthentificationRepository.findAll(pageable)
            .map(horaireAuthentificationMapper::toDto);
    }


    public List<HoraireAuthentificationDTO> findAll(){
        return horaireAuthentificationMapper.toDto(horaireAuthentificationRepository.findAll());
    }



    @Transactional(readOnly = true)
    public List<HoraireAuthentification> findAllBy(Jour jour,Long profilId) {
        return horaireAuthentificationRepository.findByMultiParamsWithProfil(jour,profilId);
    }

    /**
     *  Get one horaireAuthentification by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public HoraireAuthentificationDTO findOne(Long id) {
        log.debug("Request to get HoraireAuthentification : {}", id);
        HoraireAuthentification horaireAuthentification = horaireAuthentificationRepository.findById(id).orElse(null);
        return horaireAuthentificationMapper.toDto(horaireAuthentification);
    }

    /**
     *  Delete the  horaireAuthentification by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete HoraireAuthentification : {}", id);
        horaireAuthentificationRepository.deleteById(id);
    }
}
