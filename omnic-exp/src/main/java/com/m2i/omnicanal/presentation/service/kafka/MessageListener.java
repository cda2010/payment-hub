/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service.kafka;

import com.m2i.omnicanal.model.LigneTransaction;
import com.m2i.omnicanal.presentation.http.PartnerHttpClient;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.m2i.omnicanal.presentation.domain.ApiAlive;
import com.m2i.omnicanal.presentation.domain.MessageRetour;
import com.m2i.omnicanal.presentation.domain.MethodeFonction;
import com.m2i.omnicanal.presentation.repository.ApiAliveRepository;
import com.m2i.omnicanal.presentation.service.MethodeService;
import com.m2i.omnicanal.presentation.service.cron.TacheProgramme;
import com.m2i.omnicanal.presentation.service.dto.LigneTransactionDTO;
import com.m2i.omnicanal.presentation.service.dto.TransactionDTO;
import com.m2i.omnicanal.presentation.service.impl.RestClientUtil;
import com.m2i.omnicanal.presentation.web.rest.TransactionResource;
import java.time.ZonedDateTime;

import java.util.concurrent.CountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 *
 * @author cda
 */
@Component
public class MessageListener {
    
    TacheProgramme tacheProgramme;
    MethodeService methodeService;
    RestClientUtil restClientUtil;
    ApiAliveRepository apiAliveRepository;
    private final Logger log = LoggerFactory.getLogger(MessageListener.class);
    
    public MessageListener(RestClientUtil restClientUtil, TacheProgramme tacheProgramme, MethodeService methodeService, ApiAliveRepository apiAliveRepository) {
        this.tacheProgramme = tacheProgramme;
        this.methodeService = methodeService;
        this.restClientUtil = restClientUtil;
        this.apiAliveRepository = apiAliveRepository;
    }
    
    private CountDownLatch latch = new CountDownLatch(3);
    private CountDownLatch partitionLatch = new CountDownLatch(2);
    private CountDownLatch filterLatch = new CountDownLatch(2);
    private CountDownLatch greetingLatch = new CountDownLatch(1);
    
    @KafkaListener(topics = "transactionError", containerFactory = "transactionKafkaListenerContainerFactory")
    public void listenGroupOperation(String id) throws Exception {
        System.out.println("Received Messasge in group transactionError: " + id);
        String idFinal = id.replace("\"", "");
        this.tacheProgramme.executeReversion(Long.parseLong(idFinal));
        latch.countDown();
    }
    
    @KafkaListener(topics = "transactionDiffere", containerFactory = "transactionDiffKafkaListenerContainerFactory", groupId = "transactionDiffere")
    public void listenGroupOperationTran(TransactionDTO transactionDto) throws Exception {
        System.out.println("Received Messasge in group transaction diff: " + transactionDto);
        
        this.tacheProgramme.executeDiffere(transactionDto.getTrans().getId());
        latch.countDown();
    }
    
    @KafkaListener(topics = "transactionExtourne", containerFactory = "transactionKafkaListenerContainerFactory", groupId = "transactionExtourne")
    public void listenGroupOperationTranExt(String id) throws Exception {
        System.out.println("Received Messasge in group transaction EXTOURNE: " + id);
        String idFinal = id.replace("\"", "");
        this.tacheProgramme.executeExtourne(Long.parseLong(idFinal));
        latch.countDown();
    }
    
    @KafkaListener(topics = "appelApi", containerFactory = "appelKafkaListenerContainerFactory", groupId = "appelApiGrp")
    public void listenGroupAppelApi(String id) throws Exception {
        boolean error = false;
        
        System.out.println("Received Messasge in group  appelApi: " + id);
        String idFinal = id.replace("\"", "");
        MethodeFonction methodeFonction = methodeService.findOne(Long.parseLong(idFinal));
        ResultMessage resultMessage = restClientUtil.pingCall(methodeFonction);
        ResultMessage msgRet = PartnerHttpClient.convert(resultMessage.getResponseEntity(), ResultMessage.class);
        if (msgRet != null) {
            
            ApiAlive apiAlive = new ApiAlive();
            System.out.println("MESSAGE DE RETOUR:" + msgRet.getErrCode());
        
            // log.info("Execution de la ligne" + msgRet.getErrCode());
            apiAlive.setMethode(methodeFonction);
            apiAlive.setDateRequest(ZonedDateTime.now());
            if (msgRet.getErrCode() != 0) {
                error = true;
                apiAlive.setStatut(1L);
                apiAlive.setCodeReponse(msgRet.getErrCode() + "");
            } else {
                MessageRetour msgRetour = PartnerHttpClient.convert(msgRet.getResponseEntity(), MessageRetour.class);
                if (msgRetour != null && msgRetour.getCodeErr() != null) {
                    
                    if (!"0".equals(msgRetour.getCodeErr())) {
                        
                        apiAlive.setCodeReponse(msgRetour.getCodeErr() + "");
                        apiAlive.setStatut(1L);
                        
                    } else {
                        
                        apiAlive.setCodeReponse(msgRetour.getCodeErr() + "");
                        apiAlive.setStatut(0L);
                    }
                } else {
                    msgRetour = new MessageRetour();
                    msgRetour.setCodeErr("-1");
                    error = true;
                    apiAlive.setCodeReponse(msgRetour.getCodeErr() + "");
                    apiAlive.setStatut(1L);
                    //  throw new CustomParameterizedException("Erreur lors du traitement");
                }
                
            }
            apiAliveRepository.save(apiAlive);
        }
        /* LigneTransactionDTO lg = new LigneTransactionDTO();
        LigneTransaction ligneTrans = new LigneTransaction();
        ligneTrans.setMethodeApi(methodeFonction);
        lg.setLigneTransaction(ligneTrans);*/
        latch.countDown();
    }
    
}
