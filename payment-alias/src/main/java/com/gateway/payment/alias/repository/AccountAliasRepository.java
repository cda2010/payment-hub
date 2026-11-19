package com.gateway.payment.alias.repository;

import com.gateway.payment.alias.domain.AccountAlias;
import com.gateway.payment.alias.domain.AliasOwnerKind;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountAliasRepository extends JpaRepository<AccountAlias, UUID> {

    List<AccountAlias> findByTenantIdAndActiveTrueOrderByLabelAsc(String tenantId);

    List<AccountAlias> findByTenantIdAndOwnerKindAndOwnerIdAndActiveTrue(
            String tenantId, AliasOwnerKind ownerKind, UUID ownerId);

    Optional<AccountAlias> findByTenantIdAndAliasCode(String tenantId, String aliasCode);

    List<AccountAlias> findByTenantIdAndOwnerKindAndOwnerIdAndParDefautTrueAndActiveTrue(
            String tenantId, AliasOwnerKind ownerKind, UUID ownerId);

    Optional<AccountAlias> findByTenantIdAndOwnerKindAndOwnerIdAndEtiquetteAndActiveTrue(
            String tenantId, AliasOwnerKind ownerKind, UUID ownerId, String etiquette);

    boolean existsByTenantIdAndAliasCode(String tenantId, String aliasCode);
}
