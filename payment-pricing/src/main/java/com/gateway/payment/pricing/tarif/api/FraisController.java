package com.gateway.payment.pricing.tarif.api;

import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.ApplicableRequest;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.CalculRequest;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.LocalisationDto;
import com.gateway.payment.pricing.tarif.moteur.DemandeFrais;
import com.gateway.payment.pricing.tarif.moteur.GrilleApplicable;
import com.gateway.payment.pricing.tarif.moteur.Localisation;
import com.gateway.payment.pricing.tarif.moteur.MoteurFrais;
import com.gateway.payment.pricing.tarif.moteur.ResultatFrais;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/frais")
@Tag(name = "Calcul des frais", description = "Le serveur choisit la grille publiée, la tranche et la formule")
public class FraisController {

    private final MoteurFrais moteur;

    public FraisController(MoteurFrais moteur) {
        this.moteur = moteur;
    }

    @PostMapping("/calculer")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Calculer les frais d'une transaction (produit, canal, devise, montant ; date, source et "
            + "destination facultatives : unité, pays ou zone)")
    public ResultatFrais calculer(@Valid @RequestBody CalculRequest body) {
        return moteur.calculer(new DemandeFrais(body.produitCode(), body.canalCode(), body.devise(), body.montant(),
                body.dateCalcul(), localisation(body.source()), localisation(body.destination())));
    }

    @PostMapping("/grille-applicable")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Grille publiée qui s'applique à une source et une destination, sans montant (TROUVEE, AUCUNE "
            + "ou AMBIGUE), grilles moins précises et nombre de grilles qui visent déjà des unités sœurs")
    public GrilleApplicable applicable(@Valid @RequestBody ApplicableRequest body) {
        return moteur.applicable(new DemandeFrais(body.produitCode(), body.canalCode(), body.devise(), null,
                body.dateCalcul(), localisation(body.source()), localisation(body.destination())));
    }

    static Localisation localisation(LocalisationDto dto) {
        return dto == null ? null : new Localisation(dto.uniteId(), dto.pays(), dto.zone());
    }
}
