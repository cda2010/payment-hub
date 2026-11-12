/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service.impl;

import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.m2i.omnicanal.presentation.repository.LigneOperationRepository;
import com.m2i.omnicanal.presentation.repository.OperationRepository;
import com.m2i.omnicanal.presentation.service.TransactionService;
import com.m2i.omnicanal.presentation.service.camel.OutputFile;
import com.m2i.omnicanal.presentation.service.dto.TransactionDTO;
import com.m2i.omnicanal.presentation.service.kafka.MessageProducer;
import com.m2i.omnicanal.presentation.web.rest.TransactionResource;
import java.util.ArrayList;
import java.util.List;
import jakarta.inject.Inject;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *
 * @author cda
 */
@Service
public class TransactionExService {

    private static final String MAIN_SERVICE = "mainService";
    private final Logger log = LoggerFactory.getLogger(TransactionExService.class);
    @Inject
    TransactionService transactionService;

    @Inject
    ObjectTransact transaction;

    private final String login;
    private String password;

    private String url;

    private String clientId;

    private String secretWord;

    private String port;
    private LigneOperationRepository ligneOperationRepository;
    private OperationRepository operationRepository;
    MessageProducer messageProducer;

    public TransactionExService(MessageProducer messageProducer, @Value("${connexion.login}") String login,
            @Value("${connexion.password}") String password, @Value("${connexion.url}") String url,
            @Value("${connexion.clientId}") String clientId, @Value("${connexion.secretWord}") String secretWord, LigneOperationRepository ligneOperationRepository,
            OperationRepository operationRepository) {
        this.login = login;
        this.password = password;
        this.clientId = clientId;
        this.url = url;
        this.secretWord = secretWord;
        this.ligneOperationRepository = ligneOperationRepository;
        this.operationRepository = operationRepository;
        this.messageProducer = messageProducer;

    }

    public List<ResultMessage> getAllTransact(TransactionDTO transDTO) throws Exception {
        CamelContext context;
        List<ResultMessage> reponse = new ArrayList<ResultMessage>();
        context = new DefaultCamelContext();

        context.addRoutes(new OutputFile(login, password, url, clientId, secretWord, reponse, ligneOperationRepository, operationRepository, this.messageProducer));
        // context.addRoutes(new Output());
        ProducerTemplate template = context.createProducerTemplate();
        try {
            context.start();
            template.sendBody("direct:transaction", transDTO);
        } catch (Exception ex) {
            log.debug("REST request to Virement : {}", ex);

        } finally {
            Thread.sleep(1000); // pour avoir le temps d’exécuter la route
            context.stop();
            context.close();
        }

        return reponse;
    }
}
