package com.m2i.pti.external.service;

import com.google.common.hash.Hashing;
import com.m2i.pti.domain.*;
import com.m2i.pti.domain.enumeration.TypeFrais;
import com.m2i.pti.mobile.dto.MResponse;
import com.m2i.pti.external.domain.ExternalResponse;
import com.m2i.pti.gateway.rest.GatewayResource;
import com.m2i.pti.mobile.dto.MFrais;
import com.m2i.pti.mobile.dto.MOperation;
import com.m2i.pti.mobile.dto.MOperationFacture;
import com.m2i.pti.mobile.dto.MParamFrais;
import com.m2i.pti.mobile.dto.MParametre;
import com.m2i.pti.mobile.dto.MResponseOperation;
import com.m2i.pti.mobile.service.MobileService;
import com.m2i.pti.repository.*;
import com.m2i.pti.service.ApiService;
import com.m2i.pti.service.ClientService;
import com.m2i.pti.service.CompteService;
import com.m2i.pti.service.dto.ClientDTO;
import com.m2i.pti.service.dto.Customers;
import com.m2i.pti.service.dto.Message;
import com.m2i.pti.service.util.RandomUtil;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Service

public class ExternalService {

    private final ParametreAppRepository parametreAppRepository;
    private final CompteRepository compteRepository;
    private final CompteService compteService;
    private final GatewayResource gatewayResource;
    private final MobileService mobileService;
    private final CredentialApiRepository credentialApiRepository;
    private final ExternalOTPRepository externalOTPRepository;
    private final OauthClientDetailsRepository oauthClientDetailsRepository;
    private final ApiService apiService;
    private final ClientRepository clientRepository;
    private final ClientService clientService ;
    private final int DELAIS = 600;
    private final Logger log = LoggerFactory.getLogger(ClientService.class);

    public ExternalService(ParametreAppRepository parametreAppRepository, CompteRepository compteRepository, CompteService compteService, GatewayResource gatewayResource, MobileService mobileService, CredentialApiRepository credentialApiRepository, ExternalOTPRepository externalOTPRepository, OauthClientDetailsRepository oauthClientDetailsRepository, ApiService apiService,ClientRepository clientRepository,ClientService clientService) {
        this.parametreAppRepository = parametreAppRepository;
        this.compteRepository = compteRepository;
        this.compteService = compteService;
        this.gatewayResource = gatewayResource;
        this.mobileService = mobileService;
        this.credentialApiRepository = credentialApiRepository;
        this.externalOTPRepository = externalOTPRepository;
        this.oauthClientDetailsRepository = oauthClientDetailsRepository;
        this.apiService = apiService;
        this.clientRepository=clientRepository;
        this.clientService=clientService;
    }

    @Transactional(readOnly = true)
    public Message getSolde(String codePays, String telephone) throws IOException, JAXBException {
        ParametreApp parametreApp = parametreAppRepository.findByTypeAndNomAndCodeFse("FOURNISSEUR_MONNAIE", codePays);
        if (parametreApp == null || parametreApp.getValeur() == null) {
            throw new CustomParameterizedException("Impossible de trouvé le fournisseur de monnaie !");
        } else {
            Compte cpte = mobileService.findComptePersonne(telephone, codePays);

            //compteService.findByCompteFse(telephone, parametreApp.getValeur());
            if (cpte == null) {
                throw new CustomParameterizedException("Compte introuvable");
            } else {
                return gatewayResource.getSolde(cpte.getValeurCompte(), parametreApp.getValeur());
            }

        }
    }

    @Transactional(readOnly = true)
    public Message getHistorique(String codePays, String telephone, String debutPeriode, String finPeriode) throws IOException, JAXBException, NoSuchAlgorithmException {
        ParametreApp parametreApp = parametreAppRepository.findByTypeAndNomAndCodeFse("FOURNISSEUR_MONNAIE", codePays);
        if (parametreApp == null || parametreApp.getValeur() == null) {
            throw new CustomParameterizedException("Impossible de trouvé le fournisseur de monnaie !");
        } else {
            Compte cpte = mobileService.findComptePersonne(telephone, codePays);
            if (cpte == null) {
                throw new CustomParameterizedException("Compte introuvable");
            } else {
                return gatewayResource.getHistorique(cpte.getValeurCompte(), parametreApp.getValeur(), debutPeriode, finPeriode);
            }

        }
    }

