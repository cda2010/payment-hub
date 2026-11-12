package com.gateway.payment.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableConfigurationProperties(GatewayPerformanceProperties.class)
public class OrchestrationPerformanceConfig {

    @Bean(name = "connectorLegExecutor", destroyMethod = "shutdown")
    @ConditionalOnMissingBean(name = "connectorLegExecutor")
    ExecutorService connectorLegExecutor(GatewayPerformanceProperties performance) {
        if (performance.isVirtualThreads() && Runtime.version().feature() >= 21) {
            try {
                return (ExecutorService) Executors.class
                        .getMethod("newVirtualThreadPerTaskExecutor")
                        .invoke(null);
            } catch (ReflectiveOperationException ignored) {
                // fall through
            }
        }
        return Executors.newFixedThreadPool(Math.max(8, performance.getLegExecutorThreads()));
    }
}
