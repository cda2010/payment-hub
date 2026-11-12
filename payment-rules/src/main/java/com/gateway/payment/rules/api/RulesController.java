package com.gateway.payment.rules.api;

import com.gateway.payment.rules.api.dto.RulesDtos.*;
import com.gateway.payment.rules.moteur.MoteurRegles;
import com.gateway.payment.rules.service.CatalogueRulesService;
import com.gateway.payment.rules.service.CumulService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Rules", description = "Moteur de règles métier / réglementaires")
public class RulesController {

    private final MoteurRegles moteur;
    private final CatalogueRulesService catalogue;
    private final CumulService cumuls;

    public RulesController(MoteurRegles moteur, CatalogueRulesService catalogue, CumulService cumuls) {
        this.moteur = moteur;
        this.catalogue = catalogue;
        this.cumuls = cumuls;
    }

    @PostMapping("/rules/evaluate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Évaluer les règles (réserve les cumuls si ALLOW)")
    public EvaluationResponse evaluate(@RequestBody EvaluateRequest body) {
        return EvaluationResponse.from(moteur.evaluate(body.toDemande()));
    }

    @PostMapping("/rules/simulate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Simuler sans réserver de limite")
    public EvaluationResponse simulate(@RequestBody EvaluateRequest body) {
        return EvaluationResponse.from(moteur.simulate(body.toDemande()));
    }

    @GetMapping("/rules")
    @PreAuthorize("isAuthenticated()")
    public List<RuleDto> rules() {
        return catalogue.listerRegles();
    }

    @GetMapping("/rules/{id}")
    @PreAuthorize("isAuthenticated()")
    public RuleDto rule(@PathVariable UUID id) {
        return catalogue.detailRegle(id);
    }

    @PostMapping("/rules")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','COMPLIANCE')")
    public RuleDto create(@RequestBody CreateRuleRequest body) {
        return catalogue.creerBrouillon(body);
    }

    @PutMapping("/rules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','COMPLIANCE')")
    @Operation(summary = "Modifier le libellé, le seuil et la date d'effet d'une règle")
    public RuleDto update(@PathVariable UUID id, @RequestBody UpdateRuleRequest body) {
        return catalogue.modifier(id, body);
    }

    @PostMapping("/rules/{id}/publish")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','COMPLIANCE')")
    public RuleDto publish(@PathVariable UUID id) {
        return catalogue.publier(id);
    }

    @GetMapping("/rule-groups")
    @PreAuthorize("isAuthenticated()")
    public List<GroupDto> groups() {
        return catalogue.listerGroupes();
    }

    @GetMapping("/rule-groups/{code}")
    @PreAuthorize("isAuthenticated()")
    public GroupDto group(@PathVariable String code) {
        return catalogue.detailGroupe(code);
    }

    @PostMapping("/rule-groups")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','COMPLIANCE')")
    @Operation(summary = "Créer un groupe de règles")
    public GroupDto createGroup(@RequestBody SaveGroupRequest body) {
        return catalogue.creerGroupe(body);
    }

    @PutMapping("/rule-groups/{code}")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','COMPLIANCE')")
    @Operation(summary = "Modifier un groupe et les règles qu'il évalue")
    public GroupDto updateGroup(@PathVariable String code, @RequestBody SaveGroupRequest body) {
        return catalogue.modifierGroupe(code, body);
    }

    @PostMapping("/limits/reservations/{id}/confirm")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Confirmer une réservation (txn réussie)")
    public void confirm(@PathVariable UUID id) {
        cumuls.confirm(id);
    }

    @PostMapping("/limits/reservations/{id}/release")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Libérer une réservation (txn annulée / rejetée)")
    public void release(@PathVariable UUID id) {
        cumuls.release(id);
    }

    @PostMapping("/limits/reservations/by-transaction/{txId}/confirm")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Confirmer toutes les réservations HELD d'une transaction")
    public Map<String, Object> confirmByTx(@PathVariable String txId) {
        return Map.of("confirmed", cumuls.confirmByTransaction(txId));
    }

    @PostMapping("/limits/reservations/by-transaction/{txId}/release")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Libérer toutes les réservations HELD d'une transaction")
    public Map<String, Object> releaseByTx(@PathVariable String txId) {
        return Map.of("released", cumuls.releaseByTransaction(txId));
    }
}
