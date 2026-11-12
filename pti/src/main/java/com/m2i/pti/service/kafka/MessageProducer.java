/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.kafka;

import com.m2i.pti.domain.Notification;
import com.m2i.pti.mobile.dto.InstructionComptableExecuteDTO;
import com.m2i.pti.mobile.dto.OperationCardlessDTO;

import com.m2i.pti.mobile.dto.LigneChargementTransfertDTO;
import com.m2i.pti.service.dto.*;
import com.m2i.pti.mobile.dto.OperationSuiviClientDTO;
import java.util.concurrent.CompletableFuture;
import org.springframework.kafka.support.SendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.m2i.pti.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import com.m2i.pti.mobile.dto.ListOperationCardlessDTO;

/**
 *
 * @author cda
 */
@Service
public class MessageProducer {

    @Value(value = "${tenant.numero}")

    private String numeroTenant;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private KafkaTemplate<String, OperationReglDto> operationReglementKafkaTemplate;
    @Autowired
    private KafkaTemplate<String, OperationSuiviClientDTO> kafkaOperationTemplate;
    @Autowired
    private KafkaTemplate<String, OperationCommissionDTO> kafkaCommissionTemplate;

    @Autowired
    private KafkaTemplate<String, Notification> otpnotificationKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, Notification> notificationKafkaTemplate;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private KafkaTemplate<String, ListOperationCardlessDTO> operationCardlessKafkaTemplate;
    @Autowired
    private KafkaTemplate<String, OperationAmbigueDTO> operationAmbigueKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, CompteEventDTO> compteEventTemplate;

    @Autowired
    private KafkaTemplate<String, ActionEventDTO> actionEventTemplate;

    @Autowired
    private KafkaTemplate<String, LigneChargementTransfertDTO> transfertEventTemplate;

    @Autowired
    private KafkaTemplate<String, OperationCardlessDTO> operationVerifCardlessTemplate;

    @Autowired
    private KafkaTemplate<String, InstructionComptableExecuteDTO> instructionTemplate;

