package com.m2i.omnicanal.presentation.service;

import com.m2i.omnicanal.model.Api;
import com.m2i.omnicanal.model.Parametre;
import com.m2i.omnicanal.outils.restclient.AuthTokenInfo;
import com.m2i.omnicanal.presentation.execution.PartnerExecutionService;
import com.m2i.omnicanal.presentation.http.PartnerHttpClient;
import com.m2i.omnicanal.outils.restclient.constants.ErreurConstants;
import com.m2i.omnicanal.outils.restclient.model.Message;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.m2i.omnicanal.presentation.domain.MethodeFonction;
import com.m2i.omnicanal.presentation.resilience.ApiCircuitBreaker;
import com.m2i.omnicanal.presentation.domain.ParametreMethod;
import com.m2i.omnicanal.presentation.repository.MethodeRepository;
import com.m2i.omnicanal.presentation.service.dto.LigneTransactionDTO;
import java.util.HashMap;
import java.util.function.Supplier;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Exécute une ligne de transaction vers le FSE, ou renvoie le succès dummy.
 * Appelé directement par le processeur et par les endpoints HTTP.
 */
@Service
public class MiddlewareService {

    private static final Logger log = LoggerFactory.getLogger(MiddlewareService.class);

    private final ParametreMethodeService paramMethode;
    private final OperationService operationService;
    private final MethodeRepository methodeRepository;
    private final TokenCacheService tokenCacheService;
    private final ApiCircuitBreaker apiCircuit;
    private final PartnerHttpClient partnerHttp;
    private final PartnerExecutionService partnerExecution;

    public MiddlewareService(TokenCacheService tokenCacheService, ParametreMethodeService paramMethode,
            OperationService operationService, MethodeRepository methodeRepository, ApiCircuitBreaker apiCircuit,
            PartnerHttpClient partnerHttp, PartnerExecutionService partnerExecution) {
        this.paramMethode = paramMethode;
        this.operationService = operationService;
        this.methodeRepository = methodeRepository;
        this.tokenCacheService = tokenCacheService;
        this.apiCircuit = apiCircuit;
        this.partnerHttp = partnerHttp;
        this.partnerExecution = partnerExecution;
    }

    private ResultMessage suivre(ApiCircuitBreaker.Guard circuit, Api api, String codeMethode, String httpMethod,
            Map<String, String> raw, Map<String, Object> headers, Supplier<ResultMessage> call) {
        PartnerExecutionService.Outcome outcome = partnerExecution.execute(api, codeMethode, httpMethod, raw, headers, call);
        if (outcome.partnerCalled()) {
            circuit.record(outcome.result());
        } else {
            circuit.ignore();
        }
        return outcome.result();
    }

    public ResultMessage execute(LigneTransactionDTO lg) {
        StopWatch stopWatch = new StopWatch("Measure Midlleware");
        long tempsDebut = System.currentTimeMillis();
        log.info("EXECUTION MIDDLEWARE");
        ResultMessage resultat = null;
        Api api = null;
        MethodeFonction methode = methodeRepository.findOne(lg.getLigneTransaction().getMethodeApi().getId());
        if (methode != null && methode.getApi() != null) {
            api = methode.getApi();
            ApiCircuitBreaker.Guard circuit = ApiCircuitBreaker.Guard.passthrough();
            try {
                switch (methode.getApi().getType()) {
                    case REST:
                        List<ParametreMethod> result = paramMethode.findByMethode(methode.getId());
                        String urlComplement = getUrlParametres(result);
                        ConcurrentHashMap<String, Object> mapParametre = getParametres(lg.getLigneTransaction().getParametreService(), result);
                        String shttp = methode.getApi().isUseSsl() ? "https" : "http";
                        String url;
                        if (methode.getApi().getUseProxy()) {
                            url = shttp + "://" + methode.getApi().getHost() + ":" + methode.getApi().getPort()
                                    + "/" + methode.getApi().getCodeApi() + "/" + methode.getNomMethode()  + urlComplement;
                        } else {
                            url = shttp + "://" + methode.getApi().getHost() + ":" + methode.getApi().getPort()
                                    + "/" + methode.getNomMethode()  + urlComplement;
                        }
                        log.debug("Appel de l'api {}", url);
                        circuit = apiCircuit.guard(api);
                        stopWatch.start("1. Execution de la Middleware");
                        if (!circuit.rejected()) {
                            Map<String, Object> headers = new HashMap<>();
                            if (lg.getTerminal() != null) {
                                headers.put("X-AppID", lg.getTerminal());
                            }
                            Map<String, String> raw = new HashMap<>();
                            if (lg.getLigneTransaction().getParametreService() != null) {
                                for (Parametre parametre : lg.getLigneTransaction().getParametreService()) {
                                    if (parametre.getCodeParametre() != null && parametre.getValeur() != null) {
                                        raw.put(parametre.getCodeParametre(), parametre.getValeur());
                                    }
                                }
                            }
                            String codeMethode = methode.getTypeMethode() == null ? methode.getNomMethode() : methode.getTypeMethode().toString();
                            switch (methode.getApi().getTypeAutorization()) {
                                case OAUTH2:
                                    AuthTokenInfo tokenInfo = tokenCacheService.tokenCache(methode.getApi());
                                    resultat = suivre(circuit, api, codeMethode, methode.getTypeAppel().toString(), raw, headers,
                                            () -> partnerHttp.call(url, tokenInfo, mapParametre, methode.getTypeAppel().toString(), "1", methode.getTypeReponse().toString(), headers));
                                    break;
                                case BASIC:
                                    resultat = PartnerHttpClient.result(ErreurConstants.FONCTION_NON_IMPLEMENTE, "Non implementé!!!", "1", null, methode.getTypeReponse().toString());
                                    break;
                                case OAUTH:
                                    resultat = PartnerHttpClient.result(ErreurConstants.FONCTION_NON_IMPLEMENTE, "Non implementé!!!", "1", null, methode.getTypeReponse().toString());
                                    break;
                                case NOAUTH:
                                    resultat = suivre(circuit, api, codeMethode, methode.getTypeAppel().toString(), raw, headers,
                                            () -> partnerHttp.call(url, null, mapParametre, methode.getTypeAppel().toString(), "1", methode.getTypeReponse().toString(), headers));
                                    break;
                                default:
                                    break;
                            }
                            stopWatch.stop();
                            circuit.record(resultat);
                        } else {
                            resultat = apiCircuit.unavailable(
                                    methode.getTypeReponse() != null ? methode.getTypeReponse().toString() : null,
                                    "Ressource indisponible");
                        }
                        break;
                    case WEBSERVICE:
                        resultat = PartnerHttpClient.result(ErreurConstants.FONCTION_NON_IMPLEMENTE, "Non implementé!!!", "1", null, methode.getTypeReponse().toString());
                        break;
                    default:
                        break;
                }
            } catch (Exception e) {
                log.error("Erreur middleware", e);
                circuit.record(e);
                resultat = PartnerHttpClient.result(ErreurConstants.UNKNOWN_ERROR, "Erreur inconnue!!!", "1", null, methode.getTypeReponse().toString());
            } finally {
                if (stopWatch.isRunning()) {
                    stopWatch.stop();
                }
                enregistrerHistorique(api, methode, resultat, tempsDebut);
                log.debug(stopWatch.prettyPrint());
            }
        } else {
            resultat = PartnerHttpClient.result(ErreurConstants.RESSOURCE_ACCESS_ERROR, "Ressource inactive", "1", null, null);
        }
        return resultat;
    }

