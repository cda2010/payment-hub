package com.gateway.payment.identity.api.dto;

import com.gateway.payment.identity.domain.*;

import org.springframework.data.domain.Page;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class IdentityDtos {
    private IdentityDtos() {}

    public record PageDto<T>(List<T> contenu, long total, int page, int taille) {
        public static <T> PageDto<T> of(Page<T> p) {
            return new PageDto<>(p.getContent(), p.getTotalElements(), p.getNumber(), p.getSize());
        }
    }

    public record PartyDto(
            UUID id,
            String tenantId,
            String code,
            PartyKind kind,
            String displayName,
            String phone,
            String email,
            String externalRef,
            String kycLevel,
            String customerStatus,
            boolean active
    ) {
        public static PartyDto from(IdentityParty p) {
            return new PartyDto(
                    p.getId(), p.getTenantId(), p.getCode(), p.getKind(),
                    p.getDisplayName(), p.getPhone(), p.getEmail(),
                    p.getExternalRef(),
                    p.getKycLevel() != null ? p.getKycLevel() : "STANDARD",
                    p.getCustomerStatus() != null ? p.getCustomerStatus() : (p.isActive() ? "ACTIVE" : "INACTIVE"),
                    p.isActive());
        }
    }

    public record PartyRequest(
            String tenantId,
            String code,
            PartyKind kind,
            String displayName,
            String phone,
            String email,
            String externalRef,
            String kycLevel,
            String customerStatus,
            Boolean active
    ) {}

    public record OrgNodeDto(
            UUID id,
            String tenantId,
            String code,
            String name,
            OrgNodeType type,
            UUID parentId,
            boolean active,
            List<OrgNodeDto> children
    ) {
        public static OrgNodeDto from(IdentityOrgNode n, List<OrgNodeDto> children) {
            return new OrgNodeDto(
                    n.getId(), n.getTenantId(), n.getCode(), n.getName(),
                    n.getType(), n.getParentId(), n.isActive(), children);
        }

        public static OrgNodeDto flat(IdentityOrgNode n) {
            return from(n, List.of());
        }
    }

    public record OrgNodeRequest(
            String tenantId,
            String code,
            String name,
            OrgNodeType type,
            UUID parentId
    ) {}

    public record UserDto(
            UUID id,
            String tenantId,
            String username,
            UUID partyId,
            IdentityAudience audience,
            String role,
            boolean enabled,
            Instant lastLoginAt
    ) {
        public static UserDto from(IdentityChannelUser u) {
            return new UserDto(
                    u.getId(), u.getTenantId(), u.getUsername(), u.getPartyId(),
                    u.getAudience(), u.getRole(), u.isEnabled(), u.getLastLoginAt());
        }
    }

    public record UserRequest(
            String tenantId,
            String username,
            UUID partyId,
            IdentityAudience audience,
            String role,
            String passwordHash
    ) {}

    /** Mise à jour partielle d'un utilisateur canal (rôle, enabled, audience). */
    public record UserUpdateRequest(
            String role,
            Boolean enabled,
            IdentityAudience audience
    ) {}

    public record MembershipDto(
            UUID id,
            UUID userId,
            UUID orgNodeId,
            AccessScope scope,
            boolean active
    ) {
        public static MembershipDto from(IdentityMembership m) {
            return new MembershipDto(m.getId(), m.getUserId(), m.getOrgNodeId(), m.getScope(), m.isActive());
        }
    }

    public record MembershipRequest(UUID userId, UUID orgNodeId, AccessScope scope) {}

    /** Corps PUT /users/{id}/memberships — remplace les scopes actifs. */
    public record MembershipReplaceRequest(List<MembershipRequest> memberships) {}

    // ——— Profils d'offre (SERVICE, TARIFF ou LIMIT) ———

    public record ProfileServiceDto(String serviceCode, String libelle, Boolean active) {
        public static ProfileServiceDto from(IdentityProfileService s) {
            return new ProfileServiceDto(s.getServiceCode(), s.getLibelle(), s.isActive());
        }

        public boolean estActif() {
            return active == null || active;
        }
    }

    public record ProfileGridDto(String produitCode, String grilleCode, String canalCode, String devise) {
        public static ProfileGridDto from(IdentityProfileGrid g) {
            return new ProfileGridDto(
                    g.getProduitCode(), g.getGrilleCode(),
                    g.getCanalCode() == null || g.getCanalCode().isBlank() ? null : g.getCanalCode(),
                    g.getDevise());
        }
    }

    /** Ligne d'un profil de limite : produit + sens → code de groupe payment-rules. */
    public record ProfileLimitDto(String produitCode, SensLimite sens, String groupeCode) {
        public static ProfileLimitDto from(IdentityProfileLimit l) {
            return new ProfileLimitDto(l.getProduitCode(), l.getSens(), l.getGroupeCode());
        }
    }

    public record ProfileDto(
            UUID id,
            String tenantId,
            String code,
            String libelle,
            String description,
            ProfileKind kind,
            IdentityAudience audience,
            boolean active,
            List<ProfileServiceDto> services,
            List<ProfileGridDto> grilles,
            List<ProfileLimitDto> limites
    ) {
        public static ProfileDto from(
                IdentityProfile p,
                List<ProfileServiceDto> services,
                List<ProfileGridDto> grilles,
                List<ProfileLimitDto> limites
        ) {
            return new ProfileDto(
                    p.getId(), p.getTenantId(), p.getCode(), p.getLibelle(), p.getDescription(),
                    p.getKind() != null ? p.getKind() : ProfileKind.SERVICE,
                    p.getAudience(), p.isActive(), services, grilles, limites);
        }

        public static ProfileDto resume(IdentityProfile p) {
            return from(p, List.of(), List.of(), List.of());
        }
    }

    public record ProfileRequest(
            String tenantId,
            String code,
            String libelle,
            String description,
            ProfileKind kind,
            IdentityAudience audience,
            List<ProfileServiceDto> services,
            List<ProfileGridDto> grilles,
            List<ProfileLimitDto> limites
    ) {}

    public record PartyProfileDto(
            UUID id,
            UUID partyId,
            UUID profileId,
            String profileCode,
            String profileLibelle,
            ProfileKind profileKind,
            LocalDate dateDebut,
            LocalDate dateFin,
            boolean active
    ) {}

    public record PartyProfileRequest(UUID profileId, LocalDate dateDebut, LocalDate dateFin) {}

    /** Affectation d'un profil à une unité du réseau (payment-network). */
    public record UniteProfileDto(
            UUID id,
            UUID uniteId,
            UUID profileId,
            String profileCode,
            String profileLibelle,
            ProfileKind profileKind,
            LocalDate dateDebut,
            LocalDate dateFin,
            boolean active
    ) {}

    public record UniteProfileRequest(
            UUID profileId,
            String tenantId,
            LocalDate dateDebut,
            LocalDate dateFin
    ) {}

    /**
     * Offre résolue à une date : union des services, grilles et limites des profils actifs.
     * {@code ownerKind} = {@code PARTY} ou {@code UNITE}.
     * Pour une même clé (produit + canal, ou produit + sens), l'affectation la plus récente l'emporte.
     */
    public record OffreDto(
            String ownerKind,
            UUID ownerId,
            String ownerCode,
            LocalDate date,
            List<ProfileDto> profils,
            List<ProfileServiceDto> services,
            List<ProfileGridDto> grilles,
            List<ProfileLimitDto> limites
    ) {}
}
