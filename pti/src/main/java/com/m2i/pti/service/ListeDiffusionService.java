package com.m2i.pti.service;

import com.m2i.pti.domain.ListeDiffusion;
import com.m2i.pti.repository.DetailListeDiffusionRepository;
import com.m2i.pti.repository.ListeDiffusionRepository;
import com.m2i.pti.service.dto.DetailListeDiffusionDTO;
import com.m2i.pti.service.dto.ListeDiffusionDTO;
import com.m2i.pti.service.mapper.ListeDiffusionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing ListeDiffusion.
 */
@Service
@Transactional
public class ListeDiffusionService {

    private final Logger log = LoggerFactory.getLogger(ListeDiffusionService.class);

    private final ListeDiffusionRepository listeDiffusionRepository;

    private final ListeDiffusionMapper listeDiffusionMapper;
    private  final DetailListeDiffusionRepository detailListeDiffusionRepository;
    private  final DetailListeDiffusionService detailListeDiffusionService;

    public ListeDiffusionService(ListeDiffusionRepository listeDiffusionRepository, ListeDiffusionMapper listeDiffusionMapper, DetailListeDiffusionRepository detailListeDiffusionRepository, DetailListeDiffusionService detailListeDiffusionService) {
        this.listeDiffusionRepository = listeDiffusionRepository;
        this.listeDiffusionMapper = listeDiffusionMapper;
        this.detailListeDiffusionRepository = detailListeDiffusionRepository;
        this.detailListeDiffusionService = detailListeDiffusionService;
    }

    /**
     * Save a listeDiffusion.
     *
     * @param listeDiffusionDTO the entity to save
     * @return the persisted entity
     */
    public ListeDiffusionDTO save(ListeDiffusionDTO listeDiffusionDTO) {
        log.debug("Request to save ListeDiffusion : {}", listeDiffusionDTO);
        ListeDiffusion listeDiffusion = listeDiffusionMapper.toEntity(listeDiffusionDTO);
        listeDiffusion = listeDiffusionRepository.save(listeDiffusion);
        if(listeDiffusion.getId()!=null&&listeDiffusionDTO.getContacts().length>0){
            detailListeDiffusionRepository.deleteDetailListeDiffusionByListeId(listeDiffusion.getId());
            for(DetailListeDiffusionDTO item:listeDiffusionDTO.getContacts()){
                item.setListeId(listeDiffusion.getId());
                detailListeDiffusionService.save(item);
            }
        }
        return listeDiffusionMapper.toDto(listeDiffusion);
    }

    /**
     *  Get all the listeDiffusions.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<ListeDiffusionDTO> findAll(Pageable pageable) {
        log.debug("Request to get all ListeDiffusions");
        return listeDiffusionRepository.findAll(pageable)
            .map(listeDiffusionMapper::toDto);
    }

    /**
     *  Get one listeDiffusion by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public ListeDiffusionDTO findOne(Long id) {
        log.debug("Request to get ListeDiffusion : {}", id);
        ListeDiffusion listeDiffusion = listeDiffusionRepository.findById(id).orElse(null);
        return listeDiffusionMapper.toDto(listeDiffusion);
    }

    /**
     *  Delete the  listeDiffusion by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete ListeDiffusion : {}", id);
        listeDiffusionRepository.deleteById(id);
    }
}
