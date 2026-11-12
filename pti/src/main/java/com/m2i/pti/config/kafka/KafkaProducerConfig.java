/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.config.kafka;

import com.m2i.pti.domain.LigneChargementTransfert;
import com.m2i.pti.domain.Notification;
import com.m2i.pti.mobile.dto.LigneChargementTransfertDTO;

import com.m2i.pti.mobile.dto.ListOperationCardlessDTO;
import com.m2i.pti.mobile.dto.OperationCardlessDTO;
import com.m2i.pti.service.dto.*;
import com.m2i.pti.mobile.dto.OperationSuiviClientDTO;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.support.serializer.JsonSerializer;
import com.m2i.pti.mobile.dto.InstructionComptableExecuteDTO;

/**
 *
 * @author cda
 */
@EnableKafka
@Configuration
public class KafkaProducerConfig {
    @Value(value = "${spring.kafka.producer.bootstrapAddress}")
    private String bootstrapAddress;
    private Map<String, Object> getConfig() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "-1");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, "100000");
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "43554432");
        //   props.put(ConsumerConfig., 10000);
        return configProps;
    }

    @Bean
    public ProducerFactory<String, Notification> otpNotificationProducerFactory() {
        Map<String, Object> configProps = getConfig();
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "-1");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32 * 1024));
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, "20");
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "43554432");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    public ProducerFactory<String, OperationSuiviClientDTO> operationProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "-1");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, "100000");
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "43554432");
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    @Bean
    public ProducerFactory<String, OperationControlDTO> operationExtourneProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "-1");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, "100000");
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "43554432");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ProducerFactory<String, ListOperationCardlessDTO> operationCardlessProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "-1");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, "100000");
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "43554432");

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ProducerFactory<String, LigneChargementTransfertDTO> operationChargementFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "-1");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, "100000");
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "43554432");

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ProducerFactory<String, OperationCardlessDTO> operationVerifCardlessProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "-1");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, "100000");
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "43554432");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ProducerFactory<String, OperationReglDto> operationReglementFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "-1");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, "100000");
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "43554432");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ProducerFactory<String, OperationAmbigueDTO> operationAmbigueFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CustomSerializer.class);
        //     configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "-1");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, "16384");
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "43554432");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ProducerFactory<String, OperationCommissionDTO> operationCommissionProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "-1");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, "100000");
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "43554432");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ProducerFactory<String, Notification> notificationProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "-1");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, "100000");
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "43554432");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ProducerFactory<String, CompteEventDTO> compteEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "-1");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, "100000");
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "43554432");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    
     @Bean
    public ProducerFactory<String, OperationAuditEventDTO> operationAuditEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "-1");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, "100000");
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "43554432");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    
     @Bean
    public ProducerFactory<String, InstructionComptableExecuteDTO> instructionComptableProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "-1");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, "100000");
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "43554432");
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    @Bean
    public ProducerFactory<String, ActionEventDTO> actionEventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "-1");
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, "100000");
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "43554432");
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ProducerFactory<String, UserToUpdate> actionUpdateUserTelephoneProducerFactory() {
        Map<String, Object> configProps = getConfig();
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Notification> notificationKafkaTemplate() {
        return new KafkaTemplate<>(notificationProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, Notification> otpnotificationKafkaTemplate() {
        return new KafkaTemplate<>(otpNotificationProducerFactory());
    }
    
    
    @Bean
    public KafkaTemplate<String, OperationSuiviClientDTO> operationKafkaTemplate() {
        return new KafkaTemplate<>(operationProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, OperationCommissionDTO> operationCommissionKafkaTemplate() {
        return new KafkaTemplate<>(operationCommissionProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, ListOperationCardlessDTO> operationCardlessKafkaTemplate() {
        return new KafkaTemplate<>(operationCardlessProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, OperationReglDto> operationReglementKafkaTemplate() {
        return new KafkaTemplate<>(operationReglementFactory());
    }

    @Bean
    public KafkaTemplate<String, OperationAmbigueDTO> operationAmbigueKafkaTemplate() {
        return new KafkaTemplate<>(operationAmbigueFactory());
    }

    @Bean
    public KafkaTemplate<String, OperationCardlessDTO> operationVerifCardlessTemplate() {
        return new KafkaTemplate<>(operationVerifCardlessProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, CompteEventDTO> compteEventTemplate() {
        return new KafkaTemplate<>(compteEventProducerFactory());
    }
@Bean
    public KafkaTemplate<String, OperationAuditEventDTO> operationAuditEventTemplate() {
        return new KafkaTemplate<>(operationAuditEventProducerFactory());
    }
    
    @Bean
    public KafkaTemplate<String, InstructionComptableExecuteDTO> instructionTemplate() {
        return new KafkaTemplate<>(instructionComptableProducerFactory());
    }
    
    
    @Bean
    public KafkaTemplate<String, ActionEventDTO> actionEventTemplate() {
        return new KafkaTemplate<>(actionEventProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, LigneChargementTransfertDTO> transfertEventTemplate() {
        return new KafkaTemplate<>(operationChargementFactory());
    }

    @Bean
    public KafkaTemplate<String, UserToUpdate> actionUpdateUserTelephoneKafkaTemplate() {
        return new KafkaTemplate<>(actionUpdateUserTelephoneProducerFactory());
    }

   @Bean
    public KafkaTemplate<String, OperationControlDTO> extourneExecOperationKafkaTemplate() {
        return new KafkaTemplate<>(operationExtourneProducerFactory());
    }




}
