/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service.cron;

import com.m2i.omnicanal.model.LigneTransaction;
import com.m2i.omnicanal.model.Operation;
import com.m2i.omnicanal.model.Parametre;
import com.m2i.omnicanal.model.Transaction;
import com.m2i.omnicanal.model.enumeration.PrioriteExec;
import com.m2i.omnicanal.model.enumeration.TypeTransaction;
import com.m2i.omnicanal.model.util.JsonLigneTransaction;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.m2i.omnicanal.presentation.domain.LigneOper;
import com.m2i.omnicanal.presentation.domain.Oper;

import com.m2i.omnicanal.presentation.repository.OperationRepository;
import com.m2i.omnicanal.presentation.repository.LigneOperationRepository;
import com.m2i.omnicanal.presentation.service.camel.OutputExtourne;
import com.m2i.omnicanal.presentation.service.camel.OutputFile;
import com.m2i.omnicanal.presentation.service.dto.TransactionDTO;
import com.m2i.omnicanal.presentation.service.kafka.MessageProducer;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author cda
 */
@Service
public class TacheProgramme {

    private final String login;
    private String password;

    private String url;

    private String clientId;

    private String secretWord;

    private String port;
    LigneOperationRepository ligneOperationRepository;
    OperationRepository operationRepository;
    MessageProducer messageProducer;

    public TacheProgramme(@Value("${connexion.login}") String login,
            @Value("${connexion.password}") String password, @Value("${connexion.url}") String url,
            @Value("${connexion.clientId}") String clientId, @Value("${connexion.secretWord}") String secretWord, LigneOperationRepository ligneOperationRepository, OperationRepository operationRepository, MessageProducer messageProducer) {
        this.login = login;
        this.password = password;
        this.clientId = clientId;
        this.url = url;
        this.secretWord = secretWord;
        this.ligneOperationRepository = ligneOperationRepository;
        this.operationRepository = operationRepository;
        this.messageProducer = messageProducer;
    }

//    @Scheduled(cron="0 0 13,19 * * *")
    // @Scheduled(fixedRate = 50000)
    //  @Scheduled(fixedRate = 50000)
    public void executeProgramme() throws Exception {
        executeDiffere();
        Thread.sleep(10);
        // SystemSleep()
        // executeReversion();
    }

    // @Scheduled(cron="* * 0/1 * * *")
    //@Scheduled(fixedRate = 50000)
    public void executeReversion() throws Exception {
        List<Oper> lgOperId = ligneOperationRepository.findDistinctTransaction();
        Iterator itOperId = lgOperId.iterator();
        while (itOperId.hasNext()) {
            Oper oper = (Oper) itOperId.next();

            String terminal = oper.getTerminal();

            List<LigneOper> lgOper = ligneOperationRepository.findReversalTransactionOperId(oper.getId());
            Iterator it = lgOper.iterator();
            Set<LigneTransaction> listLg = new HashSet();
            while (it.hasNext()) {
                LigneOper newLigne = new LigneOper();
                LigneOper lg = (LigneOper) it.next();
                newLigne = lg;

                if (!"0".equals(lg.getCodereponse())) {

                }
                JsonLigneTransaction jsonLg = lg.getTextLigneTransaction();
                if (jsonLg != null) {
                    LigneTransaction lgT = jsonLg.getLigneTransation();
                    Set<Parametre> listParma = lgT.getParametreService();
                    Iterator itParam = listParma.iterator();
                    Set<Parametre> newListParama = new HashSet();
                    while (itParam.hasNext()) {
                        Parametre param = (Parametre) itParam.next();
                        if (param.getCodeParametreRevers() != null) {
                            param.setCodeParametre(param.getCodeParametreRevers());

                        }
                        newListParama.add(param);
                    }

                    lgT.setParametreService(newListParama);
                    listLg.add(lgT);
                }

            }

            Transaction trans = new Transaction();
            trans.setTypeTransaction(TypeTransaction.REVERS);
            trans.setId(oper.getId());
            trans.setLigneTransaction(listLg);
            List<ResultMessage> resMsg = new ArrayList<>();
            TransactionDTO transDTO = new TransactionDTO(trans, resMsg);
            CamelContext context;
            List<ResultMessage> reponse = new ArrayList<ResultMessage>();
            context = new DefaultCamelContext();
            context.addRoutes(new OutputFile(login, password, url, clientId, secretWord, reponse, ligneOperationRepository, operationRepository, this.messageProducer));
            // context.addRoutes(new Output());
            ProducerTemplate template = context.createProducerTemplate();
            try {
                if (!context.isStarted()) {
                    context.start();
                }

                template.sendBody("direct:transaction", transDTO);
            } catch (Exception ex) {
            } finally {
                Thread.sleep(10); // pour avoir le temps d’exécuter la route
                context.stop();
            }
        }

    }

