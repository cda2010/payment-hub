/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.kafka;

import com.m2i.pti.domain.ActionEvent;
import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.CompteEvent;
import com.m2i.pti.domain.InstructionComptableRegistre;
import com.m2i.pti.domain.OperationSuivis;
import com.m2i.pti.domain.Notification;
import com.m2i.pti.domain.Operation;
import com.m2i.pti.domain.OperationAuditEvent;
import com.m2i.pti.domain.enumeration.ActionService;

import com.m2i.pti.domain.enumeration.ActionEnum;
import com.m2i.pti.domain.enumeration.InstructionEcriture;
import com.m2i.pti.domain.enumeration.StatutOperation;
import com.m2i.pti.domain.enumeration.TypeNotification;
import com.m2i.pti.fcm.service.ApiFcmService;
import com.m2i.pti.mobile.dto.InstructionComptableExecuteDTO;
import com.m2i.pti.mobile.dto.LigneChargementTransfertDTO;
import com.m2i.pti.mobile.dto.OperationCardlessDTO;
import com.m2i.pti.mobile.dto.OperationSuiviClientDTO;
import com.m2i.pti.mobile.service.MobileService;
import com.m2i.pti.repository.OperationRepository;
import com.m2i.pti.service.ApiService;
import com.m2i.pti.service.ClientService;
import com.m2i.pti.service.CommissionService;
import com.m2i.pti.service.MailService;
import com.m2i.pti.service.OperationService;
import com.m2i.pti.service.OperationSuivisService;
import com.m2i.pti.service.dto.OperationAmbigueDTO;
import com.m2i.pti.service.dto.OperationCommissionDTO;
import com.m2i.pti.service.dto.OperationDTO;
import com.m2i.pti.mobile.dto.ListOperationCardlessDTO;
import com.m2i.pti.mobile.dto.MLigneChargement;
import com.m2i.pti.repository.ActionEventRepository;
import com.m2i.pti.repository.InstructionComptableRegistreRepository;

import com.m2i.pti.repository.OperationAuditEventRepository;

import com.m2i.pti.service.dto.CompteEventDTO;
import com.m2i.pti.service.dto.OperationReglDto;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.concurrent.CountDownLatch;
import jakarta.xml.bind.JAXBException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.m2i.pti.service.CompteEventService;
import com.m2i.pti.service.pentaho.TransformationChargementCall;
import com.m2i.pti.service.dto.ActionEventDTO;
import java.time.LocalDateTime;
import com.m2i.pti.repository.NotificationRepository;
import com.m2i.pti.service.InstructionComptableService;
import com.m2i.pti.service.dto.OperationAuditEventDTO;
import com.m2i.pti.service.dto.OperationControlDTO;

/**
 *
 * @author cda
 */
@Component

public class MessageListener {
    
    @Value(value = "${tenant.numero}")
    private String numeroTenant;
    ApiFcmService apiFcmService;
    ApiService apiService;
    OperationSuivisService operationSuivisService;
    private final ClientService clientService;
    private final MailService mailService;
    MobileService mobileService;
    private final OperationRepository operationRepository;
    private final OperationService operationService;
    private final CommissionService commissionService;
    private final CompteEventService compteEventService;
    private final TransformationChargementCall transformationChargementCall;
    private final ActionEventRepository actionEventRepository;
    private final NotificationRepository notificationRepository;
    InstructionComptableRegistreRepository instructionComptableRegistreRepository;
    InstructionComptableService instructionComptableService;
    OperationAuditEventRepository operationAuditEventRepository;

    public MessageListener(OperationAuditEventRepository operationAuditEventRepository, InstructionComptableService instructionComptableService, InstructionComptableRegistreRepository instructionComptableRegistreRepository, NotificationRepository notificationRepository, ActionEventRepository actionEventRepository, TransformationChargementCall transformationChargementCall, CompteEventService compteEventService, MobileService mobileService, OperationService operationService, CommissionService commissionService, ApiFcmService apiFcmService, MailService mailService, ApiService apiService, OperationSuivisService operationSuivisService, ClientService clientService, OperationRepository operationRepository) {
        this.apiService = apiService;
        this.operationSuivisService = operationSuivisService;
        this.clientService = clientService;
        this.mailService = mailService;
        this.mobileService = mobileService;
        this.operationRepository = operationRepository;
        this.operationService = operationService;
        this.compteEventService = compteEventService;
        this.apiFcmService = apiFcmService;
        this.commissionService = commissionService;
        this.transformationChargementCall = transformationChargementCall;
        this.actionEventRepository = actionEventRepository;
        this.notificationRepository = notificationRepository;
        this.instructionComptableRegistreRepository = instructionComptableRegistreRepository;
        this.instructionComptableService = instructionComptableService;
        this.operationAuditEventRepository = operationAuditEventRepository;
    }
    
