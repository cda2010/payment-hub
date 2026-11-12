package com.m2i.omnicanal.application.service.impl;



import com.m2i.omnicanal.application.domain.Oper;

import com.m2i.omnicanal.application.repository.FournisseurServiceRepository;
import com.m2i.omnicanal.application.repository.OperationRepository;
import com.m2i.omnicanal.application.service.OperationService;
import com.m2i.omnicanal.application.service.dto.OperationDTO;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing Operation.
 */
@Service
@Transactional
public class OperationServiceImpl implements OperationService{

    private final Logger log = LoggerFactory.getLogger(OperationServiceImpl.class);

    private final OperationRepository operationRepository;

    private final FournisseurServiceRepository fournisseurServiceRepository;

    public OperationServiceImpl(OperationRepository operationRepository, FournisseurServiceRepository fournisseurServiceRepository) {
        this.operationRepository = operationRepository;
        this.fournisseurServiceRepository = fournisseurServiceRepository;
    }

    /**
     * Save a operation.
     *
     * @param operation the entity to save
     * @return the persisted entity
     */
    @Override
    public Oper save(Oper operation) {
         ZonedDateTime dateoperation;
        log.debug("Request to save Operation : {}", operation);

        dateoperation=java.time.ZonedDateTime.now();

        if(operation.getId()==null){
            String code = dateoperation.getYear()+""+dateoperation.getMonth().getValue()
                    +""+dateoperation.getDayOfMonth()+""+dateoperation.getHour()
                    +""+dateoperation.getMinute()+""+dateoperation.getSecond();
            //operation.setCodeOperation(Long.parseLong(code));
            operation.setCodeOperation(code);
        }
        if (operation.getFseCodeDest() != null)
            operation.setFseDestNom(fournisseurServiceRepository.findByCodeFse(operation.getFseCodeDest()).getNom());
        if (operation.getFseCodeDest() != null)
            operation.setFseSourceNom(fournisseurServiceRepository.findByCodeFse(operation.getFseCodeSource()).getNom());
        Oper result = operationRepository.save(operation);
        return result;
    }

    /**
     *  Get all the operations.
     *
     *  @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<OperationDTO> findAll() {
        log.debug("Request to get all Operations");
        List<OperationDTO> result = operationRepository.findAllDateOperation();
        /*
        for (OperationDTO oper : result) {
            log.debug("Request to get all Operations 1");
            if (oper.getFseDestNom() == null || oper.getFseSourceNom() == null){
                log.debug("Request to get all Operations 2");
                oper.setFseDestNom(fournisseurServiceRepository.findByCodeFse(oper.getFseCodeDest()).getNom());
                oper.setFseSourceNom(fournisseurServiceRepository.findByCodeFse(oper.getFseCodeSource()).getNom());
            }
        }*/
        log.debug("Request to get all Operations 4");
        return result;
    }

    /**
     *  Get one operation by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public Oper findOne(Long id) {
        log.debug("Request to get Operation : {}", id);
        Oper operation = operationRepository.findOne(id);
        return operation;
    }

    /**
     *  Delete the  operation by id.
     *
     *  @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Operation : {}", id);
        operationRepository.deleteById(id);
    }

    @Override
    public List<Oper> findAllOrderByDateOperation() {
          log.debug("Request to get all Operations");
     //   List<Oper> result = operationRepository.findAllDateOperation();
   List<Oper> result=null;
        return result;
    }
}
