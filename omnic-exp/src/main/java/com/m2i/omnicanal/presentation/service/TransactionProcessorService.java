/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service;

import com.m2i.omnicanal.model.LigneTransaction;
import com.m2i.omnicanal.model.enumeration.PrioriteExec;
import com.m2i.omnicanal.model.enumeration.TypeTransaction;
import com.m2i.omnicanal.outils.restclient.AuthTokenInfo;
import com.m2i.omnicanal.presentation.http.PartnerHttpClient;
import com.m2i.omnicanal.outils.restclient.constants.ErreurConstants;
import com.m2i.omnicanal.outils.restclient.model.Message;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.m2i.omnicanal.presentation.domain.LigneOper;
import com.m2i.omnicanal.presentation.domain.MessageRetour;
import com.m2i.omnicanal.presentation.domain.Oper;
import com.m2i.omnicanal.presentation.repository.LigneOperationRepository;
import com.m2i.omnicanal.presentation.repository.OperationRepository;
import com.m2i.omnicanal.presentation.service.dto.LigneTransactionDTO;
import com.m2i.omnicanal.presentation.service.dto.TransactionDTO;
import com.m2i.omnicanal.presentation.service.kafka.MessageProducer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StopWatch;

/**
 *
 * @author cda
 */
@Service
public class TransactionProcessorService {

    private static final Logger log = LoggerFactory.getLogger(TransactionProcessorService.class);

    private String login;
    private String password;

    private String url;

    private String clientId;

    private String secretWord;

    private String port;
    private LigneOperationRepository ligneOperationRepository;
    private OperationRepository operationRepository;
    MessageProducer messageProducer;
    private final MiddlewareService middlewareService;
@Autowired
private TransactionTemplate txTemplate;
    public TransactionProcessorService(MessageProducer messageProducer, @Value("${connexion.login}") String login,
            @Value("${connexion.password}") String password, @Value("${connexion.url}") String url,
            @Value("${connexion.clientId}") String clientId, @Value("${connexion.secretWord}") String secretWord, LigneOperationRepository ligneOperationRepository,
            OperationRepository operationRepository, MiddlewareService middlewareService) {
        this.login = login;
        this.password = password;
        this.clientId = clientId;
        this.url = url;
        this.secretWord = secretWord;
        this.ligneOperationRepository = ligneOperationRepository;
        this.operationRepository = operationRepository;
        this.messageProducer = messageProducer;
        this.middlewareService = middlewareService;

    }

    /**
     * Traite une transaction de manière synchrone et efficace
     */
    public List<ResultMessage> processTransaction(TransactionDTO transDTO) {
        log.info("Début du traitement de la transaction: {}", transDTO.getTrans().getId());
        List<ResultMessage> result = new ArrayList<>();
        try {
            // 1. Validation de la transaction

            result = processTransactionLogic(transDTO);
            log.info("Transaction traitée avec succès: {}", transDTO.getTrans().getId());

        } catch (Exception e) {
            log.error("Erreur lors du traitement de la transaction: {}", transDTO.getTrans().getId(), e);
            result.add(createErrorResult(e.getMessage()));
        }

        return result;
    }

    private ResultMessage createErrorResult(String errorMessage) {
        ResultMessage errorResult = new ResultMessage();
        errorResult.setErrCode(-1);
        errorResult.setMsg(errorMessage);
        errorResult.setNumCouche("3");
        return errorResult;
    }

