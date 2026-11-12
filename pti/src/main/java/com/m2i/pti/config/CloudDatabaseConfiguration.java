package com.m2i.pti.config;

import io.github.jhipster.config.JHipsterConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * L'ancien connecteur Spring Cloud Connectors n'existe plus en Boot 3.
 * La datasource vient de application-dev.yml / application-prod*.yml.
 */
@Configuration
@Profile(JHipsterConstants.SPRING_PROFILE_CLOUD)
public class CloudDatabaseConfiguration {
}
