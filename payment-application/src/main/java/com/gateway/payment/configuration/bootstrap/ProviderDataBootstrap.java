package com.gateway.payment.configuration.bootstrap;

import com.gateway.payment.configuration.config.GatewayBootstrapProperties;
import com.gateway.payment.configuration.service.OmnicanalConnectivityMigrationService;
import com.gateway.payment.configuration.service.ProviderTemplateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(20)
@ConditionalOnProperty(prefix = "gateway.bootstrap", name = "providers", havingValue = "true")
public class ProviderDataBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProviderDataBootstrap.class);

    private final ProviderTemplateService templateService;
    private final OmnicanalConnectivityMigrationService omnicanalMigrationService;
    private final GatewayBootstrapProperties bootstrapProperties;

    public ProviderDataBootstrap(
            ProviderTemplateService templateService,
            OmnicanalConnectivityMigrationService omnicanalMigrationService,
            GatewayBootstrapProperties bootstrapProperties
    ) {
        this.templateService = templateService;
        this.omnicanalMigrationService = omnicanalMigrationService;
        this.bootstrapProperties = bootstrapProperties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!bootstrapProperties.providers()) {
            return;
        }
        templateService.seedDemoProviders();
        omnicanalMigrationService.seedBundledFixtures();
        log.info("Fournisseurs de démonstration initialisés (gateway.bootstrap.providers=true)");
    }
}
