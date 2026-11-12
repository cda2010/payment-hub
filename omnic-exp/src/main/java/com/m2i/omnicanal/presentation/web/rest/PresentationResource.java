/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParser;
import com.m2i.omnicanal.model.Api;
import com.m2i.omnicanal.model.Operation;
import com.m2i.omnicanal.model.ParametreUtilisateur;
import com.m2i.omnicanal.model.Transaction;
import com.m2i.omnicanal.model.TypeService;
import com.m2i.omnicanal.model.enumeration.TypeMethode;
import com.m2i.omnicanal.model.enumeration.ProprieteApi;
import com.m2i.omnicanal.model.enumeration.TypeEnvoi;

import com.m2i.omnicanal.outils.restclient.AuthTokenInfo;
import com.m2i.omnicanal.presentation.http.PartnerHttpClient;
import com.m2i.omnicanal.outils.restclient.constants.ErreurConstants;
import com.m2i.omnicanal.outils.restclient.model.Message;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.m2i.omnicanal.presentation.domain.CodeErreur;
import com.m2i.omnicanal.presentation.domain.ExternalInfos;

import com.m2i.omnicanal.presentation.domain.InterfaceUser;
import com.m2i.omnicanal.presentation.domain.MessageRet;
import com.m2i.omnicanal.presentation.domain.MethodeFonction;
import com.m2i.omnicanal.presentation.domain.Oper;
import com.m2i.omnicanal.presentation.domain.OperationID;

import com.m2i.omnicanal.presentation.domain.ParametreAppelDTO;
import com.m2i.omnicanal.presentation.domain.ParametreDTO;
import com.m2i.omnicanal.presentation.domain.ParametreMethod;
import com.m2i.omnicanal.presentation.domain.ParametreUser;
import com.m2i.omnicanal.presentation.domain.ReponseOperation;
import com.m2i.omnicanal.presentation.execution.PartnerExecutionService;
import com.m2i.omnicanal.presentation.domain.util.ErrorCodes;
import com.m2i.omnicanal.presentation.domain.util.RetourMessage;
import com.m2i.omnicanal.presentation.repository.CodeErreurRepository;
import com.m2i.omnicanal.presentation.repository.HistoriqueAppelRepository;
import com.m2i.omnicanal.presentation.repository.InterfaceUtilisateurRepository;
import com.m2i.omnicanal.presentation.repository.LigneOperationRepository;
import com.m2i.omnicanal.presentation.repository.MethodeRepository;
import com.m2i.omnicanal.presentation.repository.OperationRepository;

import com.m2i.omnicanal.presentation.service.InterfaceUtilisateurService;
import com.m2i.omnicanal.presentation.service.MethodeService;
import com.m2i.omnicanal.presentation.service.OperationService;
import com.m2i.omnicanal.presentation.service.ParametreMethodeService;
import com.m2i.omnicanal.presentation.service.PresentationDetailService;
import com.m2i.omnicanal.presentation.service.PresentationService;
import com.m2i.omnicanal.presentation.service.TransactionProcessorService;
import com.m2i.omnicanal.presentation.service.TypeServiceService;
import com.m2i.omnicanal.presentation.service.dto.TransactionDTO;
import com.m2i.omnicanal.presentation.service.impl.MsgBean;
import com.m2i.omnicanal.presentation.service.impl.TransactionExService;
import com.m2i.omnicanal.presentation.service.kafka.MessageProducer;
import com.m2i.omnicanal.presentation.service.util.Util;
import java.net.URISyntaxException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;
import org.json.simple.JSONObject;

import org.json.simple.parser.ParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.m2i.omnicanal.presentation.resilience.ApiCircuitBreaker;
import com.m2i.omnicanal.presentation.service.HazelcastConfigCacheService;
import com.m2i.omnicanal.presentation.service.TokenCacheService;
import com.m2i.omnicanal.presentation.service.util.FastXml;

/**
 *
 * @author cda
 */
/**
 *
 * La classe represente le ressource de la couche presentaton.
 *
 * dans cette couche nous recuperons les données de l'interface utilisateur.
 *
 * nous procedons à un contrôle en fonction des contraintes definis dans le
 * pramètrage.
 *
 * nous recuperons les lignes de transactions utiles pour l'appel de la couche
 * transaction après tout nous recuperons les reponsess issues de l'appel des
 * couches que nous portons à l'utiluateur.
 *
 */
@RestController
@RequestMapping("/api")
public class PresentationResource {

    @Inject
    MsgBean msgBean;
    @Inject
    PresentationDetailService presentationDetailService;
    @Inject
    InterfaceUtilisateurRepository interfaceUtilisateurRepository;
    private final Logger log = LoggerFactory.getLogger(PresentationResource.class);
    private static final String MESSAGE_DUMMY = "Opération terminée avec Succès";
    private final PresentationService presentationService;
    private final OperationService operationService;
    private final InterfaceUtilisateurService interfaceUtilisateurService;
    private final TypeServiceService typeServiceService;
    LigneOperationRepository ligneOperationRepository;
    private final String login;

    private final ParametreMethodeService paramMethode;

    private final MethodeService methodeService;
    private final String password;

    private final String url;

    private final String clientId;

    private final String secretWord;
    private final MessageProducer messageProducer;
    private String port;
    private final boolean proxyenabled;
    TransactionExService transactionExService;
    OperationRepository operationRepository;
    HistoriqueAppelRepository historiqueAppelRepository;
    MethodeRepository methodeRepository;
    TransactionProcessorService transactionProcessorService;
    HazelcastConfigCacheService hazelcastConfigCacheService;
    private final ConcurrentHashMap<String, AuthTokenInfo> cache = new ConcurrentHashMap<>();
    TokenCacheService tokenCacheService;
    CodeErreurRepository codeErreurRepository;
    private final ApiCircuitBreaker apiCircuit;
    private final PartnerHttpClient partnerHttp;
    private final PartnerExecutionService partnerExecution;

