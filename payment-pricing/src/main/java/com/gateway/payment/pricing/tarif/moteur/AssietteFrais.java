package com.gateway.payment.pricing.tarif.moteur;

import com.gateway.payment.pricing.tarif.TarifException;
import com.gateway.payment.pricing.tarif.domain.BaseCalculFrais;
import com.gateway.payment.pricing.tarif.domain.GrilleTarifaire;
import com.gateway.payment.pricing.tarif.domain.PeriodeCumul;
import com.gateway.payment.pricing.tarif.domain.ScopeCumul;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Map;

/**
 * Résout le scalaire d'assiette à partir de la grille et du contexte d'appel.
 */
public final class AssietteFrais {

    private AssietteFrais() {
    }

    public record Resolution(BigDecimal montant, BaseCalculFrais baseCalcul, PeriodeCumul period, ScopeCumul scope) {
    }

    public static Resolution resoudre(GrilleTarifaire grille, DemandeFrais demande) {
        BaseCalculFrais base = grille.getBaseCalcul() != null
                ? grille.getBaseCalcul()
                : BaseCalculFrais.TRANSACTION_AMOUNT;
        PeriodeCumul period = grille.getCumulPeriod();
        ScopeCumul scope = grille.getCumulScope();

        // Override explicite (parcours BPMN / CUSTOM)
        if (demande.feeBaseOverride() != null && demande.feeBaseOverride().signum() > 0) {
            return new Resolution(scale(demande.feeBaseOverride()), base, period, scope);
        }

        BigDecimal raw = switch (base) {
            case TRANSACTION_AMOUNT -> demande.montant();
            case CONVERTED_AMOUNT -> {
                BigDecimal c = demande.convertedAmount();
                yield c != null && c.signum() > 0 ? c : demande.montant();
            }
            case CUMUL_AMOUNT -> cumul(demande.aggregates(), "cumulAmount", period, scope, base);
            case CUMUL_COUNT -> cumul(demande.aggregates(), "cumulCount", period, scope, base);
            case CUSTOM -> throw new TarifException(HttpStatus.BAD_REQUEST, "ASSIETTE_CUSTOM_REQUISE",
                    "La grille " + grille.libelleVersion()
                            + " exige feeBaseAmount (baseCalcul=CUSTOM).");
        };

        if (raw == null || raw.signum() < 0) {
            throw new TarifException(HttpStatus.BAD_REQUEST, "ASSIETTE_INVALIDE",
                    "Assiette de frais invalide pour " + grille.libelleVersion() + " (" + base + ").");
        }
        return new Resolution(scale(raw), base, period, scope);
    }

    private static BigDecimal cumul(
            Map<String, BigDecimal> aggregates,
            String metric,
            PeriodeCumul period,
            ScopeCumul scope,
            BaseCalculFrais base
    ) {
        if (period == null || scope == null) {
            throw new TarifException(HttpStatus.UNPROCESSABLE_ENTITY, "ASSIETTE_CUMUL_CONFIG",
                    "baseCalcul=" + base + " exige cumulPeriod et cumulScope sur la grille.");
        }
        String p = period.name();
        String s = scope.name();
        BigDecimal v = first(
                aggregates,
                metric + "." + p + "." + s,
                metric + "." + p,
                metric + "." + s,
                metric,
                // aliases historiques (commission / agent)
                "cumulAmount".equals(metric) ? "agentMonthlyAmountBefore" : null,
                "cumulCount".equals(metric) ? "transactionCount" : null
        );
        if (v == null) {
            throw new TarifException(HttpStatus.UNPROCESSABLE_ENTITY, "ASSIETTE_CUMUL_MANQUANTE",
                    "Agrégat manquant pour " + base + " (" + period + "/" + scope
                            + "). Fournissez aggregates." + metric + ".");
        }
        return v;
    }

    private static BigDecimal first(Map<String, BigDecimal> map, String... keys) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        for (String k : keys) {
            if (k == null || k.isBlank()) {
                continue;
            }
            BigDecimal v = map.get(k);
            if (v == null) {
                // lookup case-insensitive
                for (Map.Entry<String, BigDecimal> e : map.entrySet()) {
                    if (e.getKey() != null && e.getKey().equalsIgnoreCase(k) && e.getValue() != null) {
                        return e.getValue();
                    }
                }
            } else {
                return v;
            }
        }
        return null;
    }

    private static BigDecimal scale(BigDecimal v) {
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    public static boolean estCumul(BaseCalculFrais base) {
        return base == BaseCalculFrais.CUMUL_AMOUNT || base == BaseCalculFrais.CUMUL_COUNT;
    }

    public static void validerConfig(BaseCalculFrais base, PeriodeCumul period, ScopeCumul scope) {
        BaseCalculFrais b = base != null ? base : BaseCalculFrais.TRANSACTION_AMOUNT;
        if (estCumul(b)) {
            if (period == null || scope == null) {
                throw new TarifException(HttpStatus.BAD_REQUEST, "ASSIETTE_CUMUL_CONFIG",
                        "cumulPeriod et cumulScope sont obligatoires lorsque baseCalcul=" + b);
            }
        }
    }

    public static String cleAggregate(String metric, PeriodeCumul period, ScopeCumul scope) {
        return metric.toLowerCase(Locale.ROOT) + "." + period.name() + "." + scope.name();
    }
}
