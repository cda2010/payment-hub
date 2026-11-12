package com.gateway.payment.catalog.bpmn;

import com.gateway.payment.catalog.casting.CatalogBindingDefinition;
import com.gateway.payment.catalog.casting.CatalogBindingKind;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Fusionne le binding catalogue d'une étape avec les attributs {@code pay:*} de la tâche BPMN.
 * <p>
 * Priorité :
 * <ol>
 *   <li>binding catalogue avec {@code capability} : il surcharge l'étape, le BPMN ne comble que les vides ;</li>
 *   <li>sinon {@code pay:capability} : la capability et son {@code operation} viennent du BPMN
 *       (l'{@code operation} historique du binding, pensée pour l'inférence, est ignorée) ;</li>
 *   <li>sinon : le BPMN comble les vides du binding.</li>
 * </ol>
 */
public final class CatalogStepBindings {

    private CatalogStepBindings() {
    }

    public static CatalogBindingDefinition merge(
            CatalogBindingDefinition catalog,
            BpmnStepAttributes bpmn,
            String step
    ) {
        if (bpmn == null || bpmn.isEmpty()) {
            return catalog;
        }
        if (catalog == null) {
            return new CatalogBindingDefinition(
                    step,
                    CatalogBindingKind.CAPABILITY,
                    bpmn.actorRole(),
                    null,
                    null,
                    null,
                    null,
                    bpmn.operation(),
                    bpmn.capability(),
                    Map.of()
            );
        }
        if (StringUtils.hasText(catalog.capability())) {
            return copy(catalog,
                    firstNonBlank(catalog.actorRole(), bpmn.actorRole()),
                    firstNonBlank(catalog.operation(), bpmn.operation()),
                    catalog.capability());
        }
        if (bpmn.capability() != null) {
            return copy(catalog,
                    firstNonBlank(bpmn.actorRole(), catalog.actorRole()),
                    bpmn.operation(),
                    bpmn.capability());
        }
        return copy(catalog,
                firstNonBlank(catalog.actorRole(), bpmn.actorRole()),
                firstNonBlank(catalog.operation(), bpmn.operation()),
                null);
    }

    private static CatalogBindingDefinition copy(
            CatalogBindingDefinition source,
            String actorRole,
            String operation,
            String capability
    ) {
        return new CatalogBindingDefinition(
                source.step(),
                source.kind(),
                actorRole,
                source.providerCode(),
                source.formKey(),
                source.formSource(),
                source.billerScreen(),
                operation,
                capability,
                source.params()
        );
    }

    private static String firstNonBlank(String a, String b) {
        return StringUtils.hasText(a) ? a : (StringUtils.hasText(b) ? b : null);
    }
}
