package com.gateway.payment.omnicanal.capability;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.configuration.domain.PaymentProvider;
import com.gateway.payment.configuration.domain.ProviderOperation;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.configuration.repository.ProviderOperationRepository;
import com.gateway.payment.orchestration.domain.InterfaceUtilisateur;
import com.gateway.payment.orchestration.repository.InterfaceUtilisateurRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Catalogue capacités FSE (miroir Omnicanal FseCapabilityCatalogService) —
 * dérivé des providers + opérations + interfaces utilisateur.
 */
@Service
public class FseCapabilityCatalogService {

    private final PaymentProviderRepository providerRepository;
    private final ProviderOperationRepository operationRepository;
    private final InterfaceUtilisateurRepository interfaceRepository;

    public FseCapabilityCatalogService(
            PaymentProviderRepository providerRepository,
            ProviderOperationRepository operationRepository,
            InterfaceUtilisateurRepository interfaceRepository
    ) {
        this.providerRepository = providerRepository;
        this.operationRepository = operationRepository;
        this.interfaceRepository = interfaceRepository;
    }

    @Transactional(readOnly = true)
    public List<FseCapabilityView> listAll() {
        List<FseCapabilityView> out = new ArrayList<>();
        for (PaymentProvider p : providerRepository.findByCodeFseIsNotNull()) {
            if (p.getCodeFse() == null || p.getCodeFse().isBlank()) {
                continue;
            }
            out.add(toView(p));
        }
        out.sort(Comparator.comparing(FseCapabilityView::getCodeFse));
        return out;
    }

    @Transactional(readOnly = true)
    public FseCapabilityView getByCodeFse(String codeFse) {
        PaymentProvider p = providerRepository.findByCodeFse(codeFse)
                .orElseThrow(() -> new IllegalArgumentException("FSE inconnu: " + codeFse));
        return toView(p);
    }

    private FseCapabilityView toView(PaymentProvider p) {
        FseCapabilityView view = new FseCapabilityView();
        view.setCodeFse(p.getCodeFse());
        view.setProviderCode(p.getCode());
        view.setProviderLibelle(p.getLibelle());

        List<ProviderOperation> ops = operationRepository.findByProviderId(p.getId());
        List<InterfaceUtilisateur> ifaces = interfaceRepository.findByProviderIdOrderByTypeMethodeAsc(p.getId());

        for (ProviderOperation op : ops) {
            if (!op.isActif()) {
                continue;
            }
            String capability = mapCapability(op.getCode());
            view.getOperations().add(new CapabilityOperationView(
                    capability, op.getCode(), op.getCode(), true
            ));
        }
        // Enrichir avec interfaces même sans opération orpheline
        for (InterfaceUtilisateur iface : ifaces) {
            boolean exists = view.getOperations().stream()
                    .anyMatch(o -> o.getTypeMethode().equalsIgnoreCase(iface.getTypeMethode()));
            if (!exists && iface.isActif()) {
                view.getOperations().add(new CapabilityOperationView(
                        mapCapability(iface.getTypeMethode()),
                        iface.getTypeMethode(),
                        iface.getProviderOperation() != null ? iface.getProviderOperation().getCode() : iface.getTypeMethode(),
                        true
                ));
            }
        }
        return view;
    }

    private static String mapCapability(String typeMethode) {
        if (typeMethode == null) {
            return "UNKNOWN";
        }
        ServiceTypeCode st = ServiceTypeCode.fromString(typeMethode);
        if (st != null) {
            return st.name();
        }
        return switch (typeMethode.toUpperCase()) {
            case "DEBIT", "CREDIT", "TRANSFERT" -> ServiceTypeCode.TRANSFERT.name();
            case "CONSULTATIONSOLDE", "CONSULTATIONHIST" -> ServiceTypeCode.CONSULTATION_SOLDE.name();
            default -> typeMethode.toUpperCase();
        };
    }
}
