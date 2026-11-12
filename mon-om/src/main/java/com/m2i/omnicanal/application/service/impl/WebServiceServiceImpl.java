package com.m2i.omnicanal.application.service.impl;


import com.m2i.omnicanal.model.Api;
import com.m2i.omnicanal.application.domain.ApplicationApi;
import com.m2i.omnicanal.application.domain.RestUser;
import com.m2i.omnicanal.application.domain.WebServiceUser;
import com.m2i.omnicanal.application.repository.ApiRepository;
import com.m2i.omnicanal.application.repository.RestRepository;
import com.m2i.omnicanal.application.repository.WebServiceRepository;
import com.m2i.omnicanal.application.service.ApiService;
import com.m2i.omnicanal.application.service.RestService;
import com.m2i.omnicanal.application.service.WebServiceService;
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
public class WebServiceServiceImpl implements WebServiceService{

    private final Logger log = LoggerFactory.getLogger(WebServiceServiceImpl.class);
   
    private final WebServiceRepository webServiceRepository;

    public WebServiceServiceImpl(WebServiceRepository webServiceRepository) {
        this.webServiceRepository = webServiceRepository;
    }

    /**
     * Save a api.
     *
     * @param restUser
     * @param api the entity to save
     * @return the persisted entity
     */
    @Override
    public WebServiceUser save(WebServiceUser webServiceUser) {
        log.debug("Request to save Api : {}", webServiceUser);
        WebServiceUser result = webServiceRepository.save(webServiceUser);
        return result;
    }

    /**
     *  Get all the apis.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<WebServiceUser> findAll() {
        log.debug("Request to get all Apis");
        
        
        List<WebServiceUser> result = webServiceRepository.findAll();

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
    public WebServiceUser findOne(Long id) {
        log.debug("Request to get Api : {}", id);
        WebServiceUser api = webServiceRepository.findOne(id);
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
        webServiceRepository.deleteById(id);
    }
}