    /**
     * Succès local, même condition que l'ancien POST /api/dummy :
     * ligne non immédiate, ou ligne immédiate sans paramètres à envoyer.
     */
    @Transactional(readOnly = true)
    public ResultMessage dummy(LigneTransactionDTO lg) {
        MethodeFonction methode = methodeRepository.findOne(lg.getLigneTransaction().getMethodeApi().getId());
        try {
            Message msg = new Message();
            msg.setCodeErr("0");
            msg.setMsg("Opération terminée avec Succès");
            String typeReponse = methode != null && methode.getTypeReponse() != null ? methode.getTypeReponse().toString() : "JSON";
            return PartnerHttpClient.result(ErreurConstants.SUCCESS, "Opération terminée avec Succès", "1", msg, typeReponse);
        } catch (Exception e) {
            log.error("Erreur dummy", e);
            String typeReponse = methode != null && methode.getTypeReponse() != null ? methode.getTypeReponse().toString() : "JSON";
            return PartnerHttpClient.result(ErreurConstants.UNKNOWN_ERROR, "Erreur inconnue!!!", "1", null, typeReponse);
        }
    }

    private void enregistrerHistorique(Api api, MethodeFonction methode, ResultMessage resultat, long tempsDebut) {
        if (resultat == null) {
            return;
        }
        try {
            long duree = System.currentTimeMillis() - tempsDebut;
            operationService.enregistreHistoriqueAppel(api, methode, String.valueOf(resultat.getErrCode()), resultat.getMsg(), duree);
        } catch (Exception e) {
            log.error("Historique d'appel non enregistré", e);
        }
    }

    private ConcurrentHashMap<String, Object> getParametres(Set<Parametre> listparam, List<ParametreMethod> listparamethode) {
        ConcurrentHashMap<String, Object> mapParametre = new ConcurrentHashMap<>();
        for (ParametreMethod elementParam : listparamethode) {
            String valeur = rechercheParamMethode(listparam, elementParam.getCodeParametre());
            if (valeur != null) {
                mapParametre.put(elementParam.getNomExterne(), valeur);
                log.info("Variable================= {}", elementParam.getNomExterne() + ":" + valeur);
            }
        }
        return mapParametre;
    }

    private String getUrlParametres(List<ParametreMethod> listparamethode) {
        int i = 1;
        UriComponentsBuilder builder = null;
        String url = "?";
        String separateur = "&";
        for (ParametreMethod elementParam : listparamethode) {
            if (elementParam.getNomExterne() != null) {
                builder = UriComponentsBuilder.fromUriString(url).queryParam(elementParam.getNomExterne(), elementParam.getValeur());
                String urlInterm;
                if (i < listparamethode.size()) {
                    urlInterm = elementParam.getNomExterne() + "=" + "{" + elementParam.getNomExterne() + "}" + separateur;
                } else {
                    urlInterm = elementParam.getNomExterne() + "=" + "{" + elementParam.getNomExterne() + "}";
                }
                url = url + urlInterm;
                i = i + 1;
            }
        }
        if (builder != null) {
            log.debug("URL EST {}", builder.build().toUriString());
        }
        return url;
    }

    private String rechercheParamMethode(Set<Parametre> listparam, String code) {
        if (listparam == null || code == null) {
            return null;
        }
        Iterator<Parametre> it = listparam.iterator();
        while (it.hasNext()) {
            Parametre param = it.next();
            if (code.equals(param.getCodeParametre())) {
                return param.getValeur();
            }
        }
        return null;
    }
}
