package com.gateway.payment.catalog.capability;

import com.gateway.payment.bpm.ProviderCallOperationRegistry;
import com.gateway.payment.catalog.api.dto.StepVocabularyResponse;
import com.gateway.payment.catalog.bpmn.BpmnStepAttributes;
import com.gateway.payment.catalog.casting.CatalogProductRoleCode;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Expose les capabilities enregistrées (une par {@link CapabilityHandler}), les opérations
 * fournisseur et les rôles, pour la saisie des attributs {@code pay:*} dans le concepteur.
 */
@Service
public class CapabilityCatalogService {

    public static final String DELEGATE_EXPRESSION = "${catalogStepDelegate}";

    private static final Map<String, String[]> LABELS = Map.ofEntries(
            Map.entry(CapabilityCodes.VALIDATE_AMOUNT,
                    new String[]{"Valider le montant", "Exécute les softRules de l'étape (montant, limites)"}),
            Map.entry(CapabilityCodes.VALIDATE_ACCOUNT,
                    new String[]{"Valider le compte", "Exécute les softRules de l'étape ; sans règle, l'étape ne contrôle rien"}),
            Map.entry(CapabilityCodes.CALCULATE_FEES,
                    new String[]{"Calculer les frais", "payment-pricing/quote (live) ; softRules DEFAULT_FEE en secours"}),
            Map.entry(CapabilityCodes.EVALUATE_RULES,
                    new String[]{"Évaluer les règles", "payment-rules : plafonds / cumuls ; réserve si ALLOW"}),
            Map.entry(CapabilityCodes.GENERATE_CODE,
                    new String[]{"Générer un code", "Code de retrait / dépôt"}),
            Map.entry(CapabilityCodes.NOTIFY,
                    new String[]{"Notifier et clôturer", "Clôture la transaction en SUCCESS"}),
            Map.entry(CapabilityCodes.WAIT,
                    new String[]{"Attendre", "Attente agent / règlement"}),
            Map.entry(CapabilityCodes.APPROVAL_OUTCOME,
                    new String[]{"Issue de validation", "Retour à l'initiateur ou rejet"}),
            Map.entry(CapabilityCodes.SOFT_RULES,
                    new String[]{"Règles seules", "Exécute uniquement les softRules de l'étape"}),
            Map.entry(CapabilityCodes.RESOLVE_DOCUMENTS,
                    new String[]{"Déterminer les pièces", "DMN montant / partenaire → profil de pièces"}),
            Map.entry(CapabilityCodes.VALIDATE_DOCUMENTS,
                    new String[]{"Vérifier les pièces", "Exige les pièces requises dans le contexte"}),
            Map.entry(CapabilityCodes.GET_INFOS,
                    new String[]{"Consulter le compte", "Appel partenaire GETINFOS"}),
            Map.entry(CapabilityCodes.INFO_CLIENT,
                    new String[]{"Rechercher le titulaire", "Appel partenaire INFOCLIENT (wallet / banque)"}),
            Map.entry(CapabilityCodes.SAVE_TRANSFER_BENEFICIARY,
                    new String[]{"Enregistrer le bénéficiaire", "Écriture carnet transfer_beneficiary"}),
            Map.entry(CapabilityCodes.GET_BILLS,
                    new String[]{"Consulter les factures", "Appel facturier LOOKUP_BILL"}),
            Map.entry(CapabilityCodes.GET_BALANCE,
                    new String[]{"Consulter le solde", "Appel partenaire GET_BALANCE"}),
            Map.entry(CapabilityCodes.EXECUTE_TOPUP,
                    new String[]{"Créditer (top-up)", "Appel partenaire CONFIRM_PAYMENT"}),
            Map.entry(CapabilityCodes.PAY_BILL,
                    new String[]{"Payer la facture", "Appel facturier PAY_BILL ; clôture en SUCCESS"}),
            Map.entry(CapabilityCodes.EXECUTE_TRANSFER,
                    new String[]{"Exécuter le transfert", "Appel partenaire TRANSFERT"}),
            Map.entry(CapabilityCodes.EXECUTE_CASHOUT,
                    new String[]{"Exécuter le cash-out", "Débit source via SpecDriven TRANSFERT"}),
            Map.entry(CapabilityCodes.EXECUTE_CASHIN,
                    new String[]{"Exécuter le cash-in", "Crédit destination via SpecDriven TRANSFERT"}),
            Map.entry(CapabilityCodes.CREATE_PAYMENT,
                    new String[]{"Créer le paiement", "Session partenaire ; redirection ou attente"}),
            Map.entry(CapabilityCodes.EVALUATE_APPROVAL,
                    new String[]{"Évaluer la validation", "DMN → nombre de niveaux (approvalLevelsRequired)"}),
            Map.entry(CapabilityCodes.GENERATE_OTP,
                    new String[]{"Générer l'OTP", "Appel partenaire GENERATE_OTP ; écran suivant ENTER_OTP"}),
            Map.entry(CapabilityCodes.CONFIRM_PAYMENT,
                    new String[]{"Confirmer le paiement", "Appel partenaire CONFIRM_PAYMENT (après OTP)"}),
            Map.entry(CapabilityCodes.EXECUTE_LINK,
                    new String[]{"Rattacher le compte", "Appel partenaire LINK"}),
            Map.entry(CapabilityCodes.EXECUTE_UNLINK,
                    new String[]{"Détacher le compte", "Appel partenaire UNLINK"}),
            Map.entry(CapabilityCodes.GET_QUOTE,
                    new String[]{"Coter", "FX + frais + durée de validité"}),
            Map.entry(CapabilityCodes.EXECUTE_REMITTANCE,
                    new String[]{"Envoyer la remittance", "Appel partenaire REMITTANCE"}),
            Map.entry(CapabilityCodes.LOOKUP_REMITTANCE,
                    new String[]{"Rechercher l'envoi", "Charge l'envoi par référence"}),
            Map.entry(CapabilityCodes.VALIDATE_PAYOUT,
                    new String[]{"Contrôler la remise", "Contrôles locaux avant remise"}),
            Map.entry(CapabilityCodes.EXECUTE_PAYOUT,
                    new String[]{"Exécuter la remise", "Appel partenaire PAYOUT"}),
            Map.entry(CapabilityCodes.COMPLETE_PAYOUT,
                    new String[]{"Clôturer la remise", "Clôt la réception et l'envoi parent"})
    );