    private LigneOper dupliqueLigneOper(LigneOper lo, Oper o) {
        LigneOper newLigneOper = new LigneOper();
        newLigneOper.setTextLigneTransaction(lo.getTextLigneTransaction());
        newLigneOper.setOperation(o);

        return null;
    }

    private Oper dupliqueOper(Oper o) {
        Oper newOper = new Oper();
        newOper.setCodeMsg(null);
        newOper.setFseCodeDest(o.getFseCodeDest());
        newOper.setFseCodeSource(o.getFseCodeSource());
        newOper.setDateOperation(ZonedDateTime.now());
        newOper.setTypeService(o.getTypeService());
        newOper.setTextTransaction(o.getTextTransaction());
        newOper.setTerminal(o.getTerminal());
        return operationRepository.save(newOper);

    }

    public List<ResultMessage> executeExtourne(Long trnId) throws Exception {
        CamelContext context = null;
        String terminal = "";
        List<ResultMessage> reponse = new ArrayList<ResultMessage>();
        Integer compte = ligneOperationRepository.findCountExcuteTransactionOperId(trnId);
        if (compte > 0) {
            List<LigneOper> lgOper = ligneOperationRepository.findTransactionOperId(trnId);
            Iterator it = lgOper.iterator();
            Set<LigneTransaction> listLg = new HashSet();
            while (it.hasNext()) {
                LigneOper newLigne = new LigneOper();
                LigneOper lg = (LigneOper) it.next();
                newLigne = lg;
                terminal = lg.getOperation().getTerminal();
                if (!"0".equals(lg.getCodereponse())) {
                }
                JsonLigneTransaction jsonLg = lg.getTextLigneTransaction();
                lg.setExecute(0);
                lg.setIstraite(1);
                ligneOperationRepository.save(lg);
                if (jsonLg != null) {
                    LigneTransaction lgT = jsonLg.getLigneTransation();
                    Set<Parametre> listParma = lgT.getParametreService();
                    Iterator itParam = listParma.iterator();
                    Set<Parametre> newListParama = new HashSet();
                    while (itParam.hasNext()) {
                        Parametre param = (Parametre) itParam.next();
                        if (param.getCodeParametreRevers() != null) {
                            param.setCodeParametre(param.getCodeParametreRevers());
                        }
                        newListParama.add(param);
                    }
                    lgT.setIdligne(lg.getId() == null ? null : lg.getId().toString());
                    lgT.setParametreService(newListParama);
                    listLg.add(lgT);
                }
            }
            Transaction trans = new Transaction();
            trans.setTypeTransaction(TypeTransaction.REVERS);
            trans.setId(trnId);
            trans.setLigneTransaction(listLg);
            List<ResultMessage> resMsg = new ArrayList<>();
            TransactionDTO transDTO = new TransactionDTO(trans, resMsg);
            context = new DefaultCamelContext();
            context.addRoutes(new OutputFile(login, password, url, clientId, secretWord, reponse, ligneOperationRepository, operationRepository, this.messageProducer));
            // context.addRoutes(new Output());
            ProducerTemplate template = context.createProducerTemplate();
            try {
                if (!context.isStarted()) {
                    context.start();
                }
                template.sendBody("direct:transaction", transDTO);
            } catch (Exception ex) {
                System.out.print("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
            } finally {
                Thread.sleep(1000); // pour avoir le temps d’exécuter la route
                if (context.isStarted()) {
                    context.stop();
                }
            }
        } else {

        }
        return reponse;
    }

    public void executeReversion(Long trnId) throws Exception {
        CamelContext context = null;
        String terminal = "";
        Integer compte = ligneOperationRepository.findCountNonExcuteTransactionOperId(trnId);
        if (compte > 0) {
            List<LigneOper> lgOper = ligneOperationRepository.findReversalTransactionOperId(trnId);
            Iterator it = lgOper.iterator();
            Set<LigneTransaction> listLg = new HashSet();
            while (it.hasNext()) {
                LigneOper newLigne = new LigneOper();
                LigneOper lg = (LigneOper) it.next();
                newLigne = lg;
                if (!"0".equals(lg.getCodereponse())) {
                }
                terminal = lg.getOperation().getTerminal();
                lg.setExecute(0);
                ligneOperationRepository.save(lg);
                JsonLigneTransaction jsonLg = lg.getTextLigneTransaction();
                if (jsonLg != null) {
                    LigneTransaction lgT = jsonLg.getLigneTransation();
                    Set<Parametre> listParma = lgT.getParametreService();
                    Iterator itParam = listParma.iterator();
                    Set<Parametre> newListParama = new HashSet();
                    while (itParam.hasNext()) {
                        Parametre param = (Parametre) itParam.next();
                        if (param.getCodeParametreRevers() != null) {
                            param.setCodeParametre(param.getCodeParametreRevers());
                        }
                        newListParama.add(param);
                    }
                    lgT.setParametreService(newListParama);
                    listLg.add(lgT);
                }
            }

            Transaction trans = new Transaction();
            trans.setTypeTransaction(TypeTransaction.REVERS);
            trans.setId(trnId);
            trans.setLigneTransaction(listLg);
            List<ResultMessage> resMsg = new ArrayList<>();
            TransactionDTO transDTO = new TransactionDTO(trans, resMsg);

            List<ResultMessage> reponse = new ArrayList<ResultMessage>();
            context = new DefaultCamelContext();
            context.addRoutes(new OutputFile(login, password, url, clientId, secretWord, reponse, ligneOperationRepository, operationRepository, this.messageProducer));
            // context.addRoutes(new Output());
            ProducerTemplate template = context.createProducerTemplate();
            try {
                if (!context.isStarted()) {
                    context.start();
                }

                template.sendBody("direct:transaction", transDTO);
            } catch (Exception ex) {
            } finally {

                Thread.sleep(1000); // pour avoir le temps d’exécuter la route
                if (!context.isStarted()) {
                    context.stop();
                }

            }
        } else {

        }

    }

    public void executeDiffere() throws Exception {
        List<Oper> lgOperId = ligneOperationRepository.findDistinctTransactionDiffere();
        Iterator itOperId = lgOperId.iterator();
        while (itOperId.hasNext()) {
            Oper oper = (Oper) itOperId.next();
            String terminal = oper.getTerminal();
            List<LigneOper> lgOper = ligneOperationRepository.findDiffereTransactionOperId(oper.getId());
            Iterator it = lgOper.iterator();
            Set<LigneTransaction> listLg = new HashSet();
            while (it.hasNext()) {
                LigneOper lg = (LigneOper) it.next();
                JsonLigneTransaction jsonLg = lg.getTextLigneTransaction();
                if (jsonLg != null) {
                    LigneTransaction lgT = jsonLg.getLigneTransation();
                    Set<Parametre> listParma = lgT.getParametreService();
                    Iterator itParam = listParma.iterator();
                    Set<Parametre> newListParama = new HashSet();
                    lgT.setPriorite(PrioriteExec.IMMEDIAT);
                    /*
                        while (itParam.hasNext()){
                                    Parametre param = (Parametre)itParam.next();
                                    if ( param.)!=null){
                                        param.setCodeParametre(param.getCodeParametreRevers());
                                       
                                    }
                                     newListParama.add(param);
                       }*/

                    //  lgT.setParametreService(newListParama);
                    listLg.add(lgT);
                }

            }
            Transaction trans = new Transaction();
            trans.setTypeTransaction(TypeTransaction.NORMAL);
            trans.setId(oper.getId());
            trans.setLigneTransaction(listLg);
            List<ResultMessage> resMsg = new ArrayList<>();
            TransactionDTO transDTO = new TransactionDTO(trans, resMsg);
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
            } finally {
                Thread.sleep(1000); // pour avoir le temps d’exécuter la route
                context.stop();
            }
        }

    }

