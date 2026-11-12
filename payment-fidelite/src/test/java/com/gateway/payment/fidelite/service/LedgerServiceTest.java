package com.gateway.payment.fidelite.service;

import com.gateway.payment.fidelite.domain.LoyaltyAccount;
import com.gateway.payment.fidelite.domain.PointMovement;
import com.gateway.payment.fidelite.domain.Programme;
import com.gateway.payment.fidelite.repository.LoyaltyAccountRepository;
import com.gateway.payment.fidelite.repository.PointMovementRepository;
import com.gateway.payment.fidelite.repository.ProgrammeRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LedgerServiceTest {

    @Mock LoyaltyAccountRepository accounts;
    @Mock PointMovementRepository movements;
    @Mock ProgrammeRepository programmes;

    LedgerService ledger;

    @BeforeEach
    void setUp() {
        ledger = new LedgerService(accounts, movements, programmes, null);
    }

    @Test
    void theSameBusinessReferenceCreditsOnlyOnce() {
        when(movements.existsByTenantIdAndBusinessReference("CORIS_DEMO", "PAY-1")).thenReturn(true);
        when(accounts.findByTenantIdAndProgramme_IdAndPartyId(any(), any(), any())).thenReturn(Optional.empty());

        ledger.crediter("CORIS_DEMO", UUID.randomUUID(), UUID.randomUUID(), 50, "Paiement", "PAY-1");

        verify(accounts, never()).credit(any(), any(), anyLong());
        verify(movements, never()).save(any());
    }

    @Test
    void aDebitIsRefusedWhenTheReservationMisses() {
        UUID programmeId = UUID.randomUUID();
        UUID partyId = UUID.randomUUID();
        LoyaltyAccount account = account("CORIS_DEMO");
        when(movements.existsByTenantIdAndBusinessReference("CORIS_DEMO", "ECHANGE:1")).thenReturn(false);
        when(accounts.findByTenantIdAndProgramme_IdAndPartyId("CORIS_DEMO", programmeId, partyId))
                .thenReturn(Optional.of(account));
        when(accounts.reserve(account.getId(), "CORIS_DEMO", 500)).thenReturn(0);

        assertThatThrownBy(() -> ledger.consommer("CORIS_DEMO", programmeId, partyId, 500, "Transfert", "ECHANGE:1"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(movements, never()).save(any());
    }

    @Test
    void aSuccessfulDebitWritesANegativeMovement() {
        UUID programmeId = UUID.randomUUID();
        UUID partyId = UUID.randomUUID();
        LoyaltyAccount account = account("CORIS_DEMO");
        account.setBalance(800);
        when(movements.existsByTenantIdAndBusinessReference("CORIS_DEMO", "ECHANGE:2")).thenReturn(false);
        when(accounts.findByTenantIdAndProgramme_IdAndPartyId("CORIS_DEMO", programmeId, partyId))
                .thenReturn(Optional.of(account));
        when(accounts.reserve(account.getId(), "CORIS_DEMO", 500)).thenReturn(1);
        when(accounts.consume(account.getId(), "CORIS_DEMO", 500)).thenReturn(1);
        when(movements.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ledger.consommer("CORIS_DEMO", programmeId, partyId, 500, "Transfert sans frais", "ECHANGE:2");

        ArgumentCaptor<PointMovement> written = ArgumentCaptor.forClass(PointMovement.class);
        verify(movements).save(written.capture());
        assertThat(written.getValue().getPoints()).isEqualTo(-500);
        assertThat(written.getValue().getTenantId()).isEqualTo("CORIS_DEMO");
    }

    @Test
    void aCreditLooksUpTheAccountInsideItsTenant() {
        UUID programmeId = UUID.randomUUID();
        UUID partyId = UUID.randomUUID();
        when(movements.existsByTenantIdAndBusinessReference("TENANT-B", "PAY-B")).thenReturn(false);
        when(accounts.findByTenantIdAndProgramme_IdAndPartyId("TENANT-B", programmeId, partyId))
                .thenReturn(Optional.empty());
        when(programmes.getReferenceById(programmeId)).thenReturn(new Programme());
        when(accounts.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(movements.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(accounts.credit(any(), eq("TENANT-B"), eq(20L))).thenReturn(1);

        ledger.crediter("TENANT-B", programmeId, partyId, 20, "Paiement", "PAY-B");

        verify(accounts, atLeastOnce()).findByTenantIdAndProgramme_IdAndPartyId("TENANT-B", programmeId, partyId);
        verify(accounts, never()).findByTenantIdAndProgramme_IdAndPartyId(eq("TENANT-A"), any(), any());
        ArgumentCaptor<LoyaltyAccount> opened = ArgumentCaptor.forClass(LoyaltyAccount.class);
        verify(accounts).save(opened.capture());
        assertThat(opened.getValue().getTenantId()).isEqualTo("TENANT-B");
    }

    private static LoyaltyAccount account(String tenant) {
        LoyaltyAccount account = new LoyaltyAccount();
        account.setId(UUID.randomUUID());
        account.setTenantId(tenant);
        account.setBalance(100);
        return account;
    }
}
