package com.gateway.payment.catalog.casting;

import java.util.Map;

/**
 * Binding d'une activityId BPMN ({@code configuration.bindings[]}).
 */
public record CatalogBindingDefinition(
        String step,
        CatalogBindingKind kind,
        String actorRole,
        /** Provider-specific override ; null = tous les providers du rôle. */
        String providerCode,
        String formKey,
        CatalogFormSource formSource,
        /** LOOKUP | CONFIRM | AMOUNT quand formSource=BILLER_PRODUCT. */
        String billerScreen,
        String operation,
        /** Code capability ({@code CALCULATE_FEES}, {@code NOTIFY}, …) — optionnel. */
        String capability,
        /** Params capability / soft (optionnel). */
        Map<String, Object> params
) {
    public CatalogBindingDefinition {
        params = params == null || params.isEmpty() ? Map.of() : Map.copyOf(params);
    }

    /** Compat constructeurs / tests sans capability. */
    public CatalogBindingDefinition(
            String step,
            CatalogBindingKind kind,
            String actorRole,
            String providerCode,
            String formKey,
            CatalogFormSource formSource,
            String billerScreen,
            String operation
    ) {
        this(step, kind, actorRole, providerCode, formKey, formSource, billerScreen, operation, null, Map.of());
    }
}
