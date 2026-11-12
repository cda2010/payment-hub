package com.m2i.omnicanal.presentation.service.impl;

import com.m2i.omnicanal.model.SchemaEcriture;
import com.m2i.omnicanal.model.enumeration.TypeMethode;
import com.m2i.omnicanal.presentation.domain.MethodeFonction;
import com.m2i.omnicanal.presentation.domain.util.CacheObject;
import com.m2i.omnicanal.presentation.repository.MethodeRepository;
import com.m2i.omnicanal.presentation.service.HazelcastConfigCacheService;
import com.m2i.omnicanal.presentation.service.MethodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import org.apache.commons.collections4.map.PassiveExpiringMap;

import java.util.List;

/**
 * Service Implementation for managing Methode.
 */
@Service
@Transactional
public class MethodeServiceImpl implements MethodeService {

    private final Logger log = LoggerFactory.getLogger(MethodeServiceImpl.class);

    private final MethodeRepository methodeRepository;
    PassiveExpiringMap<String, MethodeFonction> methodeMapDto = new PassiveExpiringMap<>(600000);
    private final HazelcastConfigCacheService hazelcastConfigCacheService;

    public MethodeServiceImpl(MethodeRepository methodeRepository, HazelcastConfigCacheService hazelcastConfigCacheService) {
        this.methodeRepository = methodeRepository;
        this.hazelcastConfigCacheService = hazelcastConfigCacheService;
    }

    /**
     * Save a methode.
     *
     * @param methode the entity to save
     * @return the persisted entity
     */
    @Override
    public MethodeFonction save(MethodeFonction methode) {
        log.debug("Request to save Methode : {}", methode);
        MethodeFonction result = methodeRepository.save(methode);
        return result;
    }

    /**
     * Get all the methodes.
     *
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<MethodeFonction> findAll() {
        log.debug("Request to get all Methodes");
        List<MethodeFonction> result = methodeRepository.findAll();

        return result;
    }

    /**
     * Get one methode by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public MethodeFonction findOne(Long id) {
        log.debug("Request to get Methode : {}", id);
        MethodeFonction methode = methodeRepository.findOne(id);
        return methode;
    }

    /**
     * Delete the methode by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Methode : {}", id);
        methodeRepository.deleteById(id);
    }

    @Override
    public List<MethodeFonction> findByApi(Long idApi) {
        List<MethodeFonction> result = methodeRepository.findByApi(idApi);
        return result;
    }

    @Override
    public MethodeFonction findBycodeFonction(String codeFonction) {
        log.debug("Request to get Methode : {}", codeFonction);
        MethodeFonction methode;

      //  CacheObject<MethodeFonction> resultat = hazelcastConfigCacheService.getValue(codeFonction);
/*
        if (resultat != null) {
            return resultat.getContent();
        } else {
            methode = methodeRepository.findByCodeMethode(codeFonction);
            CacheObject<List<SchemaEcriture>> redisCache = new CacheObject(methode, codeFonction);
            hazelcastConfigCacheService.putValue(redisCache.getHashKey(), redisCache);
            return methode;
        }
  */      
          if (methodeMapDto.containsKey(codeFonction) && methodeMapDto.get(codeFonction) != null) {
            methode = methodeMapDto.get(codeFonction);
        } else {
            methode = methodeRepository.findByCodeMethode(codeFonction);
            if (methode != null) {
                methodeMapDto.put(codeFonction, methode);
            }

        }
        //   MethodeFonction methode = methodeRepository.findByCodeMethode(codeFonction);
           return methode;
    }

    @Override
    public MethodeFonction findBycodeMethodeAndFSE(TypeMethode codeMethode, String codeFSE) {
         final String key = codeMethode.name() + '|' + codeFSE;

        MethodeFonction methode;
    
      //    CacheObject<MethodeFonction> resultat = hazelcastConfigCacheService.getValue(key);
/*
        if (resultat != null) {
            return resultat.getContent();
        } else {
             methode = methodeRepository.findBycodeMethodeAndFSE(codeMethode, codeFSE);
            CacheObject<MethodeFonction> redisCache = new CacheObject(methode, key);
            hazelcastConfigCacheService.putValue(redisCache.getHashKey(), redisCache);
            return methode;
        }*/
        
        if (methodeMapDto!=null && methodeMapDto.containsKey(key) && methodeMapDto.get(key) != null) {
            methode = methodeMapDto.get(key);
        } else {
            methode = methodeRepository.findBycodeMethodeAndFSE(codeMethode, codeFSE);
            if (methode != null) {
                methodeMapDto.put(key, methode);
            }

        }
        return methode;
    }

    @Override
    public List<MethodeFonction> findBycodeMethode(TypeMethode codeMethode) {
        List<MethodeFonction> listmethode;

        listmethode = methodeRepository.findBycodeMethode(codeMethode);

        return listmethode;
    }

}
