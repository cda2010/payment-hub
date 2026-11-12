package com.gateway.payment.pricing.tarif.formule;

import com.gateway.payment.pricing.tarif.domain.Formule;
import com.gateway.payment.pricing.tarif.domain.TypeFormule;
import com.gateway.payment.pricing.tarif.repository.FormuleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Refuse le démarrage si une formule prédéfinie active en base n'a pas d'implémentation,
 * ou si son schéma de paramètres diffère de celui de l'implémentation.
 */
@Component
public class FormuleCatalogueVerifier implements ApplicationRunner {

    private final FormuleRepository formules;
    private final FormulaRegistry registry;

    public FormuleCatalogueVerifier(FormuleRepository formules, FormulaRegistry registry) {
        this.formules = formules;
        this.registry = registry;
    }

    @Override
    @Transactional(readOnly = true)
    public void run(ApplicationArguments args) {
        List<String> ecarts = ecarts(formules.findAll(), registry);
        if (!ecarts.isEmpty()) {
            throw new IllegalStateException("Catalogue de formules incohérent :\n - " + String.join("\n - ", ecarts));
        }
    }

    static List<String> ecarts(Collection<Formule> catalogue, FormulaRegistry registry) {
        List<String> ecarts = new ArrayList<>();
        for (Formule formule : catalogue) {
            if (formule.getType() != TypeFormule.PREDEFINIE || !formule.estActive()) {
                continue;
            }
            FeeFormula impl = registry.trouver(formule.getCode(), formule.getVersion()).orElse(null);
            if (impl == null) {
                ecarts.add(formule.libelleVersion() + " : aucune implémentation enregistrée");
                continue;
            }
            Map<String, ParametreAttendu> attendus = impl.parametres().stream()
                    .collect(Collectors.toMap(ParametreAttendu::code, Function.identity()));
            Map<String, ParametreAttendu> declares = formule.getParametres().stream()
                    .map(p -> new ParametreAttendu(p.getCode(), p.getTypeDonnee(), p.isObligatoire()))
                    .collect(Collectors.toMap(ParametreAttendu::code, Function.identity()));
            if (!attendus.equals(declares)) {
                ecarts.add(formule.libelleVersion() + " : paramètres en base " + declares.values()
                        + " ≠ implémentation " + attendus.values());
            }
        }
        return ecarts;
    }
}
