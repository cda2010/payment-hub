/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service.camel;

import com.m2i.omnicanal.model.LigneTransaction;
import com.m2i.omnicanal.model.Parametre;
import com.m2i.omnicanal.model.Transaction;
import com.m2i.omnicanal.model.enumeration.TypeTransaction;
import com.m2i.omnicanal.model.util.JsonLigneTransaction;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.m2i.omnicanal.presentation.domain.LigneOper;
import com.m2i.omnicanal.presentation.repository.LigneOperationRepository;
import com.m2i.omnicanal.presentation.service.cron.TacheProgramme;
import com.m2i.omnicanal.presentation.service.dto.TransactionDTO;
import com.m2i.omnicanal.presentation.service.kafka.MessageProducer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.RoutePolicy;

/**
 *
 * @author cda
 */
public class TransactionDiffereProcessor implements Processor {

    TacheProgramme tacheProgramme;
    LigneOperationRepository ligneOperationRepository;
    private final MessageProducer messageProducer;

    public TransactionDiffereProcessor(MessageProducer messageProducer, LigneOperationRepository ligneOperationRepository) {
        this.messageProducer = messageProducer;
        this.ligneOperationRepository = ligneOperationRepository;
    }

    @Override
    public void process(Exchange exchange) throws Exception {
      //  try {
            System.out.println("Entrer Differee");
            TransactionDTO trans = exchange.getIn().getBody(TransactionDTO.class);
            Integer compte = ligneOperationRepository.findCountTransactionDiffere(trans.getTrans().getId());
             List<LigneOper> lgOper = ligneOperationRepository.findDiffereTransactionOperId(trans.getTrans().getId());
            if (compte > 0 && lgOper.size()> 0 ) {
                System.out.println(trans.getTrans().getId());
                this.messageProducer.sendMessageTransactionDiffere(trans);
               exchange.setRouteStop(true);
                
            }

      /*  } catch (Exception e) {
                System.out.println(e.getMessage());
        }*/

    }

}