    @Autowired
    private KafkaTemplate<String, UserToUpdate> actionUpdateUserTelephoneKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, OperationAuditEventDTO> operationAuditEventTemplate;
 @Autowired
    private KafkaTemplate<String, OperationControlDTO> operationExtourneTemplate;
    public void sendMessage(String message, String topicName) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topicName, message);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.out.println("Unable to send message=[" + message + "] due to : " + ex.getMessage());
            } else {
                System.out.println("Sent message=[" + message + "] with offset=[" + result.getRecordMetadata()
                        .offset() + "]");
            }
        });
    }

    public void sendMessageCardless(ListOperationCardlessDTO message) {
        String topicName = "cardless" + numeroTenant;
        CompletableFuture<SendResult<String, ListOperationCardlessDTO>> future = operationCardlessKafkaTemplate.send(topicName, message);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.out.println("Unable to send message=[" + message + "] due to : " + ex.getMessage());
            } else {
                System.out.println("Sent message=[" + message + "] with offset=[" + result.getRecordMetadata()
                        .offset() + "]");
            }
        });;
    }

    public void sendMessageOperation(OperationSuiviClientDTO op) {
        String topicName = "followme" + numeroTenant;
        CompletableFuture<SendResult<String, OperationSuiviClientDTO>> future = kafkaOperationTemplate.send(topicName, op);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.out.println("Unable to send message=[" + op + "] due to : " + ex.getMessage());
            } else {
                System.out.println("Sent message=[" + op + "] with offset=[" + result.getRecordMetadata()
                        .offset() + "]");
            }
        });
    }

    public void sendMessageLigneTransfertOperation(LigneChargementTransfertDTO op) {
        String topicName = "lignetransfert" + numeroTenant;
        CompletableFuture<SendResult<String, LigneChargementTransfertDTO>> future = transfertEventTemplate.send(topicName, op);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.out.println("Unable to send message=[" + op + "] due to : " + ex.getMessage());
            } else {
                System.out.println("Sent message=[" + op + "] with offset=[" + result.getRecordMetadata()
                        .offset() + "]");
            }
        });
    }

    public void sendMessageReglementOperation(OperationReglDto op) {
        String topicName = "reglement" + numeroTenant;
        CompletableFuture<SendResult<String, OperationReglDto>> future = operationReglementKafkaTemplate.send(topicName, op);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.out.println("Unable to send message=[" + op + "] due to : " + ex.getMessage());
            } else {
                System.out.println("Sent message=[" + op + "] with offset=[" + result.getRecordMetadata()
                        .offset() + "]");
            }
        });
    }

    
    
    
    public void sendMessageAmbigueOperation(OperationAmbigueDTO op) {
        String topicName = "ambigue" + numeroTenant;
        CompletableFuture<SendResult<String, OperationAmbigueDTO>> future = operationAmbigueKafkaTemplate.send(topicName, op);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.out.println("Unable to send message=[" + op + "] due to : " + ex.getMessage());
            } else {
                System.out.println("Sent message=[" + op + "] with offset=[" + result.getRecordMetadata()
                        .offset() + "]");
            }
        });
    }

    public void sendMessageInstructionComptable(InstructionComptableExecuteDTO op) {
        String topicName = "instructioncomp" + numeroTenant;
        CompletableFuture<SendResult<String, InstructionComptableExecuteDTO>> future = instructionTemplate.send(topicName, op);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.out.println("Unable to send message=[" + op + "] due to : " + ex.getMessage());
            } else {
                System.out.println("Sent message=[" + op + "] with offset=[" + result.getRecordMetadata()
                        .offset() + "]");
            }
        });
    }

    public void sendMessageCommission(OperationCommissionDTO com) {
        String topicName = "commissionnew" + numeroTenant;
        CompletableFuture<SendResult<String, OperationCommissionDTO>> future = kafkaCommissionTemplate.send(topicName, com);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.out.println("Unable to send message=[" + com + "] due to : " + ex.getMessage());
            } else {
                System.out.println("TOPIC:" + topicName);
                System.out.println("Sent message=[" + com + "] with offset=[" + result.getRecordMetadata()
                        .offset() + "]");
            }
        });
    }

    public void sendMessageCommissionParUo(OperationCommissionDTO com) {
        String topicName = "commissionperuo" + numeroTenant;
        CompletableFuture<SendResult<String, OperationCommissionDTO>> future = kafkaCommissionTemplate.send(topicName, com);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.out.println("Unable to send message=[" + com + "] due to : " + ex.getMessage());
            } else {
                System.out.println("TOPIC:" + topicName);
                System.out.println("Sent message=[" + com + "] with offset=[" + result.getRecordMetadata()
                        .offset() + "]");
            }
        });
    }
    
    
     public void sendCashBack(String cashBack) {
        String topicName = "cashback" + numeroTenant;
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topicName, cashBack);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.out.println("Unable to send message=[" + cashBack + "] due to : " + ex.getMessage());
            } else {
                System.out.println("Sent message=[" + cashBack + "] with offset=[" + result.getRecordMetadata()
                        .offset() + "]");
            }
        });
    }
     
     
 public void sendOperationExtourne(OperationControlDTO operation) {
        String topicName = "extourne" + numeroTenant;
        CompletableFuture<SendResult<String, OperationControlDTO>> future = operationExtourneTemplate.send(topicName, operation);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.out.println("Unable to send message=[" + operation + "] due to : " + ex.getMessage());
            } else {
                System.out.println("Sent message=[" + operation + "] with offset=[" + result.getRecordMetadata()
                        .offset() + "]");
            }
        });
    }
    public void sendMessageCompteEvent(CompteEventDTO op) {
        String topicName = "compteevent" + numeroTenant;
        CompletableFuture<SendResult<String, CompteEventDTO>> future = compteEventTemplate.send(topicName, op);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.out.println("Unable to send message=[" + op + "] due to : " + ex.getMessage());
            } else {
                System.out.println("Sent message=[" + op + "] with offset=[" + result.getRecordMetadata()
                        .offset() + "]");
            }
        });
    }

    public void sendMessageOperationAuditEvent(OperationAuditEventDTO op) {
        String topicName = "operationauditevent" + numeroTenant;
        CompletableFuture<SendResult<String, OperationAuditEventDTO>> future = operationAuditEventTemplate.send(topicName, op);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.out.println("Unable to send message=[" + op + "] due to : " + ex.getMessage());
            } else {
                System.out.println("Sent message=[" + op + "] with offset=[" + result.getRecordMetadata()
                        .offset() + "]");
            }
        });
    }

    public void sendMessageActionEvent(ActionEventDTO op) {
        String topicName = "actionevent" + numeroTenant;
        CompletableFuture<SendResult<String, ActionEventDTO>> future = actionEventTemplate.send(topicName, op);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                System.out.println("Unable to send message=[" + op + "] due to : " + ex.getMessage());
            } else {
                System.out.println("Sent message=[" + op + "] with offset=[" + result.getRecordMetadata()
                        .offset() + "]");
            }
        });
    }

    public void sendNotification(Notification notification, String topicName) {
        topicName = topicName;
        CompletableFuture<SendResult<String, Notification>> future = otpnotificationKafkaTemplate.send(topicName, notification);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                notification.setDelivred(Boolean.FALSE);
                System.out.println("Unable to send message=[" + notification + "] due to : " + ex.getMessage());
            } else {

                System.out.println("Sent message=[" + notification + "] with offset=[" + result.getRecordMetadata()
                        .offset() + "]");
            }
        });
    }

}
