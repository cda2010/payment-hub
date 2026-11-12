package com.gateway.payment.orchestration.service;

import com.gateway.payment.orchestration.domain.CompteFse;
import com.gateway.payment.orchestration.domain.TypeCompte;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompteFieldExtractorRegistryTest {

    private final CompteFieldExtractorRegistry registry = CompteFieldExtractorRegistry.defaults();

    @Test
    void resolve_mapsLegacyAndTypedCodes() {
        assertSame(CompteFieldExtractors.NUMERO_COMPTE, registry.resolve("getNumeroCompte"));
        assertSame(CompteFieldExtractors.NUMERO_COMPTE, registry.resolve("NUMERO_COMPTE"));
        assertSame(CompteFieldExtractors.FSE_TENEUR, registry.resolve("FSE_TENEUR"));
        assertSame(CompteFieldExtractors.TEL_QUEL, registry.resolve(null));
        assertSame(CompteFieldExtractors.TEL_QUEL, registry.resolve("inconnu"));
        assertTrue(CompteFieldExtractors.TEL_QUEL.passthrough());
    }

    @Test
    void extract_readsCompteFields() {
        CompteFse compte = CompteFse.builder()
                .numeroCompte("0011000003090")
                .fseTeneur("0011")
                .fseTitulaire("0011")
                .codeBanque("00000")
                .codeAgence("")
                .extype("CompteFse")
                .libelleCompte("")
                .agenceCode("01001")
                .typeCompte(TypeCompte.builder().codeParametre("CPTELORO").libelle("LORO").build())
                .build();

        assertEquals("0011000003090", CompteFieldExtractors.NUMERO_COMPTE.extract(compte));
        assertEquals("0011", CompteFieldExtractors.FSE_TENEUR.extract(compte));
        assertEquals("0011", CompteFieldExtractors.FSE_TITULAIRE.extract(compte));
        assertEquals("00000", CompteFieldExtractors.CODE_BANQUE.extract(compte));
        assertEquals("CPTELORO", CompteFieldExtractors.TYPE_COMPTE.extract(compte));
        assertSame(CompteFieldExtractors.CODE_BANQUE, registry.resolve("getCodeBanque"));
        assertSame(CompteFieldExtractors.CLE, registry.resolve("CLE"));
    }

    @Test
    void customBeanOverridesOrExtendsRegistry() {
        CompteFieldExtractor custom = new CompteFieldExtractor() {
            @Override
            public String code() {
                return "LIBELLE_DEMO";
            }

            @Override
            public String extract(CompteFse compte) {
                return "demo";
            }
        };
        CompteFieldExtractorRegistry extended = new CompteFieldExtractorRegistry(java.util.List.of(custom));
        assertEquals("demo", extended.resolve("LIBELLE_DEMO").extract(null));
        assertSame(CompteFieldExtractors.NUMERO_COMPTE, extended.resolve("NUMERO_COMPTE"));
    }
}
