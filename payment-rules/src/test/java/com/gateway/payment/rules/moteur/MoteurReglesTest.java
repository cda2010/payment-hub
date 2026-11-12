package com.gateway.payment.rules.moteur;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.rules.domain.*;
import com.gateway.payment.rules.repository.RuleExecutionRepository;
import com.gateway.payment.rules.repository.RuleGroupRepository;
import com.gateway.payment.rules.service.CumulService;
import com.gateway.payment.rules.source.ContextSourceResolver;
import com.gateway.payment.rules.source.CumulSnapshot;
import com.gateway.payment.rules.source.CumulSourceResolver;
import com.gateway.payment.rules.source.SourceRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MoteurReglesTest {

    @Mock RuleGroupRepository groups;
    @Mock CumulService cumuls;
    @Mock RuleExecutionRepository executions;

    MoteurRegles moteur;

    @BeforeEach
    void setUp() {
        SourceRegistry registry = new SourceRegistry(List.of(
                new ContextSourceResolver(),
                new CumulSourceResolver(cumuls)));
        moteur = new MoteurRegles(groups, registry, cumuls, executions, new ObjectMapper());
    }

    @Test
    void plafondJournalier_rejectSiCumul420kPlus100k() {
        RuleGroup group = groupeCashOut();
        when(groups.findDetailedByCode("CASH_OUT_STANDARD")).thenReturn(Optional.of(group));
        when(cumuls.snapshot(eq("CASHOUT_DAILY_LIMIT"), any(), any(), any()))
                .thenReturn(new CumulSnapshot(new BigDecimal("420000"), 3, BigDecimal.ZERO, 0));

        ResultatEvaluation r = moteur.simulate(new DemandeEvaluation(
                "TXN-1", "CASH_OUT", null, "CASH_OUT_STANDARD",
                new BigDecimal("100000"), "XOF", "CLI001", "WALLET001", null, null,
                Map.of("kycLevel", "STANDARD", "customerStatus", "ACTIVE", "walletBalance", 1_000_000)));

        assertThat(r.decision()).isEqualTo(RuleDecision.REJECT);
        assertThat(r.authorized()).isFalse();
        assertThat(r.violations()).anyMatch(v -> "CASHOUT_DAILY_LIMIT".equals(v.ruleCode()));
        assertThat(r.violations().stream()
                .filter(v -> "CASHOUT_DAILY_LIMIT".equals(v.ruleCode()))
                .findFirst().orElseThrow().projectedValue())
                .isEqualByComparingTo("520000");
    }

    @Test
    void bceao_detentionRefuseAuDelaDe4Millions() {
        RuleGroup group = groupeBceao();
        when(groups.findDetailedByCode("CASH_IN_STANDARD")).thenReturn(Optional.of(group));

        ResultatEvaluation r = moteur.simulate(new DemandeEvaluation(
                "TXN-BCEAO-1", "CASH_IN", null, "CASH_IN_STANDARD",
                new BigDecimal("500000"), "XOF", "CLI001", "WALLET001", null, null,
                Map.of("kycLevel", "STANDARD", "walletBalance", 3_600_000)));

        assertThat(r.decision()).isEqualTo(RuleDecision.REJECT);
        assertThat(r.violations()).anyMatch(v -> "BCEAO_EME_SOLDE_MAX".equals(v.ruleCode()));
    }

    @Test
    void bceao_rechargementMensuelRefuseAuDelaDe15Millions() {
        RuleGroup group = groupeBceao();
        when(groups.findDetailedByCode("CASH_IN_STANDARD")).thenReturn(Optional.of(group));
        when(cumuls.snapshot(eq("BCEAO_EME_RECHARGEMENT_MOIS"), any(), any(), any()))
                .thenReturn(new CumulSnapshot(new BigDecimal("14950000"), 4, BigDecimal.ZERO, 0));

        ResultatEvaluation r = moteur.simulate(new DemandeEvaluation(
                "TXN-BCEAO-2", "CASH_IN", null, "CASH_IN_STANDARD",
                new BigDecimal("100000"), "XOF", "CLI001", "WALLET001", null, null,
                Map.of("kycLevel", "FULL", "walletBalance", 100_000)));

        assertThat(r.decision()).isEqualTo(RuleDecision.REJECT);
        assertThat(r.violations()).anyMatch(v -> "BCEAO_EME_RECHARGEMENT_MOIS".equals(v.ruleCode()));
    }

    @Test
    void bceao_transfertRecuRefuseSiDetentionDepassee() {
        RuleGroup group = groupeDetention();
        when(groups.findDetailedByCode("ACCOUNT_TRANSFER_CREDIT")).thenReturn(Optional.of(group));

        ResultatEvaluation r = moteur.simulate(new DemandeEvaluation(
                "TXN-BCEAO-REC-1", "ACCOUNT_TRANSFER", null, "ACCOUNT_TRANSFER_CREDIT",
                new BigDecimal("500000"), "XOF", "CLI-BENEF", "WALLET-B", null, null,
                Map.of("kycLevel", "STANDARD", "walletBalance", 3_700_000, "sourceCustomerId", "CLI-PAYER")));

        assertThat(r.decision()).isEqualTo(RuleDecision.REJECT);
        assertThat(r.violations()).anyMatch(v -> "BCEAO_EME_SOLDE_MAX".equals(v.ruleCode()));
    }

    @Test
    void bceao_virementInterneMemeClientIgnoreLaDetention() {
        RuleGroup group = groupeDetention();
        when(groups.findDetailedByCode("ACCOUNT_TRANSFER_CREDIT")).thenReturn(Optional.of(group));

        ResultatEvaluation r = moteur.simulate(new DemandeEvaluation(
                "TXN-BCEAO-REC-2", "ACCOUNT_TRANSFER", null, "ACCOUNT_TRANSFER_CREDIT",
                new BigDecimal("5000000"), "XOF", "CLI-A", "WALLET-B", null, null,
                Map.of("kycLevel", "FULL", "walletBalance", 3_000_000, "sourceCustomerId", "CLI-A")));

        assertThat(r.decision()).isEqualTo(RuleDecision.ALLOW);
        assertThat(r.evaluatedRules()).isEmpty();
    }

    @Test
    void bceao_neSappliquePasAuDistributeur() {
        RuleGroup group = groupeBceao();
        when(groups.findDetailedByCode("CASH_IN_STANDARD")).thenReturn(Optional.of(group));

        ResultatEvaluation r = moteur.simulate(new DemandeEvaluation(
                "TXN-BCEAO-3", "CASH_IN", null, "CASH_IN_STANDARD",
                new BigDecimal("20000000"), "XOF", "DIST001", "WALLET-DIST", null, null,
                Map.of("kycLevel", "FULL", "walletBalance", 10_000_000, "subjectRole", "DISTRIBUTEUR")));

        assertThat(r.decision()).isEqualTo(RuleDecision.ALLOW);
        assertThat(r.evaluatedRules()).isEmpty();
    }

    @Test
    void nombreMensuelRefuseALa101eOperation() {
        RuleGroup group = groupeNombre();
        when(groups.findDetailedByCode("CASH_IN_STANDARD")).thenReturn(Optional.of(group));
        when(cumuls.snapshot(eq("NOMBRE_OPERATIONS_MOIS"), any(), any(), any()))
                .thenReturn(new CumulSnapshot(BigDecimal.ZERO, 100, BigDecimal.ZERO, 0));

        ResultatEvaluation r = moteur.simulate(new DemandeEvaluation(
                "TXN-NBR-1", "CASH_IN", null, "CASH_IN_STANDARD",
                new BigDecimal("1000"), "XOF", "CLI001", "WALLET001", null, null,
                Map.of("kycLevel", "STANDARD")));

        assertThat(r.decision()).isEqualTo(RuleDecision.REJECT);
        assertThat(r.violations()).anyMatch(v -> "NOMBRE_OPERATIONS_MOIS".equals(v.ruleCode()));
    }

    @Test
    void allowSiSousPlafond() {
        RuleGroup group = groupeCashOut();
        when(groups.findDetailedByCode("CASH_OUT_STANDARD")).thenReturn(Optional.of(group));
        when(cumuls.snapshot(eq("CASHOUT_DAILY_LIMIT"), any(), any(), any()))
                .thenReturn(new CumulSnapshot(new BigDecimal("100000"), 1, BigDecimal.ZERO, 0));

        ResultatEvaluation r = moteur.simulate(new DemandeEvaluation(
                "TXN-2", "CASH_OUT", null, "CASH_OUT_STANDARD",
                new BigDecimal("50000"), "XOF", "CLI001", "WALLET001", null, null,
                Map.of("kycLevel", "STANDARD", "customerStatus", "ACTIVE", "walletBalance", 500_000)));

        assertThat(r.decision()).isEqualTo(RuleDecision.ALLOW);
        assertThat(r.authorized()).isTrue();
        assertThat(r.violations()).isEmpty();
    }

    private static RuleGroup groupeCashOut() {
        RuleDefinition daily = RuleDefinition.builder()
                .id(UUID.randomUUID()).code("CASHOUT_DAILY_LIMIT").libelle("Daily")
                .category(RuleCategory.LIMIT).status(RuleStatus.PUBLIEE).build();
        RuleVersion dailyV = RuleVersion.builder()
                .id(UUID.randomUUID()).rule(daily).version(1).priority(40)
                .conditionsJson("{\"transactionType\":\"CASH_OUT\",\"kycLevel\":\"STANDARD\"}")
                .sourceType(SourceType.CUMUL_AMOUNT).sourcePeriod(LimitPeriod.DAY)
                .sourceScope(LimitScope.WALLET).includeCurrent(true)
                .operator(RuleOperator.LTE).limitValue(new BigDecimal("500000"))
                .actionOnViolation(RuleDecision.REJECT).message("Plafond journalier dépassé")
                .mandatory(true).effectiveFrom(LocalDate.of(2026, 1, 1))
                .status(RuleStatus.PUBLIEE).build();

        RuleDefinition status = RuleDefinition.builder()
                .id(UUID.randomUUID()).code("CHECK_CUSTOMER_STATUS").libelle("Status")
                .category(RuleCategory.RESTRICTION).status(RuleStatus.PUBLIEE).build();
        RuleVersion statusV = RuleVersion.builder()
                .id(UUID.randomUUID()).rule(status).version(1).priority(10)
                .conditionsJson("{\"transactionType\":\"CASH_OUT\"}")
                .sourceType(SourceType.CUSTOMER_STATUS).includeCurrent(false)
                .operator(RuleOperator.EQ).limitValuesJson("[\"ACTIVE\"]")
                .actionOnViolation(RuleDecision.REJECT).message("Client non actif")
                .mandatory(true).effectiveFrom(LocalDate.of(2026, 1, 1))
                .status(RuleStatus.PUBLIEE).build();

        RuleDefinition kyc = RuleDefinition.builder()
                .id(UUID.randomUUID()).code("CHECK_KYC_LEVEL").libelle("KYC")
                .category(RuleCategory.KYC).status(RuleStatus.PUBLIEE).build();
        RuleVersion kycV = RuleVersion.builder()
                .id(UUID.randomUUID()).rule(kyc).version(1).priority(20)
                .conditionsJson("{\"transactionType\":\"CASH_OUT\"}")
                .sourceType(SourceType.KYC_LEVEL).includeCurrent(false)
                .operator(RuleOperator.IN).limitValuesJson("[\"STANDARD\",\"ENHANCED\",\"FULL\"]")
                .actionOnViolation(RuleDecision.REJECT).message("Niveau KYC insuffisant")
                .mandatory(true).effectiveFrom(LocalDate.of(2026, 1, 1))
                .status(RuleStatus.PUBLIEE).build();

        RuleDefinition unit = RuleDefinition.builder()
                .id(UUID.randomUUID()).code("CHECK_TRANSACTION_LIMIT").libelle("Unit")
                .category(RuleCategory.LIMIT).status(RuleStatus.PUBLIEE).build();
        RuleVersion unitV = RuleVersion.builder()
                .id(UUID.randomUUID()).rule(unit).version(1).priority(30)
                .conditionsJson("{\"transactionType\":\"CASH_OUT\",\"kycLevel\":\"STANDARD\"}")
                .sourceType(SourceType.TRANSACTION_AMOUNT).includeCurrent(true)
                .operator(RuleOperator.LTE).limitValue(new BigDecimal("200000"))
                .actionOnViolation(RuleDecision.REJECT).message("Montant supérieur au plafond unitaire")
                .mandatory(true).effectiveFrom(LocalDate.of(2026, 1, 1))
                .status(RuleStatus.PUBLIEE).build();

        RuleDefinition bal = RuleDefinition.builder()
                .id(UUID.randomUUID()).code("CHECK_WALLET_BALANCE").libelle("Balance")
                .category(RuleCategory.LIMIT).status(RuleStatus.PUBLIEE).build();
        RuleVersion balV = RuleVersion.builder()
                .id(UUID.randomUUID()).rule(bal).version(1).priority(60)
                .conditionsJson("{\"transactionType\":\"CASH_OUT\"}")
                .sourceType(SourceType.PROJECTED_BALANCE).sourceScope(LimitScope.WALLET)
                .includeCurrent(true)
                .operator(RuleOperator.GTE).limitValue(BigDecimal.ZERO)
                .actionOnViolation(RuleDecision.REJECT).message("Solde insuffisant")
                .mandatory(true).effectiveFrom(LocalDate.of(2026, 1, 1))
                .status(RuleStatus.PUBLIEE).build();

        RuleGroup g = RuleGroup.builder()
                .id(UUID.randomUUID()).code("CASH_OUT_STANDARD").libelle("Cash-out")
                .transactionType("CASH_OUT")
                .evaluationStrategy(EvaluationStrategy.STOP_ON_REJECT)
                .status("ACTIVE").build();
        g.getItems().addAll(List.of(
                item(g, statusV, 10),
                item(g, kycV, 20),
                item(g, unitV, 30),
                item(g, dailyV, 40),
                item(g, balV, 60)));
        return g;
    }

    private static RuleGroup groupeDetention() {
        String conditions = """
                {"transactionTypes":["CASH_IN","ACCOUNT_TRANSFER"],"kycLevels":["STANDARD","ENHANCED","FULL"],"skipSameCustomer":true}""";
        RuleDefinition solde = RuleDefinition.builder()
                .id(UUID.randomUUID()).code("BCEAO_EME_SOLDE_MAX").libelle("Détention")
                .category(RuleCategory.LIMIT).status(RuleStatus.PUBLIEE).build();
        RuleVersion soldeV = RuleVersion.builder()
                .id(UUID.randomUUID()).rule(solde).version(1).priority(45)
                .conditionsJson(conditions)
                .sourceType(SourceType.PROJECTED_HOLDING).sourceScope(LimitScope.CUSTOMER)
                .includeCurrent(true)
                .operator(RuleOperator.LTE).limitValue(new BigDecimal("4000000"))
                .actionOnViolation(RuleDecision.REJECT)
                .message("Plafond de détention dépassé")
                .mandatory(true).effectiveFrom(LocalDate.of(2026, 11, 2))
                .status(RuleStatus.PUBLIEE).build();
        RuleGroup g = RuleGroup.builder()
                .id(UUID.randomUUID()).code("ACCOUNT_TRANSFER_CREDIT").libelle("Crédit reçu")
                .transactionType("ACCOUNT_TRANSFER")
                .evaluationStrategy(EvaluationStrategy.STOP_ON_REJECT)
                .status("ACTIVE").build();
        g.getItems().add(item(g, soldeV, 10));
        return g;
    }

    private static RuleGroup groupeBceao() {
        String conditions = """
                {"transactionType":"CASH_IN","kycLevels":["STANDARD","ENHANCED","FULL"],"excludeSubjectRoles":["DISTRIBUTEUR","ACCEPTEUR"]}""";
        RuleDefinition solde = RuleDefinition.builder()
                .id(UUID.randomUUID()).code("BCEAO_EME_SOLDE_MAX").libelle("Détention")
                .category(RuleCategory.LIMIT).status(RuleStatus.PUBLIEE).build();
        RuleVersion soldeV = RuleVersion.builder()
                .id(UUID.randomUUID()).rule(solde).version(1).priority(45)
                .conditionsJson(conditions)
                .sourceType(SourceType.PROJECTED_HOLDING).sourceScope(LimitScope.CUSTOMER)
                .includeCurrent(true)
                .operator(RuleOperator.LTE).limitValue(new BigDecimal("4000000"))
                .actionOnViolation(RuleDecision.REJECT)
                .message("Plafond de détention dépassé")
                .mandatory(true).effectiveFrom(LocalDate.of(2026, 11, 2))
                .status(RuleStatus.PUBLIEE).build();

        RuleDefinition mois = RuleDefinition.builder()
                .id(UUID.randomUUID()).code("BCEAO_EME_RECHARGEMENT_MOIS").libelle("Rechargement")
                .category(RuleCategory.LIMIT).status(RuleStatus.PUBLIEE).build();
        RuleVersion moisV = RuleVersion.builder()
                .id(UUID.randomUUID()).rule(mois).version(1).priority(50)
                .conditionsJson(conditions)
                .sourceType(SourceType.CUMUL_AMOUNT).sourcePeriod(LimitPeriod.MONTH)
                .sourceScope(LimitScope.CUSTOMER).includeCurrent(true)
                .operator(RuleOperator.LTE).limitValue(new BigDecimal("15000000"))
                .actionOnViolation(RuleDecision.REJECT)
                .message("Plafond mensuel de rechargement dépassé")
                .mandatory(true).effectiveFrom(LocalDate.of(2026, 11, 2))
                .status(RuleStatus.PUBLIEE).build();

        RuleGroup g = RuleGroup.builder()
                .id(UUID.randomUUID()).code("CASH_IN_STANDARD").libelle("Cash-in")
                .transactionType("CASH_IN")
                .evaluationStrategy(EvaluationStrategy.STOP_ON_REJECT)
                .status("ACTIVE").build();
        g.getItems().addAll(List.of(item(g, soldeV, 45), item(g, moisV, 50)));
        return g;
    }

    private static RuleGroup groupeNombre() {
        RuleDefinition nombre = RuleDefinition.builder()
                .id(UUID.randomUUID()).code("NOMBRE_OPERATIONS_MOIS").libelle("Nombre")
                .category(RuleCategory.LIMIT).status(RuleStatus.PUBLIEE).build();
        RuleVersion nombreV = RuleVersion.builder()
                .id(UUID.randomUUID()).rule(nombre).version(1).priority(55)
                .conditionsJson("""
                        {"transactionTypes":["CASH_IN","ACCOUNT_TRANSFER"],"kycLevels":["STANDARD","ENHANCED","FULL"]}""")
                .sourceType(SourceType.CUMUL_COUNT).sourcePeriod(LimitPeriod.MONTH)
                .sourceScope(LimitScope.CUSTOMER).includeCurrent(true)
                .operator(RuleOperator.LTE).limitValue(new BigDecimal("100"))
                .actionOnViolation(RuleDecision.REJECT)
                .message("Nombre d'opérations du mois dépassé")
                .mandatory(true).effectiveFrom(LocalDate.of(2026, 11, 2))
                .status(RuleStatus.PUBLIEE).build();
        RuleGroup g = RuleGroup.builder()
                .id(UUID.randomUUID()).code("CASH_IN_STANDARD").libelle("Cash-in")
                .transactionType("CASH_IN")
                .evaluationStrategy(EvaluationStrategy.STOP_ON_REJECT)
                .status("ACTIVE").build();
        g.getItems().add(item(g, nombreV, 55));
        return g;
    }

    private static RuleGroupItem item(RuleGroup g, RuleVersion v, int order) {
        return RuleGroupItem.builder().id(UUID.randomUUID()).group(g).ruleVersion(v).sortOrder(order).build();
    }
}