    private final CountDownLatch latch = new CountDownLatch(3);
    private CountDownLatch partitionLatch = new CountDownLatch(2);
    private CountDownLatch filterLatch = new CountDownLatch(2);
    private CountDownLatch greetingLatch = new CountDownLatch(1);
    
    private OperationSuivis createOpertionSuivi(OperationSuiviClientDTO operationSuiviClientDTO) {
        
        Client c = clientService.findClient(operationSuiviClientDTO.getClientId());
        Operation op = operationRepository.findByOperationId(operationSuiviClientDTO.getOperationId());
        OperationSuivis opsuivi = new OperationSuivis();
        opsuivi.setOperation(op);
        opsuivi.setClient(c);
        return opsuivi;
    }
    
    @KafkaListener(topics = "followme" + "${tenant.numero}", containerFactory = "operationKafkaListenerContainerFactory", groupId = "operation")
    public void listenGroupOperation(OperationSuiviClientDTO operationSuivis) {
        operationSuivisService.save(createOpertionSuivi(operationSuivis));
        latch.countDown();
    }

//    @KafkaListener(topics = "sms5555", containerFactory = "smsKafkaListenerContainerFactory")
//    public void listenGroupSimpleMessage(String message) {
//        System.out.println("Received Messasge in group 'sms': " + message);
//        //     apiService.sendSms("22676477115",message);
//        latch.countDown();
//    }
    @KafkaListener(topics = "error" + "${tenant.numero}", containerFactory = "errorKafkaListenerContainerFactory")
    public void listenGroupErrorMessage(String message) {
        System.out.println("Received Messasge in group 'sms': " + message);
        //     apiService.sendSms("22676477115",message);
        latch.countDown();
    }
    
    @KafkaListener(topics = "cardless" + "${tenant.numero}", containerFactory = "operationCardlessKafkaListenerContainerFactory")
    public void listenGroupVerifCardlessMessage(ListOperationCardlessDTO message) throws IOException, NoSuchAlgorithmException, JAXBException {
        System.out.println("Received Messasge in group 'cardless': " + message);
        for (OperationCardlessDTO item : message.getOperationCardlessDTO()) {
            mobileService.cardlessTransactionSetllement(item);
        }

        //     apiService.sendSms("22676477115",message);
        latch.countDown();
    }
    
    @KafkaListener(topics = "lignetransfert" + "${tenant.numero}", containerFactory = "chargementTransfertEventKafkaListenerContainerFactory")
    public void listenGroupLigneChargementMessage(LigneChargementTransfertDTO message) throws IOException, NoSuchAlgorithmException, JAXBException {
        System.out.println("Received Messasge in group 'lignetransfert': " + message.getLigneChargementTransfert());
        
        executeTransfert(message.getLigneChargementTransfert(), message.getOperId(), message.getCompteDebit());

        //    message.getLigneChargementTransfertDTO().stream().forEach(lcgmt -> executeTransfert(lcgmt, message.getOperId(), message.getCompteDebit()));
        //     apiService.sendSms("22676477115",message);
        latch.countDown();
    }

//    @KafkaListener(topics = "cardless" + "${tenant.numero}", containerFactory = "operationVerifCardlessKafkaListenerContainerFactory")
//    public void listenGroupCardlessMessage(OperationCardlessDTO message) throws IOException, NoSuchAlgorithmException, JAXBException {
//        System.out.println("Received Messasge in group 'cardless': " + message);
//        mobileService.cardlessTransactionSetllement(message) ;//     apiService.sendSms("22676477115",message);
//        latch.countDown();
//    }
    @KafkaListener(topics = "reglement" + "${tenant.numero}", containerFactory = "reglementKafkaListenerContainerFactory")
    public void listenGroupSimpleReglementMessage(OperationReglDto message) throws IOException {
        System.out.println("Received Messasge in group reglement: " + message);
        OperationDTO operationDTO = new OperationDTO();
        operationDTO.setCode(message.getTypeService());
        operationDTO.setLogin(message.getLogin());
        operationDTO.setCanalLibelleCanal(message.getCanal());

//       operationService.doOperationRegelement(operationDTO, message.getUoAcq());
        latch.countDown();
    }
    
   
    
