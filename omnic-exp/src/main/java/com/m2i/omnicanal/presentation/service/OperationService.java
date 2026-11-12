package com.m2i.omnicanal.presentation.service;

import com.m2i.omnicanal.model.Api;
import com.m2i.omnicanal.model.Operation;
import com.m2i.omnicanal.presentation.domain.MethodeFonction;
import com.m2i.omnicanal.presentation.domain.Oper;
import java.util.List;

/**
 * Service Interface for managing Operation.
 */
public interface OperationService {

    /**
     * Save a operation.
     *
     * @param operation the entity to save
     * @return the persisted entity
     */
    Oper save(Oper operation);

    /**
     * Get all the operations.
     *
     * @return the list of entities
     */
    List<Oper> findAll();

    /**
     * Get the "id" operation.
     *
     * @param id the id of the entity
     * @return the entity
     */
    Oper findOne(Long id);

    /**
     * Delete the "id" operation.
     *
     * @param id the id of the entity
     */
    void delete(Long id);

    public List<Oper> findAllOrderByDateOperation();

    public Oper findByCodeOperation(String codeOperation);

    public List<Oper> findAllBycodeFseOrderByDateOperation(String codefse);

    public Long findByReference(String codeOperationEx);

    public void enregistreHistoriqueAppel(Api api, MethodeFonction methode, String codeReponse, String msg, Long duree);
        public  Oper findByReferenceEx(String codeOperationEx);

}
