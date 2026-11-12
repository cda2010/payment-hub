/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service;

import com.m2i.omnicanal.model.Api;
import com.m2i.omnicanal.outils.restclient.AuthTokenInfo;
import com.m2i.omnicanal.presentation.http.PartnerHttpClient;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 *
 * @author cda
 */
@Service
public class TokenCacheService {

    private final ConcurrentHashMap<String, AuthTokenInfo> cache = new ConcurrentHashMap<>();
    private final PartnerHttpClient partnerHttp;

    public TokenCacheService(PartnerHttpClient partnerHttp) {
        this.partnerHttp = partnerHttp;
    }

    public AuthTokenInfo tokenCache(Api api) {
        final String key = api.getCodeApi().trim();
        AuthTokenInfo resultat = cache.get(key);
        AuthTokenInfo tokeinfo;

        if (resultat != null) {
            if (isTokenValid(resultat)) {
                return resultat;
            } else {
                tokeinfo = getTokenOauth(api);
                if (tokeinfo != null) {
                    cache.put(key, tokeinfo);
                }

                return tokeinfo;
            }

        } else {
            tokeinfo = getTokenOauth(api);
            if (tokeinfo != null) {
                cache.put(key, tokeinfo);
            }

            return tokeinfo;
        }
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

    private AuthTokenInfo getTokenOauth(Api api) {
        String grant = PartnerHttpClient.passwordGrant(api.getPassWord(), api.getLogin(), api.getGrantType(),
                api.getClientSercret(), api.getClientId());
        return partnerHttp.requestToken(getUrl(api) + "/oauth/token", grant, api.getClientId(), api.getClientSercret());
    }
}
