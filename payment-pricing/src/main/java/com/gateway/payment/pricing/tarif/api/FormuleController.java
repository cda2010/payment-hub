package com.gateway.payment.pricing.tarif.api;

import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.EssaiRequest;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.FormuleDetail;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.FormuleResume;
import com.gateway.payment.pricing.tarif.moteur.EssaiFormule;
import com.gateway.payment.pricing.tarif.service.FormuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/formules")
@Tag(name = "Formules", description = "Catalogue versionné des formules de frais")
@PreAuthorize("isAuthenticated()")
public class FormuleController {

    private final FormuleService formules;

    public FormuleController(FormuleService formules) {
        this.formules = formules;
    }

    @GetMapping
    @Operation(summary = "Lister les formules disponibles")
    public List<FormuleResume> lister() {
        return formules.lister();
    }

    @GetMapping("/{code}/versions/{version}")
    @Operation(summary = "Consulter une version de formule et son schéma de paramètres")
    public FormuleDetail consulter(@PathVariable String code, @PathVariable int version) {
        return formules.consulter(code, version);
    }

    @PostMapping("/{code}/versions/{version}/essais")
    @Operation(summary = "Essayer une formule avec des paramètres saisis (aucune écriture, sans arrondi de grille)")
    public EssaiFormule essayer(@PathVariable String code, @PathVariable int version,
                                @Valid @RequestBody EssaiRequest body) {
        return formules.essayer(code, version, body);
    }
}
