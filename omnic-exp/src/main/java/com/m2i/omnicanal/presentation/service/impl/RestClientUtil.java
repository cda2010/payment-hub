/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service.impl;

import com.m2i.omnicanal.model.Api;
import com.m2i.omnicanal.model.enumeration.PrioriteExec;
import com.m2i.omnicanal.outils.restclient.AuthTokenInfo;
import com.m2i.omnicanal.presentation.http.PartnerHttpClient;
import com.m2i.omnicanal.outils.restclient.constants.ErreurConstants;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.m2i.omnicanal.presentation.domain.MessageRetour;
import com.m2i.omnicanal.presentation.domain.MethodeFonction;
import com.m2i.omnicanal.presentation.domain.ParametreMethod;
import com.m2i.omnicanal.presentation.repository.LigneOperationRepository;
import com.m2i.omnicanal.presentation.repository.OperationRepository;
import com.m2i.omnicanal.presentation.service.ParametreMethodeService;
import com.m2i.omnicanal.presentation.service.dto.LigneTransactionDTO;
import com.m2i.omnicanal.presentation.service.kafka.MessageProducer;
import com.m2i.omnicanal.presentation.web.rest.MiddleWareResource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 *
 * @author cda
 */
@Service
public class RestClientUtil {

    private final Logger log = LoggerFactory.getLogger(MiddleWareResource.class);
    private final ParametreMethodeService paramMethode;
    private final PartnerHttpClient partnerHttp;
    String password;
    String url;
    String clientId;
    String secretWord;

    String login;

    public RestClientUtil(ParametreMethodeService paramMethode, PartnerHttpClient partnerHttp,
            @Value("${connexion.login}") String login,
            @Value("${connexion.password}") String password, @Value("${connexion.url}") String url,
            @Value("${connexion.clientId}") String clientId, @Value("${connexion.secretWord}") String secretWord) {
        this.partnerHttp = partnerHttp;
        this.password = password;
        this.login = login;
        this.clientId = clientId;
        this.url = url;
        this.secretWord = secretWord;
        this.paramMethode = paramMethode;
    }

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

    private AuthTokenInfo getTokenOauth(Api api) {
        String grant = PartnerHttpClient.passwordGrant(api.getPassWord(), api.getLogin(), api.getGrantType(),
                api.getClientSercret(), api.getClientId());
        return partnerHttp.requestToken(getUrl(api) + "/oauth/token", grant, api.getClientId(), api.getClientSercret());
    }

    public ResultMessage pingCall(MethodeFonction methode) {
        String shttp;
        String codeApi;
        ResultMessage resultat = null;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("X-AppID", "CORISMONEY");
        switch (methode.getApi().getType()) {
            case REST:
                ConcurrentHashMap<String, Object> mapParametre = new ConcurrentHashMap();
                String urlComplement;
                List<ParametreMethod> result = paramMethode.findByMethode(methode.getId());
                urlComplement = "";//getuLRParametres(result);
                mapParametre.put("0", "0");
                if (methode.getApi().isUseSsl() == true) {
                    shttp = "https";
                } else {
                    shttp = "http";

                }
                String url;
                if ((methode.getApi().getUseProxy() == true)) {
                    codeApi = methode.getApi().getCodeApi();
                    url = shttp + "://" + methode.getApi().getHost() + ":" + methode.getApi().getPort() + "/" + codeApi + "/" + methode.getNomMethode() + "/" + urlComplement;
                } else {
                    url = shttp + "://" + methode.getApi().getHost() + ":" + methode.getApi().getPort() + "/" + methode.getNomMethode() + "/" + urlComplement;
                }
                log.debug("Appel de l'api {}", url);

                switch (methode.getApi().getTypeAutorization()) {
                    case OAUTH2:
                        AuthTokenInfo tokeinfo = getTokenOauth(methode.getApi());
                        resultat = partnerHttp.call(url, tokeinfo, mapParametre, methode.getTypeAppel().toString(), "1", methode.getTypeReponse().toString(), map);
                        break;
                    case BASIC:
                        resultat = PartnerHttpClient.result(ErreurConstants.FONCTION_NON_IMPLEMENTE, "Non implementé!!!", "1", null, methode.getTypeReponse().toString());
                        break;
                    case OAUTH:
                        resultat = PartnerHttpClient.result(ErreurConstants.FONCTION_NON_IMPLEMENTE, "Non implementé!!!", "1", null, methode.getTypeReponse().toString());
                        break;
                    case NOAUTH:
                        resultat = partnerHttp.call(url, null, mapParametre, methode.getTypeAppel().toString(), "1", methode.getTypeReponse().toString(), map);
                        break;
                }

                break;
            case WEBSERVICE:
                resultat = PartnerHttpClient.result(ErreurConstants.FONCTION_NON_IMPLEMENTE, "Non implementé!!!", "1", null, methode.getTypeReponse().toString());
                break;

        }
        return resultat;
    }

}
