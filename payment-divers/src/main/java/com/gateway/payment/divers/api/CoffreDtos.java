package com.gateway.payment.divers.api;

import com.gateway.payment.divers.domain.CoffreGoal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public final class CoffreDtos {

    private CoffreDtos() {
    }

    public record CreateCoffreRequest(
            @NotBlank String name,
            @NotNull @DecimalMin(value = "1") BigDecimal target,
            @NotNull CoffreGoal goal
    ) {
    }

    public record DepositRequest(
            @NotNull @DecimalMin(value = "1") BigDecimal amount
    ) {
    }

    public record LockRequest(@NotNull Boolean locked) {
    }

    public record CoffreView(
            UUID id,
            String name,
            BigDecimal balance,
            BigDecimal target,
            CoffreGoal goal,
            boolean locked,
            Instant createdAt
    ) {
    }
}
