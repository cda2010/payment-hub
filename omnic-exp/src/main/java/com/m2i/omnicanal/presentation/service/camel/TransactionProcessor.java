/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service.camel;

import com.m2i.omnicanal.model.LigneTransaction;
import com.m2i.omnicanal.model.enumeration.PrioriteExec;
import com.m2i.omnicanal.model.enumeration.TypeTransaction;
import com.m2i.omnicanal.model.util.JsonLigneTransaction;
import com.m2i.omnicanal.presentation.http.PartnerHttpClient;
import com.m2i.omnicanal.outils.restclient.constants.ErreurConstants;
import com.m2i.omnicanal.outils.restclient.model.Message;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.m2i.omnicanal.presentation.domain.LigneOper;
import com.m2i.omnicanal.presentation.domain.MessageRetour;
import com.m2i.omnicanal.presentation.domain.Oper;
import com.m2i.omnicanal.presentation.repository.LigneOperationRepository;
import com.m2i.omnicanal.presentation.repository.MethodeRepository;
import com.m2i.omnicanal.presentation.repository.OperationRepository;
import com.m2i.omnicanal.presentation.service.OperationService;
import com.m2i.omnicanal.presentation.service.ParametreMethodeService;
import com.m2i.omnicanal.presentation.service.dto.LigneTransactionDTO;
import com.m2i.omnicanal.presentation.service.dto.TransactionDTO;
import com.m2i.omnicanal.presentation.service.kafka.MessageProducer;
import com.m2i.omnicanal.presentation.web.rest.MiddleWareResource;
import com.m2i.omnicanal.presentation.web.rest.errors.CustomParameterizedException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

/**
 *
 * @author cda
 */
public class TransactionProcessor implements Processor {

    private final String login;
    OperationRepository operationRepository;
    LigneOperationRepository ligneOperationRepository;
    private final MessageProducer messageProducer;
    private final String password;
    private final String url;
    private final String clientId;
    private final String secretWord;
    private List<ResultMessage> reponse;
    String externalOperation;
    
    private final Logger log = LoggerFactory.getLogger(TransactionProcessor.class);

