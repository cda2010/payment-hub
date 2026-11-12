package com.gateway.payment.configuration.bootstrap;

import com.gateway.payment.common.crypto.SecretEncryptionService;
import com.gateway.payment.common.domain.AuthType;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.domain.ProviderApi;
import com.gateway.payment.configuration.domain.ProviderEnvironment;
import com.gateway.payment.configuration.domain.ProviderFseApi;
import com.gateway.payment.configuration.domain.ProviderOperation;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.configuration.repository.ProviderApiRepository;
import com.gateway.payment.configuration.repository.ProviderEnvironmentRepository;
import com.gateway.payment.configuration.repository.ProviderFseApiRepository;
import com.gateway.payment.configuration.repository.ProviderOperationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Branche {@code CORIS_MONEY} sur {@code coris-money-connector} (:9180) :
 * remplace les URL {@code mock://} et aligne GENERATE_OTP / CONFIRM_PAYMENT.
 */
@Component
@Order(26)
public class CorisMoneyConnectorBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CorisMoneyConnectorBootstrap.class);
    private static final String PROVIDER_CODE = "CORIS_MONEY";
    private static final String FSE_API_CODE = "CORIS_LOCAL";

    private final PaymentProviderRepository providerRepository;
    private final ProviderEnvironmentRepository environmentRepository;
    private final ProviderFseApiRepository fseApiRepository;
    private final ProviderOperationRepository operationRepository;
    private final ProviderApiRepository apiRepository;
    private final SecretEncryptionService encryptionService;
    private final String connectorBaseUrl;
    private final String clientId;
    private final String clientSecret;
    private final boolean enabled;

    public CorisMoneyConnectorBootstrap(
            PaymentProviderRepository providerRepository,
            ProviderEnvironmentRepository environmentRepository,
            ProviderFseApiRepository fseApiRepository,
            ProviderOperationRepository operationRepository,
            ProviderApiRepository apiRepository,
            SecretEncryptionService encryptionService,
            @Value("${gateway.coris-money.connector-base-url:http://localhost:9180}") String connectorBaseUrl,
            @Value("${gateway.coris-money.client-id:ptimobile}") String clientId,
            @Value("${gateway.coris-money.client-secret:ptimobile-secret}") String clientSecret,
            @Value("${gateway.coris-money.rewire-mock-env:true}") boolean enabled
    ) {
        this.providerRepository = providerRepository;
        this.environmentRepository = environmentRepository;
        this.fseApiRepository = fseApiRepository;
        this.operationRepository = operationRepository;
        this.apiRepository = apiRepository;
        this.encryptionService = encryptionService;
        this.connectorBaseUrl = connectorBaseUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.enabled = enabled;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        PaymentProvider provider = providerRepository.findByCode(PROVIDER_CODE).orElse(null);
        if (provider == null) {
            return;
        }

        String base = trimSlash(connectorBaseUrl);
        ProviderEnvironment sandbox = null;
        for (ProviderEnvironment env : environmentRepository.findByProviderId(provider.getId())) {
            String url = env.getBaseUrl();
            if (url == null || url.isBlank() || url.startsWith("mock://") || !base.equals(trimSlash(url))) {
                env.setBaseUrl(base);
                environmentRepository.save(env);
                log.info("CORIS_MONEY env {} : {} → {}", env.getCode(), url, env.getBaseUrl());
            }
            if ("SANDBOX".equalsIgnoreCase(env.getCode())
                    || "DEVELOPPEMENT".equalsIgnoreCase(env.getCode())
                    || sandbox == null) {
                sandbox = env;
            }
        }
        if (sandbox == null) {
            log.warn("CORIS_MONEY : aucun environnement — endpoints non branchés");
            return;
        }

        ProviderFseApi fse = ensureFseApi(provider, sandbox, base);
        ensureEndpoint(provider, sandbox, fse, "GENERATE_OTP", "Générer OTP", "POST", "/payments/otp",
                "{\"telephone\":\"${phoneNumber}\",\"msisdn\":\"${phoneNumber}\","
                        + "\"phone\":\"${phoneNumber}\",\"codeFse\":\"0011\","
                        + "\"amount\":\"${amount}\",\"currency\":\"${currency}\"}");
        ensureEndpoint(provider, sandbox, fse, "CONFIRM_PAYMENT", "Confirmer paiement", "POST", "/payments",
                "{\"msisdn\":\"${phoneNumber}\",\"phone\":\"${phoneNumber}\","
                        + "\"otp\":\"${otp}\",\"transactionAmount\":\"${amount}\","
                        + "\"currencyCode\":\"${currency}\"}");
        ensureEndpoint(provider, sandbox, fse, "GET_STATUS", "Statut", "GET",
                "/payments/${providerReference}", null);

        log.info("CORIS_MONEY branché sur {} (endpoints /payments*)", base);
    }

    private ProviderFseApi ensureFseApi(PaymentProvider provider, ProviderEnvironment env, String base) {
        ProviderFseApi fse = fseApiRepository.findByProviderIdAndCode(provider.getId(), FSE_API_CODE)
                .orElseGet(() -> ProviderFseApi.builder()
                        .provider(provider)
                        .code(FSE_API_CODE)
                        .nom("Coris Money Connector local")
                        .typeApi("REST")
                        .baseUrl(base)
                        .host("localhost")
                        .port("9180")
                        .useSsl(false)
                        .useProxy(false)
                        .actif(true)
                        .build());
        fse.setEnvironment(env);
        fse.setBaseUrl(base);
        fse.setHost(hostOf(base));
        fse.setPort(portOf(base));
        fse.setUseSsl(base.startsWith("https://"));
        fse.setActif(true);
        fse.setAuthType(AuthType.OAUTH2_CLIENT_CREDENTIALS);
        fse.setTokenUrl(base + "/oauth/token");
        if (StringUtils.hasText(clientId)) {
            fse.setClientId(clientId);
        }
        if (StringUtils.hasText(clientSecret)) {
            fse.setClientSecretEnc(encryptionService.encrypt(clientSecret));
        }
        return fseApiRepository.save(fse);
    }

    private void ensureEndpoint(
            PaymentProvider provider,
            ProviderEnvironment env,
            ProviderFseApi fse,
            String opCode,
            String opNom,
            String method,
            String path,
            String mapping
    ) {
        ProviderOperation op = operationRepository.findByProviderIdAndCode(provider.getId(), opCode)
                .orElseGet(() -> operationRepository.save(ProviderOperation.builder()
                        .provider(provider)
                        .code(opCode)
                        .nom(opNom)
                        .idempotent(true)
                        .actif(true)
                        .build()));
        if (!op.isActif()) {
            op.setActif(true);
            operationRepository.save(op);
        }

        List<ProviderApi> existing = apiRepository.findByOperationId(op.getId());
        ProviderApi api = existing.stream().filter(ProviderApi::isActif).findFirst()
                .or(() -> existing.stream().findFirst())
                .orElseGet(() -> ProviderApi.builder()
                        .provider(provider)
                        .operation(op)
                        .code(opCode)
                        .nom(opCode)
                        .retryEnabled(false)
                        .maxAttempts(1)
                        .retryDelayMs(2000L)
                        .backoffMultiplier(1.0)
                        .actif(true)
                        .build());

        api.setEnvironment(env);
        api.setFseApi(fse);
        api.setHttpMethod(method);
        api.setPath(path);
        api.setContentType("application/json");
        api.setTimeoutSeconds(30);
        api.setRequestMappingJson(mapping);
        api.setActif(true);
        apiRepository.save(api);
    }

    private static String hostOf(String baseUrl) {
        try {
            return java.net.URI.create(baseUrl).getHost();
        } catch (Exception ex) {
            return "localhost";
        }
    }

    private static String portOf(String baseUrl) {
        try {
            int p = java.net.URI.create(baseUrl).getPort();
            return p > 0 ? String.valueOf(p) : (baseUrl.startsWith("https://") ? "443" : "80");
        } catch (Exception ex) {
            return "9180";
        }
    }

    private static String trimSlash(String url) {
        if (url == null) {
            return null;
        }
        String u = url.trim();
        while (u.endsWith("/")) {
            u = u.substring(0, u.length() - 1);
        }
        return u;
    }
}
