package com.gateway.payment.identity.service;

import com.gateway.payment.identity.api.dto.IdentityDtos.*;
import com.gateway.payment.identity.domain.*;
import com.gateway.payment.identity.repository.*;
import com.gateway.payment.inbox.kafka.PartyActionEvent;
import com.gateway.payment.inbox.kafka.PartyActionPublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class IdentityService {

    public static final String DEFAULT_TENANT = "CORIS_DEMO";

    private static final Logger log = LoggerFactory.getLogger(IdentityService.class);

    private final IdentityPartyRepository parties;
    private final IdentityOrgNodeRepository orgs;
    private final IdentityChannelUserRepository users;
    private final IdentityMembershipRepository memberships;
    private final IdentityProfileRepository profiles;
    private final IdentityProfileServiceRepository profileServices;
    private final IdentityProfileGridRepository profileGrids;
    private final IdentityProfileLimitRepository profileLimits;
    private final IdentityPartyProfileRepository partyProfiles;
    private final IdentityUniteProfileRepository uniteProfiles;
    private final PartyActionPublisher actions;

    public IdentityService(
            IdentityPartyRepository parties,
            IdentityOrgNodeRepository orgs,
            IdentityChannelUserRepository users,
            IdentityMembershipRepository memberships,
            IdentityProfileRepository profiles,
            IdentityProfileServiceRepository profileServices,
            IdentityProfileGridRepository profileGrids,
            IdentityProfileLimitRepository profileLimits,
            IdentityPartyProfileRepository partyProfiles,
            IdentityUniteProfileRepository uniteProfiles,
            PartyActionPublisher actions
    ) {
        this.parties = parties;
        this.orgs = orgs;
        this.users = users;
        this.memberships = memberships;
        this.profiles = profiles;
        this.profileServices = profileServices;
        this.profileGrids = profileGrids;
        this.profileLimits = profileLimits;
        this.partyProfiles = partyProfiles;
        this.uniteProfiles = uniteProfiles;
        this.actions = actions;
    }

    // ——— Parties ———

    public record FiltreParty(String tenantId, PartyKind kind, Boolean active, String texte) {}

    @Transactional(readOnly = true)
    public List<PartyDto> listParties(String tenantId) {
        return parties.findByTenantIdAndActiveTrueOrderByDisplayNameAsc(tenant(tenantId)).stream()
                .map(PartyDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageDto<PartyDto> rechercherParties(FiltreParty filtre, Pageable pageable) {
        return PageDto.of(parties.findAll(specificationParty(filtre), pageable).map(PartyDto::from));
    }

    @Transactional(readOnly = true)
    public PartyDto getParty(UUID id) {
        return PartyDto.from(parties.findById(id).orElseThrow(() -> notFound("Party", id)));
    }

    public PartyDto createParty(PartyRequest req) {
        String tenant = tenant(req.tenantId());
        require(req.code(), "code");
        require(req.displayName(), "displayName");
        if (parties.findByTenantIdAndCode(tenant, req.code()).isPresent()) {
            throw new IllegalArgumentException("Party code already exists: " + req.code());
        }
        IdentityParty p = IdentityParty.builder()
                .tenantId(tenant)
                .code(req.code().trim())
                .kind(req.kind() != null ? req.kind() : PartyKind.PERSON)
                .displayName(req.displayName().trim())
                .phone(blankToNull(req.phone()))
                .email(blankToNull(req.email()))
                .externalRef(blankToNull(req.externalRef()))
                .kycLevel(blankToNull(req.kycLevel()) != null ? req.kycLevel().trim().toUpperCase() : "STANDARD")
                .customerStatus(blankToNull(req.customerStatus()) != null
                        ? req.customerStatus().trim().toUpperCase() : "ACTIVE")
                .active(req.active() == null || req.active())
                .build();
        IdentityParty saved = parties.save(p);
        signalKyc(saved);
        return PartyDto.from(saved);
    }

    public PartyDto updateParty(UUID id, PartyRequest req) {
        IdentityParty p = parties.findById(id).orElseThrow(() -> notFound("Party", id));
        require(req.displayName(), "displayName");
        if (req.kind() != null) {
            p.setKind(req.kind());
        }
        p.setDisplayName(req.displayName().trim());
        p.setPhone(blankToNull(req.phone()));
        p.setEmail(blankToNull(req.email()));
        p.setExternalRef(blankToNull(req.externalRef()));
        if (blankToNull(req.kycLevel()) != null) {
            p.setKycLevel(req.kycLevel().trim().toUpperCase());
        }
        if (blankToNull(req.customerStatus()) != null) {
            p.setCustomerStatus(req.customerStatus().trim().toUpperCase());
        }
        if (req.active() != null) {
            p.setActive(req.active());
            if (req.customerStatus() == null) {
                p.setCustomerStatus(req.active() ? "ACTIVE" : "INACTIVE");
            }
        }
        IdentityParty saved = parties.save(p);
        signalKyc(saved);
        return PartyDto.from(saved);
    }

    /**
     * KYC_RENEW appartient à identity. Un niveau BASIC ouvre l'action,
     * un niveau supérieur la clôt. L'échec Kafka ne bloque pas l'écriture de la party.
     */
    private void signalKyc(IdentityParty party) {
        if (party.getId() == null) {
            return;
        }
        try {
            if ("BASIC".equalsIgnoreCase(party.getKycLevel())) {
                actions.publish(PartyActionEvent.open(
                        party.getId(),
                        party.getTenantId(),
                        "KYC_RENEW",
                        "party",
                        party.getId().toString(),
                        null,
                        Instant.now().plus(30, ChronoUnit.DAYS)));
            } else {
                actions.publish(PartyActionEvent.complete(
                        party.getId(),
                        party.getTenantId(),
                        "KYC_RENEW",
                        "party",
                        party.getId().toString(),
                        "DONE"));
            }
        } catch (Exception e) {
            log.warn("Échec publication KYC_RENEW pour {} : {}", party.getId(), e.getMessage());
        }
    }

    // ——— Org ———

    @Transactional(readOnly = true)
    public List<OrgNodeDto> listOrgFlat(String tenantId) {
        return orgs.findByTenantIdAndActiveTrueOrderByNameAsc(tenant(tenantId)).stream()
                .map(OrgNodeDto::flat)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrgNodeDto> orgTree(String tenantId) {
        String t = tenant(tenantId);
        List<IdentityOrgNode> all = orgs.findByTenantIdAndActiveTrueOrderByNameAsc(t);
        Map<UUID, List<IdentityOrgNode>> byParent = all.stream()
                .filter(n -> n.getParentId() != null)
                .collect(Collectors.groupingBy(IdentityOrgNode::getParentId));
        return all.stream()
                .filter(n -> n.getParentId() == null)
                .map(n -> buildTree(n, byParent))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrgNodeDto> children(UUID parentId) {
        IdentityOrgNode parent = orgs.findById(parentId).orElseThrow(() -> notFound("OrgNode", parentId));
        return orgs.findByTenantIdAndParentIdOrderByNameAsc(parent.getTenantId(), parentId).stream()
                .map(OrgNodeDto::flat)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrgNodeDto> legalEntities(String tenantId) {
        return orgs.findByTenantIdAndTypeAndActiveTrue(tenant(tenantId), OrgNodeType.LEGAL_ENTITY).stream()
                .map(OrgNodeDto::flat)
                .toList();
    }

    public OrgNodeDto createOrgNode(OrgNodeRequest req) {
        String tenant = tenant(req.tenantId());
        require(req.code(), "code");
        require(req.name(), "name");
        if (orgs.findByTenantIdAndCode(tenant, req.code()).isPresent()) {
            throw new IllegalArgumentException("Org code already exists: " + req.code());
        }
        if (req.parentId() != null) {
            orgs.findById(req.parentId()).orElseThrow(() -> notFound("OrgNode", req.parentId()));
        }
        IdentityOrgNode n = IdentityOrgNode.builder()
                .tenantId(tenant)
                .code(req.code().trim())
                .name(req.name().trim())
                .type(req.type() != null ? req.type() : OrgNodeType.LEGAL_ENTITY)
                .parentId(req.parentId())
                .active(true)
                .build();
        return OrgNodeDto.flat(orgs.save(n));
    }

    // ——— Users ———

    @Transactional(readOnly = true)
    public List<UserDto> listUsers(String tenantId, IdentityAudience audience, boolean includeDisabled) {
        String t = tenant(tenantId);
        List<IdentityChannelUser> list;
        if (includeDisabled) {
            list = audience == null
                    ? users.findByTenantIdOrderByUsernameAsc(t)
                    : users.findByTenantIdAndAudienceOrderByUsernameAsc(t, audience);
        } else {
            list = audience == null
                    ? users.findByTenantIdAndEnabledTrueOrderByUsernameAsc(t)
                    : users.findByTenantIdAndAudienceAndEnabledTrue(t, audience);
        }
        return list.stream().map(UserDto::from).toList();
    }

    @Transactional(readOnly = true)
    public UserDto getUser(UUID id) {
        return UserDto.from(users.findById(id).orElseThrow(() -> notFound("User", id)));
    }

    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String tenantId, String username) {
        return UserDto.from(users.findByTenantIdAndUsername(tenant(tenantId), username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username)));
    }

    public UserDto createUser(UserRequest req) {
        String tenant = tenant(req.tenantId());
        require(req.username(), "username");
        if (req.partyId() == null) {
            throw new IllegalArgumentException("partyId required");
        }
        parties.findById(req.partyId()).orElseThrow(() -> notFound("Party", req.partyId()));
        if (users.findByTenantIdAndUsername(tenant, req.username()).isPresent()) {
            throw new IllegalArgumentException("Username already exists: " + req.username());
        }
        IdentityChannelUser u = IdentityChannelUser.builder()
                .tenantId(tenant)
                .username(req.username().trim())
                .partyId(req.partyId())
                .audience(req.audience() != null ? req.audience() : IdentityAudience.CORPORATE)
                .role(req.role() != null ? req.role() : "USER")
                .passwordHash(blankToNull(req.passwordHash()))
                .enabled(true)
                .build();
        return UserDto.from(users.save(u));
    }

    public UserDto updateUser(UUID id, UserUpdateRequest req) {
        IdentityChannelUser u = users.findById(id).orElseThrow(() -> notFound("User", id));
        if (blankToNull(req.role()) != null) {
            u.setRole(req.role().trim());
        }
        if (req.enabled() != null) {
            u.setEnabled(req.enabled());
        }
        if (req.audience() != null) {
            u.setAudience(req.audience());
        }
        return UserDto.from(users.save(u));
    }

    /** Soft-delete : désactive le compte canal. */
    public UserDto deactivateUser(UUID id) {
        IdentityChannelUser u = users.findById(id).orElseThrow(() -> notFound("User", id));
        u.setEnabled(false);
        return UserDto.from(users.save(u));
    }

    // ——— Memberships ———

    @Transactional(readOnly = true)
    public List<MembershipDto> membershipsForUser(UUID userId) {
        users.findById(userId).orElseThrow(() -> notFound("User", userId));
        return memberships.findByUserIdAndActiveTrue(userId).stream()
                .map(MembershipDto::from)
                .toList();
    }

    public MembershipDto grantMembership(MembershipRequest req) {
        if (req.userId() == null || req.orgNodeId() == null) {
            throw new IllegalArgumentException("userId and orgNodeId required");
        }
        users.findById(req.userId()).orElseThrow(() -> notFound("User", req.userId()));
        orgs.findById(req.orgNodeId()).orElseThrow(() -> notFound("OrgNode", req.orgNodeId()));
        Optional<IdentityMembership> existing = memberships.findByUserIdAndOrgNodeId(req.userId(), req.orgNodeId());
        if (existing.isPresent()) {
            IdentityMembership m = existing.get();
            m.setActive(true);
            if (req.scope() != null) {
                m.setScope(req.scope());
            }
            return MembershipDto.from(memberships.save(m));
        }
        IdentityMembership m = IdentityMembership.builder()
                .userId(req.userId())
                .orgNodeId(req.orgNodeId())
                .scope(req.scope() != null ? req.scope() : AccessScope.DIRECT)
                .active(true)
                .build();
        return MembershipDto.from(memberships.save(m));
    }

    public void revokeMembership(UUID membershipId) {
        IdentityMembership m = memberships.findById(membershipId)
                .orElseThrow(() -> notFound("Membership", membershipId));
        m.setActive(false);
        memberships.save(m);
    }

    /**
     * Remplace les scopes actifs d'un utilisateur par la liste fournie
     * (désactive les absents, upsert les présents).
     */
    public List<MembershipDto> replaceMemberships(UUID userId, List<MembershipRequest> desired) {
        users.findById(userId).orElseThrow(() -> notFound("User", userId));
        List<IdentityMembership> current = memberships.findByUserId(userId);
        Set<UUID> keepOrgIds = new HashSet<>();
        List<MembershipDto> out = new ArrayList<>();
        if (desired != null) {
            for (MembershipRequest req : desired) {
                if (req == null || req.orgNodeId() == null) continue;
                orgs.findById(req.orgNodeId()).orElseThrow(() -> notFound("OrgNode", req.orgNodeId()));
                keepOrgIds.add(req.orgNodeId());
                out.add(grantMembership(new MembershipRequest(userId, req.orgNodeId(), req.scope())));
            }
        }
        for (IdentityMembership m : current) {
            if (m.isActive() && !keepOrgIds.contains(m.getOrgNodeId())) {
                m.setActive(false);
                memberships.save(m);
            }
        }
        return out;
    }

    // ——— Profils d'offre (SERVICE | TARIFF | LIMIT) ———

    @Transactional(readOnly = true)
    public List<ProfileDto> listProfiles(String tenantId, IdentityAudience audience, ProfileKind kind) {
        String t = tenant(tenantId);
        List<IdentityProfile> list;
        if (kind != null && audience != null) {
            list = profiles.findByTenantIdAndKindAndAudienceAndActiveTrueOrderByCodeAsc(t, kind, audience);
        } else if (kind != null) {
            list = profiles.findByTenantIdAndKindAndActiveTrueOrderByCodeAsc(t, kind);
        } else if (audience != null) {
            list = profiles.findByTenantIdAndAudienceAndActiveTrueOrderByCodeAsc(t, audience);
        } else {
            list = profiles.findByTenantIdAndActiveTrueOrderByCodeAsc(t);
        }
        return list.stream().map(this::toProfileDto).toList();
    }

    @Transactional(readOnly = true)
    public ProfileDto getProfile(UUID id) {
        return toProfileDto(profiles.findById(id).orElseThrow(() -> notFound("Profile", id)));
    }

    public ProfileDto createProfile(ProfileRequest req) {
        String tenant = tenant(req.tenantId());
        require(req.code(), "code");
        require(req.libelle(), "libelle");
        ProfileKind kind = req.kind() != null ? req.kind() : ProfileKind.SERVICE;
        String code = req.code().trim().toUpperCase(Locale.ROOT);
        if (profiles.findByTenantIdAndCode(tenant, code).isPresent()) {
            throw new IllegalArgumentException("Profile code already exists: " + code);
        }
        verifierContenu(kind, req.services(), req.grilles(), req.limites());
        List<ProfileServiceDto> services = kind == ProfileKind.SERVICE ? ouVide(req.services()) : List.of();
        List<ProfileGridDto> grilles = kind == ProfileKind.TARIFF ? ouVide(req.grilles()) : List.of();
        List<ProfileLimitDto> limites = kind == ProfileKind.LIMIT ? ouVide(req.limites()) : List.of();
        IdentityProfile p = IdentityProfile.builder()
                .tenantId(tenant)
                .code(code)
                .libelle(req.libelle().trim())
                .description(blankToNull(req.description()))
                .kind(kind)
                .audience(req.audience())
                .active(true)
                .build();
        p = profiles.save(p);
        remplacerContenu(p.getId(), services, grilles, limites);
        return toProfileDto(p);
    }

    public ProfileDto updateProfile(UUID id, ProfileRequest req) {
        IdentityProfile p = profiles.findById(id).orElseThrow(() -> notFound("Profile", id));
        ProfileKind kind = p.getKind() != null ? p.getKind() : ProfileKind.SERVICE;
        if (req.libelle() != null && !req.libelle().isBlank()) {
            p.setLibelle(req.libelle().trim());
        }
        if (req.description() != null) {
            p.setDescription(blankToNull(req.description()));
        }
        if (req.audience() != null) {
            p.setAudience(req.audience());
        }
        profiles.save(p);
        if (req.services() != null || req.grilles() != null || req.limites() != null) {
            verifierContenu(kind, req.services(), req.grilles(), req.limites());
            remplacerContenu(p.getId(),
                    kind == ProfileKind.SERVICE ? ouVide(req.services()) : List.of(),
                    kind == ProfileKind.TARIFF ? ouVide(req.grilles()) : List.of(),
                    kind == ProfileKind.LIMIT ? ouVide(req.limites()) : List.of());
        }
        return toProfileDto(p);
    }

    @Transactional(readOnly = true)
    public List<PartyProfileDto> listPartyProfiles(UUID partyId) {
        IdentityParty party = parties.findById(partyId).orElseThrow(() -> notFound("Party", partyId));
        Map<UUID, IdentityProfile> byId = profiles.findByIdIn(
                partyProfiles.findByPartyIdOrderByDateDebutDesc(party.getId()).stream()
                        .map(IdentityPartyProfile::getProfileId).toList()
        ).stream().collect(Collectors.toMap(IdentityProfile::getId, x -> x, (a, b) -> a));
        return partyProfiles.findByPartyIdOrderByDateDebutDesc(partyId).stream()
                .map(a -> toPartyProfileDto(a, byId.get(a.getProfileId())))
                .toList();
    }

    public PartyProfileDto assignProfile(UUID partyId, PartyProfileRequest req) {
        IdentityParty party = parties.findById(partyId).orElseThrow(() -> notFound("Party", partyId));
        if (req.profileId() == null) {
            throw new IllegalArgumentException("profileId required");
        }
        IdentityProfile profile = profiles.findById(req.profileId())
                .orElseThrow(() -> notFound("Profile", req.profileId()));
        if (!party.getTenantId().equals(profile.getTenantId())) {
            throw new IllegalArgumentException("Profile and party must share the same tenant");
        }
        if (!profile.isActive()) {
            throw new IllegalArgumentException("Profile " + profile.getCode() + " is inactive");
        }
        Optional<IdentityPartyProfile> existing = partyProfiles.findByPartyIdAndProfileId(partyId, profile.getId());
        IdentityPartyProfile a;
        if (existing.isPresent()) {
            a = existing.get();
            a.setActive(true);
            a.setDateDebut(req.dateDebut() != null ? req.dateDebut() : a.getDateDebut());
            a.setDateFin(req.dateFin());
        } else {
            a = IdentityPartyProfile.builder()
                    .partyId(partyId)
                    .profileId(profile.getId())
                    .dateDebut(req.dateDebut() != null ? req.dateDebut() : LocalDate.now())
                    .dateFin(req.dateFin())
                    .active(true)
                    .build();
        }
        return toPartyProfileDto(partyProfiles.save(a), profile);
    }

    public void revokeProfile(UUID partyId, UUID profileId) {
        parties.findById(partyId).orElseThrow(() -> notFound("Party", partyId));
        IdentityPartyProfile a = partyProfiles.findByPartyIdAndProfileId(partyId, profileId)
                .orElseThrow(() -> new NoSuchElementException("Assignment not found"));
        a.setActive(false);
        if (a.getDateFin() == null) {
            a.setDateFin(LocalDate.now());
        }
        partyProfiles.save(a);
    }

    /**
     * Offre d'une Party à une date : union des services, et pour les grilles comme pour les limites
     * l'affectation la plus récente l'emporte (produit+canal, ou produit+sens).
     */
    @Transactional(readOnly = true)
    public OffreDto offreParty(UUID partyId, LocalDate date) {
        IdentityParty party = parties.findById(partyId).orElseThrow(() -> notFound("Party", partyId));
        LocalDate d = date != null ? date : LocalDate.now();
        List<UUID> profileIds = partyProfiles.findByPartyIdAndActiveTrueOrderByDateDebutDesc(partyId).stream()
                .filter(a -> a.couvre(d))
                .map(IdentityPartyProfile::getProfileId)
                .toList();
        return resoudreOffre("PARTY", party.getId(), party.getCode(), d, profileIds);
    }

    @Transactional(readOnly = true)
    public OffreDto offreUser(UUID userId, LocalDate date) {
        IdentityChannelUser u = users.findById(userId).orElseThrow(() -> notFound("User", userId));
        return offreParty(u.getPartyId(), date);
    }

    // ——— Profils d'une unité du réseau ———

    @Transactional(readOnly = true)
    public List<UniteProfileDto> listUniteProfiles(UUID uniteId) {
        Map<UUID, IdentityProfile> byId = profiles.findByIdIn(
                uniteProfiles.findByUniteIdOrderByDateDebutDesc(uniteId).stream()
                        .map(IdentityUniteProfile::getProfileId).toList()
        ).stream().collect(Collectors.toMap(IdentityProfile::getId, x -> x, (a, b) -> a));
        return uniteProfiles.findByUniteIdOrderByDateDebutDesc(uniteId).stream()
                .map(a -> toUniteProfileDto(a, byId.get(a.getProfileId())))
                .toList();
    }

    /**
     * Affecte un profil à une unité. Pas de contrôle d'existence de l'unité (BC séparé) ;
     * le tenant du profil doit correspondre au tenant demandé (défaut CORIS_DEMO).
     */
    public UniteProfileDto assignUniteProfile(UUID uniteId, UniteProfileRequest req) {
        if (uniteId == null) {
            throw new IllegalArgumentException("uniteId required");
        }
        if (req.profileId() == null) {
            throw new IllegalArgumentException("profileId required");
        }
        IdentityProfile profile = profiles.findById(req.profileId())
                .orElseThrow(() -> notFound("Profile", req.profileId()));
        String t = tenant(req.tenantId());
        if (!t.equals(profile.getTenantId())) {
            throw new IllegalArgumentException("Profile tenant does not match");
        }
        if (!profile.isActive()) {
            throw new IllegalArgumentException("Profile " + profile.getCode() + " is inactive");
        }
        Optional<IdentityUniteProfile> existing = uniteProfiles.findByUniteIdAndProfileId(uniteId, profile.getId());
        IdentityUniteProfile a;
        if (existing.isPresent()) {
            a = existing.get();
            a.setActive(true);
            a.setDateDebut(req.dateDebut() != null ? req.dateDebut() : a.getDateDebut());
            a.setDateFin(req.dateFin());
        } else {
            a = IdentityUniteProfile.builder()
                    .uniteId(uniteId)
                    .profileId(profile.getId())
                    .dateDebut(req.dateDebut() != null ? req.dateDebut() : LocalDate.now())
                    .dateFin(req.dateFin())
                    .active(true)
                    .build();
        }
        return toUniteProfileDto(uniteProfiles.save(a), profile);
    }

    public void revokeUniteProfile(UUID uniteId, UUID profileId) {
        IdentityUniteProfile a = uniteProfiles.findByUniteIdAndProfileId(uniteId, profileId)
                .orElseThrow(() -> new NoSuchElementException("Assignment not found"));
        a.setActive(false);
        if (a.getDateFin() == null) {
            a.setDateFin(LocalDate.now());
        }
        uniteProfiles.save(a);
    }

    /** Offre résolue d'une unité du réseau (mêmes règles d'union que pour une Party). */
    @Transactional(readOnly = true)
    public OffreDto offreUnite(UUID uniteId, LocalDate date) {
        LocalDate d = date != null ? date : LocalDate.now();
        List<UUID> profileIds = uniteProfiles.findByUniteIdAndActiveTrueOrderByDateDebutDesc(uniteId).stream()
                .filter(a -> a.couvre(d))
                .map(IdentityUniteProfile::getProfileId)
                .toList();
        return resoudreOffre("UNITE", uniteId, null, d, profileIds);
    }

    /**
     * Construit l'offre à partir d'une liste ordonnée de profils (les premiers l'emportent sur les grilles et les limites).
     */
    private OffreDto resoudreOffre(
            String ownerKind, UUID ownerId, String ownerCode, LocalDate d, List<UUID> profileIds
    ) {
        Map<UUID, IdentityProfile> byId = profiles.findByIdIn(profileIds).stream()
                .filter(IdentityProfile::isActive)
                .collect(Collectors.toMap(IdentityProfile::getId, x -> x, (a, b) -> a));
        List<ProfileDto> profils = profileIds.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(this::toProfileDto)
                .toList();

        List<UUID> serviceIds = profils.stream()
                .filter(p -> p.kind() == null || p.kind() == ProfileKind.SERVICE)
                .map(ProfileDto::id)
                .toList();
        List<UUID> tariffIds = profils.stream()
                .filter(p -> p.kind() == ProfileKind.TARIFF)
                .map(ProfileDto::id)
                .toList();
        List<UUID> limitIds = profils.stream()
                .filter(p -> p.kind() == ProfileKind.LIMIT)
                .map(ProfileDto::id)
                .toList();

        Map<String, ProfileServiceDto> services = new LinkedHashMap<>();
        if (!serviceIds.isEmpty()) {
            for (IdentityProfileService s : profileServices.findByProfileIdInAndActiveTrue(serviceIds)) {
                services.putIfAbsent(s.getServiceCode().toUpperCase(Locale.ROOT), ProfileServiceDto.from(s));
            }
        }

        Map<String, ProfileGridDto> grilles = new LinkedHashMap<>();
        if (!tariffIds.isEmpty()) {
            Map<UUID, Integer> ordre = new HashMap<>();
            for (int i = 0; i < profileIds.size(); i++) {
                ordre.putIfAbsent(profileIds.get(i), i);
            }
            List<IdentityProfileGrid> toutes = new ArrayList<>(profileGrids.findByProfileIdIn(tariffIds));
            toutes.sort(Comparator.comparingInt(g -> ordre.getOrDefault(g.getProfileId(), Integer.MAX_VALUE)));
            for (IdentityProfileGrid g : toutes) {
                String cle = g.getProduitCode().toUpperCase(Locale.ROOT) + "|"
                        + (g.getCanalCode() == null ? "" : g.getCanalCode().toUpperCase(Locale.ROOT));
                grilles.putIfAbsent(cle, ProfileGridDto.from(g));
            }
        }

        Map<String, ProfileLimitDto> limites = new LinkedHashMap<>();
        if (!limitIds.isEmpty()) {
            Map<UUID, Integer> ordre = new HashMap<>();
            for (int i = 0; i < profileIds.size(); i++) {
                ordre.putIfAbsent(profileIds.get(i), i);
            }
            List<IdentityProfileLimit> toutes = new ArrayList<>(profileLimits.findByProfileIdIn(limitIds));
            toutes.sort(Comparator.comparingInt(l -> ordre.getOrDefault(l.getProfileId(), Integer.MAX_VALUE)));
            for (IdentityProfileLimit l : toutes) {
                String cle = l.getProduitCode().toUpperCase(Locale.ROOT) + "|" + l.getSens().name();
                limites.putIfAbsent(cle, ProfileLimitDto.from(l));
            }
        }

        return new OffreDto(ownerKind, ownerId, ownerCode, d, profils,
                List.copyOf(services.values()), List.copyOf(grilles.values()), List.copyOf(limites.values()));
    }

    private void remplacerContenu(
            UUID profileId,
            List<ProfileServiceDto> services,
            List<ProfileGridDto> grilles,
            List<ProfileLimitDto> limites
    ) {
        profileServices.deleteByProfileId(profileId);
        profileGrids.deleteByProfileId(profileId);
        profileLimits.deleteByProfileId(profileId);
        if (services != null) {
            for (ProfileServiceDto s : services) {
                require(s.serviceCode(), "serviceCode");
                profileServices.save(IdentityProfileService.builder()
                        .profileId(profileId)
                        .serviceCode(s.serviceCode().trim().toUpperCase(Locale.ROOT))
                        .libelle(blankToNull(s.libelle()))
                        .active(s.estActif())
                        .build());
            }
        }
        if (grilles != null) {
            for (ProfileGridDto g : grilles) {
                require(g.produitCode(), "produitCode");
                require(g.grilleCode(), "grilleCode");
                profileGrids.save(IdentityProfileGrid.builder()
                        .profileId(profileId)
                        .produitCode(g.produitCode().trim().toUpperCase(Locale.ROOT))
                        .grilleCode(g.grilleCode().trim().toUpperCase(Locale.ROOT))
                        .canalCode(g.canalCode() == null || g.canalCode().isBlank()
                                ? "" : g.canalCode().trim().toUpperCase(Locale.ROOT))
                        .devise(blankToNull(g.devise()) != null
                                ? g.devise().trim().toUpperCase(Locale.ROOT) : null)
                        .build());
            }
        }
        if (limites != null) {
            Set<String> vues = new HashSet<>();
            for (ProfileLimitDto l : limites) {
                require(l.produitCode(), "produitCode");
                require(l.groupeCode(), "groupeCode");
                if (l.sens() == null) {
                    throw new IllegalArgumentException("Le sens de la limite est requis");
                }
                String produit = l.produitCode().trim().toUpperCase(Locale.ROOT);
                String cle = produit + "|" + l.sens().name();
                if (!vues.add(cle)) {
                    throw new IllegalArgumentException("Une limite est déjà définie pour " + produit + " / " + l.sens());
                }
                profileLimits.save(IdentityProfileLimit.builder()
                        .profileId(profileId)
                        .produitCode(produit)
                        .sens(l.sens())
                        .groupeCode(l.groupeCode().trim().toUpperCase(Locale.ROOT))
                        .build());
            }
        }
    }

    private static void verifierContenu(
            ProfileKind kind,
            List<ProfileServiceDto> services,
            List<ProfileGridDto> grilles,
            List<ProfileLimitDto> limites
    ) {
        if (kind != ProfileKind.SERVICE && nonVide(services)) {
            throw new IllegalArgumentException("Un profil " + kind + " ne contient pas de services (utiliser un profil SERVICE)");
        }
        if (kind != ProfileKind.TARIFF && nonVide(grilles)) {
            throw new IllegalArgumentException("Un profil " + kind + " ne contient pas de grilles (utiliser un profil TARIFF)");
        }
        if (kind != ProfileKind.LIMIT && nonVide(limites)) {
            throw new IllegalArgumentException("Un profil " + kind + " ne contient pas de limites (utiliser un profil LIMIT)");
        }
    }

    private static boolean nonVide(List<?> list) {
        return list != null && !list.isEmpty();
    }

    private static <T> List<T> ouVide(List<T> list) {
        return list != null ? list : List.of();
    }

    private ProfileDto toProfileDto(IdentityProfile p) {
        List<ProfileServiceDto> services = profileServices.findByProfileIdOrderByServiceCodeAsc(p.getId()).stream()
                .map(ProfileServiceDto::from)
                .toList();
        List<ProfileGridDto> grilles = profileGrids.findByProfileIdOrderByProduitCodeAsc(p.getId()).stream()
                .map(ProfileGridDto::from)
                .toList();
        List<ProfileLimitDto> limites = profileLimits.findByProfileIdOrderByProduitCodeAsc(p.getId()).stream()
                .map(ProfileLimitDto::from)
                .toList();
        return ProfileDto.from(p, services, grilles, limites);
    }

    private static PartyProfileDto toPartyProfileDto(IdentityPartyProfile a, IdentityProfile p) {
        return new PartyProfileDto(
                a.getId(), a.getPartyId(), a.getProfileId(),
                p != null ? p.getCode() : null,
                p != null ? p.getLibelle() : null,
                p != null && p.getKind() != null ? p.getKind() : ProfileKind.SERVICE,
                a.getDateDebut(), a.getDateFin(), a.isActive());
    }

    private static UniteProfileDto toUniteProfileDto(IdentityUniteProfile a, IdentityProfile p) {
        return new UniteProfileDto(
                a.getId(), a.getUniteId(), a.getProfileId(),
                p != null ? p.getCode() : null,
                p != null ? p.getLibelle() : null,
                p != null && p.getKind() != null ? p.getKind() : ProfileKind.SERVICE,
                a.getDateDebut(), a.getDateFin(), a.isActive());
    }

    // ——— helpers ———

    private static Specification<IdentityParty> specificationParty(FiltreParty f) {
        List<Specification<IdentityParty>> specs = new ArrayList<>();
        if (f.tenantId() != null && !f.tenantId().isBlank()) {
            String t = f.tenantId().trim();
            specs.add((root, q, cb) -> cb.equal(root.get("tenantId"), t));
        }
        if (f.kind() != null) {
            specs.add((root, q, cb) -> cb.equal(root.get("kind"), f.kind()));
        }
        if (f.active() != null) {
            specs.add((root, q, cb) -> cb.equal(root.get("active"), f.active()));
        }
        if (f.texte() != null && !f.texte().isBlank()) {
            String motif = "%" + f.texte().trim().toLowerCase(Locale.ROOT) + "%";
            specs.add((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("code")), motif),
                    cb.like(cb.lower(root.get("displayName")), motif),
                    cb.like(cb.lower(cb.coalesce(root.get("phone"), "")), motif),
                    cb.like(cb.lower(cb.coalesce(root.get("email"), "")), motif),
                    cb.like(cb.lower(cb.coalesce(root.get("externalRef"), "")), motif)));
        }
        return Specification.allOf(specs);
    }

    private OrgNodeDto buildTree(IdentityOrgNode node, Map<UUID, List<IdentityOrgNode>> byParent) {
        List<OrgNodeDto> kids = byParent.getOrDefault(node.getId(), List.of()).stream()
                .map(c -> buildTree(c, byParent))
                .toList();
        return OrgNodeDto.from(node, kids);
    }

    private static String tenant(String tenantId) {
        return tenantId == null || tenantId.isBlank() ? DEFAULT_TENANT : tenantId.trim();
    }

    private static void require(String v, String field) {
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException(field + " required");
        }
    }

    private static String blankToNull(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }

    private static NoSuchElementException notFound(String type, UUID id) {
        return new NoSuchElementException(type + " not found: " + id);
    }
}
