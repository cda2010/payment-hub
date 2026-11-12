package com.gateway.payment.divers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.gateway.payment.divers.security.DiversSecurityProperties;

@SpringBootApplication
@EnableConfigurationProperties(DiversSecurityProperties.class)
public class DiversApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiversApplication.class, args);
    }
}
