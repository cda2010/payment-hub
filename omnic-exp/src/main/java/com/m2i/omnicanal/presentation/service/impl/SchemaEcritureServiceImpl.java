package com.m2i.omnicanal.presentation.service.impl;

import com.m2i.omnicanal.model.SchemaEcriture;
import com.m2i.omnicanal.model.TypeService;
import com.m2i.omnicanal.presentation.domain.MethodeFonction;

import com.m2i.omnicanal.presentation.domain.SchemaEcrit;
import com.m2i.omnicanal.presentation.domain.util.CacheObject;
import com.m2i.omnicanal.presentation.repository.SchemaEcritureRepository;
import com.m2i.omnicanal.presentation.service.SchemaEcritureService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import jakarta.persistence.Cacheable;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import com.m2i.omnicanal.presentation.service.HazelcastConfigCacheService;
import java.util.ArrayList;

/**
 * Service Implementation for managing SchemaEcriture.
 */
@Service
@Transactional
@Cacheable(value = true)
public class SchemaEcritureServiceImpl implements SchemaEcritureService {

    private final Logger log = LoggerFactory.getLogger(SchemaEcritureServiceImpl.class);
    private final HazelcastConfigCacheService hazelcastConfigCacheService;
    private final SchemaEcritureRepository schemaEcritureRepository;
    PassiveExpiringMap<String, List<SchemaEcriture>> schemaMapDto = new PassiveExpiringMap<>(6400000);

    public SchemaEcritureServiceImpl(SchemaEcritureRepository schemaEcritureRepository, HazelcastConfigCacheService hazelcastConfigCacheService) {
        this.schemaEcritureRepository = schemaEcritureRepository;
        this.hazelcastConfigCacheService = hazelcastConfigCacheService;
    }

    /**
     * Save a schemaEcriture.
     *
     * @param schemaEcriture the entity to save
     * @return the persisted entity
     */
    @Override
    public SchemaEcrit save(SchemaEcrit schemaEcriture) {
        log.debug("Request to save SchemaEcriture : {}", schemaEcriture);
        SchemaEcrit result = schemaEcritureRepository.save(schemaEcriture);
        return result;
    }

    /**
     * Get all the schemaEcritures.
     *
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<SchemaEcrit> findAll() {
        log.debug("Request to get all SchemaEcritures");
        List<SchemaEcrit> result = schemaEcritureRepository.findAll();

        return result;
    }

    /**
     * Get one schemaEcriture by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public SchemaEcrit findOne(Long id) {
        log.debug("Request to get SchemaEcriture : {}", id);
        SchemaEcrit schemaEcriture = schemaEcritureRepository.findOne(id);
        return schemaEcriture;
    }

    /**
     * Delete the schemaEcriture by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete SchemaEcriture : {}", id);
        schemaEcritureRepository.deleteById(id);
    }

    @Override

    public List<SchemaEcriture> findByService(TypeService typeService) {
        List<SchemaEcriture> result ;

        //CacheObject<List<SchemaEcriture>> redisCache = new CacheObject(result, typeService.getId().toString());
        CacheObject< List<SchemaEcriture>> resultat = hazelcastConfigCacheService.getValue(typeService.getId().toString());
        if (resultat != null) {
            return resultat.getContent();
        } else {
            result = schemaEcritureRepository.findByService(typeService);
            CacheObject<List<SchemaEcriture>> redisCache = new CacheObject(result, typeService.getId().toString());
            hazelcastConfigCacheService.putValue(redisCache.getHashKey(), redisCache);
            return result;
        }
        /*
        if (schemaMapDto.containsKey(typeService.getId().toString()) && schemaMapDto.get(typeService.getId().toString()) != null) {
            result = schemaMapDto.get(typeService.getId().toString());
        } else {
            result = schemaEcritureRepository.findByService(typeService);
            if (result != null) {
                schemaMapDto.put(typeService.getId().toString(), result);
            }

        }
         */
        // return result;
    }

    @Override

    public List<SchemaEcriture> findByService(Long typeServiceId) {
        //    List<SchemaEcriture> result= schemaEcritureRepository.findByService(typeServiceId);
        return null;
    }
}
