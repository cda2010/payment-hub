package com.m2i.pti.service;

import com.m2i.pti.domain.DetailReleveCompte;
import com.m2i.pti.repository.DetailReleveCompteRepository;
import com.m2i.pti.service.dto.DetailReleveCompteDTO;
import com.m2i.pti.service.mapper.DetailReleveCompteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


/**
 * Service Implementation for managing DetailReleveCompte.
 */
@Service
@Transactional
public class DetailReleveCompteService {

    private final Logger log = LoggerFactory.getLogger(DetailReleveCompteService.class);

    private final DetailReleveCompteRepository detailReleveCompteRepository;

    private final DetailReleveCompteMapper detailReleveCompteMapper;

    public DetailReleveCompteService(DetailReleveCompteRepository detailReleveCompteRepository, DetailReleveCompteMapper detailReleveCompteMapper) {
        this.detailReleveCompteRepository = detailReleveCompteRepository;
        this.detailReleveCompteMapper = detailReleveCompteMapper;
    }

    /**
     * Save a detailReleveCompte.
     *
     * @param detailReleveCompteDTO the entity to save
     * @return the persisted entity
     */
    public DetailReleveCompteDTO save(DetailReleveCompteDTO detailReleveCompteDTO) {
        log.debug("Request to save DetailReleveCompte : {}", detailReleveCompteDTO);
        DetailReleveCompte detailReleveCompte = detailReleveCompteMapper.toEntity(detailReleveCompteDTO);
        detailReleveCompte = detailReleveCompteRepository.save(detailReleveCompte);
        return detailReleveCompteMapper.toDto(detailReleveCompte);
    }

    /**
     *  Get all the detailReleveComptes.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<DetailReleveCompteDTO> findAll(Pageable pageable) {
        log.debug("Request to get all DetailReleveComptes");
        return detailReleveCompteRepository.findAll(pageable)
            .map(detailReleveCompteMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<DetailReleveCompteDTO> findByReleve(Long releveId) {
            List<DetailReleveCompte> detailReleveComptes=detailReleveCompteRepository.findAllByReleveId(releveId);
            List<DetailReleveCompteDTO> detailReleveCompteDTOS=new ArrayList<>();
            if(detailReleveComptes!=null){
                detailReleveComptes.forEach(detailReleveCompte -> {
                    detailReleveCompteDTOS.add(detailReleveCompteMapper.toDto(detailReleveCompte));
                });
            }
            return detailReleveCompteDTOS;
    }

    /**
     *  Get one detailReleveCompte by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public DetailReleveCompteDTO findOne(Long id) {
        log.debug("Request to get DetailReleveCompte : {}", id);
        DetailReleveCompte detailReleveCompte = detailReleveCompteRepository.findById(id).orElse(null);
        return detailReleveCompteMapper.toDto(detailReleveCompte);
    }

    /**
     *  Delete the  detailReleveCompte by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete DetailReleveCompte : {}", id);
        detailReleveCompteRepository.deleteById(id);
    }
}