    public TransactionProcessor( String login,
            String password, String url,
            String clientId, String secretWord, List<ResultMessage> reponse, LigneOperationRepository ligneOperationRepository, OperationRepository operationRepository, MessageProducer messageProducer) {
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

    public void process(Exchange exchng) throws IOException {

        long tempsDebut, tempsFin, duree;
        tempsDebut = System.currentTimeMillis();

        StopWatch stopWatch = new StopWatch("Measure Transaction");
        TransactionDTO trans = exchng.getIn().getBody(TransactionDTO.class);
        boolean canExecute = true;
        log.info("EXECUTION DANS OMNICANAL TRANSACTION");
        //   try {
        // TransactionDTO trans = exchng.getIn().getBody(TransactionDTO.class);
        Set<LigneTransaction> listLg = trans.getTrans().getLigneTransaction();
        Long trnId = trans.getTrans().getId();
        List<LigneTransaction> sortedDataLigne = listLg.stream().sorted((s1, s2) -> (s1.getOrdre() - s2.getOrdre())).collect(Collectors.toList());
        List<Integer> ld = sortedDataLigne.stream().map(x -> x.getOrdre()).collect(Collectors.toList());
        System.out.println(ld);
        Iterator it = sortedDataLigne.iterator();
        LigneTransaction lg;
        String jsonString = null;
        ResultMessage rep;
        List<ResultMessage> res = new ArrayList<ResultMessage>();
        int cpt = 0;
        boolean reserv = false;
        boolean error = false;
        ResultMessage msgRet;
        Map<String, Object> map = new HashMap<>();
        boolean dummy = false;
        String terminal = trans.getTrans().getTerminal();
        stopWatch.start("1. Traitement des ligne transaction ");
        while (it.hasNext() && error == false) {
            lg = (LigneTransaction) it.next();
            tempsDebut = System.currentTimeMillis();
            LigneOper lgOper = ligneOperationRepository.findByIdLigneAndOperation(lg.getIdligne() == null ? null : Long.valueOf(lg.getIdligne()), trnId);
            tempsFin = System.currentTimeMillis();
            duree = tempsFin - tempsDebut;

            System.out.println("LA DUREE DE TRAITEMENT EST : " + duree);
            log.info("Execution de la ligne" + lg.getIdligne());
            ResponseEntity<Object> resultat = null;
            Long opID = trans.getTrans().getId();
            LigneTransactionDTO ligneDto = new LigneTransactionDTO(reponse, lg, opID, terminal);
            List<ResultMessage> res2 = new ArrayList<ResultMessage>();
            if (lg.getPriorite().equals(PrioriteExec.IMMEDIAT)) {
                if (lg.getZeroExiste() == 0 && !lg.getParametreService().isEmpty()) {
                     rep = PartnerHttpClient.current().post(url + "/api/middleware", null, ligneDto, "2", "JSON");
                  
                    dummy = false;
                } else {
                    dummy = true;

                    Message msg = new Message();
                    msg.setCodeErr("0");
                    msg.setMsg("Opération terminée avec Succès");
                    rep = PartnerHttpClient.result(ErreurConstants.SUCCESS, "Opération terminée avec Succès", "1", msg, "JSON");
                    rep = PartnerHttpClient.result(ErreurConstants.SUCCESS, "Opération terminée avec Succès", "3", rep, "JSON");

                }

                //reponse=   resClient.getRessources(null,ligneDto,reponse,"2","JSON");
                String resultMsg = "";

                //  lgOper.setRevers(0);
                //     if (reponse.size() > 0) {
                msgRet = PartnerHttpClient.convert(rep.getResponseEntity(), ResultMessage.class);
                if (msgRet != null) {
                    System.out.println("MESSAGE DE RETOUR:" + msgRet.getErrCode());
                    log.info("Execution de la ligne" + msgRet.getErrCode());
                    if (msgRet.getErrCode() != 0) {
                        lgOper.setCodereponse(msgRet.getErrCode() + "");
                        lgOper.setReponse(msgRet.getMsg());
                        log.info("ERREUR D'EXECUTION {}", ligneDto);
                        error = true;
                        //  throw new IOException("Forced");
                    } else {
                        MessageRetour msgRetour = PartnerHttpClient.convert(msgRet.getResponseEntity(), MessageRetour.class);
                        System.out.println("message 2:" + msgRetour);

                        if (msgRetour != null && msgRetour.getCodeErr() != null) {
                            lgOper.setCodereponse(msgRetour.getCodeErr());
                            lgOper.setReponse(msgRetour.getMsg());
                            if (!"0".equals(msgRetour.getCodeErr())) {
                                // lgOper.setExecute(1);
                                //   ligneOperationRepository.save(lgOper);
                                error = true;
                                log.info("MESSAGE DE RETOUR:" + msgRet.getErrCode());
                                System.out.println("MESSAGE DE RETOUR:" + msgRet.getErrCode());
                                // throw new CustomParameterizedException(msgRetour.getMsg());
                            }
                        } else {

                            msgRetour = new MessageRetour();
                            msgRetour.setCodeErr("-1");
                            error = true;

                            //  throw new CustomParameterizedException("Erreur lors du traitement");
                        }

                    }
                } else {
                    System.out.println("ERREUR DANS LA LIGNE");
                    error = true;
                }

                if (trans.getTrans().getTypeTransaction() != null && trans.getTrans().getTypeTransaction().equals(TypeTransaction.REVERS)) {
                    if (lgOper.getReponse() != null && lgOper.getCodereponse().equals("0")) {
                        lgOper.setRevers(1);
                        reserv = true;
                    } else {
                        reserv = false;
                    }

                }
                if (lgOper.getExecute() == 0) {
                    lgOper.setExecute(1);
                    //  JsonLigneTransaction lgJson = new JsonLigneTransaction();
                    // lgJson.setLigneTransation(lg);
                    //lgOper.setTextLigneTransaction(lgJson);
                }

                //}
                cpt = cpt + 1;
                System.out.println(cpt);

            } else {
                rep = PartnerHttpClient.current().post(url + "/api/dummy", null, ligneDto, "2", "JSON");
                msgRet = PartnerHttpClient.convert(rep.getResponseEntity(), ResultMessage.class);
                /* ne pas exécuter */
                lgOper.setExecute(0);
            }
            if (msgRet != null) {
                reponse.add(msgRet);
            }

            ligneOperationRepository.saveAndFlush(lgOper);

        }
        stopWatch.stop();
        if (reserv) {
            Oper oper = operationRepository.findOne(trnId);
            oper.setId(trnId);
            oper.setCodeMsg("0");
            oper.setAck(1);
            oper.setReponse("REVERSION - " + oper.getReponse());
            operationRepository.saveAndFlush(oper);
        }
        System.out.println(stopWatch.prettyPrint());

    }

}
