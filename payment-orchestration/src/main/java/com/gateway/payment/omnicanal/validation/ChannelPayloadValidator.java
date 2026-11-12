package com.gateway.payment.omnicanal.validation;

import com.gateway.payment.common.domain.OmnicanalTypeMethode;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.domain.ProviderOperation;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.configuration.repository.ProviderOperationRepository;
import com.gateway.payment.orchestration.domain.InterfaceUtilisateur;
import com.gateway.payment.orchestration.domain.TypeEnvoi;
import com.gateway.payment.orchestration.repository.InterfaceUtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Validation stricte des codes métier canal avant tout appel FSE (miroir Omnicanal).
 */
@Service
public class ChannelPayloadValidator {

    private final PaymentProviderRepository providerRepository;
    private final ProviderOperationRepository operationRepository;
    private final InterfaceUtilisateurRepository interfaceRepository;

    public ChannelPayloadValidator(
            PaymentProviderRepository providerRepository,
            ProviderOperationRepository operationRepository,
            InterfaceUtilisateurRepository interfaceRepository
    ) {
        this.providerRepository = providerRepository;
        this.operationRepository = operationRepository;
        this.interfaceRepository = interfaceRepository;
    }

    public String requireTypeMethode(String codeMethode) {
        if (!StringUtils.hasText(codeMethode)) {
            throw new ChannelValidationException("400", "codeMethode obligatoire");
        }
        String n = codeMethode.trim().toUpperCase();
        try {
            OmnicanalTypeMethode.valueOf(n);
        } catch (IllegalArgumentException ex) {
            // accepter aussi les codes opération libres (CONSULTATIONSOLDE déjà dans l'enum)
            if (!n.matches("[A-Z0-9_]+")) {
                throw new ChannelValidationException("400", "codeMethode inconnu: " + codeMethode);
            }
        }
        return n;
    }

    public PaymentProvider requireFse(String codeFse) {
        if (!StringUtils.hasText(codeFse)) {
            throw new ChannelValidationException("400", "codeFSE obligatoire");
        }
        return providerRepository.findByCodeFse(codeFse.trim())
                .orElseThrow(() -> new ChannelValidationException("400", "codeFSE inconnu: " + codeFse));
    }

    public InterfaceUtilisateur requireInterface(String codeFse, String codeMethode) {
        String typeMethode = requireTypeMethode(codeMethode);
        requireFse(codeFse);
        return interfaceRepository.findActiveByCodeFseAndTypeMethodeAndTypeEnvoi(
                        codeFse.trim(), typeMethode, TypeEnvoi.REQUEST)
                .orElseThrow(() -> new ChannelValidationException(
                        "400",
                        "Interface utilisateur absente pour FSE=" + codeFse + " codeMethode=" + codeMethode
                ));
    }

    public ProviderOperation requireOperation(PaymentProvider provider, String codeMethode) {
        String typeMethode = requireTypeMethode(codeMethode);
        return operationRepository.findByProviderIdAndCode(provider.getId(), typeMethode)
                .orElseThrow(() -> new ChannelValidationException(
                        "400",
                        "Opération non configurée pour provider=" + provider.getCode() + " codeMethode=" + codeMethode
                ));
    }

    public void requireOtrParametre(String otrParametre) {
        if (!StringUtils.hasText(otrParametre)) {
            throw new ChannelValidationException("400", "otrParametre obligatoire");
        }
    }

    public InterfaceUtilisateur validateBoundaryByTypeMethode(String codeFse, String codeMethode, String otrParametre) {
        requireOtrParametre(otrParametre);
        return requireInterface(codeFse, codeMethode);
    }
}
