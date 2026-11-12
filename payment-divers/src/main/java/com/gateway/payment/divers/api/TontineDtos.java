package com.gateway.payment.divers.api;

import com.gateway.payment.divers.domain.TontineFrequency;
import com.gateway.payment.divers.domain.TontineStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class TontineDtos {

    private TontineDtos() {
    }

    public record CreateTontineRequest(
            @NotBlank String name,
            @NotNull @DecimalMin(value = "1") BigDecimal contribution,
            @NotNull TontineFrequency frequency
    ) {
    }

    public record JoinTontineRequest(@NotBlank String inviteCode) {
    }

    public record MemberView(
            UUID id,
            String name,
            String ini,
            boolean paid,
            int turn,
            boolean self,
            String role
    ) {
    }

    public record TontineView(
            UUID id,
            String name,
            BigDecimal contribution,
            TontineFrequency frequency,
            int round,
            int totalRounds,
            String inviteCode,
            int nextBeneficiaryIndex,
            boolean myPaid,
            String role,
            TontineStatus status,
            Instant createdAt,
            List<MemberView> members
    ) {
    }
}
