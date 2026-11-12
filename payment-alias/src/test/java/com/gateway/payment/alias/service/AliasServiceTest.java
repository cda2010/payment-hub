package com.gateway.payment.alias.service;

import com.gateway.payment.alias.api.dto.AliasDtos.AliasDto;
import com.gateway.payment.alias.api.dto.AliasDtos.AliasRequest;
import com.gateway.payment.alias.domain.AccountAlias;
import com.gateway.payment.alias.domain.AliasOwnerKind;
import com.gateway.payment.alias.domain.AliasType;
import com.gateway.payment.alias.repository.AccountAliasRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AliasServiceTest {

    @Mock AccountAliasRepository repo;
    AliasService service;

    @BeforeEach
    void setUp() {
        service = new AliasService(repo);
    }

    @Test
    void creerAliasPourUniteNetwork() {
        UUID uniteId = UUID.fromString("10000000-0000-0000-0000-000000000021");
        when(repo.existsByTenantIdAndAliasCode("CORIS_DEMO", "ALIAS-MS")).thenReturn(false);
        when(repo.save(any())).thenAnswer(inv -> {
            AccountAlias a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        AliasDto dto = service.create(new AliasRequest(
                null, "alias-ms", "Till Sahel", AliasType.TILL, "TILL-MS", "XOF",
                AliasOwnerKind.UNITE, uniteId, "CORIS_MONEY"));

        assertThat(dto.ownerKind()).isEqualTo(AliasOwnerKind.UNITE);
        assertThat(dto.ownerId()).isEqualTo(uniteId);
        assertThat(dto.aliasCode()).isEqualTo("ALIAS-MS");

        ArgumentCaptor<AccountAlias> cap = ArgumentCaptor.forClass(AccountAlias.class);
        verify(repo).save(cap.capture());
        assertThat(cap.getValue().getOwnerKind()).isEqualTo(AliasOwnerKind.UNITE);
    }

    @Test
    void creerAliasPourPartyIdentity() {
        UUID partyId = UUID.fromString("a1000001-0000-4000-8000-000000000006");
        when(repo.existsByTenantIdAndAliasCode("CORIS_DEMO", "ALIAS-AWA")).thenReturn(false);
        when(repo.save(any())).thenAnswer(inv -> {
            AccountAlias a = inv.getArgument(0);
            a.setId(UUID.randomUUID());
            return a;
        });

        AliasDto dto = service.create(new AliasRequest(
                "CORIS_DEMO", "ALIAS-AWA", "Wallet", AliasType.WALLET, "W-1", "XOF",
                AliasOwnerKind.PARTY, partyId, "CORIS_MONEY"));

        assertThat(dto.ownerKind()).isEqualTo(AliasOwnerKind.PARTY);
        assertThat(dto.ownerId()).isEqualTo(partyId);
    }

    @Test
    void refuseCodeDuplique() {
        when(repo.existsByTenantIdAndAliasCode("CORIS_DEMO", "X")).thenReturn(true);
        assertThatThrownBy(() -> service.create(new AliasRequest(
                null, "X", "L", AliasType.IBAN, "ref", "XOF",
                AliasOwnerKind.ORG, UUID.randomUUID(), null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void resolveExigeActif() {
        when(repo.findByTenantIdAndAliasCode("CORIS_DEMO", "DEAD"))
                .thenReturn(Optional.of(AccountAlias.builder()
                        .aliasCode("DEAD").active(false).build()));
        assertThatThrownBy(() -> service.resolve(null, "DEAD"))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }
}
