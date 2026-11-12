package com.m2i.omnicanal.application.service;


import com.m2i.omnicanal.application.domain.MethodeFonction;
import java.util.List;

/**
 * Service Interface for managing Methode.
 */
public interface MethodeService {

    /**
     * Save a methode.
     *
     * @param methode the entity to save
     * @return the persisted entity
     */
    MethodeFonction save(MethodeFonction methode);

    /**
     *  Get all the methodes.
     *  
     *  @return the list of entities
     */
    List<MethodeFonction> findAll();

    /**
     *  Get the "id" methode.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    MethodeFonction findOne(Long id);

    /**
     *  Delete the "id" methode.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    public List<MethodeFonction> findByApi(Long idApi);

    public Integer findMaxByApi(Long id);

    public List<MethodeFonction> findByApiFse(Long idFse);
}
