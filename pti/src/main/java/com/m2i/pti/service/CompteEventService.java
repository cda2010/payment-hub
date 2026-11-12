/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service;

import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.CompteEvent;
import com.m2i.pti.domain.Operation;
import com.m2i.pti.repository.CompteEventRepository;
import com.m2i.pti.service.dto.CompteEventDTO;
import com.m2i.pti.service.kafka.MessageProducer;
import java.math.BigDecimal;
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

public class CompteEventService {

    private final Logger log = LoggerFactory.getLogger(CompteEventService.class);

    private final CompteEventRepository compteEventRepository;
MessageProducer  messageProducer;
    public CompteEventService(CompteEventRepository compteEventRepository,MessageProducer  messageProducer) {
        this.compteEventRepository = compteEventRepository;
         this.messageProducer=messageProducer;
    }
@Transactional
    public CompteEvent save(CompteEvent compteEvent) {
        log.info("Request to save CompteBanque : {}", compteEvent);

        CompteEvent compteEve = compteEventRepository.save(compteEvent);
        return compteEve;
    }
    
  @Async("taskExecutor")
    public void sendCompteEvent(Compte compte, String sens, BigDecimal montant, Operation operation) {
        CompteEventDTO compteEvent = new CompteEventDTO();
        String reference = "";
        if (operation != null) {
            reference = operation.getCodeExterne() != null ? operation.getCodeExterne() : operation.getCodeOperation();
        }
        compteEvent.setCompteId(compte.getId());
        compteEvent.setMontant(montant);
        compteEvent.setReferenceOperation(reference);
        compteEvent.setSens(sens);
        compteEvent.setSoldeCompte(compte.getCompteAutre().getSolde());
        messageProducer.sendMessageCompteEvent(compteEvent);
        //taskExecutor.execute(new MessageKafka(not, "sms"));
    }
    
}
