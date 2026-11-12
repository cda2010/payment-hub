package com.m2i.pti.service;

import com.m2i.pti.domain.DetailListeDiffusion;
import com.m2i.pti.repository.DetailListeDiffusionRepository;
import com.m2i.pti.service.dto.DetailListeDiffusionDTO;
import com.m2i.pti.service.mapper.DetailListeDiffusionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


/**
 * Service Implementation for managing DetailListeDiffusion.
 */
@Service
@Transactional
public class DetailListeDiffusionService {

    private final Logger log = LoggerFactory.getLogger(DetailListeDiffusionService.class);

    private final DetailListeDiffusionRepository detailListeDiffusionRepository;

    private final DetailListeDiffusionMapper detailListeDiffusionMapper;

    public DetailListeDiffusionService(DetailListeDiffusionRepository detailListeDiffusionRepository, DetailListeDiffusionMapper detailListeDiffusionMapper) {
        this.detailListeDiffusionRepository = detailListeDiffusionRepository;
        this.detailListeDiffusionMapper = detailListeDiffusionMapper;
    }

    /**
     * Save a detailListeDiffusion.
     *
     * @param detailListeDiffusionDTO the entity to save
     * @return the persisted entity
     */
    public DetailListeDiffusionDTO save(DetailListeDiffusionDTO detailListeDiffusionDTO) {
        log.debug("Request to save DetailListeDiffusion : {}", detailListeDiffusionDTO);
        DetailListeDiffusion detailListeDiffusion = detailListeDiffusionMapper.toEntity(detailListeDiffusionDTO);
        detailListeDiffusion = detailListeDiffusionRepository.save(detailListeDiffusion);
        return detailListeDiffusionMapper.toDto(detailListeDiffusion);
    }

    /**
     *  Get all the detailListeDiffusions.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<DetailListeDiffusionDTO> findAll(Pageable pageable) {
        log.debug("Request to get all DetailListeDiffusions");
        return detailListeDiffusionRepository.findAll(pageable)
            .map(detailListeDiffusionMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<DetailListeDiffusionDTO> findDetailOfList(Long id) {
        log.debug("Request to get all DetailListeDiffusions");
        List<DetailListeDiffusion> details=detailListeDiffusionRepository.findDetailListeDiffusionByListeId(id);
        if(details.size()>0){
            List<DetailListeDiffusionDTO> retour=new ArrayList<DetailListeDiffusionDTO>();
            for (DetailListeDiffusion item:details){
                retour.add(detailListeDiffusionMapper.toDto(item));
            }
            return retour;
        }
        return null;
    }

    /**
     *  Get one detailListeDiffusion by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public DetailListeDiffusionDTO findOne(Long id) {
        log.debug("Request to get DetailListeDiffusion : {}", id);
        DetailListeDiffusion detailListeDiffusion = detailListeDiffusionRepository.findById(id).orElse(null);
        return detailListeDiffusionMapper.toDto(detailListeDiffusion);
    }

    /**
     *  Delete the  detailListeDiffusion by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete DetailListeDiffusion : {}", id);
        detailListeDiffusionRepository.deleteById(id);
    }
}
