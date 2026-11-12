package com.gateway.payment.commission.moteur;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.commission.domain.*;
import com.gateway.payment.commission.formule.CommissionFormula;
import com.gateway.payment.commission.formule.CommissionFormulaRegistry;
import com.gateway.payment.commission.formule.ResultatFormule;
import com.gateway.payment.commission.repository.GrilleCommissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * Ordre CDC : éligibilité → source → formule → bornes → arrondi → reliquat.
 * Lot 1 : simulation sur grille PUBLIEE (ou grille forcée), formules FIXED/PERCENT/HYBRID.
 */
@Service
public class MoteurCommission {

    private final GrilleCommissionRepository grilles;
    private final CommissionFormulaRegistry registry;
    private final ObjectMapper objectMapper;

    public MoteurCommission(
            GrilleCommissionRepository grilles,
            CommissionFormulaRegistry registry,
            ObjectMapper objectMapper
    ) {
        this.grilles = grilles;
        this.registry = registry;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public ResultatSimulation simuler(DemandeSimulation demande) {
        require(demande.operation(), "operation");
        BigDecimal fees = nvl(demande.fees());
        BigDecimal amount = nvl(demande.amount());

        GrilleCommission grille = selectionnerGrille(demande)
                .orElseThrow(() -> new NoSuchElementException(
                        "Aucune grille PUBLIEE pour opération " + demande.operation()));

        List<String> warnings = new ArrayList<>();
        List<PartCommission> parts = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        List<RegleCommission> regles = grille.getRegles().stream()
                .filter(RegleCommission::isActive)
                .sorted(Comparator.comparingInt(RegleCommission::getPriority))
                .toList();

        for (RegleCommission regle : regles) {
            BigDecimal base = resoudreBase(regle.getCommissionBase(), amount, fees, demande.aggregates());
            Map<String, BigDecimal> params = lireParams(regle.getParamsJson());
            CommissionFormula impl = registry.trouver(regle.getFormule().getCode(), regle.getFormule().getVersion())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Formule non implémentée : " + regle.getFormule().getCode()
                                    + " v" + regle.getFormule().getVersion()));
            ResultatFormule brut = impl.calculer(base, params);
            BigDecimal commission = appliquerBornes(brut.commission(), regle.getFloorAmount(), regle.getCapAmount());
            commission = commission.setScale(0, RoundingMode.HALF_UP);

            if (regle.getFundingType() == FundingType.SUBSIDY && (regle.getFunderRole() == null || regle.getFunderRole().isBlank())) {
                warnings.add("Règle " + regle.getCode() + " : SUBSIDY sans funderRole");
            }

            parts.add(new PartCommission(
                    regle.getId(),
                    regle.getCode(),
                    regle.getBeneficiaryRole(),
                    regle.getFormule().getCode(),
                    regle.getCommissionBase().name(),
                    base,
                    commission,
                    regle.getFundingType().name(),
                    regle.getFunderRole(),
                    brut.detail()));
            total = total.add(commission);
        }

        BigDecimal reliquat = BigDecimal.ZERO;
        if (grille.getEnvelopeMode() == EnvelopeMode.FULL_FEES
                || grille.getEnvelopeMode() == EnvelopeMode.PARTIAL_FEES) {
            reliquat = fees.subtract(total);
            if (reliquat.compareTo(BigDecimal.ZERO) < 0) {
                warnings.add("Total commissions (" + total + ") > frais (" + fees + ") sans financeur externe");
            }
        }

        return new ResultatSimulation(
                grille.getId(),
                grille.getCode(),
                grille.getVersion(),
                fees,
                total,
                reliquat,
                grille.getEnvelopeMode().name(),
                parts,
                warnings);
    }

    private Optional<GrilleCommission> selectionnerGrille(DemandeSimulation d) {
        List<GrilleCommission> candidates = grilles.findByOperationTypeAndStatusOrderByVersionDesc(
                d.operation(), StatutGrilleCommission.PUBLIEE);
        LocalDate today = LocalDate.now();
        return candidates.stream()
                .filter(g -> g.estApplicable(today))
                .filter(g -> d.channel() == null || d.channel().isBlank()
                        || g.getChannelCode() == null || g.getChannelCode().isBlank()
                        || g.getChannelCode().equalsIgnoreCase(d.channel()))
                .filter(g -> d.productCode() == null || d.productCode().isBlank()
                        || g.getProductCode() == null || g.getProductCode().isBlank()
                        || g.getProductCode().equalsIgnoreCase(d.productCode()))
                .filter(g -> d.currency() == null || d.currency().isBlank()
                        || g.getCurrency().equalsIgnoreCase(d.currency()))
                .findFirst();
    }

    private BigDecimal resoudreBase(
            SourceCommission source, BigDecimal amount, BigDecimal fees, Map<String, BigDecimal> aggregates
    ) {
        return switch (source) {
            case TRANSACTION_AMOUNT -> amount;
            case TRANSACTION_FEES -> fees;
            case CUMUL_AMOUNT -> getAggregate(aggregates, "cumulAmount", "agentMonthlyAmountBefore");
            case CUMUL_FEES -> getAggregate(aggregates, "cumulFees");
            case TRANSACTION_COUNT -> getAggregate(aggregates, "transactionCount");
            case NET_REVENUE -> getAggregate(aggregates, "netRevenue");
        };
    }

    private static BigDecimal getAggregate(Map<String, BigDecimal> aggregates, String... keys) {
        if (aggregates == null) {
            throw new IllegalArgumentException("Agrégat requis pour cette source (simulation uniquement)");
        }
        for (String k : keys) {
            if (aggregates.containsKey(k) && aggregates.get(k) != null) {
                return aggregates.get(k);
            }
        }
        throw new IllegalArgumentException("Agrégat manquant : " + Arrays.toString(keys));
    }

    private Map<String, BigDecimal> lireParams(String json) {
        try {
            Map<String, Object> raw = objectMapper.readValue(json == null || json.isBlank() ? "{}" : json,
                    new TypeReference<>() {});
            Map<String, BigDecimal> out = new HashMap<>();
            for (Map.Entry<String, Object> e : raw.entrySet()) {
                if (e.getValue() != null) {
                    out.put(e.getKey(), new BigDecimal(e.getValue().toString()));
                }
            }
            return out;
        } catch (Exception e) {
            throw new IllegalArgumentException("params_json invalide : " + e.getMessage());
        }
    }

    private static BigDecimal appliquerBornes(BigDecimal value, BigDecimal floor, BigDecimal cap) {
        BigDecimal v = value;
        if (floor != null && v.compareTo(floor) < 0) {
            v = floor;
        }
        if (cap != null && v.compareTo(cap) > 0) {
            v = cap;
        }
        return v;
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static void require(String v, String field) {
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException(field + " required");
        }
    }
}
