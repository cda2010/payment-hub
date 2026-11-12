/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pti.emeraude.gateway;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pti.emeraude.gateway.domain.AuthTokenInfo;
import com.pti.emeraude.gateway.domain.WsResponse;

import java.io.IOError;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * @author cda
 */
public class PISFNSetup {

    private final Logger log = LoggerFactory.getLogger(PISFNSetup.class);

    private RestTemplate restTemplate;
    private String clientSecureUrl;
    private String clientGatewayUrl;
    private String clientSecret;
    private String clientId;

    AuthTokenInfo tokenInfo;
    
    private static final String SERVICE_TRANSACTION = "srvtransaction";
    private static final String SERVICE_ALIAS = "srvalias";
    private static final String SERVICE_PARTICIPANTS = "srvparticipant";

    private static final String CREATE_ALIAS_URL = "api/aliases/mobile/new-alias";
    private static final String FIND_ALIAS_URL = "api/aliases/mobile/cle";
    private static final String DELETE_ALIAS_URL = "api/aliases/mobile/supprimer";
    private static final String GET_LIST_PARTICIPANT_ISO_URL = "api/participants/codepays/";
    private static final String GET_PARTICIPANT_URL = "api/participants/institutions";
    private static final String GET_LIST_TRANSACTION_URL = "api/v1/transactions/getClientLast10";
    private static final String GET_TRANSACTIONS_HISTORY_URL = "api/v1/transactions/transactionHistory";
    private static final String FIND_VERIFICATION_ALIAS = "api/aliases/mobile/findalias";
    private static final String CALCUL_FRAIS = "api/external/getFrais";
    private static final String GET_QR_STR = "api/v1/transactions/getQrCode";
    private static final String DO_OPERATION_URL = "api/v1/transactions/mobile";
    private static final String FIND_VERIFICATION_IBAN = "api/external/verifications-identites";
    private static final String CHECK_INFOS_IDENTITE_URL = "api/external/verifications-identites/rapport";
    private static final String FIND_DETAIL_FROM_QR_URL = "api/v1/transactions/qrCode/interpretation";
    private static final String GET_TRANSACTION_RECEIPT = "api/v1/transactions/mobile/reports";
    private static final String DEMAMDE_ANNULATION_URL = "api/demande-annulations";
    private static final String DEMANDE_PAIEMENT = "api/demandes-paiement";
    private static final String REJETER_DEMANDE_PAIEMENT = "api/demandes-paiement/rejets";
    private static final String ACCEPTER_DEMANDE_PAIEMENT = "api/demandes-paiement/acceptations";
    private static final String TRANSACTION_BASE = "api/v1/transactions";
    private static final String PROGRAMMATION_TRANSACTION_URL = "api/schedule-transactions";

    /** Revendication ENDPOINTS*/
    private static final String CREATION_REVENDICATION_URL = "api/mobile/revendications";
    private static final String ACCEPTER_REVENDICATION_URL = "api/mobile/revendications/acceptations";
    private static final String REJETER_REVENDICATION_URL = "api/mobile/revendications/rejets";

    public PISFNSetup() { }

    public PISFNSetup(String clientSecureUrl, String clientGatewayUrl, String clientSecret, String clientId) {
        this.restTemplate = new RestTemplate();
        this.clientSecureUrl = clientSecureUrl;
        this.clientGatewayUrl = clientGatewayUrl;
        this.clientSecret = clientSecret;
        this.clientId = clientId;
    }

    private static final String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$";
    private static final Pattern UUID_PATTERN = Pattern.compile(UUID_REGEX, Pattern.CASE_INSENSITIVE);

    public static boolean isValidUUID(String uuid) {
        return uuid != null && UUID_PATTERN.matcher(uuid).matches();
    }

    public AuthTokenInfo sendTokenRequest() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

        MultiValueMap<String, String> mapBody = new LinkedMultiValueMap<>();
        mapBody.add("grant_type", "client_credentials");
        mapBody.add("client_secret", this.clientSecret);
        mapBody.add("client_id", this.clientId);

        System.out.println("REQUEST URL  : " + this.clientSecureUrl);
        System.out.println("REQUEST BODY  : " + mapBody);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(mapBody, headers);
        ResponseEntity<Object> response = this.restTemplate.exchange(this.clientSecureUrl, HttpMethod.POST, request, Object.class, new Object[0]);

        System.out.println("Status code " + response.getStatusCode());
        System.out.println("Response body  : " + response.getBody());

