package com.gateway.payment.billing.bootstrap;

import com.gateway.payment.common.crypto.SecretEncryptionService;
import com.gateway.payment.common.domain.AuthType;
import com.gateway.payment.configuration.domain.FseParticipantKind;
import com.gateway.payment.configuration.domain.FseStatut;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.domain.ProviderApi;
import com.gateway.payment.configuration.domain.ProviderAuthentication;
import com.gateway.payment.configuration.domain.ProviderEnvironment;
import com.gateway.payment.configuration.domain.ProviderFseApi;
import com.gateway.payment.configuration.domain.ProviderOperation;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.configuration.repository.ProviderApiRepository;
import com.gateway.payment.configuration.repository.ProviderAuthenticationRepository;
import com.gateway.payment.configuration.repository.ProviderEnvironmentRepository;
import com.gateway.payment.configuration.repository.ProviderFseApiRepository;
import com.gateway.payment.configuration.repository.ProviderOperationRepository;
import com.gateway.payment.orchestration.service.OrchestrationSeedService;
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
 * Branche le provider {@code BILL_PAYMENT} sur {@code bill-payment-connector} (:9182) :
 * codeFse, environnements, Api FSE (clé API), opérations, endpoints {@code /bills/*},
 * et seed des InterfaceUtilisateur REQUEST.
 */
@Component
@Order(25)
public class BillPaymentConnectorBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(BillPaymentConnectorBootstrap.class);
    private static final String PROVIDER_CODE = "BILL_PAYMENT";
    /** Identité FSE pour le rail IU REQUEST (aligné Api FSE {@link #FSE_API_CODE}). */
    private static final String CODE_FSE = "BILLS";
    private static final String FSE_API_CODE = "BILLS";

    private final PaymentProviderRepository providerRepository;
    private final ProviderEnvironmentRepository environmentRepository;
    private final ProviderAuthenticationRepository authenticationRepository;
    private final ProviderFseApiRepository fseApiRepository;
    private final ProviderOperationRepository operationRepository;
    private final ProviderApiRepository apiRepository;
    private final SecretEncryptionService encryptionService;
    private final OrchestrationSeedService orchestrationSeedService;
    private final String connectorBaseUrl;
    private final String connectorApiKey;
    private final boolean enabled;

    public BillPaymentConnectorBootstrap(
            PaymentProviderRepository providerRepository,
            ProviderEnvironmentRepository environmentRepository,
            ProviderAuthenticationRepository authenticationRepository,
            ProviderFseApiRepository fseApiRepository,
            ProviderOperationRepository operationRepository,
            ProviderApiRepository apiRepository,
            SecretEncryptionService encryptionService,
            OrchestrationSeedService orchestrationSeedService,
            @Value("${gateway.bill-payment.connector-base-url:http://localhost:9182}") String connectorBaseUrl,
            @Value("${gateway.bill-payment.connector-api-key:change-me-bills}") String connectorApiKey,
            @Value("${gateway.bill-payment.rewire-mock-env:true}") boolean enabled
    ) {
        this.providerRepository = providerRepository;
        this.environmentRepository = environmentRepository;
        this.authenticationRepository = authenticationRepository;
        this.fseApiRepository = fseApiRepository;
        this.operationRepository = operationRepository;
        this.apiRepository = apiRepository;
        this.encryptionService = encryptionService;
        this.orchestrationSeedService = orchestrationSeedService;
        this.connectorBaseUrl = connectorBaseUrl;
        this.connectorApiKey = connectorApiKey;
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

        ensureCodeFse(provider);

        String base = trimSlash(connectorBaseUrl);
        ProviderEnvironment sandbox = null;
        for (ProviderEnvironment env : environmentRepository.findByProviderId(provider.getId())) {
            String url = env.getBaseUrl();
            if (url == null || url.isBlank() || url.startsWith("mock://") || !base.equals(trimSlash(url))) {
                env.setBaseUrl(base);
                environmentRepository.save(env);
                log.info("BILL_PAYMENT env {} : {} → {}", env.getCode(), url, env.getBaseUrl());
            }
            ensureEnvAuth(provider, env);
            if ("SANDBOX".equalsIgnoreCase(env.getCode()) || sandbox == null) {
                sandbox = env;
            }
        }
        if (sandbox == null) {
            log.warn("BILL_PAYMENT : aucun environnement — endpoints non branchés");
            return;
        }

        ProviderFseApi fse = ensureFseApi(provider, sandbox, base);
        ensureEndpoint(provider, sandbox, fse, "LOOKUP_BILL", "Consulter facture", "POST", "/bills/lookup",
                "[{\"billerCode\":\"${billerCode}\"},{\"productCode\":\"${productCode}\"},"
                        + "{\"billReference\":\"${billReference}\"},{\"meterNumber\":\"${meterNumber}\"},"
                        + "{\"subscriberReference\":\"${subscriberReference}\"},{\"amount\":\"${amount}\"},"
                        + "{\"currency\":\"${currency}\"}]");
        ensureEndpoint(provider, sandbox, fse, "BILL_LIST", "Lister factures", "POST", "/bills/list",
                "[{\"billerCode\":\"${billerCode}\"},{\"productCode\":\"${productCode}\"},"
                        + "{\"subscriberReference\":\"${subscriberReference}\"},"
                        + "{\"customerReference\":\"${customerReference}\"},{\"status\":\"OPEN\"}]");
        ensureEndpoint(provider, sandbox, fse, "GENERATE_OTP", "OTP paiement facture", "POST", "/bills/otp",
                "[{\"billerCode\":\"${billerCode}\"},{\"billReference\":\"${billReference}\"},"
                        + "{\"productCode\":\"${productCode}\"}]");
        ensureEndpoint(provider, sandbox, fse, "PAY_BILL", "Payer facture", "POST", "/bills/pay",
                "[{\"billerCode\":\"${billerCode}\"},{\"billReference\":\"${billReference}\"},"
                        + "{\"productCode\":\"${productCode}\"},{\"amount\":\"${amount}\"},"
                        + "{\"otp\":\"${otp}\"},{\"currency\":\"${currency}\"},"
                        + "{\"idempotencyKey\":\"${paymentId}\"},{\"transactionReference\":\"${paymentId}\"}]");
        ensureEndpoint(provider, sandbox, fse, "GET_STATUS", "Statut", "GET",
                "/bills/${providerReference}?biller=${billerCode}&productCode=${productCode}",
                null);

        try {
            List<String> seeded = orchestrationSeedService.seedInterfacesForProvider(provider);
            log.info("BILL_PAYMENT branché sur {} (codeFse={}, Api FSE {}, IU REQUEST={})",
                    base, provider.getCodeFse(), FSE_API_CODE, seeded);
        } catch (Exception ex) {
            log.warn("BILL_PAYMENT : seed IU REQUEST ignoré — {}", ex.getMessage());
            log.info("BILL_PAYMENT branché sur {} (codeFse={}, Api FSE {}, endpoints /bills/*)",
                    base, provider.getCodeFse(), FSE_API_CODE);
        }
    }

    private void ensureCodeFse(PaymentProvider provider) {
        if (StringUtils.hasText(provider.getCodeFse())) {
            return;
        }
        if (providerRepository.existsByCodeFse(CODE_FSE)
                && providerRepository.findByCodeFse(CODE_FSE)
                .map(other -> !other.getId().equals(provider.getId()))
                .orElse(false)) {
            log.warn("BILL_PAYMENT : codeFse {} déjà pris — IU REQUEST non seedables automatiquement", CODE_FSE);
            return;
        }
        provider.setCodeFse(CODE_FSE);
        provider.setCodeExterne(CODE_FSE);
        provider.setGroupCode("BILLS");
        provider.setCountryCode("BF");
        provider.setParticipantKind(FseParticipantKind.FACTURIER);
        provider.setFseStatut(FseStatut.NONBANQUE);
        providerRepository.save(provider);
        log.info("BILL_PAYMENT codeFse posé → {}", CODE_FSE);
    }

    private ProviderFseApi ensureFseApi(PaymentProvider provider, ProviderEnvironment env, String base) {
        ProviderFseApi fse = fseApiRepository.findByProviderIdAndCode(provider.getId(), FSE_API_CODE)
                .orElseGet(() -> ProviderFseApi.builder()
                        .provider(provider)
                        .code(FSE_API_CODE)
                        .nom("Bill Payment Connector")
                        .typeApi("REST")
                        .baseUrl(base)
                        .host("localhost")
                        .port("9182")
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
        fse.setAuthType(AuthType.API_KEY);
        fse.setCustomHeaderName("X-API-Key");
        if (StringUtils.hasText(connectorApiKey)) {
            fse.setApiKeyEnc(encryptionService.encrypt(connectorApiKey));
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

    private void ensureEnvAuth(PaymentProvider provider, ProviderEnvironment env) {
        ProviderAuthentication auth = authenticationRepository
                .findByProviderIdAndEnvironmentId(provider.getId(), env.getId())
                .orElseGet(() -> ProviderAuthentication.builder()
                        .provider(provider)
                        .environment(env)
                        .build());
        auth.setAuthType(AuthType.API_KEY);
        auth.setCustomHeaderName("X-API-Key");
        if (StringUtils.hasText(connectorApiKey)) {
            auth.setApiKeyEnc(encryptionService.encrypt(connectorApiKey));
        }
        authenticationRepository.save(auth);
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
            return "9182";
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
