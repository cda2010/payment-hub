/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service.camel;

import com.m2i.omnicanal.presentation.domain.LigneOper;
import com.m2i.omnicanal.presentation.repository.LigneOperationRepository;
import com.m2i.omnicanal.presentation.service.cron.TacheProgramme;
import com.m2i.omnicanal.presentation.service.dto.TransactionDTO;
import com.m2i.omnicanal.presentation.service.kafka.MessageProducer;
import java.util.List;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 *
 * @author cda
 */
public class TransactionReversingProcessor implements Processor {

    TacheProgramme tacheProgramme;
    LigneOperationRepository ligneOperationRepository;
    private final MessageProducer messageProducer;

    public TransactionReversingProcessor(MessageProducer messageProducer, LigneOperationRepository ligneOperationRepository) {
        this.messageProducer = messageProducer;
        this.ligneOperationRepository = ligneOperationRepository;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            System.out.println("Entrer Reversing");
            TransactionDTO trans = exchange.getIn().getBody(TransactionDTO.class);
            Integer compte = ligneOperationRepository.findCountNonExcuteTransactionOperId(trans.getTrans().getId());
             List<LigneOper> lgOper = ligneOperationRepository.findReversalTransactionOperId(trans.getTrans().getId());
            if (compte > 0 && lgOper.size()> 0 ) {
                System.out.println(trans.getTrans().getId());
                this.messageProducer.sendMessageTransaction(trans.getTrans().getId().toString());
               exchange.setRouteStop(true);
                
            }

        } catch (Exception e) {

        }

    }

}