    public PresentationResource(CodeErreurRepository codeErreurRepository, TokenCacheService tokenCacheService, TransactionProcessorService transactionProcessorService, OperationRepository operationRepository, HistoriqueAppelRepository historiqueAppelRepository, TransactionExService transactionExService, MessageProducer messageProducer, LigneOperationRepository ligneOperationRepository, TypeServiceService typeServiceService, PresentationService presentationService, OperationService operationService, @Value("${connexion.login}") String login,
            @Value("${connexion.password}") String password, @Value("${connexion.url}") String url,
            @Value("${connexion.clientId}") String clientId, @Value("${connexion.secretWord}") String secretWord, @Value("${param.proxyenable}") boolean proxyenabled,
            InterfaceUtilisateurService interfaceUtilisateurService, ParametreMethodeService paramMethode, MethodeService methodeService, MethodeRepository methodeRepository,
            ApiCircuitBreaker apiCircuit, PartnerHttpClient partnerHttp, PartnerExecutionService partnerExecution) {
        this.presentationService = presentationService;
        this.login = login;
        this.proxyenabled = proxyenabled;
        this.password = password;
        this.clientId = clientId;
        this.url = url;
        this.secretWord = secretWord;
        this.operationService = operationService;
        this.interfaceUtilisateurService = interfaceUtilisateurService;
        this.paramMethode = paramMethode;
        this.methodeService = methodeService;
        this.typeServiceService = typeServiceService;
        this.ligneOperationRepository = ligneOperationRepository;
        this.messageProducer = messageProducer;
        this.transactionExService = transactionExService;
        this.historiqueAppelRepository = historiqueAppelRepository;
        this.methodeRepository = methodeRepository;
        this.transactionProcessorService = transactionProcessorService;
        this.tokenCacheService = tokenCacheService;
        this.codeErreurRepository = codeErreurRepository;
        this.apiCircuit = apiCircuit;
        this.partnerHttp = partnerHttp;
        this.partnerExecution = partnerExecution;
    }

    private ResultMessage envoyer(ApiCircuitBreaker.Guard circuit, Api api, String codeMethode, String httpMethod,
            List<ParametreUtilisateur> rawList, Map<String, Object> headers, Supplier<ResultMessage> call) {
        Map<String, String> raw = new HashMap<>();
        if (rawList != null) {
            for (ParametreUtilisateur parametre : rawList) {
                if (parametre.getCodeParametre() != null && parametre.getValeur() != null) {
                    raw.put(parametre.getCodeParametre(), parametre.getValeur());
                }
            }
        }
        PartnerExecutionService.Outcome outcome = partnerExecution.execute(api, codeMethode, httpMethod, raw, headers, call);
        if (circuit != null) {
            if (outcome.partnerCalled()) {
                circuit.record(outcome.result());
            } else {
                circuit.ignore();
            }
        }
        return outcome.result();
    }

    @GetMapping("/msgRetour")
    @Timed
    public CodeErreur rechercheRetourOmnicanal(@RequestParam("codeOperation") String codeOperation) throws URISyntaxException {
        Oper operation = operationService.findByReferenceEx((codeOperation));
        if (operation != null) {
            String strCommand = operation.getExternalInfos();
          
            strCommand = strCommand.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>", "");
           
            ExternalInfos externalInfos = FastXml.fromXml(strCommand, ExternalInfos.class);
            if (externalInfos != null && externalInfos.getListExternalInfos()!=null &&  !externalInfos.getListExternalInfos().isEmpty() && externalInfos.getListExternalInfos().get(0).getCode() != null) {
                List<CodeErreur> listCode = codeErreurRepository.findAllCodeErreur(externalInfos.getListExternalInfos().get(0).getCode());

                if (listCode != null && !listCode.isEmpty()) {  
                    return new CodeErreur(ErrorCodes.RESSOURCE_EXTERNAL_ERROR, listCode.get(0).getLibelle(), externalInfos.getListExternalInfos().get(0).getCpteSrc(), externalInfos.getListExternalInfos().get(0).getCpteDest());
                } else {
                    return new CodeErreur(ErrorCodes.STANDARD_ERROR, "Une erreur s'est produite l'ors de l'operation");
                }

            } else {
                return new CodeErreur(ErrorCodes.STANDARD_ERROR, "Une erreur s'est produite l'ors de l'operation");
            }

        } else {
            return new CodeErreur(ErrorCodes.STANDARD_ERROR, "Une erreur s'est produite l'ors de l'operation");
        }

    }

    @GetMapping("/sendAck")
    @Timed
    public void sendAck(@RequestParam("codeOperation") String codeOperation) throws URISyntaxException {
        Oper operation = operationService.findByCodeOperation((codeOperation));
        if (operation != null) {
            operation.setAck(0);
            Transaction trans = new Transaction();
            trans.setId(operation.getId());
            TransactionDTO transDto = new TransactionDTO();
            transDto.setTrans(trans);
            operationService.save(operation);
            messageProducer.sendMessageTransactionEx(transDto.getTrans().getId().toString());
        }

    }

    private String getUrl(Api api) {
        String baseUrl = null;
        String codeApi = "";
        String shttp;
        api.getNomApi();

        if (api.isUseSsl() == true) {
            shttp = "https";
        } else {
            shttp = "http";
        }

        if ((api.getUseProxy() == true)) {
            codeApi = "/" + api.getCodeApi();
        }

        //  if (serviceInstance != null) {
        //    baseUrl = serviceInstance.getUri().toString() + codeApi;
        //} else {
        baseUrl = shttp + "://" + api.getHost() + ":" + api.getPort() + codeApi;
        //}

        return baseUrl;
    }

    private String getUrl2(Api api) {
        String baseUrl = null;
        String codeApi = "";
        String shttp;
        api.getNomApi();
        if (api.isUseSsl() == true) {
            shttp = "https";
        } else {
            shttp = "http";
        }

        if ((api.getUseProxy() == true)) {
            codeApi = api.getCodeApi();
            baseUrl = shttp + "://" + api.getHost() + ":" + api.getPort() + "/" + codeApi;
        } else {
            baseUrl = shttp + "://" + api.getHost() + ":" + api.getPort();
        }

        // if (serviceInstance != null) {
        //  baseUrl = serviceInstance.getUri().toString() + "/" + codeApi;
        // } else {
        //  }
        return baseUrl;
    }

