/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.m2i.omnicanal.presentation.domain.MethodeFonction;
import com.m2i.omnicanal.presentation.domain.Parameters;
import com.m2i.omnicanal.presentation.domain.ParametreMethod;
import com.m2i.omnicanal.presentation.repository.MethodeRepository;
import com.m2i.omnicanal.presentation.service.MiddlewareService;

import com.m2i.omnicanal.presentation.service.dto.LigneTransactionDTO;
import com.m2i.omnicanal.model.Api;
import com.m2i.omnicanal.model.Parametre;
import com.m2i.omnicanal.model.Resultat;
import com.m2i.omnicanal.outils.restclient.AuthTokenInfo;
import com.m2i.omnicanal.presentation.http.PartnerHttpClient;
import com.m2i.omnicanal.outils.restclient.constants.ErreurConstants;
import com.m2i.omnicanal.outils.restclient.model.Message;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author cda
 */
@RestController
@RequestMapping("/api")
public class MiddleWareResource {

    private final Logger log = LoggerFactory.getLogger(MiddleWareResource.class);
    private final MethodeRepository methodeRepository;
    private final MiddlewareService middlewareService;
    private final PartnerHttpClient partnerHttp;

    public MiddleWareResource(MethodeRepository methodeRepository, MiddlewareService middlewareService,
            PartnerHttpClient partnerHttp) {
        this.methodeRepository = methodeRepository;
        this.middlewareService = middlewareService;
        this.partnerHttp = partnerHttp;
    }

    /*
    public MiddleWareResource(ParametreMethodeService paramMethode, OperationService operationService, MethodeRepository methodeRepository, @Value("${param.proxyenable}") boolean proxyenabled) {
        this.paramMethode = paramMethode;
        this.operationService = operationService;
        this.proxyenabled = proxyenabled;
        this.methodeRepository = methodeRepository;
    }
     */
    /**
     * Fonction middleware
     *
     * @param lg
     * @return
     */
    @PostMapping("/middleware")
    @Timed
    public ResultMessage getAllMiddleWare(@RequestBody LigneTransactionDTO lg) {
        return middlewareService.execute(lg);
    }

    @PostMapping("/error")
    @Timed
    @Transactional(readOnly = true)
    public ResultMessage getAllMiddleWareError(@RequestBody LigneTransactionDTO lg) {
        MethodeFonction methode = new MethodeFonction();
        ResultMessage resultat = null;
        methode = methodeRepository.findOne(lg.getLigneTransaction().getMethodeApi().getId());
        try {
            Message msg = new Message();
            msg.setCodeErr("-1");
            msg.setMsg("Erreur d'execution");
            resultat = PartnerHttpClient.result(ErreurConstants.HTTP_CODE_STATUS_ERROR, "Error lors de lexecution", "1", msg, methode.getTypeReponse().toString());
        } catch (Exception e) {
            resultat = PartnerHttpClient.result(ErreurConstants.UNKNOWN_ERROR, "Erreur inconnue!!!", "1", null, methode.getTypeReponse().toString());
            return resultat;

        }

        return resultat;

    }

    @GetMapping("/check")
    @Timed
    public String getCheckMiddleWareDummy() {
        RestTemplate restTemplate = new RestTemplate();
        String res = restTemplate.getForObject("http://localhost:8080/management/health", String.class);
        return res;

    }

    /**
     * cette fonction n'appel accun api
     *
     *
     */
    @PostMapping("/dummy")
    @Timed
    @Transactional(readOnly = true)
    public ResultMessage getAllMiddleWareDummy(@RequestBody LigneTransactionDTO lg) {
        return middlewareService.dummy(lg);
    }

    /**
     *
     * @param api
     * @return
     */
    private String getUrl(Api api) {
        String baseUrl = null;
        String codeApi = "";
        String shttp;
        String application = api.getNomApi();

        if (api.isUseSsl() == true) {
            shttp = "https";
        } else {
            shttp = "http";
        }
        if ((api.getUseProxy() == true)) {
            codeApi = api.getCodeApi();
        }

        baseUrl = shttp + "://" + api.getHost() + ":" + api.getPort() + "/" + codeApi;

        return baseUrl;
    }

    /**
     *
     * @param api
     * @return
     */
    private AuthTokenInfo getTokenOauth(Api api) {
        String grant = PartnerHttpClient.passwordGrant(api.getPassWord(), api.getLogin(), api.getGrantType(),
                api.getClientSercret(), api.getClientId());
        return partnerHttp.requestToken(getUrl(api) + "/oauth/token", grant, api.getClientId(), api.getClientSercret());
    }

    /**
     *
     * @param api
     * @return
     */
    private AuthTokenInfo getTokenOauth2(Api api) {
        String grant = PartnerHttpClient.passwordGrant(api.getPassWord(), api.getLogin(), api.getGrantType(),
                api.getClientSercret(), api.getClientId());
        return partnerHttp.requestToken(api.getAuthentificationUrl(), grant, api.getClientId(), api.getClientSercret());
    }

    /**
     *
     * @param code
     * @param msg
     * @param couche
     * @param resultatDetail
     * @return
     */
    private Resultat setResultat(String code, String msg, String couche, List<?> resultatDetail) {
        Resultat res = new Resultat();
        res.setErrCode(code);
        res.setMsg(msg);
        res.setNumCouche(couche);
        res.setResultatDetail(resultatDetail);
        return res;
    }

}
