package com.gateway.payment.rules.source;

import com.gateway.payment.rules.domain.RuleVersion;
import com.gateway.payment.rules.domain.SourceType;
import com.gateway.payment.rules.moteur.DemandeEvaluation;
import com.gateway.payment.rules.service.CumulService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

@Component
@Order(20)
public class CumulSourceResolver implements RuleSourceResolver {

    private static final Set<SourceType> SUPPORTED = EnumSet.of(SourceType.CUMUL_AMOUNT, SourceType.CUMUL_COUNT);

    private final CumulService cumulService;

    public CumulSourceResolver(CumulService cumulService) {
        this.cumulService = cumulService;
    }

    @Override
    public boolean supports(RuleVersion version) {
        return SUPPORTED.contains(version.getSourceType());
    }

    @Override
    public SourceValue resolve(RuleVersion version, DemandeEvaluation d, CumulSnapshot provided) {
        CumulSnapshot snap = provided != null ? provided
                : cumulService.snapshot(version.getRule().getCode(), version.getSourceScope(),
                version.getSourcePeriod(), d);

        BigDecimal amount = d.amount() == null ? BigDecimal.ZERO : d.amount();
        if (version.getSourceType() == SourceType.CUMUL_AMOUNT) {
            BigDecimal current = snap.amountWithReservation();
            BigDecimal projected = version.isIncludeCurrent() ? current.add(amount) : current;
            return SourceValue.cumul(current, projected);
        }
        int current = snap.countWithReservation();
        int projected = version.isIncludeCurrent() ? current + 1 : current;
        return SourceValue.cumul(BigDecimal.valueOf(current), BigDecimal.valueOf(projected));
    }
}