    private final CapabilityRegistry registry;
    private final ProviderCallOperationRegistry operationRegistry;

    public CapabilityCatalogService(CapabilityRegistry registry, ProviderCallOperationRegistry operationRegistry) {
        this.registry = registry;
        this.operationRegistry = operationRegistry;
    }

    public StepVocabularyResponse vocabulary() {
        List<StepVocabularyResponse.CapabilityView> capabilities = registry.all().values().stream()
                .map(this::toView)
                .sorted(Comparator.comparing(StepVocabularyResponse.CapabilityView::category)
                        .thenComparing(StepVocabularyResponse.CapabilityView::code))
                .toList();
        List<StepVocabularyResponse.OperationView> operations = operationRegistry.all().stream()
                .map(s -> new StepVocabularyResponse.OperationView(
                        s.operation(), s.rail().name(), s.onSuccess().name()))
                .toList();
        List<StepVocabularyResponse.ActorRoleView> roles = Arrays.stream(CatalogProductRoleCode.values())
                .map(r -> new StepVocabularyResponse.ActorRoleView(r.name(), r.defaultLabel()))
                .toList();
        return new StepVocabularyResponse(
                BpmnStepAttributes.NAMESPACE,
                BpmnStepAttributes.PREFIX,
                DELEGATE_EXPRESSION,
                capabilities,
                operations,
                roles
        );
    }

    public static String category(CapabilityHandler handler) {
        if (CapabilityCodes.EVALUATE_APPROVAL.equalsIgnoreCase(handler.code())) {
            return "APPROVAL";
        }
        return handler.defaultOperation() != null ? "PROVIDER" : "LOCAL";
    }

    private StepVocabularyResponse.CapabilityView toView(CapabilityHandler handler) {
        String code = handler.code().trim().toUpperCase(Locale.ROOT);
        String[] label = LABELS.getOrDefault(code, new String[]{code, null});
        return new StepVocabularyResponse.CapabilityView(
                code, label[0], label[1], category(handler), handler.defaultOperation());
    }
}
