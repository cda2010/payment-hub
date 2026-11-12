package com.gateway.payment.catalog.casting;

import com.gateway.payment.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Parse / valide roles, participants et bindings d'une version catalogue (P1–P4).
 */
@Service
public class CatalogProductCastingService {

    public CatalogProductCasting parse(Map<String, Object> configuration) {
        if (configuration == null || configuration.isEmpty()) {
            return new CatalogProductCasting(List.of(), List.of(), List.of());
        }
        return new CatalogProductCasting(
                parseRoles(configuration.get("roles")),
                parseParticipants(configuration.get("participants")),
                parseBindings(configuration.get("bindings"))
        );
    }

    public void validate(CatalogProductCasting casting) {
        if (casting == null || casting.isEmpty()) {
            return;
        }
        Set<String> roleCodes = new LinkedHashSet<>();
        for (CatalogRoleDefinition role : casting.roles()) {
            if (!StringUtils.hasText(role.code())) {
                throw new BusinessException("CATALOG_ROLE_INVALID", "roles[].code obligatoire");
            }
            String code = role.code();
            if (!CatalogProductRoleCode.isKnown(code)) {
                throw new BusinessException(
                        "CATALOG_ROLE_UNKNOWN",
                        "Rôle hors référentiel plateforme: " + code
                                + " — attendu: " + Arrays.toString(CatalogProductRoleCode.values()));
            }
            if (!roleCodes.add(code)) {
                throw new BusinessException("CATALOG_ROLE_DUPLICATE", "Rôle dupliqué: " + code);
            }
            if (role.selection() == CatalogRoleSelection.DERIVED) {
                if (!StringUtils.hasText(role.derivedFrom())) {
                    throw new BusinessException(
                            "CATALOG_ROLE_DERIVED_MISSING",
                            "roles[].derivedFrom obligatoire pour selection=DERIVED (" + code + ")");
                }
                if (!CatalogProductRoleCode.isKnown(role.derivedFrom())) {
                    throw new BusinessException(
                            "CATALOG_ROLE_DERIVED_UNKNOWN",
                            "derivedFrom hors référentiel: " + role.derivedFrom());
                }
            }
        }
        for (CatalogRoleDefinition role : casting.roles()) {
            if (role.selection() == CatalogRoleSelection.DERIVED) {
                String from = normalizeCode(role.derivedFrom());
                if (!roleCodes.contains(from)) {
                    throw new BusinessException(
                            "CATALOG_ROLE_DERIVED_UNKNOWN",
                            "derivedFrom inconnu: " + from + " (rôle " + role.code() + ")");
                }
                if (from.equals(role.code())) {
                    throw new BusinessException(
                            "CATALOG_ROLE_DERIVED_SELF",
                            "Un rôle ne peut pas dériver de lui-même: " + role.code());
                }
            }
        }

        Set<String> defaultedRoles = new LinkedHashSet<>();
        for (CatalogParticipantDefinition p : casting.participants()) {
            if (!StringUtils.hasText(p.role())) {
                throw new BusinessException("CATALOG_PARTICIPANT_INVALID", "participants[].role obligatoire");
            }
            if (!StringUtils.hasText(p.providerCode())) {
                throw new BusinessException(
                        "CATALOG_PARTICIPANT_INVALID",
                        "participants[].providerCode obligatoire");
            }
            if (!casting.roles().isEmpty() && !roleCodes.contains(p.role())) {
                throw new BusinessException(
                        "CATALOG_PARTICIPANT_ROLE_UNKNOWN",
                        "participants[].role inconnu: " + p.role());
            }
            if (p.defaultParticipant()) {
                if (!defaultedRoles.add(p.role())) {
                    throw new BusinessException(
                            "CATALOG_PARTICIPANT_DEFAULT_DUPLICATE",
                            "Un seul default=true par rôle: " + p.role());
                }
            }
        }

        for (CatalogRoleDefinition role : casting.roles()) {
            if (role.selection() == CatalogRoleSelection.FIXED) {
                long count = casting.participants().stream()
                        .filter(p -> p.role().equals(role.code()))
                        .count();
                if (count == 0) {
                    throw new BusinessException(
                            "CATALOG_ROLE_FIXED_NO_PARTICIPANT",
                            "Rôle FIXED sans participant: " + role.code());
                }
            }
        }

        for (CatalogBindingDefinition b : casting.bindings()) {
            if (!StringUtils.hasText(b.step())) {
                throw new BusinessException("CATALOG_BINDING_INVALID", "bindings[].step obligatoire");
            }
            if (b.kind() == null) {
                throw new BusinessException("CATALOG_BINDING_INVALID", "bindings[].kind obligatoire");
            }
            if (StringUtils.hasText(b.actorRole()) && !roleCodes.isEmpty() && !roleCodes.contains(b.actorRole())) {
                throw new BusinessException(
                        "CATALOG_BINDING_ROLE_UNKNOWN",
                        "bindings[].actorRole inconnu: " + b.actorRole());
            }
            if (b.kind() == CatalogBindingKind.UI) {
                if (b.formSource() == CatalogFormSource.BILLER_PRODUCT) {
                    if (!StringUtils.hasText(b.billerScreen())) {
                        throw new BusinessException(
                                "CATALOG_BINDING_BILLER_SCREEN",
                                "bindings[].billerScreen requis (LOOKUP|CONFIRM|AMOUNT|LIST) pour step "
                                        + b.step());
                    }
                } else if (!StringUtils.hasText(b.formKey()) && !StringUtils.hasText(b.actorRole())) {
                    throw new BusinessException(
                            "CATALOG_BINDING_FORM_KEY",
                            "UI step " + b.step()
                                    + " : formKey partagé OU actorRole (nomenclature product-role-provider)");
                }
            }
            if (b.kind() == CatalogBindingKind.INVOKE && !StringUtils.hasText(b.operation())) {
                throw new BusinessException(
                        "CATALOG_BINDING_OPERATION",
                        "bindings[].operation requis pour INVOKE step " + b.step());
            }
            if (b.kind() == CatalogBindingKind.CAPABILITY && !StringUtils.hasText(b.capability())) {
                throw new BusinessException(
                        "CATALOG_BINDING_CAPABILITY",
                        "bindings[].capability requis pour CAPABILITY step " + b.step());
            }
        }
    }

