package com.gateway.payment.spend.kafka;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(SpendKafkaProperties.class)
public class SpendKafkaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SpendCodec spendCodec() {
        return new SpendCodec();
    }

    @Bean
    @ConditionalOnProperty(prefix = "spend.kafka", name = "enabled", havingValue = "true")
    public SpendPublisher kafkaSpendPublisher(SpendKafkaProperties properties, SpendCodec codec) {
        return new KafkaSpendPublisher(properties, codec);
    }

    @Bean
    @ConditionalOnProperty(prefix = "spend.kafka", name = "enabled", havingValue = "false", matchIfMissing = true)
    public SpendPublisher noOpSpendPublisher() {
        return new NoOpSpendPublisher();
    }
}
