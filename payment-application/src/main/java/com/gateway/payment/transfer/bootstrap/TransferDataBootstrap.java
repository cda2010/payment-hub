package com.gateway.payment.transfer.bootstrap;

import com.gateway.payment.configuration.config.GatewayBootstrapProperties;
import com.gateway.payment.transfer.domain.*;
import com.gateway.payment.transfer.repository.FeeRuleRepository;
import com.gateway.payment.transfer.repository.TransferFeePolicyRepository;
import com.gateway.payment.transfer.repository.TransferRailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@Order(25)
@ConditionalOnProperty(prefix = "gateway.bootstrap", name = "providers", havingValue = "true")
public class TransferDataBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TransferDataBootstrap.class);

    private static final String STAR_EXTRA_FIELDS = """
            [{"name":"bankCode","label":"Code banque","type":"text","required":true,"placeholder":"BF001"},
             {"name":"purpose","label":"Motif","type":"text","required":false,"placeholder":"Paiement"}]
            """;
    private static final String RTGS_EXTRA_FIELDS = """
            [{"name":"priority","label":"Priorité","type":"select","required":true,"options":["NORMAL","URGENT"]},
             {"name":"purpose","label":"Motif","type":"text","required":true,"placeholder":"Virement RTGS"}]
            """;
    private static final String SWIFT_EXTRA_FIELDS = """
            [{"name":"bic","label":"BIC/SWIFT","type":"text","required":true,"placeholder":"CORIBFBF"},
             {"name":"iban","label":"IBAN","type":"text","required":true,"placeholder":"BF…"},
             {"name":"purpose","label":"Motif","type":"text","required":true,"placeholder":"Transfer"}]
            """;

    private final FeeRuleRepository feeRuleRepository;
    private final TransferFeePolicyRepository feePolicyRepository;
    private final TransferRailRepository railRepository;
    private final GatewayBootstrapProperties bootstrapProperties;

    public TransferDataBootstrap(
            FeeRuleRepository feeRuleRepository,
            TransferFeePolicyRepository feePolicyRepository,
            TransferRailRepository railRepository,
            GatewayBootstrapProperties bootstrapProperties
    ) {
        this.feeRuleRepository = feeRuleRepository;
        this.feePolicyRepository = feePolicyRepository;
        this.railRepository = railRepository;
        this.bootstrapProperties = bootstrapProperties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!bootstrapProperties.providers()) {
            return;
        }

        upsertRail("INTRA_GROUP", "Intra-groupe", "Transfert interne même groupe (ex. Coris Money ↔ Coris Bank)",
                RailScope.INTERNAL, null, null, false, false, true, false, "[]", 0);
        upsertRail("LOCAL", "Local", "Transfert local même pays",
                RailScope.INTERNAL, null, null, false, true, false, false, "[]", 1);
        upsertRail("STAR_UEMOA", "STAR UEMOA", "Compensation interbancaire UEMOA",
                RailScope.EXTERNAL, null, null, true, false, false, false, STAR_EXTRA_FIELDS, 2);
        upsertRail("RTGS", "RTGS", "Virement temps réel gros montants",
                RailScope.EXTERNAL, new BigDecimal("5000000"), null, true, false, false, false, RTGS_EXTRA_FIELDS, 3);
        upsertRail("SWIFT", "SWIFT", "Transfert international cross-border",
                RailScope.EXTERNAL, null, null, true, false, false, true, SWIFT_EXTRA_FIELDS, 4);

        FeeRule same = upsertFee("FEE_SAME_MM", "Frais same Mobile Money",
                List.of(
                        line(FeeType.FIXED, "0", "10000", "100", 0),
                        line(FeeType.PERCENT, "10001", "100000", "1", 1),
                        line(FeeType.FIXED, "100001", null, "1500", 2)
                ));
        FeeRule cross = upsertFee("FEE_CROSS_MM", "Frais cross Mobile Money",
                List.of(
                        line(FeeType.FIXED, "0", "10000", "200", 0),
                        line(FeeType.PERCENT, "10001", "100000", "1.5", 1),
                        line(FeeType.FIXED, "100001", null, "2500", 2)
                ));
        FeeRule bank = upsertFee("FEE_BANK", "Frais banque",
                List.of(line(FeeType.FIXED, "0", null, "500", 0)));
        FeeRule feeLocal = upsertFee("FEE_LOCAL", "Frais rail LOCAL",
                List.of(line(FeeType.FIXED, "0", "50000", "100", 0),
                        line(FeeType.PERCENT, "50001", null, "0.8", 1)));
        FeeRule feeIntra = upsertFee("FEE_INTRA", "Frais rail INTRA_GROUP",
                List.of(line(FeeType.FIXED, "0", null, "50", 0)));
        FeeRule feeStar = upsertFee("FEE_STAR", "Frais rail STAR_UEMOA",
                List.of(line(FeeType.FIXED, "0", null, "750", 0)));
        FeeRule feeRtgs = upsertFee("FEE_RTGS", "Frais rail RTGS",
                List.of(line(FeeType.FIXED, "0", null, "2500", 0)));
        FeeRule feeSwift = upsertFee("FEE_SWIFT", "Frais rail SWIFT",
                List.of(line(FeeType.FIXED, "0", null, "15000", 0)));

        upsertPolicy("POL_SAME", "Same fournisseur", FeePolicyType.SAME_PROVIDER, null, null, null, same, 1);
        upsertPolicy("POL_CROSS", "Inter-fournisseurs", FeePolicyType.CROSS_PROVIDER, null, null, null, cross, 2);
        upsertPolicy("POL_BANK_CORIS", "Banque → Coris", FeePolicyType.PAIR_OVERRIDE,
                "BANK_TRANSFER", "CORIS_MONEY", null, bank, 0);

        upsertPolicy("POL_LOCAL", "Frais LOCAL", FeePolicyType.CROSS_PROVIDER, null, null, "LOCAL", feeLocal, 10);
        upsertPolicy("POL_INTRA", "Frais INTRA_GROUP", FeePolicyType.CROSS_PROVIDER, null, null, "INTRA_GROUP", feeIntra, 11);
        upsertPolicy("POL_STAR", "Frais STAR_UEMOA", FeePolicyType.CROSS_PROVIDER, null, null, "STAR_UEMOA", feeStar, 12);
        upsertPolicy("POL_RTGS", "Frais RTGS", FeePolicyType.CROSS_PROVIDER, null, null, "RTGS", feeRtgs, 13);
        upsertPolicy("POL_SWIFT", "Frais SWIFT", FeePolicyType.CROSS_PROVIDER, null, null, "SWIFT", feeSwift, 14);

        log.info("Rails et politiques de frais de démonstration initialisés");
    }

    private TransferRail upsertRail(
            String code, String name, String description, RailScope scope,
            BigDecimal minAmount, BigDecimal maxAmount,
            boolean requiresBank, boolean sameCountryOnly, boolean sameGroupOnly, boolean crossBorderOnly,
            String extraFieldsJson, int ordre
    ) {
        return railRepository.findByCodeIgnoreCase(code).map(existing -> {
            existing.setName(name);
            existing.setDescription(description);
            existing.setRailScope(scope);
            existing.setMinAmount(minAmount);
            existing.setMaxAmount(maxAmount);
            existing.setRequiresBank(requiresBank);
            existing.setSameCountryOnly(sameCountryOnly);
            existing.setSameGroupOnly(sameGroupOnly);
            existing.setCrossBorderOnly(crossBorderOnly);
            existing.setExtraFieldsJson(extraFieldsJson);
            existing.setActif(true);
            existing.setOrdre(ordre);
            return railRepository.save(existing);
        }).orElseGet(() -> railRepository.save(TransferRail.builder()
                .code(code)
                .name(name)
                .description(description)
                .railScope(scope)
                .currency("XOF")
                .minAmount(minAmount)
                .maxAmount(maxAmount)
                .requiresBank(requiresBank)
                .sameCountryOnly(sameCountryOnly)
                .sameGroupOnly(sameGroupOnly)
                .crossBorderOnly(crossBorderOnly)
                .extraFieldsJson(extraFieldsJson)
                .actif(true)
                .ordre(ordre)
                .build()));
    }

    private void upsertPolicy(
            String code, String name, FeePolicyType type,
            String emitterCode, String beneficiaryCode, String railCode, FeeRule fee, int ordre
    ) {
        if (feePolicyRepository.existsByCodeIgnoreCase(code)) {
            return;
        }
        feePolicyRepository.save(TransferFeePolicy.builder()
                .code(code)
                .name(name)
                .policyType(type)
                .countryCode("BF")
                .currency("XOF")
                .emitterCode(emitterCode)
                .beneficiaryCode(beneficiaryCode)
                .railCode(railCode)
                .feeRule(fee)
                .actif(true)
                .ordre(ordre)
                .build());
    }

    private FeeRule upsertFee(String code, String name, List<FeeRuleLine> lines) {
        return feeRuleRepository.findByCodeIgnoreCase(code).orElseGet(() -> {
            FeeRule rule = FeeRule.builder()
                    .code(code)
                    .name(name)
                    .currency("XOF")
                    .actif(true)
                    .build();
            rule.replaceLines(lines);
            return feeRuleRepository.save(rule);
        });
    }

    private FeeRuleLine line(FeeType type, String min, String max, String value, int ordre) {
        return FeeRuleLine.builder()
                .feeType(type)
                .minAmount(min == null ? null : new BigDecimal(min))
                .maxAmount(max == null ? null : new BigDecimal(max))
                .value(new BigDecimal(value))
                .ordre(ordre)
                .build();
    }
}
