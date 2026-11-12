package com.gateway.payment.rules.source;

import java.math.BigDecimal;

/** Vue lecture des compteurs (confirmés + réservés) pour une clé de limite. */
public record CumulSnapshot(
        BigDecimal amountSum,
        int countSum,
        BigDecimal reservedAmount,
        int reservedCount
) {
    public static CumulSnapshot empty() {
        return new CumulSnapshot(BigDecimal.ZERO, 0, BigDecimal.ZERO, 0);
    }

    public BigDecimal amountWithReservation() {
        return amountSum.add(reservedAmount);
    }

    public int countWithReservation() {
        return countSum + reservedCount;
    }
}
