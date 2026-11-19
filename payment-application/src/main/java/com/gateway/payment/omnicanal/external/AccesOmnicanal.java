package com.gateway.payment.omnicanal.external;

import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Seul accès HTTP du moteur vers Omnicanal.
 * Catalogue, comptes, schémas et exécution passent par ici.
 */
public interface AccesOmnicanal {

    boolean enabled();

    Map<String, Object> boundary(String codeFse, String codeMethode, String otrParametre);

    Map<String, Object> presentation(ExternalPresentationRequest request);

    List<Map<String, Object>> comptes();

    List<Map<String, Object>> schemas();

    List<Map<String, Object>> fournisseurs();

    Map<String, Object> updateFournisseur(String codeFse, Map<String, Object> body);

    Map<String, Object> catalog(String codeFse);

    static String otrParametre(Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        return params.entrySet().stream()
                .filter(e -> StringUtils.hasText(e.getKey()))
                .map(e -> e.getKey() + "=" + (e.getValue() == null ? "" : e.getValue()))
                .collect(Collectors.joining("|"));
    }
}
