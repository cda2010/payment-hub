/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.config.kafka;

import com.m2i.pti.domain.LigneChargementTransfert;
import com.m2i.pti.domain.Notification;
import com.m2i.pti.mobile.dto.InstructionComptableExecuteDTO;
import com.m2i.pti.mobile.dto.OperationCardlessDTO;

import com.m2i.pti.mobile.dto.ListOperationCardlessDTO;
import com.m2i.pti.mobile.dto.OperationSuiviClientDTO;
import com.m2i.pti.service.dto.CompteEventDTO;
import com.m2i.pti.service.dto.OperationAmbigueDTO;
import com.m2i.pti.service.dto.OperationCommissionDTO;
import com.m2i.pti.service.dto.OperationReglDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import com.m2i.pti.mobile.dto.LigneChargementTransfertDTO;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import com.m2i.pti.service.dto.ActionEventDTO;
import com.m2i.pti.service.dto.OperationAuditEventDTO;
import com.m2i.pti.service.dto.OperationControlDTO;
import com.m2i.pti.service.dto.ParametreDTO;
import org.springframework.kafka.support.serializer.JsonDeserializer;

/**
 *
 * @author cda
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value(value = "${spring.kafka.consumer.bootstrapAddress}")
    private String bootstrapAddress;

    private Map<String, Object> getProps() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 16384);
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 52428800);
        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 1048576);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 1000);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 300000);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 50);

        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        //   props.put(ConsumerConfig., 10000);
        return props;
    }

    @Bean
    public ConsumerFactory<String, String> consumerFactory(String groupId) {
        Map<String, Object> props = new HashMap<>();
        props = getProps();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                groupId);
        props.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        props.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);

        return new DefaultKafkaConsumerFactory<>(props);
    }

    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(String groupId) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AckMode.RECORD);
        factory.setConsumerFactory(consumerFactory(groupId));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> smsKafkaListenerContainerFactory() {
        return kafkaListenerContainerFactory("sms");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> cardlessKafkaListenerContainerFactory() {
        return kafkaListenerContainerFactory("cardless");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> errorKafkaListenerContainerFactory() {
        return kafkaListenerContainerFactory("error");
    }

    public ConsumerFactory<String, CompteEventDTO> compteEventConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props = getProps();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "compteevent");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(CompteEventDTO.class));
    }

     
    
    
    public ConsumerFactory<String, InstructionComptableExecuteDTO> instructionComptableExecuteDTOConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props = getProps();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "instructioncomp");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(InstructionComptableExecuteDTO.class));
    }

    public ConsumerFactory<String, ActionEventDTO> actionEventConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props = getProps();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "actionevent");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(ActionEventDTO.class));
    }

    public ConsumerFactory<String, OperationAuditEventDTO> operationAuditEventConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props = getProps();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "operationauditevent");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(OperationAuditEventDTO.class));
    }

    public ConsumerFactory<String, String> updateTelephoneConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props = getProps();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "actionevent");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(String.class));
    }

    public ConsumerFactory<String, LigneChargementTransfertDTO> compteLigneChargementConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props = getProps();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "lignetransfert");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(LigneChargementTransfertDTO.class));
    }

    public ConsumerFactory<String, OperationSuiviClientDTO> operationConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props = getProps();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "operation");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(OperationSuiviClientDTO.class));
    }

    public ConsumerFactory<String, ListOperationCardlessDTO> operationCardlessConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props = getProps();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "verifcardless");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(ListOperationCardlessDTO.class));
    }
    
     public ConsumerFactory<String, OperationControlDTO> operationExtourneConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props = getProps();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "extourne");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(OperationControlDTO.class));
    }

    public ConsumerFactory<String, OperationCardlessDTO> operationVerifCardlessConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props = getProps();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "cardless");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(OperationCardlessDTO.class));
    }

    public ConsumerFactory<String, OperationAmbigueDTO> operationAmbigueFactory() {
        Map<String, Object> props = new HashMap<>();
        props = getProps();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CustomDeserializer.class);

        props.put(ConsumerConfig.GROUP_ID_CONFIG, "ambigue");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(OperationAmbigueDTO.class));
    }

    public ConsumerFactory<String, OperationReglDto> operationReglementFactory() {

        Map<String, Object> props = new HashMap<>();
        props = getProps();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "reglement");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(OperationReglDto.class));
    }

    public ConsumerFactory<String, OperationCommissionDTO> operationComptaConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "commission");
        //     props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        //   props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 500000);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(OperationCommissionDTO.class));
    }
    
     public ConsumerFactory<String, ParametreDTO> execOperationFactory() {

        Map<String, Object> props = new HashMap<>();
        props = getProps();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "execOperation");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(ParametreDTO.class));
    }
 public ConsumerFactory<String, String> cashBackConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props = getProps();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "cashback");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(String.class));
    }
    public ConsumerFactory<String, OperationCommissionDTO> repartitionCommissionConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props = getProps();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "commissionperuo");
        // props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        //  props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 500000);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(OperationCommissionDTO.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OperationReglDto> reglementKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OperationReglDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AckMode.RECORD);
        factory.getContainerProperties().setSyncCommits(true);
        factory.setConsumerFactory(operationReglementFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OperationAmbigueDTO> operationAmbigueKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OperationAmbigueDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AckMode.RECORD);
        factory.getContainerProperties().setSyncCommits(true);
        factory.setConsumerFactory(operationAmbigueFactory());
        return factory;
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ParametreDTO> execOperationKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ParametreDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AckMode.RECORD);
        factory.getContainerProperties().setSyncCommits(true);
        factory.setConsumerFactory(execOperationFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ListOperationCardlessDTO> operationCardlessKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ListOperationCardlessDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AckMode.RECORD);
        factory.getContainerProperties().setSyncCommits(true);
        factory.setConsumerFactory(operationCardlessConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CompteEventDTO> compteEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CompteEventDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AckMode.RECORD);
        factory.getContainerProperties().setSyncCommits(true);
        factory.setConsumerFactory(compteEventConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InstructionComptableExecuteDTO> instructionComptableKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, InstructionComptableExecuteDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AckMode.RECORD);
        factory.getContainerProperties().setSyncCommits(true);
        factory.setConsumerFactory(instructionComptableExecuteDTOConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OperationAuditEventDTO> operationAuditEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OperationAuditEventDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AckMode.RECORD);
        factory.getContainerProperties().setSyncCommits(true);
        factory.setConsumerFactory(operationAuditEventConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ActionEventDTO> actionEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ActionEventDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AckMode.RECORD);
        factory.getContainerProperties().setSyncCommits(true);
        factory.setConsumerFactory(actionEventConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> updateTelephoneKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AckMode.RECORD);
        factory.getContainerProperties().setSyncCommits(true);
        factory.setConsumerFactory(updateTelephoneConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, LigneChargementTransfertDTO> chargementTransfertEventKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, LigneChargementTransfertDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AckMode.RECORD);
        factory.getContainerProperties().setSyncCommits(true);
        factory.setConsumerFactory(compteLigneChargementConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OperationCardlessDTO> operationVerifCardlessKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OperationCardlessDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AckMode.RECORD);
        factory.getContainerProperties().setSyncCommits(true);
        factory.setConsumerFactory(operationVerifCardlessConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OperationSuiviClientDTO> operationKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OperationSuiviClientDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AckMode.RECORD);
        factory.getContainerProperties().setSyncCommits(true);
        factory.setConsumerFactory(operationConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OperationCommissionDTO> operationComptaKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OperationCommissionDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AckMode.RECORD);
        factory.getContainerProperties().setSyncCommits(true);

        factory.setConsumerFactory(operationComptaConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OperationCommissionDTO> repartitionCommissionKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OperationCommissionDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AckMode.RECORD);
        factory.getContainerProperties().setSyncCommits(true);
        factory.setConsumerFactory(repartitionCommissionConsumerFactory());

        return factory;
    }

    public ConsumerFactory<String, Notification> notificationConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "sms");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(Notification.class));
    }

    public ConsumerFactory<String, Notification> otpNotificationConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "otp");
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(Notification.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Notification> notificationOTPKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Notification> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AckMode.RECORD);
        factory.getContainerProperties().setSyncCommits(true);
        factory.setConsumerFactory(otpNotificationConsumerFactory());
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Notification> notificationKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Notification> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AckMode.RECORD);
        factory.getContainerProperties().setSyncCommits(true);
        factory.setConsumerFactory(notificationConsumerFactory());
        return factory;
    }
    
      @Bean
    public ConcurrentKafkaListenerContainerFactory<String, OperationControlDTO> operationExtourneKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OperationControlDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AckMode.RECORD);
        factory.getContainerProperties().setSyncCommits(true);
        factory.setConsumerFactory(operationExtourneConsumerFactory());
        return factory;
    }
    
        @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> cashbackKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.getContainerProperties().setAckMode(AckMode.RECORD);
        factory.getContainerProperties().setSyncCommits(true);
        factory.setConsumerFactory(cashBackConsumerFactory());
        return factory;
    }
   
}
