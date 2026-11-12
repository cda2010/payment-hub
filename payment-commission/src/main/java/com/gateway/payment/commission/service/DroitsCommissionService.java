package com.gateway.payment.commission.service;

import com.gateway.payment.commission.domain.DroitCommission;
import com.gateway.payment.commission.domain.LiquidationCommission;
import com.gateway.payment.commission.moteur.DemandeSimulation;
import com.gateway.payment.commission.moteur.MoteurCommission;
import com.gateway.payment.commission.moteur.PartCommission;
import com.gateway.payment.commission.moteur.ResultatSimulation;
import com.gateway.payment.commission.repository.DroitCommissionRepository;
import com.gateway.payment.commission.repository.LiquidationCommissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DroitsCommissionService {

    private final MoteurCommission moteur;
    private final DroitCommissionRepository droits;
    private final LiquidationCommissionRepository liquidations;

    public DroitsCommissionService(
            MoteurCommission moteur,
            DroitCommissionRepository droits,
            LiquidationCommissionRepository liquidations
    ) {
        this.moteur = moteur;
        this.droits = droits;
        this.liquidations = liquidations;
    }

    @Transactional
    public List<DroitCommission> accrue(DemandeSimulation demande, String tenantId) {
        if (demande == null || demande.transactionId() == null) {
            return List.of();
        }
        if (droits.existsByTransactionId(demande.transactionId())) {
            return droits.findByTransactionId(demande.transactionId());
        }
        ResultatSimulation sim = moteur.simuler(demande);
        List<DroitCommission> created = new ArrayList<>();
        for (PartCommission part : sim.parts()) {
            created.add(droits.save(DroitCommission.builder()
                    .tenantId(tenantId)
                    .transactionId(demande.transactionId())
                    .grilleId(sim.grilleId())
                    .grilleCode(sim.grilleCode())
                    .grilleVersion(sim.grilleVersion())
                    .ruleId(part.ruleId())
                    .ruleCode(part.ruleCode())
                    .beneficiaryRole(part.beneficiaryRole())
                    .currency(demande.currency() == null ? "XOF" : demande.currency())
                    .amount(part.commissionAmount() == null ? BigDecimal.ZERO : part.commissionAmount())
                    .fundingType(part.fundingType())
                    .funderRole(part.funderRole())
                    .status("ACCRUED")
                    .detail(part.detail())
                    .build()));
        }
        return created;
    }

    @Transactional(readOnly = true)
    public List<DroitCommission> byTransaction(String transactionId) {
        return droits.findByTransactionId(transactionId);
    }

    @Transactional
    public LiquidationCommission liquidate(String beneficiaryRole, String beneficiaryRef,
                                           String currency, String tenantId) {
        String ccy = currency == null ? "XOF" : currency;
        List<DroitCommission> pending = droits.findByStatusAndBeneficiaryRoleAndCurrency(
                "ACCRUED", beneficiaryRole, ccy);
        if (beneficiaryRef != null && !beneficiaryRef.isBlank()) {
            pending = pending.stream()
                    .filter(d -> beneficiaryRef.equals(d.getBeneficiaryRef())
                            || d.getBeneficiaryRef() == null)
                    .toList();
        }
        BigDecimal total = pending.stream()
                .map(DroitCommission::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        LiquidationCommission liq = liquidations.save(LiquidationCommission.builder()
                .tenantId(tenantId)
                .beneficiaryRole(beneficiaryRole)
                .beneficiaryRef(beneficiaryRef)
                .currency(ccy)
                .totalAmount(total)
                .droitCount(pending.size())
                .status("COMPLETED")
                .build());
        Instant now = Instant.now();
        for (DroitCommission d : pending) {
            d.setStatus("LIQUIDATED");
            d.setLiquidationId(liq.getId());
            d.setLiquidatedAt(now);
            if (beneficiaryRef != null && d.getBeneficiaryRef() == null) {
                d.setBeneficiaryRef(beneficiaryRef);
            }
            droits.save(d);
        }
        return liq;
    }

    public record AccrueRequest(
            String transactionId,
            String operation,
            String productCode,
            String channel,
            String currency,
            BigDecimal amount,
            BigDecimal fees,
            String tenantId,
            Map<String, BigDecimal> aggregates
    ) {
        public DemandeSimulation toDemande() {
            return new DemandeSimulation(
                    transactionId, operation, productCode, channel, currency,
                    amount, fees, aggregates == null ? Map.of() : aggregates);
        }
    }
}
