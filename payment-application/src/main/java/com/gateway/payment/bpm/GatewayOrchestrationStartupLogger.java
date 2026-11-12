package com.gateway.payment.bpm;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GatewayOrchestrationStartupLogger {

    private static final Logger log = LoggerFactory.getLogger(GatewayOrchestrationStartupLogger.class);

    private final GatewayOrchestrationProperties properties;

    public GatewayOrchestrationStartupLogger(GatewayOrchestrationProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void logMode() {
        if (properties.isFlowable()) {
            log.info(
                    "Orchestration checkout = FLOWABLE. "
                            + "Chaque PaymentFlow avec bpmProcessKey démarre la définition Flowable "
                            + "(sinon fallback PaymentEngine).");
        } else {
            log.info("Orchestration checkout = ENGINE (PaymentEngine / étapes DB).");
        }
    }
}
