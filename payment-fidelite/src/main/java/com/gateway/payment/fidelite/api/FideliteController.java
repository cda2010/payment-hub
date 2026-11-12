package com.gateway.payment.fidelite.api;

import com.gateway.payment.fidelite.domain.PalierSaisie;
import com.gateway.payment.fidelite.domain.ProgrammePortee;
import com.gateway.payment.fidelite.service.FideliteService;
import com.gateway.payment.fidelite.service.FideliteService.CreerProgramme;
import com.gateway.payment.fidelite.service.FideliteService.EchangeView;
import com.gateway.payment.fidelite.service.FideliteService.MouvementView;
import com.gateway.payment.fidelite.service.FideliteService.RecompenseView;
import com.gateway.payment.fidelite.service.FideliteService.ParrainageView;
import com.gateway.payment.fidelite.service.FideliteService.ProgrammeView;
import com.gateway.payment.fidelite.service.FideliteService.Situation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fidelite")
public class FideliteController {

    private final FideliteService fidelite;

    public FideliteController(FideliteService fidelite) {
        this.fidelite = fidelite;
    }

    @PostMapping("/programmes")
    @ResponseStatus(HttpStatus.CREATED)
    public ProgrammeView creer(@Valid @RequestBody CreerProgrammeRequest request) {
        return fidelite.creer(CurrentUser.tenantId(), new CreerProgramme(
                request.nom(),
                request.portee(),
                request.uniteId(),
                request.uniteNom(),
                request.profils(),
                request.pointsParMille(),
                request.pointsParrain(),
                request.pointsFilleul(),
                paliers(request.paliers())));
    }

    @GetMapping("/programmes")
    public List<ProgrammeView> visibles(@RequestParam(required = false) Set<String> profils) {
        return fidelite.visibles(CurrentUser.tenantId(), CurrentUser.partyId(), profils);
    }

    @GetMapping("/unites/{uniteId}/programmes")
    public List<ProgrammeView> deLUnite(@PathVariable UUID uniteId) {
        return fidelite.deLUnite(uniteId);
    }

    @PostMapping("/programmes/{id}/clients")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void inscrire(@PathVariable UUID id, @Valid @RequestBody ClientRequest request) {
        fidelite.adherer(id, request.partyId(), Set.of(), true);
    }

    @PostMapping("/programmes/{id}/adhesions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void adherer(@PathVariable UUID id, @RequestParam(required = false) Set<String> profils) {
        fidelite.adherer(id, partyId(), profils, false);
    }

    @PostMapping("/programmes/{id}/parrainages")
    @ResponseStatus(HttpStatus.CREATED)
    public ParrainageView parrainer(@PathVariable UUID id) {
        return fidelite.parrainer(id, partyId());
    }

    @PostMapping("/parrainages/{code}/accepter")
    public ParrainageView accepter(
            @PathVariable String code,
            @RequestParam(required = false) Set<String> profils
    ) {
        return fidelite.accepter(code, partyId(), profils);
    }

    @GetMapping("/programmes/{id}/solde")
    public Situation solde(@PathVariable UUID id) {
        return fidelite.solde(id, partyId());
    }

    @GetMapping("/programmes/{id}/mouvements")
    public List<MouvementView> mouvements(@PathVariable UUID id) {
        return fidelite.mouvements(id, partyId());
    }

    @GetMapping("/programmes/{id}/recompenses")
    public List<RecompenseView> recompenses(@PathVariable UUID id) {
        return fidelite.recompenses(id);
    }

    @PostMapping("/programmes/{id}/recompenses/{code}/echanger")
    public EchangeView echanger(@PathVariable UUID id, @PathVariable String code) {
        return fidelite.echanger(id, code, partyId());
    }

    private static UUID partyId() {
        UUID partyId = CurrentUser.partyId();
        if (partyId == null) {
            throw new IllegalArgumentException("partyId requis");
        }
        return partyId;
    }

    private static List<PalierSaisie> paliers(List<PalierRequest> requests) {
        if (requests == null) {
            return null;
        }
        return requests.stream()
                .map(request -> new PalierSaisie(
                        request.code(),
                        request.label(),
                        request.seuilPoints(),
                        request.pointsParMille(),
                        request.avantages()))
                .toList();
    }

    public record CreerProgrammeRequest(
            @NotBlank String nom,
            ProgrammePortee portee,
            UUID uniteId,
            String uniteNom,
            Set<String> profils,
            int pointsParMille,
            int pointsParrain,
            int pointsFilleul,
            List<PalierRequest> paliers
    ) {
    }

    public record PalierRequest(
            @NotBlank String code,
            @NotBlank String label,
            int seuilPoints,
            BigDecimal pointsParMille,
            List<String> avantages
    ) {
    }

    public record ClientRequest(@NotNull UUID partyId) {
    }
}
