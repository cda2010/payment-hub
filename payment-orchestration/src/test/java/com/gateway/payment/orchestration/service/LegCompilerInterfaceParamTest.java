package com.gateway.payment.orchestration.service;

import com.gateway.payment.orchestration.domain.ParametreUtilisateur;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LegCompilerInterfaceParamTest {

    /**
     * GENERATE_OTP Coris Money : telephone / msisdn / phone / phoneNumber partagent
     * le code méthode TELEPHONE. Seul msisdn est saisi (mobile émetteur du formulaire)
     * et les alias vides ne doivent pas l'effacer, quel que soit leur ordre.
     */
    @Test
    void emptyAliasDoesNotEraseFilledMethodSlot() {
        List<ParametreUtilisateur> params = List.of(
                alias("phoneNumber", "TELEPHONE"),
                alias("telephone", "TELEPHONE"),
                alias("msisdn", "TELEPHONE"),
                alias("phone", "TELEPHONE")
        );

        Map<String, String> resolved = LegCompilerService.bindInterfaceParams(
                params, new HashMap<>(Map.of("msisdn", "22670582844")));

        assertEquals("22670582844", resolved.get("TELEPHONE"));

        LegCompilerService.propagateInterfaceAliases(params, resolved);

        assertEquals("22670582844", resolved.get("phoneNumber"));
        assertEquals("22670582844", resolved.get("telephone"));
        assertEquals("22670582844", resolved.get("phone"));
        assertEquals("22670582844", resolved.get("msisdn"));
    }

    /** Une valeur par défaut en base reste utilisée quand aucun alias n'est saisi. */
    @Test
    void fallsBackOnConfiguredDefault() {
        List<ParametreUtilisateur> params = List.of(
                ParametreUtilisateur.builder()
                        .codeParametre("codeFse")
                        .valeur("0011")
                        .build()
        );

        Map<String, String> resolved = LegCompilerService.bindInterfaceParams(params, new HashMap<>());

        assertEquals("0011", resolved.get("codeFse"));
    }

    private static ParametreUtilisateur alias(String code, String methode) {
        return ParametreUtilisateur.builder()
                .codeParametre(code)
                .codeParametreMethode(methode)
                .build();
    }
}
