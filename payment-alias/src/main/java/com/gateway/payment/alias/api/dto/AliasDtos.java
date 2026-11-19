package com.gateway.payment.alias.api.dto;

import com.gateway.payment.alias.domain.AccountAlias;
import com.gateway.payment.alias.domain.AliasOwnerKind;
import com.gateway.payment.alias.domain.AliasType;

import java.util.UUID;

public final class AliasDtos {
    private AliasDtos() {}

    public record AliasDto(
            UUID id,
            String tenantId,
            String aliasCode,
            String label,
            AliasType aliasType,
            String externalRef,
            String currency,
            AliasOwnerKind ownerKind,
            UUID ownerId,
            String providerCode,
            String etiquette,
            boolean active,
            boolean parDefaut
    ) {
        public static AliasDto from(AccountAlias a) {
            return new AliasDto(
                    a.getId(), a.getTenantId(), a.getAliasCode(), a.getLabel(),
                    a.getAliasType(), a.getExternalRef(), a.getCurrency(),
                    a.getOwnerKind(), a.getOwnerId(), a.getProviderCode(), a.getEtiquette(), a.isActive(),
                    a.isParDefaut());
        }
    }

    public record AliasRequest(
            String tenantId,
            String aliasCode,
            String label,
            AliasType aliasType,
            String externalRef,
            String currency,
            AliasOwnerKind ownerKind,
            UUID ownerId,
            String providerCode,
            String etiquette
    ) {}

    public record AliasResolveDto(
            String aliasCode,
            String label,
            AliasType aliasType,
            String externalRef,
            String currency,
            String providerCode,
            AliasOwnerKind ownerKind,
            UUID ownerId,
            String etiquette
    ) {
        public static AliasResolveDto from(AccountAlias a) {
            return new AliasResolveDto(
                    a.getAliasCode(), a.getLabel(), a.getAliasType(),
                    a.getExternalRef(), a.getCurrency(), a.getProviderCode(),
                    a.getOwnerKind(), a.getOwnerId(), a.getEtiquette());
        }
    }
}
