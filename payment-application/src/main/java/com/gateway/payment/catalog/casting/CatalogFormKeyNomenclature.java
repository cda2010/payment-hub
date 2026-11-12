package com.gateway.payment.catalog.casting;

import org.springframework.util.StringUtils;

import java.util.Locale;

/**
 * Nomenclature des formKeys Form.io pour les écrans multi-providers.
 * <p>
 * Pattern : {@code {productSlug}-{actorRoleSlug}-{providerSlug}}
 * <br>Exemple : {@code ACCOUNT_TRANSFER} + {@code SOURCE} + {@code WAVE}
 * → {@code account-transfer-source-wave}
 * <p>
 * Résolution runtime (voir {@link CatalogUiBindingResolver}) :
 * <ol>
 *   <li>Override explicite {@code bindings[].providerCode + formKey}</li>
 *   <li>Convention si formKey binding vide et casting connu</li>
 *   <li>{@code bindings[].formKey} partagé (même écran pour tous)</li>
 *   <li>formKey BPMN</li>
 * </ol>
 */
public final class CatalogFormKeyNomenclature {

    private CatalogFormKeyNomenclature() {
    }

    /**
     * formKey conventionnel, ou {@code null} si un segment manque.
     */
    public static String conventionalFormKey(String productCode, String actorRole, String providerCode) {
        String product = slug(productCode);
        String role = slug(actorRole);
        String provider = slug(providerCode);
        if (!StringUtils.hasText(product) || !StringUtils.hasText(role) || !StringUtils.hasText(provider)) {
            return null;
        }
        return product + "-" + role + "-" + provider;
    }

    /**
     * Slug Form.io : lower-case, {@code _} / espaces → {@code -}, compacte les tirets.
     */
    public static String slug(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String s = raw.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[\\s_]+", "-")
                .replaceAll("[^a-z0-9-]", "")
                .replaceAll("-{2,}", "-");
        if (s.startsWith("-")) {
            s = s.substring(1);
        }
        if (s.endsWith("-")) {
            s = s.substring(0, s.length() - 1);
        }
        return StringUtils.hasText(s) ? s : null;
    }
}