    private TypeMethode getTypeMethodeValue(String code) {
        TypeMethode type = null;
        switch (code) {
            case "DEBIT": {
                type = TypeMethode.DEBIT;
            }
            break;
            case "CREDIT": {
                type = TypeMethode.CREDIT;
            }
            break;

            case "CONSULTATION": {
                type = TypeMethode.CONSULTATION;
            }
            break;

            case "NOP": {
                type = TypeMethode.NOP;
            }
            break;
            case "TRANSFERT": {
                type = TypeMethode.TRANSFERT;
            }
            break;
            case "CONSULTATIONSOLDE": {
                type = TypeMethode.CONSULTATIONSOLDE;
            }
            break;
            case "CONSULTATIONHIST": {
                type = TypeMethode.CONSULTATIONHIST;
            }
            break;
            case "ENVOISMS": {
                type = TypeMethode.ENVOISMS;
            }
            break;
            case "LISTEVENEMENT": {
                type = TypeMethode.LISTEVENEMENT;
            }
            break;
            case "INFOCLIENT": {
                type = TypeMethode.INFOCLIENT;
            }
            break;
            case "CARTEMOUVEMENT": {
                type = TypeMethode.CARTEMOUVEMENT;
            }
            case "GETMONTANT": {
                type = TypeMethode.GETMONTANT;
            }
            break;
            case "CONSULTATIONFACTURE": {
                type = TypeMethode.CONSULTATIONFACTURE;
            }
            break;
            case "SENDFILEFTP": {
                type = TypeMethode.SENDFILEFTP;
            }
            break;
            case "SENDFILEMAIL": {
                type = TypeMethode.SENDFILEMAIL;
            }
            break;
            case "GETDATA": {
                type = TypeMethode.GETDATA;
            }
            break;
        }
        return type;
    }

    @PostMapping("presentationsXX")
    @Timed
    public ResponseEntity<?> getAllXX(@RequestParam("codeFSE") String codeFSE, @RequestParam("codeMethode") String codeMethode, @RequestParam("otrParametre") String otrParametre) throws ParseException, Exception {
        TypeMethode ty = getTypeMethodeValue(codeMethode);

        MethodeFonction methode = methodeService.findBycodeMethodeAndFSE(TypeMethode.valueOf(codeMethode), codeFSE);
        Api api = methode.getApi();
        ResultMessage resultat = null;
        String shttp;
        String baseUrl = null;
        String url;
        AuthTokenInfo tokeinfo = null;
        switch (methode.getApi().getType()) {
            case REST:
                ConcurrentHashMap<String, Object> mapParametre;
                InterfaceUser interfaceUt = interfaceUtilisateurRepository.findParTypeMethode(api.getFournisseurService().getCodeFse(), ty, TypeEnvoi.REQUEST);
                Set<ParametreUtilisateur> paramUserInterface = interfaceUt.getConstitues();

                List<ParametreMethod> result = paramMethode.findByMethode(methode.getId());
                String urlComplement = getuLRParametres(result); // on constitue l'url pour l'appel
                List<ParametreUtilisateur> parametreUser = getUserParametre(otrParametre, null); // on recupère les paramètres utilisateur

                Set<ParametreUtilisateur> paramReturnedUserInterface = presentationDetailService.getParametresInterfaceValue(parametreUser, paramUserInterface);

                List<ParametreUtilisateur> listParametreUser = new ArrayList<>(paramReturnedUserInterface);
                //    private Set<ParametreUtilisateur> getParametresInterfaceValue(List<ParametreUtilisateur> listparamUser,   Set<ParametreUtilisateur>  paramUserInterface)
                mapParametre = getParametres(listParametreUser, result);
                if (methode.getApi().isUseSsl() == true) {
                    shttp = "https";
                } else {
                    shttp = "http";
                }

                baseUrl = getUrl(api);
                //  if (baseUrl != null) {
                url = baseUrl + "/" + methode.getNomMethode() + "/" + urlComplement;
                //} else {
                //  url = shttp + "://" + api.getHost() + ":" + api.getPort() + "/" + methode.getNomMethode() + "/" + urlComplement;
                //}

                Map<String, Object> headers = new HashMap<>();
                switch (methode.getApi().getTypeAutorization()) {
                    case OAUTH2:
                        if (isTokenValid(tokeinfo) == false) {
                            tokeinfo = getTokenOauth2(methode.getApi());
                        }

                        AuthTokenInfo token = tokeinfo;
                        resultat = envoyer(null, api, codeMethode, methode.getTypeAppel().toString(), parametreUser, headers,
                                () -> partnerHttp.call(url, token, mapParametre, methode.getTypeAppel().toString(), "1", methode.getTypeReponse().toString(), headers));
                        break;
                    case BASIC:
                        resultat = PartnerHttpClient.result(ErreurConstants.FONCTION_NON_IMPLEMENTE, "Non implementé!!!", "1", null, methode.getTypeReponse().toString());
                        break;
                    case OAUTH:
                        resultat = PartnerHttpClient.result(ErreurConstants.FONCTION_NON_IMPLEMENTE, "Non implementé!!!", "1", null, methode.getTypeReponse().toString());
                        break;
                    case NOAUTH:
                        resultat = envoyer(null, api, codeMethode, methode.getTypeAppel().toString(), parametreUser, headers,
                                () -> partnerHttp.call(url, null, mapParametre, methode.getTypeAppel().toString(), "1", methode.getTypeReponse().toString(), headers));
                        break;
                }

                break;

        }

        return ResponseEntity.ok().body(resultat);

    }

    @PostMapping("/checkFonction")
    @Timed
    public ResponseEntity<?> checkFonction(@RequestParam("codeMethode") String codeMethode, @RequestParam("codeFSE") String codeFSE, HttpServletRequest req, HttpServletResponse res) {
        Message mesRetour = new Message();
        MethodeFonction methode = methodeService.findBycodeMethodeAndFSE(TypeMethode.valueOf(codeMethode), codeFSE);
        if (methode != null && methode.getApi() != null) {
            mesRetour.setMsg("OK");
            mesRetour.setCodeErr(ErreurConstants.SUCCESS + "");
        } else {
            mesRetour.setMsg("Erreur");
            mesRetour.setCodeErr(ErreurConstants.FONCTION_NON_IMPLEMENTE + "");

        }
        return ResponseEntity.ok().body(mesRetour);
    }

