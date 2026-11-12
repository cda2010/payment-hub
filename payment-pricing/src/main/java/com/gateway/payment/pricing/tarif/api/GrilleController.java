package com.gateway.payment.pricing.tarif.api;

import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.GrilleDetail;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.GrilleRequest;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.GrilleResume;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.PageDto;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.RapportValidation;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.SimulationRequest;
import com.gateway.payment.pricing.tarif.api.dto.TarifDtos.TrancheRequest;
import com.gateway.payment.pricing.tarif.domain.StatutGrille;
import com.gateway.payment.pricing.tarif.moteur.ResultatFrais;
import com.gateway.payment.pricing.tarif.service.GrilleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/grilles")
@Tag(name = "Grilles tarifaires", description = "Grilles, tranches, contrôle de cohérence et simulation")
@PreAuthorize("isAuthenticated()")
public class GrilleController {

    private static final String ECRITURE = "hasAnyRole('ADMIN','CONFIG_MANAGER')";

    private final GrilleService grilles;

    public GrilleController(GrilleService grilles) {
        this.grilles = grilles;
    }

    @GetMapping
    @Operation(summary = "Rechercher des grilles (code, libellé, produit, canal, devise, statut, date de validité, "
            + "source ou destination : code de zone ou d'unité) ; paginé (page, taille : 50 par défaut, 200 au plus)")
    public PageDto<GrilleResume> rechercher(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String libelle,
            @RequestParam(required = false) String produit,
            @RequestParam(required = false) String canal,
            @RequestParam(required = false) String devise,
            @RequestParam(required = false) StatutGrille statut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer taille
    ) {
        return grilles.rechercher(new GrilleService.Filtre(code, libelle, produit, canal, devise, statut, date,
                source, destination), page, taille);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulter une grille et ses tranches")
    public GrilleDetail consulter(@PathVariable UUID id) {
        return grilles.consulter(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Créer une grille en brouillon (version 1) ; source et destination : vides (toutes), une zone "
            + "ou une unité du réseau de n'importe quel type")
    public GrilleDetail creer(@Valid @RequestBody GrilleRequest body, Authentication auth) {
        return grilles.creer(body, auth.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Modifier un brouillon")
    public GrilleDetail modifier(@PathVariable UUID id, @Valid @RequestBody GrilleRequest body, Authentication auth) {
        return grilles.modifier(id, body, auth.getName());
    }

    @PostMapping("/{id}/tranches")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Ajouter une tranche à un brouillon")
    public GrilleDetail ajouterTranche(@PathVariable UUID id, @Valid @RequestBody TrancheRequest body,
                                       Authentication auth) {
        return grilles.ajouterTranche(id, body, auth.getName());
    }

    @PutMapping("/{id}/tranches/{trancheId}")
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Modifier une tranche d'un brouillon")
    public GrilleDetail modifierTranche(@PathVariable UUID id, @PathVariable UUID trancheId,
                                        @Valid @RequestBody TrancheRequest body, Authentication auth) {
        return grilles.modifierTranche(id, trancheId, body, auth.getName());
    }

    @DeleteMapping("/{id}/tranches/{trancheId}")
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Supprimer une tranche d'un brouillon")
    public GrilleDetail supprimerTranche(@PathVariable UUID id, @PathVariable UUID trancheId, Authentication auth) {
        return grilles.supprimerTranche(id, trancheId, auth.getName());
    }

    @PostMapping("/{id}/tranches/{trancheId}/dupliquer")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Dupliquer une tranche d'un brouillon")
    public GrilleDetail dupliquerTranche(@PathVariable UUID id, @PathVariable UUID trancheId, Authentication auth) {
        return grilles.dupliquerTranche(id, trancheId, auth.getName());
    }

    @PostMapping("/{id}/versions")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Créer une nouvelle version en brouillon d'une grille (ou reprendre le brouillon en cours)")
    public GrilleDetail nouvelleVersion(@PathVariable UUID id, Authentication auth) {
        return grilles.nouvelleVersion(id, auth.getName());
    }

    @PostMapping("/{id}/publier")
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Publier un brouillon cohérent ; il remplace la version publiée du même code")
    public GrilleDetail publier(@PathVariable UUID id, Authentication auth) {
        return grilles.publier(id, auth.getName());
    }

    @PostMapping("/{id}/valider")
    @Operation(summary = "Lancer les contrôles de cohérence (sans changer le statut)")
    public RapportValidation valider(@PathVariable UUID id) {
        return grilles.valider(id);
    }

    @PostMapping("/{id}/simulations")
    @Operation(summary = "Simuler les frais d'un montant avec cette grille (aucune écriture)")
    public ResultatFrais simuler(@PathVariable UUID id, @Valid @RequestBody SimulationRequest body) {
        return grilles.simuler(id, body);
    }
}
