package com.gateway.payment.pricing.tarif.service;

import com.gateway.payment.pricing.tarif.TarifException;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.EssaiRequest;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.FormuleDetail;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.FormuleResume;
import com.gateway.payment.pricing.tarif.domain.Formule;
import com.gateway.payment.pricing.tarif.moteur.EssaiFormule;
import com.gateway.payment.pricing.tarif.moteur.MoteurFrais;
import com.gateway.payment.pricing.tarif.repository.FormuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true, noRollbackFor = TarifException.class)
public class FormuleService {

    private final FormuleRepository formules;
    private final MoteurFrais moteur;

    public FormuleService(FormuleRepository formules, MoteurFrais moteur) {
        this.formules = formules;
        this.moteur = moteur;
    }

    public List<FormuleResume> lister() {
        return formules.findAllByOrderByCodeAscVersionAsc().stream().map(FormuleResume::from).toList();
    }

    public FormuleDetail consulter(String code, int version) {
        return FormuleDetail.from(charger(code, version));
    }

    /** Calcule les frais d'un montant avec des paramètres saisis, sans rien enregistrer. */
    public EssaiFormule essayer(String code, int version, EssaiRequest req) {
        return moteur.essayer(charger(code, version), req.parametres(), req.montant(), req.fraisMin(), req.fraisMax());
    }

    private Formule charger(String code, int version) {
        return formules.findByCodeAndVersion(code.trim().toUpperCase(Locale.ROOT), version)
                .orElseThrow(() -> TarifException.introuvable("FORMULE_INEXISTANTE",
                        "Formule " + code + " v" + version + " introuvable."));
    }
}