    @Transactional(readOnly = true)
    public Message getInfosClient(String codePays, String telephone) throws IOException, JAXBException {
        return gatewayResource.getInfoClientCompte(telephone, codePays);
    }

    public CredentialApi verifieHashParam(String params, String hash_param, String client_id) {
        System.out.println("client_id :" + client_id);
        OauthClientDetails oauthClientDetails = oauthClientDetailsRepository.findClient_id(client_id);
        if (oauthClientDetails != null) {
            System.out.println(Hashing.sha256().hashString(params + oauthClientDetails.getClient_secret(), StandardCharsets.UTF_8).toString());
            if (Hashing.sha256().hashString(params + oauthClientDetails.getClient_secret(), StandardCharsets.UTF_8).toString().equals(hash_param)) {
                log.debug("Credential : {}", oauthClientDetails);
                CredentialApi credentialApi = new CredentialApi();
                credentialApi.setClient_id(oauthClientDetails.getClient_id());
                credentialApi.setClient_secret(oauthClientDetails.getClient_secret());
                return credentialApi;
            }
        }
        return null;

    }

    public MFrais calculFrais(MParamFrais mParamFrais) {
        return mobileService.calculFrais(mParamFrais,TypeFrais.FRAIS);
    }

    public ExternalResponse doOperationRetraitCompte(MOperation mOperation) throws IOException, NoSuchAlgorithmException, JAXBException {
        MResponseOperation mResponseOperation = mobileService.doOperationRetraitCompte(mOperation);
        ExternalResponse externalResponse;// = new ExternalResponse("-1", "Impossible de terminer la transaction", null, null);
        if (mResponseOperation != null && !mResponseOperation.getCode().equals("-1") && mResponseOperation.getOperation() != null) {
            mobileService.envoiDesMessage(mResponseOperation.getOperation(), null);
            mobileService.repartirCommission(mResponseOperation.getOperation());
            externalResponse = new ExternalResponse(mResponseOperation.getCode(), mResponseOperation.getMessage(), mResponseOperation.getOperation().getCodeOperation(), mOperation.getMontant().toString());
        } else {
            externalResponse = new ExternalResponse("-1", mResponseOperation.getMessage(), null, null);
        }

        return externalResponse;
    }

    public MResponse doOperationPaiementFacture(MOperationFacture mOperation) throws IOException, NoSuchAlgorithmException, JAXBException {

        MResponse mResponse = mobileService.doOperationPaiementFacture(mOperation);
        log.debug("reponse : {}", mResponse);
        String transactionId = "0";
        List<MParametre> extraData = null;
        if (mResponse != null && mResponse.getTansactionId() != null) {

            Operation oper = mobileService.getOperation(Long.parseLong(mResponse.getTansactionId()));
            if (mResponse.getExtraData() != null) {
                extraData = mResponse.getExtraData();
            } else {
                extraData = mobileService.populateMessage(oper);
            }

            mobileService.envoiDesMessage(oper, mResponse.getExtraData());
            mobileService.repartirCommission(oper);
            transactionId = oper.getCodeOperation();
            return new MResponse(mResponse.getCode(), mResponse.getMessage(), transactionId, extraData);

        } else if (mResponse != null) {
            return new MResponse(mResponse.getCode(), mResponse.getMessage());
        }
        return new MResponse("-1", "Erreur inconnue");
    }

    public MResponse doOperationPaiementCreditAirtime(MOperationFacture mOperation) throws IOException, NoSuchAlgorithmException, JAXBException {

        MResponseOperation mResponse = mobileService.doOperationPaiementCredit(mOperation);
        log.debug("reponse : {}", mResponse);
        String transactionId = "0";
        List<MParametre> extraData = null;
        if (mResponse != null && mResponse.getTansactionId() != null) {

            Operation oper = mobileService.getOperation(Long.parseLong(mResponse.getTansactionId()));

            mobileService.repartirCommission(oper);
            transactionId = oper.getCodeOperation();
            return new MResponse(mResponse.getCode(), mResponse.getMessage(), transactionId, extraData);

        } else if (mResponse != null) {
            return new MResponse(mResponse.getCode(), mResponse.getMessage());
        }
        return new MResponse("-1", "Erreur inconnue");
    }

