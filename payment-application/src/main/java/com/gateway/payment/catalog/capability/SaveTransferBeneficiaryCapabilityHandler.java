package com.gateway.payment.catalog.capability;

import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.transfer.domain.TransferBeneficiary;
import com.gateway.payment.transfer.service.TransferBeneficiaryService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Enregistre le bénéficiaire dans le carnet après recherche {@code INFO_CLIENT}.
 * <p>
 * Lit le contexte (titulaire trouvé + identifiant FSE) — pas d'appel connecteur.
 */
@Component
public class SaveTransferBeneficiaryCapabilityHandler implements CapabilityHandler {

    private final TransferBeneficiaryService beneficiaries;

    public SaveTransferBeneficiaryCapabilityHandler(TransferBeneficiaryService beneficiaries) {
        this.beneficiaries = beneficiaries;
    }

    @Override
    public String code() {
        return CapabilityCodes.SAVE_TRANSFER_BENEFICIARY;
    }

    @Override
    public CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration) {
        Map<String, Object> ctx = context.context();
        String provider = first(ctx,
                "destinationProvider",
                "providerRail",
                "invokeProviderCode");
        if (!StringUtils.hasText(provider) && ctx.get("roleAssignments") instanceof Map<?, ?> roles) {
            Object dest = roles.get("DESTINATION");
            if (dest != null) {
                provider = String.valueOf(dest).trim();
            }
        }
        if (!StringUtils.hasText(provider)) {
            throw new BusinessException("BENEFICIARY_PROVIDER_REQUIRED", "Réseau bénéficiaire manquant");
        }

        String label = first(ctx, "beneficiaryName", "nom", "label", "accountName", "titulaire");
        if (!StringUtils.hasText(label)) {
            label = first(ctx, "phoneNumber", "msisdn", "telephone", "accountNumber", "numeroCompte");
        }
        if (!StringUtils.hasText(label)) {
            throw new BusinessException("BENEFICIARY_NAME_REQUIRED", "Nom du bénéficiaire introuvable après recherche");
        }

        String phone = first(ctx, "phoneNumber", "msisdn", "telephone", "destPhone");
        String account = first(ctx, "accountNumber", "numeroCompte", "destAccount");
        String agency = first(ctx, "agence", "codeAgence", "agencyCode");
        String destinationCode = resolveParticipantCode(provider.trim());

        TransferBeneficiary saved = beneficiaries.create(TransferBeneficiary.builder()
                .label(label.trim())
                .phone(StringUtils.hasText(phone) ? phone.trim() : null)
                .accountNumber(StringUtils.hasText(account) ? account.trim() : null)
                .agencyCode(StringUtils.hasText(agency) ? agency.trim() : null)
                .destinationProvider(destinationCode)
                .icon("person-outline")
                .build());

        ctx.put("beneficiaryId", saved.getId() != null ? saved.getId().toString() : null);
        ctx.put("beneficiaryName", saved.getLabel());
        ctx.put("label", saved.getLabel());

        List<CapabilityResult.CapabilityEvent> events = new ArrayList<>();
        events.add(new CapabilityResult.CapabilityEvent(
                "BENEFICIARY_SAVED",
                "Bénéficiaire enregistré : " + saved.getLabel()));
        return CapabilityResult.of(
                false,
                events,
                Map.of(
                        "beneficiaryId", saved.getId() != null ? saved.getId().toString() : "",
                        "beneficiaryName", saved.getLabel()
                ),
                CapabilityResult.TerminalEffect.NONE,
                null
        );
    }

    private String resolveParticipantCode(String providerOrParticipant) {
        return providerOrParticipant.trim();
    }

    private static String first(Map<String, Object> ctx, String... keys) {
        if (ctx == null || keys == null) {
            return null;
        }
        for (String key : keys) {
            Object v = ctx.get(key);
            if (v != null && StringUtils.hasText(String.valueOf(v))) {
                return String.valueOf(v).trim();
            }
        }
        return null;
    }
}
