package com.gateway.payment.identity.api;

import com.gateway.payment.identity.api.dto.PartyActionDtos.CompleteActionRequest;
import com.gateway.payment.identity.api.dto.PartyActionDtos.OpenActionRequest;
import com.gateway.payment.identity.api.dto.PartyActionDtos.PartyActionView;
import com.gateway.payment.identity.domain.IdentityChannelUser;
import com.gateway.payment.identity.repository.IdentityChannelUserRepository;
import com.gateway.payment.identity.service.PartyActionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Inbox « À faire » — une party, ses actions ouvertes.
 */
@RestController
@RequestMapping("/api/v1/identity/parties")
@Tag(name = "Party actions", description = "Inbox À faire par party")
@SecurityRequirement(name = "bearerAuth")
public class PartyActionController {

    private static final Set<String> STAFF = Set.of(
            "ROLE_ADMIN", "ROLE_CONFIG_MANAGER", "ROLE_OPERATOR", "ROLE_SYSTEM", "ROLE_GROUP_ADMIN"
    );

    private final PartyActionService actions;
    private final IdentityChannelUserRepository users;

    public PartyActionController(PartyActionService actions, IdentityChannelUserRepository users) {
        this.actions = actions;
        this.users = users;
    }

    @GetMapping("/{partyId}/actions")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les actions ouvertes (À faire)")
    public List<PartyActionView> listOpen(@PathVariable UUID partyId) {
        assertPartyAccess(partyId);
        return actions.listOpen(partyId);
    }

    @PostMapping("/{partyId}/actions")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR','SYSTEM')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ouvrir ou rafraîchir une action")
    public PartyActionView open(@PathVariable UUID partyId, @Valid @RequestBody OpenActionRequest body) {
        return actions.open(partyId, body);
    }

    @PostMapping("/{partyId}/actions/complete")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR','SYSTEM')")
    @Operation(summary = "Clôturer l'action ouverte d'un agrégat")
    public PartyActionView complete(@PathVariable UUID partyId, @Valid @RequestBody CompleteActionRequest body) {
        return actions.complete(partyId, body);
    }

    @PostMapping("/{partyId}/actions/{actionId}/dismiss")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Masquer une action")
    public PartyActionView dismiss(@PathVariable UUID partyId, @PathVariable UUID actionId) {
        assertPartyAccess(partyId);
        return actions.dismiss(partyId, actionId);
    }

    /**
     * Un rôle staff lit toute party. Un client ne lit que la party de son utilisateur canal.
     */
    private void assertPartyAccess(UUID partyId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentification requise");
        }
        boolean staff = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(STAFF::contains);
        if (staff) {
            return;
        }
        String username = auth.getName();
        boolean owns = users.findByUsernameIgnoreCaseAndEnabledTrue(username).stream()
                .map(IdentityChannelUser::getPartyId)
                .anyMatch(partyId::equals);
        if (!owns) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Accès refusé à cette party");
        }
    }
}