    @PostMapping("/boundaryByParam")
    @Timed
    // @HystrixCommand(fallbackMethod = "getFallBackResult")
    public ResponseEntity<?> getApiResult(@RequestBody ParametreAppelDTO paramDto, HttpServletRequest req, HttpServletResponse res) throws JsonProcessingException {

        long tempsDebut, tempsFin;
        tempsDebut = System.currentTimeMillis();
        long duree;
        ResultMessage resultat = null;
        String shttp;
        String baseUrl = null;
        Integer nbreCompteError = 0;
        String url;
        AuthTokenInfo tokeinfo = null;
        boolean exceptionOccured = false;

        //  TypeMethode ty = getTypeMethodeValue(paramDto.getCodeMethode());
        TypeMethode ty = TypeMethode.valueOf(paramDto.getCodeMethode());
        MethodeFonction methode = methodeService.findBycodeMethodeAndFSE(ty, paramDto.getFse());
        if (methode == null) {
            resultat = PartnerHttpClient.result(ErreurConstants.RESSOURCE_ACCESS_ERROR, "Ressource temporairement indisponible", "1", null, null);

        } else {
            Api api = methode.getApi();
            ApiCircuitBreaker.Guard circuit = ApiCircuitBreaker.Guard.passthrough();
            List<ParametreUtilisateur> listparamUser = new ArrayList<>();

           
            /*   ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonStr = ow.writeValueAsString(paramDto);
        System.out.println(jsonStr);*/
            Map<String, Object> map = PartnerHttpClient.appHeaders(req);
            for (ParametreUser par : paramDto.getParam()) {

                ParametreUtilisateur param = new ParametreUtilisateur();
                param.setValeur(par.getValeur());
                param.setCodeParametre(par.getCodeParametre());
                /**
                 * Recherche l'interface utilisateur concerné par le type
                 * service **
                 */
                param.setInterfaceUtilisateur(par.getInterfaceUtilisateur());
                param.setParametreApi(null);
                listparamUser.add(param);
            }
            ConcurrentHashMap<String, Object> mapParametre;
            /*Recuperation de l'interface**/
            boolean circuitBreaker = false;
            InterfaceUser interfaceUtil = interfaceUtilisateurService.getInterfaceUser(paramDto.getFse(), ty, TypeEnvoi.REQUEST);
            Set<ParametreUtilisateur> paramUserInterface = interfaceUtil.getConstitues();
            Set<ParametreUtilisateur> paramReturnedUserInterface = presentationDetailService.getParametresInterfaceValue(listparamUser, paramUserInterface);
            List<ParametreMethod> result = paramMethode.findByMethode(methode.getId());
            List<ParametreUtilisateur> paramUser = new ArrayList<ParametreUtilisateur>(paramReturnedUserInterface);
            mapParametre = getParametres(paramUser, result);
            String urlComplement = getuLRParametres(result); // on constitue l'url pour l'appel
            try {
                switch (methode.getApi().getType()) {
                    case REST:
                        /*
                if (methode.getApi().isUseSsl() == true) {
                    shttp = "https";
                } else {
                    shttp = "http";
                }
                         */

                        baseUrl = getUrl(api);
                        // if (baseUrl != null) {
                        url = baseUrl + "/" + methode.getNomMethode() + "" + urlComplement;
                        // } else {
                        //   url = shttp + "://" + api.getHost() + ":" + api.getPort() + "/" + methode.getNomMethode() + "/" + urlComplement;
                        // }

                        circuit = apiCircuit.guard(api);
                        if (!circuit.rejected()) {
                            switch (methode.getApi().getTypeAutorization()) {
                                case OAUTH2:
                                    tempsDebut = System.currentTimeMillis();
                                    tokeinfo = tokenCacheService.tokenCache(methode.getApi());
                                    tempsFin = System.currentTimeMillis();
                                    duree = tempsFin - tempsDebut;
                                  
                                    AuthTokenInfo token = tokeinfo;
                                    resultat = envoyer(circuit, api, paramDto.getCodeMethode(), methode.getTypeAppel().toString(), listparamUser, map,
                                            () -> partnerHttp.call(url, token, mapParametre, methode.getTypeAppel().toString(), "1", methode.getTypeReponse().toString(), map));
                                    
                                    break;
                                case BASIC:
                                    resultat = PartnerHttpClient.result(ErreurConstants.FONCTION_NON_IMPLEMENTE, "Non implementé!!!", "1", null, methode.getTypeReponse().toString());
                                    break;
                                case OAUTH:
                                    resultat = PartnerHttpClient.result(ErreurConstants.FONCTION_NON_IMPLEMENTE, "Non implementé!!!", "1", null, methode.getTypeReponse().toString());
                                    break;
                                case NOAUTH:
                                    resultat = envoyer(circuit, api, paramDto.getCodeMethode(), methode.getTypeAppel().toString(), listparamUser, map,
                                            () -> partnerHttp.call(url, null, mapParametre, methode.getTypeAppel().toString(), "1", methode.getTypeReponse().toString(), map));
                                    break;
                            }
                            circuit.record(resultat);
                        } else {
                            circuitBreaker = true;
                            resultat = apiCircuit.unavailable(methode.getTypeReponse().toString(), "Ressource indisponible");
                        }

                        break;

                }

            } catch (Exception e) {
                exceptionOccured = true;
                circuit.record(e);
                resultat = new ResultMessage();
                resultat.setMsg("Une erreur est survenue lors de l'appel");
                resultat.setErrCode(ErreurConstants.UNKNOWN_ERROR);
            } finally {

                tempsFin = System.currentTimeMillis();
                duree = tempsFin - tempsDebut;
                String codeReponse = String.valueOf(resultat.getErrCode());
                operationService.enregistreHistoriqueAppel(api, methode, codeReponse, resultat.getMsg(), duree);

            }
        }

        return ResponseEntity.ok().body(resultat);
    }

