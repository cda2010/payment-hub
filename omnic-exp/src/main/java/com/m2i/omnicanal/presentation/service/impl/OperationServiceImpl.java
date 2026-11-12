package com.m2i.omnicanal.presentation.service.impl;

import com.m2i.omnicanal.model.Api;
import com.m2i.omnicanal.model.Operation;
import com.m2i.omnicanal.model.enumeration.TypeMethode;
import com.m2i.omnicanal.presentation.domain.ApiAlive;
import com.m2i.omnicanal.presentation.domain.ApplicationApi;
import com.m2i.omnicanal.presentation.domain.HistAppel;
import com.m2i.omnicanal.presentation.domain.MethodeFonction;
import com.m2i.omnicanal.presentation.domain.Oper;
import com.m2i.omnicanal.presentation.domain.User;
import com.m2i.omnicanal.presentation.repository.ApiAliveRepository;
import com.m2i.omnicanal.presentation.repository.ApiRepository;
import com.m2i.omnicanal.presentation.repository.HistoriqueAppelRepository;
import com.m2i.omnicanal.presentation.repository.MethodeRepository;
import com.m2i.omnicanal.presentation.repository.OperationRepository;
import com.m2i.omnicanal.presentation.service.MethodeService;
import com.m2i.omnicanal.presentation.service.OperationService;
import com.m2i.omnicanal.presentation.service.kafka.MessageProducer;
import java.time.ZonedDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Service Implementation for managing Operation.
 */
@Service
@Transactional
public class OperationServiceImpl implements OperationService {

    MessageProducer messageProducer;
    private final Logger log = LoggerFactory.getLogger(OperationServiceImpl.class);
    private final OperationRepository operationRepository;
    private final HistoriqueAppelRepository historiqueAppelRepository;
    private final MethodeService methodeService;
    ApiAliveRepository apiAliveRepository;

    public OperationServiceImpl(ApiAliveRepository apiAliveRepository, MessageProducer messageProducer, OperationRepository operationRepository, HistoriqueAppelRepository historiqueAppelRepository, MethodeService methodeService) {
        this.operationRepository = operationRepository;
        this.historiqueAppelRepository = historiqueAppelRepository;
        this.methodeService = methodeService;
        this.messageProducer = messageProducer;
        this.apiAliveRepository = apiAliveRepository;
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

        dateoperation = java.time.ZonedDateTime.now();

        if (operation.getId() == null) {
            String uuid = UUID.randomUUID().toString().toUpperCase();
            String code = dateoperation.getYear() + "" + dateoperation.getMonth().getValue()
                    + "" + dateoperation.getDayOfMonth() + "" + dateoperation.getHour()
                    + "" + dateoperation.getMinute() + "" + dateoperation.getSecond();
            operation.setCodeOperation(uuid);
        }
        Oper result = operationRepository.save(operation);
        return result;
    }

    /**
     * Get all the operations.
     *
     * @return the list of entities
     */
    @Override
    @Transactional(readOnly = true)
    public List<Oper> findAll() {
        log.debug("Request to get all Operations");
        List<Oper> result = operationRepository.findAll();

        return result;
    }

    /**
     * Get one operation by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Override
    @Transactional(readOnly = true)
    public Oper findOne(Long id) {
        log.debug("Request to get Operation : {}", id);
        Oper operation = operationRepository.findOne(id);
        return operation;
    }

    /**
     * Delete the operation by id.
     *
     * @param id the id of the entity
     */
    @Override
    public void delete(Long id) {
        log.debug("Request to delete Operation : {}", id);
        operationRepository.deleteById(id);
    }

    @Override
    public List<Oper> findAllOrderByDateOperation() {
        log.debug("Request to get all Operations");
        List<Oper> result = operationRepository.findAllDateOperation();

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Oper findByCodeOperation(String codeOperation) {
        Oper operation = operationRepository.findByCodeOperation(codeOperation);
        return operation;
    }

    @Override
    public List<Oper> findAllBycodeFseOrderByDateOperation(String codefse) {
        log.debug("Request to get all Operations");
        List<Oper> result = operationRepository.findAllBycodeFseOrderByDateOperation(codefse);
        return result;
    }

    @Override
    public Long findByReference(String codeOperationEx) {
        Long compte = operationRepository.findByReference(codeOperationEx);
        return compte;
    }

    @Override
    public Oper findByReferenceEx(String codeOperationEx) {
        Oper oper = operationRepository.findByReferenceEx(codeOperationEx);
        return oper;
    }

    @Override
    public void enregistreHistoriqueAppel(Api api, MethodeFonction methode, String codeReponse, String msg, Long duree) {
        HistAppel historiqueAppel = new HistAppel();
        if (api != null && methode != null) {
            historiqueAppel.setCodeFse(api.getFournisseurService().getCodeFse());
            historiqueAppel.setCodeReponse(codeReponse);
            historiqueAppel.setMsg(msg);
            historiqueAppel.setNomApi(api.getNomApi());
            historiqueAppel.setDuree(duree);
            if (methode.getTypeMethode() != null) {
                historiqueAppel.setNomFonction(methode.getTypeMethode().name());
            }
            historiqueAppel.setDateRequest(ZonedDateTime.now());
            log.info("BEGIN CODE : " + historiqueAppel.getCodeFse() + ";" + "CODE REPONSE: " + historiqueAppel.getCodeReponse() + ";" + "API : " + historiqueAppel.getNomApi() + ";" + "DUREE : " + historiqueAppel.getDuree() + ";" + "NOMFONCTION : " + historiqueAppel.getNomFonction() + ";" + historiqueAppel.getDateRequest() + " END");
            historiqueAppelRepository.save(historiqueAppel);
        }

        //
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void removeHistoriqueAppelRepository() {
        ZonedDateTime now = ZonedDateTime.now();
        List<HistAppel> histAppels = historiqueAppelRepository.findAllDateRequestBefore(now.minusDays(2));
        for (HistAppel histAppel : histAppels) {
            log.debug("Suppression des historiques {}", histAppel.getId());
            historiqueAppelRepository.delete(histAppel);
        }

    }

    @Scheduled(cron = "0 0 1 * * ?")
    public void removeAliveAppelRepository() {
        ZonedDateTime now = ZonedDateTime.now();
        List<ApiAlive> listApiAlive = apiAliveRepository.findAllDateRequestBefore(now.minusDays(2));
        apiAliveRepository.deleteInBatch(listApiAlive);

    }

//    @Scheduled(cron = "0/30 * * * * ?")
    public void appelApi() {
        ZonedDateTime now = ZonedDateTime.now();
        log.info("Execution");
        List<MethodeFonction> listMethodeFonction = this.methodeService.findBycodeMethode(TypeMethode.PING);
        for (MethodeFonction methode : listMethodeFonction) {
            log.debug("Envoi des api {}", methode.getId());
            messageProducer.sendApi(methode.getId().toString());
        }
    }
}
