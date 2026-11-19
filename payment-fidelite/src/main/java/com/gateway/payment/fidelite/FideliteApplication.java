package com.gateway.payment.fidelite;

import com.gateway.payment.fidelite.security.FideliteSecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(FideliteSecurityProperties.class)
public class FideliteApplication {

    public static void main(String[] args) {
        SpringApplication.run(FideliteApplication.class, args);
    }
}
