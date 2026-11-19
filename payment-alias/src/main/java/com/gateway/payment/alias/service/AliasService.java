package com.gateway.payment.alias.service;

import com.gateway.payment.alias.api.dto.AliasDtos.*;
import com.gateway.payment.alias.domain.AccountAlias;
import com.gateway.payment.alias.domain.AliasOwnerKind;
import com.gateway.payment.alias.domain.AliasType;
import com.gateway.payment.alias.repository.AccountAliasRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class AliasService {

    public static final String DEFAULT_TENANT = "CORIS_DEMO";

    private final AccountAliasRepository aliases;

    public AliasService(AccountAliasRepository aliases) {
        this.aliases = aliases;
    }

    @Transactional(readOnly = true)
    public List<AliasDto> list(String tenantId, AliasOwnerKind ownerKind, UUID ownerId, String etiquette) {
        String t = tenant(tenantId);
        String role = normaliserEtiquette(etiquette);
        if (ownerKind != null && ownerId != null) {
            return aliases.findByTenantIdAndOwnerKindAndOwnerIdAndActiveTrue(t, ownerKind, ownerId).stream()
                    .filter(a -> role == null || role.equals(a.getEtiquette()))
                    .map(AliasDto::from)
                    .toList();
        }
        if (role != null) {
            throw new IllegalArgumentException("ownerKind and ownerId required to filter by etiquette");
        }
        return aliases.findByTenantIdAndActiveTrueOrderByLabelAsc(t).stream()
                .map(AliasDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public AliasDto get(UUID id) {
        return AliasDto.from(aliases.findById(id).orElseThrow(() -> notFound(id)));
    }

    @Transactional(readOnly = true)
    public AliasResolveDto resolve(String tenantId, String aliasCode) {
        AccountAlias a = aliases.findByTenantIdAndAliasCode(tenant(tenantId), aliasCode)
                .filter(AccountAlias::isActive)
                .orElseThrow(() -> new NoSuchElementException("Alias not found: " + aliasCode));
        return AliasResolveDto.from(a);
    }

    public AliasDto create(AliasRequest req) {
        String tenant = tenant(req.tenantId());
        require(req.aliasCode(), "aliasCode");
        require(req.externalRef(), "externalRef");
        if (req.ownerKind() == null || req.ownerId() == null) {
            throw new IllegalArgumentException("ownerKind and ownerId required");
        }
        String code = req.aliasCode().trim().toUpperCase(Locale.ROOT);
        if (aliases.existsByTenantIdAndAliasCode(tenant, code)) {
            throw new IllegalArgumentException("Alias already exists: " + code);
        }
        AccountAlias a = AccountAlias.builder()
                .tenantId(tenant)
                .aliasCode(code)
                .label(req.label() != null && !req.label().isBlank() ? req.label().trim() : code)
                .aliasType(req.aliasType() != null ? req.aliasType() : AliasType.INTERNAL)
                .externalRef(req.externalRef().trim())
                .currency(req.currency() != null && !req.currency().isBlank()
                        ? req.currency().trim().toUpperCase(Locale.ROOT) : "XOF")
                .ownerKind(req.ownerKind())
                .ownerId(req.ownerId())
                .providerCode(blankToNull(req.providerCode()))
                .etiquette(normaliserEtiquette(req.etiquette()))
                .active(true)
                .build();
        occuperEtiquette(tenant, a.getOwnerKind(), a.getOwnerId(), a.getEtiquette(), null);
        return AliasDto.from(aliases.save(a));
    }

    public AliasDto update(UUID id, AliasRequest req) {
        AccountAlias a = aliases.findById(id).orElseThrow(() -> notFound(id));
        if (req.label() != null && !req.label().isBlank()) {
            a.setLabel(req.label().trim());
        }
        if (req.externalRef() != null && !req.externalRef().isBlank()) {
            a.setExternalRef(req.externalRef().trim());
        }
        if (req.aliasType() != null) {
            a.setAliasType(req.aliasType());
        }
        if (req.currency() != null && !req.currency().isBlank()) {
            a.setCurrency(req.currency().trim().toUpperCase(Locale.ROOT));
        }
        if (req.providerCode() != null) {
            a.setProviderCode(blankToNull(req.providerCode()));
        }
        if (req.ownerKind() != null) {
            a.setOwnerKind(req.ownerKind());
        }
        if (req.ownerId() != null) {
            a.setOwnerId(req.ownerId());
        }
        if (req.etiquette() != null) {
            a.setEtiquette(normaliserEtiquette(req.etiquette()));
        }
        occuperEtiquette(a.getTenantId(), a.getOwnerKind(), a.getOwnerId(), a.getEtiquette(), a.getId());
        return AliasDto.from(aliases.save(a));
    }

    public void deactivate(UUID id) {
        AccountAlias a = aliases.findById(id).orElseThrow(() -> notFound(id));
        a.setActive(false);
        a.setParDefaut(false);
        aliases.save(a);
    }

    /** Marque cet alias comme compte par défaut du propriétaire et retire le défaut précédent. */
    public AliasDto definirDefaut(UUID id, UUID partyId, boolean backoffice) {
        AccountAlias choisi = aliases.findById(id).orElseThrow(() -> notFound(id));
        if (!choisi.isActive()) {
            throw new IllegalArgumentException("Alias inactive");
        }
        if (!backoffice) {
            if (choisi.getOwnerKind() != AliasOwnerKind.PARTY || partyId == null
                    || !partyId.equals(choisi.getOwnerId())) {
                throw new AccesRefuse("Ce compte n'appartient pas au client connecté.");
            }
        }
        for (AccountAlias autre : aliases.findByTenantIdAndOwnerKindAndOwnerIdAndParDefautTrueAndActiveTrue(
                choisi.getTenantId(), choisi.getOwnerKind(), choisi.getOwnerId())) {
            if (!autre.getId().equals(choisi.getId())) {
                autre.setParDefaut(false);
                aliases.save(autre);
            }
        }
        aliases.flush();
        choisi.setParDefaut(true);
        return AliasDto.from(aliases.save(choisi));
    }

    /** Le client connecté n'est pas le propriétaire de l'alias. */
    public static class AccesRefuse extends RuntimeException {
        public AccesRefuse(String message) {
            super(message);
        }
    }

    private static String tenant(String tenantId) {
        return tenantId == null || tenantId.isBlank() ? DEFAULT_TENANT : tenantId.trim();
    }

    private static void require(String v, String field) {
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException(field + " required");
        }
    }

    private void occuperEtiquette(
            String tenant, AliasOwnerKind kind, UUID ownerId, String etiquette, UUID saufId
    ) {
        if (etiquette == null || kind == null || ownerId == null) {
            return;
        }
        aliases.findByTenantIdAndOwnerKindAndOwnerIdAndEtiquetteAndActiveTrue(tenant, kind, ownerId, etiquette)
                .filter(existant -> saufId == null || !saufId.equals(existant.getId()))
                .ifPresent(existant -> {
                    throw new IllegalArgumentException("Etiquette already used: " + etiquette);
                });
    }

    private static String normaliserEtiquette(String etiquette) {
        if (etiquette == null || etiquette.isBlank()) {
            return null;
        }
        return etiquette.trim().toUpperCase(Locale.ROOT);
    }

    private static String blankToNull(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }

    private static NoSuchElementException notFound(UUID id) {
        return new NoSuchElementException("Alias not found: " + id);
    }
}
