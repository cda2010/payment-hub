package com.gateway.payment.inbox.kafka;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(InboxKafkaProperties.class)
public class InboxKafkaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PartyActionCodec partyActionCodec() {
        return new PartyActionCodec();
    }

    @Bean
    @ConditionalOnProperty(prefix = "inbox.kafka", name = "enabled", havingValue = "true")
    public PartyActionPublisher kafkaPartyActionPublisher(InboxKafkaProperties properties, PartyActionCodec codec) {
        return new KafkaPartyActionPublisher(properties, codec);
    }

    @Bean
    @ConditionalOnProperty(prefix = "inbox.kafka", name = "enabled", havingValue = "false", matchIfMissing = true)
    public PartyActionPublisher noOpPartyActionPublisher() {
        return new NoOpPartyActionPublisher();
    }
}