    private List<ResultMessage> processTransactionLogic(TransactionDTO transDTO) throws IOException {

    final List<ResultMessage> reponse = new ArrayList<ResultMessage>();
    if (transDTO == null || transDTO.getTrans() == null) {
        reponse.add(createErrorResult("TransactionDTO ou transaction null"));
        return reponse;
    }

    final Long trnId = transDTO.getTrans().getId();
    final String terminal = transDTO.getTrans().getTerminal();
    final Set<LigneTransaction> listLg = transDTO.getTrans().getLigneTransaction();

    // ⚠️ En prod: supprime les System.out.println
    // System.out.println(...);

    // Tri Java 8 safe
    final List<LigneTransaction> sortedDataLigne = listLg.stream()
            .sorted(Comparator.comparingInt(LigneTransaction::getOrdre))
            .collect(Collectors.toList());

    // -------------------------
    // PHASE 1 : READ DB (court)
    // -------------------------
    final Map<Long, LigneOper> lgOperByLigneId = txTemplate.execute(status -> {
       
        Map<Long, LigneOper> map = new HashMap<Long, LigneOper>();
        List<LigneOper> ops = ligneOperationRepository.findByOperationId(trnId);

        for (LigneOper op : ops) {
            if (op.getId() != null) {
                map.put(op.getId(), op);
            }
        }
        return map;
    });

    // -------------------------
    // PHASE 2 : APPELS RESEAU (hors TX)
    // -------------------------
    boolean error = false;
    boolean allOkForRevers = true;

    final List<LigneOper> toSave = new ArrayList<LigneOper>(sortedDataLigne.size());

    for (LigneTransaction lg : sortedDataLigne) {
        if (error) break;

        final long startLine = System.currentTimeMillis();

        Long ligneId = ligneOperationId(lg.getIdligne());
        LigneOper lgOper = ligneId == null ? null : lgOperByLigneId.get(ligneId);
        if (lgOper == null) {
            lgOper = ligneOperationRepository.findByIdLigneAndOperation(ligneId, trnId);
        }

        ResultMessage msgRet;

        if (lgOper == null) {
            msgRet = createErrorResult("Ligne opération introuvable");
            reponse.add(msgRet);
            error = true;
            continue;
        }

        try {
            LigneTransactionDTO ligneDto = new LigneTransactionDTO(reponse, lg, trnId, terminal);

            if (PrioriteExec.IMMEDIAT.equals(lg.getPriorite())) {

                if (lg.getZeroExiste() == 0 && lg.getParametreService() != null && !lg.getParametreService().isEmpty()) {
                    msgRet = middlewareService.execute(ligneDto);
                } else {
                    msgRet = middlewareService.dummy(ligneDto);
                }

                applyResultToLigneOper(lgOper, msgRet);

                lgOper.setExecute(1);

            } else {
                msgRet = middlewareService.dummy(ligneDto);

                lgOper.setExecute(0);
                if (msgRet == null) {
                    msgRet = createErrorResult("Réponse dummy null");
                    lgOper.setCodereponse("-1");
                    lgOper.setReponse(msgRet.getMsg());
                }
            }

        } catch (Exception ex) {
            msgRet = createErrorResult(ex.getMessage());
            lgOper.setCodereponse(String.valueOf(msgRet.getErrCode()));
            lgOper.setReponse(msgRet.getMsg());
            error = true;
        }

        reponse.add(msgRet);

        boolean ok = (lgOper.getCodereponse() != null && "0".equals(lgOper.getCodereponse()));
        allOkForRevers = allOkForRevers && ok;
        if (!ok) error = true;

        toSave.add(lgOper);

        long durationMs = System.currentTimeMillis() - startLine;
        log.info("Txn[{}] - Ligne {} traitée en {} ms (execute={}, code={})",
                trnId, lg.getIdligne(), durationMs, lgOper.getExecute(), lgOper.getCodereponse());
    }

    // -------------------------
    // PHASE 3 : WRITE DB (court)
    // -------------------------
    final boolean finalAllOkForRevers = allOkForRevers;
    txTemplate.execute(status -> {
        // Spring Data : préfère saveAll (selon version)
        ligneOperationRepository.saveAll(toSave);
        ligneOperationRepository.flush();

        if (transDTO.getTrans().getTypeTransaction() != null
                && TypeTransaction.REVERS.equals(transDTO.getTrans().getTypeTransaction())
                && finalAllOkForRevers) {

            Oper oper = operationRepository.findOne(trnId);
            if (oper != null) {
                oper.setCodeMsg("0");
                oper.setAck(1);
                oper.setReponse("REVERSION - " + oper.getReponse());
                operationRepository.saveAndFlush(oper);
            }
        }
        return null;
    });

    return reponse;
}

private Long ligneOperationId(String idligne) {
    if (idligne == null) {
        return null;
    }
    try {
        return Long.valueOf(idligne);
    } catch (NumberFormatException ex) {
        log.warn("idligne illisible: {}", idligne);
        return null;
    }
}

private void applyResultToLigneOper(LigneOper lgOper, ResultMessage msgRet) throws IOException {
    if (msgRet == null) {
        lgOper.setCodereponse("-1");
        lgOper.setReponse("ERREUR: msgRet null");
        return;
    }

    if (msgRet.getErrCode() != 0) {
        lgOper.setCodereponse(String.valueOf(msgRet.getErrCode()));
        lgOper.setReponse(msgRet.getMsg());
        return;
    }

    MessageRetour msgRetour = PartnerHttpClient.convert(msgRet.getResponseEntity(), MessageRetour.class);
    if (msgRetour == null || msgRetour.getCodeErr() == null) {
        lgOper.setCodereponse("-1");
        lgOper.setReponse("Erreur (MessageRetour null)");
        return;
    }

    lgOper.setCodereponse(msgRetour.getCodeErr());
    lgOper.setReponse(msgRetour.getMsg());
}
    /**
     * Validation de la transaction
     */
    /**
     * Logique métier de traitement
     */
    
    /**
     * Envoi vers Kafka (asynchrone)
     */
}