    public ExternalResponse doOperationCompteACompte(MOperation mOperation) throws IOException, NoSuchAlgorithmException, JAXBException {
        MResponseOperation mResponseOperation = mobileService.doOperation(mOperation);
        ExternalResponse externalResponse = new ExternalResponse("-1", "Impossible de terminer la transaction", null, null);

        if (mResponseOperation != null && !mResponseOperation.getCode().equals("-1") && mResponseOperation.getOperation() != null) {
            mobileService.envoiDesMessage(mResponseOperation.getOperation(), null);
            mobileService.repartirCommission(mResponseOperation.getOperation());
            externalResponse = new ExternalResponse(mResponseOperation.getCode(), mResponseOperation.getMessage(), mResponseOperation.getOperation().getCodeOperation(), mOperation.getMontant().toString());
        }

        return externalResponse;
    }

    public ExternalResponse doOperationDepotCompte(MOperation mOperation) throws IOException, NoSuchAlgorithmException, JAXBException {
        ExternalResponse externalResponse = new ExternalResponse("-1", "Impossible de terminer la transaction", null, null);

        try {
            MResponseOperation mResponseOperation = mobileService.doOperation(mOperation);
            if (mResponseOperation != null && !mResponseOperation.getCode().equals("-1") && mResponseOperation.getOperation() != null) {
                mobileService.envoiDesMessage(mResponseOperation.getOperation(), null);
                mobileService.repartirCommission(mResponseOperation.getOperation());
                externalResponse = new ExternalResponse(mResponseOperation.getCode(), mResponseOperation.getMessage(), mResponseOperation.getOperation().getCodeOperation(), mOperation.getMontant().toString());
            } else {
                externalResponse = new ExternalResponse("-1", mResponseOperation.getMessage(), null, null);
            }

            return externalResponse;
        } catch (Exception e) {
            return externalResponse;
        }

    }

    private ExternalResponse showMessage(String code, String message, String transaction) {
        return new ExternalResponse(code, message, transaction, null);
    }

    public Message generateAndSendCodeOTP(String codePays, String telephone) {
        String[] otpAndSecret = RandomUtil.generateOtp(DELAIS);
        Message message = new Message();
        try {

            if (otpAndSecret.length >= 1) {
                String secret = otpAndSecret[0];
                String otp = otpAndSecret[1];
                ExternalOTP ExternalOTP = new ExternalOTP();
                //Initialisation de l'objct externalOTP
                ExternalOTP.setCodePays(codePays);
                ExternalOTP.setTelephone(telephone);
                ExternalOTP.setSecret(secret);
                ExternalOTP.setOtp(otp);
                System.out.println("CODE OTP FOUND" + otp);
                externalOTPRepository.save(ExternalOTP);
                apiService.envoiSms(codePays + telephone, "Votre code de validation est: " + otp,"OTP",codePays);
                System.out.println(ExternalOTP.toString());
                message.setMsg("Opération éffectuée avec succès !");
                message.setText("Code OTP envoyé au numero : " + codePays + telephone);
            } else {
                message.setMsg("Echec de d'envoi du code OTP !");
                message.setCodeErr("-1");
            }
        } catch (Exception e) {

        }

        return message;
    }
    public Message generateAndSendCodeOTP(String codePays, String telephone, String numeroCompte) {
        String[] otpAndSecret = RandomUtil.generateOtp(DELAIS);
        Message message = new Message();
        try {
            if (otpAndSecret.length >= 1) {
                String secret = otpAndSecret[0];
                String otp = otpAndSecret[1];
                ExternalOTP externalOTP = new ExternalOTP();
                externalOTP.setCodePays(codePays);
                externalOTP.setIdentifiant(numeroCompte);
                externalOTP.setTelephone(telephone);
                externalOTP.setSecret(secret);
                externalOTP.setOtp(otp);
                System.out.println("CODE OTP FOUND" + otp);
                externalOTPRepository.save(externalOTP);
                message.setMsg("Opération éffectuée avec succès !");
                message.setText("Code OTP envoyé au numero : " + codePays + telephone);
            } else {
                message.setMsg("Echec de d'envoi du code OTP !");
                message.setCodeErr("-1");
            }
        } catch (Exception e) {

        }

        return message;
    }