        if (response.getStatusCode().is2xxSuccessful()) {
            LinkedHashMap<String, Object> map = (LinkedHashMap) response.getBody();
            this.tokenInfo = new AuthTokenInfo();
            if (map != null) {
                long expiresIn = System.currentTimeMillis();
                this.tokenInfo = new AuthTokenInfo();
                this.tokenInfo.setAccess_token((String) map.get("access_token"));
                this.tokenInfo.setToken_type((String) map.get("token_type"));
                this.tokenInfo.setRefresh_token((String) map.get("refresh_token_in"));
                this.tokenInfo.setExpires_in((Integer) map.get("expires_in"));
                this.tokenInfo.setScope((String) map.get("scope"));
                expiresIn += (this.tokenInfo.getExpires_in() * 1000L);
                this.tokenInfo.setExpireTime(expiresIn);
            }

            return this.tokenInfo;
        } else {
            return null;
        }
    }

    public boolean isTokenValid(AuthTokenInfo token) {

        log.debug("Vérification de la validité du token");

        if (token != null) {
            Date now = new Date();
            Long tim = now.getTime();
            return token.getExpireTime() > tim;
        } else {
            return false;
        }
    }


    public WsResponse fetchResource(
            AuthTokenInfo tokenInfo,
            Object requestBody,
            String httpMethod,
            String contentType,
            String serviceName,
            String uri,
            Map<String, String> requestHeader
    ) {

        HttpHeaders headers = new HttpHeaders();

        if (contentType.equals("XML")) {
            headers.add("Accept", MediaType.APPLICATION_XML_VALUE);
            headers.add("Content-Type", MediaType.APPLICATION_XML_VALUE);
        } else if (contentType.equals("JSON")) {
            headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
            headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE + "; charset=UTF-8");
        } else if (contentType.equals("TEXT")) {
            headers.add("Accept", MediaType.TEXT_PLAIN_VALUE);
            headers.add("Content-Type", MediaType.TEXT_PLAIN_VALUE);
        }

        if (Objects.nonNull(requestHeader)) {
            requestHeader.forEach(headers::add);
        }
        headers.add("Authorization", "Bearer " + tokenInfo.getAccess_token());

        String url = "";
        if (serviceName != null) {
            url = clientGatewayUrl + "/services/" + serviceName + "/" + uri;
        } else {
            url = clientGatewayUrl + "/" + uri;
        }
        System.out.println("APPEL A : " + url);

        HttpEntity<?> request = new HttpEntity<>(requestBody, headers);
        HttpMethod methode = null;
        switch (httpMethod) {
            case "POST":
                methode = HttpMethod.POST;
                break;
            case "GET":
                methode = HttpMethod.GET;
                break;
            case "PUT":
                methode = HttpMethod.PUT;
                break;
            case "PATCH":
                methode = HttpMethod.PATCH;
                break;
            case "DELETE":
                methode = HttpMethod.DELETE;
                break;
        }
        ResponseEntity<Object> response = null;
        try {
            response = restTemplate.exchange(url, methode, request, Object.class);
        } catch (HttpClientErrorException e) {

            String body = new String(e.getResponseBodyAsByteArray(), StandardCharsets.UTF_8);
            ObjectMapper mapper = new ObjectMapper();
            try {
                Map<String, Object> map = mapper.readValue(body, new TypeReference<Map<String, Object>>() {});
                return new WsResponse(e.getStatusCode().value(), map);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

        } catch (HttpServerErrorException | IOError e) {
            Map map = new HashMap();
            map.put("message", e.getMessage());
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    map
            );
        }

        return new WsResponse(response.getStatusCode().value(), response.getBody());
    }



    public WsResponse fetchPdfResource(
            AuthTokenInfo tokenInfo,
            Object requestBody,
            String httpMethod,
            String serviceName,
            String uri,
            Map<String, String> requestHeader
    ) {

        HttpHeaders headers = new HttpHeaders();

        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
        headers.add("Content-Type", MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.add("Authorization", "Bearer " + tokenInfo.getAccess_token());

        if (Objects.nonNull(requestHeader)) {
            requestHeader.forEach(headers::add);
        }

        String url = "";
        if (serviceName != null) {
            url = clientGatewayUrl + "/services/" + serviceName + "/" + uri;
        } else {
            url = clientGatewayUrl + "/" + uri;
        }
        System.out.println("APPEL A : " + url);

        HttpEntity<?> request = new HttpEntity<>(requestBody, headers);
        HttpMethod methode = null;
        switch (httpMethod) {
            case "POST":
                methode = HttpMethod.POST;
                break;
            case "GET":
                methode = HttpMethod.GET;
                break;
        }
        ResponseEntity<byte[]> response = null;
        try {
            response = restTemplate.exchange(url, methode, request, byte[].class);
        } catch (HttpServerErrorException e) {
            response = ResponseEntity.status(500).build();
        }

        return new WsResponse(response.getStatusCode().value(), response.getBody());
    }


    /** Revendication */
    public WsResponse createRevendication(Object body, String codeFse) {

        if (body == null || StringUtils.isBlank(codeFse)) {
            return new WsResponse(400, "Le corps ou les paramètres de la requête est vide");
        }

        AuthTokenInfo token = sendTokenRequest();

        if (token != null) {
            try {

                WsResponse response = this.fetchResource(
                        token,
                        body,
                        HttpMethod.POST.name(),
                        "JSON",
                        SERVICE_ALIAS,
                        CREATION_REVENDICATION_URL + "?codeFse=" + codeFse,
                        null
                );

                if (response == null) {
                    return new WsResponse(500, "Une erreur interne du serveur est survenue");
                }

                System.out.println("[CREATION REVENDICATION] - CODE STATUT : " + response.getStatusCode());
                System.out.println("[CREATION REVENDICATION] - BODY : " + response.getContent());

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Erreur survenue lors de la revendication");
            } catch (Exception ex) {
                return new WsResponse(500, "Une erreur interne du serveur est survenue");
            }
        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }

    public WsResponse accepterRevendication(Object body, String codeFse) {

        if (body == null || StringUtils.isBlank(codeFse)) {
            return new WsResponse(400, "Le corps ou les paramètres de la requête est vide");
        }

        AuthTokenInfo token = sendTokenRequest();
        if (token != null) {
            try {

                WsResponse response = this.fetchResource(
                        token,
                        body,
                        HttpMethod.POST.name(),
                        "JSON",
                        SERVICE_ALIAS,
                        ACCEPTER_REVENDICATION_URL + "?codeFse=" + codeFse,
                        null
                );

                if (response == null) {
                    return new WsResponse(500, "Une erreur interne du serveur est survenue");
                }

                System.out.println("[ACCEPTER REVENDICATION] - CODE STATUT : " + response.getStatusCode());
                System.out.println("[ACCEPTER REVENDICATION] - BODY : " + response.getContent());

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Erreur survenue lors de l'acceptation de la revendication");
            } catch (Exception ex) {
                return new WsResponse(500, "Une erreur interne du serveur est survenue");
            }
        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }

    public WsResponse rejetsRevendication(Object body, String codeFse) {

        if (body == null || StringUtils.isBlank(codeFse)) {
            return new WsResponse(400, "Le corps ou les paramètres de la requête est vide");
        }

        AuthTokenInfo token = sendTokenRequest();
        if (token != null) {
            try {

                WsResponse response = this.fetchResource(
                        token,
                        body,
                        HttpMethod.POST.name(),
                        "JSON",
                        SERVICE_ALIAS,
                        REJETER_REVENDICATION_URL + "?codeFse=" + codeFse,
                        null
                );

                if (response == null) {
                    return new WsResponse(500, "Une erreur interne du serveur est survenue");
                }

                System.out.println("[REJETS REVENDICATION] - CODE STATUT : " + response.getStatusCode());
                System.out.println("[REJETS REVENDICATION] - BODY : " + response.getContent());

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Erreur survenue lors du rejet de la revendication");
            } catch (Exception ex) {
                return new WsResponse(500, "Une erreur interne du serveur est survenue");
            }
        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }

    public WsResponse getParticipant(String codeParticipant) {

        if (StringUtils.isBlank(codeParticipant)) {
            return new WsResponse(400, "Le code participant est null ou vide");
        }

        AuthTokenInfo token = sendTokenRequest();
        if (token != null) {
            try {
                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.GET.name(),
                        "JSON",
                        SERVICE_PARTICIPANTS,
                        GET_PARTICIPANT_URL + "/" + codeParticipant,
                        null
                );

                if (response == null) {
                    return new WsResponse(500, "Une erreur interne du serveur est survenue");
                }

                System.out.println("[GET PARTICIPANT] - CODE STATUT : " + response.getStatusCode());
                System.out.println("[GET PARTICIPANT] - BODY : " + response.getContent());

                return response;
            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Erreur survenue lors du rejet de la revendication");
            } catch (Exception ex) {
                return new WsResponse(500, "Une erreur interne du serveur est survenue");
            }
        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }

    public WsResponse getRevendication(String id, String codeFse) {

        if (StringUtils.isBlank(id) || StringUtils.isBlank(codeFse)) {
            return new WsResponse(400, "Le corps de la requête est vide");
        }

        AuthTokenInfo token = sendTokenRequest();
        if (token != null) {
            try {

                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.GET.name(),
                        "JSON",
                        SERVICE_ALIAS,
                        CREATION_REVENDICATION_URL + "/" + id + "?codeFse=" + codeFse,
                        null
                );

                if (response == null) {
                    return new WsResponse(500, "Une erreur interne du serveur est survenue");
                }

                System.out.println("[REJETS REVENDICATION] - CODE STATUT : " + response.getStatusCode());
                System.out.println("[REJETS REVENDICATION] - BODY : " + response.getContent());

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Erreur survenue lors du rejet de la revendication");
            } catch (Exception ex) {
                return new WsResponse(500, "Une erreur interne du serveur est survenue");
            }
        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }


    public WsResponse planifierTransaction(Object body) {

        if (body == null) {
            return new WsResponse(400, "Le corps de la requête est vide");
        }

        AuthTokenInfo token = sendTokenRequest();
        
        if (token != null) {
            try {

                WsResponse response = this.fetchResource(
                        token,
                        body,
                        HttpMethod.POST.name(),
                        "JSON",
                        SERVICE_TRANSACTION,
                        PROGRAMMATION_TRANSACTION_URL + "/planifications",
                        null
                );

                if (response == null) {
                    return new WsResponse(500, "Une erreur interne du serveur est survenue");
                }

                System.out.println("[PROGRAMMATION TRX] - CODE STATUT : " + response.getStatusCode());
                System.out.println("[PROGRAMMATION TRX] - BODY : " + response.getContent());

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Erreur survenue lors de la planification");
            } catch (Exception ex) {
                return new WsResponse(500, "Une erreur interne du serveur est survenue");
            }
        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }


    public WsResponse transactionPlanifier(String alias) {
        if (StringUtils.isEmpty(alias)) {
            return new WsResponse(400, "L'alias fourni est null ou vide");
        }
        AuthTokenInfo token = sendTokenRequest();
        if (token != null) {

            try {

                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.GET.name(),
                        "JSON",
                        SERVICE_TRANSACTION,
                        PROGRAMMATION_TRANSACTION_URL + "/find/" + alias,
                        null
                );

                if (response == null) {
                    return new WsResponse(500, "Une erreur interne du serveur est survenue");
                }

                System.out.println("[CLIENT PROGRAMMATIONS] - CODE STATUT : " + response.getStatusCode());
                System.out.println("[CLIENT PROGRAMMATIONS] - BODY : " + response.getContent());

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            } catch (Exception ex) {
                return new WsResponse(500, "Une erreur interne du serveur est survenue");
            }

        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }


    public WsResponse updateTransactionPlanifier(Long id, Object body) {

        if (body == null) {
            return new WsResponse(400, "Le corps de la demande est vide");
        }
        if (id == null) {
            return new WsResponse(400, "L'id fourni est null ou vide");
        }

        AuthTokenInfo token = sendTokenRequest();
        if (token != null) {
            try {

                Map<String, String> requestHeaders = new HashMap<>();

                WsResponse response = this.fetchResource(
                        token,
                        body,
                        HttpMethod.PUT.name(),
                        "JSON",
                        SERVICE_TRANSACTION,
                        PROGRAMMATION_TRANSACTION_URL + "/" + id,
                        requestHeaders
                );

                if (response == null) {
                    return new WsResponse(500, "Une erreur interne du serveur est survenue");
                }

                System.out.println("[Update Trans Planifiée] - CODE STATUT : " + response.getStatusCode());
                System.out.println("[Update Trans Planifiée] - BODY : " + response.getContent());

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            } catch (Exception ex) {
                return new WsResponse(500, "Une erreur interne du serveur est survenue");
            }
        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }


    public WsResponse deleteTransactionPlanifier(Long id) {

        if (id == null) {
            return new WsResponse(400, "L'id fourni est null ou vide");
        }

        AuthTokenInfo token = sendTokenRequest();
        if (token != null) {
            try {

                Map<String, String> requestHeaders = new HashMap<>();

                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.DELETE.name(),
                        "JSON",
                        SERVICE_TRANSACTION,
                        PROGRAMMATION_TRANSACTION_URL + "/" + id,
                        requestHeaders
                );

                if (response == null) {
                    return new WsResponse(500, "Une erreur interne du serveur est survenue");
                }

                System.out.println("[Delete Trans Planifiée] - CODE STATUT : " + response.getStatusCode());
                System.out.println("[Delete Trans Planifiée] - BODY : " + response.getContent());

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            } catch (Exception ex) {
                return new WsResponse(500, "Une erreur interne du serveur est survenue");
            }
        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }


    public WsResponse demandePaiement(String codeParticipant, Object body) {

        if (body == null) {
            return new WsResponse(400, "Le corps de la requête est vide");
        }
        if (StringUtils.isBlank(codeParticipant)) {
            return new WsResponse(400, "Le code du participant est vide");
        }

        AuthTokenInfo token = sendTokenRequest();

        if (token != null) {
            try {

                Map<String, String> requestHeaders = new HashMap<>();
                requestHeaders.put("X-Tenant", codeParticipant);

                WsResponse response = this.fetchResource(
                        token,
                        body,
                        HttpMethod.POST.name(),
                        "JSON",
                        SERVICE_TRANSACTION,
                        DEMANDE_PAIEMENT,
                        requestHeaders
                );

                if (response == null) {
                    return new WsResponse(500, "Une erreur interne du serveur est survenue");
                }

                System.out.println("[Demande Paiement] - CODE STATUT : " + response.getStatusCode());
                System.out.println("[Demande Paiement] - BODY : " + response.getContent());

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            } catch (Exception ex) {
                return new WsResponse(500, "Une erreur interne du serveur est survenue");
            }
        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }

    }

    public WsResponse rejeterDemandePaiement(String endToEndId, String transactionId, String raison, String tenant) {

        if (StringUtils.isBlank(tenant) || StringUtils.isBlank(endToEndId) || StringUtils.isBlank(transactionId) || StringUtils.isBlank(raison)) {
            return new WsResponse(400, "Les paramètres sont invalides");
        }

        AuthTokenInfo token = sendTokenRequest();

        if (token != null) {
            try {

                String url = "?endToEndId=" + endToEndId + "&transactionId=" + transactionId + "&raison=" + raison + "&tenant=" + tenant;

                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.GET.name(),
                        "JSON",
                        SERVICE_TRANSACTION,
                        REJETER_DEMANDE_PAIEMENT + url,
                        null
                );

                if (response == null) {
                    return new WsResponse(500, "Une erreur interne du serveur est survenue");
                }

                System.out.println("[Rejeter Demande Paiement] - CODE STATUT : " + response.getStatusCode());
                System.out.println("[Rejeter Demande Paiement] - BODY : " + response.getContent());

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            } catch (Exception ex) {
                return new WsResponse(500, "Une erreur interne du serveur est survenue");
            }
        } else {
            return new WsResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Autorisations manquantes pour cette opération"
            );
        }
    }


    public WsResponse accepterDemandePaiement(String codeParticipant, String endToEndId, String transactionId) {

        if (StringUtils.isBlank(codeParticipant) || StringUtils.isBlank(endToEndId) || StringUtils.isBlank(transactionId)) {
            return new WsResponse(400, "Les paramètres sont invalides");
        }

        AuthTokenInfo token = sendTokenRequest();

        if (token != null) {
            try {

                Map<String, String> requestHeaders = new HashMap<>();
                requestHeaders.put("X-Tenant", codeParticipant);

                String url = "?endToEndId=" + endToEndId + "&txId=" + transactionId;
                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.POST.name(),
                        "JSON",
                        SERVICE_TRANSACTION,
                        ACCEPTER_DEMANDE_PAIEMENT + url,
                        requestHeaders
                );

                if (response == null) {
                    return new WsResponse(500, "Une erreur interne du serveur est survenue");
                }

                System.out.println("[Accepter Demande Paiement] - CODE STATUT : " + response.getStatusCode());
                System.out.println("[Accepter Demande Paiement] - BODY : " + response.getContent());

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            } catch (Exception ex) {
                return new WsResponse(500, "Une erreur interne du serveur est survenue");
            }
        } else {
            return new WsResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Autorisations manquantes pour cette opération"
            );
        }

    }


    public WsResponse retourDeFonds(String endToEndId, String participant, BigDecimal montant,String raison) {

        if (StringUtils.isBlank(endToEndId) || StringUtils.isBlank(participant) || StringUtils.isBlank(raison)) {
            return new WsResponse(400, "Les parameters sont invalides");
        }

        AuthTokenInfo token = sendTokenRequest();

        if (token != null) {
            try {
                String url = "/" + endToEndId + "/retours?" + "participant=" + participant;

                if (montant != null && montant.compareTo(BigDecimal.ZERO) > 0) {
                    url = url + "&montant=" + montant.longValue();
                }

                url = url + "&raison=" + raison;

                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.GET.name(),
                        "JSON",
                        SERVICE_TRANSACTION,
                        TRANSACTION_BASE + url,
                        null
                );

                if (response == null) {
                    return new WsResponse(500, "Une erreur interne du serveur est survenue");
                }

                System.out.println("[Retours Fonds] - CODE STATUT : " + response.getStatusCode());
                System.out.println("[Retours Fonds] - BODY : " + response.getContent());

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            } catch (Exception ex) {
                return new WsResponse(500, "Une erreur interne du serveur est survenue");
            }
        } else {
            return new WsResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Autorisations manquantes pour cette opération"
            );
        }
    }

    public WsResponse findClientDemandePaiement(String alias) {

        if (StringUtils.isBlank(alias) || !isValidUUID(alias)) {
            return new WsResponse(400, "L'alias est vide ou invalide");
        }

        AuthTokenInfo token = sendTokenRequest();

        if (token != null) {
            try {

                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.GET.name(),
                        "JSON",
                        SERVICE_TRANSACTION,
                        DEMANDE_PAIEMENT + "/client/" + alias,
                        null
                );

                if (response == null) {
                    return new WsResponse(500, "Une erreur interne du serveur est survenue");
                }

                System.out.println("[Rapport Retours Fonds] - CODE STATUT : " + response.getStatusCode());
                System.out.println("[Rapport Retours Fonds] - BODY : " + response.getContent());

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            } catch (Exception ex) {
                return new WsResponse(500, "Une erreur interne du serveur est survenue");
            }
        } else {
            return new WsResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Autorisations manquantes pour cette opération"
            );
        }

    }

    public WsResponse rapportRetourDeFonds(String endToEndId, String statut, String codeParticipant, String raison) {

        if (StringUtils.isBlank(codeParticipant) || StringUtils.isBlank(statut) || StringUtils.isBlank(endToEndId)) {
            return new WsResponse(400, "Les parameters sont invalides");
        }

        AuthTokenInfo token = sendTokenRequest();

        if (token != null) {
            try {

                String url = "/retours/rapports?endToEndId=" + endToEndId + "&statut=" + statut + "&codeParticipant=" + codeParticipant;

                if (StringUtils.isNotBlank(raison)) {
                    url = url + "&raison=" + raison;
                }

                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.GET.name(),
                        "JSON",
                        SERVICE_TRANSACTION,
                        TRANSACTION_BASE + url,
                        null
                );

                if (response == null) {
                    return new WsResponse(500, "Une erreur interne du serveur est survenue");
                }

                System.out.println("[Rapport Retours Fonds] - CODE STATUT : " + response.getStatusCode());
                System.out.println("[Rapport Retours Fonds] - BODY : " + response.getContent());

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            } catch (Exception ex) {
                return new WsResponse(500, "Une erreur interne du serveur est survenue");
            }
        } else {
            return new WsResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Autorisations manquantes pour cette opération"
            );
        }
    }


    public WsResponse demandeAnnulation(
            String endToEndId,
            String participant,
            String raison
    ) {

        if (StringUtils.isBlank(endToEndId)
                || StringUtils.isBlank(participant)
                || StringUtils.isBlank(raison)
        ) {
            return new WsResponse(400, "Les parameters sont invalides");
        }

        AuthTokenInfo token = sendTokenRequest();

        if (token != null) {
            try {
                String url = "/" + endToEndId + "/annulations?" + "participant=" + participant;
                url = url +  "&raison=" + raison;

                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.GET.name(),
                        "JSON",
                        SERVICE_TRANSACTION,
                        TRANSACTION_BASE + url,
                        null
                );

                if (response == null) {
                    return new WsResponse(500, "Une erreur interne du serveur est survenue");
                }

                System.out.println("[Demande Annulation] - CODE STATUT : " + response.getStatusCode());
                System.out.println("[Demande Annulation] - BODY : " + response.getContent());

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            } catch (Exception ex) {
                return new WsResponse(500, "Une erreur interne du serveur est survenue");
            }
        } else {
            return new WsResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Autorisations manquantes pour cette opération"
            );
        }
    }

    public WsResponse rejeterDemandeAnnulation(
            String endToEndId,
            String participant,
            String raison
    ) {

        if (StringUtils.isBlank(endToEndId)
                || StringUtils.isBlank(participant)
                || StringUtils.isBlank(raison)
        ) {
            return new WsResponse(400, "Les parameters sont invalides");
        }

        AuthTokenInfo token = sendTokenRequest();

        if (token != null) {
            try {
                String url = "/" + endToEndId + "/annulations/rapports?" + "participant=" + participant + "&raison=" + raison;

                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.GET.name(),
                        "JSON",
                        SERVICE_TRANSACTION,
                        TRANSACTION_BASE + url,
                        null
                );

                if (response == null) {
                    return new WsResponse(500, "Une erreur interne du serveur est survenue");
                }

                System.out.println("[Rejeter Demande Annulation] - CODE STATUT : " + response.getStatusCode());
                System.out.println("[Rejeter Demande Annulation] - BODY : " + response.getContent());

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            } catch (Exception ex) {
                return new WsResponse(500, "Une erreur interne du serveur est survenue");
            }
        } else {
            return new WsResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Autorisations manquantes pour cette opération"
            );
        }
    }


    public WsResponse findDemandeAnnulation(String numeroCompte) {

        if (StringUtils.isBlank(numeroCompte)) {
            return new WsResponse(400, "Le numéro de compte est invalide");
        }

        AuthTokenInfo token = sendTokenRequest();

        if (token != null) {
            try {

                String url = "/liste?numeroCompte=" + numeroCompte;

                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.GET.name(),
                        "JSON",
                        SERVICE_TRANSACTION,
                        DEMAMDE_ANNULATION_URL + url,
                        null
                );

                if (response.getContent() == null) {
                    return new WsResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "L'opération de création d'alias à échouée"
                    );
                }

                log.debug("[CLIENT ANNULATIONS] - CODE : {}", response.getStatusCode());
                log.debug("[CLIENT ANNULATIONS] - BODY : {}", response.getContent());

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            }
        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }

    }


    public WsResponse calculFrais(String codeFse, String montant, String service) {

        AuthTokenInfo token = sendTokenRequest();

        if (token != null) {
            try {
                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.GET.name(),
                        "JSON",
                        SERVICE_TRANSACTION,
                        CALCUL_FRAIS + "?codeFse=" + codeFse + "&montant=" + montant + "&service=" + service,
                        null
                );
                log.debug("RESPONSE CODE : {}", response.getStatusCode());
                log.debug("RESPONSE BODY : {}", response.getContent());

                if (response.getContent() == null) {
                    return new WsResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "L'opération de création d'alias à échouée"
                    );
                }

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            }

        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }


    public WsResponse doLink(Object payload) {

        AuthTokenInfo token = sendTokenRequest();

        if (token != null) {
            try {
                WsResponse response = this.fetchResource(
                        token,
                        payload,
                        HttpMethod.POST.name(),
                        "JSON",
                        SERVICE_ALIAS,
                        CREATE_ALIAS_URL,
                        null
                );
                log.debug("RESPONSE CODE : {}", response.getStatusCode());
                log.debug("RESPONSE BODY : {}", response.getContent());

                if (response.getContent() == null) {
                    return new WsResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "L'opération de création d'alias à échouée"
                    );
                }

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            }

        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }

    public WsResponse doOperation(Object payload, String tenant, String alias, String numeroCompte) {

        AuthTokenInfo token = sendTokenRequest();
        String url = "?tenant=" + tenant;
        if (numeroCompte != null) {
            url = url + "&numeroCompte=" + numeroCompte;
        } else if (alias != null) {
            url = url + "&alias=" + alias;
        }

        if (token != null) {
            try {
                WsResponse response = this.fetchResource(
                        token,
                        payload,
                        HttpMethod.POST.name(),
                        "JSON",
                        SERVICE_TRANSACTION,
                        DO_OPERATION_URL + url,
                        null
                );
                log.debug("RESPONSE CODE : {}", response.getStatusCode());
                log.debug("RESPONSE BODY : {}", response.getContent());

                if (response.getContent() == null) {
                    return new WsResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "L'opération de création d'alias à échouée"
                    );
                }

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            }

        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }


    public WsResponse findAlias(String cle) {
        AuthTokenInfo token = sendTokenRequest();
        if (token != null) {
            try {
                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.GET.name(),
                        "JSON",
                        SERVICE_ALIAS,
                        FIND_ALIAS_URL + "/" + cle,
                        null
                );
                log.debug("RESPONSE CODE : {}", response.getStatusCode());
                log.debug("RESPONSE BODY : {}", response.getContent());

                if (response.getContent() == null) {
                    return new WsResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "L'opération de création d'alias à échouée"
                    );
                }

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            }

        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }


    public WsResponse deleteAlias(String alias, String raison, String codeParticipant) {
        AuthTokenInfo token = sendTokenRequest();
        if (token != null) {
            try {
                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.GET.name(),
                        "JSON",
                        SERVICE_ALIAS,
                        DELETE_ALIAS_URL + "/" + alias + "?raison=" + raison + "&codeParticipant=" + codeParticipant,
                        null
                );
                System.out.println("STATUS CODE : " + response.getStatusCode());
                System.out.println("RESPONSE BODY : " + response.getContent());

                if (response.getContent() == null) {
                    return new WsResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "La suppression de l'alias à échouée"
                    );
                }

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            }

        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }


    public WsResponse getDetailAliasFromQr(String valeur) {
        AuthTokenInfo token = sendTokenRequest();
        if (token != null) {
            try {
                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.GET.name(),
                        "JSON",
                        SERVICE_TRANSACTION,
                        FIND_DETAIL_FROM_QR_URL + "?value=" + valeur,
                        null
                );
                log.debug("RESPONSE CODE : {}", response.getStatusCode());
                log.debug("RESPONSE BODY : {}", response.getContent());

                if (response.getContent() == null) {
                    return new WsResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "L'opération de création d'alias à échouée"
                    );
                }
                return response;
            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            }

        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }


    public WsResponse findListTransaction(String numeroCompte) {
        AuthTokenInfo token = sendTokenRequest();
        if (token != null) {
            try {
                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.GET.name(),
                        "JSON",
                        SERVICE_TRANSACTION,
                        GET_LIST_TRANSACTION_URL + "?numeroCompte=" + numeroCompte,
                        null
                );
                log.debug("RESPONSE CODE : {}", response.getStatusCode());
                log.debug("RESPONSE BODY : {}", response.getContent());

                if (response.getContent() == null) {
                    return new WsResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Aucune transaction disponible"
                    );
                }

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            }

        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }

    public WsResponse transactionHistory(String numeroCompte) {
        AuthTokenInfo token = sendTokenRequest();

        if (token != null) {
            try {
                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.GET.name(),
                        "JSON",
                        SERVICE_TRANSACTION,
                        GET_TRANSACTIONS_HISTORY_URL + "?numeroCompte=" + numeroCompte,
                        null
                );
                log.debug("RESPONSE CODE : {}", response.getStatusCode());
                log.debug("RESPONSE BODY : {}", response.getContent());

                if (response.getContent() == null) {
                    return new WsResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Aucune transaction disponible"
                    );
                }

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            }

        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }


    public WsResponse getQrcode(String alias, String codePays, String reference, String canal, String montant) {
        AuthTokenInfo token = sendTokenRequest();
        if (token != null) {
            try {
                String url = "?alias=" + alias + "&codePays=" + codePays + "&canal=" + canal;
                if (montant != null) {
                    url = url + "&montant=" + montant;
                }
                if (reference != null) {
                    url = url + "&reference=" + reference;
                }

                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.GET.name(),
                        "JSON",
                        SERVICE_TRANSACTION,
                        GET_QR_STR + url,
                        null
                );
                log.debug("RESPONSE CODE : {}", response.getStatusCode());
                log.debug("RESPONSE BODY : {}", response.getContent());

                if (response.getContent() == null) {
                    return new WsResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Qr code"
                    );
                }

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            }

        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }

    public WsResponse getListParticipant(String codeIsoPays) {
        AuthTokenInfo token = sendTokenRequest();
        if (token != null) {
            try {
                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.GET.name(),
                        "JSON",
                        "srvparticipant",
                        GET_LIST_PARTICIPANT_ISO_URL + "/" + codeIsoPays,
                        null
                );
                log.debug("RESPONSE CODE : {}", response.getStatusCode());
                log.debug("RESPONSE BODY : {}", response.getContent());

                if (response.getContent() == null) {
                    return new WsResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Liste des participants introuvable"
                    );
                }

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            }

        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }


    public WsResponse verificationAlias(String alias, String codefse) {
        AuthTokenInfo token = sendTokenRequest();
        if (token != null) {
            try {
                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.GET.name(),
                        "JSON",
                        SERVICE_ALIAS,
                        FIND_VERIFICATION_ALIAS + "/" + alias + "?codefse=" + codefse,
                        null
                );
                log.debug("RESPONSE CODE : {}", response.getStatusCode());
                log.debug("RESPONSE BODY : {}", response.getContent());

                if (response.getContent() == null) {
                    return new WsResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "La vérification de l'alias a échouée"
                    );
                }

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            }

        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }

    public WsResponse verificationIdentite(String codeParticipantEmetteur, String codePaticipantBenef, String iban) {
        AuthTokenInfo token = sendTokenRequest();
        if (token != null) {
            try {
                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.POST.name(),
                        "JSON",
                        SERVICE_TRANSACTION,
                        FIND_VERIFICATION_IBAN + "?codeEmetteur=" + codeParticipantEmetteur + "&codeBenef=" + codePaticipantBenef + "&iban=" + iban,
                        null
                );
                log.debug("RESPONSE CODE : {}", response.getStatusCode());
                log.debug("RESPONSE BODY : {}", response.getContent());

                if (response.getContent() == null) {
                    return new WsResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Erreur survenue lors de la vérification"
                    );
                }

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            }

        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }

    public WsResponse checkInfosIdentite(String requestId, String endToEndId) {
        AuthTokenInfo token = sendTokenRequest();
        if (token != null) {
            try {
                WsResponse response = this.fetchResource(
                        token,
                        null,
                        HttpMethod.GET.name(),
                        "JSON",
                        SERVICE_TRANSACTION,
                        CHECK_INFOS_IDENTITE_URL + "?requestId=" + requestId + "&endToEndId=" + endToEndId,
                        null
                );

                log.debug("-- RESPONSE CODE : {}", response.getStatusCode());
                log.debug("-- RESPONSE BODY : {}", response.getContent());

                if (response.getStatusCode() == HttpStatus.OK.value()) {
                    return response;
                }

                if (response.getStatusCode() == HttpStatus.NO_CONTENT.value()) {
                    return new WsResponse(204, "Erreur survenue lors de la vérification d'identité");
                }

                return new WsResponse(500, "Erreur survenue lors de la vérification d'identité");

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            }

        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }

    public WsResponse downloadTransactionReceipt(String codePartcipant, String endToEndId) {
        AuthTokenInfo token = sendTokenRequest();

        Map<String, String> reqHeaders = new HashMap<>();
        reqHeaders.put("X-Tenant", codePartcipant);

        if (token != null) {
            try {
                WsResponse response = this.fetchPdfResource(
                        token,
                        null,
                        HttpMethod.GET.name(),
                        SERVICE_TRANSACTION,
                        GET_TRANSACTION_RECEIPT + "/" + endToEndId,
                        reqHeaders
                );
                log.debug("RESPONSE CODE : {}", response.getStatusCode());
                log.debug("RESPONSE BODY : {}", response.getContent());

                if (response.getContent() == null) {
                    return new WsResponse(
                            HttpStatus.INTERNAL_SERVER_ERROR.value(),
                            "Une erreur interne est survenue lors de l'opération"
                    );
                }

                return response;

            } catch (HttpClientErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), ex.getMessage());
            } catch (HttpServerErrorException ex) {
                return new WsResponse(ex.getStatusCode().value(), "Une erreur interne du serveur est survenue");
            }

        } else {
            return new WsResponse(HttpStatus.UNAUTHORIZED.value(), "Autorisations manquantes pour cette opération");
        }
    }

    public String getClientSecureUrl() {
        return clientSecureUrl;
    }

    public void setClientSecureUrl(String clientSecureUrl) {
        this.clientSecureUrl = clientSecureUrl;
    }

    public String getClientGatewayUrl() {
        return clientGatewayUrl;
    }

    public void setClientGatewayUrl(String clientGatewayUrl) {
        this.clientGatewayUrl = clientGatewayUrl;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public AuthTokenInfo getTokenInfo() {
        return tokenInfo;
    }

    public void setTokenInfo(AuthTokenInfo tokenInfo) {
        this.tokenInfo = tokenInfo;
    }

}
