package com.m2i.pti.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.m2i.omnicanal.outils.restclient.AuthTokenInfo;
import com.m2i.omnicanal.outils.restclient.RestClient;
import com.m2i.pti.mobile.dto.CredentialBack;
import com.m2i.pti.mobile.dto.MtypeDoc;

@Service
public class BackService {
    private final String login;
    private final String password;
    private final String url;
    private final CredentialBack credentialBack;
    private final RestClient restClient;
    public BackService( @Value("${connexionBack.login}") String login,@Value("${connexionBack.password}") String password,@Value("${connexionBack.url}") String url) {
        this.login = login;
        this.password = password;
        this.url = url;
        credentialBack  = new CredentialBack();
        credentialBack.setUsername(this.login);
        credentialBack.setPassword(this.password);
        this.restClient = new RestClient();
        this.restClient.setLogin(login);
        this.restClient.setPassword(password);
        restClient.setGrantUri("?username=" + login + "&password=" + password);
        this.restClient.setSecureServerUri(this.url+"/api/authenticate");
    }
    /*
    Service de test
     */

    public List<MtypeDoc> getTypeDoc() throws IOException {
        AuthTokenInfo tokeinfo = restClient.sendTokenRequestJWT(credentialBack);
        System.out.println(tokeinfo);

        restClient.setRestServerUri(this.url + "/api/pti/typedoc");
        ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
        //ResponseEntity result = restClient.getRessources(tokeinfo, param, "GET");
       // System.out.println(result);
        List<MtypeDoc> li = new ArrayList();
        //List<MtypeDoc> list = ( List<MtypeDoc>) restClient.convert(result.getBody(),li.getClass());
        //return list;
        return null;
    }

    public ResponseEntity resetPwd(String login) throws IOException {
        AuthTokenInfo tokeinfo = restClient.sendTokenRequestJWT(credentialBack);
        System.out.println(tokeinfo);
        String otrParametre = login;
        restClient.setRestServerUri(this.url + "/api/resetpassword/"+login);
        ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
        param.put("otrParametre", otrParametre);
        //ResponseEntity result = restClient.getRessources(tokeinfo, param, "GET");
        //System.out.println(result);
        //return result;
        return null;
    }

    public ResponseEntity checkuser(String telephone) throws IOException {
        AuthTokenInfo tokeinfo = restClient.sendTokenRequestJWT(credentialBack);
        String otrParametre = telephone;
        restClient.setRestServerUri(this.url + "/api/checkuserbytelephone/"+telephone);
        ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
        param.put("otrParametre", otrParametre);
        ResponseEntity result = restClient.getRessources(tokeinfo, param, "GET");
        return result;

    }
}