    public Boolean verifyOTP(String codePays, String telephone, String otp) {
        ExternalOTP externalOTP = externalOTPRepository.findOTPValide(codePays, telephone, otp);
        AtomicReference<Boolean> retour = new AtomicReference<>(false);
        if (externalOTP != null) {
            System.out.println("CODE OTP FOUND");
            if (externalOTP.getOtp().equals(otp) && RandomUtil.verifyOtp(externalOTP.getSecret(), otp, DELAIS)) {
                externalOTP.setValide(false);
                externalOTP.setSecret("xxx");
                externalOTPRepository.save(externalOTP);
                retour.set(true);
            }

        }
        return retour.get();
    }
    
    public Boolean verifyOTP(String codePays, String telephone, String otp, String identifiant) {
        ExternalOTP externalOTP = externalOTPRepository.findOTPValide(codePays, telephone, otp);
        AtomicReference<Boolean> retour = new AtomicReference<>(false);
        if (externalOTP != null) {
            System.out.println("CODE OTP FOUND");
            if (externalOTP.getOtp().equals(otp) && RandomUtil.verifyOtp(externalOTP.getSecret(), otp, DELAIS)) {
                externalOTP.setValide(false);
                externalOTP.setSecret("xxx");
                externalOTPRepository.save(externalOTP);
                retour.set(true);
            }

        }
        return retour.get();
    }

    public Boolean verifyOTPxxxxxxx(String codePays, String telephone, String otp) {
        List<ExternalOTP> externalOTPS = externalOTPRepository.findAllByCodePaysAndTelephoneAndValide(codePays, telephone, true);
        AtomicReference<Boolean> retour = new AtomicReference<>(false);
        if (externalOTPS != null) {
            System.out.println("CODE OTP FOUND");
            externalOTPS.forEach(externalOTP -> {
                externalOTP.setValide(false);
                externalOTPRepository.save(externalOTP);
                if (externalOTP.getOtp().equals(otp) && RandomUtil.verifyOtp(externalOTP.getSecret(), otp, DELAIS)) {
                    retour.set(true);
                }
            });

        }
        return retour.get();
    }
    @Transactional(readOnly = true)
    public Page<ClientDTO> getAllClientPageOptimise(String phoneNumber, String lastName, String firstName, String cardNumber, String last4Digit, String type, Pageable pageable) {
        List<ClientDTO> pclient = null;
        Long taille = new Long(pageable.getPageSize());
        Long page = new Long(pageable.getPageSize() * pageable.getPageNumber());
        int totalClient = 0;

        if(type.equals("COMO")){
            if (cardNumber != null && !cardNumber.trim().isEmpty()) {
                // cardNumber porte ici le walletNumber (valeur_compte) pour Coris Money
                totalClient = clientRepository.countClientPhysiqueWalletByCriteria(null, phoneNumber, null, lastName, firstName, cardNumber, null, null);
                pclient = clientRepository.findClientPhysiqueByWalletCriteriaOptimise(null, phoneNumber, null, lastName, firstName, cardNumber, taille, page);
            } else {
                totalClient = clientRepository.countClientPhysiqueByCriteria(null,phoneNumber,null,lastName, firstName,null,null);
                pclient = clientRepository.findClientPhysiqueByCriteriaOptimise(null,phoneNumber,null, lastName, firstName,taille, page);
            }
        } else {
            totalClient = clientRepository.countClientPhysiqueCarteByCriteria(null, phoneNumber, null, lastName, firstName, cardNumber, last4Digit, null, null);
            pclient = clientRepository.findClientPhysiqueByCarteCriteriaOptimise(null, phoneNumber, null, lastName, firstName, cardNumber, last4Digit, taille, page);
        }
        if (pclient == null) {
            pclient = new ArrayList<>();
        }
        return new PageImpl<>(pclient, pageable, totalClient);
    }

    public List<Customers> getAllDTOClientExternal(Page<ClientDTO> pclient, String walletNumber,String type) {
        System.out.println("Devbut du Count-------");
        List<Customers> result = new ArrayList<>();
        if (pclient != null) {
            System.out.println("Count-------" + pclient.getNumberOfElements());
            pclient.forEach(client -> {
                boolean matchesFilter;
                if (walletNumber == null || walletNumber.trim().isEmpty()) {
                    matchesFilter = true;
                } else if ("COMO".equals(type)) {
                    matchesFilter = clientService.clientCompteContainValeur(client, walletNumber);
                } else {
                    matchesFilter = clientService.clientPiecesContainRapport(client, walletNumber);
                }
                if (matchesFilter) {
                    result.add(clientService.getClientAllDTOExternal(client, null,type));
                }
            });
        }
        return result;
    }

  
}
