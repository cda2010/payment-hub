package com.gateway.payment.catalog.casting;

import com.gateway.payment.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogProductCastingServiceTest {

    private CatalogProductCastingService service;

    @BeforeEach
    void setUp() {
        service = new CatalogProductCastingService();
    }

    @Test
    void parseAndValidate_accountTransferShape() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("roles", List.of(
                Map.of("code", "SOURCE", "label", "Émetteur", "selection", "USER", "cardinality", "ONE_REQUIRED"),
                Map.of("code", "DESTINATION", "selection", "USER")
        ));
        config.put("participants", List.of(
                Map.of("role", "SOURCE", "providerCode", "CORIS_MONEY", "default", true),
                Map.of("role", "DESTINATION", "providerCode", "WAVE", "default", true)
        ));
        config.put("bindings", List.of(
                Map.of("step", "SELECT_SOURCE_ACCOUNT", "kind", "UI", "actorRole", "SOURCE",
                        "formKey", "account-transfer-source"),
                Map.of("step", "EXECUTE_TRANSFER", "kind", "INVOKE", "actorRole", "SOURCE",
                        "operation", "TRANSFER_EXECUTE")
        ));

        CatalogProductCasting casting = service.parse(config);
        service.validate(casting);

        assertEquals(2, casting.roles().size());
        assertEquals(2, casting.bindings().size());
        assertTrue(casting.findBinding("SELECT_SOURCE_ACCOUNT", "CORIS_MONEY").isPresent());
        assertEquals("TRANSFER_EXECUTE",
                casting.findBinding("EXECUTE_TRANSFER", null).orElseThrow().operation());
    }

    @Test
    void validate_rejectsUnknownPlatformRole() {
        CatalogProductCasting casting = new CatalogProductCasting(
                List.of(new CatalogRoleDefinition(
                        "FOO_BAR", "Inconnu",
                        CatalogRoleCardinality.ONE_REQUIRED,
                        CatalogRoleSelection.USER,
                        null)),
                List.of(),
                List.of()
        );
        BusinessException ex = assertThrows(BusinessException.class, () -> service.validate(casting));
        assertEquals("CATALOG_ROLE_UNKNOWN", ex.getCode());
    }

    @Test
    void parse_fillsDefaultLabelFromPlatformRole() {
        CatalogProductCasting casting = service.parse(Map.of(
                "roles", List.of(Map.of("code", "PAYER", "selection", "USER"))
        ));
        assertEquals("Payeur", casting.roles().get(0).label());
    }

    @Test
    void validate_rejectsUnknownParticipantRole() {
        CatalogProductCasting casting = new CatalogProductCasting(
                List.of(new CatalogRoleDefinition(
                        "SOURCE", "Émetteur",
                        CatalogRoleCardinality.ONE_REQUIRED,
                        CatalogRoleSelection.USER,
                        null)),
                List.of(new CatalogParticipantDefinition("DESTINATION", "WAVE", true)),
                List.of()
        );
        BusinessException ex = assertThrows(BusinessException.class, () -> service.validate(casting));
        assertEquals("CATALOG_PARTICIPANT_ROLE_UNKNOWN", ex.getCode());
    }

    @Test
    void validate_rejectsDuplicateDefault() {
        CatalogProductCasting casting = new CatalogProductCasting(
                List.of(new CatalogRoleDefinition(
                        "SOURCE", null,
                        CatalogRoleCardinality.ONE_REQUIRED,
                        CatalogRoleSelection.USER,
                        null)),
                List.of(
                        new CatalogParticipantDefinition("SOURCE", "CORIS_MONEY", true),
                        new CatalogParticipantDefinition("SOURCE", "WAVE", true)
                ),
                List.of()
        );
        BusinessException ex = assertThrows(BusinessException.class, () -> service.validate(casting));
        assertEquals("CATALOG_PARTICIPANT_DEFAULT_DUPLICATE", ex.getCode());
    }

    @Test
    void validate_fixedRequiresParticipant() {
        CatalogProductCasting casting = new CatalogProductCasting(
                List.of(new CatalogRoleDefinition(
                        "BILLER", "Facturier",
                        CatalogRoleCardinality.ONE_REQUIRED,
                        CatalogRoleSelection.FIXED,
                        null)),
                List.of(),
                List.of()
        );
        BusinessException ex = assertThrows(BusinessException.class, () -> service.validate(casting));
        assertEquals("CATALOG_ROLE_FIXED_NO_PARTICIPANT", ex.getCode());
    }

    @Test
    void resolveRoleAssignments_usesDefaultsAndClient() {
        CatalogProductCasting casting = service.parse(Map.of(
                "roles", List.of(
                        Map.of("code", "SOURCE", "selection", "USER"),
                        Map.of("code", "DESTINATION", "selection", "USER")
                ),
                "participants", List.of(
                        Map.of("role", "SOURCE", "providerCode", "CORIS_MONEY", "default", true),
                        Map.of("role", "DESTINATION", "providerCode", "WAVE", "default", true)
                )
        ));
        Map<String, String> assigned = service.resolveRoleAssignments(
                casting,
                Map.of("roleAssignments", Map.of("SOURCE", "ORANGE_MONEY"))
        );
        assertEquals("ORANGE_MONEY", assigned.get("SOURCE"));
        assertEquals("WAVE", assigned.get("DESTINATION"));
    }

    @Test
    void normalizeConfiguration_uppercasesAndKeepsOtherKeys() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("serviceType", "TRANSFERT");
        config.put("steps", List.of("CONFIRM"));
        config.put("roles", List.of(Map.of("code", "source", "selection", "user")));
        config.put("participants", List.of(
                Map.of("role", "source", "providerCode", "coris_money", "default", true)
        ));
        config.put("bindings", List.of(
                Map.of("step", "CONFIRM", "kind", "ui", "actorRole", "source", "formKey", "account-transfer-confirm")
        ));

        Map<String, Object> normalized = service.normalizeConfiguration(config);
        assertEquals("TRANSFERT", normalized.get("serviceType"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> roles = (List<Map<String, Object>>) normalized.get("roles");
        assertEquals("SOURCE", roles.get(0).get("code"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> bindings = (List<Map<String, Object>>) normalized.get("bindings");
        assertEquals("UI", bindings.get(0).get("kind"));
        assertFalse(service.parse(normalized).isEmpty());
    }

    @Test
    void emptyCasting_ok() {
        service.validate(new CatalogProductCasting(List.of(), List.of(), List.of()));
        assertTrue(service.normalizeConfiguration(Map.of("steps", List.of("A"))).containsKey("steps"));
    }
}