    /**
     * cette fonction est utilisée lorsque l'on utilise un type méthode dans les
     * paramètres
     *
     * @param codeFSE
     * @param codeMethode
     * @param otrParametre
     * @return
     */
    @PostMapping("/boundaryByTypeMethode")
    @Timed
    //   @HystrixCommand(fallbackMethod = "getFallBackResult")
    public ResponseEntity<?> getApiResult(@RequestParam("codeFSE") String codeFSE, @RequestParam("codeMethode") String codeMethode, @RequestParam("otrParametre") String otrParametre, HttpServletRequest req, HttpServletResponse res) {
        /* find param of Api
         codeMethode

         get fonction paramater

         */
        Long tempsFin;
        Long duree;
        Long tempsDebut;
        tempsDebut = System.currentTimeMillis();
        boolean circuitBreaker = false;
        TypeMethode ty = getTypeMethodeValue(codeMethode);
        boolean exceptionOccured = false;
        ResultMessage resultat = null;
        MethodeFonction methode = methodeService.findBycodeMethodeAndFSE(TypeMethode.valueOf(codeMethode), codeFSE);
        if (methode == null) {
            resultat = PartnerHttpClient.result(ErreurConstants.RESSOURCE_ACCESS_ERROR, "Ressource temporairement indisponible", "1", null, null);

        } else {
            Api api = methode.getApi();
            ApiCircuitBreaker.Guard circuit = ApiCircuitBreaker.Guard.passthrough();

            String shttp;
            String baseUrl = null;
            String url;
            AuthTokenInfo tokeinfo = null;
            try {
                switch (methode.getApi().getType()) {
                    case REST:
                        ConcurrentHashMap<String, Object> mapParametre;
                        List<ParametreMethod> result = paramMethode.findByMethode(methode.getId());
                        String urlComplement = getuLRParametres(result); // on constitue l'url pour l'appel
                        List<ParametreUtilisateur> parametreUser = getUserParametre(otrParametre, null); // on recupère les paramètres utilisateur
                        mapParametre = getParametres(parametreUser, result);
                        if (methode.getApi().isUseSsl() == true) {
                            shttp = "https";
                        } else {
                            shttp = "http";
                        }
                        Map<String, Object> map = PartnerHttpClient.appHeaders(req);
                        baseUrl = getUrl(api);
                        // if (baseUrl != null) {
                        url = baseUrl + "/" + methode.getNomMethode() + "" + urlComplement;
                        // } else {
                        // url = shttp + "://" + api.getHost() + ":" + api.getPort() + "/" + methode.getNomMethode() + "/" + urlComplement;
                        //    }
                        circuit = apiCircuit.guard(api);
                        if (!circuit.rejected()) {
                            switch (methode.getApi().getTypeAutorization()) {
                                case OAUTH2:
                                    // if (isTokenValid(tokeinfo) == false) {
                                    tokeinfo = tokenCacheService.tokenCache(methode.getApi());
                                    //   }
                                    AuthTokenInfo token = tokeinfo;
                                    resultat = envoyer(circuit, api, codeMethode, methode.getTypeAppel().toString(), parametreUser, map,
                                            () -> partnerHttp.call(url, token, mapParametre, methode.getTypeAppel().toString(), "1", methode.getTypeReponse().toString(), map));
                                    break;
                                case BASIC:
                                    resultat = PartnerHttpClient.result(ErreurConstants.FONCTION_NON_IMPLEMENTE, "Non implementé!!!", "1", null, methode.getTypeReponse().toString());
                                    break;
                                case OAUTH:
                                    resultat = PartnerHttpClient.result(ErreurConstants.FONCTION_NON_IMPLEMENTE, "Non implementé!!!", "1", null, methode.getTypeReponse().toString());
                                    break;
                                case NOAUTH:
                                    resultat = envoyer(circuit, api, codeMethode, methode.getTypeAppel().toString(), parametreUser, map,
                                            () -> partnerHttp.call(url, null, mapParametre, methode.getTypeAppel().toString(), "1", methode.getTypeReponse().toString(), map));
                                    break;
                            }
                            circuit.record(resultat);
                        } else {
                            circuitBreaker = true;
                            log.info("BREAK CIRCUIT=====================");
                            resultat = apiCircuit.unavailable(methode.getTypeReponse().toString(), "Ressource indisponible!!!");

                        }
                        break;

                }
            } catch (Exception e) {
                exceptionOccured = true;
                circuit.record(e);
                resultat = new ResultMessage();
                resultat.setMsg("Une erreur est survenue lors de l'appel");
                resultat.setErrCode(ErreurConstants.UNKNOWN_ERROR);
            } finally {
                tempsFin = System.currentTimeMillis();
                duree = tempsFin - tempsDebut;
                String codeReponse = String.valueOf(resultat.getErrCode());
                operationService.enregistreHistoriqueAppel(api, methode, codeReponse, resultat.getMsg(), duree);
            }

        }

//       } finally {
//           /* tempsFin = System.currentTimeMillis();
//            duree = tempsFin - tempsDebut;
//            String codeReponse = String.valueOf(resultat.getErrCode());
//
//*/
//            //metricManager.incrementPartnerApiCall("OMNICANAL_BOUNDARY_BY_TYPE_METHOD_API_CALL", api.getFournisseurService().getNom(), api.getNomApi(), methode.getNomMethode(), result);
//        }
        return ResponseEntity.ok().body(resultat);
    }

    @PostMapping("/boundary")
    @Timed
    public ResponseEntity<?> getApiResult(@RequestParam("codeFonction") String codeFonction, @RequestParam("otrParametre") String otrParametre, HttpServletRequest req, HttpServletResponse res) {
        /* find param of Api
         codeMethode

         get fonction paramater

         */
        MethodeFonction methode = methodeService.findBycodeFonction(codeFonction);
        ResultMessage resultat = null;
        if (methode == null) {
            resultat = PartnerHttpClient.result(ErreurConstants.RESSOURCE_ACCESS_ERROR, "Ressource temporairement indisponible", "1", null, null);
        } else {
            Api api = methode.getApi();

            String shttp;
            String baseUrl = null;
            String url;
            AuthTokenInfo tokeinfo = null;
            log.info(req.getHeader("X-AppID"));
//        Map<String, Object> map = PartnerHttpClient.appHeaders(req);
            switch (methode.getApi().getType()) {
                case REST:
                    ConcurrentHashMap<String, Object> mapParametre;

                    List<ParametreMethod> result = paramMethode.findByMethode(methode.getId());
                    String urlComplement = getuLRParametres(result); // on constitue l'url pour l'appel
                    List<ParametreUtilisateur> parametreUser = getUserParametre(otrParametre, null); // on recupère les paramètres utilisateur
                    mapParametre = getParametres(parametreUser, result);
                    if (methode.getApi().isUseSsl() == true) {
                        shttp = "https";
                    } else {
                        shttp = "http";
                    }

                    baseUrl = getUrl(api);
                    //  if (baseUrl != null) {
                    url = baseUrl + "/" + methode.getNomMethode() + "" + urlComplement;
                    //     } else {
                    // url = shttp + "://" + api.getHost() + ":" + api.getPort() + "/" + methode.getNomMethode() + "/" + urlComplement;
                    //   }
                    log.debug("appel à l'API " + url);
                    Map<String, Object> headers = new HashMap<>();
                    switch (methode.getApi().getTypeAutorization()) {
                        case OAUTH2:
                            if (isTokenValid(tokeinfo) == false) {
                                tokeinfo = tokenCache(methode.getApi());
                            }

                            AuthTokenInfo token = tokeinfo;
                            resultat = envoyer(null, api, codeFonction, methode.getTypeAppel().toString(), parametreUser, headers,
                                    () -> partnerHttp.call(url, token, mapParametre, methode.getTypeAppel().toString(), "1", methode.getTypeReponse().toString(), headers));
                            break;
                        case BASIC:
                            resultat = PartnerHttpClient.result(ErreurConstants.FONCTION_NON_IMPLEMENTE, "Non implementé!!!", "1", null, methode.getTypeReponse().toString());
                            break;
                        case OAUTH:
                            resultat = PartnerHttpClient.result(ErreurConstants.FONCTION_NON_IMPLEMENTE, "Non implementé!!!", "1", null, methode.getTypeReponse().toString());
                            break;
                        case NOAUTH:
                            resultat = envoyer(null, api, codeFonction, methode.getTypeAppel().toString(), parametreUser, headers,
                                    () -> partnerHttp.call(url, null, mapParametre, methode.getTypeAppel().toString(), "1", methode.getTypeReponse().toString(), headers));
                            break;
                    }

                    break;

            }

        }
        return ResponseEntity.ok().body(resultat);
    }

