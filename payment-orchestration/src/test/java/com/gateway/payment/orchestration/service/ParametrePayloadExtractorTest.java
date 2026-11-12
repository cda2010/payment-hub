package com.gateway.payment.orchestration.service;

import com.gateway.payment.orchestration.domain.ParametreUtilisateur;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ParametrePayloadExtractorTest {

    private ParametrePayloadExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new ParametrePayloadExtractor();
    }

    @Test
    void regex_extractsVignetteNumero() {
        ParametreUtilisateur p = ParametreUtilisateur.builder()
                .codeParametre("vignetteNumero")
                .codeParametreMethode("vignette")
                .attribut("REGEX:^(?:[^-]+-){3}(\\d+)$")
                .ordre(1)
                .build();
        Map<String, String> params = new HashMap<>();
        params.put("vignette", "SN-DK-2026-004521");

        extractor.apply(List.of(p), params);

        assertEquals("004521", params.get("vignetteNumero"));
    }

    @Test
    void regex_barePattern_defaultsToGroup1() {
        ParametreUtilisateur p = ParametreUtilisateur.builder()
                .codeParametre("billRef")
                .codeParametreMethode("reference")
                .attribut("/(\\d+)$")
                .ordre(1)
                .build();
        Map<String, String> params = new HashMap<>();
        params.put("reference", "FAC/2026/77881");

        extractor.apply(List.of(p), params);

        assertEquals("77881", params.get("billRef"));
    }

    @Test
    void groovy_usedWhenExtractScriptPresent() {
        ParametreUtilisateur p = ParametreUtilisateur.builder()
                .codeParametre("vignetteRegion")
                .codeParametreMethode("vignette")
                .attribut("REGEX:.*") // ignoré car script prioritaire
                .extractScript("def parts = (source ?: '').tokenize('-'); return parts.size() >= 2 ? parts[1] : null")
                .ordre(1)
                .build();
        Map<String, String> params = new HashMap<>();
        params.put("vignette", "SN-DK-2026-004521");

        extractor.apply(List.of(p), params);

        assertEquals("DK", params.get("vignetteRegion"));
    }

    @Test
    void sourceParametre_overridesCodeMethode() {
        ParametreUtilisateur p = ParametreUtilisateur.builder()
                .codeParametre("vignetteNumero")
                .codeParametreMethode("autre")
                .sourceParametre("vignette")
                .attribut("REGEX:^(?:[^-]+-){3}(\\d+)$")
                .ordre(1)
                .build();
        Map<String, String> params = new HashMap<>();
        params.put("vignette", "SN-DK-2026-004521");
        params.put("autre", "XX-YY-2020-000001");

        extractor.apply(List.of(p), params);

        assertEquals("004521", params.get("vignetteNumero"));
    }

    @Test
    void compteAttribut_skippedWithoutScript() {
        ParametreUtilisateur p = ParametreUtilisateur.builder()
                .codeParametre("compteDebiteur")
                .codeParametreMethode("CPTSOURCE")
                .attribut("NUMERO_COMPTE")
                .ordre(1)
                .build();
        Map<String, String> params = new HashMap<>();
        params.put("CPTSOURCE", "SN-DK-2026-004521");

        extractor.apply(List.of(p), params);

        assertNull(params.get("compteDebiteur"));
    }
}
