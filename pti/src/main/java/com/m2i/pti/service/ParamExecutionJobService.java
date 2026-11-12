package com.m2i.pti.service;

import com.m2i.pti.domain.ParamExecutionJob;
import com.m2i.pti.domain.PtiJobDetails;
import com.m2i.pti.repository.ParamExecutionJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing ParamExecutionJob.
 */
@Service
@Transactional
public class ParamExecutionJobService {

    private final Logger log = LoggerFactory.getLogger(ParamExecutionJobService.class);

    private final ParamExecutionJobRepository paramExecutionJobRepository;

    public ParamExecutionJobService(ParamExecutionJobRepository paramExecutionJobRepository) {
        this.paramExecutionJobRepository = paramExecutionJobRepository;
    }

    /**
     * Save a paramExecutionJob.
     *
     * @param paramExecutionJob the entity to save
     * @return the persisted entity
     */
    public ParamExecutionJob save(ParamExecutionJob paramExecutionJob) {
        log.debug("Request to save ParamExecutionJob : {}", paramExecutionJob);
        return paramExecutionJobRepository.save(paramExecutionJob);
    }

    /**
     *  Get all the paramExecutionJobs.
     *
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<ParamExecutionJob> findAll() {
        log.debug("Request to get all ParamExecutionJobs");
        return paramExecutionJobRepository.findAll();
    }

    /**
     *  Get one paramExecutionJob by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public ParamExecutionJob findOne(Long id) {
        log.debug("Request to get ParamExecutionJob : {}", id);
        return paramExecutionJobRepository.findById(id).orElse(null);
    }

    /**
     *  Delete the  paramExecutionJob by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete ParamExecutionJob : {}", id);
        paramExecutionJobRepository.deleteById(id);
    }

    public List<ParamExecutionJob> findByPtiJobDetails(PtiJobDetails jobInfo) {
     List<ParamExecutionJob>  resul=    paramExecutionJobRepository.queryByPtiJobDetail(jobInfo.getId());
     return resul;
    }
}
