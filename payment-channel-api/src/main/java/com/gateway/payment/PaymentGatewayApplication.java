package com.gateway.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Gateway + RapidFrame admin (meta/designer).
 * Le runtime paiement ({@code /api/v1/payments|bills|transfers}) reste hors RF.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@ComponentScan(
        basePackages = {"com.gateway.payment", "com.rapidframe"},
        excludeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.REGEX,
                        pattern = "com\\.rapidframe\\.search\\..*"),
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = com.rapidframe.config.RapidFrameDatabaseConfig.class),
                // Conflit de nom de bean avec com.gateway.payment.events.DomainEventPublisher
                @ComponentScan.Filter(
                        type = FilterType.ASSIGNABLE_TYPE,
                        classes = com.rapidframe.event.DomainEventPublisher.class)
        })
@EntityScan(basePackages = {
        "com.gateway.payment",
        "com.rapidframe.meta"
})
@EnableJpaRepositories(basePackages = {
        "com.gateway.payment",
        "com.rapidframe.meta"
})
@EnableJpaAuditing
@EnableScheduling
public class PaymentGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentGatewayApplication.class, args);
    }
}
