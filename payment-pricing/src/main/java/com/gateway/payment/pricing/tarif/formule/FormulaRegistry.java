package com.gateway.payment.pricing.tarif.formule;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Associe chaque code + version de formule prédéfinie à son implémentation approuvée. */
@Component
public class FormulaRegistry {

    private final Map<String, FeeFormula> formules;

    public FormulaRegistry(List<FeeFormula> implementations) {
        this.formules = implementations.stream().collect(Collectors.toUnmodifiableMap(
                f -> cle(f.code(), f.version()),
                Function.identity(),
                (a, b) -> {
                    throw new IllegalStateException("Formule enregistrée deux fois : " + a.code() + " v" + a.version());
                }));
    }

    public Optional<FeeFormula> trouver(String code, int version) {
        return Optional.ofNullable(formules.get(cle(code, version)));
    }

    public Collection<FeeFormula> toutes() {
        return formules.values();
    }

    private static String cle(String code, int version) {
        return code + ":" + version;
    }
}
