package com.m2i.omnicanal.presentation.service.impl;

import com.m2i.omnicanal.model.Api;
import com.m2i.omnicanal.model.Methode;
import com.m2i.omnicanal.presentation.domain.ApplicationApi;
import com.m2i.omnicanal.presentation.domain.MethodeFonction;
import com.m2i.omnicanal.presentation.repository.ApiRepository;
import com.m2i.omnicanal.presentation.service.ApiService;
import com.m2i.omnicanal.presentation.service.HazelcastConfigCacheService;
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
public class ApiServiceImpl implements ApiService {

    private final Logger log = LoggerFactory.getLogger(ApiServiceImpl.class);

    private final ApiRepository apiRepository;
    private final HazelcastConfigCacheService hazelcastConfigCacheService;

    public ApiServiceImpl(ApiRepository apiRepository, HazelcastConfigCacheService hazelcastConfigCacheService) {
        this.apiRepository = apiRepository;
        this.hazelcastConfigCacheService = hazelcastConfigCacheService;
    }

    /**
     * Save a api.
     *
     * @param api the entity to save
     * @return the persisted entity
     */
    @Override
    public ApplicationApi save(ApplicationApi api) {
        log.debug("Request to save Api : {}", api);
         if (api.getId() != null) {
            for (Methode methode : api.getConstitues()) {
                hazelcastConfigCacheService.deleteValue(methode.getCodeMethode());
                hazelcastConfigCacheService.deleteValue(methode.getTypeMethode().name() + api.getFournisseurService().getCodeFse());
            }
        }
        ApplicationApi result = apiRepository.save(api);
       

        return result;
    }

    /**
     * Get all the apis.
     *
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<ApplicationApi> findAll() {
        log.debug("Request to get all Apis");

        List<ApplicationApi> result = apiRepository.findAll();

        return result;
    }

    /**
     * Get one api by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public ApplicationApi findOne(Long id) {
        log.debug("Request to get Api : {}", id);
        ApplicationApi api = apiRepository.findOne(id);
        return api;
    }

    /**
     * Delete the api by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Api : {}", id);
        apiRepository.deleteById(id);
    }
}
