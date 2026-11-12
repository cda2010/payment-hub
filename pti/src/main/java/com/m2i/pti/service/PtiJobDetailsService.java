package com.m2i.pti.service;

import com.m2i.pti.domain.PtiJobDetails;
import com.m2i.pti.repository.FournisseurServiceRepository;
import com.m2i.pti.repository.PtiJobDetailsRepository;
import com.m2i.pti.service.dto.PtiJobDetailsDTO;
import com.m2i.pti.service.mapper.PtiJobDetailsMapper;
import org.quartz.JobBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.quartz.*;  
import org.springframework.beans.factory.annotation.Autowired;  
import org.springframework.context.ApplicationContext;  
import org.springframework.data.domain.Page;  
import org.springframework.data.domain.Pageable;  
import org.springframework.scheduling.quartz.QuartzJobBean;  
import org.springframework.scheduling.quartz.SchedulerFactoryBean;  
import org.springframework.stereotype.Service;  
import org.springframework.transaction.annotation.Transactional;  
import  com.m2i.pti.scheduler.service.*;
import java.util.Date;
import java.util.List;
/**
 * Service Implementation for managing PtiJobDetails.
 */
@Service
@Transactional
public class PtiJobDetailsService {

    private final Logger log = LoggerFactory.getLogger(PtiJobDetailsService.class);

    private final PtiJobDetailsRepository ptiJobDetailsRepository;

    private final PtiJobDetailsMapper ptiJobDetailsMapper;
    private final FournisseurServiceRepository fournisseurServiceRepository;
   // @Autowired  
    private SchedulerFactoryBean schedulerFactoryBean;  
      @Autowired  
  private SchedulerService schedulerService;
  
  
    @Autowired  
    private ApplicationContext context;  
  
    //@Autowired  
    private JobScheduleCreator scheduleCreator;  
  
    public PtiJobDetailsService(PtiJobDetailsRepository ptiJobDetailsRepository, PtiJobDetailsMapper ptiJobDetailsMapper, FournisseurServiceRepository fournisseurServiceRepository) {
        this.ptiJobDetailsRepository = ptiJobDetailsRepository;
        this.ptiJobDetailsMapper = ptiJobDetailsMapper;
        this.fournisseurServiceRepository = fournisseurServiceRepository;
    }

    /**
     * Save a ptiJobDetails.
     *
     * @param ptiJobDetailsDTO the entity to save
     * @return the persisted entity
     */
    public PtiJobDetailsDTO save(PtiJobDetailsDTO ptiJobDetailsDTO) throws SchedulerException {
        log.debug("Request to save PtiJobDetails : {}", ptiJobDetailsDTO);
        PtiJobDetails ptiJobDetails = ptiJobDetailsMapper.toEntity(ptiJobDetailsDTO);
        if(ptiJobDetailsDTO.getFournisseurServiceId()!=null){
            ptiJobDetails.setFournisseurService(fournisseurServiceRepository.findById(ptiJobDetailsDTO.getFournisseurServiceId()).orElse(null));
        }
        ptiJobDetails = ptiJobDetailsRepository.save(ptiJobDetails);
        schedulerService.startAllSchedulers();
        return ptiJobDetailsMapper.toDto(ptiJobDetails);
    }
    
public PtiJobDetails toEntityMap(PtiJobDetailsDTO ptiJobDetailsDTO) {
       
        PtiJobDetails ptiJobDetails = ptiJobDetailsMapper.toEntity(ptiJobDetailsDTO);
        
        return ptiJobDetails;
    }
    /**
     *  Get all the ptiJobDetails.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<PtiJobDetailsDTO> findAll(Pageable pageable) {
        log.debug("Request to get all PtiJobDetails");
        return ptiJobDetailsRepository.findAll(pageable)
            .map(ptiJobDetailsMapper::toDto);
    }

    /**
     *  Get one ptiJobDetails by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public PtiJobDetailsDTO findOne(Long id) {
        log.debug("Request to get PtiJobDetails : {}", id);
        PtiJobDetails ptiJobDetails = ptiJobDetailsRepository.findById(id).orElse(null);
        return ptiJobDetailsMapper.toDto(ptiJobDetails);
    }

    /**
     *  Delete the  ptiJobDetails by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) throws SchedulerException {
        log.debug("Request to delete PtiJobDetails : {}", id);
        ptiJobDetailsRepository.deleteById(id);
        schedulerService.startAllSchedulers();
    }
  
   
  
    /** 
           * time out   
     * 
     * @param jobName  job name 
     * @param jobGroup job group 
     * @return 
     */  
   
   
  
    /** 
           * Recovery 
     * 
     * @param jobName  job name 
     * @param jobGroup job group 
     * @return 
     */  

  
     
}
