package com.m2i.pti.service;

import com.m2i.pti.domain.GroupRepartition;
import com.m2i.pti.repository.GroupRepartitionRepository;
import com.m2i.pti.service.dto.GroupRepartitionDTO;
import com.m2i.pti.service.mapper.GroupRepartitionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Service Implementation for managing GroupRepartition.
 */
@Service
@Transactional
public class GroupRepartitionService {

    private final Logger log = LoggerFactory.getLogger(GroupRepartitionService.class);

    private final GroupRepartitionRepository groupRepartitionRepository;

    private final GroupRepartitionMapper groupRepartitionMapper;

    public GroupRepartitionService(GroupRepartitionRepository groupRepartitionRepository, GroupRepartitionMapper groupRepartitionMapper) {
        this.groupRepartitionRepository = groupRepartitionRepository;
        this.groupRepartitionMapper = groupRepartitionMapper;
    }

    /**
     * Save a groupRepartition.
     *
     * @param groupRepartitionDTO the entity to save
     * @return the persisted entity
     */
    public GroupRepartitionDTO save(GroupRepartitionDTO groupRepartitionDTO) {
        log.debug("Request to save GroupRepartition : {}", groupRepartitionDTO);
        GroupRepartition groupRepartition = groupRepartitionMapper.toEntity(groupRepartitionDTO);
        groupRepartition = groupRepartitionRepository.save(groupRepartition);
        return groupRepartitionMapper.toDto(groupRepartition);
    }

    /**
     *  Get all the groupRepartitions.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<GroupRepartitionDTO> findAll(Pageable pageable) {
        log.debug("Request to get all GroupRepartitions");
        return groupRepartitionRepository.findAll(pageable)
            .map(groupRepartitionMapper::toDto);
    }
    @Transactional(readOnly = true)
    public List<GroupRepartition> findAll() {
        log.debug("Request to get all GroupRepartitions");
        return groupRepartitionRepository.findAll();
    }

    /**
     *  Get one groupRepartition by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public GroupRepartitionDTO findOne(Long id) {
        log.debug("Request to get GroupRepartition : {}", id);
        GroupRepartition groupRepartition = groupRepartitionRepository.findById(id).orElse(null);
        return groupRepartitionMapper.toDto(groupRepartition);
    }

    /**
     *  Delete the  groupRepartition by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete GroupRepartition : {}", id);
        groupRepartitionRepository.deleteById(id);
    }
}
