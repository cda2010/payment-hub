package com.gateway.payment.catalog.omnicanal;

import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.omnicanal.external.RuntimeProviderResolver;
import com.gateway.payment.omnicanal.response.CanalLegResult;
import com.gateway.payment.omnicanal.response.SpecRunResult;
import com.gateway.payment.omnicanal.spec.SpecDrivenTransferExecutor;
import com.gateway.payment.orchestration.api.dto.SchemaLegResult;
import com.gateway.payment.orchestration.domain.ProprieteApi;
import com.gateway.payment.orchestration.service.SchemaLegExecutorService;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.provider.ProviderExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Pont catalogue → Omnicanal :
 * <ul>
 *   <li>{@link #invokeOperation} — 1 jambe (GET_FEES, GET_STATUS, GENERATE_OTP…)</li>
 *   <li>{@link #executeTransfer} — SpecDriven multi-jambes (EXECUTE_TRANSFER)</li>
 * </ul>
 * Quand l'omnicanal externe est actif, la méthode part en boundary et le virement
 * en présentation. Sinon les connecteurs restent derrière {@link SchemaLegExecutorService}.
 */
@Service
public class OmnicanalCatalogInvocationService {

    private static final Logger log = LoggerFactory.getLogger(OmnicanalCatalogInvocationService.class);

    private static final Set<String> TRANSFER_OPS = Set.of(
            "TRANSFER_EXECUTE",
            "EXECUTE_TRANSFER",
            "EXECUTE_CASHOUT",
            "EXECUTE_CASHIN",
            "TRANSFERT"
    );

    private static final Set<String> GENERIC_OPS = Set.of(
            "GET_FEES",
            "CALCULATE_FEES",
            "GET_STATUS",
            "GET_BALANCE",
            "CHECK_BALANCE",
            "BALANCE_CHECK",
            "CONSULTATIONSOLDE",
            "VALIDATE_ACCOUNT",
            "GETINFOS",
            "GET_INFOS",
            "INFOCLIENT",
            "INFO_CLIENT",
            "GENERATE_OTP",
            "DEBIT",
            "CREDIT",
            "TOPUP",
            "EXECUTE_TOPUP",
            "LOOKUP_BILL",
            "BILL_LIST",
            "PAY_BILL",
            "CONFIRM_PAYMENT",
            "CREATE_PAYMENT",
            "LINK",
            "EXECUTE_LINK",
            "REGISTER",
            "UNLINK",
            "EXECUTE_UNLINK",
            "LIMITS_CHECK",
            "CHECK_LIMITS",
            "DATASOURCE"
    );

    private final CatalogOmnicanalProperties properties;
    private final SchemaLegExecutorService legExecutor;
    private final SpecDrivenTransferExecutor specDrivenTransferExecutor;
    private final PaymentProviderRepository providerRepository;
    private final ExternalOmnicanalCatalogGateway externalGateway;
    private final RuntimeProviderResolver runtimeProviders;

    public OmnicanalCatalogInvocationService(
            CatalogOmnicanalProperties properties,
            SchemaLegExecutorService legExecutor,
            SpecDrivenTransferExecutor specDrivenTransferExecutor,
            PaymentProviderRepository providerRepository,
            ExternalOmnicanalCatalogGateway externalGateway,
            RuntimeProviderResolver runtimeProviders
    ) {
        this.properties = properties;
        this.legExecutor = legExecutor;
        this.specDrivenTransferExecutor = specDrivenTransferExecutor;
        this.providerRepository = providerRepository;
        this.externalGateway = externalGateway;
        this.runtimeProviders = runtimeProviders;
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public boolean isFallbackToProvider() {
        return properties.isFallbackToProvider();
    }

    public boolean isTransferOperation(String operationCode) {
        return operationCode != null && TRANSFER_OPS.contains(operationCode.trim().toUpperCase(Locale.ROOT));
    }

    /**
     * Solde d'un compte via CONSULTATIONSOLDE (omnicanal externe).
     * Retourne uniquement le montant, pas le corps fournisseur.
     */
    public Map<String, Object> consultBalance(String providerCode, String numeroCompte) {
        if (externalGateway == null || !externalGateway.enabled()) {
            throw new BusinessException("EXTERNAL_OMNICANAL_DISABLED", "Omnicanal externe indisponible");
        }
        if (!StringUtils.hasText(providerCode) || !StringUtils.hasText(numeroCompte)) {
            throw new BusinessException("BALANCE_PARAMS", "providerCode et numeroCompte sont requis");
        }
        Optional<String> codeFse = resolveCodeFse(providerCode.trim(), Map.of());
        if (codeFse.isEmpty()) {
            throw new BusinessException(
                    "CODE_FSE_REQUIRED",
                    "codeFse introuvable pour " + providerCode.trim()
            );
        }
        Map<String, String> params = new LinkedHashMap<>();
        params.put("numeroCompte", numeroCompte.trim());
        params.put("sourceAccount", numeroCompte.trim());
        params.put("codeFse", codeFse.get());
        ProviderExecutionResult result = externalGateway.boundary(
                null, "CONSULTATIONSOLDE", "CONSULTATIONSOLDE", codeFse.get(), params);
        Object solde = result.getResponseBody() == null ? null : result.getResponseBody().get("solde");
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("numeroCompte", numeroCompte.trim());
        out.put("providerCode", providerCode.trim());
        out.put("success", result.isSuccess());
        out.put("solde", solde);
        return out;
    }

    public boolean isGenericOmnicanalOperation(String operationCode) {
        if (operationCode == null || operationCode.isBlank()) {
            return false;
        }
        String op = operationCode.trim().toUpperCase(Locale.ROOT);
        return GENERIC_OPS.contains(op);
    }

    /**
     * Appel unitaire Omnicanal (1 FSE / 1 typeMethode → connecteur).
     *
     * @return empty uniquement si le routage est désactivé / op non applicable ;
     *         codeFse manquant → {@code CODE_FSE_REQUIRED}
     */
    public Optional<ProviderExecutionResult> invokeOperation(
            PaymentTransaction tx,
            String providerCode,
            String operationCode,
            String stepCode,
            Map<String, Object> context,
            Map<String, Object> input
    ) {
        if (!properties.isEnabled()) {
            return Optional.empty();
        }
        if (!StringUtils.hasText(operationCode) || isTransferOperation(operationCode)) {
            return Optional.empty();
        }

        Optional<String> codeFse = resolveCodeFse(providerCode, context);
        if (codeFse.isEmpty()) {
            throw new BusinessException(
                    "CODE_FSE_REQUIRED",
                    "codeFse requis pour " + operationCode + " (provider=" + providerCode
                            + ") — InterfaceUtilisateur REQUEST ; rail direct supprimé"
            );
        }

        String typeMethode = toTypeMethode(operationCode);
        Map<String, String> userParams = buildUserParams(context, input, codeFse.get(), null);
        userParams.putIfAbsent("codeFse", codeFse.get());
        enrichTxUserParams(tx, userParams);

        if (externalGateway != null && externalGateway.enabled()) {
            markExternal(context);
            log.info(
                    "Omnicanal externe opération paymentId={} op={} typeMethode={} fse={}",
                    tx.getPaymentId(), operationCode, typeMethode, codeFse.get()
            );
            return Optional.of(externalGateway.boundary(
                    tx, operationCode, typeMethode, codeFse.get(), userParams));
        }

        try {
            SchemaLegResult leg = legExecutor.executeLegByStep(
                    tx,
                    null,
                    typeMethode,
                    codeFse.get(),
                    codeFse.get(),
                    userParams,
                    ProprieteApi.FSESOURCE,
                    typeMethode
            );
            log.info(
                    "Omnicanal opération paymentId={} op={} typeMethode={} fse={} success={}",
                    tx.getPaymentId(), operationCode, typeMethode, codeFse.get(), leg.success()
            );
            return Optional.of(toProviderResult(leg, operationCode));
        } catch (BusinessException ex) {
            log.warn(
                    "Omnicanal opération impossible paymentId={} op={} : {} — {}",
                    tx.getPaymentId(), operationCode, ex.getCode(), ex.getMessage()
            );
            throw ex;
        } catch (Exception ex) {
            log.warn(
                    "Omnicanal opération erreur paymentId={} op={} : {}",
                    tx.getPaymentId(), operationCode, ex.getMessage(), ex
            );
            return Optional.of(ProviderExecutionResult.builder()
                    .success(false)
                    .gatewayStatus(PaymentStatus.FAILED)
                    .providerMessage(ex.getMessage())
                    .providerStatus("OMNICANAL_ERROR")
                    .build());
        }
    }

    /**
     * Transfert SpecDriven (SOURCE/DEST → schéma → N jambes → connecteurs).
     */
    public Optional<ProviderExecutionResult> executeTransfer(
            PaymentTransaction tx,
            Map<String, Object> context,
            Map<String, Object> input
    ) {
        if (!properties.isEnabled()) {
            return Optional.empty();
        }

        String sourceProvider = firstNonBlank(
                roleProvider(context, "SOURCE"),
                string(context, "payProviderCode"),
                string(context, "providerCode"),
                string(context, "sourceRail"),
                tx.getProviderCode()
        );
        String destProvider = firstNonBlank(
                roleProvider(context, "DESTINATION"),
                roleProvider(context, "AGENT_NETWORK"),
                string(context, "agentNetwork"),
                string(context, "destinationProvider"),
                string(context, "billProviderCode"),
                sourceProvider
        );

        Optional<String> sourceFse = resolveTransferFse(context,
                roleProvider(context, "SOURCE"),
                string(context, "payProviderCode"),
                string(context, "providerCode"),
                string(context, "sourceRail"),
                tx.getProviderCode(),
                roleProvider(context, "AGENT_NETWORK"),
                string(context, "agentNetwork"));
        Optional<String> destFse = resolveTransferFse(context,
                roleProvider(context, "DESTINATION"),
                roleProvider(context, "AGENT_NETWORK"),
                string(context, "agentNetwork"),
                string(context, "destinationProvider"),
                string(context, "billProviderCode"),
                sourceProvider);
        if (sourceFse.isEmpty() || destFse.isEmpty()) {
            throw new BusinessException(
                    "CODE_FSE_REQUIRED",
                    "codeFse SOURCE/DEST requis pour EXECUTE_TRANSFER (source="
                            + sourceProvider + ", dest=" + destProvider
                            + ") — rail direct supprimé"
            );
        }

        Map<String, String> userParams = buildUserParams(context, input, sourceFse.get(), destFse.get());
        enrichTxUserParams(tx, userParams);

        if (externalGateway != null && externalGateway.enabled()) {
            markExternal(context);
            log.info(
                    "Omnicanal externe transfert paymentId={} sourceFse={} destFse={}",
                    tx.getPaymentId(), sourceFse.get(), destFse.get()
            );
            return Optional.of(externalGateway.presentation(
                    tx, sourceFse.get(), destFse.get(), userParams));
        }

        ServiceTypeCode serviceType = ServiceTypeCode.TRANSFERT;
        Object st = context.get("serviceType");
        if (st != null) {
            try {
                ServiceTypeCode parsed = ServiceTypeCode.fromString(String.valueOf(st));
                if (parsed != null) {
                    serviceType = parsed;
                }
            } catch (Exception ignored) {
                // garder TRANSFERT
            }
        }

        try {
            SpecRunResult run = specDrivenTransferExecutor.execute(
                    new SpecDrivenTransferExecutor.SpecDrivenRequest(
                            serviceType,
                            sourceFse.get(),
                            destFse.get(),
                            null,
                            null,
                            userParams,
                            tx
                    )
            );
            log.info(
                    "Omnicanal executeTransfer paymentId={} sourceFse={} destFse={} error={} legs={}",
                    tx.getPaymentId(),
                    sourceFse.get(),
                    destFse.get(),
                    run.error(),
                    run.getLegs() != null ? run.getLegs().size() : 0
            );
            return Optional.of(toProviderResult(run));
        } catch (BusinessException ex) {
            log.warn(
                    "Omnicanal transfer impossible paymentId={} : {} — {}",
                    tx.getPaymentId(), ex.getCode(), ex.getMessage()
            );
            throw ex;
        } catch (Exception ex) {
            log.warn("Omnicanal transfer erreur paymentId={} : {}", tx.getPaymentId(), ex.getMessage());
            return Optional.of(ProviderExecutionResult.builder()
                    .success(false)
                    .gatewayStatus(PaymentStatus.FAILED)
                    .providerMessage(ex.getMessage())
                    .providerStatus("OMNICANAL_TRANSFER_ERROR")
                    .build());
        }
    }

    /** Catalogue operation → typeMethode Omnicanal / opération connecteur. */
    static String toTypeMethode(String operationCode) {
        if (operationCode == null) {
            return "NOP";
        }
        return switch (operationCode.trim().toUpperCase(Locale.ROOT)) {
            case "GET_FEES", "CALCULATE_FEES" -> "GET_FEES";
            case "GET_STATUS" -> "GET_STATUS";
            case "GET_BALANCE", "CHECK_BALANCE", "BALANCE_CHECK", "CONSULTATIONSOLDE" -> "CONSULTATIONSOLDE";
            case "VALIDATE_ACCOUNT", "GETINFOS", "GET_INFOS" -> "GETINFOS";
            case "INFOCLIENT", "INFO_CLIENT", "INFO_CLIENT_COMPTE" -> "INFOCLIENT";
            case "GENERATE_OTP" -> "GENERATE_OTP";
            case "LIMITS_CHECK", "CHECK_LIMITS" -> "GET_STATUS";
            case "TRANSFER_EXECUTE", "EXECUTE_TRANSFER", "EXECUTE_CASHOUT", "EXECUTE_CASHIN" -> "TRANSFERT";
            case "TOPUP", "EXECUTE_TOPUP" -> "CREDIT";
            case "LINK", "EXECUTE_LINK", "REGISTER" -> "LINK";
            case "UNLINK", "EXECUTE_UNLINK" -> "UNLINK";
            case "DATASOURCE" -> "DATASOURCE";
            default -> operationCode.trim().toUpperCase(Locale.ROOT);
        };
    }

    /**
     * Premier code fournisseur qui porte un {@code codeFse}.
     * Ignore un hôte technique sans FSE (ex. {@code CASH_OUT}) et passe au rail réel.
     */
    private Optional<String> resolveTransferFse(Map<String, Object> context, String... providerCodes) {
        if (providerCodes == null) {
            return Optional.empty();
        }
        for (String code : providerCodes) {
            if (!StringUtils.hasText(code)) {
                continue;
            }
            Optional<String> fse = resolveCodeFse(code, context);
            if (fse.isPresent()) {
                return fse;
            }
        }
        return Optional.empty();
    }

    private Optional<String> resolveCodeFse(String providerCode, Map<String, Object> context) {
        if (StringUtils.hasText(providerCode)) {
            String trimmed = providerCode.trim();
            if (providerRepository != null) {
                Optional<PaymentProvider> p = providerRepository.findByCode(trimmed);
                if (p.isPresent() && StringUtils.hasText(p.get().getCodeFse())) {
                    return Optional.of(p.get().getCodeFse().trim());
                }
            }
            if (runtimeProviders != null) {
                Optional<String> remote = runtimeProviders.codeFse(trimmed);
                if (remote.isPresent()) {
                    return remote;
                }
            }
            // Provider explicite sans FSE : ne pas réutiliser un codeFse wallet laissé
            // dans le contexte après OTP (sinon PAY_BILL part sur le mauvais FSE).
            return Optional.empty();
        }
        String fromCtx = firstNonBlank(
                string(context, "codeFse"),
                string(context, "sourceCodeFse"),
                string(context, "CODEFSESOURCE")
        );
        return StringUtils.hasText(fromCtx) ? Optional.of(fromCtx.trim()) : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private static String roleProvider(Map<String, Object> context, String role) {
        if (context == null) {
            return null;
        }
        Object raw = context.get("roleAssignments");
        if (!(raw instanceof Map<?, ?> map)) {
            return null;
        }
        Object v = map.get(role);
        if (v == null) {
            v = map.get(role.toUpperCase(Locale.ROOT));
        }
        if (v == null) {
            v = map.get(role.toLowerCase(Locale.ROOT));
        }
        return v != null && StringUtils.hasText(String.valueOf(v)) ? String.valueOf(v).trim() : null;
    }

    private static Map<String, String> buildUserParams(
            Map<String, Object> context,
            Map<String, Object> input,
            String sourceFse,
            String destFse
    ) {
        Map<String, String> params = new LinkedHashMap<>();
        putAllString(params, context);
        putAllString(params, input);

        params.put("CODEFSESOURCE", sourceFse);
        params.put("codeFseSource", sourceFse);
        if (StringUtils.hasText(destFse)) {
            params.put("CODEFSEDEST", destFse);
            params.put("codeFseDest", destFse);
        }

        // Alias UI → clés canal : uniquement via ParametreUtilisateur (codeParametreMethode)
        // dans LegCompilerService.bindInterfaceParams / propagateInterfaceAliases.

        return params;
    }

    /** Alimente le sac IU depuis la TX (placeholders mapping) — pas le body HTTP. */
    private static void enrichTxUserParams(PaymentTransaction tx, Map<String, String> params) {
        if (tx == null || params == null) {
            return;
        }
        if (StringUtils.hasText(tx.getPaymentId())) {
            params.putIfAbsent("paymentId", tx.getPaymentId());
            params.putIfAbsent("transactionReference", tx.getPaymentId());
        }
        if (StringUtils.hasText(tx.getIdempotencyKey())) {
            params.putIfAbsent("idempotencyKey", tx.getIdempotencyKey());
        } else if (StringUtils.hasText(tx.getPaymentId())) {
            params.putIfAbsent("idempotencyKey", tx.getPaymentId());
        }
        if (tx.getAmount() != null) {
            params.putIfAbsent("amount", tx.getAmount().toPlainString());
        }
        if (StringUtils.hasText(tx.getCurrency())) {
            params.putIfAbsent("currency", tx.getCurrency());
        }
        if (StringUtils.hasText(tx.getPhoneNumber())) {
            params.putIfAbsent("phoneNumber", tx.getPhoneNumber());
            params.putIfAbsent("phone", tx.getPhoneNumber());
        }
        if (StringUtils.hasText(tx.getCustomerReference())) {
            params.putIfAbsent("customerReference", tx.getCustomerReference());
        }
        if (StringUtils.hasText(tx.getMerchantReference())) {
            params.putIfAbsent("merchantReference", tx.getMerchantReference());
        }
    }

    private static void putAllString(Map<String, String> target, Map<String, Object> source) {
        if (source == null) {
            return;
        }
        for (Map.Entry<String, Object> e : source.entrySet()) {
            if (e.getKey() == null || e.getValue() == null) {
                continue;
            }
            if (e.getValue() instanceof Map<?, ?> || e.getValue() instanceof Iterable<?>) {
                continue;
            }
            String v = String.valueOf(e.getValue()).trim();
            if (!v.isEmpty() && !"null".equals(v)) {
                target.putIfAbsent(e.getKey(), v);
            }
        }
    }

    private static ProviderExecutionResult toProviderResult(SchemaLegResult leg, String operationCode) {
        Map<String, Object> body = new HashMap<>();
        if (leg.responseBody() != null && !leg.responseBody().isEmpty()) {
            body.putAll(leg.responseBody());
        }
        body.put("omnicanal", true);
        body.put("typeMethode", leg.typeMethode());
        body.put("codeFse", leg.codeFse());
        body.put("operationCode", leg.operationCode() != null ? leg.operationCode() : operationCode);
        return ProviderExecutionResult.builder()
                .success(leg.success())
                .gatewayStatus(leg.gatewayStatus() != null ? leg.gatewayStatus()
                        : (leg.success() ? PaymentStatus.PROCESSING : PaymentStatus.FAILED))
                .providerStatus(leg.providerStatus())
                .providerMessage(leg.providerMessage())
                .providerReference(leg.providerReference())
                .responseBody(body)
                .build();
    }

    private static ProviderExecutionResult toProviderResult(SpecRunResult run) {
        Map<String, Object> body = new HashMap<>();
        body.put("omnicanal", true);
        body.put("omnicanalTransfer", true);
        body.put("schemaId", run.getSchemaId() != null ? run.getSchemaId().toString() : null);
        body.put("patternCode", run.getPatternCode());
        body.put("workflowName", run.getWorkflowName());
        body.put("compensated", run.isCompensated());
        String lastRef = null;
        String lastStatus = null;
        String lastMessage = run.getMessage();
        if (run.getLegs() != null) {
            body.put("legCount", run.getLegs().size());
            for (CanalLegResult leg : run.getLegs()) {
                if (leg.getProviderReference() != null) {
                    lastRef = leg.getProviderReference();
                }
                if (leg.getProviderStatus() != null) {
                    lastStatus = leg.getProviderStatus();
                }
                if (leg.getProviderMessage() != null) {
                    lastMessage = leg.getProviderMessage();
                }
            }
        }
        boolean success = !run.error();
        return ProviderExecutionResult.builder()
                .success(success)
                .gatewayStatus(success ? PaymentStatus.PROCESSING : PaymentStatus.FAILED)
                .providerStatus(success ? (lastStatus != null ? lastStatus : "0")
                        : (run.getErrorCode() != null ? run.getErrorCode().name() : "ORCH_ERROR"))
                .providerMessage(lastMessage)
                .providerReference(lastRef)
                .responseBody(body)
                .build();
    }

    private static void markExternal(Map<String, Object> context) {
        if (context == null) {
            return;
        }
        try {
            context.put("externalOmnicanal", true);
        } catch (UnsupportedOperationException ignored) {
            // contexte immuable
        }
    }

    private static String string(Map<String, Object> context, String key) {
        if (context == null || key == null) {
            return null;
        }
        Object v = context.get(key);
        return v != null ? String.valueOf(v).trim() : null;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                return v.trim();
            }
        }
        return null;
    }
}
