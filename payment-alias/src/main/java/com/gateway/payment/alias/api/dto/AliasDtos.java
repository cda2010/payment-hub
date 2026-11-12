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
            boolean active
    ) {
        public static AliasDto from(AccountAlias a) {
            return new AliasDto(
                    a.getId(), a.getTenantId(), a.getAliasCode(), a.getLabel(),
                    a.getAliasType(), a.getExternalRef(), a.getCurrency(),
                    a.getOwnerKind(), a.getOwnerId(), a.getProviderCode(), a.isActive());
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
            String providerCode
    ) {}

    public record AliasResolveDto(
            String aliasCode,
            String label,
            AliasType aliasType,
            String externalRef,
            String currency,
            String providerCode,
            AliasOwnerKind ownerKind,
            UUID ownerId
    ) {
        public static AliasResolveDto from(AccountAlias a) {
            return new AliasResolveDto(
                    a.getAliasCode(), a.getLabel(), a.getAliasType(),
                    a.getExternalRef(), a.getCurrency(), a.getProviderCode(),
                    a.getOwnerKind(), a.getOwnerId());
        }
    }
}
