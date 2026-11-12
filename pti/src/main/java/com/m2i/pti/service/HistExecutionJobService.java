package com.m2i.pti.service;

import com.m2i.pti.domain.HistExecutionJob;
import com.m2i.pti.repository.HistExecutionJobRepository;
import com.m2i.pti.service.dto.HistExecutionJobDTO;
import com.m2i.pti.service.mapper.HistExecutionJobMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing HistExecutionJob.
 */
@Service
@Transactional
public class HistExecutionJobService {

    private final Logger log = LoggerFactory.getLogger(HistExecutionJobService.class);

    private final HistExecutionJobRepository histExecutionJobRepository;

    private final HistExecutionJobMapper histExecutionJobMapper;

    public HistExecutionJobService(HistExecutionJobRepository histExecutionJobRepository, HistExecutionJobMapper histExecutionJobMapper) {
        this.histExecutionJobRepository = histExecutionJobRepository;
        this.histExecutionJobMapper = histExecutionJobMapper;
    }

    /**
     * Save a histExecutionJob.
     *
     * @param histExecutionJobDTO the entity to save
     * @return the persisted entity
     */
    public HistExecutionJobDTO save(HistExecutionJobDTO histExecutionJobDTO) {
        log.debug("Request to save HistExecutionJob : {}", histExecutionJobDTO);
        HistExecutionJob histExecutionJob = histExecutionJobMapper.toEntity(histExecutionJobDTO);
        histExecutionJob = histExecutionJobRepository.save(histExecutionJob);
        return histExecutionJobMapper.toDto(histExecutionJob);
    }

     public HistExecutionJob save(HistExecutionJob histExecutionJob) {
        histExecutionJob = histExecutionJobRepository.save(histExecutionJob);
        return histExecutionJob;
    }
    /**
     *  Get all the histExecutionJobs.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<HistExecutionJobDTO> findAll(Pageable pageable) {
        log.debug("Request to get all HistExecutionJobs");
        return histExecutionJobRepository.findAll(pageable)
            .map(histExecutionJobMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<HistExecutionJobDTO> findAllByPtiJobDetail(Long id,Pageable pageable) {
        log.debug("Request to get all HistExecutionJobs");
        return histExecutionJobRepository.findAllByPtiJobDetailId(id,pageable)
                .map(histExecutionJobMapper::toDto);
    }

    /**
     *  Get one histExecutionJob by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public HistExecutionJobDTO findOne(Long id) {
        log.debug("Request to get HistExecutionJob : {}", id);
        HistExecutionJob histExecutionJob = histExecutionJobRepository.findById(id).orElse(null);
        return histExecutionJobMapper.toDto(histExecutionJob);
    }

    /**
     *  Delete the  histExecutionJob by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete HistExecutionJob : {}", id);
        histExecutionJobRepository.deleteById(id);
    }
}
