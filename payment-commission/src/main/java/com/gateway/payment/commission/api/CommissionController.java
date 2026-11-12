package com.gateway.payment.commission.api;

import com.gateway.payment.commission.api.dto.CommissionDtos.*;
import com.gateway.payment.commission.domain.DroitCommission;
import com.gateway.payment.commission.domain.LiquidationCommission;
import com.gateway.payment.commission.domain.StatutGrilleCommission;
import com.gateway.payment.commission.moteur.DemandeSimulation;
import com.gateway.payment.commission.moteur.MoteurCommission;
import com.gateway.payment.commission.service.CatalogueCommissionService;
import com.gateway.payment.commission.service.DroitsCommissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/commission")
@Tag(name = "Commission", description = "Moteur de commissionnement — formules, grilles, simulation, droits")
public class CommissionController {

    private final CatalogueCommissionService catalogue;
    private final MoteurCommission moteur;
    private final DroitsCommissionService droits;

    public CommissionController(
            CatalogueCommissionService catalogue,
            MoteurCommission moteur,
            DroitsCommissionService droits
    ) {
        this.catalogue = catalogue;
        this.moteur = moteur;
        this.droits = droits;
    }

    @GetMapping("/formulas")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Catalogue des formules de commission")
    public List<FormuleDto> formulas() {
        return catalogue.listerFormules();
    }

    @GetMapping("/grids")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les grilles de commissionnement")
    public List<GrilleDto> grids(@RequestParam(required = false) StatutGrilleCommission status) {
        return catalogue.listerGrilles(status);
    }

    @GetMapping("/grids/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Détail d'une grille (règles incluses)")
    public GrilleDto grid(@PathVariable UUID id) {
        return catalogue.detailGrille(id);
    }

    @PostMapping("/grids")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER')")
    @Operation(summary = "Créer une grille brouillon")
    public GrilleDto createGrid(@RequestBody CreateGrilleRequest body) {
        return catalogue.creerGrille(body);
    }

    @PutMapping("/grids/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER')")
    @Operation(summary = "Modifier l'en-tête d'une grille brouillon")
    public GrilleDto updateGrid(@PathVariable UUID id, @RequestBody UpdateGrilleRequest body) {
        return catalogue.majGrille(id, body);
    }

    @DeleteMapping("/grids/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER')")
    @Operation(summary = "Supprimer une grille brouillon")
    public void deleteGrid(@PathVariable UUID id) {
        catalogue.supprimerGrille(id);
    }

    @PostMapping("/grids/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER')")
    @Operation(summary = "Publier une grille")
    public GrilleDto publish(@PathVariable UUID id) {
        return catalogue.publier(id);
    }

    @PostMapping("/grids/{id}/versions")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER')")
    @Operation(summary = "Créer une nouvelle version brouillon (copie)")
    public GrilleDto newVersion(@PathVariable UUID id) {
        return catalogue.nouvelleVersion(id);
    }

    @PostMapping("/grids/{id}/rules")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER')")
    @Operation(summary = "Ajouter une règle à une grille brouillon")
    public RegleDto addRule(@PathVariable UUID id, @RequestBody UpsertRegleRequest body) {
        return catalogue.ajouterRegle(id, body);
    }

    @PutMapping("/grids/{id}/rules/{ruleId}")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER')")
    @Operation(summary = "Modifier une règle")
    public RegleDto updateRule(
            @PathVariable UUID id,
            @PathVariable UUID ruleId,
            @RequestBody UpsertRegleRequest body
    ) {
        return catalogue.majRegle(id, ruleId, body);
    }

    @DeleteMapping("/grids/{id}/rules/{ruleId}")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER')")
    @Operation(summary = "Supprimer une règle")
    public void deleteRule(@PathVariable UUID id, @PathVariable UUID ruleId) {
        catalogue.supprimerRegle(id, ruleId);
    }

    @PostMapping("/simulations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Simuler la répartition des commissions pour une transaction")
    public SimulationResponse simulate(@RequestBody SimulationRequest body) {
        return SimulationResponse.from(moteur.simuler(new DemandeSimulation(
                body.transactionId(),
                body.operation(),
                body.productCode(),
                body.channel(),
                body.currency(),
                body.amount(),
                body.fees(),
                body.aggregates())));
    }

    @PostMapping("/accruals")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Accrue les droits (simule + persiste) — appelé au SUCCESS du gateway")
    public List<DroitCommission> accrue(@RequestBody DroitsCommissionService.AccrueRequest body) {
        return droits.accrue(body.toDemande(), body.tenantId());
    }

    @GetMapping("/droits")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Droits d'une transaction")
    public List<DroitCommission> droits(@RequestParam String transactionId) {
        return droits.byTransaction(transactionId);
    }

    @PostMapping("/liquidations")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','FINANCE')")
    @Operation(summary = "Liquidation V1 : solde les droits ACCRUED d'un rôle")
    public LiquidationCommission liquidate(@RequestBody Map<String, String> body) {
        return droits.liquidate(
                body.get("beneficiaryRole"),
                body.get("beneficiaryRef"),
                body.getOrDefault("currency", "XOF"),
                body.get("tenantId"));
    }
}
