package com.m2i.omnicanal.application.service.impl;


import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.m2i.omnicanal.application.domain.ApplicationApi;
import com.m2i.omnicanal.application.domain.MethodeFonction;
import com.m2i.omnicanal.application.domain.ParametreMethod;
import com.m2i.omnicanal.application.repository.ApiRepository;
import com.m2i.omnicanal.application.service.ApiService;
import com.m2i.omnicanal.application.service.MethodeService;
import com.m2i.omnicanal.application.service.ParametreMethodeService;
import com.m2i.omnicanal.model.Api;
import com.m2i.omnicanal.model.Methode;
import com.m2i.omnicanal.model.ParametreMethode;

/**
 * Service Implementation for managing Api.
 */
@Service
@Transactional
public class ApiServiceImpl implements ApiService{

    private final Logger log = LoggerFactory.getLogger(ApiServiceImpl.class);
    
    private final ApiRepository apiRepository;

    private final MethodeService methodeService;
    private final ParametreMethodeService parametreMethodeService;

    public ApiServiceImpl(ApiRepository apiRepository,MethodeService methodeService,ParametreMethodeService parametreMethodeService) {
        this.apiRepository = apiRepository;
        this.methodeService = methodeService;
        this.parametreMethodeService = parametreMethodeService;
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
        ApplicationApi result = apiRepository.save(api);
        return result;
    }

    /**
     *  Get all the apis.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<Api> findAll() {
        log.debug("Request to get all Apis");
        
        
        List<Api> result = apiRepository.findAll();

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
    public Api findOne(Long id) {
        log.debug("Request to get Api : {}", id);
        Api api = apiRepository.findOne(id);
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
        apiRepository.deleteById(id);
    }

    @Override
    public Integer findMaxCodeApi() {
          log.debug("Request to selectMax Api : {}");
   
      Integer max= apiRepository.findMaxCodeApi();
      return max;
    }
    // Duplication de l'API avec les methodes et les parametres
    @Override
    @Transactional
    public ApplicationApi duplicateWithMethodsAndParams(Api originalApi, ApplicationApi newApi) {
        // Enregistrement de la nouvelle API
        ApplicationApi savedApi = save(newApi);
        
        // Duplication de toutes les methodes
        for (Methode originalMethod : originalApi.getConstitues()) {
            MethodeFonction newMethod = new MethodeFonction();
            BeanUtils.copyProperties(originalMethod, newMethod, "id", "api","codeMethode");
            Integer codeMtd = methodeService.findMaxByApi(savedApi.getId());
            String codeApi =  savedApi.getCodeApi();
            String strCodeMethode =codeApi+StringUtils.leftPad( codeMtd.toString(),2, "0");
            newMethod.setCodeMethode(strCodeMethode);
            newMethod.setApi(savedApi);
            
            // Enregistrement de la nouvelle methode
            Methode savedMethod = methodeService.save(newMethod);
            
            // duplication des parametres de la methode
            for (ParametreMethode originalParam : originalMethod.getPossedes()) {
                ParametreMethod newParam = new ParametreMethod();
                BeanUtils.copyProperties(originalParam, newParam, "id", "methode");
                newParam.setMethode(savedMethod);
                
                // Enregistrement du nouveau parametre
                parametreMethodeService.save(newParam);
            }
        }
        
        return savedApi;
    }
}