    public void executeDiffere(Long trnId) throws Exception {
        List<Oper> lgOperId = ligneOperationRepository.findDistinctTransactionDiffere();
        Iterator itOperId = lgOperId.iterator();
        while (itOperId.hasNext()) {
            Oper oper = (Oper) itOperId.next();
            Map<String, Object> map = new HashMap<>();
            String terminal = oper.getTerminal();
            List<LigneOper> lgOper = ligneOperationRepository.findDiffereTransactionOperId(oper.getId());
            Iterator it = lgOper.iterator();
            Set<LigneTransaction> listLg = new HashSet();
            while (it.hasNext()) {
                LigneOper lg = (LigneOper) it.next();
                JsonLigneTransaction jsonLg = lg.getTextLigneTransaction();
                if (jsonLg != null) {
                    LigneTransaction lgT = jsonLg.getLigneTransation();
                    lgT.setIdligne(lg.getId() == null ? null : lg.getId().toString());
                    Set<Parametre> listParma = lgT.getParametreService();
                    Iterator itParam = listParma.iterator();
                    Set<Parametre> newListParama = new HashSet();
                    if (lgT.getPriorite().equals(PrioriteExec.DIFFEREUN) || lgT.getPriorite().equals(PrioriteExec.DIFFEREDEUX) || lgT.getPriorite().equals(PrioriteExec.DIFFERETROIS)) {
                        lgT.setPriorite(PrioriteExec.IMMEDIAT);
                    }

                    /*
                        while (itParam.hasNext()){
                                    Parametre param = (Parametre)itParam.next();
                                    if ( param.)!=null){
                                        param.setCodeParametre(param.getCodeParametreRevers());
                                       
                                    }
                                     newListParama.add(param);
                       }*/
                    //  lgT.setParametreService(newListParama);
                    listLg.add(lgT);
                }

            }
            Transaction trans = new Transaction();
            trans.setTypeTransaction(TypeTransaction.NORMAL);
            trans.setId(oper.getId());
            trans.setLigneTransaction(listLg);
            List<ResultMessage> resMsg = new ArrayList<>();
            TransactionDTO transDTO = new TransactionDTO(trans, resMsg);
            CamelContext context;
            List<ResultMessage> reponse = new ArrayList<ResultMessage>();
            context = new DefaultCamelContext();
            context.addRoutes(new OutputFile(login, password, url, clientId, secretWord, reponse, ligneOperationRepository, operationRepository, this.messageProducer));
            // context.addRoutes(new Output());
            ProducerTemplate template = context.createProducerTemplate();
            try {
                if (!context.isStarted()) {
                    context.start();
                }
                template.sendBody("direct:transaction", transDTO);
            } catch (Exception ex) {
            } finally {
                Thread.sleep(1000); // pour avoir le temps d’exécuter la route
                context.stop();
            }
        }

    }

}
