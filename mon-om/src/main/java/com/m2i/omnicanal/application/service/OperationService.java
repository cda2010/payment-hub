package com.m2i.omnicanal.application.service;



import com.m2i.omnicanal.application.domain.Oper;
import com.m2i.omnicanal.application.service.dto.OperationDTO;
import com.m2i.omnicanal.application.service.impl.OperationServiceImpl;

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
     *  Get all the operations.
     *
     *  @return the list of entities
     */
    List<OperationDTO> findAll();

    /**
     *  Get the "id" operation.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    Oper findOne(Long id);

    /**
     *  Delete the "id" operation.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    public List<Oper> findAllOrderByDateOperation();

}
