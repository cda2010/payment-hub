package com.gateway.payment.bpm;

import com.gateway.payment.catalog.omnicanal.OmnicanalCatalogInvocationService;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.provider.ProviderExecutionResult;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Optional;

/**
 * Appel fournisseur catalogue-aware : toujours via InterfaceUtilisateur REQUEST + ParametreUtilisateur
 * (Omnicanal / LegCompiler), puis connecteur HTTP.
 * <p>
 * Pas de rail direct (contexte BPM brut) — IU / codeFse manquants → erreur métier.
 */
@Component
public class CatalogProviderInvoker {

    private final ProviderCallOperationRegistry operationRegistry;
    private final OmnicanalCatalogInvocationService omnicanalCatalogInvocationService;

    public CatalogProviderInvoker(
            ProviderCallOperationRegistry operationRegistry,
            OmnicanalCatalogInvocationService omnicanalCatalogInvocationService
    ) {
        this.operationRegistry = operationRegistry;
        this.omnicanalCatalogInvocationService = omnicanalCatalogInvocationService;
    }

    /**
     * Résout le provider cible (rail registry) puis invoque via IU REQUEST.
     */
    public ProviderExecutionResult invoke(
            PaymentTransaction tx,
            String stepCode,
            String operationCode,
            Map<String, Object> input,
            Map<String, Object> context
    ) {
        ProviderCallOperationSpec spec = operationRegistry.resolve(operationCode);
        String invokeProvider = resolveProvider(tx, context, spec.rail());
        return invokeForProvider(tx, invokeProvider, stepCode, operationCode, input, context);
    }

    /**
     * Même chemin avec un provider déjà résolu (ex. binding catalogue {@code actorRole}).
     */
    public ProviderExecutionResult invokeForProvider(
            PaymentTransaction tx,
            String invokeProvider,
            String stepCode,
            String operationCode,
            Map<String, Object> input,
            Map<String, Object> context
    ) {
        Map<String, Object> safeInput = input != null ? input : Map.of();
        Map<String, Object> ctx = context != null ? context : Map.of();
        String provider = StringUtils.hasText(invokeProvider) ? invokeProvider.trim() : tx.getProviderCode();

        if (!omnicanalCatalogInvocationService.isEnabled()) {
            throw new BusinessException(
                    "INTERFACE_PATH_DISABLED",
                    "Routage InterfaceUtilisateur désactivé (gateway.catalog.omnicanal.enabled=false)"
            );
        }

        Optional<ProviderExecutionResult> viaInterface = Optional.empty();
        if (omnicanalCatalogInvocationService.isTransferOperation(operationCode)) {
            viaInterface = omnicanalCatalogInvocationService.executeTransfer(tx, ctx, safeInput);
            if (viaInterface.isPresent()) {
                putRail(ctx, external(ctx) ? "EXTERNAL_OMNICANAL" : "OMNICANAL_TRANSFER");
            }
        } else {
            viaInterface = omnicanalCatalogInvocationService.invokeOperation(
                    tx, provider, operationCode, stepCode, ctx, safeInput);
            if (viaInterface.isPresent()) {
                putRail(ctx, external(ctx) ? "EXTERNAL_OMNICANAL" : "OMNICANAL_OPERATION");
            }
        }

        if (viaInterface.isPresent()) {
            return viaInterface.get();
        }

        throw new BusinessException(
                "INTERFACE_REQUIRED",
                "Appel via InterfaceUtilisateur REQUEST requis pour " + operationCode
                        + " (provider=" + provider + ") — codeFse / IU manquant ; rail direct supprimé"
        );
    }

    /**
     * @deprecated Utiliser {@link #invokeForProvider(PaymentTransaction, String, String, String, Map, Map)}.
     * Le flag failIfOmnicanalRequired est ignoré (toujours obligatoire).
     */
    @Deprecated
    public ProviderExecutionResult invokeForProvider(
            PaymentTransaction tx,
            String invokeProvider,
            String stepCode,
            String operationCode,
            Map<String, Object> input,
            Map<String, Object> context,
            boolean failIfOmnicanalRequired
    ) {
        return invokeForProvider(tx, invokeProvider, stepCode, operationCode, input, context);
    }

    public String resolveProvider(
            PaymentTransaction tx,
            Map<String, Object> context,
            ProviderInvokeRail rail
    ) {
        String billProvider = firstNonBlank(
                stringFromMap(context, "billProviderCode"),
                tx.getProviderCode());
        String payProvider = firstNonBlank(
                stringFromMap(context, "payProviderCode"),
                stringFromMap(context, "providerCode"),
                tx.getProviderCode());
        return switch (rail != null ? rail : ProviderInvokeRail.PRIMARY) {
            case BILLER -> billProvider;
            case PAYER -> payProvider;
            case PRIMARY -> firstNonBlank(stringFromMap(context, "providerCode"), tx.getProviderCode());
        };
    }

    public String resolveProvider(
            PaymentTransaction tx,
            Map<String, Object> context,
            ProviderCallOperationSpec spec
    ) {
        return resolveProvider(tx, context, spec != null ? spec.rail() : ProviderInvokeRail.PRIMARY);
    }

    /**
     * Rôle catalogue ({@code roleAssignments}) prioritaire, sinon rail de la spec.
     */
    public String resolveProvider(
            PaymentTransaction tx,
            Map<String, Object> context,
            ProviderCallOperationSpec spec,
            String actorRole
    ) {
        if (StringUtils.hasText(actorRole) && context != null
                && context.get("roleAssignments") instanceof Map<?, ?> raw) {
            Object v = raw.get(actorRole);
            if (v == null) {
                v = raw.get(actorRole.toUpperCase());
            }
            if (v != null && !String.valueOf(v).isBlank()) {
                return String.valueOf(v).trim();
            }
        }
        return resolveProvider(tx, context, spec);
    }

    private static boolean external(Map<String, Object> context) {
        return context != null && Boolean.TRUE.equals(context.get("externalOmnicanal"));
    }

    private static void putRail(Map<String, Object> context, String rail) {
        if (context == null) {
            return;
        }
        try {
            context.put("invocationRail", rail);
        } catch (UnsupportedOperationException ignored) {
            // Map.of() / immutable
        }
    }

    private static String stringFromMap(Map<String, Object> map, String key) {
        if (map == null || key == null || map.get(key) == null) {
            return null;
        }
        String v = String.valueOf(map.get(key)).trim();
        return v.isEmpty() ? null : v;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return null;
    }
}