    @KafkaListener(topics = "compteevent" + "${tenant.numero}", containerFactory = "compteEventKafkaListenerContainerFactory")
    public void listenGroupCompteEventMessage(CompteEventDTO message) throws IOException, NoSuchAlgorithmException, JAXBException {
        System.out.println("Received Messasge in group 'comptevent': " + message);
        CompteEvent cptEve = new CompteEvent();
        cptEve.setCompteId(message.getCompteId());
        cptEve.setMontant(message.getMontant());
        cptEve.setInstruction(InstructionEcriture.valueOf(message.getSens()));
        cptEve.setDateOperation(ZonedDateTime.now());
        cptEve.setReferenceOp(message.getReferenceOperation());
        compteEventService.save(cptEve);

        //     apiService.sendSms("22676477115",message);
        latch.countDown();
    }
    
    @KafkaListener(topics = "actionevent" + "${tenant.numero}", containerFactory = "actionEventKafkaListenerContainerFactory")
    public void listenGroupActionEventMessage(ActionEventDTO message) throws IOException, NoSuchAlgorithmException, JAXBException {
        System.out.println("Received Messasge in group 'actionEvent': " + message);
        ActionEvent actEve = new ActionEvent();
        actEve.setAction(ActionEnum.valueOf(message.getAction()));
        actEve.setEntityId(message.getEntityId());
        actEve.setLogin(message.getLogin());
        actEve.setDateAction(LocalDateTime.now());
        actEve.setObjet(message.getObjet());
        actionEventRepository.save(actEve);

        //     apiService.sendSms("22676477115",message);
        latch.countDown();
    }
    
    @KafkaListener(topics = "operationauditevent" + "${tenant.numero}", containerFactory = "operationAuditEventKafkaListenerContainerFactory")
    public void listenGroupOperationAuditEventMessage(OperationAuditEventDTO message) throws IOException, NoSuchAlgorithmException, JAXBException {
        System.out.println("Received Messasge in group 'operationauditevent': " + message);
        OperationAuditEvent optEve = new OperationAuditEvent();
        optEve.setActionService(ActionService.valueOf(message.getActionService()));
        optEve.setReferenceOperation(message.getReferenceOperation());
        optEve.setDateEnregistrement(ZonedDateTime.now());
        optEve.setDescription(message.getDescription());
        optEve.setCodeReponse(message.getCodeReponse());
        optEve.setIdentifiant(message.getIdentifiant());
        optEve.setTypeAttribut(message.getTypeAttribut());
        operationAuditEventRepository.save(optEve);

        //     apiService.sendSms("22676477115",message);
        latch.countDown();
    }
    
    @KafkaListener(topics = "ambigue" + "${tenant.numero}", containerFactory = "operationAmbigueKafkaListenerContainerFactory")
    public void listenGroupOperationAmbigueMessage(OperationAmbigueDTO message) throws IOException {
        System.out.println("Received Messasge in group ambigue: " + message);
        OperationDTO operationDTO = operationService.findByCodeOperation(message.getCodeOperation());
        if (operationDTO != null) {
            //   operationService.reversionOperation(operationDTO.getId());
            operationDTO.setStatut(StatutOperation.REJETE);
            operationService.save(operationDTO);
        }

        //     apiService.sendSms("22676477115",message);
        latch.countDown();
    }
     @KafkaListener(topics = "commissionperuo" + "${tenant.numero}", containerFactory = "repartitionCommissionKafkaListenerContainerFactory")
    public void listenGroupSimpleCommissionUoMessage(OperationCommissionDTO message) throws IOException, InterruptedException {
        System.out.println("Received Messasge in group commissionuo: " + message);
        commissionService.repartitionDesCommissionParUo(message.getUoId(), message.getOperationId());

        //     apiService.sendSms("22676477115",message);
        latch.countDown();
    }
    @KafkaListener(topics = "commissionnew" + "${tenant.numero}", containerFactory = "operationComptaKafkaListenerContainerFactory")
    public void listenGroupCommissionMessage(OperationCommissionDTO message) throws IOException {
        System.out.println("Received Messasge in group commission: " + message);
        
        commissionService.repartitionCommissionWithId(message.getUoId(), message.getOperationId());

        //     apiService.sendSms("22676477115",message);
        latch.countDown();
    }
    
