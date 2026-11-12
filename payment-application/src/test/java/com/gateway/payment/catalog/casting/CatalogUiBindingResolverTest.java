package com.gateway.payment.catalog.casting;

import com.gateway.payment.catalog.document.CatalogDocumentUploadSchemaBuilder;
import com.gateway.payment.catalog.i18n.CatalogConfigurationLocalizer;
import com.gateway.payment.catalog.recap.CatalogRecapSchemaBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CatalogUiBindingResolverTest {

    private CatalogUiBindingResolver resolver;

    @BeforeEach
    void setUp() {
        // Biller deps inutiles pour les cas FORM_KEY / nomenclature ;
        // localizer sans TranslationService : aucun context de test ne porte de version catalogue.
        resolver = new CatalogUiBindingResolver(
                new CatalogProductCastingService(),
                null,
                null,
                new CatalogRecapSchemaBuilder(),
                new CatalogDocumentUploadSchemaBuilder(),
                new CatalogConfigurationLocalizer(null));
    }

    @Test
    void resolve_sharedFormKey_whenBindingHasFormKey() {
        Map<String, Object> ctx = baseContext(List.of(
                Map.of(
                        "step", "CAPTURE_DESTINATION",
                        "kind", "UI",
                        "actorRole", "DESTINATION",
                        "formSource", "FORM_KEY",
                        "formKey", "account-transfer-destination"
                )
        ));
        ctx.put("roleAssignments", Map.of("DESTINATION", "WAVE"));

        CatalogUiBindingResolver.ResolvedUi ui =
                resolver.resolve(ctx, "CAPTURE_DESTINATION", "bpmn-fallback");

        assertEquals("account-transfer-destination", ui.formKey());
        assertEquals(CatalogUiBindingResolver.FormKeyResolution.SHARED_BINDING, ui.formKeyResolution());
    }

    @Test
    void resolve_convention_whenFormKeyBlank() {
        Map<String, Object> ctx = baseContext(List.of(
                Map.of(
                        "step", "CAPTURE_DESTINATION",
                        "kind", "UI",
                        "actorRole", "DESTINATION",
                        "formSource", "FORM_KEY"
                )
        ));
        ctx.put("roleAssignments", Map.of("DESTINATION", "CORIS_MONEY"));

        CatalogUiBindingResolver.ResolvedUi ui =
                resolver.resolve(ctx, "CAPTURE_DESTINATION", "bpmn-fallback");

        assertEquals("account-transfer-destination-coris-money", ui.formKey());
        assertEquals(CatalogUiBindingResolver.FormKeyResolution.CONVENTION, ui.formKeyResolution());
    }

    @Test
    void resolve_explicitProviderOverride() {
        Map<String, Object> ctx = baseContext(List.of(
                Map.of(
                        "step", "CAPTURE_DESTINATION",
                        "kind", "UI",
                        "actorRole", "DESTINATION",
                        "formSource", "FORM_KEY",
                        "formKey", "account-transfer-destination"
                ),
                Map.of(
                        "step", "CAPTURE_DESTINATION",
                        "kind", "UI",
                        "actorRole", "DESTINATION",
                        "providerCode", "WAVE",
                        "formSource", "FORM_KEY",
                        "formKey", "wave-special-destination"
                )
        ));
        ctx.put("roleAssignments", Map.of("DESTINATION", "WAVE"));

        CatalogUiBindingResolver.ResolvedUi ui =
                resolver.resolve(ctx, "CAPTURE_DESTINATION", "bpmn-fallback");

        assertEquals("wave-special-destination", ui.formKey());
        assertEquals(CatalogUiBindingResolver.FormKeyResolution.EXPLICIT_PROVIDER, ui.formKeyResolution());
    }

    @Test
    void resolve_bpmn_whenNoBinding() {
        Map<String, Object> ctx = baseContext(List.of());
        CatalogUiBindingResolver.ResolvedUi ui = resolver.resolve(ctx, "UNKNOWN", "from-bpmn");
        assertEquals("from-bpmn", ui.formKey());
        assertEquals(CatalogUiBindingResolver.FormKeyResolution.BPMN, ui.formKeyResolution());
        assertNull(ui.actorRole());
    }

    private static Map<String, Object> baseContext(List<Map<String, Object>> bindings) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("roles", List.of(
                Map.of("code", "SOURCE", "selection", "USER"),
                Map.of("code", "DESTINATION", "selection", "USER")
        ));
        config.put("bindings", bindings);
        Map<String, Object> ctx = new LinkedHashMap<>();
        ctx.put("catalogServiceCode", "ACCOUNT_TRANSFER");
        ctx.put("catalogConfiguration", config);
        return ctx;
    }
}
