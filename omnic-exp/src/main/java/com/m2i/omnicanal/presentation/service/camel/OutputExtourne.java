/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service.camel;

import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.m2i.omnicanal.presentation.repository.LigneOperationRepository;
import com.m2i.omnicanal.presentation.repository.OperationRepository;
import com.m2i.omnicanal.presentation.service.kafka.MessageProducer;
import java.util.List;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;

/**
 *
 * @author cda
 */
public class OutputExtourne extends RouteBuilder {
    // private static CamelContext context;

    private final String login;

    private final String password;

    private final String url;
    MessageProducer messageProducer;
    private final String clientId;

    private LigneOperationRepository ligneOperationRepository;
    private OperationRepository operationRepository;
    private final String secretWord;
    private final List<ResultMessage> reponse;

    public OutputExtourne(String login, String password, String url, String clientId, String secretWord, List<ResultMessage> reponse, LigneOperationRepository ligneOperationRepository, OperationRepository operationRepository, MessageProducer messageProducer) {
        this.login = login;
        this.password = password;
        this.url = url;
        this.clientId = clientId;
        this.secretWord = secretWord;
        this.reponse = reponse;
        this.ligneOperationRepository = ligneOperationRepository;
        this.operationRepository = operationRepository;
        this.messageProducer = messageProducer;

    }

    @Override
    public void configure() throws Exception {
        onException(Exception.class)
                .handled(true).maximumRedeliveries(2);
        errorHandler(defaultErrorHandler()
                .retryAttemptedLogLevel(LoggingLevel.ERROR));
        from("direct:transaction")
                .process(new TransactionProcessor(login, password, url, clientId, secretWord, reponse, ligneOperationRepository, operationRepository, messageProducer))
                .to("direct:differe");
        from("direct:differe").routeId("differeRoute")
                .process(new TransactionDiffereProcessor(messageProducer, ligneOperationRepository))
                .to("direct:reversion");
        from("direct:reversion").routeId("myRoute")
                .process(new TransactionReversingProcessor(messageProducer, ligneOperationRepository))
                .to("mock:end")
                .stop();

    }
}