    @KafkaListener(topics = "instructioncomp" + "${tenant.numero}", containerFactory = "instructionComptableKafkaListenerContainerFactory")
    public void listenGroupExecuteInstruction(InstructionComptableExecuteDTO message) throws IOException {
        System.out.println("Received Messasge in group instructioncomp: " + message);
        InstructionComptableRegistre instComptable = instructionComptableRegistreRepository.findById(message.getInstructionId()).orElse(null);
        if (instComptable.getIsConsumed() == null || (instComptable.getIsConsumed() == false)) {
            instructionComptableService.comptabiliserOperationWithRegle(instComptable.getOperation(), instComptable.getUoDestAgenceSiege(), instComptable.getUoSrcAgenceSiege(), instComptable.getRegleCommission());
        }
        instComptable.setIsConsumed(Boolean.TRUE);
        instructionComptableRegistreRepository.save(instComptable);
        //  commissionService.repartitionCommissionWithId(message.getUoId(), message.getOperationId());
        //     apiService.sendSms("22676477115",message);
        latch.countDown();
    }
    
    
     @KafkaListener(topics = "cashback" + "${tenant.numero}", containerFactory = "cashbackKafkaListenerContainerFactory")
    public void listenGroupCashExecuteOperation(String codeOperation) throws IOException {
        System.out.println("Received Messasge in group operation: " + codeOperation);
        Long opId = Long.parseLong(codeOperation);
        Operation op = operationRepository.findByOperationId(opId);
        mobileService.doOperationCashBack(op);
        //   apiService.sendRequestComptabilisation(message);
        latch.countDown();
    }
    
     @KafkaListener(topics = "extourne" + "${tenant.numero}", containerFactory = "operationExtourneKafkaListenerContainerFactory")
    public void listenGroupExtourneExecuteOperation(OperationControlDTO operationControlDTO) throws IOException, JAXBException {
        System.out.println("Received Messasge in group operation: " + operationControlDTO.getCodeOperation());
      boolean bool=  mobileService.doOperationExtourne(operationControlDTO);
        OperationDTO operationDTO = operationService.findByCodeOperation(operationControlDTO.getCodeOperation());
        if (bool && operationDTO != null) {
            //   operationService.reversionOperation(operationDTO.getId());
           // mobileService.doOperationExtourne(operationControlDTO);
            operationDTO.setStatut(StatutOperation.ANNULLE);
            operationService.save(operationDTO);
        }
        //   apiService.sendRequestComptabilisation(message);
        latch.countDown();
    }

    
    
    @KafkaListener(topics = "otp", containerFactory = "notificationOTPKafkaListenerContainerFactory")
    public void listenGroupNotficationOtp(Notification notification) throws IOException, JAXBException {
        System.out.println("Received Messasge in group 'otp': " + notification);
        if (notification.getTypeNotification().equals(TypeNotification.OTP)) {
            System.out.println("***************Send otp*************");
            apiService.envoiSms(notification.getDestinataire(), notification.getContenu(),"NOTIFICATION",notification.getCodeIndicatif());
        } else if (notification.getTypeNotification().equals(TypeNotification.EMAIL)) {
            /* On utilise le système mail de coris money burkina**/
            apiService.envoiMail(notification.getDestinataire(), notification.getContenu(), "Message systeme");
        }
        latch.countDown();
    }
    
    @KafkaListener(topics = "sms", containerFactory = "notificationKafkaListenerContainerFactory")
    public void listenGroupNotfication(Notification notification) throws IOException, JAXBException {
        System.out.println("Received Messasge in group 'sms': " + notification);
        Notification notif = notificationRepository.findById(notification.getId()).orElse(null);
        // if (notif.getIsConsumed() == null || (notif.getIsConsumed() == false)) {
        if (notification.getTypeNotification().equals(TypeNotification.SMS)) {
            System.out.println("***************Send sms*************");
            apiService.envoiSms(notification.getDestinataire(), notification.getContenu(),"NOTIFICATION",notification.getCodeIndicatif());
            //   apiService.sendSms(notification.getDestinataire(), notification.getContenu());
        } else if (notification.getTypeNotification().equals(TypeNotification.EMAIL)) {
            /* On utilise le système mail de coris money burkina**/
            apiService.envoiMail(notification.getDestinataire(), notification.getContenu(), "Message systeme");
        } else if (notification.getTypeNotification().equals(TypeNotification.MESSAGEPUSH)) {
            //  apiFcmService.sendFcmMessage(notification.contenu("Coris Money",notification.getContenu()));
        }
        // }
        notif.setIsConsumed(Boolean.TRUE);
        notif.setDateConsumession(LocalDateTime.now());
        notificationRepository.save(notif);
        latch.countDown();
    }
    
    private void executeTransfert(MLigneChargement lgDto, Long operId, Long compteId) {
        try {
            transformationChargementCall.chargementLigne(lgDto, compteId, operId);
        } catch (Exception e) {
            
        }
        
    }
}
