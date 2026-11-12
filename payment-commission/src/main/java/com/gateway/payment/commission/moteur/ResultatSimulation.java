package com.gateway.payment.commission.moteur;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ResultatSimulation(
        UUID grilleId,
        String grilleCode,
        int grilleVersion,
        BigDecimal fees,
        BigDecimal totalCommissions,
        BigDecimal reliquat,
        String envelopeMode,
        List<PartCommission> parts,
        List<String> warnings
) {}
