package com.gateway.payment.alias.api;

import com.gateway.payment.alias.api.dto.AliasDtos.*;
import com.gateway.payment.alias.domain.AliasOwnerKind;
import com.gateway.payment.alias.service.AliasService;
import com.gateway.payment.alias.service.AliasService.AccesRefuse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/aliases")
@Tag(name = "Aliases", description = "Alias de comptes (Party / Org identity, Unité network)")
public class AliasController {

    private final AliasService aliases;

    public AliasController(AliasService aliases) {
        this.aliases = aliases;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les alias (optionnel : filtre par propriétaire)")
    public List<AliasDto> list(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) AliasOwnerKind ownerKind,
            @RequestParam(required = false) UUID ownerId,
            @RequestParam(required = false) String etiquette
    ) {
        return wrapIllegal(() -> aliases.list(tenantId, ownerKind, ownerId, etiquette));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Détail d'un alias")
    public AliasDto get(@PathVariable UUID id) {
        return wrap(() -> aliases.get(id));
    }

    @GetMapping("/resolve/{aliasCode}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Résoudre un alias vers la référence core")
    public AliasResolveDto resolve(
            @PathVariable String aliasCode,
            @RequestParam(required = false) String tenantId
    ) {
        return wrap(() -> aliases.resolve(tenantId, aliasCode));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR','GROUP_ADMIN')")
    @Operation(summary = "Créer un alias (ownerKind = PARTY | ORG | UNITE | POINT_VENTE), avec une étiquette")
    public AliasDto create(@RequestBody AliasRequest body) {
        return wrapIllegal(() -> aliases.create(body));
    }

    @PostMapping("/{id}/defaut")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Définir cet alias comme compte par défaut de son propriétaire")
    public AliasDto definirDefaut(@PathVariable UUID id, Authentication auth) {
        return wrapIllegal(() -> aliases.definirDefaut(id, partyId(auth), backoffice(auth)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR','GROUP_ADMIN')")
    @Operation(summary = "Modifier un alias")
    public AliasDto update(@PathVariable UUID id, @RequestBody AliasRequest body) {
        return wrapIllegal(() -> aliases.update(id, body));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR','GROUP_ADMIN')")
    @Operation(summary = "Désactiver un alias")
    public void deactivate(@PathVariable UUID id) {
        wrap(() -> {
            aliases.deactivate(id);
            return null;
        });
    }

    private static boolean backoffice(Authentication auth) {
        if (auth == null) {
            return false;
        }
        for (GrantedAuthority droit : auth.getAuthorities()) {
            String role = droit.getAuthority();
            if ("ROLE_ADMIN".equals(role) || "ROLE_CONFIG_MANAGER".equals(role)
                    || "ROLE_OPERATOR".equals(role) || "ROLE_GROUP_ADMIN".equals(role)) {
                return true;
            }
        }
        return false;
    }

    private static UUID partyId(Authentication auth) {
        if (auth == null || !(auth.getCredentials() instanceof UUID id)) {
            return null;
        }
        return id;
    }

    private <T> T wrap(java.util.concurrent.Callable<T> call) {
        try {
            return call.call();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private <T> T wrapIllegal(java.util.concurrent.Callable<T> call) {
        try {
            return call.call();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (AccesRefuse e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
