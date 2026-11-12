package com.gateway.payment.identity.api;

import com.gateway.payment.identity.api.dto.IdentityDtos.*;
import com.gateway.payment.identity.domain.IdentityAudience;
import com.gateway.payment.identity.domain.PartyKind;
import com.gateway.payment.identity.domain.ProfileKind;
import com.gateway.payment.identity.service.IdentityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/identity")
@Tag(name = "Identity", description = "Référentiel parties, org, utilisateurs canaux, alias, profils d'offre")
public class IdentityController {

    private final IdentityService identity;

    public IdentityController(IdentityService identity) {
        this.identity = identity;
    }

    // ——— Parties (clients / organisations) ———

    @GetMapping("/parties")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les parties actives (clients / orgs) — liste courte pour listes déroulantes")
    public List<PartyDto> listParties(@RequestParam(required = false) String tenantId) {
        return identity.listParties(tenantId);
    }

    @GetMapping("/parties/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Recherche avancée des parties (texte, kind, tenant, actif), page par page")
    public PageDto<PartyDto> searchParties(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) PartyKind kind,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String texte,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer taille,
            @RequestParam(required = false) String tri,
            @RequestParam(required = false) String sens
    ) {
        return identity.rechercherParties(
                new IdentityService.FiltreParty(tenantId, kind, active, texte),
                Pagination.parties(page, taille, tri, sens));
    }

    @GetMapping("/parties/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Détail d'une party")
    public PartyDto getParty(@PathVariable UUID id) {
        return wrap(() -> identity.getParty(id));
    }

    @PostMapping("/parties")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR','GROUP_ADMIN')")
    @Operation(summary = "Créer une party")
    public PartyDto createParty(@RequestBody PartyRequest body) {
        return wrapIllegal(() -> identity.createParty(body));
    }

    @PutMapping("/parties/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR','GROUP_ADMIN')")
    @Operation(summary = "Modifier une party (libellé, contacts, kind, actif ; le code est immuable)")
    public PartyDto updateParty(@PathVariable UUID id, @RequestBody PartyRequest body) {
        return wrapIllegal(() -> identity.updateParty(id, body));
    }

    // ——— Organisation ———

    @GetMapping("/org-nodes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les nœuds org (plat)")
    public List<OrgNodeDto> listOrg(@RequestParam(required = false) String tenantId) {
        return identity.listOrgFlat(tenantId);
    }

    @GetMapping("/org-nodes/tree")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Arbre organisationnel (entité / sous-entité)")
    public List<OrgNodeDto> orgTree(@RequestParam(required = false) String tenantId) {
        return identity.orgTree(tenantId);
    }

    @GetMapping("/org-nodes/legal-entities")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Entités légales uniquement")
    public List<OrgNodeDto> legalEntities(@RequestParam(required = false) String tenantId) {
        return identity.legalEntities(tenantId);
    }

    @GetMapping("/org-nodes/{id}/children")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Enfants d'un nœud")
    public List<OrgNodeDto> children(@PathVariable UUID id) {
        return wrap(() -> identity.children(id));
    }

    @PostMapping("/org-nodes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR','GROUP_ADMIN')")
    @Operation(summary = "Créer un nœud org")
    public OrgNodeDto createOrg(@RequestBody OrgNodeRequest body) {
        return wrapIllegal(() -> identity.createOrgNode(body));
    }

    // ——— Channel users ———

    @GetMapping("/users")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les utilisateurs canaux")
    public List<UserDto> listUsers(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) IdentityAudience audience,
            @RequestParam(required = false, defaultValue = "false") boolean includeDisabled
    ) {
        return identity.listUsers(tenantId, audience, includeDisabled);
    }

    @GetMapping("/users/by-username/{username}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Utilisateur canal par username")
    public UserDto getByUsername(
            @PathVariable String username,
            @RequestParam(required = false) String tenantId
    ) {
        return wrap(() -> identity.getUserByUsername(tenantId, username));
    }

    @GetMapping("/users/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Détail utilisateur canal")
    public UserDto getUser(@PathVariable UUID id) {
        return wrap(() -> identity.getUser(id));
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR','GROUP_ADMIN')")
    @Operation(summary = "Créer un utilisateur canal")
    public UserDto createUser(@RequestBody UserRequest body) {
        return wrapIllegal(() -> identity.createUser(body));
    }

    @PutMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR','GROUP_ADMIN')")
    @Operation(summary = "Modifier un utilisateur canal (rôle, actif, party)")
    public UserDto updateUser(@PathVariable UUID id, @RequestBody UserUpdateRequest body) {
        return wrapIllegal(() -> identity.updateUser(id, body));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR','GROUP_ADMIN')")
    @Operation(summary = "Désactiver un utilisateur canal (soft-delete)")
    public UserDto deactivateUser(@PathVariable UUID id) {
        return wrapIllegal(() -> identity.deactivateUser(id));
    }

    @GetMapping("/users/{id}/memberships")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Scopes org d'un utilisateur")
    public List<MembershipDto> memberships(@PathVariable UUID id) {
        return wrap(() -> identity.membershipsForUser(id));
    }

    @PutMapping("/users/{id}/memberships")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR','GROUP_ADMIN')")
    @Operation(summary = "Remplacer les memberships (scopes) d'un utilisateur")
    public List<MembershipDto> replaceMemberships(
            @PathVariable UUID id,
            @RequestBody MembershipReplaceRequest body
    ) {
        return wrapIllegal(() -> identity.replaceMemberships(
                id,
                body != null ? body.memberships() : java.util.List.of()
        ));
    }

    @PostMapping("/memberships")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR','GROUP_ADMIN')")
    @Operation(summary = "Accorder un membership org (upsert)")
    public MembershipDto grant(@RequestBody MembershipRequest body) {
        return wrapIllegal(() -> identity.grantMembership(body));
    }

    @DeleteMapping("/memberships/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR','GROUP_ADMIN')")
    @Operation(summary = "Révoquer un membership (soft)")
    public void revokeMembership(@PathVariable UUID id) {
        wrapIllegal(() -> {
            identity.revokeMembership(id);
            return null;
        });
    }

    // ——— Profils d'offre (services disponibles + grilles tarifaires) ———

    @GetMapping("/profiles")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lister les profils (SERVICE = produits, TARIFF = grilles, LIMIT = plafonds)")
    public List<ProfileDto> listProfiles(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) IdentityAudience audience,
            @RequestParam(required = false) ProfileKind kind
    ) {
        return identity.listProfiles(tenantId, audience, kind);
    }

    @GetMapping("/profiles/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Détail d'un profil (services, grilles ou limites)")
    public ProfileDto getProfile(@PathVariable UUID id) {
        return wrap(() -> identity.getProfile(id));
    }

    @PostMapping("/profiles")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR')")
    @Operation(summary = "Créer un profil d'offre")
    public ProfileDto createProfile(@RequestBody ProfileRequest body) {
        return wrapIllegal(() -> identity.createProfile(body));
    }

    @PutMapping("/profiles/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR')")
    @Operation(summary = "Modifier un profil (libellé, services, grilles ou limites)")
    public ProfileDto updateProfile(@PathVariable UUID id, @RequestBody ProfileRequest body) {
        return wrapIllegal(() -> identity.updateProfile(id, body));
    }

    @GetMapping("/parties/{id}/profiles")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Profils affectés à une party")
    public List<PartyProfileDto> partyProfiles(@PathVariable UUID id) {
        return wrap(() -> identity.listPartyProfiles(id));
    }

    @PostMapping("/parties/{id}/profiles")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR','GROUP_ADMIN')")
    @Operation(summary = "Affecter un profil à une party")
    public PartyProfileDto assignProfile(@PathVariable UUID id, @RequestBody PartyProfileRequest body) {
        return wrapIllegal(() -> identity.assignProfile(id, body));
    }

    @DeleteMapping("/parties/{partyId}/profiles/{profileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR','GROUP_ADMIN')")
    @Operation(summary = "Retirer un profil d'une party (désactivation)")
    public void revokeProfile(@PathVariable UUID partyId, @PathVariable UUID profileId) {
        wrapVoid(() -> identity.revokeProfile(partyId, profileId));
    }

    @GetMapping("/parties/{id}/offer")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Offre résolue d'une party : services, grilles et limites")
    public OffreDto partyOffer(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return wrap(() -> identity.offreParty(id, date));
    }

    @GetMapping("/users/{id}/offer")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Offre résolue via la party de l'utilisateur canal")
    public OffreDto userOffer(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return wrap(() -> identity.offreUser(id, date));
    }

    @GetMapping("/unites/{id}/profiles")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Profils d'offre affectés à une unité du réseau")
    public List<UniteProfileDto> uniteProfiles(@PathVariable UUID id) {
        return wrap(() -> identity.listUniteProfiles(id));
    }

    @PostMapping("/unites/{id}/profiles")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR','GROUP_ADMIN')")
    @Operation(summary = "Affecter un profil d'offre à une unité du réseau")
    public UniteProfileDto assignUniteProfile(@PathVariable UUID id, @RequestBody UniteProfileRequest body) {
        return wrapIllegal(() -> identity.assignUniteProfile(id, body));
    }

    @DeleteMapping("/unites/{uniteId}/profiles/{profileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR','GROUP_ADMIN')")
    @Operation(summary = "Retirer un profil d'une unité (désactivation)")
    public void revokeUniteProfile(@PathVariable UUID uniteId, @PathVariable UUID profileId) {
        wrapVoid(() -> identity.revokeUniteProfile(uniteId, profileId));
    }

    @GetMapping("/unites/{id}/offer")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Offre résolue d'une unité du réseau : services, grilles et limites")
    public OffreDto uniteOffer(
            @PathVariable UUID id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return wrap(() -> identity.offreUnite(id, date));
    }

    private void wrapVoid(Runnable call) {
        wrap(() -> {
            call.run();
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