    private List<ParametreUtilisateur> getUserParametre(String otrParametre, InterfaceUser interfaceUser) {
        String valeur;
        List<ParametreUtilisateur> listparamUser = new ArrayList<>();
        String[] listeParametre = otrParametre.split(Pattern.quote("|"));
        String codeParametre;
        for (String parametres : listeParametre) {
            String[] parametre = parametres.split(Pattern.quote("="));
            codeParametre = parametre[0];
            valeur = parametre[1];
            ParametreUtilisateur param = new ParametreUtilisateur();
            param.setValeur(valeur);
            param.setCodeParametre(codeParametre);
            param.setInterfaceUtilisateur(interfaceUser);
            listparamUser.add(param);
        }
        return listparamUser;
    }

    private String getuLRParametres(List<ParametreMethod> listparamethode) {
        ParametreMethod elementParam = null;
        int i = 1;
        String url = "?";
        String separateur = "&";
        String urlInterm;
        Iterator itParam = listparamethode.iterator();
        while (itParam.hasNext()) {
            elementParam = (ParametreMethod) itParam.next();
            if (elementParam.getNomExterne() != null) {
                if (i < listparamethode.size()) {
                    urlInterm = elementParam.getNomExterne() + "=" + "{" + elementParam.getNomExterne() + "}" + separateur;
                } else {
                    urlInterm = elementParam.getNomExterne() + "=" + "{" + elementParam.getNomExterne() + "}";
                }
                url = url + urlInterm;
                i = i + 1;
            }

        }

        return url;
    }

    private ConcurrentHashMap<String, Object> getParametres(List<ParametreUtilisateur> listparam, List<ParametreMethod> listparamethode) {
        ConcurrentHashMap<String, Object> mapParametre = new ConcurrentHashMap<>();
        for (ParametreMethod elementParam : listparamethode) {
            String valeur = valeurDuParametre(listparam, elementParam);
            if (renseigne(valeur)) {
                mapParametre.put(elementParam.getNomExterne(), valeur);
                log.info("Variable================= {}", elementParam.getNomExterne() + ":" + valeur);
            }
        }
        return mapParametre;
    }

    private String valeurDuParametre(List<ParametreUtilisateur> listparam, ParametreMethod elementParam) {
        String valeur = premiereValeur(listparam,
                elementParam.getCodeParametre(),
                elementParam.getCodeParametreFonction(),
                elementParam.getCodeParametreRevers());
        if (!renseigne(valeur)) {
            valeur = valeurLiee(listparam, elementParam);
        }
        if (!renseigne(valeur) && renseigne(elementParam.getValeur())) {
            valeur = elementParam.getValeur();
        }
        return valeur;
    }

    private String valeurLiee(List<ParametreUtilisateur> listparam, ParametreMethod elementParam) {
        if (listparam == null || elementParam == null) {
            return null;
        }
        for (ParametreUtilisateur param : listparam) {
            if (!renseigne(param.getValeur())) {
                continue;
            }
            if (memeCode(param.getCodeParametreMethode(), elementParam.getCodeParametre())
                    || memeCode(param.getCodeParametreMethode(), elementParam.getCodeParametreFonction())
                    || memeCode(param.getCodeParametreFonction(), elementParam.getCodeParametre())
                    || memeCode(param.getCodeParametreRevers(), elementParam.getCodeParametre())) {
                return param.getValeur();
            }
        }
        return null;
    }

    private String premiereValeur(List<ParametreUtilisateur> listparam, String... codes) {
        if (codes == null) {
            return null;
        }
        for (String code : codes) {
            String valeur = rechercheParamMthode(listparam, code);
            if (renseigne(valeur)) {
                return valeur;
            }
        }
        return null;
    }

    private static boolean memeCode(String gauche, String droite) {
        return renseigne(gauche) && gauche.equals(droite);
    }

    private static boolean renseigne(String valeur) {
        return valeur != null && !valeur.isBlank();
    }

    private String rechercheParamMthode(List<ParametreUtilisateur> listparam, String code) {
        if (!renseigne(code) || listparam == null) {
            return null;
        }
        for (ParametreUtilisateur param : listparam) {
            if (code.equals(param.getCodeParametre())) {
                return param.getValeur();
            }
        }
        return null;
    }

    public boolean isTokenValid(AuthTokenInfo token) {

        if (token != null) {
            Date now = new Date();
            Long tim = now.getTime();
            return token.getExpireTime() > tim;
        } else {
            return false;
        }

    }

    private String getOperationId(Operation op) throws JAXBException {
        OperationID operationID = new OperationID();
        operationID.setCodeOperation(op.getCodeOperation());
        operationID.setDateOperation(op.getDateOperation().format(DateTimeFormatter.ISO_DATE_TIME));
        String str = (String) Util.marshall(operationID, String.class);
        return str;
    }

    private AuthTokenInfo tokenCache(Api api) {
        final String key = api.getCodeApi().trim();
        AuthTokenInfo resultat = cache.get(key);
        AuthTokenInfo tokeinfo;

        if (resultat != null) {
            if (isTokenValid(resultat)) {
                return resultat;
            } else {
                tokeinfo = getTokenOauth2(api);
                cache.put(key, tokeinfo);

                return tokeinfo;
            }

        } else {
            tokeinfo = getTokenOauth2(api);
            cache.put(key, tokeinfo);
            return tokeinfo;
        }
    }

    private AuthTokenInfo getTokenOauth2(Api api) {
        String urlServ = getUrl(api) + "/oauth/token";
        String grant = PartnerHttpClient.passwordGrant(api.getPassWord(), api.getLogin(), api.getGrantType(),
                api.getClientSercret(), api.getClientId());
        return partnerHttp.requestToken(urlServ, grant, api.getClientId(), api.getClientSercret());
    }

