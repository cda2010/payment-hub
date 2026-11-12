package com.gateway.payment.commission.formule;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class CommissionFormulaRegistry {

    private final Map<String, CommissionFormula> formules;

    public CommissionFormulaRegistry(List<CommissionFormula> implementations) {
        this.formules = implementations.stream()
                .collect(Collectors.toMap(f -> cle(f.code(), f.version()), Function.identity(), (a, b) -> a));
    }

    public Optional<CommissionFormula> trouver(String code, int version) {
        return Optional.ofNullable(formules.get(cle(code, version)));
    }

    private static String cle(String code, int version) {
        return code.toUpperCase() + ":" + version;
    }
}
