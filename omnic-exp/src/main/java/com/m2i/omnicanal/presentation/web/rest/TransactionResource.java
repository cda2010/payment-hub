/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.m2i.omnicanal.model.Operation;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.m2i.omnicanal.presentation.domain.LigneOper;
import com.m2i.omnicanal.presentation.repository.LigneOperationRepository;
import com.m2i.omnicanal.presentation.repository.MethodeRepository;
import com.m2i.omnicanal.presentation.repository.OperationRepository;
import com.m2i.omnicanal.presentation.service.OperationService;
import com.m2i.omnicanal.presentation.service.ParametreMethodeService;
import com.m2i.omnicanal.presentation.service.TransactionProcessorService;

import com.m2i.omnicanal.presentation.service.TransactionService;
import com.m2i.omnicanal.presentation.service.camel.OutputFile;
import com.m2i.omnicanal.presentation.service.cron.TacheProgramme;
import com.m2i.omnicanal.presentation.service.dto.TransactionDTO;
import com.m2i.omnicanal.presentation.service.impl.MsgBean;
import com.m2i.omnicanal.presentation.service.impl.ObjectTransact;
import com.m2i.omnicanal.presentation.service.kafka.MessageProducer;
import com.m2i.omnicanal.presentation.web.rest.errors.CustomParameterizedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author cda
 */
@RestController
@RequestMapping("/api")
public class TransactionResource {

    private static final String MAIN_SERVICE = "mainService";
    private final Logger log = LoggerFactory.getLogger(TransactionResource.class);
    @Inject
    TransactionService transactionService;

    @Inject
    ObjectTransact transaction;

    @Inject
    MsgBean msgBean;

    private String login;
    private String password;

    private String url;

    private String clientId;

    private String secretWord;

    private String port;
    private LigneOperationRepository ligneOperationRepository;
    private OperationRepository operationRepository;
    MessageProducer messageProducer;
    TransactionProcessorService transactionProcessorService;

    /* public TransactionResource(LigneOperationRepository ligneOperationRepository, OperationRepository operationRepository, MessageProducer messageProducer) {

        this.ligneOperationRepository = ligneOperationRepository;
        this.operationRepository = operationRepository;
        this.messageProducer = messageProducer;
         
    }*/
    public TransactionResource(TransactionProcessorService transactionProcessorService, MessageProducer messageProducer, @Value("${connexion.login}") String login,
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
        this.transactionProcessorService = transactionProcessorService;

    }

    @PostMapping("/transactionsvgggg")
    @Timed
    public List<ResultMessage> getAllTransactXXXX(@RequestBody TransactionDTO transDTO) throws Exception {
        CamelContext context;
        List<ResultMessage> reponse = new ArrayList<ResultMessage>();
        context = new DefaultCamelContext();

        System.out.println("DEBUT DES LOG TRANSACTION");
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonStr = ow.writeValueAsString(transDTO);
        System.out.println(jsonStr);

        /*
        if (!transDTO.getTrans().getReferenceEx().equals(externalOperation)) {
            return null;
        }*/
        context.addRoutes(new OutputFile(login, password, url, clientId, secretWord, reponse, ligneOperationRepository, operationRepository, this.messageProducer));
        // context.addRoutes(new Output());
        ProducerTemplate template = context.createProducerTemplate();
        try {
            context.start();
            template.sendBody("direct:transaction", transDTO);
        } catch (Exception ex) {
            log.debug("REST request to Virement : {}", ex);

        } finally {
            //  Thread.sleep(50); // pour avoir le temps d’exécuter la route
            context.stop();
            context.close();
        }

        return reponse;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @PostMapping("/transactions-old2")
    @Timed
    public List<ResultMessage> getAllTransactxx(@RequestBody TransactionDTO transDTO, HttpServletRequest req, HttpServletResponse res) throws Exception {
        CamelContext context;
        List<ResultMessage> reponse = new ArrayList<ResultMessage>();
        context = new DefaultCamelContext();

        System.out.println("DEBUT DES LOG TRANSACTION");
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonStr = ow.writeValueAsString(transDTO);
        System.out.println(jsonStr);

        /*
        if (!transDTO.getTrans().getReferenceEx().equals(externalOperation)) {
            return null;
        }*/
        context.addRoutes(new OutputFile(login, password, getUrl(), clientId, secretWord, reponse, ligneOperationRepository, operationRepository, this.messageProducer));
        // context.addRoutes(new Output());
        ProducerTemplate template = context.createProducerTemplate();
        try {
            context.start();
            template.sendBody("direct:transaction", transDTO);
        } catch (Exception ex) {
            log.debug("REST request to Virement : {}", ex);

        } finally {
            Thread.sleep(50); // pour avoir le temps d’exécuter la route
            context.stop();
            context.close();
        }

        return reponse;
    }

    @PostMapping("/transactions")
    @Timed
    public List<ResultMessage> getAllTransact(@RequestBody TransactionDTO transDTO, HttpServletRequest req, HttpServletResponse res) throws Exception {
        List<ResultMessage> reponse = new ArrayList<ResultMessage>();
        System.out.println("DEBUT DES LOG TRANSACTION");
        try {
            reponse = transactionProcessorService.processTransaction(transDTO);
        } catch (Exception ex) {
            log.debug("REST request to Virement : {}", ex);
        } finally {

        }
        return reponse;
    }
    /*
    @PostMapping("/transactions2")
    @Timed
    public     List <ResultMessage> getAllTransaction(@RequestBody TransactionDTO  transDTO) {

     Map<String, JobParameter> parameters = 
                    new HashMap<String, JobParameter>();
    msgBean.setMsg(transDTO.getMsg());
   
        try {
          StopWatch stopWatch = new StopWatch();
        stopWatch.start();

            JobParameters jobParameters = new JobParametersBuilder()
                    
                    .addLong("time", System.currentTimeMillis())
                    .addLong("opID", transDTO.getTrans().getId())
                     .addString("login", login)
                     .addString("password", password)
                     .addString("clientId", clientId)
                    .addString("secretWord", secretWord)
                     .addString("url", url)
                    .toJobParameters();
              
            jobLauncher.run(job, jobParameters);
             stopWatch.stop();

        } catch (Exception e) {
           log.debug(e.getMessage());
        }
       
   return transDTO.getMsg();
        
    }*/
}
