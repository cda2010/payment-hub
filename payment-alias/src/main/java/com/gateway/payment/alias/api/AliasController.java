package com.gateway.payment.alias.api;

import com.gateway.payment.alias.api.dto.AliasDtos.*;
import com.gateway.payment.alias.domain.AliasOwnerKind;
import com.gateway.payment.alias.service.AliasService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
            @RequestParam(required = false) UUID ownerId
    ) {
        return aliases.list(tenantId, ownerKind, ownerId);
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
    @Operation(summary = "Créer un alias (ownerKind = PARTY | ORG | UNITE)")
    public AliasDto create(@RequestBody AliasRequest body) {
        return wrapIllegal(() -> aliases.create(body));
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
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
