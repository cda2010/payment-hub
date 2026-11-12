package com.gateway.payment.fidelite.service;

import com.gateway.payment.fidelite.domain.Evenement;
import com.gateway.payment.fidelite.domain.Parrainage;
import com.gateway.payment.fidelite.domain.ParrainageStatut;
import com.gateway.payment.fidelite.domain.Programme;
import com.gateway.payment.fidelite.domain.ProgrammeStatut;
import com.gateway.payment.fidelite.repository.InboundEventRepository;
import com.gateway.payment.fidelite.repository.ParrainageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferralQualificationServiceTest {

    @Mock ParrainageRepository parrainages;
    @Mock InboundEventRepository inbox;
    @Mock LedgerService ledger;

    ReferralQualificationService service;

    UUID programmeId;
    UUID parrain;
    UUID filleul;
    Programme programme;
    Parrainage row;

    @BeforeEach
    void setUp() {
        service = new ReferralQualificationService(parrainages, inbox, ledger);
        programmeId = UUID.randomUUID();
        parrain = UUID.randomUUID();
        filleul = UUID.randomUUID();
        programme = new Programme();
        programme.setId(programmeId);
        programme.setTenantId("CORIS_DEMO");
        programme.setStatut(ProgrammeStatut.ACTIF);
        programme.setPointsParrain(200);
        programme.setPointsFilleul(80);
        row = new Parrainage();
        row.setProgramme(programme);
        row.setParrainPartyId(parrain);
        row.setFilleulPartyId(filleul);
        row.setCode("PAR-TEST");
        row.setStatut(ParrainageStatut.ASSOCIE);
        row.setUsedAt(Instant.now());
        when(parrainages.findByFilleulPartyIdAndStatut(filleul, ParrainageStatut.ASSOCIE))
                .thenAnswer(invocation -> row.getStatut() == ParrainageStatut.ASSOCIE ? List.of(row) : List.of());
    }

    @Test
    void paymentAloneRemembersTheTransaction() {
        service.considere("CORIS_DEMO", payment(new BigDecimal("10000"), "XOF", "CASH_IN"));

        assertThat(row.getStatut()).isEqualTo(ParrainageStatut.ASSOCIE);
        assertThat(row.getEligibleEventId()).isNotNull();
        verify(ledger, never()).crediter(any(), any(), any(), anyLong(), any(), any());
    }

    @Test
    void kycThenPaymentCreditsBothOnce() {
        service.considere("CORIS_DEMO", kyc());
        when(parrainages.countByProgramme_IdAndParrainPartyIdAndStatutAndQualifiedAtGreaterThanEqual(
                eq(programmeId), eq(parrain), eq(ParrainageStatut.QUALIFIE), any()))
                .thenReturn(0L);

        service.considere("CORIS_DEMO", payment(new BigDecimal("5000"), "XOF", "MERCHANT_PAYMENT"));

        assertThat(row.getStatut()).isEqualTo(ParrainageStatut.QUALIFIE);
        verify(ledger).crediter(
                "CORIS_DEMO", programmeId, parrain, 200L, "Parrainage", "PARRAINAGE:PAR-TEST:PARRAIN");
        verify(ledger).crediter(
                "CORIS_DEMO", programmeId, filleul, 80L, "Bienvenue parrainage", "PARRAINAGE:PAR-TEST:FILLEUL");

        service.considere("CORIS_DEMO", payment(new BigDecimal("20000"), "XOF", "MERCHANT_PAYMENT"));
        verify(ledger, org.mockito.Mockito.times(1)).crediter(
                eq("CORIS_DEMO"), eq(programmeId), eq(parrain), eq(200L), any(), any());
    }

    @Test
    void amountBelowTheMinimumDoesNotCount() {
        service.considere("CORIS_DEMO", payment(new BigDecimal("4999"), "XOF", "MERCHANT_PAYMENT"));

        assertThat(row.getEligibleEventId()).isNull();
        verify(ledger, never()).crediter(any(), any(), any(), anyLong(), any(), any());
    }

    @Test
    void listedProductMustMatch() {
        programme.setReferralProducts("MERCHANT_PAYMENT,BILL_PAYMENT");
        row.setKycApproved(true);

        service.considere("CORIS_DEMO", payment(new BigDecimal("10000"), "XOF", "CASH_IN"));
        assertThat(row.getEligibleEventId()).isNull();

        when(parrainages.countByProgramme_IdAndParrainPartyIdAndStatutAndQualifiedAtGreaterThanEqual(
                any(), any(), any(), any())).thenReturn(0L);
        service.considere("CORIS_DEMO", payment(new BigDecimal("10000"), "XOF", "BILL_PAYMENT"));

        assertThat(row.getStatut()).isEqualTo(ParrainageStatut.QUALIFIE);
    }

    @Test
    void lateReferralExpiresWithoutPoints() {
        row.setUsedAt(Instant.now().minus(31, ChronoUnit.DAYS));

        service.considere("CORIS_DEMO", payment(new BigDecimal("10000"), "XOF", "CASH_IN"));

        assertThat(row.getStatut()).isEqualTo(ParrainageStatut.EXPIRE);
        verify(ledger, never()).crediter(any(), any(), any(), anyLong(), any(), any());
    }

    @Test
    void monthlyCapRejectsTheReward() {
        row.setKycApproved(true);
        when(parrainages.countByProgramme_IdAndParrainPartyIdAndStatutAndQualifiedAtGreaterThanEqual(
                any(), any(), any(), any())).thenReturn(20L);

        service.considere("CORIS_DEMO", payment(new BigDecimal("10000"), "XOF", "CASH_IN"));

        assertThat(row.getStatut()).isEqualTo(ParrainageStatut.REJETE);
        assertThat(row.getRejectionReason()).isEqualTo("PLAFOND");
        verify(ledger, never()).crediter(any(), any(), any(), anyLong(), any(), any());
    }

    @Test
    void paymentIsEnoughWhenKycIsNotRequired() {
        programme.setReferralKycRequired(false);
        when(parrainages.countByProgramme_IdAndParrainPartyIdAndStatutAndQualifiedAtGreaterThanEqual(
                any(), any(), any(), any())).thenReturn(0L);

        service.considere("CORIS_DEMO", payment(new BigDecimal("10000"), "XOF", "CASH_IN"));

        assertThat(row.getStatut()).isEqualTo(ParrainageStatut.QUALIFIE);
        assertThat(row.isKycApproved()).isFalse();
    }

    private Evenement payment(BigDecimal amount, String currency, String product) {
        return new Evenement(
                UUID.randomUUID(),
                ReferralQualificationService.PAYMENT_COMPLETED,
                filleul.toString(),
                amount,
                currency,
                Map.of("productCode", product, "serviceType", product));
    }

    private Evenement kyc() {
        return new Evenement(
                UUID.randomUUID(),
                ReferralQualificationService.KYC_APPROVED,
                filleul.toString(),
                null,
                null,
                Map.of("kycLevel", "FULL"));
    }
}
