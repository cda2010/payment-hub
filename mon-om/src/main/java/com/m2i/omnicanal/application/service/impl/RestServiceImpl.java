package com.m2i.omnicanal.application.service.impl;


import com.m2i.omnicanal.model.Api;
import com.m2i.omnicanal.application.domain.ApplicationApi;
import com.m2i.omnicanal.application.domain.RestUser;
import com.m2i.omnicanal.application.repository.ApiRepository;
import com.m2i.omnicanal.application.repository.RestRepository;
import com.m2i.omnicanal.application.service.ApiService;
import com.m2i.omnicanal.application.service.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing Api.
 */
@Service
@Transactional
public class RestServiceImpl implements RestService{

    private final Logger log = LoggerFactory.getLogger(RestServiceImpl.class);
   
    private final RestRepository restRepository;

    public RestServiceImpl(RestRepository restRepository) {
        this.restRepository = restRepository;
    }

    /**
     * Save a api.
     *
     * @param restUser
     * @param api the entity to save
     * @return the persisted entity
     */
    @Override
    public RestUser save(RestUser restUser) {
        log.debug("Request to save Api : {}", restUser);
        RestUser result = restRepository.save(restUser);
        return result;
    }

    /**
     *  Get all the apis.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<RestUser> findAll() {
        log.debug("Request to get all Apis");
        
        
        List<RestUser> result = restRepository.findAll();

        return result;
    }

    /**
     *  Get one api by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public RestUser findOne(Long id) {
        log.debug("Request to get Api : {}", id);
        RestUser api = restRepository.findOne(id);
        return api;
    }

    /**
     *  Delete the  api by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Api : {}", id);
        restRepository.deleteById(id);
    }
}
