package com.gateway.payment.network.api;

import com.gateway.payment.network.api.dto.NetworkDtos.MembreDto;
import com.gateway.payment.network.api.dto.NetworkDtos.MembreRequest;
import com.gateway.payment.network.api.dto.NetworkDtos.PageDto;
import com.gateway.payment.network.api.dto.NetworkDtos.UniteResume;
import com.gateway.payment.network.service.RegroupementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/network/regroupements")
@Tag(name = "Regroupements", description = "Membres d'un regroupement et unités couvertes")
@PreAuthorize("isAuthenticated()")
public class RegroupementController {

    private static final String ECRITURE = "hasAnyRole('ADMIN','CONFIG_MANAGER')";

    private final RegroupementService regroupements;

    public RegroupementController(RegroupementService regroupements) {
        this.regroupements = regroupements;
    }

    @GetMapping("/{id}/membres")
    @Operation(summary = "Membres directs du regroupement")
    public List<MembreDto> membres(@PathVariable UUID id) {
        return regroupements.membres(id);
    }

    @GetMapping("/{id}/perimetre")
    @Operation(summary = "Unités couvertes, page par page : les membres et toute leur descendance")
    public PageDto<UniteResume> perimetre(@PathVariable UUID id,
                                          @RequestParam(required = false) Integer page,
                                          @RequestParam(required = false) Integer taille) {
        return regroupements.perimetre(id, Pagination.de(page, taille, null, null));
    }

    @PostMapping("/{id}/membres")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Ajouter une unité de n'importe quel niveau ; renvoie la liste des membres")
    public List<MembreDto> ajouter(@PathVariable UUID id, @Valid @RequestBody MembreRequest body, Authentication auth) {
        return regroupements.ajouterMembre(id, body.uniteId(), auth.getName());
    }

    @DeleteMapping("/{id}/membres/{uniteId}")
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Retirer un membre ; renvoie la liste des membres")
    public List<MembreDto> retirer(@PathVariable UUID id, @PathVariable UUID uniteId) {
        return regroupements.retirerMembre(id, uniteId);
    }
}
