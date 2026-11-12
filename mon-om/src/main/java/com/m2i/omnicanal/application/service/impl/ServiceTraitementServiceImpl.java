package com.m2i.omnicanal.application.service.impl;


import com.m2i.omnicanal.application.domain.Service;
import com.m2i.omnicanal.application.repository.ServiceTraitementRepository;
import com.m2i.omnicanal.application.service.ServiceTraitementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

/**
 * Service Implementation for managing ServiceTraitement.
 */
@org.springframework.stereotype.Service
@Transactional
public class ServiceTraitementServiceImpl implements ServiceTraitementService{

    private final Logger log = LoggerFactory.getLogger(ServiceTraitementServiceImpl.class);
    
    private final ServiceTraitementRepository serviceTraitementRepository;

    public ServiceTraitementServiceImpl(ServiceTraitementRepository serviceTraitementRepository) {
        this.serviceTraitementRepository = serviceTraitementRepository;
    }

    /**
     * Save a serviceTraitement.
     *
     * @param serviceTraitement the entity to save
     * @return the persisted entity
     */
    @Override
    public Service save(Service serviceTraitement) {
        log.debug("Request to save ServiceTraitement : {}", serviceTraitement);
        Service result = serviceTraitementRepository.save(serviceTraitement);
        return result;
    }

    /**
     *  Get all the serviceTraitements.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<Service> findAll() {
        log.debug("Request to get all ServiceTraitements");
        List<Service> result = serviceTraitementRepository.findAll();

        return result;
    }

    /**
     *  Get one serviceTraitement by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public Service findOne(Long id) {
        log.debug("Request to get ServiceTraitement : {}", id);
        Service serviceTraitement = serviceTraitementRepository.findOne(id);
        return serviceTraitement;
    }

    /**
     *  Delete the  serviceTraitement by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete ServiceTraitement : {}", id);
        serviceTraitementRepository.deleteById(id);
    }
}
