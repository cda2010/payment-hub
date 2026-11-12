package com.gateway.payment.network.api;

import com.gateway.payment.network.api.dto.NetworkDtos.CreationZoneRequest;
import com.gateway.payment.network.api.dto.NetworkDtos.ModificationZoneRequest;
import com.gateway.payment.network.api.dto.NetworkDtos.ZoneDto;
import com.gateway.payment.network.service.ZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/network/zones")
@Tag(name = "Zones", description = "Zones monétaires du réseau (code, libellé, devise)")
@PreAuthorize("isAuthenticated()")
public class ZoneController {

    private static final String ECRITURE = "hasAnyRole('ADMIN','CONFIG_MANAGER')";

    private final ZoneService zones;

    public ZoneController(ZoneService zones) {
        this.zones = zones;
    }

    @GetMapping
    @Operation(summary = "Lister les zones, avec leur nombre de pays et d'unités")
    public List<ZoneDto> lister() {
        return zones.lister();
    }

    @GetMapping("/{code}")
    @Operation(summary = "Consulter une zone")
    public ZoneDto consulter(@PathVariable String code) {
        return zones.consulter(code);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Créer une zone (code unique et définitif)")
    public ZoneDto creer(@Valid @RequestBody CreationZoneRequest body, Authentication auth) {
        return zones.creer(body, auth.getName());
    }

    @PutMapping("/{code}")
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Modifier le libellé, et la devise tant que la zone ne contient aucune unité")
    public ZoneDto modifier(@PathVariable String code, @Valid @RequestBody ModificationZoneRequest body,
                            Authentication auth) {
        return zones.modifier(code, body, auth.getName());
    }

    @PostMapping("/{code}/activer")
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Réactiver une zone")
    public ZoneDto activer(@PathVariable String code, Authentication auth) {
        return zones.activer(code, auth.getName());
    }

    @PostMapping("/{code}/desactiver")
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Désactiver une zone (refusé s'il lui reste des pays actifs)")
    public ZoneDto desactiver(@PathVariable String code, Authentication auth) {
        return zones.desactiver(code, auth.getName());
    }
}
