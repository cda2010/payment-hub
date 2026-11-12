/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service;

import com.m2i.pti.domain.enumeration.ActionService;
import com.m2i.pti.domain.OperationAuditEvent;
import com.m2i.pti.domain.Operation;
import com.m2i.pti.repository.OperationAuditEventRepository;
import com.m2i.pti.service.dto.OperationAuditEventDTO;
import com.m2i.pti.service.kafka.MessageProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author cda
 */
@Service
public class OperationAuditEventService {

    private final Logger log = LoggerFactory.getLogger(OperationAuditEventService.class);
    private final OperationAuditEventRepository operationAuditEventRepository;
    private final MessageProducer messageProducer;

    public OperationAuditEventService(OperationAuditEventRepository operationAuditEventRepository, MessageProducer messageProducer) {
        this.operationAuditEventRepository = operationAuditEventRepository;
        this.messageProducer = messageProducer;
    }

    @Transactional
    public OperationAuditEvent save(OperationAuditEvent operationAuditEvent) {
        log.info("Request to save CompteBanque : {}", operationAuditEvent);

        OperationAuditEvent operationAuditEve = operationAuditEventRepository.save(operationAuditEvent);
        return operationAuditEve;
    }

    @Async("taskExecutor")
    public void sendOperationEvent(ActionService actionService, String  codoperation, String codeReponse, String description,String identifiant, String typeAttribut) {
        OperationAuditEventDTO operationEvent = new OperationAuditEventDTO();
        String reference = "";
       
        operationEvent.setActionService(actionService.name());
        operationEvent.setDescription(description);
        operationEvent.setReferenceOperation(codoperation);
        operationEvent.setCodeReponse(codeReponse);
        operationEvent.setIdentifiant(identifiant);
       operationEvent.setTypeAttribut(typeAttribut); 
        messageProducer.sendMessageOperationAuditEvent(operationEvent);
        //taskExecutor.execute(new MessageKafka(not, "sms"));
    }
}
