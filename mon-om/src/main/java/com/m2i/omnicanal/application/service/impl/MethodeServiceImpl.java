package com.m2i.omnicanal.application.service.impl;


import com.m2i.omnicanal.application.domain.MethodeFonction;
import com.m2i.omnicanal.application.repository.MethodeRepository;
import com.m2i.omnicanal.application.service.MethodeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing Methode.
 */
@Service
@Transactional
public class MethodeServiceImpl implements MethodeService{

    private final Logger log = LoggerFactory.getLogger(MethodeServiceImpl.class);
    
    private final MethodeRepository methodeRepository;

    public MethodeServiceImpl(MethodeRepository methodeRepository) {
        this.methodeRepository = methodeRepository;
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
     *  Get all the methodes.
     *  
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<MethodeFonction> findAll() {
        log.debug("Request to get all Methodes");
        List<MethodeFonction> result = methodeRepository.findAll();

        return result;
    }

    /**
     *  Get one methode by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public MethodeFonction findOne(Long id) {
        log.debug("Request to get Methode : {}", id);
        MethodeFonction methode = methodeRepository.findOne(id);
        return methode;
    }

    /**
     *  Delete the  methode by id.
     *
     *  @param id the id of the entity
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
    public Integer findMaxByApi(Long id) {
       return methodeRepository.findMaxByApi( id) ;
    }

    @Override
    public List<MethodeFonction> findByApiFse(Long idFse) {
        List<MethodeFonction> result = methodeRepository.findByApiFse(idFse);

        return result;
    }
}