    public Map<String, Object> normalizeConfiguration(Map<String, Object> configuration) {
        Map<String, Object> out = configuration == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(configuration);
        CatalogProductCasting casting = parse(out);
        validate(casting);
        if (!casting.roles().isEmpty()) {
            out.put("roles", toRoleMaps(casting.roles()));
        } else {
            out.remove("roles");
        }
        if (!casting.participants().isEmpty()) {
            out.put("participants", toParticipantMaps(casting.participants()));
        } else {
            out.remove("participants");
        }
        if (!casting.bindings().isEmpty()) {
            out.put("bindings", toBindingMaps(casting.bindings()));
        } else {
            out.remove("bindings");
        }
        return out;
    }

    /**
     * Applique les assignations client + defaults participants + rôles DERIVED/FIXED.
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> resolveRoleAssignments(
            CatalogProductCasting casting,
            Map<String, Object> data
    ) {
        Map<String, String> assignments = new LinkedHashMap<>();
        if (data != null) {
            Object raw = data.get("roleAssignments");
            if (raw instanceof Map<?, ?> map) {
                for (Map.Entry<?, ?> e : map.entrySet()) {
                    if (e.getKey() != null && e.getValue() != null) {
                        String role = normalizeCode(String.valueOf(e.getKey()));
                        String provider = normalizeCode(String.valueOf(e.getValue()));
                        if (role != null && provider != null) {
                            assignments.put(role, provider);
                        }
                    }
                }
            }
        }
        if (casting == null) {
            return assignments;
        }
        for (CatalogParticipantDefinition p : casting.participants()) {
            if (p.defaultParticipant() && !assignments.containsKey(p.role())) {
                assignments.put(p.role(), p.providerCode());
            }
        }
        for (CatalogRoleDefinition role : casting.roles()) {
            if (role.selection() == CatalogRoleSelection.FIXED && !assignments.containsKey(role.code())) {
                casting.participants().stream()
                        .filter(p -> p.role().equals(role.code()))
                        .findFirst()
                        .ifPresent(p -> assignments.put(role.code(), p.providerCode()));
            }
            if (role.selection() == CatalogRoleSelection.DERIVED && role.derivedFrom() != null) {
                String from = assignments.get(role.derivedFrom());
                if (from != null) {
                    assignments.put(role.code(), from);
                }
            }
        }
        for (CatalogRoleDefinition role : casting.roles()) {
            if (role.cardinality() == CatalogRoleCardinality.ONE_REQUIRED
                    && role.selection() == CatalogRoleSelection.USER
                    && !assignments.containsKey(role.code())) {
                throw new BusinessException(
                        "CATALOG_ROLE_ASSIGNMENT_REQUIRED",
                        "Assignation requise pour le rôle " + role.code());
            }
        }
        return assignments;
    }

    public String providerForRole(Map<String, String> roleAssignments, String actorRole, String fallback) {
        if (StringUtils.hasText(actorRole) && roleAssignments != null) {
            String p = roleAssignments.get(normalizeCode(actorRole));
            if (StringUtils.hasText(p)) {
                return p;
            }
        }
        return fallback;
    }

    private static List<Map<String, Object>> toRoleMaps(List<CatalogRoleDefinition> roles) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (CatalogRoleDefinition r : roles) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", r.code());
            if (StringUtils.hasText(r.label())) {
                m.put("label", r.label());
            }
            m.put("cardinality", r.cardinality().name());
            m.put("selection", r.selection().name());
            if (StringUtils.hasText(r.derivedFrom())) {
                m.put("derivedFrom", r.derivedFrom());
            }
            list.add(m);
        }
        return list;
    }

    private static List<Map<String, Object>> toParticipantMaps(List<CatalogParticipantDefinition> participants) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (CatalogParticipantDefinition p : participants) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("role", p.role());
            m.put("providerCode", p.providerCode());
            m.put("default", p.defaultParticipant());
            list.add(m);
        }
        return list;
    }

    private static List<Map<String, Object>> toBindingMaps(List<CatalogBindingDefinition> bindings) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (CatalogBindingDefinition b : bindings) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("step", b.step());
            m.put("kind", b.kind().name());
            if (StringUtils.hasText(b.actorRole())) {
                m.put("actorRole", b.actorRole());
            }
            if (StringUtils.hasText(b.providerCode())) {
                m.put("providerCode", b.providerCode());
            }
            if (StringUtils.hasText(b.formKey())) {
                m.put("formKey", b.formKey());
            }
            if (b.formSource() != null) {
                m.put("formSource", b.formSource().name());
            }
            if (StringUtils.hasText(b.billerScreen())) {
                m.put("billerScreen", b.billerScreen());
            }
            if (StringUtils.hasText(b.operation())) {
                m.put("operation", b.operation());
            }
            if (StringUtils.hasText(b.capability())) {
                m.put("capability", b.capability());
            }
            if (b.params() != null && !b.params().isEmpty()) {
                m.put("params", b.params());
            }
            list.add(m);
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    private List<CatalogRoleDefinition> parseRoles(Object raw) {
        if (raw == null) {
            return List.of();
        }
        if (!(raw instanceof List<?> list)) {
            throw new BusinessException("CATALOG_ROLES_INVALID", "configuration.roles doit être un tableau");
        }
        List<CatalogRoleDefinition> out = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                throw new BusinessException("CATALOG_ROLE_INVALID", "Chaque rôle doit être un objet");
            }
            Map<String, Object> m = (Map<String, Object>) map;
            String code = normalizeCode(stringVal(m, "code"));
            String label = stringVal(m, "label");
            CatalogRoleCardinality cardinality = parseEnum(
                    stringVal(m, "cardinality"),
                    CatalogRoleCardinality.class,
                    CatalogRoleCardinality.ONE_REQUIRED,
                    "CATALOG_ROLE_CARDINALITY_INVALID");
            CatalogRoleSelection selection = parseEnum(
                    stringVal(m, "selection"),
                    CatalogRoleSelection.class,
                    CatalogRoleSelection.USER,
                    "CATALOG_ROLE_SELECTION_INVALID");
            String derivedFrom = normalizeCode(stringVal(m, "derivedFrom", "derived_from"));
            if (!StringUtils.hasText(code)) {
                throw new BusinessException("CATALOG_ROLE_INVALID", "roles[].code obligatoire");
            }
            if (!CatalogProductRoleCode.isKnown(code)) {
                throw new BusinessException(
                        "CATALOG_ROLE_UNKNOWN",
                        "Rôle hors référentiel plateforme: " + code
                                + " — attendu: " + Arrays.toString(CatalogProductRoleCode.values()));
            }
            if (!StringUtils.hasText(label)) {
                label = CatalogProductRoleCode.tryParse(code).map(CatalogProductRoleCode::defaultLabel).orElse(null);
            }
            out.add(new CatalogRoleDefinition(code, blankToNull(label), cardinality, selection, blankToNull(derivedFrom)));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private List<CatalogParticipantDefinition> parseParticipants(Object raw) {
        if (raw == null) {
            return List.of();
        }
        if (!(raw instanceof List<?> list)) {
            throw new BusinessException(
                    "CATALOG_PARTICIPANTS_INVALID",
                    "configuration.participants doit être un tableau");
        }
        List<CatalogParticipantDefinition> out = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                throw new BusinessException(
                        "CATALOG_PARTICIPANT_INVALID",
                        "Chaque participant doit être un objet");
            }
            Map<String, Object> m = (Map<String, Object>) map;
            String role = normalizeCode(stringVal(m, "role"));
            String provider = normalizeCode(stringVal(m, "providerCode", "provider_code", "provider"));
            boolean isDefault = boolVal(m, "default", "defaultParticipant", "isDefault");
            out.add(new CatalogParticipantDefinition(role, provider, isDefault));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private List<CatalogBindingDefinition> parseBindings(Object raw) {
        if (raw == null) {
            return List.of();
        }
        if (!(raw instanceof List<?> list)) {
            throw new BusinessException(
                    "CATALOG_BINDINGS_INVALID",
                    "configuration.bindings doit être un tableau");
        }
        List<CatalogBindingDefinition> out = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                throw new BusinessException("CATALOG_BINDING_INVALID", "Chaque binding doit être un objet");
            }
            Map<String, Object> m = (Map<String, Object>) map;
            String step = stringVal(m, "step", "activityId", "activity_id");
            CatalogBindingKind kind = parseEnum(
                    stringVal(m, "kind"),
                    CatalogBindingKind.class,
                    CatalogBindingKind.UI,
                    "CATALOG_BINDING_KIND_INVALID");
            String actorRole = normalizeCode(stringVal(m, "actorRole", "actor_role"));
            String providerCode = normalizeCode(stringVal(m, "providerCode", "provider_code"));
            String formKey = blankToNull(stringVal(m, "formKey", "form_key"));
            CatalogFormSource formSource = parseEnum(
                    stringVal(m, "formSource", "form_source"),
                    CatalogFormSource.class,
                    formKey != null ? CatalogFormSource.FORM_KEY : CatalogFormSource.FORM_KEY,
                    "CATALOG_BINDING_FORM_SOURCE_INVALID");
            if ("BILLER_PRODUCT".equalsIgnoreCase(stringVal(m, "formSource", "form_source"))) {
                formSource = CatalogFormSource.BILLER_PRODUCT;
            }
            if ("RECAP".equalsIgnoreCase(stringVal(m, "formSource", "form_source"))) {
                formSource = CatalogFormSource.RECAP;
                if (formKey == null) {
                    formKey = "product-recap";
                }
            }
            if ("DOCUMENTS".equalsIgnoreCase(stringVal(m, "formSource", "form_source"))) {
                formSource = CatalogFormSource.DOCUMENTS;
                if (formKey == null) {
                    formKey = "product-document-upload";
                }
            }
            String billerScreen = normalizeCode(stringVal(m, "billerScreen", "biller_screen", "screen"));
            String operation = normalizeCode(stringVal(m, "operation", "operationCode", "operation_code"));
            String capability = normalizeCode(stringVal(m, "capability", "capabilityCode", "capability_code"));
            Map<String, Object> params = Map.of();
            if (m.get("params") instanceof Map<?, ?> rawParams) {
                Map<String, Object> copy = new LinkedHashMap<>();
                rawParams.forEach((k, v) -> {
                    if (k != null) {
                        copy.put(String.valueOf(k), v);
                    }
                });
                params = copy;
            }
            if (!StringUtils.hasText(step)) {
                throw new BusinessException("CATALOG_BINDING_INVALID", "bindings[].step obligatoire");
            }
            out.add(new CatalogBindingDefinition(
                    step.trim(),
                    kind,
                    blankToNull(actorRole),
                    blankToNull(providerCode),
                    formKey,
                    formSource,
                    blankToNull(billerScreen),
                    blankToNull(operation),
                    blankToNull(capability),
                    params
            ));
        }
        return out;
    }

    private static <E extends Enum<E>> E parseEnum(String raw, Class<E> type, E defaultValue, String errorCode) {
        if (!StringUtils.hasText(raw)) {
            return defaultValue;
        }
        try {
            return Enum.valueOf(type, raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(errorCode, "Valeur invalide: " + raw);
        }
    }

    private static String stringVal(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object v = map.get(key);
            if (v != null && StringUtils.hasText(String.valueOf(v))) {
                return String.valueOf(v).trim();
            }
        }
        return null;
    }

    private static boolean boolVal(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object v = map.get(key);
            if (v instanceof Boolean b) {
                return b;
            }
            if (v != null) {
                return Boolean.parseBoolean(String.valueOf(v));
            }
        }
        return false;
    }

    private static String normalizeCode(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        return raw.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