    @PostMapping(path = "omnicanalPresentation")
    @Timed
    public  ResponseEntity<?> getOmnicanal(@RequestBody ParametreDTO paramDto) throws ParseException, Exception {
        //   Iterator itPdto = paramDto.iterator();
        List<ParametreUtilisateur> listparamUser = new ArrayList<>();
        Transaction trans = null;
        List<ResultMessage> listMessageTransaction = new ArrayList<>();
        ResultMessage messageTransaction = null;
        ObjectMapper mapper = new ObjectMapper();
        TypeService typeService = null;
        //  while(itPdto.hasNext()){
        // ParametreDTO parametreDTO  =(ParametreDTO) itPdto.next();
        ProprieteApi apiUtil = paramDto.getProprieteApi();
        String codeService = paramDto.getCodeService();
        String codeUo = paramDto.getCodeUniteOrganisation();
        typeService = typeServiceService.findByCodeTypeService(codeService);

        log.info("LES PARAMETRES D'ENTREE AU NIVEAU DE LA COUCHE PRESENTATION");
        String canal = paramDto.getCanal();

        for (ParametreUser par : paramDto.getParam()) {

            ParametreUtilisateur param = new ParametreUtilisateur();
            param.setValeur(par.getValeur());
            param.setCodeParametre(par.getCodeParametre());
            /**
             * Recherche l'interface utilisateur concerné par le type service **
             */
            log.info("Variable================= {}", par.getCodeParametre() + ":" + par.getValeur());
            param.setInterfaceUtilisateur(par.getInterfaceUtilisateur());
            param.setParametreApi(apiUtil);
            listparamUser.add(param);
        }

        //  typeService= interfaceUser.getService().getTypeService();
        //  InterfaceUtilisateur interfaceUt= interfaceUser;
        ResultMessage resultat = new ResultMessage();
        String couche = "3";
        List<ResultMessage> resMsg = new ArrayList<ResultMessage>();

        List<ResultMessage> res = null;
        JSONObject object = new JSONObject();
        List<JSONObject> tabobject = new ArrayList<JSONObject>();
        trans = presentationService.findTransaction(codeUo, typeService, listparamUser, paramDto.getReferenceExterne(), canal);
        JSONObject json = new JSONObject();
        List<String> list = new ArrayList<String>();
        boolean bool;
        JsonParser p = new JsonParser();

        if (trans != null) {
            Integer nbreLigne;
            if (trans.getLigneTransaction() != null) {
                nbreLigne = trans.getLigneTransaction().size();
            }
            AuthTokenInfo tokeinfo = null;
            String s = null;
            ConcurrentHashMap<String, Object> param = null;
            TransactionDTO transDTO = new TransactionDTO(trans, resMsg);
         
            List<ResultMessage> resulMsg = transactionProcessorService.processTransaction(transDTO);
            // resMsg = resClient.getRessource(tokeinfo, transDTO, resMsg, couche, "JSON");

            resMsg.add(PartnerHttpClient.result(ErreurConstants.SUCCESS, "Opération terminée avec Succès", couche, resulMsg, "JSON"));

            resultat = presentationService.findByNumeroCouche(resMsg, couche);
            if (resultat != null && resultat.getErrCode() == ErreurConstants.SUCCESS) {
                mapper = new ObjectMapper();
                res = presentationService.findResponseEntity(resMsg, "3");
                listMessageTransaction = mapper.convertValue(res, new TypeReference<List<ResultMessage>>() {
                });
            } else {
                if (resultat == null) {
                    messageTransaction = presentationService.getResultMessage("3", ErreurConstants.HTTP_CLIENT_STATUS_ERROR, "Une erreur lors de la requete", "JSON", null);
                    listMessageTransaction.add(messageTransaction);
                } else {
                    messageTransaction = presentationService.getResultMessage(resultat.getNumCouche(), resultat.getErrCode(), resultat.getMsg(), "JSON", null);
                    listMessageTransaction.add(messageTransaction);
                }
            }
        } else {
            ReponseOperation refus = new ReponseOperation();
            refus.setCodeErr(String.valueOf(ErreurConstants.FSE_INEXISTANT));
            refus.setMsg("FSE Inexistant ou schema inexistant");
            refus.setStatut(ReponseOperation.ECHEC);
            ReponseOperation.OperationResume resume = new ReponseOperation.OperationResume();
            resume.setReferenceExterne(paramDto.getReferenceExterne());
            refus.setOperation(resume);
            log.info("RESULTAT codeErr={} msg={} statut={}", refus.getCodeErr(), refus.getMsg(), refus.getStatut());
            return ResponseEntity.ok().body(refus);
        }

        ReponseOperation mesRetour = new ReponseOperation();
        Integer nombreNotOk;
        Integer nombreExecute;

        Oper op = operationService.findOne(trans.getId());
        List<RetourMessage> listExternalInfos = new ArrayList();
        ExternalInfos externalInfos = new ExternalInfos();
        nombreNotOk = ligneOperationRepository.findLigneNotOk(op.getId());
        nombreExecute = ligneOperationRepository.findLigneExecute(op.getId());
        renseignerRetour(mesRetour, listMessageTransaction, listExternalInfos, op, paramDto.getReferenceExterne(), nombreExecute, nombreNotOk);
        externalInfos.setListExternalInfos(listExternalInfos);
        String strExternalInfos = (String) Util.marshall(externalInfos, String.class);
        op.setReponse(mesRetour.getMsg());
        op.setCodeMsg(mesRetour.getCodeErr());
        op.setExternalInfos(strExternalInfos);
        String str = getOperationId(op);

        mesRetour.setText(str);
        log.info("RESULTAT codeErr={} msg={} statut={}", mesRetour.getCodeErr(), mesRetour.getMsg(), mesRetour.getStatut());
        operationService.save(op);

        return ResponseEntity.ok().body(mesRetour);

    }

