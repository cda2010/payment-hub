package com.m2i.omnicanal.application.service.impl;

import com.m2i.omnicanal.application.domain.ServiceMethod;
import com.m2i.omnicanal.application.repository.ServiceMethodeRepository;
import com.m2i.omnicanal.application.service.ServiceMethodeService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing ServiceMethode.
 */
@Service
@Transactional
public class ServiceMethodeServiceImpl implements ServiceMethodeService{

    private final Logger log = LoggerFactory.getLogger(ServiceMethodeServiceImpl.class);
    
    private final ServiceMethodeRepository serviceMethodeRepository;

    public ServiceMethodeServiceImpl(ServiceMethodeRepository serviceMethodeRepository) {
        this.serviceMethodeRepository = serviceMethodeRepository;
    }

    /**
     * Save a serviceMethode.
     *
     * @param serviceMethode the entity to save
     * @return the persisted entity
     */
    @Override
    public ServiceMethod save(ServiceMethod serviceMethode) {
        log.debug("Request to save ServiceMethode : {}", serviceMethode);
        ServiceMethod result = serviceMethodeRepository.save(serviceMethode);
        return result;
    }

    /**
     *  Get all the serviceMethodes.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<ServiceMethod> findAll() {
        log.debug("Request to get all ServiceMethodes");
        List<ServiceMethod> result = serviceMethodeRepository.findAll();

        return result;
    }

    /**
     *  Get one serviceMethode by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public ServiceMethod findOne(Long id) {
        log.debug("Request to get ServiceMethode : {}", id);
        ServiceMethod serviceMethode = serviceMethodeRepository.findOne(id);
        return serviceMethode;
    }

    /**
     *  Delete the  serviceMethode by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete ServiceMethode : {}", id);
        serviceMethodeRepository.deleteById(id);
    }

    @Override
    public List<ServiceMethod> findByService(Long idService) {
       List<ServiceMethod> result =  serviceMethodeRepository.findByService(idService);
       return result;
    }

   
}
