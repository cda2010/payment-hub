package com.m2i.pti.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.m2i.omnicanal.outils.restclient.AuthTokenInfo;
import com.m2i.omnicanal.outils.restclient.RestClient;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.m2i.omnicanal.outils.util.ResponseStatus;
import com.m2i.omnicanal.outils.util.StandardResponse;
import com.m2i.pti.domain.*;
import com.m2i.pti.cardless.service.TripleDES;
import com.m2i.pti.domain.enumeration.*;
import com.m2i.pti.domain.util.CodeErreur;
import com.m2i.pti.external.domain.GrData;
import com.m2i.pti.mobile.dto.MParametre;
import com.m2i.pti.mobile.dto.SettlementResponse;
import com.m2i.pti.repository.*;
import com.m2i.pti.security.SecurityUtils;
import com.m2i.pti.service.dto.*;
import com.m2i.pti.service.dto.Evenement;
import com.m2i.pti.service.kafka.MessageProducer;
import com.m2i.pti.service.util.RandomUtil;
import com.m2i.pti.service.util.Util;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.m2i.pti.mobile.dto.ListOperationCardlessDTO;
import com.m2i.pti.mobile.dto.MDataTransaction;
import com.m2i.pti.mobile.dto.MOperation;
import com.m2i.pti.mobile.dto.MResponse;
import com.m2i.pti.mobile.dto.MlistParametre;
import com.m2i.pti.repository.ParamCanalRepository;
import com.m2i.pti.repository.PersonnePhysiqueRepository;
import com.m2i.pti.service.util.FastXml;
import com.m2i.pti.web.rest.errors.CustomSettlementException;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StopWatch;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService {

    private static final String CODE_SERVICE_DESTRUCTION_MONNAIE = "012";
    private static final String CODE_SERVICE_CREATION_MONNAIE = "011";

    private static final String CODE_SERVICE_PAIEMENT_BIEN = "010";
    private static final String CODE_SERVICE_RETRAIT_COMPTE = "009";
    private static final String CODE_SERVICE_ACCOUNT_CASH = "004";
    private static final String CODE_SERVICE_APPROVISIONNEMENT = "006";
    public static final String CODE_SERVICE_ACCOUNT_ACCOUNT = "005";
    private static final String CODE_SERVICE_VIREMENT = "008";
    private static final String CODE_SERVICE_ENVOI_CASH = "001";
    private static final String CODE_SERVICE_RETRAIT = "002";
    private static final String CODE_SERVICE_CASH_ACCOUNT = "003";
    private static final String SERVICE_INFO_SOLDE = "/api/boundaryByParam";
    private static final String SERVICE_INFO_CLIENT = "/api/boundaryByTypeMethode?codeFSE={codeFSE}&codeMethode={codeMethode}&otrParametre={otrParametre}";
    private static final String SERVICE_LIST_EVENEMENT = "/api/boundaryByTypeMethode?codeFSE={codeFSE}&codeMethode={codeMethode}&otrParametre={otrParametre}";
    private static final String SERVICE_ENVOIE_SMS = "/api/boundary?codeFonction={codeFonction}&otrParametre={otrParametre}";
    private static final String SERVICE_FUNCTION = "/api/checkFonction?codeMethode={codeMethode}&codeFSE={codeFSE}";
    private static final String SERVICE_ACK = "/api/sendAck?codeOperation={codeOperation}";
    private static final String SERVICE_MSG_RETOUR = "/api/msgRetour?codeOperation={codeOperation}";

    private static final String SERVICE_LIST_FOURNISSEUR_SERVICE = "/api/fournisseur-services-ext";
    private static final String SERVIE_FSE_COMPENSE = "api/fournisseur-services/compense";
    private static final String SERVIE_FSE_COMPTE = "api/comptesfse/fournisseur?codefse={codefse}&etiquette={etiquette}";
    private static final String PARAM_MACHINE = "api/gen-cert/v1/gencert?adresseIp={adresseIp}&uuid={uuid}&libelleOrganisation={libelleOrganisation}&currentUser={currentUser}&caKey={caKey}&hostId={hostId}&p12Pass={p12Pass}";
    private static final String ADRESSE_AUTORISER = "api/gen-cert/v1/allowMachine?adresses={adresses}";
    private final Logger log = LoggerFactory.getLogger(ApiService.class);

    private final String clientId;
    private final String secretWord;
    private final String login;
    private final String password;
    private final String url;
    private final String mUrl;

    private final CompteService compteService;
    private final UniteOrganisationService uniteOrganisationService;
    private final String URL_PRESENTATION = "/api/omnicanalPresentation";
    private final PersonneService personneService;
    private final UniteOrganisationRepository uniteOrganisationRepository;
    private final RegleCommissionService regleCommissionService;
    private final InstructionComptableService instructionComptableService;
    private final CompteRepository compteRepository;
    private final String CAS = "CAS";
    private final String CAS_NORMAL = "NORMAL";
    private final String CAS_REVERSION = "REVERSION";
    private TaskExecutor taskExecutor;
    private NotificationRepository notificationRepository;
    private final MachineRepository machineRepository;
    private final MessageProducer messageProducer;
    private final String key;
    private final TypeUniteOrganisationRepository typeUniteOrganisationRepository;
    private final String salt;
    private final String passwordCardless;
    private final PersonnePhysiqueRepository personnePhysiqueRepository;
    @Value("${applicationID.id}")
    private String appId;
    Map<String, Object> paramMap = new HashMap<String, Object>();
    private final String keyCrypt;

    @Value("${gateway.url}")
    private String urlGatewayRessource;
    @Value("${gateway.code}")
    private String codeGateWay;
    //Paramétrages des codes fonctions en fonction du code FSE
    HashMap<String, String> sms = new HashMap<>();
    RestClient restClient;
    ParamCanalRepository paramCanalRepository;
    ParametreAppRepository parametreAppRepository;

    {
        sms.put("0004", "000214");//BF
        sms.put("0009", "12345");//CI
    }

    HashMap<String, String> evenements = new HashMap<>();

    {
        sms.put("0004", "000109");//BF
        sms.put("0009", "000109");//CI
    }

    HashMap<String, String> infosClient = new HashMap<>();

    {
        infosClient.put("0004", "000112");//BF
        infosClient.put("0009", "000112");//CI
    }

    HashMap<String, String> verifBordereau = new HashMap<>();

    {
        verifBordereau.put("0004", "000113");//BF
        verifBordereau.put("CI", "12345");//CI
    }

    public String getTenant() {
        return "300";
    }

    @Autowired
    private Environment env;

    public ApiService(ParametreAppRepository parametreAppRepository, ParamCanalRepository paramCanalRepository, @Value("${decryptParam.key}") String key, @Value("${decryptParam.salt}") String salt, @Value("${decryptParam.password}") String passwordCardless, @Value("${decryptParam.keyCrypt}") String keyCrypt, MessageProducer messageProducer, NotificationRepository notificationRepository, TaskExecutor taskExecutor, @Value("${connexionOmnicanal.clientId}") String clientId, @Value("${connexionOmnicanal.secretWord}") String secretWord, @Value("${connexionOmnicanal.login}") String login, @Value("${connexionOmnicanal.password}") String password, @Value("${connexionOmnicanal.url}") String url, @Value("${connexionAlloxMachine.url}") String mUrl, CompteService compteService, UniteOrganisationService uniteOrganisationService, PersonneService personneService, RepartitionOpService repartitionOpService, UniteOrganisationRepository uniteOrganisationRepository, RegleCommissionService regleCommissionService, InstructionComptableService instructionComptableService, CompteRepository compteRepository, MachineRepository machineRepository, TypeUniteOrganisationRepository typeUniteOrganisationRepository, PersonnePhysiqueRepository personnePhysiqueRepository) {
        this.typeUniteOrganisationRepository = typeUniteOrganisationRepository;
        this.key = key;
        this.salt = salt;
        this.passwordCardless = passwordCardless;
        this.keyCrypt = keyCrypt;
        this.compteService = compteService;
        this.uniteOrganisationService = uniteOrganisationService;
        this.personneService = personneService;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.regleCommissionService = regleCommissionService;
        this.instructionComptableService = instructionComptableService;
        this.compteRepository = compteRepository;
        this.machineRepository = machineRepository;
        this.personnePhysiqueRepository = personnePhysiqueRepository;
        this.restClient = new RestClient();
        this.clientId = clientId;
        this.secretWord = secretWord;
        this.login = login;
        this.password = password;
        this.url = url;
        this.mUrl = mUrl;
        restClient.setPassword(password);
        this.taskExecutor = taskExecutor;
        this.notificationRepository = notificationRepository;
        this.messageProducer = messageProducer;
        this.paramCanalRepository = paramCanalRepository;
        this.parametreAppRepository = parametreAppRepository;
        restClient.setLogin(login);
        restClient.setPlainClientCredentials(secretWord);
        restClient.setClientID(clientId);
        restClient.setGrantUri("?username=" + login + "&password=" + password + "&grant_type=password&scope=read write&client_secret=" + secretWord + "&client_id=" + clientId);
        restClient.setSecureServerUri(this.url + "oauth/token");

    }

    private RestClient setParam() {
        RestClient localRestClient = new RestClient();
        localRestClient.setPassword(password);
        localRestClient.setLogin(login);
        localRestClient.setPlainClientCredentials(secretWord);
        localRestClient.setClientID(clientId);
        localRestClient.setGrantUri("?username=" + login + "&password=" + password + "&grant_type=password&scope=read write&client_secret=" + secretWord + "&client_id=" + clientId);
        localRestClient.setSecureServerUri(this.url + "oauth/token");
        return localRestClient;
    }

    void repartirCommission(OperationCommissionDTO opCommission) {
        messageProducer.sendMessageCommission(opCommission);

    }
    void sendCashBack(String operationId) {
        messageProducer.sendCashBack(operationId);

    }


    public Integer verifIfWithdrawal(Operation operation, String entityCode) throws IOException {
        com.m2i.omnicanal.outils.restclient.model.Message msg = cardLessTransactionStatus(operation.getCodeOperation(), entityCode);
        if (msg != null && msg.getCodeErr() != null && msg.getCodeErr().equals("0")) {
            return 1;
        } else {
            return 0;
        }
    }

    public void sendMessageCardless(ListOperationCardlessDTO evtId) {
        messageProducer.sendMessageCardless(evtId);

    }

    /*une classe privée qui permet d'envoyer des message*/
    /**
     * Service permettant de recuperer un code retrait CARDLESS
     *
     * @param telephone
     * @param montant
     * @param reference
     * @return
     */
    public ResponseEntity getCodeRetraitGab(String telephone, String montant, String reference) {
        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();
        String entityCode = "RP";
        String otrParametre = "telephone=" + telephone + "|montant=" + montant + "|reference=" + reference + "|entityCode=" + entityCode;
        localRestClient.setRestServerUri(this.url + "/api/boundary?codeFonction={codeFonction}&otrParametre={otrParametre}");
        ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
        param.put("codeFonction", "000235");
        param.put("otrParametre", otrParametre);
        ResponseEntity result = localRestClient.getRessources(tokeinfo, param, "POST");
        return result;
    }

    /**
     * Recupère la liste des evenements obtenu suite à un versement effectue par
     * un client dans une agence Afin de verifier la validité du bordereau de
     * versement en agence
     *
     * @param username
     * @param numeroCompte
     * @param sens
     * @param montant
     * @param codeFse
     * @param agence
     * @return
     * @throws IOException
     */
    public List<Evenement> getListEvenement(String username, String numeroCompte, String sens,
            String montant, String codeFse, String agence) throws IOException {

        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + SERVICE_LIST_EVENEMENT);
        ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
        String otrParametre = "numerocompte=" + numeroCompte + "|sens=" + sens + "|username=" + username.toUpperCase() + "|montant=" + montant + "|agence=" + agence;
        param.put("codeMethode", "LISTEVENEMENT");
        param.put("codeFSE", codeFse);
        param.put("otrParametre", otrParametre);
        ResponseEntity result = localRestClient.getRessources(tokeinfo, param, "POST");
        ResultMessage resMessageTran = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class);
        if (resMessageTran != null) {
            if (resMessageTran.getErrCode() == 0) {
                MessageEve listTrans = localRestClient.convert(resMessageTran.getResponseEntity(), MessageEve.class);
                return listTrans.getListeEvenement();
            }
        }
        return null;
    }

    public Message checkPing(String codeFse) throws IOException {
        log.info("CHECK PING : ");
        this.paramMap = new HashMap<String, Object>();
        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = null;//restClient.sendTokenRequest();
        localRestClient.setRestServerUri(this.url + SERVICE_INFO_CLIENT);
        ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
        param.put("codeMethode", "PING");
        param.put("codeFSE", codeFse);
        param.put("otrParametre", "test=test");
        paramMap.put("X-AppID", this.appId);
        ResponseEntity result = localRestClient.getRessources(tokeinfo, param, "POST", paramMap);
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class);
        log.info("============" + result.getBody());
        if (resultMessage != null) {
            log.info("===RESULTMESSAGE=========" + resultMessage.getResponseEntity());
            Message resultM = (Message) localRestClient.convert(resultMessage.getResponseEntity(), Message.class);
            log.info("===RESULTD=========" + resultM);
            return resultM;
        } else {
            throw new CustomParameterizedException("Le compte est introuvable!");
        }
    }

    public Message checkPing(String codeFse, List<MParametre> listparam) throws IOException, JAXBException {
        log.info("CHECK PING : ");
        RestClient localRestClient = setParam();
        Message resultM = new Message();
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        //  codeFse = "0013";
        parametres = getParametres(listparam);
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("PING");
        parametreAppelDTO.setParam(parametres);

        // ConcurrentHashMap<String, Object> param = setParametre(listparam);
        AuthTokenInfo tokeinfo = null;//restClient.sendTokenRequest();
        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);

        HashMap<String, String> pMap = new HashMap<String, String>();
        pMap.put("X-AppID", getApplicationId());
        ResponseEntity result = localRestClient.getRessourcesObjectAndHeader(tokeinfo, parametreAppelDTO, "POST", pMap);
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class);
        log.info("============" + result.getBody());
        if (resultMessage != null && resultMessage.getResponseEntity() != null) {
            log.info("===RESULTMESSAGE=========" + resultMessage.getResponseEntity());
            resultM = (Message) localRestClient.convert(resultMessage.getResponseEntity(), Message.class);
            log.info("===RESULTD=========" + resultM);

        } else {
            resultM.setCodeErr("-1");
            resultM.setErrCode("-1");
            resultM.setMsg("Erreur");
        }
        return resultM;
    }

    private ConcurrentHashMap<String, Object> setParametre(List<MParametre> listparam) throws JAXBException {
        ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
        if (listparam != null) {
            MlistParametre mlistParametre = new MlistParametre();
            mlistParametre.setParametre(listparam);
            String str = (String) Util.marshall(mlistParametre, String.class);
            param.put("SPARAM", str);
            Iterator itParam = listparam.iterator();
            while (itParam.hasNext()) {
                MParametre elementParam = (MParametre) itParam.next();
                param.put(elementParam.getCodeParametre(), elementParam.getValeur());

            }

        }

        return param;
    }

    public Message checkStatus(String codeFse, List<MParametre> listparam) throws IOException, JAXBException {
        log.info("CHECK STATUS : ");

        RestClient localRestClient = setParam();
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        //  codeFse = "0013";
        parametres = getParametres(listparam);
        log.debug("parametres:" + parametres);
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("TRANSACTIONSTATUS");
        parametreAppelDTO.setParam(parametres);

        HashMap<String, String> param = new HashMap<String, String>();
        AuthTokenInfo tokeinfo = null;//restClient.sendTokenRequest();
        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        param.put("X-AppID", this.appId);

        Message resultM = new Message();
        ResponseEntity result = localRestClient.getRessourcesObjectAndHeader(tokeinfo, parametreAppelDTO, "POST", param);
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class);
        log.info("============" + result.getBody());
        if (resultMessage != null && resultMessage.getResponseEntity() != null) {
            log.info("===RESULTMESSAGE=========" + resultMessage.getResponseEntity());
            resultM = (Message) localRestClient.convert(resultMessage.getResponseEntity(), Message.class);
            log.info("===RESULTD=========" + resultM);

        } else {
            resultM.setCodeErr("-1");
            resultM.setErrCode("-1");
            resultM.setMsg("Erreur");
        }
        return resultM;
    }

    /**
     * Recupere les informations d'un client sur la base des informations de son
     * compte Utilisé pour les comptes CBI
     *
     * @param numeroCompte
     * @param agence
     * @param cle
     * @param idFse
     * @return
     * @throws IOException
     */
    public PersonneAllDTO getClientInfo(String numeroCompte, String agence, String cle, Long idFse) throws IOException, JAXBException {
        log.info("service de récupération des information du compte : " + numeroCompte);
        String codeFse;
        this.paramMap = new HashMap<String, Object>();
        if (idFse != null) {//Verification de la validité du fournisseur de service
            UniteOrganisation fse = uniteOrganisationRepository.findByUoId(idFse);
            if (fse != null) {
                codeFse = fse.getCodeFse();
            } else {
                throw new CustomParameterizedException("Fournisseur de service inconnu.");
            }
        } else {
            throw new CustomParameterizedException("Fournisseur de service inconnu.");
        }

        /* AuthTokenInfo tokeinfo = null;//restClient.sendTokenRequest();
        String otrParametre = "numeroCompte=" + numeroCompte + "|agence=" + agence + "|cle=" + cle;
        restClient.setRestServerUri(this.url + SERVICE_INFO_CLIENT);
        ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
        param.put("otrParametre", otrParametre);
        param.put("codeMethode", "INFOCLIENT");
        param.put("codeFSE", codeFse);
        paramMap.put("X-AppID", this.appId);
        ResponseEntity result = restClient.getRessources(tokeinfo, param, "POST", paramMap);
        ResultMessage resultMessage = (ResultMessage) restClient.convert(result.getBody(), ResultMessage.class);
        log.info("============" + result.getBody());*/
        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();
        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres.add(addParametre(1L, "AGENCE", agence));
        parametres.add(addParametre(2L, "NUMEROCOMPTE", numeroCompte));
        parametres.add(addParametre(3L, "CLE", cle));
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("INFOCLIENT");
        parametreAppelDTO.setParam(parametres);
        paramMap.put("X-AppID", this.appId);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST", this.paramMap);
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class);
        log.info("resultMessage" + resultMessage);

        if (resultMessage != null) {
            log.info("===RESULTMESSAGE=========" + resultMessage.getResponseEntity());
            Result resultD = (Result) localRestClient.convert(resultMessage.getResponseEntity(), Result.class);

            log.info("===RESULTD=========" + resultD);
            if (resultD != null && resultD.getInfoClient() != null) {
                CustomerInformation info = Util.unmarshall(resultD.getText(), CustomerInformation.class);
                return personneService.getPersonneAllWithInfoClientCBI(resultD.getInfoClient(), codeFse, info);
            } else {
                throw new CustomParameterizedException("Le compte est introuvable!");
            }
        } else {
            throw new CustomParameterizedException("Le compte est introuvable!");
        }
    }

    /**
     * Recupère la liste des fournisseurs de service disponible sur l'omnicanal
     *
     * @return
     * @throws IOException
     */
    public List<FseDTO> getListeFournisseur() throws IOException {
        log.info("Récupération de la liste des fournisseurs de service disponible sur OMNICANAL");

        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();

        String destUrl = this.url + SERVICE_LIST_FOURNISSEUR_SERVICE;
        localRestClient.setRestServerUri(destUrl);
        ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
        ResponseEntity fse = localRestClient.getRessources(tokeinfo, param, "GET");
        List<FseDTO> fo = new ArrayList();
        List<FseDTO> list = (List<FseDTO>) localRestClient.convert(fse.getBody(), fo.getClass());
        return list;
    }

    /**
     * Recupère la liste des fournisseurs de compensation d'un FSE donné
     *
     * @param codeFse
     * @return
     * @throws IOException
     */
    public FseDTO getFseCompense(String codeFse) throws IOException {
        log.info("service de récupération du FSE de compensation");

        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + SERVIE_FSE_COMPENSE + codeFse);
        ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
        ResponseEntity result = localRestClient.getRessources(tokeinfo, param, "GET");
        FseDTO fo = new FseDTO();
        FseDTO fseDTO = (FseDTO) localRestClient.convert(result.getBody(), fo.getClass());
        return fseDTO;
    }

    /**
     * Liste des relations entre FSE existant sur l'omnicanal
     *
     * @return
     * @throws IOException
     */
    public List<RelationDto> getListeRelationFournisseur() throws IOException {
        log.info("service de récupération de la liste des relations entre fournisseur de service");

        RestClient localRestClient = setParam();
           AuthTokenInfo tokeinfo = null;// localRestClient.sendTokenRequest();

        String destUrl = this.url + "/api/listrelations";
        localRestClient.setRestServerUri(destUrl);
        ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
        ResponseEntity fse = localRestClient.getRessources(tokeinfo, param, "GET");
        ObjectMapper mapper = new ObjectMapper();
        List<RelationDto> newsList = new ArrayList<>();
        List<RelationDto> list = mapper.convertValue(
                fse.getBody(),
                new TypeReference<List<RelationDto>>() {
        });

        if (list != null) {
            long i = 0;
            for (int j = 0; j < list.size(); j++) {
                RelationDto relationDto = list.get(j);
                relationDto.setId(i++);
                UniteOrganisation fseSource = uniteOrganisationService.findFseByCode(relationDto.getFseSource().getCodeFse());
                UniteOrganisation fseDest = uniteOrganisationService.findFseByCode(relationDto.getFseDestination().getCodeFse());
                if (fseSource != null && fseDest != null) {
                    relationDto.setId(i++);
                    relationDto.getFseSource().setId(fseSource.getId());
                    relationDto.getFseDestination().setId(fseDest.getId());
                    relationDto.setLibelle(relationDto.getFseSource().getLibelleFse() + "--->" + relationDto.getFseDestination().getLibelleFse());
                    newsList.add(relationDto);
                }
            }
        }
        return newsList;
    }

    /**
     *
     * @param body
     * @return
     * @throws IOException
     */
    public List<StatsMontantOp> convert(Object body) throws IOException {
        List<StatsMontantOp> fo = new ArrayList();
        RestClient localRestClient = setParam();
        List<StatsMontantOp> list = (List<StatsMontantOp>) localRestClient.convert(body, fo.getClass());
        return list;
    }

    public AgentInformation getInfoAgentCompte(String agence, String codeFse) throws IOException, JAXBException {

        RestClient localRestClient = setParam();
           AuthTokenInfo tokeinfo = null;//ocalRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres.add(addParametre(1L, "AGENCE", agence));
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("INFOSAGENT");
        parametreAppelDTO.setParam(parametres);
        paramMap.put("X-AppID", this.appId);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST", this.paramMap);
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class);
        log.info("resultMessage" + resultMessage);
        AgentInformation agt = new AgentInformation();
        if (resultMessage != null) {
            Result resultD = (Result) localRestClient.convert(resultMessage.getResponseEntity(), Result.class);
            log.info("resultD======" + resultMessage.getResponseEntity());
            if (resultD != null && resultD.getCodeErr() != null && resultD.getCodeErr().equals("0")) {
                log.info("GET TEST======" + resultD.getText());
                agt = Util.unmarshall(resultD.getText(), AgentInformation.class);
            }

        }
        return agt;
    }

    /**
     * Vérifier un versement à travers le code Service utilisé lors de la
     * realisation des opérations au niveau des agences
     *
     * @param codeOp
     * @param sens
     * @param rrn
     * @param etiquette
     * @return
     * @throws IOException
     * @old
     */
    public Evenement getEve(String codeOp, String sens, String rrn, EtiquetteCompteCode etiquette) throws IOException {
        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();
        Compte compte = compteService.getCompteOfOrganisationConnecte(etiquette);
        if (compte != null) {
            String numeroCompte = "";
            numeroCompte = compte.getValeurCompte();
            String otrParametre = "numeroCompte=" + numeroCompte + "|sens=" + sens + "|codeOperation=" + codeOp + "|rrn=" + rrn;
            localRestClient.setRestServerUri(this.url + "/api/boundary?codeFonction={codeFonction}&otrParametre={otrParametre}");
            ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
            UniteOrganisation uo = uniteOrganisationService.getFilialeOfUniteOrganisationConnecte();
            param.put("codeFonction", verifBordereau.get(uo.getCodeFse()));
            param.put("otrParametre", otrParametre);
            ResponseEntity result = localRestClient.getRessources(tokeinfo, param, "POST");
            ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class);
            Evenement evenement = (Evenement) localRestClient.convert(resultMessage.getResponseEntity(), Evenement.class);
            return evenement;
        } else {
            throw new CustomParameterizedException("Compte de l'agence introuvable !");
        }
    }

    /**
     * @param id
     * @param codeParam
     * @param valeur
     * @return
     */
    public static ParametreUserDTO addParametre(Long id, String codeParam, String valeur) {
        InterfaceUtilisateur interfaceUtilisateur = new InterfaceUtilisateur();
        ParametreUserDTO p = new ParametreUserDTO();
        p.setId(id);
        p.setCodeParametre(codeParam);
        p.setInterfaceUtilisateur(interfaceUtilisateur);
        p.setParametreApi(ProprieteApi.FSESOURCE);
        p.setNomParametre("");
        p.setTypeParametre(ParamatreType.STRING);
        p.setValeur(valeur);
        return p;
    }

    public synchronized String getApplicationId() {
        return env.getProperty("applicationID.id");
    }

    /**
     * Service de comptabilisation d'une operation OM_APPROVISSIONNEMENT
     *
     * @param operation
     * @param organisationSource
     * @param organisationDest
     * @return
     * @throws IOException
     * @label CPTERESEAUMASTER
     * @label CPTEOPERATIONACTEUR
     */
    public Result sendRequestComptabilisationOfApprovisionnementToOmnicanal(Operation operation, UniteOrganisation organisationSource, UniteOrganisation organisationDest) throws IOException {
        log.info("------Service de comptabilisation d'une opération d'approvissionnement " + this.url);

        RestClient localRestClient = setParam();
           AuthTokenInfo tokeinfo = null;// localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + URL_PRESENTATION);
        Compte compteOperationActeur = compteService.getCompteOperationActeur(organisationSource);
        Compte compteReseauMaster = compteService.getCompteOperationActeur(organisationDest);
        if (compteOperationActeur != null && compteReseauMaster != null) {
            String codeFseDest = uniteOrganisationService.getCodeUoFse(organisationDest);
            String codeFseSource = uniteOrganisationService.getCodeUoFse(organisationSource);
            String codeOfCpteOperation;
            String codeAgenceSrc;
            if (compteOperationActeur.getCompteBanque() != null && compteOperationActeur.getCategorieCompte().equals(CategorieCompte.BANQUE)) {
                codeOfCpteOperation = compteOperationActeur.getValeurCompte() + "#" + compteOperationActeur.getCompteBanque().getCodeAgence() + "#" + compteOperationActeur.getCompteBanque().getCodeBanque();
                codeAgenceSrc = compteOperationActeur.getCompteBanque().getCodeAgence();
            } else {
                codeAgenceSrc = "0";
                codeOfCpteOperation = compteOperationActeur.getValeurCompte();
            }
            String codeOfCompteReseauMaster;
            String codeAgenceDest;
            if (compteReseauMaster.getCompteBanque() != null && compteReseauMaster.getCategorieCompte().equals(CategorieCompte.BANQUE)) {
                codeOfCompteReseauMaster = compteReseauMaster.getValeurCompte() + "#" + compteReseauMaster.getCompteBanque().getCodeAgence() + "#" + compteReseauMaster.getCompteBanque().getCodeBanque();
                codeAgenceDest = compteReseauMaster.getCompteBanque().getCodeAgence();
            } else {
                codeAgenceDest = "0";
                codeOfCompteReseauMaster = compteReseauMaster.getValeurCompte();
            }

            // String codeAgenceSrc = compteOperationActeur.getCompteBanque().getCodeAgence();
            List<ParametreUserDTO> parametres = new ArrayList<>();
            parametres.add(addParametre(1L, MontantsAComptabiliser.MONTANTHT.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTHT, operation).intValue())));
            parametres.add(addParametre(2L, MontantsAComptabiliser.MONTANTTAXE.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTTAXE, operation).intValue())));
            parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMM.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMM, operation).intValue())));
            parametres.add(addParametre(4L, MontantsAComptabiliser.MONTANTHTCOMACQ.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTHTCOMACQ, operation).intValue())));
            parametres.add(addParametre(5L, MontantsAComptabiliser.MONTANTCOMACQ.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMACQ, operation).intValue())));

            parametres.add(addParametre(6L, "CODEFSEDEST", codeFseDest));
            parametres.add(addParametre(7L, "CODEFSESOURCE", codeFseSource));
            parametres.add(addParametre(8L, "CODEAGENCEDEST", codeAgenceDest));
            parametres.add(addParametre(9L, "CODEAGENCESOURCE", codeAgenceSrc));
            parametres.add(addParametre(10L, compteOperationActeur.getEtiquette(), codeOfCpteOperation));
            parametres.add(addParametre(12L, EtiquetteCompteCode.CPTERESEAUMASTER.toString(), codeOfCompteReseauMaster));
            parametres.add(addParametre(13L, CAS, CAS_NORMAL));
            parametres.add(addParametre(14L, "REFERENCE", operation.getCodeOperation()));
            parametres.add(addParametre(15L, "DONNEURORDRE", "-"));
            parametres.add(addParametre(16L, "BENEFICIAIRE", "-"));
            parametres.add(addParametre(16L, "DESCRIPTION", getDescription(operation)));
            ParametreDTO param = setParams(parametres, CODE_SERVICE_APPROVISIONNEMENT, operation.getCodeOperation());
            String message = showLogs(parametres);
            parametres.add(addParametre(17L, "CANAL", getCanal(operation, this.appId)));
            //   AuthTokenInfo tokeinfo = null;//restClient.sendTokenRequest();
            param.setCanal(this.appId);
            ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, param, "POST");
            if (result != null) {
                Result resultOp = localRestClient.convert(result.getBody(), Result.class);
                if (resultOp != null) {
                    resultOp.setDesc(message);
                }

                return resultOp;
            } else {
                return null;
            }
        } else {
            throw new CustomParameterizedException("Les comptes de comptabilisation sont introuvables.");
        }
    }

    /**
     * Service de comptabilisation d'une operation de type OM_ENVOI_CASH &
     * OM_REDRESSEMENT
     *
     * @param uoSrc
     * @param uoDest
     * @param operation
     * @param fseSrc
     * @param fseDest
     * @param fseDestAgenceSiege
     * @return
     * @throws IOException
     */
    public Result sendRequestForComptabilisationEnvoiCash(UniteOrganisation uoSrc, UniteOrganisation uoDest, Operation operation,
            UniteOrganisation fseSrc, UniteOrganisation fseDest,
            UniteOrganisation fseDestAgenceSiege, Boolean normal) throws IOException {
        log.info(" Service de comptabilisation d'une opération d'envoi de cash ou de redressement en cours");
        UniteOrganisation uoPv = operation.getUoEmetteur();
        Compte compteOperationActeur = compteService.getCompteOperationActeur(uoPv);
        Compte compteOperationActeurEnvoi = null;
        if (compteService.useTwoOperationAccount()) {
            compteOperationActeurEnvoi = compteService.getCompteOfOrganisation(uoPv, EtiquetteCompteCode.CPTEOPERATIONENVOI);
            if (compteOperationActeurEnvoi != null) {
                compteOperationActeur = compteOperationActeurEnvoi;
            }
        }
        Compte compteAttenteRetrait = compteService.getCompteOfOrganisation(fseDestAgenceSiege, EtiquetteCompteCode.CPTEATTENTERETRAIT);
        if (compteOperationActeur != null) {
            RestClient localRestClient = setParam();
            AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();
            localRestClient.setRestServerUri(this.url + URL_PRESENTATION);
            String codeFseDest = fseDest.getCodeFse();
            String codeFseSource = fseSrc.getCodeFse();
            String codeAgenceSrc = "0";
            String codeAgenceDest = "0";
            if (compteOperationActeur.getCompteBanque() != null) {
                codeAgenceSrc = compteOperationActeur.getCompteBanque().getCodeAgence();
                codeAgenceDest = compteAttenteRetrait.getCompteBanque().getCodeAgence();
            }
            String code;
            if (compteOperationActeur.getCompteBanque() != null && compteOperationActeur.getCategorieCompte().equals(CategorieCompte.BANQUE)) {

                code = compteOperationActeur.getValeurCompte() + "#" + compteOperationActeur.getCompteBanque().getCodeAgence() + "#" + compteOperationActeur.getCompteBanque().getCodeBanque();
                codeAgenceSrc = compteAttenteRetrait.getCompteBanque().getCodeAgence();
            } else {
                codeAgenceSrc = "0";
                code = compteOperationActeur.getValeurCompte();
            }
            UniteOrganisation uo = uniteOrganisationService.findOneById(operation.getPointVente().getUniteOrganisation().getId());
            TypeUniteOrganisation typeUniteOrganisa = typeUniteOrganisationRepository.findById(uo.getTypeUO().getId()).orElse(null);
            RegleCommission regleCommission = null;
            if (!Util.isBiggerThan(operation.getMontantFraisClient(), BigDecimal.ZERO)) {/// si le client n'est pas responsable de frais alors
                regleCommission = regleCommissionService.verifRegleCommission(TypeOperateur.valueOf(typeUniteOrganisa.getCodeTypeUO()), operation.getTypeService().getId(), compteOperationActeur, uoSrc.getId(), uoDest.getId());
            }
            String codeOfComptSrcFrais = getCompteSrcFrais(compteOperationActeur, regleCommission);
            List<ParametreUserDTO> parametres = new ArrayList<>();
            parametres.add(addParametre(1L, MontantsAComptabiliser.MONTANTHT.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTHT, operation).intValue())));
            parametres.add(addParametre(2L, MontantsAComptabiliser.MONTANTTAXE.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTTAXE, operation).intValue())));
            parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMM.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMM, operation).intValue())));
            parametres.add(addParametre(4L, MontantsAComptabiliser.MONTANTHTCOMACQ.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTHTCOMACQ, operation).intValue())));
            parametres.add(addParametre(5L, MontantsAComptabiliser.MONTANTCOMACQ.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMACQ, operation).intValue())));
            parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMEMT.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMEMT, operation).intValue())));

            parametres.add(addParametre(6L, "CODEFSEDEST", codeFseDest));
            parametres.add(addParametre(7L, "CODEFSESOURCE", codeFseSource));

            parametres.add(addParametre(8L, "CODEAGENCEDEST", codeAgenceDest));
            parametres.add(addParametre(9L, "CODEAGENCESOURCE", codeAgenceSrc));
            parametres.add(addParametre(16L, "DESCRIPTION", getDescription(operation)));
            parametres.add(addParametre(10L, "CPTEOPERATIONACTEUR", code));//CPTEOPERATIONACTEUR
            parametres.add(addParametre(11L, EtiquetteCompteCode.CPTESRCFRAIS.toString(), codeOfComptSrcFrais));//CPTESRCFRAIS

            parametres.add(addParametre(12L, "REFERENCE", operation.getCodeOperation()));
            parametres.add(addParametre(13L, "DONNEURORDRE", "-"));
            parametres.add(addParametre(14L, "BENEFICIAIRE", "-"));
            parametres.add(addParametre(17L, "CANAL", getCanal(operation, this.appId)));
            if (normal) {
                parametres.add(addParametre(15L, CAS, CAS_NORMAL));
            } else {
                parametres.add(addParametre(16L, CAS, CAS_REVERSION));
            }

            ParametreDTO param = setParams(parametres, CODE_SERVICE_ENVOI_CASH, operation.getCodeOperation());
            String message = showLogs(parametres);
            param.setCanal(this.appId);
            ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, param, "POST");
            if (result != null) {
                Result resultOp = localRestClient.convert(result.getBody(), Result.class);
                if (resultOp != null) {
                    resultOp.setDesc(message);
                }
                return resultOp;
            } else {
                return null;
            }
        } else {
            throw new CustomParameterizedException("Les comptes de comptabilisation sont introuvables.");
        }
    }

    /**
     * Service de comptabilisation d'une operation de type OM_RETRAIT
     *
     * @param operation
     * @param organisationSource
     * @return
     * @throws IOException
     */
    public Result sendRequestForComptabilisationRetrait(Operation operation, UniteOrganisation organisationSource) throws IOException {
        log.info(ZonedDateTime.now() + " Service de comptabilisation d'une opération de retrait ");
        organisationSource = uniteOrganisationService.findOneById(organisationSource.getId());
        Compte compteOperationActeurRetrait = null;
        Compte compteOperationActeur;

        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();

        compteOperationActeur = compteService.getCompteOfOrganisation(organisationSource, EtiquetteCompteCode.CPTEOPERATIONACTEUR);
        if (compteService.useTwoOperationAccount()) {
            compteOperationActeurRetrait = compteService.getCompteOfOrganisation(organisationSource, EtiquetteCompteCode.CPTEOPERATIONRETRAIT);
            if (compteOperationActeurRetrait != null) {
                compteOperationActeur = compteOperationActeurRetrait;
            }
        }
        UniteOrganisation uoDestAgenceSiege = uniteOrganisationService.getFseAgenceSiege(organisationSource);
        Compte compteAttenteRetrait = compteService.getCompteOfOrganisation(uoDestAgenceSiege, EtiquetteCompteCode.CPTEATTENTERETRAIT);
        String codeOfCpteOperation;
        String codeAgenceSrc;
        String codeAgenceDest;
        if (compteOperationActeur != null && compteAttenteRetrait != null) {
            localRestClient.setRestServerUri(this.url + URL_PRESENTATION);

            organisationSource = uniteOrganisationService.findOneById(organisationSource.getId());
            String codeFseSource = uniteOrganisationService.getCodeUoFse(organisationSource);
            String codeFseDest = codeFseSource;

            if (compteOperationActeur.getCompteBanque() != null && compteOperationActeur.getCategorieCompte().equals(CategorieCompte.BANQUE)) {

                codeOfCpteOperation = compteOperationActeur.getValeurCompte() + "#" + compteOperationActeur.getCompteBanque().getCodeAgence() + "#" + compteOperationActeur.getCompteBanque().getCodeBanque();
                codeAgenceDest = compteOperationActeur.getCompteBanque().getCodeAgence();
            } else {
                codeAgenceDest = "0";
                codeOfCpteOperation = compteOperationActeur.getValeurCompte();
            }

            if (compteAttenteRetrait.getCompteBanque() != null && compteAttenteRetrait.getCategorieCompte().equals(CategorieCompte.BANQUE)) {
                codeAgenceSrc = compteOperationActeur.getCompteBanque().getCodeAgence();
            } else {
                codeAgenceSrc = "0";
            }
            //  String codeOfCpteOperation = compteOperationActeur.getValeurCompte() + "#" + compteOperationActeur.getCompteBanque().getCodeAgence() + "#" + compteOperationActeur.getCompteBanque().getCodeBanque();

            List<ParametreUserDTO> parametres = new ArrayList<>();

            parametres.add(addParametre(1L, MontantsAComptabiliser.MONTANTHT.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTHT, operation))));
            parametres.add(addParametre(2L, "CODEFSEDEST", codeFseDest));//A recuperer à partir du compte reseau master
            parametres.add(addParametre(3L, "CODEFSESOURCE", codeFseSource));
            parametres.add(addParametre(4L, "CODEAGENCEDEST", codeAgenceDest));
            parametres.add(addParametre(5L, "CODEAGENCESOURCE", codeAgenceSrc));
            parametres.add(addParametre(6L, "CPTEOPERATIONACTEUR", codeOfCpteOperation));//CPTEOPERATIONACTEUR
            parametres.add(addParametre(7L, CAS, CAS_NORMAL));
            parametres.add(addParametre(8L, "REFERENCE", operation.getCodeOperation()));
            parametres.add(addParametre(9L, "DONNEURORDRE", "-"));
            parametres.add(addParametre(10L, "BENEFICIAIRE", "-"));
            parametres.add(addParametre(11L, "DESCRIPTION", getDescription(operation)));
            parametres.add(addParametre(17L, "CANAL", getCanal(operation, this.appId)));
            String message = showLogs(parametres);
            ParametreDTO param = setParams(parametres, CODE_SERVICE_RETRAIT, operation.getCodeOperation());
            param.setCanal(this.appId);
            ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, param, "POST");
            if (result != null) {
                Result resultOp = localRestClient.convert(result.getBody(), Result.class);
                if (resultOp != null) {
                    resultOp.setDesc(message);
                }
                return resultOp;
            } else {
                return null;
            }
        } else {
            throw new CustomParameterizedException("Les comptes de comptabilisation sont introuvables.");
        }
    }

    private String getEmetteur(Operation operation) {
        String em = "-";
        if (operation.getEmetteur() != null && operation.getEmetteur().getPersonne().getPersonnePhysique() != null) {
            em = operation.getEmetteur().getPersonne().getPersonnePhysique().getNomClient() + " " + operation.getEmetteur().getPersonne().getPersonnePhysique().getPrenomClient();
        } else if (operation.getEmetteur() != null && operation.getEmetteur().getPersonne() != null && operation.getEmetteur().getPersonne().getPersonneMorale() != null) {
            em = operation.getEmetteur().getPersonne().getPersonneMorale().getRaisonSociale() != null ? operation.getEmetteur().getPersonne().getPersonneMorale().getRaisonSociale() : "-";
        }
        return em;
    }

    private String getBeneficiaire(Operation operation) {
        String ben = "-";
        if (operation.getBeneficaire() != null && operation.getBeneficaire().getPersonne() != null) {
            if (operation.getBeneficaire().getPersonne().getPersonnePhysique() != null) {
                ben = operation.getBeneficaire().getPersonne().getPersonnePhysique().getNomClient() + " " + operation.getBeneficaire().getPersonne().getPersonnePhysique().getPrenomClient();
            } else if (operation.getBeneficaire().getPersonne().getPersonneMorale() != null) {
                ben = operation.getBeneficaire().getPersonne().getPersonneMorale().getRaisonSociale() != null ? operation.getBeneficaire().getPersonne().getPersonneMorale().getRaisonSociale() : "-";
            }
        }
        return ben;
    }

    private static String removeDiacritics(String input) {
        String nrml = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD);
        StringBuilder stripped = new StringBuilder();
        for (int i = 0; i < nrml.length(); ++i) {
            if (Character.getType(nrml.charAt(i)) != Character.NON_SPACING_MARK) {
                stripped.append(nrml.charAt(i));
            }
        }
        return stripped.toString();
    }

    public String removeSpecialCaractere(String name) {
        if (name != null) {
            name = name.replaceAll("[^\\p{L}\\p{N}]", " ");
        }
        return name;
    }

    private String getDescription(Operation operation) {
        String desc = "";
        String telephone = "";
        String nomOrganisation = "";
        if (operation.getPointVente().getUniteOrganisation().getLibelleUniteOrganisation() != null) {
            nomOrganisation = operation.getPointVente().getUniteOrganisation().getLibelleUniteOrganisation();
        }

        String typeOperation = operation.getTypeService().getDescription();
        String reference = operation.getCodeOperation();
        desc = typeOperation + " " + nomOrganisation;
        if (reference != null) {
            if (reference.length() > 0) {
                desc = desc + " " + reference;
            }
        }
        if (operation.getEmetteur() != null) {
            if (operation.getEmetteur().getPersonne() != null) {
                if (operation.getEmetteur().getPersonne().getTelephone() != null) {
                    telephone = operation.getEmetteur().getPersonne().getTelephone();
                    if (operation.getEmetteur().getPersonne().getPersonnePhysique() != null) {
                        PersonnePhysiqueDTO personnePhysiqueDTO = personnePhysiqueRepository.findByIdReport(operation.getEmetteur().getPersonne().getPersonnePhysique().getId());
                        String clientEmet = personnePhysiqueDTO.getNomClient();
                        clientEmet = removeSpecialCaractere(clientEmet);
                        desc = desc + " de " + clientEmet + " " + telephone;
                    } else {
                        desc = desc + " de " + telephone;
                    }

                }

            }
        }
        if (operation.getTypeService().getCode().equals(TypeServiceLibelle.PAIEMENT_FACTURE.name())) {
            if (operation.getMotif() != null) {
                desc = desc + ", " + operation.getMotif();
            }

        }
        if (operation.getBeneficaire() != null) {
            if (operation.getBeneficaire().getPersonne() != null) {
                if (operation.getBeneficaire().getPersonne().getTelephone() != null) {
                    telephone = operation.getBeneficaire().getPersonne().getTelephone();
                    if (operation.getBeneficaire().getPersonne().getPersonnePhysique() != null) {
                        if (operation.getEmetteur() != null && operation.getBeneficaire().getPersonne().equals(operation.getEmetteur().getPersonne())) {
                            desc = desc + " vers lui même ";
                        } else {
                            PersonnePhysiqueDTO personnePhysiqueDTO = personnePhysiqueRepository.findByIdReport(operation.getBeneficaire().getPersonne().getPersonnePhysique().getId());
                            String clientBenef = personnePhysiqueDTO.getNomClient();
                            clientBenef = removeSpecialCaractere(clientBenef);
                            desc = desc + " vers " + clientBenef + " " + telephone;
                        }

                    } else {
                        desc = desc + " vers " + telephone;
                    }

                }

            }
        }
        desc = removeDiacritics(desc);
        return desc;
    }

    /**
     * *
     *
     * @param operation
     * @param fseSrc
     * @param fseDest
     * @param compteClient
     * @param normal
     * @param avecFrais
     * @param service
     * @return
     * @throws IOException
     */
    public Result sendRequestComptabilisationDestruction(Operation operation, UniteOrganisation fseSrc, UniteOrganisation fseDest) throws IOException {
        log.info("Service de comptabilisation d'une opération de Account Cash");
        RestClient localRestClient = setParam();
            AuthTokenInfo tokeinfo = null;// = localRestClient.sendTokenRequest();

        localRestClient.sendTokenRequest();//token
        localRestClient.setRestServerUri(this.url + URL_PRESENTATION);
        String codeOfCpteOperation = "";
        //  UniteOrganisation fseDestAgenceSiege = uniteOrganisationService.getUoAgenceSiege(fseDest);//L'agence siège de l'organisation initiateur du MAD
        String codeFseDest = fseDest.getCodeFse();
        String codeFseSource = fseSrc.getCodeFse();
        RegleCommission regleCommission = null;
        UniteOrganisation uo = uniteOrganisationService.findOneById(operation.getPointVente().getUniteOrganisation().getId());
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres.add(addParametre(1L, MontantsAComptabiliser.MONTANTHT.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTHT, operation).toBigInteger())));
        parametres.add(addParametre(5L, "CODEFSEDEST", codeFseDest));
        parametres.add(addParametre(6L, "CODEFSESOURCE", codeFseSource));
        parametres.add(addParametre(16L, "DESCRIPTION", getDescription(operation)));
        parametres.add(addParametre(11L, CAS, CAS_NORMAL));
        parametres.add(addParametre(13L, "REFERENCE", operation.getCodeOperation()));
        parametres.add(addParametre(14L, "DONNEURORDRE", "BANQUE"));
        parametres.add(addParametre(15L, "BENEFICIAIRE", "BANQUE"));
        parametres.add(addParametre(17L, "CANAL", getCanal(operation, this.appId)));
        ParametreDTO param;
        param = setParams(parametres, CODE_SERVICE_CREATION_MONNAIE, operation.getCodeOperation());
        String message = showLogs(parametres);
        param.setCanal(this.appId);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, param, "POST");
        if (result != null) {
            Result resultOp = localRestClient.convert(result.getBody(), Result.class);
            if (resultOp != null) {
                resultOp.setDesc(message);
            }
            return resultOp;
        } else {
            return null;
        }

    }

    /**
     * *
     *
     * @param operation
     * @param fseSrc
     * @param fseDest
     * @param compteClient
     * @param normal
     * @param avecFrais
     * @param service
     * @return
     * @throws IOException
     */
    public Result sendRequestComptabilisationOfRetraitAccountCashToOmnicanal(Operation operation, UniteOrganisation fseSrc, UniteOrganisation fseDest, Compte compteClient, Boolean normal, Boolean avecFrais, String service) throws IOException {
        log.info("Service de comptabilisation d'une opération de Account Cash");
        RestClient localRestClient = setParam();
            AuthTokenInfo tokeinfo = null;//= localRestClient.sendTokenRequest();

        // restClient.sendTokenRequest();//token
        String codeAgenceDest = "0";
        localRestClient.setRestServerUri(this.url + URL_PRESENTATION);
        String codeOfCpteOperation = "";
        //  UniteOrganisation fseDestAgenceSiege = uniteOrganisationService.getUoAgenceSiege(fseDest);//L'agence siège de l'organisation initiateur du MAD
        Compte compteAttenteRetrait = compteService.getCompteOfOrganisation(operation.getPointVente().getUniteOrganisation(), EtiquetteCompteCode.CPTEOPERATIONACTEUR);
        CompteBanque clientBanque = convertCompte(compteClient);
        if (compteAttenteRetrait != null && compteClient != null && clientBanque != null) {
            String codeAgenceSrc = clientBanque.getCodeAgence().equals("") ? "01001" : clientBanque.getCodeAgence();
            if (compteAttenteRetrait.getCompteBanque() != null) {
                codeAgenceDest = compteAttenteRetrait.getCompteBanque().getCodeAgence();
                codeOfCpteOperation = compteAttenteRetrait.getValeurCompte() + "#" + compteAttenteRetrait.getCompteBanque().getCodeAgence() + "#" + compteAttenteRetrait.getCompteBanque().getCodeBanque();

            }

            if (compteAttenteRetrait.getCompteAutre() != null) {
                codeOfCpteOperation = compteAttenteRetrait.getValeurCompte();

            }

            String codeFseDest = fseDest.getCodeFse();
            String codeFseSource = fseSrc.getCodeFse();
            RegleCommission regleCommission = null;
            String codeOfCompteClient = compteClient.getValeurCompte() + "#" + clientBanque.getCodeAgence() + "#" + clientBanque.getCodeBanque();
            codeOfCompteClient = codeOfCompteClient.replace("##", "");//COMPTE NON BANCAIRE
            UniteOrganisation uo = uniteOrganisationService.findOneById(operation.getPointVente().getUniteOrganisation().getId());
            TypeUniteOrganisation typeUniteOrganisa = typeUniteOrganisationRepository.findById(uo.getTypeUO().getId()).orElse(null);
            if (!Util.isBiggerThan(operation.getMontantFraisClient(), BigDecimal.ZERO)) {/// si le client n'est pas responsable de frais alors
                regleCommission = regleCommissionService.verifRegleCommission(fseSrc.getId(), fseDest.getId(), TypeOperateur.valueOf(typeUniteOrganisa.getCodeTypeUO()), operation.getTypeService().getId(), compteClient, null, null, operation.getMontantOp());
            }

            String codeOfComptSrcFrais = getCompteSrcFrais(compteClient, regleCommission);
            codeOfComptSrcFrais = codeOfComptSrcFrais.replace("##", "");//COMPTE NON BANCAIRE

            List<ParametreUserDTO> parametres = new ArrayList<>();
            parametres.add(addParametre(1L, MontantsAComptabiliser.MONTANTHT.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTHT, operation).intValue())));
            if (avecFrais) {//Comptabilisation en prenant en compte les frais de transaction
                log.info("Comptabilisation avec prise en compte des frais de transaction");
                parametres.add(addParametre(2L, MontantsAComptabiliser.MONTANTTAXE.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTTAXE, operation).intValue())));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMM.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMM, operation).intValue())));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTHTCOMACQ.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTHTCOMACQ, operation).intValue())));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMACQ.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMACQ, operation).intValue())));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMEMT.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMEMT, operation).intValue())));

            } else {
                log.info("Comptabilisation sans prise en compte des frais de transaction");
                parametres.add(addParametre(2L, MontantsAComptabiliser.MONTANTTAXE.toString(), String.valueOf(0)));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMM.toString(), String.valueOf(0)));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTHTCOMACQ.toString(), String.valueOf(0)));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMACQ.toString(), String.valueOf(0)));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMEMT.toString(), String.valueOf(0)));

            }

            parametres.add(addParametre(5L, "CODEFSEDEST", codeFseDest));
            parametres.add(addParametre(6L, "CODEFSESOURCE", codeFseSource));

            parametres.add(addParametre(7L, "CODEAGENCEDEST", codeAgenceDest));
            parametres.add(addParametre(8L, "CODEAGENCESOURCE", codeAgenceSrc));
            parametres.add(addParametre(11L, compteAttenteRetrait.getEtiquette(), codeOfCpteOperation));//CPTEOPERATIONACTEUR
            parametres.add(addParametre(9L, compteClient.getEtiquette(), codeOfCompteClient));//CPTECLIENT
            parametres.add(addParametre(10L, EtiquetteCompteCode.CPTESRCFRAIS.toString(), codeOfComptSrcFrais));//CPTEATTENTECOMTAXE
            parametres.add(addParametre(16L, "DESCRIPTION", getDescription(operation)));
            if (normal) {
                parametres.add(addParametre(11L, CAS, CAS_NORMAL));
            } else {
                parametres.add(addParametre(12L, CAS, CAS_REVERSION));
            }

            parametres.add(addParametre(13L, "REFERENCE", operation.getCodeOperation()));
            parametres.add(addParametre(14L, "DONNEURORDRE", "-"));
            parametres.add(addParametre(15L, "BENEFICIAIRE", "-"));
            parametres.add(addParametre(17L, "CANAL", getCanal(operation, this.appId)));
            ParametreDTO param;

            param = setParams(parametres, CODE_SERVICE_RETRAIT_COMPTE, operation.getCodeOperation());

            String message = showLogs(parametres);
            param.setCanal(this.appId);
            ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, param, "POST");
            if (result != null) {
                Result resultOp = localRestClient.convert(result.getBody(), Result.class);
                if (resultOp != null) {
                    resultOp.setDesc(message);
                }
                return resultOp;
            } else {
                return null;
            }
        } else {
            throw new CustomParameterizedException("Les comptes de comptabilisation sont introuvables.");
        }
    }

    /**
     * Service de comptabilisation d'une operation de CASH_ACCOUNT
     *
     * @param uoSrc
     * @param uoDest
     * @param operation
     * @param fseSrc
     * @param fseDest
     * @param compteClient
     * @return
     * @throws IOException
     */
    public Result sendRequestComptabilisationOfCashTransfertToOmnicanal(UniteOrganisation uoSrc, UniteOrganisation uoDest,
            Operation operation, UniteOrganisation fseSrc,
            UniteOrganisation fseDest, Compte compteClient, Boolean normal, Boolean avecFrais) throws IOException {
        log.info(ZonedDateTime.now() + " Service de comptabilisation d'une opération de CashAccount");

        RestClient localRestClient = setParam();
            AuthTokenInfo tokeinfo = null;// localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + URL_PRESENTATION);

        CompteBanque clientBanque = convertCompte(compteClient);
        UniteOrganisation organisation = uniteOrganisationService.findOneById(operation.getPointVente().getUniteOrganisation().getId());
        UniteOrganisation uoDestAgenceSiege = uniteOrganisationService.getFseAgenceSiege(organisation);
        Compte compteAttenteRetrait = compteService.getCompteOfOrganisation(uoDestAgenceSiege, EtiquetteCompteCode.CPTEATTENTERETRAIT);
        Compte compteOperation = compteService.getCompteOfOrganisation(operation.getPointVente().getUniteOrganisation(), EtiquetteCompteCode.CPTEOPERATIONACTEUR);
        if (compteAttenteRetrait != null && compteClient != null && clientBanque != null) {

            UniteOrganisation fdest = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.FSE);
            UniteOrganisation fsrc = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.FSE);
            String codeFseDest = fdest.getCodeFse();
            String codeFseSource = fsrc.getCodeFse();
            String codeFse = null;
            String codeOfCpteOperation = "";
            String codeOfCompteClient = "";
            if (compteAttenteRetrait.getCompteBanque() != null) {
                codeOfCpteOperation = compteAttenteRetrait.getValeurCompte() + "#" + compteAttenteRetrait.getCompteBanque().getCodeAgence() + "#" + compteAttenteRetrait.getCompteBanque().getCodeBanque();
                codeFse = compteAttenteRetrait.getCompteBanque().getCodeFse();
            }
            if (compteAttenteRetrait.getCompteAutre() != null) {
                codeOfCpteOperation = compteAttenteRetrait.getValeurCompte();
                codeFse = compteAttenteRetrait.getCompteAutre().getCodeFse();
            }
            if (codeFseSource == null) {
                codeFseSource = codeFse;
            }
            if (compteClient.getCompteBanque() != null) {
                codeOfCompteClient = compteClient.getValeurCompte() + "#" + clientBanque.getCodeAgence() + "#" + clientBanque.getCodeBanque();
            } else {
                codeOfCompteClient = compteClient.getValeurCompte();
            }

            codeOfCompteClient = codeOfCompteClient.replace("##", "");//COMPTE NON BANCAIRE
            UniteOrganisation uo = uniteOrganisationService.findOneById(operation.getPointVente().getUniteOrganisation().getId());
            TypeUniteOrganisation typeUniteOrganisa = typeUniteOrganisationRepository.findById(uo.getTypeUO().getId()).orElse(null);
            RegleCommission regleCommission = null;
            if (!Util.isBiggerThan(operation.getMontantFraisClient(), BigDecimal.ZERO)) {/// si le client n'est pas responsable de frais alors

                regleCommission = regleCommissionService.verifRegleCommission(fseSrc.getId(), fseDest.getId(), TypeOperateur.valueOf(typeUniteOrganisa.getCodeTypeUO()), operation.getTypeService().getId(), compteAttenteRetrait, uoSrc.getId(), uoDest.getId(), operation.getMontantOp());

            }
            String codeOfComptSrcFrais = getCompteSrcFrais(compteAttenteRetrait, regleCommission);
            String codeAgenceDest = clientBanque.getCodeAgence().equals("") ? "01001" : clientBanque.getCodeAgence();
            String codeAgenceSrc = "";
            if (compteAttenteRetrait.getCompteBanque() != null) {
                codeAgenceSrc = compteAttenteRetrait.getCompteBanque().getCodeAgence();
            }

            List<ParametreUserDTO> parametres = new ArrayList<>();
            parametres.add(addParametre(1L, MontantsAComptabiliser.MONTANTHT.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTHT, operation).intValue())));
            if (avecFrais) {
                log.info("Comptabilisation avec prise en compte des frais de transaction");
                parametres.add(addParametre(2L, MontantsAComptabiliser.MONTANTTAXE.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTTAXE, operation).intValue())));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMM.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMM, operation).intValue())));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTHTCOMACQ.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTHTCOMACQ, operation).intValue())));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMACQ.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMACQ, operation).intValue())));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMEMT.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMEMT, operation).intValue())));
            } else {
                log.info("Comptabilisation sans prise en compte des frais de transaction");
                parametres.add(addParametre(2L, MontantsAComptabiliser.MONTANTTAXE.toString(), String.valueOf(0)));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMM.toString(), String.valueOf(0)));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTHTCOMACQ.toString(), String.valueOf(0)));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMACQ.toString(), String.valueOf(0)));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMEMT.toString(), String.valueOf(0)));
            }
            parametres.add(addParametre(5L, "CODEFSEDEST", codeFseDest));
            parametres.add(addParametre(6L, "CODEFSESOURCE", codeFseSource));
            parametres.add(addParametre(7L, "CODEAGENCEDEST", codeAgenceDest));
            parametres.add(addParametre(8L, "CODEAGENCESOURCE", codeAgenceSrc));

            parametres.add(addParametre(9L, compteOperation.getEtiquette(), codeOfCpteOperation));//Normalement le compte d'opération acteur mais on fourni le cpte d'attente retrait
            parametres.add(addParametre(10L, compteClient.getEtiquette(), codeOfCompteClient));//COMPTE DEST
            parametres.add(addParametre(11L, EtiquetteCompteCode.CPTESRCFRAIS.toString(), codeOfComptSrcFrais));//CPTEATTENTECOMTAXE
            if (normal) {
                parametres.add(addParametre(12L, CAS, CAS_NORMAL));
            } else {
                parametres.add(addParametre(13L, CAS, CAS_REVERSION));
            }
            parametres.add(addParametre(14L, "REFERENCE", operation.getCodeOperation()));
            parametres.add(addParametre(15L, "DONNEURORDRE", "-"));
            parametres.add(addParametre(16L, "BENEFICIAIRE", "-"));
            parametres.add(addParametre(17L, "DESCRIPTION", getDescription(operation)));
            parametres.add(addParametre(17L, "CANAL", getCanal(operation, this.appId)));
            ParametreDTO param = setParams(parametres, CODE_SERVICE_CASH_ACCOUNT, operation.getCodeOperation());
            String message = showLogs(parametres);
            param.setCanal(this.appId);
            ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, param, "POST");
            if (result != null) {
                Result resultOp = localRestClient.convert(result.getBody(), Result.class);
                if (resultOp != null) {
                    resultOp.setDesc(message);
                }

                return resultOp;
            } else {
                return null;
            }
        } else {
            throw new CustomParameterizedException("Les comptes de comptabilisation sont introuvables.");
        }

    }

    /**
     * Service de comptabilisation d'une operation de CASH_ACCOUNT
     *
     * @param uoSrc
     * @param uoDest
     * @param operation
     * @param fseSrc
     * @param fseDest
     * @param compteClient
     * @return
     * @throws IOException
     */
    public Result sendRequestComptabilisationOfCashAccountToOmnicanal(UniteOrganisation uoSrc, UniteOrganisation uoDest,
            Operation operation, UniteOrganisation fseSrc,
            UniteOrganisation fseDest, Compte compteClient, Boolean normal, Boolean avecFrais) throws IOException {
        log.info(ZonedDateTime.now() + " Service de comptabilisation d'une opération de CashAccount");

        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = null; //localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + URL_PRESENTATION);
        //AuthTokenInfo tokeinfo = null;//restClient.sendTokenRequest();
        CompteBanque clientBanque = convertCompte(compteClient);

        Compte compteOperationActeurEnvoi = null;

        Compte compteOperationActeur = compteService.getCompteOfOrganisation(operation.getPointVente().getUniteOrganisation(), EtiquetteCompteCode.CPTEOPERATIONACTEUR);
        if (compteService.useTwoOperationAccount()) {
            compteOperationActeurEnvoi = compteService.getCompteOfOrganisation(operation.getPointVente().getUniteOrganisation(), EtiquetteCompteCode.CPTEOPERATIONENVOI);
            if (compteOperationActeurEnvoi != null) {
                compteOperationActeur = compteOperationActeurEnvoi;
            }
        }

        if (compteOperationActeur != null && compteClient != null && clientBanque != null) {
            String codeFseDest = fseDest.getCodeFse();
            String codeFseSource = "";
            String codeFse = null;
            String codeOfCpteOperation = "";
            String codeOfCompteClient = "";
            if (compteOperationActeur.getCompteBanque() != null) {
                codeOfCpteOperation = compteOperationActeur.getValeurCompte() + "#" + compteOperationActeur.getCompteBanque().getCodeAgence() + "#" + compteOperationActeur.getCompteBanque().getCodeBanque();
                codeFse = compteOperationActeur.getCompteBanque().getCodeFse();
            }
            if (compteOperationActeur.getCompteAutre() != null) {
                codeOfCpteOperation = compteOperationActeur.getValeurCompte();
                codeFse = compteOperationActeur.getCompteAutre().getCodeFse();
            }
            if (codeFseSource.length() == 0) {
                codeFseSource = codeFse;
            }
            if (compteClient.getCompteBanque() != null) {
                codeOfCompteClient = compteClient.getValeurCompte() + "#" + clientBanque.getCodeAgence() + "#" + clientBanque.getCodeBanque();
            } else {
                codeOfCompteClient = compteClient.getValeurCompte();
            }

            codeOfCompteClient = codeOfCompteClient.replace("##", "");//COMPTE NON BANCAIRE
            UniteOrganisation uo = uniteOrganisationService.findOneById(operation.getPointVente().getUniteOrganisation().getId());
            TypeUniteOrganisation typeUniteOrganisa = typeUniteOrganisationRepository.findById(uo.getTypeUO().getId()).orElse(null);
            RegleCommission regleCommission = null;
            if (!Util.isBiggerThan(operation.getMontantFraisClient(), BigDecimal.ZERO)) {/// si le client n'est pas responsable de frais alors
                regleCommission = regleCommissionService.verifRegleCommission(fseDest.getId(), fseDest.getId(), TypeOperateur.valueOf(typeUniteOrganisa.getCodeTypeUO()), operation.getTypeService().getId(), compteOperationActeur, uoSrc.getId(), uoDest.getId(), operation.getMontantOp());
            }

            String codeOfComptSrcFrais = getCompteSrcFrais(compteOperationActeur, regleCommission);
            String codeAgenceDest = clientBanque.getCodeAgence().equals("") ? "01001" : clientBanque.getCodeAgence();
            String codeAgenceSrc = "";
            if (compteOperationActeur.getCompteBanque() != null) {
                codeAgenceSrc = compteOperationActeur.getCompteBanque().getCodeAgence();
            }

            List<ParametreUserDTO> parametres = new ArrayList<>();
            parametres.add(addParametre(1L, MontantsAComptabiliser.MONTANTHT.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTHT, operation).intValue())));
            if (avecFrais) {
                log.info("Comptabilisation avec prise en compte des frais de transaction");
                parametres.add(addParametre(2L, MontantsAComptabiliser.MONTANTTAXE.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTTAXE, operation).intValue())));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMM.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMM, operation).intValue())));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTHTCOMACQ.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTHTCOMACQ, operation).intValue())));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMACQ.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMACQ, operation).intValue())));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMEMT.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMEMT, operation).intValue())));
            } else {
                log.info("Comptabilisation sans prise en compte des frais de transaction");
                parametres.add(addParametre(2L, MontantsAComptabiliser.MONTANTTAXE.toString(), String.valueOf(0)));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMM.toString(), String.valueOf(0)));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTHTCOMACQ.toString(), String.valueOf(0)));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMACQ.toString(), String.valueOf(0)));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMEMT.toString(), String.valueOf(0)));
            }
            parametres.add(addParametre(5L, "CODEFSEDEST", codeFseDest));
            parametres.add(addParametre(6L, "CODEFSESOURCE", codeFseSource));
            parametres.add(addParametre(7L, "CODEAGENCEDEST", codeAgenceDest));
            parametres.add(addParametre(8L, "CODEAGENCESOURCE", codeAgenceSrc));
            parametres.add(addParametre(18L, "CPTEBENEFICIAIRE", codeOfCompteClient));
            parametres.add(addParametre(9L, compteOperationActeur.getEtiquette(), codeOfCpteOperation));//CPTEOPERATIONACTEUR
            parametres.add(addParametre(10L, compteClient.getEtiquette(), codeOfCompteClient));//COMPTE DEST
            parametres.add(addParametre(11L, EtiquetteCompteCode.CPTESRCFRAIS.toString(), codeOfComptSrcFrais));//CPTEATTENTECOMTAXE
            if (normal) {
                parametres.add(addParametre(12L, CAS, CAS_NORMAL));
            } else {
                parametres.add(addParametre(13L, CAS, CAS_REVERSION));
            }
            parametres.add(addParametre(14L, "REFERENCE", operation.getCodeOperation()));
            parametres.add(addParametre(15L, "DONNEURORDRE", "-"));
            parametres.add(addParametre(16L, "BENEFICIAIRE", "-"));
            parametres.add(addParametre(17L, "DESCRIPTION", getDescription(operation)));
            parametres.add(addParametre(17L, "CANAL", getCanal(operation, this.appId)));
            ParametreDTO param = setParams(parametres, CODE_SERVICE_CASH_ACCOUNT, operation.getCodeOperation());
            String message = showLogs(parametres);
            param.setCanal(this.appId);
            ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, param, "POST");
            if (result != null) {
                Result resultOp = localRestClient.convert(result.getBody(), Result.class);
                if (resultOp != null) {
                    resultOp.setDesc(message);
                }
                return resultOp;
            } else {
                return null;
            }
        } else {
            throw new CustomParameterizedException("Les comptes de comptabilisation sont introuvables.");
        }

    }

    public Result sendRequestComptabilisationOfAccountCashToOmnicanal_olgd(Operation operation, UniteOrganisation fseSrc, UniteOrganisation fseDest, Compte compteClient, Boolean normal, Boolean avecFrais) throws IOException {
        log.info("Service de comptabilisation d'une opération de Account Cash");
           StopWatch stopWatch = new StopWatch("sendRequestComptabilisationOfAccountCashToOmnicanal_olgd");
        //  AuthTokenInfo tokeinfo = null; //restClient.sendTokenRequest();//token
        String codeAgenceDest = "0";

        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = null;

        localRestClient.setRestServerUri(this.url + URL_PRESENTATION);
   stopWatch.start("1. RECHERCHE AGENCE SIEGE");
        UniteOrganisation fseDestAgenceSiege = uniteOrganisationService.getUoAgenceSiege(fseDest);//L'agence siège de l'organisation initiateur du MAD
        stopWatch.stop();
           stopWatch.start("1. COMPTE ATTENTE RETRAIT");
        Compte compteAttenteRetrait = compteService.getCompteOfOrganisation(fseDestAgenceSiege, EtiquetteCompteCode.CPTEATTENTERETRAIT);
          stopWatch.stop();
        CompteBanque clientBanque = convertCompte(compteClient);
        if (compteAttenteRetrait != null && compteClient != null && clientBanque != null) {
            
             stopWatch.start("1. Chargement PARAMA");
            String codeAgenceSrc = clientBanque.getCodeAgence().equals("") ? "01001" : clientBanque.getCodeAgence();
            if (compteAttenteRetrait.getCompteBanque() != null) {
                codeAgenceDest = compteAttenteRetrait.getCompteBanque().getCodeAgence();
            }
            String codeFseDest = fseDest.getCodeFse();
            String codeFseSource = fseSrc.getCodeFse();
            String codeOfCompteClient = compteClient.getValeurCompte() + "#" + clientBanque.getCodeAgence() + "#" + clientBanque.getCodeBanque();
            codeOfCompteClient = codeOfCompteClient.replace("##", "");//COMPTE NON BANCAIRE
            UniteOrganisation uo = uniteOrganisationService.findOneById(operation.getPointVente().getUniteOrganisation().getId());
            TypeUniteOrganisation typeUniteOrganisa = typeUniteOrganisationRepository.findById(uo.getTypeUO().getId()).orElse(null);
            RegleCommission regleCommission = null;
            if (!Util.isBiggerThan(operation.getMontantFraisClient(), BigDecimal.ZERO)) {/// si le client n'est pas responsable de frais alors

                regleCommission = regleCommissionService.verifRegleCommission(fseSrc.getId(), fseDest.getId(), TypeOperateur.valueOf(typeUniteOrganisa.getCodeTypeUO()), operation.getTypeService().getId(), compteClient, null, null, operation.getMontantOp());
            }
            String codeOfComptSrcFrais = getCompteSrcFrais(compteClient, regleCommission);
            codeOfComptSrcFrais = codeOfComptSrcFrais.replace("##", "");//COMPTE NON BANCAIRE
            List<ParametreUserDTO> parametres = new ArrayList<>();
            parametres.add(addParametre(2L, MontantsAComptabiliser.MONTANT.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANT, operation).intValue())));
            parametres.add(addParametre(1L, MontantsAComptabiliser.MONTANTHT.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTHT, operation).intValue())));
            if (avecFrais) {//Comptabilisation en prenant en compte les frais de transaction
                log.info("Comptabilisation avec prise en compte des frais de transaction");
                parametres.add(addParametre(2L, MontantsAComptabiliser.MONTANTTAXE.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTTAXE, operation).intValue())));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMM.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMM, operation).intValue())));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTHTCOMACQ.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTHTCOMACQ, operation).intValue())));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMACQ.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMACQ, operation).intValue())));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMEMT.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMEMT, operation).intValue())));
            } else {
                log.info("Comptabilisation sans prise en compte des frais de transaction");
                parametres.add(addParametre(2L, MontantsAComptabiliser.MONTANTTAXE.toString(), String.valueOf(0)));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMM.toString(), String.valueOf(0)));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTHTCOMACQ.toString(), String.valueOf(0)));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMACQ.toString(), String.valueOf(0)));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMEMT.toString(), String.valueOf(0)));
            }
            parametres.add(addParametre(5L, "CODEFSEDEST", codeFseDest));
            parametres.add(addParametre(6L, "CODEFSESOURCE", codeFseSource));
            parametres.add(addParametre(7L, "CODEAGENCEDEST", codeAgenceDest));
            parametres.add(addParametre(8L, "CODEAGENCESOURCE", codeAgenceSrc));
            parametres.add(addParametre(9L, compteClient.getEtiquette(), codeOfCompteClient));//CPTECLIENT
            parametres.add(addParametre(10L, EtiquetteCompteCode.CPTESRCFRAIS.toString(), codeOfComptSrcFrais));//CPTEATTENTECOMTAXE
            parametres.add(addParametre(16L, "DESCRIPTION", getDescription(operation)));
            if (normal) {
                parametres.add(addParametre(11L, CAS, CAS_NORMAL));
            } else {
                parametres.add(addParametre(12L, CAS, CAS_REVERSION));
            }
            parametres.add(addParametre(13L, "REFERENCE", operation.getCodeOperation()));
            parametres.add(addParametre(14L, "DONNEURORDRE", "-"));
            parametres.add(addParametre(15L, "BENEFICIAIRE", "-"));
            parametres.add(addParametre(17L, "CANAL", getCanal(operation, this.appId)));
            ParametreDTO param = setParams(parametres, CODE_SERVICE_ACCOUNT_CASH, operation.getCodeOperation());
            String message = showLogs(parametres);
            param.setCanal(this.appId);
            
                stopWatch.stop();
                    stopWatch.start("1. APPEL OMNICANAL");
            ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, param, "POST");
            
            stopWatch.stop();
             
            if (result != null) {
               
                Result resultOp = localRestClient.convert(result.getBody(), Result.class);
                  stopWatch.stop();
                if (resultOp != null) {
                    resultOp.setDesc(message);
                }
                
                
                 System.out.println(stopWatch.prettyPrint());
                return resultOp;
            } else {
                return null;
            }
               
        } else {
            throw new CustomParameterizedException("Les comptes de comptabilisation sont introuvables.");
        }
         
          
           
    }

    public Result sendRequestComptabilisationOfAccountCashToOmnicanal(Operation operation, UniteOrganisation fseSrc, UniteOrganisation fseDest, Compte compteClient, Boolean normal, Boolean avecFrais) throws IOException {
        log.info("Service de comptabilisation d'une opération de Account Cash");
  StopWatch stopWatch = new StopWatch("sendRequestComptabilisationOfAccountCashToOmnicanal");
        String codeAgenceDest = "0";
        RestClient restClient = setParam();
        AuthTokenInfo tokeinfo = null;//restClient.sendTokenRequest();//token
        restClient.setRestServerUri(this.url + URL_PRESENTATION);
         stopWatch.start("1. RECHERCHE AGENCE SIEGE");
          
        UniteOrganisation fseDestAgenceSiege = uniteOrganisationService.getUoAgenceSiege(fseDest);//L'agence siège de l'organisation initiateur du MAD
          stopWatch.stop();
           stopWatch.start("1. COMPTE ATTENTE RETRAIT");
        Compte compteAttenteRetrait = compteService.getCompteOfOrganisation(fseDestAgenceSiege, EtiquetteCompteCode.CPTEATTENTERETRAIT);
             stopWatch.stop();
           stopWatch.start("1. COMPTE ATTENTE RETRAIT  convertCompte");
        CompteBanque clientBanque = convertCompte(compteClient);
            stopWatch.stop();
           
        if (compteAttenteRetrait != null && compteClient != null && clientBanque != null) {
               stopWatch.start("1. COMPTE ATTENTE RETRAIT  PARAMA");
            String codeAgenceSrc = clientBanque.getCodeAgence().equals("") ? "01001" : clientBanque.getCodeAgence();
            if (compteAttenteRetrait.getCompteBanque() != null) {
                codeAgenceDest = compteAttenteRetrait.getCompteBanque().getCodeAgence();
            }
            String codeFseDest = fseDest.getCodeFse();
            String codeFseSource = fseSrc.getCodeFse();
            String codeOfCompteClient = compteClient.getValeurCompte() + "#" + clientBanque.getCodeAgence() + "#" + clientBanque.getCodeBanque();
            codeOfCompteClient = codeOfCompteClient.replace("##", "");//COMPTE NON BANCAIRE
            UniteOrganisation uo = uniteOrganisationService.findOneById(operation.getPointVente().getUniteOrganisation().getId());
            TypeUniteOrganisation typeUniteOrganisa = typeUniteOrganisationRepository.findById(uo.getTypeUO().getId()).orElse(null);
            RegleCommission regleCommission = null;
            if (!Util.isBiggerThan(operation.getMontantFraisClient(), BigDecimal.ZERO)) {/// si le client n'est pas responsable de frais alors

                regleCommission = regleCommissionService.verifRegleCommission(fseSrc.getId(), fseDest.getId(), TypeOperateur.valueOf(typeUniteOrganisa.getCodeTypeUO()), operation.getTypeService().getId(), compteClient, null, null, operation.getMontantOp());
            }
            String codeOfComptSrcFrais = getCompteSrcFrais(compteClient, regleCommission);
            codeOfComptSrcFrais = codeOfComptSrcFrais.replace("##", "");//COMPTE NON BANCAIRE
            List<ParametreUserDTO> parametres = new ArrayList<>();
            parametres.add(addParametre(1L, MontantsAComptabiliser.MONTANTHT.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTHT, operation).intValue())));
            if (avecFrais) {//Comptabilisation en prenant en compte les frais de transaction
                log.info("Comptabilisation avec prise en compte des frais de transaction");
                parametres.add(addParametre(2L, MontantsAComptabiliser.MONTANTTAXE.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTTAXE, operation).intValue())));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMM.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMM, operation).intValue())));
                parametres.add(addParametre(4L, MontantsAComptabiliser.MONTANTHTCOMACQ.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTHTCOMACQ, operation).intValue())));
                parametres.add(addParametre(5L, MontantsAComptabiliser.MONTANTCOMACQ.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMACQ, operation).intValue())));
                parametres.add(addParametre(6L, MontantsAComptabiliser.MONTANTCOMEMT.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMEMT, operation).intValue())));
            } else {
                log.info("Comptabilisation sans prise en compte des frais de transaction");
                parametres.add(addParametre(2L, MontantsAComptabiliser.MONTANTTAXE.toString(), String.valueOf(0)));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMM.toString(), String.valueOf(0)));
                parametres.add(addParametre(4L, MontantsAComptabiliser.MONTANTHTCOMACQ.toString(), String.valueOf(0)));
                parametres.add(addParametre(5L, MontantsAComptabiliser.MONTANTCOMACQ.toString(), String.valueOf(0)));
                parametres.add(addParametre(6L, MontantsAComptabiliser.MONTANTCOMEMT.toString(), String.valueOf(0)));
            }
            parametres.add(addParametre(7L, "CODEFSEDEST", codeFseDest));
            parametres.add(addParametre(8L, "CODEFSESOURCE", codeFseSource));
            parametres.add(addParametre(9L, "CODEAGENCEDEST", codeAgenceDest));
            parametres.add(addParametre(10L, "CODEAGENCESOURCE", codeAgenceSrc));
            parametres.add(addParametre(11L, compteClient.getEtiquette(), codeOfCompteClient));//CPTECLIENT
            parametres.add(addParametre(12L, EtiquetteCompteCode.CPTESRCFRAIS.toString(), codeOfComptSrcFrais));//CPTEATTENTECOMTAXE
            parametres.add(addParametre(13L, "DESCRIPTION", getDescription(operation)));
            /*  String extendedData = getExtendData(operation, codeFseSource, codeFseDest);
            if (extendedData != null) {
                parametres.add(addParametre(18L, "EXTENDEDDATA", extendedData));
            }*/
            if (normal) {
                parametres.add(addParametre(11L, CAS, CAS_NORMAL));
            } else {
                parametres.add(addParametre(12L, CAS, CAS_REVERSION));
            }
            parametres.add(addParametre(14L, "REFERENCE", operation.getCodeOperation()));
            parametres.add(addParametre(15L, "DONNEURORDRE", "-"));
            parametres.add(addParametre(16L, "BENEFICIAIRE", "-"));
            parametres.add(addParametre(17L, "CANAL", getCanal(operation, this.appId)));
            ParametreDTO param = setParams(parametres, CODE_SERVICE_ACCOUNT_CASH, operation.getCodeOperation());
            String message = showLogs(parametres);
            param.setCanal(this.appId);
               stopWatch.stop();
               
                    stopWatch.start("1. APPPEl OMNICANAL");
            ResponseEntity result = restClient.getRessourcesObject(tokeinfo, param, "POST");
             stopWatch.stop();
               System.out.println(stopWatch.prettyPrint());
            if (result != null) {
                Result resultOp = restClient.convert(result.getBody(), Result.class);
                if (resultOp != null) {
                    resultOp.setDesc(message);
                }
                return resultOp;
            } else {
                return null;
            }
        } else {
            throw new CustomParameterizedException("Les comptes de comptabilisation sont introuvables.");
        }
    }

    /**
     * Service de comptabilisation d'une operation de (ACCOUNT_CASH ||
     * APPEL_FOND)
     *
     * @param operation
     * @param fseSrc
     * @param fseDest
     * @param compteClient
     * @return
     * @throws IOException
     */
    private String getCanalOLD(Operation operation, String application) {
        Long typeServiceId = null;
        Long uniteOrganisationId = null;
        if (operation != null) {
            if (operation.getTypeService() != null) {
                typeServiceId = operation.getTypeService().getId();
            }
            if (operation.getPointVente() != null && operation.getPointVente().getUniteOrganisation() != null) {
                uniteOrganisationId = operation.getPointVente().getUniteOrganisation().getId();
            }

        }
        List<ParamCanal> listparamCanal = paramCanalRepository.getParamCanalApplication(typeServiceId, Application.valueOf(application), uniteOrganisationId);
        String sousCanal = "";
        if (listparamCanal != null && listparamCanal.size() > 0) {
            if (listparamCanal.get(0).getSousCanal() != null) {
                sousCanal = listparamCanal.get(0).getSousCanal();
                return listparamCanal.get(0).getCanal() + "#" + sousCanal;
            }
            return listparamCanal.get(0).getCanal();
        }
        /* A developper */
        return "";
    }

    private String getCanal(Operation operation, String application) {
        Long typeServiceId = null;
        Long uniteOrganisationId = null;
        if (operation != null) {
            if (operation.getTypeService() != null) {
                typeServiceId = operation.getTypeService().getId();
            }
            if (operation.getPointVente() != null && operation.getPointVente().getUniteOrganisation() != null) {
                uniteOrganisationId = operation.getPointVente().getUniteOrganisation().getId();
            }

        }
        log.info("RECHERCHE DU CANAL DE L'ORGANISATION " + uniteOrganisationId);
        log.info("RECHERCHE DU CANAL TYPE SERVICE " + typeServiceId);
        log.info("RECHERCHE DU CANAL TYPE APPLICATION " + application);

        List<ParamCanal> listparamCanal = paramCanalRepository.getParamCanalApplication(typeServiceId, Application.valueOf(application), uniteOrganisationId);
        String sousCanal = "";
        if (listparamCanal != null && listparamCanal.size() > 0) {
            String canal = "";
            if (listparamCanal.get(0).getSousCanal() != null) {
                sousCanal = listparamCanal.get(0).getSousCanal();
                canal = listparamCanal.get(0).getCanal() + "#" + sousCanal;
                log.info("RETOUR DU CANAL" + canal);
                return canal;

            }
            canal = listparamCanal.get(0).getCanal();
            return canal;
        }
        /* A developper */
        return "";
    }

    public List<ParametreUserDTO> ajouParameCard(Carte carte, List<ParametreUserDTO> parametres) {
        parametres.add(addParametre(3L, "LAST4DIGIT", carte.getLast4Digit()));

        return parametres;
    }

    /**
     * Service de comptabilisation d'une operation de type ACCOUNT_ACCOUNT En
     * fonction du contexte du service des paramètres specifiques sont fournis
     *
     * @param uoSrc
     * @param uoDest
     * @param operation
     * @param fseSrc
     * @param fseDest
     * @param compteClientSrc
     * @param compteClientDest
     * @param normal
     * @param avecFrais
     * @return
     * @throws IOException
     */
    public Result sendRequestComptabilisationOfAccountAccountToOmnicanal(UniteOrganisation uoSrc, UniteOrganisation uoDest, Operation operation, UniteOrganisation fseSrc, UniteOrganisation fseDest, Compte compteClientSrc, Compte compteClientDest, Boolean normal, Boolean avecFrais, Compte cptCommission) throws IOException {

        StopWatch stopWatch = new StopWatch("accès à l'omnicanal");
        log.info("Service de comptabilisation d'une operaiton de virement");
        ///  restClient.sendTokenRequest();

        RestClient localRestClient = setParam();
          AuthTokenInfo tokeinfo = null;//localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + URL_PRESENTATION);
        stopWatch.start("1. FIND TOKEN");
        // AuthTokenInfo tokeinfo = null; //=  restClient.sendTokenRequest();
        stopWatch.stop();
        String codeOfCompteCommission = "";

        // log.info("COMPTE SRC:" + compteClientSrc != null ? compteClientSrc.getValeurCompte() : null);
        // log.info("COMPTE DEST:" + compteClientDest != null ? compteClientDest.getValeurCompte() : null);
        //   log.info("COMPTE COMMISSION:" + cptCommission != null ? cptCommission.getValeurCompte() : null);
        CompteBanque clientBanqueSrc = convertCompte(compteClientSrc);
        CompteBanque clientBanqueDest = convertCompte(compteClientDest);

        List<ParametreUserDTO> parametres = new ArrayList<>();

        if (compteClientDest != null && compteClientDest.getCategorieCompte().equals(CategorieCompte.CARTE) && compteClientDest.getCarte() != null) {
            parametres = ajouParameCard(compteClientDest.getCarte(), parametres);
        } else if (compteClientSrc != null && compteClientSrc.getCategorieCompte().equals(CategorieCompte.CARTE) && compteClientSrc.getCarte() != null) {
            parametres = ajouParameCard(compteClientSrc.getCarte(), parametres);
        }
        log.info("Recherche du compte commission");
        CompteBanque cptCommBanqueDest = convertCompte(cptCommission);
        if (cptCommission != null && cptCommBanqueDest != null) {
            codeOfCompteCommission = cptCommission.getValeurCompte() + "#" + cptCommBanqueDest.getCodeAgence() + "#" + cptCommBanqueDest.getCodeBanque();
            codeOfCompteCommission = codeOfCompteCommission.replace("##", "");

        }
        if (compteClientSrc != null && compteClientDest != null && clientBanqueSrc != null && clientBanqueDest != null) {
            log.info("les comptes sont trouvees");
            stopWatch.start("2. AUTRES TRAITEMENT OMNIC ");
            String codeOfCompteClientSrc = compteClientSrc.getValeurCompte() + "#" + clientBanqueSrc.getCodeAgence() + "#" + clientBanqueSrc.getCodeBanque();
            String codeOfCompteClientDest = compteClientDest.getValeurCompte() + "#" + clientBanqueDest.getCodeAgence() + "#" + clientBanqueDest.getCodeBanque();

            codeOfCompteClientDest = codeOfCompteClientDest.replace("##", "");
            codeOfCompteClientSrc = codeOfCompteClientSrc.replace("##", "");
            //Verification des regles de
            // prise en charge des frais
            UniteOrganisation uo = uniteOrganisationService.findOneById(operation.getPointVente().getUniteOrganisation().getId());

            log.info("Selection de L'UO");
            TypeUniteOrganisation typeUniteOrganisa = typeUniteOrganisationRepository.findById(uo.getTypeUO().getId()).orElse(null);

            RegleCommission regleCommission = null;
            if (!Util.isBiggerThan(operation.getMontantFraisClient(), BigDecimal.ZERO)) {/// si le client n'est pas responsable de frais alors
                regleCommission = regleCommissionService.verifRegleCommission(fseSrc.getId(), fseDest.getId(), TypeOperateur.valueOf(typeUniteOrganisa.getCodeTypeUO()), operation.getTypeService().getId(), compteClientSrc, uoSrc.getId(), uoDest.getId(), operation.getMontantOp());
            }
            String codeAgenceDest = clientBanqueDest.getCodeAgence().equals("") ? "01001" : clientBanqueDest.getCodeAgence();
            String codeAgenceSrc = clientBanqueSrc.getCodeAgence().equals("") ? "01001" : clientBanqueSrc.getCodeAgence();

            String codeOfComptSrcFrais = getCompteSrcFrais(compteClientSrc, regleCommission);
            codeOfComptSrcFrais = codeOfComptSrcFrais.replace("##", "");

            String codeFseDest = fseDest.getCodeFse();
            String codeFseSource = fseSrc.getCodeFse();

            //Configuration des parametres à envoyer
            parametres.add(addParametre(1L, MontantsAComptabiliser.MONTANTHT.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTHT, operation).intValue())));
            if (avecFrais) {
                parametres.add(addParametre(2L, MontantsAComptabiliser.MONTANTTAXE.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTTAXE, operation).intValue())));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMM.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMM, operation).intValue())));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTHTCOMACQ.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTHTCOMACQ, operation).intValue())));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMACQ.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMACQ, operation).intValue())));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMEMT.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTCOMEMT, operation).intValue())));
            } else {
                parametres.add(addParametre(2L, MontantsAComptabiliser.MONTANTTAXE.toString(), String.valueOf(0)));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMM.toString(), String.valueOf(0)));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTHTCOMACQ.toString(), String.valueOf(0)));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMACQ.toString(), String.valueOf(0)));
                parametres.add(addParametre(3L, MontantsAComptabiliser.MONTANTCOMEMT.toString(), String.valueOf(0)));

            }

            if (cptCommission != null) {
                parametres.add(addParametre(17L, "CPTEATTENTECOMM", codeOfCompteCommission));
            }
            parametres.add(addParametre(5L, "CODEFSEDEST", codeFseDest));
            parametres.add(addParametre(6L, "CODEFSESOURCE", codeFseSource));

            parametres.add(addParametre(7L, "CODEAGENCEDEST", codeAgenceDest));
            parametres.add(addParametre(8L, "CODEAGENCESOURCE", codeAgenceSrc));

            parametres.add(addParametre(9L, "CPTECLIENT", codeOfCompteClientSrc));
            parametres.add(addParametre(9L, "CPTEBENEFICIAIRE", codeOfCompteClientDest));

            parametres.add(addParametre(10L, EtiquetteCompteCode.CPTESRCFRAIS.toString(), codeOfComptSrcFrais));//CPTEATTENTECOMTAXE
            if (normal) {
                parametres.add(addParametre(11L, CAS, CAS_NORMAL));
            } else {
                parametres.add(addParametre(12L, CAS, CAS_REVERSION));
            }
            parametres.add(addParametre(13L, "REFERENCE", operation.getCodeOperation()));
            parametres.add(addParametre(14L, "DONNEURORDRE", getEmetteur(operation)));
            parametres.add(addParametre(15L, "BENEFICIAIRE", getBeneficiaire(operation)));
            parametres.add(addParametre(16L, "DESCRIPTION", getDescription(operation)));
            parametres.add(addParametre(17L, "CANAL", getCanal(operation, this.appId)));

            ParametreDTO param = setParams(parametres, CODE_SERVICE_ACCOUNT_ACCOUNT, operation.getCodeOperation());
            param.setCanal(this.appId);
            String message = showLogs(parametres);
            stopWatch.stop();
            stopWatch.start("3. APPEL A OMINC TRAITEMENT OMNIC ");

            ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, param, "POST");
            stopWatch.stop();
            if (result != null) {
                stopWatch.start("3. TRAITEMENT REPONSE A OMINC TRAITEMENT OMNIC ");
                log.info("REPONSE DU TRAITEMENT" + result.getBody());
                Result resultOp = localRestClient.convert(result.getBody(), Result.class);
                if (resultOp != null) {
                    resultOp.setDesc(message);
                }
                stopWatch.stop();

                System.out.println(stopWatch.prettyPrint());
                return resultOp;
            } else {
                return null;
            }

        } else {
            throw new CustomParameterizedException("Les comptes de comptabilisation sont introuvables.");
        }

    }

    /**
     * @param compteOperationActeur
     * @param regleCommission
     * @return
     */
    private String getCompteSrcFrais(Compte compteOperationActeur, RegleCommission regleCommission) {
        String codeOfComptSrcFrais = "";
        if (regleCommission != null) {
            if (regleCommission.getCategorieCompte().equals(CategorieCompte.BANQUE)) {
                codeOfComptSrcFrais = regleCommission.getValeurCompte() + "#"
                        + regleCommission.getCodeAgence() + "#"
                        + regleCommission.getCodeBanque();
            } else {
                codeOfComptSrcFrais = regleCommission.getValeurCompte();
            }

            return codeOfComptSrcFrais;
        } else {
            log.info("Pas de regle de commission");
            CompteBanque compteBanque = convertCompte(compteOperationActeur);
            if (compteBanque != null) {
                if (compteBanque.getCodeAgence() != "") {
                    codeOfComptSrcFrais = compteOperationActeur.getValeurCompte() + "#" + compteBanque.getCodeAgence() + "#" + compteBanque.getCodeBanque();
                } else {
                    codeOfComptSrcFrais = compteOperationActeur.getValeurCompte();
                }
            }

            return codeOfComptSrcFrais;
        }
    }

    /**
     * Affichage des paramètres envoyés à l'omnicanal
     *
     * @param parametres
     */
    private String showLogs(List<ParametreUserDTO> parametres) {
        log.info("Paramètres de comptabilsiation :: ");
        String message = "";
        for (ParametreUserDTO param : parametres) {
            message += param.getCodeParametre() + ":" + param.getValeur() + " ";
            log.info(param.getCodeParametre() + "    #" + param.getValeur());
        }
        return message;
    }

    /**
     * @param parametres
     * @param codeService
     * @return
     */
    private synchronized ParametreDTO setParams(List<ParametreUserDTO> parametres, String codeService, String reference) {
        ParametreDTO param = new ParametreDTO();
        param.setCodeService(codeService); //code du type de service
        param.setCodeUniteOrganisation("01");//code UO
        param.setFse("0004"); //code FSE CBI
        param.setProprieteApi(ProprieteApi.FSESOURCE);
        param.setReferenceExterne(reference);
        param.setParam(parametres);
        return param;
    }

    /*une classe privée qui permet d'envoyer  des message*/
    private class MessageKafka implements Runnable {

        private Notification not;
        String type;

        public MessageKafka(Notification not, String type) {
            this.not = not;
            this.type = type;

        }

        public void run() {
            sendNotif(not, "sms");

        }

    }

    @Async("taskExecutor")
    public void sendNotif(Notification not, String type) {
        messageProducer.sendNotification(not, "sms");
    }

    public CodeErreur verifMessageRetourOmnicanal(String codeOperation) throws IOException {
        log.debug("verifMessageRetourOmnicanal");
        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = null;//restClient.sendTokenRequest();//token
        localRestClient.setRestServerUri(this.url + SERVICE_MSG_RETOUR);
        ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
        param.put("codeOperation", codeOperation);
        //   ResponseEntity result = this.getRessources(tokeinfo, param, "GET");
        ResponseEntity result = localRestClient.getRessources(tokeinfo, param, "GET");
        if (result != null) {
            CodeErreur resultOp = localRestClient.convert(result.getBody(), CodeErreur.class);
            return resultOp;
        } else {
           
            return new CodeErreur("-1","erreur");
        }

    }
    
    
    public void sendOperationAmbigue(OperationAmbigueDTO operationDTO) {
        messageProducer.sendMessageAmbigueOperation(operationDTO);
    }

    public void sendOperationAmbigue(OperationAmbigueDTO operationDTO, String codeFse, List<MParametre> extraData) {
        OperationControlDTO operationControlDTO = new OperationControlDTO();
        operationControlDTO.setCodeOperation(operationDTO.getCodeOperation());
        operationControlDTO.setCodeFse(codeFse);
        operationControlDTO.setExtraData(extraData);
        messageProducer.sendMessageAmbigueOperation(operationDTO);
        messageProducer.sendOperationExtourne(operationControlDTO);
    }

    @Async("taskExecutor")
    public Message doEnvoiMsg(String numero, String message) throws IOException, org.json.JSONException {

        String url = urlGatewayRessource + "/api/sendsms?numero=" + numero + "&message=" + message;
        //RestTemplate restTemplate = new RestTemplate();
        //  HttpHeaders headers = new HttpHeaders();
        //  headers.setContentType(MediaType.APPLICATION_JSON);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> map = new LinkedMultiValueMap();
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity(map, headers);
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, request, String.class, new Object[0]);
        System.out.println("" + resp);
        String retour = resp.getBody();
        JSONObject retJson = new JSONObject(retour);
        Message resultMessage = new Message();

        resultMessage.setCodeErr(retJson.getString("codeErr"));
        resultMessage.setMsg(retJson.getString("msg"));

        return resultMessage;
    }

    @Async("taskExecutor")
    public void sendMail(String email, String message) {
        log.info("Service d'envoi du MAIL");
        Notification not = new Notification();
        not.setContenu(message);
        not.setDestinataire(email);
        not.setTypeNotification(TypeNotification.EMAIL);
        not.setIsDelivred(Boolean.TRUE);
        notificationRepository.save(not);
        messageProducer.sendNotification(not, "sms");
        //   taskExecutor.execute(new MessageKafka(not, "sms"));
        //taskExecutor.execute(new MessageSender(numero, message));
    }

    @Async("taskExecutor")
    public void sendMessage(String numero, String message, String codeIndicatif) {
        Notification not = new Notification();
        not.setContenu(message);
        not.setDestinataire(numero);
        not.setTypeNotification(TypeNotification.SMS);
        not.setIsDelivred(Boolean.TRUE);
        not.setCodeIndicatif(codeIndicatif);
        not.setDateHeureEnvoie(LocalDateTime.now());
        notificationRepository.save(not);
        messageProducer.sendNotification(not, "sms");
        //taskExecutor.execute(new MessageKafka(not, "sms"));
    }

    @Async("taskExecutor")
    public void sendMessage(String numero, String message, String codeIndicatif, Long operationId) {
        Notification not = new Notification();
        not.setContenu(message);
        not.setOperationId(operationId);
        not.setDestinataire(numero);
        not.setTypeNotification(TypeNotification.SMS);
        not.setIsDelivred(Boolean.TRUE);
        not.setCodeIndicatif(codeIndicatif);
        not.setDateHeureEnvoie(LocalDateTime.now());
        notificationRepository.save(not);
        messageProducer.sendNotification(not, "sms");
        //taskExecutor.execute(new MessageKafka(not, "sms"));
    }

    @Async("taskExecutor")
    public void sendOtp(String numero, String message) {
        Notification not = new Notification();
        not.setContenu(message);
        not.setDestinataire(numero);
        not.setTypeNotification(TypeNotification.OTP);
        not.setIsDelivred(Boolean.TRUE);
        not.setCodeIndicatif(numero.substring(0, 3));
        not.setDateHeureEnvoie(LocalDateTime.now());
        messageProducer.sendNotification(not, "otp");
        //taskExecutor.execute(new MessageKafka(not, "sms"));
    }

    @Async("taskExecutor")
    public void sendError(String message) {
        messageProducer.sendMessage(message, "error");
        //taskExecutor.execute(new MessageSender(numero, message));
    }

    /**
     * Service permettant d'envoyer un sms
     *
     * @param numero
     * @param message
     * @return
     */
    @Async("taskExecutor")
    public ResponseEntity envoiMail(
            String email, String message, String titre) {

        String otrParametre = "mail=" + email + "|subject=" + titre + "|message=" + message;

        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + SERVICE_ENVOIE_SMS);
        ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
        param.put("codeFonction", "000236");
        param.put("otrParametre", otrParametre);
        ResponseEntity result = localRestClient.getRessources(tokeinfo, param, "POST");
        return result;
    }

    @Async("taskExecutor")
    public Message envoiSms(String telephone, String message,String typeNotification,String tenant) throws IOException, JAXBException {
        log.info("Service d'envoi du SMS");
        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = null;//localRestClient.sendTokenRequest();
        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        String codeFse = codeGateWay;
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres.add(addParametre(1L, "NUMEROTELEPHONE", telephone));
        parametres.add(addParametre(2L, "MESSAGE", message));
        parametres.add(addParametre(3L, "TYPENOTIFICATION", typeNotification));
        parametres.add(addParametre(4L, "TENANT", tenant));
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("ENVOISMS");
        parametreAppelDTO.setParam(parametres);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST");
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class);
        log.info("FIN Service d'envoi du SMS");
        if (resultMessage != null) {
            Message resultD = (Message) localRestClient.convert(resultMessage.getResponseEntity(), Message.class);
            return resultD;
        } else {
            throw new CustomParameterizedException("Le message n'a pas été envoyé!");
        }

    }

    public Message envoiMailXX(String email, String message, String titre, String codeFse) throws IOException, JAXBException {
        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();
        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres.add(addParametre(1L, "EMAIL", email));
        parametres.add(addParametre(1L, "TITRE", titre));
        parametres.add(addParametre(2L, "MESSAGE", message));
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("ENVOIMAIL");
        parametreAppelDTO.setParam(parametres);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST");
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class);
        if (resultMessage != null) {
            Message resultD = (Message) localRestClient.convert(resultMessage.getResponseEntity(), Message.class);
            return resultD;
        } else {
            throw new CustomParameterizedException("Le compte est introuvable!");
        }
    }

    /**
     * Service de comptabilisation des frais des commissions
     *
     * @param otrParametre
     * @return
     */
    public synchronized ResultMessage comptabiliseFraisComEmetteur(String otrParametre) throws IOException {
        log.info("Init comptabilisation %");
        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + "/api/boundary?codeFonction={codeFonction}&otrParametre={otrParametre}");
        ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
        String codeFonction = "000108";
        param.put("codeFonction", codeFonction);
        param.put("otrParametre", otrParametre);
        ResponseEntity result = localRestClient.getRessources(tokeinfo, param, "POST");
        log.info("Status code : " + result.getBody());

        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class);
        if (resultMessage != null) {
            return resultMessage;
        }
        return null;
    }

    /**
     * Service de repartion des commissions des acquereurs
     *
     * @param compteCredit
     * @param compteDebit
     * @param montant
     * @param fseSource
     * @return
     * @throws IOException
     */
    public ResponseEntity comptabiliseFraisComAcq(Compte compteCredit, Compte compteDebit, BigDecimal montant, UniteOrganisation fseSource) throws IOException {
        //   AuthTokenInfo tokeinfo = null;// restClient.sendTokenRequest();//token

        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();
        localRestClient.setRestServerUri(this.url + URL_PRESENTATION);
        ParametreDTO param = new ParametreDTO();
        String codeCredit = compteCredit.getValeurCompte() + "#" + compteCredit.getCompteBanque().getCodeAgence() + "#" + compteCredit.getCompteBanque().getCodeBanque();
        String codeDebit = compteDebit.getValeurCompte() + "#" + compteDebit.getCompteBanque().getCodeAgence() + "#" + compteDebit.getCompteBanque().getCodeBanque();
        //FSEsource est egal FSEdest
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres.add(addParametre(1L, "MONTANTOP", String.valueOf(montant)));
        parametres.add(addParametre(2L, "CODEFSEDEST", fseSource.getCodeFse()));//007 A recuperer à partir du compte reseau master
        parametres.add(addParametre(3L, "CODEFSESOURCE", fseSource.getCodeFse()));
        parametres.add(addParametre(4L, "CODEAGENCEDEST", compteCredit.getCompteBanque().getCodeAgence()));
        parametres.add(addParametre(5L, "CODEAGENCESOURCE", compteDebit.getCompteBanque().getCodeAgence()));
        parametres.add(addParametre(6L, compteDebit.getEtiquette(), codeDebit));
        parametres.add(addParametre(7L, compteCredit.getEtiquette(), codeCredit));
        param.setCodeService("007"); //code du type de service
        param.setCodeUniteOrganisation("01");//code UO
        param.setFse("0004"); //code FSE CBI
        param.setProprieteApi(ProprieteApi.FSESOURCE);
        param.setParam(parametres);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, param, "POST");
        return result;
    }

    public String setCardLessTransactionXXXX(String telephone,
            String montant,
            String reference,
            String entityCode) throws IOException, JAXBException {
        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();
        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres.add(addParametre(1L, "MONTANT", montant));
        parametres.add(addParametre(2L, "NUMEROCOMPTE", reference));
        parametres.add(addParametre(3L, "ENTITYCODE", entityCode));
        parametreAppelDTO.setCodeMethode("CARDLESS");
        parametreAppelDTO.setParam(parametres);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST");
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class);
        if (resultMessage != null) {
            StandardResponse resultD = (StandardResponse) localRestClient.convert(resultMessage.getResponseEntity(), StandardResponse.class);
            if (resultD.getResponseStatus() != null && resultD.getResponseStatus().getStatusCode().equals("0")) {
                return resultD.getResponseStatus().getMsg();
            }
        }
        return "";
    }

    public com.m2i.omnicanal.outils.restclient.model.Message cardLessTransactionStatus(
            String reference,
            String entityCode
    ) throws IOException {
        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();
        String s = null;
        String otrParametre = "entityCode=" + entityCode + "|reference=" + reference;
        localRestClient.setRestServerUri(this.url + "/api/boundary?codeFonction={codeFonction}&otrParametre={otrParametre}");
        ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
        param.put("codeFonction", "001402");
        param.put("otrParametre", otrParametre);
        ResponseEntity result = localRestClient.getRessources(tokeinfo, param, "POST");
        ResultMessage resMessage;
        resMessage = (ResultMessage) convert(result.getBody(), ResultMessage.class);
        com.m2i.omnicanal.outils.restclient.model.Message msg = new com.m2i.omnicanal.outils.restclient.model.Message();
        if (resMessage != null && resMessage.getErrCode() == 0) {
            StandardResponse response;
            response = (StandardResponse) convert(resMessage.getResponseEntity(), StandardResponse.class);
            if (response != null) {
                if (response.getResponseStatus() != null && response.getResponseStatus().getStatusCode() != null) {
                    msg.setCodeErr(response.getResponseStatus().getStatusCode());
                    msg.setMsg(response.getResponseStatus().getMsg());
                }
            } else {
                throw new CustomParameterizedException("Service indisponible");
            }

        } else {
            throw new CustomParameterizedException("Service indisponible");
        }
        return msg;

    }

    public synchronized StandardResponse setCardLessTransactionCrypted(
            String telephone,
            String montant,
            String reference,
            String entityCode
    ) throws IOException {
        //  AuthTokenInfo tokeinfo = null;// restClient.sendTokenRequest();//token
        String s = null;

        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();
        String otrParametre = "telephone=" + telephone + "|montant=" + montant + "|reference=" + reference + "|entityCode=" + entityCode;
        localRestClient.setRestServerUri(this.url + "/api/boundary?codeFonction={codeFonction}&otrParametre={otrParametre}");
        ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
        param.put("codeFonction", "001401");
        param.put("otrParametre", otrParametre);
        ResponseEntity result = localRestClient.getRessources(tokeinfo, param, "POST");
        ResultMessage resMessage;
        resMessage = (ResultMessage) convert(result.getBody(), ResultMessage.class);
        StandardResponse response = new StandardResponse();
        com.m2i.omnicanal.outils.restclient.model.Message msg = new com.m2i.omnicanal.outils.restclient.model.Message();
        if (resMessage != null && resMessage.getErrCode() == 0) {
            response = (StandardResponse) convert(resMessage.getResponseEntity(), StandardResponse.class);
        } else {
            ResponseStatus res = new ResponseStatus();
            res.setMsg("External application is down or an error occur");
            res.setStatusCode("-4");
            response.setResponseStatus(res);

        }
        return response;

    }

    public synchronized com.m2i.omnicanal.outils.restclient.model.Message setCardLessTransaction(
            String telephone,
            String montant,
            String reference,
            String entityCode
    ) throws IOException {
        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();
        //  AuthTokenInfo tokeinfo = null;//restClient.sendTokenRequest();//token
        String s = null;
        String otrParametre = "telephone=" + telephone + "|montant=" + montant + "|reference=" + reference + "|entityCode=" + entityCode;
        localRestClient.setRestServerUri(this.url + "/api/boundary?codeFonction={codeFonction}&otrParametre={otrParametre}");
        ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
        try {
            param.put("codeFonction", "001401");
            param.put("otrParametre", otrParametre);
            ResponseEntity result = localRestClient.getRessources(tokeinfo, param, "POST");
            ResultMessage resMessage;
            resMessage = (ResultMessage) convert(result.getBody(), ResultMessage.class);
            com.m2i.omnicanal.outils.restclient.model.Message msg = new com.m2i.omnicanal.outils.restclient.model.Message();
            if (resMessage != null && resMessage.getErrCode() == 0) {
                StandardResponse response;
                response = (StandardResponse) convert(resMessage.getResponseEntity(), StandardResponse.class);
                if (response != null) {
                    if (response.getResponseStatus() != null && response.getResponseStatus().getStatusCode() != null) {
                        if (response.getResponseStatus().getStatusCode().equals("0")) {
                            msg.setCodeErr(response.getResponseStatus().getStatusCode());
                            TripleDES tripleDES = new TripleDES();
                            String code = tripleDES.decrypt(response.getResponseStatus().getMsg(), this.keyCrypt);
                            if (code != null) {
                                String codeNonCrypte = code.replaceAll("F", "");
                                msg.setMsg(codeNonCrypte);
                                resMessage.setResponseEntity(msg);
                            }

                        } else {
                            throw new CustomSettlementException(response.getResponseStatus().getMsg());
                        }

                    }
                } else {
                    throw new CustomSettlementException("Service indisponible");
                }

            } else {
                throw new CustomSettlementException("Service indisponible");
            }
            return msg;
        } catch (Exception e) {
            throw new CustomSettlementException(e.getMessage());
        }

    }

    public com.m2i.omnicanal.outils.restclient.model.Message setCardLessTransaction(
            String sender,
            String receiver,
            String montant,
            String reference,
            String entityCode,
            String codeFse
    ) throws IOException {

        String s = null;
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres.add(addParametre(1L, "MONTANT", montant));
        parametres.add(addParametre(2L, "SENDER", sender));
        parametres.add(addParametre(3L, "RECEIVER", receiver));
        parametres.add(addParametre(4L, "REFERENCE", reference));
        parametres.add(addParametre(5L, "ENTITYCODE", entityCode));
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("GETCARDLESSTOKEN");
        parametreAppelDTO.setParam(parametres);
        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = null; // localRestClient.sendTokenRequest();
        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        Map<String, String> map = new HashMap<>();
        map.put("X-AppID", this.appId);
        ResponseEntity result = localRestClient.getRessourcesObjectAndHeader(tokeinfo, parametreAppelDTO, "POST", map);
        ResultMessage resMessage;
        resMessage = (ResultMessage) convert(result.getBody(), ResultMessage.class);
        com.m2i.omnicanal.outils.restclient.model.Message msg = new com.m2i.omnicanal.outils.restclient.model.Message();
        if (resMessage != null && resMessage.getErrCode() == 0) {
            StandardResponse response;
            response = (StandardResponse) convert(resMessage.getResponseEntity(), StandardResponse.class);
            if (response != null) {
                if (response.getResponseStatus() != null && response.getResponseStatus().getStatusCode() != null) {
                    if (response.getResponseStatus().getStatusCode().equals("0")) {
                        msg.setCodeErr(response.getResponseStatus().getStatusCode());
                        String code = response.getResponseStatus().getMsg();
                        if (code != null) {
                            String codeNonCrypte = code.replaceAll("F", "");
                            msg.setMsg(codeNonCrypte);
                            resMessage.setResponseEntity(msg);
                        }

                    } else {
                        msg.setCodeErr("-1");
                        msg.setMsg(response.getResponseStatus().getMsg());

                    }

                }
            } else {
                msg.setCodeErr("-4");
                msg.setMsg("Service indisponible");
                //  throw new CustomSettlementException("Service indisponible");
            }

        } else {
            msg.setCodeErr("-4");
            msg.setMsg("Service indisponible");
        }
        return msg;

    }

    /**
     * Service de comptabilisation à travers l'omnicanal
     *
     * @param montantFraisAcq
     * @param montantFraisEmt
     * @param montantOp
     * @param codeFseDest
     * @param codeFseSource
     * @param codeAgenceDest
     * @param codeAgenceSrc
     * @param etiquetteSrc
     * @param compteSrc
     * @param etiquetteDest
     * @param compteDest
     * @param codeService
     * @return
     * @throws IOException
     */
    public Result sendRequestComptabilisation(String montantFraisAcq, String montantFraisEmt, String montantOp, String codeFseDest,
            String codeFseSource, String codeAgenceDest, String codeAgenceSrc, String etiquetteSrc,
            String compteSrc, String etiquetteDest, String compteDest, String codeService, String reference) throws IOException {
        BigDecimal bigDecimalCurrency = new BigDecimal(montantOp);
        if (Util.isBiggerThan(bigDecimalCurrency, BigDecimal.ZERO)) {
            RestClient localRestClient = setParam();
            AuthTokenInfo tokeinfo = null;//localRestClient.sendTokenRequest();
            localRestClient.setRestServerUri(this.url + URL_PRESENTATION);
            List<ParametreUserDTO> parametres = new ArrayList<>();
            parametres.add(addParametre(2L, "MONTANTFRAISACQ", montantFraisAcq));
            parametres.add(addParametre(3L, "MONTANTFRAISEMT", montantFraisEmt));
            parametres.add(addParametre(4L, "MONTANTHT", montantOp));
            parametres.add(addParametre(5L, "CODEFSEDEST", codeFseDest));//A recuperer à partir du compte reseau master
            parametres.add(addParametre(6L, "CODEFSESOURCE", codeFseSource));
            parametres.add(addParametre(8L, "CODEAGENCEDEST", codeAgenceDest));
            parametres.add(addParametre(7L, "CODEAGENCESOURCE", codeAgenceSrc));
            parametres.add(addParametre(8L, "REFERENCE", codeAgenceSrc));
//            parametres.add(addParametre(17L, "CANAL", getCanal(operation)));
            if (etiquetteSrc != null && compteSrc != null) {
                parametres.add(addParametre(7L, etiquetteSrc, compteSrc));//Valeur#CodeAgence#CodeBanque
            }
            if (etiquetteDest != null && compteDest != null) {
                parametres.add(addParametre(8L, etiquetteDest, compteDest));
            }
            ParametreDTO param = setParams(parametres, codeService, reference);
            String message = showLogs(parametres);

            param.setCanal(this.appId);
            ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, param, "POST");
            if (result != null) {
                Result resultOp = localRestClient.convert(result.getBody(), Result.class);
                resultOp.setDesc(message);
                return resultOp;
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     * Service permettant de faire un virement sans frais
     *
     * @param compteSrc
     * @param compteDest
     * @param montantOp
     * @return
     * @throws IOException
     */
    public ResponseEntity virementSimple(Compte compteSrc, Compte compteDest, BigDecimal montantOp, String reference, Operation operation) throws IOException {
        String codeFseSrc = "";
        String codeFseDest = "";
        String codeAgenceSrc = "";
        String codeAgenceDest = "";
        String codeDebit = "";
        String codeCredit = "";
        log.info("DEBUT DE L'APPEL VERS L'omnicanal " + reference);
        if (compteSrc != null && compteDest != null) {
            if (compteSrc.getCompteBanque() != null) {
                codeAgenceSrc = compteSrc.getCompteBanque().getCodeAgence();
                codeFseSrc = compteSrc.getCompteBanque().getCodeFse();
                codeDebit = compteSrc.getValeurCompte() + "#" + compteSrc.getCompteBanque().getCodeAgence() + "#" + compteSrc.getCompteBanque().getCodeBanque();
            } else if (compteSrc.getCompteAutre() != null) {
                codeAgenceSrc = "0";
                codeDebit = compteSrc.getValeurCompte();
                codeFseSrc = compteSrc.getCompteAutre().getCodeFse();
            }
            if (compteDest.getCompteBanque() != null) {
                codeFseDest = compteDest.getCompteBanque().getCodeFse();
                codeAgenceDest = compteDest.getCompteBanque().getCodeAgence();
                codeCredit = compteDest.getValeurCompte() + "#" + compteDest.getCompteBanque().getCodeAgence() + "#" + compteDest.getCompteBanque().getCodeBanque();
            } else if (compteDest.getCompteAutre() != null) {
                codeFseDest = compteDest.getCompteAutre().getCodeFse();
                codeAgenceDest = "0";
                codeCredit = compteDest.getValeurCompte();

            }
            String etiquetteSrc = compteSrc.getEtiquette();
            String etiquetteDest = compteDest.getEtiquette();
            String codeService = ApiService.CODE_SERVICE_VIREMENT;
            if (Util.isBiggerThan(montantOp, BigDecimal.ZERO)) {
                RestClient localRestClient = setParam();
                AuthTokenInfo tokeinfo =null; // localRestClient.sendTokenRequest();
                localRestClient.setRestServerUri(this.url + URL_PRESENTATION);
                List<ParametreUserDTO> parametres = new ArrayList<>();
                parametres.add(addParametre(4L, "MONTANTHT", montantOp.toString()));
                parametres.add(addParametre(5L, "CODEFSEDEST", codeFseDest));//A recuperer à partir du compte reseau master
                parametres.add(addParametre(6L, "CODEFSESOURCE", codeFseSrc));
                parametres.add(addParametre(8L, "CODEAGENCEDEST", codeAgenceDest));
                parametres.add(addParametre(7L, "CODEAGENCESOURCE", codeAgenceSrc));
                parametres.add(addParametre(17L, "CANAL", getCanal(operation, this.appId)));
                if (etiquetteSrc != null && compteSrc != null) {
                    parametres.add(addParametre(7L, "CPTECLIENT", codeDebit));//Valeur#CodeAgence#CodeBanque
                }
                if (etiquetteDest != null && compteDest != null) {
                    parametres.add(addParametre(8L, "CPTEBENEFICIAIRE", codeCredit));
                }
                ParametreDTO param = setParams(parametres, codeService, reference);
                param.setCanal(this.appId);
                String message = showLogs(parametres);
                //   paramMap.put("X-AppID", this.appId);
                ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, param, "POST");
                log.info("FIN DE L'APPEL VERS L'omnicanal AVEC SUCCES " + reference);
                return result;
                /*  if (result != null) {
                Result resultOp = restClient.convert(result.getBody(), Result.class);
                return resultOp;
            } else {
                return null;
            }*/
            }

        } else {
            log.info("FIN DE L'APPEL VERS L'omnicanal avec ERREUR " + reference);
            return null;
        }

        return null;
    }

    public Result virementSimpleEx(Compte compteSrc, Compte compteDest, BigDecimal montantOp, String reference, String description, String donneurOrdre, String beneficiaire, Operation operation) throws IOException {
        String codeFseSrc = "";
        String codeFseDest = "";
        String codeAgenceSrc = "";
        String codeAgenceDest = "";
        String compteDebit = "";
        String compteCredit = "";
        log.info("DEBUT DE L'APPEL VERS L'omnicanal " + reference);
        if (compteSrc != null && compteDest != null) {
            if (compteSrc.getCompteBanque() != null) {
                codeFseSrc = compteSrc.getCompteBanque().getCodeFse();
                codeAgenceSrc = compteSrc.getCompteBanque().getCodeAgence();
                compteDebit = compteSrc.getValeurCompte() + "#" + compteSrc.getCompteBanque().getCodeAgence() + "#" + compteSrc.getCompteBanque().getCodeBanque();
            } else {
                codeFseSrc = compteSrc.getCompteAutre().getCodeFse();
                codeAgenceSrc = "0";
                compteDebit = compteSrc.getValeurCompte();
            }
            if (compteDest.getCompteBanque() != null) {
                codeFseDest = compteDest.getCompteBanque().getCodeFse();
                codeAgenceDest = compteDest.getCompteBanque().getCodeAgence();
                compteCredit = compteDest.getValeurCompte() + "#" + compteDest.getCompteBanque().getCodeAgence() + "#" + compteDest.getCompteBanque().getCodeBanque();
            } else {
                codeFseDest = compteDest.getCompteAutre().getCodeFse();
                codeAgenceDest = "0";
                compteCredit = compteDest.getValeurCompte();
            }
            String etiquetteSrc = compteSrc.getEtiquette();
            String etiquetteDest = compteDest.getEtiquette();
            String codeService = ApiService.CODE_SERVICE_ACCOUNT_ACCOUNT;
            if (Util.isBiggerThan(montantOp, BigDecimal.ZERO)) {
                RestClient localRestClient = setParam();
                AuthTokenInfo tokeinfo = null;// localRestClient.sendTokenRequest();
                localRestClient.setRestServerUri(this.url + URL_PRESENTATION);
                List<ParametreUserDTO> parametres = new ArrayList<>();
                parametres.add(addParametre(4L, "MONTANTHT", montantOp.toString()));
                parametres.add(addParametre(5L, "CODEFSEDEST", codeFseDest));//A recuperer à partir du compte reseau master
                parametres.add(addParametre(6L, "CODEFSESOURCE", codeFseSrc));
                parametres.add(addParametre(8L, "CODEAGENCEDEST", codeAgenceDest));
                parametres.add(addParametre(7L, "CODEAGENCESOURCE", codeAgenceSrc));
                parametres.add(addParametre(8L, "REFERENCE", reference));
                parametres.add(addParametre(9L, "DESCRIPTION", description));
                parametres.add(addParametre(10L, "DONNEURORDRE", donneurOrdre));
                parametres.add(addParametre(11L, "BENEFICIAIRE", beneficiaire));
                parametres.add(addParametre(7L, "CPTECLIENT", compteDebit));//Valeur#CodeAgence#CodeBanque
                parametres.add(addParametre(8L, "CPTEBENEFICIAIRE", compteCredit));
                parametres.add(addParametre(17L, "CANAL", getCanal(operation, this.appId)));
                parametres.add(addParametre(11L, CAS, CAS_NORMAL));
                ParametreDTO param = setParams(parametres, codeService, reference);
                String message = showLogs(parametres);
                //   paramMap.put("X-AppID", this.appId);
                param.setCanal(this.appId);
                ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, param, "POST");
                if (result != null) {
                    log.info("FIN DE L'APPEL VERS L'omnicanal AVEC SUCCES " + reference);
                    Result resultOp = localRestClient.convert(result.getBody(), Result.class);
                    return resultOp;
                } else {
                    log.info("FIN DE L'APPEL VERS L'omnicanal avec ERREUR " + reference);
                    Result resultOp = new Result();
                    resultOp.setCodeErr("-1");
                    resultOp.setMsg("ERREUR LORS DU VIREMENT");
                    return resultOp;

                }
            }
        } else {
            log.info("FIN DE L'APPEL VERS L'omnicanal avec ERREUR " + reference);
            Result resultOp = new Result();
            resultOp.setCodeErr("-1");
            resultOp.setMsg("ERREUR LORS DU VIREMENT");
            return resultOp;

        }

        return null;
    }

    public synchronized void sendAck(String codeOperation) throws IOException {

        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + SERVICE_ACK);
        ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
        param.put("codeOperation", codeOperation);
        //   ResponseEntity result = this.getRessources(tokeinfo, param, "GET");
        ResponseEntity result = localRestClient.getRessources(tokeinfo, param, "GET");

    }

    public Message checkFunction(String codeFse, String codeMethode) throws IOException {

        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + SERVICE_FUNCTION);//"/api/checkFonction?codeMethode="+codeMethode+"&codeFSE="+codeFse);
        ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
        param.put("codeFSE", codeFse);
        param.put("codeMethode", codeMethode);
        ResponseEntity result = localRestClient.getRessources(tokeinfo, param, "POST");
        if (result != null) {
            Message resultOp = localRestClient.convert(result.getBody(), Message.class);
            return resultOp;
        } else {
            Message resultOp = new Message();
            resultOp.setCodeErr("-1");
            resultOp.setMsg("ERREUR LORS DU VIREMENT");
            return resultOp;
        }

    }

    /**
     * Service permettant de faire un virement sans frais
     *
     * @param compteSrc
     * @param compteDest
     * @param montantOp
     * @param description
     * @return
     * @throws IOException
     */
    public synchronized Result virementSimple(Compte compteSrc, Compte compteDest, BigDecimal montantOp, String description, String reference, Operation operation) throws IOException {
        String codeFseSrc = "";
        String codeFseDest = "";
        String codeAgenceSrc = "";
        String codeAgenceDest = "";
        String codeDebit = "";
        String codeCredit = "";
        try {
            log.info("DEBUT DE L'APPEL VERS L'omnicanal " + reference);
            if (compteSrc != null && compteDest != null) {
                if (compteSrc.getCompteBanque() != null) {
                    codeAgenceSrc = compteSrc.getCompteBanque().getCodeAgence();
                    codeFseSrc = compteSrc.getCompteBanque().getCodeFse();
                    codeDebit = compteSrc.getValeurCompte() + "#" + compteSrc.getCompteBanque().getCodeAgence() + "#" + compteSrc.getCompteBanque().getCodeBanque();
                } else if (compteSrc.getCompteAutre() != null) {
                    codeAgenceSrc = "0";
                    codeDebit = compteSrc.getValeurCompte();
                    codeFseSrc = compteSrc.getCompteAutre().getCodeFse();
                }
                if (compteDest.getCompteBanque() != null) {
                    codeFseDest = compteDest.getCompteBanque().getCodeFse();
                    codeAgenceDest = compteDest.getCompteBanque().getCodeAgence();
                    codeCredit = compteDest.getValeurCompte() + "#" + compteDest.getCompteBanque().getCodeAgence() + "#" + compteDest.getCompteBanque().getCodeBanque();
                } else if (compteDest.getCompteAutre() != null) {
                    codeFseDest = compteDest.getCompteAutre().getCodeFse();
                    codeAgenceDest = "0";
                    codeCredit = compteDest.getValeurCompte();

                }

                String etiquetteSrc = compteSrc.getEtiquette();
                String etiquetteDest = compteDest.getEtiquette();
                String codeService = ApiService.CODE_SERVICE_VIREMENT;
                if (Util.isBiggerThan(montantOp, BigDecimal.ZERO)) {
                    RestClient localRestClient = setParam();
                    AuthTokenInfo tokeinfo =null;// localRestClient.sendTokenRequest();
                    localRestClient.setRestServerUri(this.url + URL_PRESENTATION);
                    List<ParametreUserDTO> parametres = new ArrayList<>();
                    parametres.add(addParametre(4L, "MONTANTHT", montantOp.setScale(0, BigDecimal.ROUND_HALF_UP).toString()));
                    parametres.add(addParametre(5L, "CODEFSEDEST", codeFseDest));//A recuperer à partir du compte reseau master
                    parametres.add(addParametre(6L, "CODEFSESOURCE", codeFseSrc));
                    parametres.add(addParametre(8L, "CODEAGENCEDEST", codeAgenceDest));
                    parametres.add(addParametre(7L, "CODEAGENCESOURCE", codeAgenceSrc));
                    parametres.add(addParametre(16L, "DESCRIPTION", description));
                    parametres.add(addParametre(16L, "REFERENCE", reference));
                    parametres.add(addParametre(17L, "CANAL", getCanal(operation, this.appId)));
                    if (etiquetteSrc != null && compteSrc != null) {
                        parametres.add(addParametre(7L, "CPTECLIENT", codeDebit));//Valeur#CodeAgence#CodeBanque
                    }
                    if (etiquetteDest != null && compteDest != null) {
                        parametres.add(addParametre(8L, "CPTEBENEFICIAIRE", codeCredit));
                    }
                    parametres.add(addParametre(11L, CAS, CAS_NORMAL));
                    ParametreDTO param = setParams(parametres, codeService, reference);
                    String message = showLogs(parametres);
                    // paramMap.put("X-AppID", this.appId);
                    param.setCanal(this.appId);
                    ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, param, "POST");
                    if (result != null) {
                        Result resultOp = localRestClient.convert(result.getBody(), Result.class);
                        resultOp.setDesc(message);
                        log.info("FIN DE L'APPEL VERS L'omnicanal " + reference + " " + resultOp);
                        return resultOp;
                    } else {
                        log.info("FIN DE L'APPEL VERS L'omnicanal avec ERREUR " + reference);
                        Result resultOp = new Result();
                        resultOp.setCodeErr("-1");
                        resultOp.setMsg("ERREUR LORS DU VIREMENT");
                        return resultOp;

                    }

                }

            } else {
                log.info("FIN DE L'APPEL VERS L'omnicanal avec ERREUR " + reference);
                Result resultOp = new Result();
                resultOp.setCodeErr("-1");
                resultOp.setMsg("Le compte debit ou credit  est introuvable");
                return resultOp;
            }

        } catch (Exception e) {
            log.info("FIN DE L'APPEL VERS L'omnicanal avec ERREUR " + reference);
            Result resultOp = new Result();
            resultOp.setCodeErr("-1");
            resultOp.setMsg(e.getMessage());
            return resultOp;
        }

        return null;
    }

    public <T extends Object> T convert(Object body, Class<T> resultClass) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        T result = null;
        try {
            String jsonInString = mapper.writeValueAsString(body);
            result = (T) (Object) mapper.readValue(jsonInString, resultClass);
        } catch (IOException e) {

        }
        return result;
    }

    /**
     * Readapter les comptes de type Autre
     *
     * @param compte
     * @return
     */
    private CompteBanque convertCompte(Compte compte) {
        log.info("Entree de la fonction convertcompte");
        Compte cpt = null;
        if (compte != null) {
            if (compte.getId() != null) {

                if (compte.getCategorieCompte().equals(CategorieCompte.AUTRE)) {
                    log.info("Type de compte AUTRE");
                    cpt = compteService.findCompteAutreByOne(compte.getId());

                } else if (compte.getCategorieCompte().equals(CategorieCompte.BANQUE)) {
                    log.info("Type de compte BANQUE");
                    cpt = compteService.findCompteBanqueByOne(compte.getId());
                } else if (compte.getCategorieCompte().equals(CategorieCompte.CARTE)) {
                    log.info("Type de compte CARTE");
                    cpt = compteService.findCompteCarteByOne(compte.getId());
                }

            }
            if (cpt != null) {
                if (cpt.getCompteBanque() != null) {
                    log.info("Compte Banque trouvée" + cpt.getNumeroCourt());
                    return compte.getCompteBanque();
                } else if (cpt.getCarte() != null && compte.getCategorieCompte().equals(CategorieCompte.CARTE)) {
                    CompteBanque compteBanque = new CompteBanque();
                    compteBanque.codeAgence(cpt.getCarte().getLast4Digit());
                    compteBanque.codeBanque("");
                    return compteBanque;
                } else if (cpt.getCompteAutre() != null) {
                    CompteBanque compteBanque = new CompteBanque();
                    compteBanque.codeAgence("");
                    compteBanque.codeBanque("");
                    return compteBanque;

                }

            }

        }
        log.info("Compte Banque non trouvée");
        log.info("fin de la fonction convertcompte");
        return null;
    }

    /**
     * Recupere le compte d'un fournisseur de service
     *
     * @param codeFse
     * @return
     * @throws IOException
     */
    public ResponseEntity getCompteOfFse(String codeFse, String etiquette) throws IOException {
        log.info("service de récupération du compte d'un fournisseur de service");
        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();
        localRestClient.setRestServerUri(this.url + SERVIE_FSE_COMPTE);
        ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
        param.put("etiquette", etiquette);
        param.put("codefse", codeFse);
        ResponseEntity result = localRestClient.getRessources(tokeinfo, param, "POST");
        return result;
    }

    /**
     * Service retournant le solde d'un compte en fonction du fournisseur de
     * service
     *
     * @param numeroCompte
     * @param agence
     * @param codeFse
     * @return
     * @throws IOException
     * @throws JAXBException
     */
    public CustomerInformation getInfoClientCompte(String numeroCompte, String agence, String codeFse) throws IOException, JAXBException {
        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = null;//localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres.add(addParametre(1L, "AGENCE", agence));
        parametres.add(addParametre(2L, "NUMEROCOMPTE", numeroCompte));
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("INFOCLIENT");
        parametreAppelDTO.setParam(parametres);
        paramMap.put("X-AppID", this.appId);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST", this.paramMap);
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class
        );
        log.info("resultMessage" + resultMessage);

        if (resultMessage != null) {
            Result resultD = (Result) localRestClient.convert(resultMessage.getResponseEntity(), Result.class
            );
            log.info("resultD======" + resultMessage.getResponseEntity());
            if (resultD != null && resultD.getCodeErr() != null && resultD.getCodeErr().equals("0")) {
                //      if (resultD.getText()==null){
                //  MessageClient resultD = (MessageClient) restClient.convert(resultMessage.getResponseEntity(), MessageClient.class);
                //    }
                log.info("GET TEST======" + resultD.getText());

                return Util.unmarshall(resultD.getText(), CustomerInformation.class
                );
            } else {
                CustomerInformation c = new CustomerInformation();

                return c;
            }
        } else {
            throw new CustomParameterizedException("Le compte est introuvable!");
        }
    }

    private List<ParametreUserDTO> getParametres(List<MParametre> listparam) throws JAXBException {
        log.debug("parametres:" + listparam);
        HashMap<String, Object> mapParametre = new HashMap<>();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        MParametre elementParam = null;
        Long i = 1L;
        MlistParametre mlistParametre = new MlistParametre();
        mlistParametre.setParametre(listparam);
        String str = (String) Util.marshall(mlistParametre, String.class
        );
        log.debug("str:" + str);
        parametres.add(addParametre(0L, "SPARAM", str));
        Iterator itParam = listparam.iterator();
        i = 1L;
        while (itParam.hasNext()) {
            elementParam = (MParametre) itParam.next();
            //   mapParametre.put(elementParam.getCodeParametre(), elementParam.getValeur());
            parametres.add(addParametre(i, elementParam.getCodeParametre(), elementParam.getValeur()));
            i = i + 1L;
        }
        log.debug("parametres:" + parametres);
        return parametres;
    }

    /**
     * permet de recuperer des donneé divers
     *
     * @param listparam
     * @param codeFse
     * @return
     * @throws IOException
     * @throws JAXBException
     */
    public List<MParametre> getData(List<MParametre> listparam, String codeFse) throws IOException, JAXBException {
        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = null;// localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        //  codeFse = "0013";
        parametres = getParametres(listparam);
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("GETDATA");
        parametreAppelDTO.setParam(parametres);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST");
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class
        );
        List<MParametre> classP = new ArrayList();
        String strReturn = "";
        String strReturnD = "";
        if (resultMessage.getErrCode() == 0) {
            List<MParametre> resultD = (List<MParametre>) localRestClient.convert(resultMessage.getResponseEntity(), classP.getClass());
            return resultD;
        } else {
            return null;
        }

    }

    /**
     * *
     *
     * @param operation
     * @param fseSrc
     * @param fseDest
     * @param compteClient
     * @param normal
     * @param avecFrais
     * @param service
     * @return
     * @throws IOException
     */
    public Result sendRequestComptabilisationDestruction(Operation operation, boolean creation) throws IOException {
        RestClient restClient = setParam();
        log.info("Service de comptabilisation d'une opération de Account Cash");
        AuthTokenInfo tokeinfo = null;
        restClient.sendTokenRequest();//token
        restClient.setRestServerUri(this.url + URL_PRESENTATION);
        String codeOfCpteOperation = "";
        //  UniteOrganisation fseDestAgenceSiege = uniteOrganisationService.getUoAgenceSiege(fseDest);//L'agence siège de l'organisation initiateur du MAD
        String codeService;
        if (creation) {
            codeService = CODE_SERVICE_CREATION_MONNAIE;
        } else {
            codeService = CODE_SERVICE_DESTRUCTION_MONNAIE;
        }
        String codeFse;

        UniteOrganisation uo = uniteOrganisationService.findOneById(operation.getPointVente().getUniteOrganisation().getId());

        UniteOrganisation uoAgenceSiege = uniteOrganisationService.getFseAgenceSiege(uo);
        if (uoAgenceSiege != null) {
            UniteOrganisation uoFse = uniteOrganisationService.findOneById(uoAgenceSiege.getUoFseId());
            ParametreApp paramcodeFse = parametreAppRepository.findByTypeAndNomAndCodeFse("BANQUE_FSE", uoFse.getCodeFse());
            /**
             * Le paramètre est positionné
             */
            if (paramcodeFse != null) {
                codeFse = paramcodeFse.getValeur();
                List<ParametreUserDTO> parametres = new ArrayList<>();
                parametres.add(addParametre(1L, MontantsAComptabiliser.MONTANT.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTHT, operation).toBigInteger())));
                parametres.add(addParametre(2L, MontantsAComptabiliser.MONTANTHT.toString(), String.valueOf(instructionComptableService.getMontantComptabilisation(MontantsAComptabiliser.MONTANTHT, operation).toBigInteger())));
                parametres.add(addParametre(5L, "CODEFSEDEST", codeFse));
                parametres.add(addParametre(6L, "CODEFSESOURCE", codeFse));
                parametres.add(addParametre(16L, "DESCRIPTION", getDescription(operation)));
                parametres.add(addParametre(11L, CAS, CAS_NORMAL));
                parametres.add(addParametre(13L, "REFERENCE", operation.getCodeOperation()));
                parametres.add(addParametre(14L, "DONNEURORDRE", "BANQUE"));
                parametres.add(addParametre(15L, "BENEFICIAIRE", "BANQUE"));
                parametres.add(addParametre(17L, "CANAL", getCanal(operation, this.appId)));
                ParametreDTO param;
                param = setParams(parametres, codeService, operation.getCodeOperation());
                String message = showLogs(parametres);
                param.setCanal(this.appId);
                ResponseEntity result = restClient.getRessourcesObject(tokeinfo, param, "POST");
                if (result != null) {
                    Result resultOp = restClient.convert(result.getBody(), Result.class);
                    if (resultOp != null) {
                        resultOp.setDesc(message);
                    }
                    return resultOp;
                } else {
                    return null;
                }
            }
        }

        return null;
    }

    /**
     * Retourne le montant dune facture
     *
     * @param listparam
     * @param codeFse
     * @return
     * @throws IOException
     * @throws JAXBException
     */
    public  ElementFacture getMontant(List<MParametre> listparam, String codeFse) throws IOException, JAXBException {

        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        //  codeFse = "0013";
        parametres = getParametres(listparam);
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("GETMONTANT");
        parametreAppelDTO.setParam(parametres);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST");
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class
        );

        if (resultMessage != null) {
            Message resultD = (Message) localRestClient.convert(resultMessage.getResponseEntity(), Message.class
            );

            if (resultD != null && resultD.getCodeErr().equals("0")) {
                return Util.unmarshall(resultD.getText(), ElementFacture.class
                );
            } else {
                return null;
            }
        }
        return null;
    }

    /**
     *
     * retourne une collection de facture
     */
    public CollectionFacture setPaymentFacture(List<MParametre> listparam, String codeFse) throws IOException, JAXBException {

        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo =null;// localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres = getParametres(listparam);

        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("CONSULTATIONFACTURE");
        parametreAppelDTO.setParam(parametres);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST");
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class
        );

        if (resultMessage != null) {
            Message resultD = (Message) localRestClient.convert(resultMessage.getResponseEntity(), Message.class
            );

            if (resultD.getCodeErr().equals("0")) {
                return Util.unmarshall(resultD.getText(), CollectionFacture.class
                );
            } else {

                return null;
            }
        } else {
            throw new CustomParameterizedException("erreur!");
        }
    }

    /**
     * Recupération des information d'un client
     *
     * @param listparam
     * @param codeFse
     * @return
     * @throws IOException
     * @throws JAXBException
     */
    public List<MParametre> getInfos(List<MParametre> listparam, String codeFse) throws IOException, JAXBException {

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonStr = ow.writeValueAsString(listparam);

        System.out.println(jsonStr);
        AuthTokenInfo tokeinfo = null;// restClient.sendTokenRequest();

        RestClient localRestClient = setParam();

        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);

        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres = getParametres(listparam);

        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("GETINFOS");
        parametreAppelDTO.setParam(parametres);
        paramMap.put("X-AppID", this.appId);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST", this.paramMap);
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class
        );
        List<MParametre> classP = new ArrayList();

        // to remove
        //for (MParametre param : listparam) {
          //  log.info("--- resultmessage: " + resultMessage.getMsg());
            //log.info("--- errcode: " + resultMessage.getErrCode());
      //  }

        if (resultMessage.getErrCode() == 0) {
            List<MParametre> resultD = (List<MParametre>) localRestClient.convert(resultMessage.getResponseEntity(), classP.getClass());
            return resultD;
        } else {
            return null;
        }

    }

    /**
     * Recupération des information d'un client
     *
     * @param listparam
     * @param codeFse
     * @return
     * @throws IOException
     * @throws JAXBException
     */
    public Message traiteData(List<MParametre> listparam, String codeFse, String action) throws IOException, JAXBException {
        AuthTokenInfo tokeinfo = null;// restClient.sendTokenRequest();
        RestClient localRestClient = setParam();
        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        MParametre paramAction = new MParametre();
        paramAction.setCodeParametre("ACTION");
        paramAction.setValeur(action);
        listparam.add(paramAction);
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres = getParametres(listparam);
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("TRAITEDATA");
        parametreAppelDTO.setParam(parametres);
        paramMap.put("X-AppID", this.appId);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST", this.paramMap);
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class);
        List<MParametre> classP = new ArrayList();
        if (resultMessage.getErrCode() == 0) {
            Message resultD = (Message) localRestClient.convert(resultMessage.getResponseEntity(), Message.class);
            return resultD;
        } else {
            return null;
        }

    }

    public Message genereOtp(List<MParametre> listparam, String codeFse) throws IOException, JAXBException {
        AuthTokenInfo tokeinfo = null;// restClient.sendTokenRequest();
        RestClient localRestClient = setParam();
        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        MParametre paramAction = new MParametre();
        paramAction.setCodeParametre("ACTION");
        paramAction.setValeur("GENERER");
        listparam.add(paramAction);
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres = getParametres(listparam);
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("TRAITEOTP");
        parametreAppelDTO.setParam(parametres);
        paramMap.put("X-AppID", this.appId);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST", this.paramMap);
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class);
        List<MParametre> classP = new ArrayList();
        if (resultMessage.getErrCode() == 0) {
            Message resultD = (Message) localRestClient.convert(resultMessage.getResponseEntity(), Message.class);
            return resultD;
        } else {
            return null;
        }

    }

    public Message verifOTP(List<MParametre> listparam, String codeFse) throws IOException, JAXBException {
        AuthTokenInfo tokeinfo = null;// restClient.sendTokenRequest();
        RestClient localRestClient = setParam();
        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        MParametre paramAction = new MParametre();
        paramAction.setCodeParametre("ACTION");
        paramAction.setValeur("VERIFIER");
        listparam.add(paramAction);
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres = getParametres(listparam);
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("TRAITEOTP");
        parametreAppelDTO.setParam(parametres);
        paramMap.put("X-AppID", this.appId);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST", this.paramMap);
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class);
        List<MParametre> classP = new ArrayList();
        if (resultMessage.getErrCode() == 0) {
            Message resultD = (Message) localRestClient.convert(resultMessage.getResponseEntity(), Message.class);
            return resultD;
        } else {
            return null;
        }

    }

    public MDataTransaction getInfosDataTransaction(List<MParametre> listparam, String codeFse) throws IOException, JAXBException {
        AuthTokenInfo tokeinfo = null;// restClient.sendTokenRequest();

        RestClient localRestClient = setParam();
        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);

        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres = getParametres(listparam);
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("GETDATATRANSACTION");
        parametreAppelDTO.setParam(parametres);
        paramMap.put("X-AppID", this.appId);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST", this.paramMap);
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class);
        MDataTransaction classDataTransaction = new MDataTransaction();
        MDataTransaction resultD;
        // to remove

        if (resultMessage.getErrCode() == 0) {
            classDataTransaction = (MDataTransaction) localRestClient.convert(resultMessage.getResponseEntity(), MDataTransaction.class);
            return classDataTransaction;
        } else {
            return null;
        }

    }

    public GrData getListeFacturier(List<MParametre> listparam, String codeFse) throws IOException, JAXBException {
        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres = getParametres(listparam);
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("LISTEFACTURIER");
        parametreAppelDTO.setParam(parametres);
        paramMap.put("X-AppID", this.appId);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST", this.paramMap);
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class);
        GrData classDataTransaction = new GrData();
        MDataTransaction resultD;
        // to remove
        for (MParametre param : listparam) {
            log.info("--- resultmessage: " + resultMessage.getMsg());
            log.info("--- errcode: " + resultMessage.getErrCode());
        }
        if (resultMessage.getErrCode() == 0) {
            classDataTransaction = (GrData) localRestClient.convert(resultMessage.getResponseEntity(), GrData.class);
            return classDataTransaction;
        } else {
            return null;
        }

    }

    public synchronized List<MParametre> getPrint(List<MParametre> listparam, String codeFse) throws IOException, JAXBException {
        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres = getParametres(listparam);
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("PRINT");
        parametreAppelDTO.setParam(parametres);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST");
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class
        );
        List<MParametre> classP = new ArrayList();

        // to remove
        for (MParametre param : listparam) {
            log.info("--- resultmessage: " + resultMessage.getMsg());
            log.info("--- errcode: " + resultMessage.getErrCode());
        }

        if (resultMessage.getErrCode() == 0) {
            List<MParametre> resultD = (List<MParametre>) localRestClient.convert(resultMessage.getResponseEntity(), classP.getClass());
            return resultD;
        } else {
            return null;
        }

    }

    /**
     * Cette fonction permet d'envoyer par maiil un fichier de reconciliation
     *
     * @param listparam
     * @param codeFse
     * @throws IOException
     * @throws JAXBException
     */
    @Async("taskExecutor")
    public synchronized void sendFileMail(List<MParametre> listparam, String codeFse) throws IOException, JAXBException {

        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres = getParametres(listparam);
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("SENDFILEMAIL");
        parametreAppelDTO.setParam(parametres);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST");

    }

    /**
     * Cette fonction permet d'envoyer par FTP un fichier de reconciliation
     *
     * @param listparam
     * @param codeFse
     * @throws IOException
     * @throws JAXBException
     */
    @Async("taskExecutor")
    public synchronized void sendFileFTP(List<MParametre> listparam, String codeFse) throws IOException, JAXBException {

        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres = getParametres(listparam);
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("SENDFILEFTP");
        parametreAppelDTO.setParam(parametres);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST");

    }

    /**
     * Faire le settlement d'une transaction
     *
     * @param listparam
     * @param codeFse
     * @return
     * @throws IOException
     * @throws JAXBException
     */
    public synchronized SettlementResponse settlement(List<MParametre> listparam, String codeFse) throws IOException, JAXBException {

        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres = getParametres(listparam);
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("SETTLEMENT");
        parametreAppelDTO.setParam(parametres);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST");
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class);
        if (resultMessage != null && resultMessage.getErrCode() == 0) {
            SettlementResponse resultD = (SettlementResponse) localRestClient.convert(resultMessage.getResponseEntity(), SettlementResponse.class);
            return resultD;
        } else {
            return null;
        }

    }

    public  MResponse settlementTransaction(List<MParametre> listparam, String codeFse) throws IOException, JAXBException {
        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();
        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres = getParametres(listparam);
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("SETTLEMENT");
        parametreAppelDTO.setParam(parametres);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST");
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class);
        if (resultMessage.getErrCode() == 0) {
            MResponse resultD = (MResponse) localRestClient.convert(resultMessage.getResponseEntity(), MResponse.class);
            return resultD;
        } else {
            return null;
        }

    }

    public CollectionFacture getFacture(List<MParametre> listparam, String codeFse) throws IOException, JAXBException {

        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres = getParametres(listparam);
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("CONSULTATIONFACTURE");
        parametreAppelDTO.setParam(parametres);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST");
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class
        );

        if (resultMessage != null) {
            Message resultD = (Message) localRestClient.convert(resultMessage.getResponseEntity(), Message.class
            );

            if (resultD != null && resultD.getCodeErr() != null && resultD.getCodeErr().equals("0")) {
                return Util.unmarshall(resultD.getText(), CollectionFacture.class
                );
            } else {

                return null;
            }
        } else {
            throw new CustomParameterizedException("erreur!");
        }
    }

    /**
     * retourner des regles particulieres
     *
     * @param numerefacture
     * @param montant
     * @param codeFse
     * @return
     * @throws IOException
     * @throws JAXBException
     */
    public RegleFacturier getRegle(String numerefacture, String montant, String codeFse) throws IOException, JAXBException {

        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres.add(addParametre(1L, "REFERENCE", numerefacture));
        parametres.add(addParametre(2L, "MONTANT", montant));
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("VERIFREGLEFACTURIER");
        parametreAppelDTO.setParam(parametres);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST");
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class
        );

        if (resultMessage != null) {
            Message resultD = (Message) localRestClient.convert(resultMessage.getResponseEntity(), Message.class
            );

            if (resultD.getCodeErr().equals("0")) {

                return Util.unmarshall(resultD.getText(), RegleFacturier.class
                );
            } else {
                RegleFacturier b = new RegleFacturier();

                return b;
            }
        } else {
            throw new CustomParameterizedException("Le compte est introuvable!");
        }
    }

    /**
     * *
     * Paiement de aitime
     *
     * @param transactionId
     * @param codetel
     * @param numeroTel
     * @param montant
     * @param codeFse
     * @return
     * @throws IOException
     * @throws JAXBException
     */
    public SettlementResponse sendAirtime(String transactionId, String codetel, String numeroTel, String montant, String codeFse, String option) throws IOException, JAXBException {
        log.info("SERVICE AIRTIME" + "    #" + numeroTel);

        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        LocalDateTime local = LocalDateTime.now();
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres.add(addParametre(1L, "CODETEL", codetel));
        parametres.add(addParametre(2L, "NUMEROTELEPHONE", numeroTel));
        parametres.add(addParametre(2L, "TRANSACTIONID", transactionId));
        parametres.add(addParametre(3L, "OPTION", option));
        parametres.add(addParametre(4L, "MONTANT", montant));
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("GETAIRTIME");
        parametreAppelDTO.setParam(parametres);
        String message = showLogs(parametres);
        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST");
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class
        );

        if (resultMessage.getErrCode() == 0) {
            SettlementResponse resultD = (SettlementResponse) localRestClient.convert(resultMessage.getResponseEntity(), SettlementResponse.class
            );
            return resultD;
        } else {
            return null;
        }

    }
    
  
    
    
    public StoppageData getStoppage(String numeroCompte, String agence, String codeFse, String sens, String eventId, String isInit, String montant) throws IOException, JAXBException {
        log.info("APPEL DE LA FONCTION SOLDE");
        log.info("APPEL DE LA FONCTION SOLDE");
        StopWatch stopWatch = new StopWatch("MESURE DU SOLDE ");
          StoppageData stoppage =new StoppageData();
        try {
            stopWatch.start("1. TOKEB");
            RestClient localRestClient = setParam();
            AuthTokenInfo tokeinfo = null;//localRestClient.sendTokenRequest();
            stopWatch.stop();
            stopWatch.start("1. PARAMETRE");
            localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
            ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
            List<ParametreUserDTO> parametres = new ArrayList<>();
            parametres.add(addParametre(1L, "AGENCE", agence));
            parametres.add(addParametre(2L, "SENS", sens));
            
               parametres.add(addParametre(2L, "MONTANT", montant));
            parametres.add(addParametre(3L, "NUMEROCOMPTE", numeroCompte));
            parametres.add(addParametre(4L, "EVENTID", eventId));
            parametres.add(addParametre(5L, "ISINIT", isInit));
            parametreAppelDTO.setFse(codeFse);
            parametreAppelDTO.setCodeMethode("STOPPAGE");
            parametreAppelDTO.setParam(parametres);
            paramMap.put("X-AppID", this.appId);
            stopWatch.stop();
            stopWatch.start("1. REcup solde");
            
            ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST", this.paramMap);
            stopWatch.stop();
            if (result == null || result.getBody() == null) {
               return stoppage;
            }
            stopWatch.start("1. CONVERSION");
            ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class);
            stopWatch.stop();
            if (resultMessage != null) {
                if (resultMessage.getErrCode() < 0) {
                    throw new CustomParameterizedException(resultMessage.getMsg());
                }
                stopWatch.start("1. CONVERSION 2");
                Message resultD = (Message) localRestClient.convert(resultMessage.getResponseEntity(), Message.class);
                stopWatch.stop();
                log.info("===================");
                   if (resultD != null && resultD.getCodeErr()!=null){
                            stoppage = FastXml.fromXml(resultD.getText(), StoppageData.class);
                   }
            } 

        } finally {
            log.info("{}", stopWatch.prettyPrint());
        }
         return stoppage;
    }

    /**
     * *
     * Solde de la trasaction
     *
     * @param numeroCompte
     * @param agence
     * @param codeFse
     * @return
     * @throws IOException
     * @throws JAXBException
     */
    public Balance getSolde(String numeroCompte, String agence, String codeFse) throws IOException, JAXBException {
        log.info("APPEL DE LA FONCTION SOLDE");
        log.info("APPEL DE LA FONCTION SOLDE");
        StopWatch stopWatch = new StopWatch("MESURE DU SOLDE ");

        try {
              stopWatch.start("1. TOKEB");
            RestClient localRestClient = setParam();
            AuthTokenInfo tokeinfo = null;//localRestClient.sendTokenRequest();
            stopWatch.stop();
            stopWatch.start("1. PARAMETRE");
            localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
            ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
            List<ParametreUserDTO> parametres = new ArrayList<>();
            parametres.add(addParametre(1L, "AGENCE", agence));
            parametres.add(addParametre(2L, "NUMEROCOMPTE", numeroCompte));
            parametreAppelDTO.setFse(codeFse);
            parametreAppelDTO.setCodeMethode("CONSULTATIONSOLDE");
            parametreAppelDTO.setParam(parametres);
            paramMap.put("X-AppID", this.appId);
            stopWatch.stop();
            stopWatch.start("1. REcup solde");
            ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST", this.paramMap);
            stopWatch.stop();
            if (result == null || result.getBody() == null) {
                throw new CustomParameterizedException("Réponse vide du service solde.");
            }
            stopWatch.start("1. CONVERSION");
            ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class);
            stopWatch.stop();

            if (resultMessage != null) {
                if (resultMessage.getErrCode() < 0) {
                    throw new CustomParameterizedException(resultMessage.getMsg());
                }
                stopWatch.start("1. CONVERSION 2");
                Message resultD = (Message) localRestClient.convert(resultMessage.getResponseEntity(), Message.class);
                stopWatch.stop();
                log.info("===================");

                if (resultD != null && resultD.getCodeErr() != null && resultD.getCodeErr().equals("0")) {
                    log.info("===================TEXT " + resultD.getText());
                    stopWatch.start("1. Marcha 2");
                    
                    
                       Balance b = FastXml.fromXml(resultD.getText(), Balance.class);
                   // Balance b = (Balance) Util.unmarshall(resultD.getText(), Balance.class);
                    stopWatch.stop();
                    return b;
                } else {
                    Balance b = new Balance();
                    b.setSolde(0D);

                    return b;
                }
            } else {
                throw new CustomParameterizedException("Le compte est introuvable!");
            }

        } finally {
            log.info("{}", stopWatch.prettyPrint());
        }
      
    }
    /**
     * Service retournant l'historique des transations d'un compte en fonction
     * du fournisseur de service
     *
     * @param numeroCompte
     * @param finPeriode
     * @param codeFse
     * @return
     * @throws IOException
     */
    public synchronized Statement getHistorique(String numeroCompte, String agence, String codeFse, String debutPeriode, String finPeriode) throws IOException, JAXBException {
        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = null;//localRestClient.sendTokenRequest();
        HashMap<String, Object> param = new HashMap();
        log.info("Consultation de l'historique des transactions d'un compte : " + numeroCompte + "#" + agence);
        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres.add(addParametre(1L, "AGENCE", agence));
        parametres.add(addParametre(2L, "NUMEROCOMPTE", numeroCompte));
        parametres.add(addParametre(3L, "DEBUTPERIODE", debutPeriode));
        parametres.add(addParametre(4L, "FINPERIODE", finPeriode));
        paramMap.put("X-AppID", this.appId);
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("CONSULTATIONHIST");
        parametreAppelDTO.setParam(parametres);

        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST", this.paramMap);
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class
        );

        if (resultMessage != null) {
            Message resultD = (Message) localRestClient.convert(resultMessage.getResponseEntity(), Message.class
            );

            if (resultD != null) {
                if (resultD.getCodeErr() != null && resultD.getCodeErr().equals("0")) {
                   // Statement st = Util.unmarshall(resultD.getText(), Statement.clas);
                     Statement st = FastXml.fromXml(resultD.getText(), Statement.class);
                   
                    return st;
                } else {
                    Statement st = new Statement();
                    st.setListeMouvement(new ArrayList<>());
                    return st;
                }
            } else {
                throw new CustomParameterizedException("Le compte est introuvable!");
            }

        } else {
            throw new CustomParameterizedException("Le compte est introuvable!");
        }
    }

    public synchronized Statement getMiniReleve(String numeroCompte, String agence, String codeFse) throws IOException, JAXBException {

        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = null;// localRestClient.sendTokenRequest();

        HashMap<String, Object> param = new HashMap();
        log.info("Consultation de l'historique des transactions d'un compte : " + numeroCompte + "#" + agence + "#" + codeFse);
        localRestClient.setRestServerUri(this.url + SERVICE_INFO_SOLDE);
        String getData = "MINIRELEVE";
        ParametreAppelDTO parametreAppelDTO = new ParametreAppelDTO();
        List<ParametreUserDTO> parametres = new ArrayList<>();
        parametres.add(addParametre(1L, "AGENCE", agence));
        parametres.add(addParametre(2L, "NUMEROCOMPTE", numeroCompte));
        parametres.add(addParametre(4L, "DATA", getData));
        parametreAppelDTO.setFse(codeFse);
        parametreAppelDTO.setCodeMethode("GETDATA");
        parametreAppelDTO.setParam(parametres);

        ResponseEntity result = localRestClient.getRessourcesObject(tokeinfo, parametreAppelDTO, "POST", this.paramMap);
        ResultMessage resultMessage = (ResultMessage) localRestClient.convert(result.getBody(), ResultMessage.class
        );

        if (resultMessage != null) {
            Message resultD = (Message) localRestClient.convert(resultMessage.getResponseEntity(), Message.class
            );

            if (resultD != null) {
                if (resultD.getCodeErr() != null && resultD.getCodeErr().equals("0")) {
                  
                     Statement  st = FastXml.fromXml(resultD.getText(), Statement.class);
                    return st;
                } else {
                    Statement st = new Statement();
                    st.setListeMouvement(new ArrayList<>());
                    return st;
                }
            } else {
                throw new CustomParameterizedException("Le compte est introuvable!");
            }

        } else {
            throw new CustomParameterizedException("Le compte est introuvable!");
        }
    }

    public void allowMachine(String adresses) {
        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();

        localRestClient.setRestServerUri(this.mUrl + ADRESSE_AUTORISER);
        ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
        param.put("adresses", adresses);
        ResponseEntity result = localRestClient.getRessources(tokeinfo, param, "POST");
        log.debug("--------------Finish------------------");
    }

    @Transactional
    public void activeMachine(String uuid) throws IOException {

        RestClient localRestClient = setParam();
        AuthTokenInfo tokeinfo = localRestClient.sendTokenRequest();

        Machine machine = this.machineRepository.findOneByUuid(uuid);
        if (machine == null) {
            throw new CustomParameterizedException("Impossible de trouver la machine identifier par {} ", uuid);
        }
        final String p12Pass = RandomUtil.generateAlphaNumeric(6);
        String login = SecurityUtils.getCurrentUserLogin();

        localRestClient.setRestServerUri(this.mUrl + PARAM_MACHINE);
        ConcurrentHashMap<String, Object> param = new ConcurrentHashMap();
        param.put("adresseIp", machine.getAdresseIp());
        param.put("uuid", machine.getUuid());
        param.put("libelleOrganisation", machine.getOrganisation().getLibelleUniteOrganisation());
        param.put("currentUser", login);
        param.put("caKey", "");
        param.put("hostId", machine.getHostId());
        param.put("p12Pass", p12Pass);
        ResponseEntity result = localRestClient.getRessources(tokeinfo, param, "POST");

    }

}
