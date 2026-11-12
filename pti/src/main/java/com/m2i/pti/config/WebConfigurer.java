package com.m2i.pti.config;

import io.github.jhipster.config.JHipsterConstants;
import io.github.jhipster.config.JHipsterProperties;
import io.github.jhipster.web.filter.CachingHttpHeadersFilter;

import com.codahale.metrics.MetricRegistry;

import io.undertow.UndertowOptions;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Configuration
public class WebConfigurer implements ServletContextInitializer, WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    private final Logger log = LoggerFactory.getLogger(WebConfigurer.class);

    private final Environment env;

    private final JHipsterProperties jHipsterProperties;

    private MetricRegistry metricRegistry;

    public WebConfigurer(Environment env, JHipsterProperties jHipsterProperties) {
        this.env = env;
        this.jHipsterProperties = jHipsterProperties;
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        if (env.getActiveProfiles().length != 0) {
            log.info("Web application configuration, using profiles: {}", (Object[]) env.getActiveProfiles());
        }
        EnumSet<DispatcherType> disps = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC);
        initMetrics(servletContext, disps);
        if (env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_PRODUCTION))) {
            initCachingHttpHeadersFilter(servletContext, disps);
        }
        log.info("Web application fully configured");
    }

    @Override
    public void customize(ConfigurableServletWebServerFactory container) {
        MimeMappings mappings = new MimeMappings(MimeMappings.DEFAULT);
        mappings.add("html", "text/html;charset=utf-8");
        mappings.add("json", "application/json;charset=utf-8");
        container.setMimeMappings(mappings);
        setLocationForStaticAssets(container);

        if (jHipsterProperties.getHttp().getVersion().equals(JHipsterProperties.Http.Version.V_2_0)
                && container instanceof UndertowServletWebServerFactory undertow) {
            undertow.addBuilderCustomizers(builder ->
                    builder.setServerOption(UndertowOptions.ENABLE_HTTP2, true));
        }
    }

    private void setLocationForStaticAssets(ConfigurableServletWebServerFactory container) {
        File root;
        String prefixPath = resolvePathPrefix();
        if (env.acceptsProfiles(Profiles.of(JHipsterConstants.SPRING_PROFILE_PRODUCTION))) {
            root = new File(prefixPath + "target/www/");
        } else {
            root = new File(prefixPath + "src/main/webapp/");
        }
        if (root.exists() && root.isDirectory()) {
            container.setDocumentRoot(root);
        }
    }

    private String resolvePathPrefix() {
        String fullExecutablePath = this.getClass().getResource("").getPath();
        String rootPath = Paths.get(".").toUri().normalize().getPath();
        String extractedPath = fullExecutablePath.replace(rootPath, "");
        int extractionEndIndex = extractedPath.indexOf("target/");
        if (extractionEndIndex <= 0) {
            return "";
        }
        return extractedPath.substring(0, extractionEndIndex);
    }

    private void initCachingHttpHeadersFilter(ServletContext servletContext, EnumSet<DispatcherType> disps) {
        log.debug("Registering Caching HTTP Headers Filter");
        FilterRegistration.Dynamic cachingHttpHeadersFilter =
                servletContext.addFilter("cachingHttpHeadersFilter", new CachingHttpHeadersFilter(jHipsterProperties));
        cachingHttpHeadersFilter.addMappingForUrlPatterns(disps, true, "/content/*");
        cachingHttpHeadersFilter.addMappingForUrlPatterns(disps, true, "/app/*");
        cachingHttpHeadersFilter.setAsyncSupported(true);
    }

    private void initMetrics(ServletContext servletContext, EnumSet<DispatcherType> disps) {
        log.debug("Metrics dropwizard non enregistrées (servlet javax incompatible avec Boot 3)");
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = jHipsterProperties.getCors();
        allowCredentialedOrigins(config);
        if (hasCorsOrigins(config)) {
            log.debug("Registering CORS filter");
            source.registerCorsConfiguration("/api/**", config);
            source.registerCorsConfiguration("/v2/api-docs", config);
            source.registerCorsConfiguration("/oauth/**", config);
        }
        return new CorsFilter(source);
    }

    /**
     * Spring refuses {@code allowedOrigins: "*"} when credentials are enabled, and that
     * rejection aborts the request before the controller. A wildcard pattern keeps the
     * same dev behavior and lets the browser send the back-office POST.
     */
    private void allowCredentialedOrigins(CorsConfiguration config) {
        List<String> origins = config.getAllowedOrigins();
        if (origins == null || !origins.contains("*")) {
            return;
        }
        List<String> concrete = new ArrayList<>();
        for (String origin : origins) {
            if (!"*".equals(origin)) {
                concrete.add(origin);
            }
        }
        config.setAllowedOrigins(concrete);
        config.addAllowedOriginPattern("*");
    }

    private boolean hasCorsOrigins(CorsConfiguration config) {
        boolean origins = config.getAllowedOrigins() != null && !config.getAllowedOrigins().isEmpty();
        boolean patterns = config.getAllowedOriginPatterns() != null && !config.getAllowedOriginPatterns().isEmpty();
        return origins || patterns;
    }

    @Autowired(required = false)
    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }
}