    private void renseignerRetour(ReponseOperation mesRetour, List<ResultMessage> lignes,
            List<RetourMessage> listExternalInfos, Oper op, String referenceDemande,
            Integer nombreExecute, Integer nombreNotOk) throws Exception {
        int succesDistants = 0;
        int echecsDistants = 0;
        StringBuilder messages = new StringBuilder();
        int ordre = 1;

        if (lignes != null) {
            for (ResultMessage ligne : lignes) {
                ReponseOperation.LigneReponse detail = lireLigne(ligne, listExternalInfos);
                detail.setOrdre(ordre++);
                mesRetour.getLignes().add(detail);
                if (detail.isLocale()) {
                    continue;
                }
                if ("0".equals(detail.getCodeErr())) {
                    succesDistants++;
                } else {
                    echecsDistants++;
                }
                ajouterMessage(messages, detail.getMsg());
            }
        }

        if (lignes == null || lignes.isEmpty()) {
            mesRetour.setStatut(ReponseOperation.ECHEC);
            mesRetour.setMsg("Aucune ligne traitée");
        } else if (echecsDistants > 0 && succesDistants > 0) {
            mesRetour.setStatut(ReponseOperation.PARTIEL);
            mesRetour.setMsg(messages.toString());
        } else if (echecsDistants > 0) {
            mesRetour.setStatut(ReponseOperation.ECHEC);
            mesRetour.setMsg(messages.length() == 0 ? "Erreur d'execution" : messages.toString());
        } else {
            mesRetour.setStatut(ReponseOperation.SUCCES);
            mesRetour.setMsg(messages.length() == 0 ? MESSAGE_DUMMY : messages.toString());
        }
        String codePartenaire = codePartenaire(mesRetour);
        mesRetour.setCodeErr(codePartenaire != null
                ? codePartenaire
                : (ReponseOperation.SUCCES.equals(mesRetour.getStatut()) ? "0" : "-1"));

        int executees = nombreExecute == null ? 0 : nombreExecute;
        int enEchec = nombreNotOk == null ? 0 : nombreNotOk;
        if (codePartenaire == null && ReponseOperation.SUCCES.equals(mesRetour.getStatut()) && executees == 0) {
            mesRetour.setCodeErr("-1");
            mesRetour.setStatut(ReponseOperation.ECHEC);
            mesRetour.setMsg("Erreur d'execution");
        } else if (ReponseOperation.SUCCES.equals(mesRetour.getStatut()) && enEchec > 0) {
            mesRetour.setCodeErr("-1");
            mesRetour.setStatut(succesDistants > 0 ? ReponseOperation.PARTIEL : ReponseOperation.ECHEC);
        }

        ReponseOperation.OperationResume resume = new ReponseOperation.OperationResume();
        resume.setCode(op.getCodeOperation());
        if (op.getDateOperation() != null) {
            resume.setDate(op.getDateOperation().format(DateTimeFormatter.ISO_DATE_TIME));
        }
        resume.setReferenceExterne(op.getReference() != null ? op.getReference() : referenceDemande);
        mesRetour.setOperation(resume);
    }

    private static String codePartenaire(ReponseOperation retour) {
        if (retour == null || retour.getLignes() == null) {
            return null;
        }
        String retenu = null;
        for (ReponseOperation.LigneReponse ligne : retour.getLignes()) {
            if (ligne == null || ligne.getCodeErr() == null) {
                continue;
            }
            if ("10".equals(ligne.getCodeErr())) {
                return "10";
            }
            if ("20".equals(ligne.getCodeErr())) {
                retenu = "20";
            }
        }
        return retenu;
    }

    private ReponseOperation.LigneReponse lireLigne(ResultMessage ligne,
            List<RetourMessage> listExternalInfos) throws Exception {
        ReponseOperation.LigneReponse detail = new ReponseOperation.LigneReponse();
        detail.setLocale(false);
        if (ligne == null) {
            detail.setCodeErr("-1");
            detail.setMsg("Ligne sans résultat");
            return detail;
        }
        if (ligne.getErrCode() == com.m2i.omnicanal.presentation.execution.ExecutionGate.OTP_REQUIS
                || ligne.getErrCode() == com.m2i.omnicanal.presentation.execution.ExecutionGate.ACCEPTE) {
            detail.setCodeErr(String.valueOf(ligne.getErrCode()));
            detail.setMsg(ligne.getMsg() != null ? ligne.getMsg() : "En attente");
            return detail;
        }
        if (ligne.getErrCode() != 0 || ligne.getResponseEntity() == null) {
            detail.setCodeErr("-1");
            detail.setMsg(ligne.getMsg() != null ? ligne.getMsg() : "Réponse vide");
            return detail;
        }

        MessageRet retour = convertirLigne(ligne.getResponseEntity());
        if (retour != null && retour.getText() != null) {
            RetourMessage retourMessage = (RetourMessage) Util.unmarshall(retour.getText(), RetourMessage.class);
            listExternalInfos.add(retourMessage);
        }
        if (retour == null || retour.getCodeErr() == null) {
            detail.setCodeErr("-1");
            detail.setMsg(texteBrut(ligne.getResponseEntity()));
            return detail;
        }
        if ("0".equals(retour.getCodeErr()) && MESSAGE_DUMMY.equals(retour.getMsg())) {
            detail.setLocale(true);
            detail.setCodeErr("0");
            detail.setMsg(retour.getMsg());
            return detail;
        }
        detail.setCodeErr(retour.getCodeErr());
        detail.setMsg(retour.getMsg() != null ? retour.getMsg() : "Erreur d'execution");
        return detail;
    }

    private MessageRet convertirLigne(Object corps) {
        try {
            return (MessageRet) PartnerHttpClient.convert(corps, MessageRet.class);
        } catch (Exception ex) {
            log.info("Corps de ligne illisible");
            return null;
        }
    }

    private String texteBrut(Object corps) {
        if (corps instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) corps;
            Object description = map.get("description");
            Object message = map.get("message");
            if (description != null) {
                return String.valueOf(description);
            }
            if (message != null) {
                return String.valueOf(message);
            }
        }
        return corps == null ? "Réponse vide" : String.valueOf(corps);
    }

    private void ajouterMessage(StringBuilder messages, String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        if (messages.length() > 0) {
            messages.append('\n');
        }
        messages.append(message);
    }

    @PostMapping("/checkOperationStatus")
    @Timed
    public ResponseEntity<Message> checkOperationStatus(@RequestParam("referenceExterne") String referenceExterne) {
        Message mesRetour = new Message();
        mesRetour.setText(null);
        mesRetour.setCodeErr(ErreurConstants.SUCCESS + "");
        Oper oper = operationService.findByReferenceEx(referenceExterne);
        if (oper != null) {
            if (oper.getExternalInfos() != null) {
                mesRetour.setText(oper.getExternalInfos());
            }
        } else {
            mesRetour.setMsg("Erreur");
            mesRetour.setCodeErr(ErreurConstants.UNKNOWN_ERROR + "");
        }
        return ResponseEntity.ok().body(mesRetour);
    }
}
