package com.gateway.payment.commission.moteur;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.commission.domain.*;
import com.gateway.payment.commission.formule.CommissionFormulaRegistry;
import com.gateway.payment.commission.formule.predefinies.FixedCommissionFormula;
import com.gateway.payment.commission.formule.predefinies.HybridCommissionFormula;
import com.gateway.payment.commission.formule.predefinies.PercentCommissionFormula;
import com.gateway.payment.commission.repository.GrilleCommissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoteurCommissionTest {

    @Mock GrilleCommissionRepository grilles;

    MoteurCommission moteur;

    @BeforeEach
    void setUp() {
        CommissionFormulaRegistry registry = new CommissionFormulaRegistry(List.of(
                new FixedCommissionFormula(),
                new PercentCommissionFormula(),
                new HybridCommissionFormula()));
        moteur = new MoteurCommission(grilles, registry, new ObjectMapper());
    }

    @Test
    void repartitionFrais1000_casR01() {
        FormuleCommission percent = FormuleCommission.builder()
                .id(UUID.randomUUID()).code("PERCENT").version(1)
                .libelle("Percent").type(TypeFormuleCommission.PERCENT)
                .status(StatutFormuleCommission.ACTIVE).build();

        GrilleCommission grille = GrilleCommission.builder()
                .id(UUID.randomUUID()).code("CASH_OUT-STD").version(1).libelle("Demo")
                .operationType("CASH_OUT").channelCode("AGENT").currency("XOF")
                .status(StatutGrilleCommission.PUBLIEE)
                .validFrom(LocalDate.of(2026, 1, 1))
                .envelopeMode(EnvelopeMode.FULL_FEES)
                .build();

        grille.getRegles().addAll(List.of(
                regle(grille, percent, "EME", "ISSUER", "{\"taux\":0.40}", 10),
                regle(grille, percent, "DIST", "DISTRIBUTOR", "{\"taux\":0.25}", 20),
                regle(grille, percent, "AGENT", "AGENT", "{\"taux\":0.20}", 30),
                regle(grille, percent, "AGG", "AGGREGATOR", "{\"taux\":0.10}", 40),
                regle(grille, percent, "PROC", "PROCESSOR", "{\"taux\":0.05}", 50)
        ));

        when(grilles.findByOperationTypeAndStatusOrderByVersionDesc(eq("CASH_OUT"), any()))
                .thenReturn(List.of(grille));

        ResultatSimulation r = moteur.simuler(new DemandeSimulation(
                "TX-1", "CASH_OUT", "CASH_OUT", "AGENT", "XOF",
                new BigDecimal("20000"), new BigDecimal("1000"), null));

        assertThat(r.parts()).hasSize(5);
        assertThat(r.parts()).extracting(PartCommission::commissionAmount)
                .containsExactly(
                        new BigDecimal("400"),
                        new BigDecimal("250"),
                        new BigDecimal("200"),
                        new BigDecimal("100"),
                        new BigDecimal("50"));
        assertThat(r.totalCommissions()).isEqualByComparingTo("1000");
        assertThat(r.reliquat()).isEqualByComparingTo("0");
    }

    private static RegleCommission regle(
            GrilleCommission g, FormuleCommission f, String code, String role, String params, int prio
    ) {
        return RegleCommission.builder()
                .id(UUID.randomUUID()).grille(g).code(code).libelle(code)
                .beneficiaryRole(role).formule(f)
                .commissionBase(SourceCommission.TRANSACTION_FEES)
                .fundingType(FundingType.TRANSACTION_FEES)
                .paramsJson(params).priority(prio).active(true)
                .build();
    }
}
