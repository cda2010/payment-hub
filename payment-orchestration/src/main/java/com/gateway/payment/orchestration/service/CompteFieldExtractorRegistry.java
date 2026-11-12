package com.gateway.payment.orchestration.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Registry des extracteurs compte : builtins + beans Spring optionnels.
 * Alias legacy Omnicanal ({@code getCodeBanque}, …) inclus.
 */
@Service
public class CompteFieldExtractorRegistry {

    private final Map<String, CompteFieldExtractor> byCode;

    @Autowired
    public CompteFieldExtractorRegistry(
            @Autowired(required = false) List<CompteFieldExtractor> customExtractors
    ) {
        Map<String, CompteFieldExtractor> map = new LinkedHashMap<>();
        for (CompteFieldExtractor extractor : CompteFieldExtractors.builtins()) {
            put(map, extractor);
        }
        if (customExtractors != null) {
            for (CompteFieldExtractor extractor : customExtractors) {
                put(map, extractor);
            }
        }
        this.byCode = Map.copyOf(map);
    }

    public static CompteFieldExtractorRegistry defaults() {
        return new CompteFieldExtractorRegistry(List.of());
    }

    public CompteFieldExtractor resolve(String attribut) {
        if (!StringUtils.hasText(attribut)) {
            return CompteFieldExtractors.TEL_QUEL;
        }
        String key = normalize(attribut);
        CompteFieldExtractor alias = legacyAlias(key);
        if (alias != null) {
            return alias;
        }
        CompteFieldExtractor found = byCode.get(key);
        // Inconnu : numéro (comportement historique Omnicanal TEL_QUEL / défaut),
        // mais uniquement si attribut vide — ici on préfère NUMERO pour compat
        // des attributs non mappés qui attendaient le compte tel quel.
        return found != null ? found : CompteFieldExtractors.TEL_QUEL;
    }

    public CompteFieldExtractor numeroCompte() {
        return CompteFieldExtractors.NUMERO_COMPTE;
    }

    public Map<String, CompteFieldExtractor> asMap() {
        return byCode;
    }

    private static CompteFieldExtractor legacyAlias(String key) {
        return switch (key) {
            case "GETNUMEROCOMPTE", "NUMEROCOMPTE" -> CompteFieldExtractors.NUMERO_COMPTE;
            case "GETCODEBANQUE", "CODEBANQUE", "COMPTEBANQUE" -> CompteFieldExtractors.CODE_BANQUE;
            case "GETCODEAGENCE", "CODEAGENCE" -> CompteFieldExtractors.CODE_AGENCE;
            case "GETCLE", "CLE" -> CompteFieldExtractors.CLE;
            case "GETDEVISE", "DEVISE" -> CompteFieldExtractors.DEVISE;
            case "GETFSETENEUR", "FSETENEUR" -> CompteFieldExtractors.FSE_TENEUR;
            case "GETFSETITULAIRE", "FSETITULAIRE" -> CompteFieldExtractors.FSE_TITULAIRE;
            case "GETLIBELLECOMPTE", "LIBELLECOMPTE" -> CompteFieldExtractors.LIBELLE_COMPTE;
            case "GETCODEUOTITULAIRE", "CODEUOTITULAIRE", "CODEUO" -> CompteFieldExtractors.CODE_UO_TITULAIRE;
            case "GETAGENCECODE", "AGENCECODE", "AGENCE" -> CompteFieldExtractors.AGENCE_CODE;
            case "GETTYPECOMPTE", "TYPECOMPTE" -> CompteFieldExtractors.TYPE_COMPTE;
            default -> null;
        };
    }

    private static void put(Map<String, CompteFieldExtractor> map, CompteFieldExtractor extractor) {
        if (extractor == null || !StringUtils.hasText(extractor.code())) {
            return;
        }
        map.put(normalize(extractor.code()), extractor);
    }

    private static String normalize(String code) {
        return code.trim().toUpperCase(Locale.ROOT).replace("_", "");
    }
}
