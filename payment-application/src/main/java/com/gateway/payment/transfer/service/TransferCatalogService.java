package com.gateway.payment.transfer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.transfer.api.dto.*;
import com.gateway.payment.transfer.domain.*;
import com.gateway.payment.transfer.repository.FeeRuleRepository;
import com.gateway.payment.transfer.repository.TransferFeePolicyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class TransferCatalogService {

    private final TransferFeePolicyRepository feePolicyRepository;
    private final FeeRuleRepository feeRuleRepository;
    private final TransferRailService railService;
    private final ObjectMapper objectMapper;

    public TransferCatalogService(
            TransferFeePolicyRepository feePolicyRepository,
            FeeRuleRepository feeRuleRepository,
            TransferRailService railService,
            ObjectMapper objectMapper
    ) {
        this.feePolicyRepository = feePolicyRepository;
        this.feeRuleRepository = feeRuleRepository;
        this.railService = railService;
        this.objectMapper = objectMapper;
    }

    public record FeeQuote(
            BigDecimal principal,
            BigDecimal feeAmount,
            BigDecimal totalDebit,
            BigDecimal amountReceived,
            FeeBearer feeBearer,
            String ruleCode,
            String breakdown
    ) {}

    @Transactional(readOnly = true)
    public List<TransferParticipantResponse> listParticipants(boolean activeOnly) {
        return List.of();
    }

    @Transactional(readOnly = true)
    public TransferParticipantResponse getParticipant(UUID id) {
        throw removedParticipants();
    }

    @Transactional
    public TransferParticipantResponse createParticipant(TransferParticipantRequest request) {
        throw removedParticipants();
    }

    @Transactional
    public TransferParticipantResponse updateParticipant(UUID id, TransferParticipantRequest request) {
        throw removedParticipants();
    }

    @Transactional(readOnly = true)
    public FeeQuote computeFeeQuoteForProviders(
            String emitterCode, String beneficiaryCode, String countryCode, String railCode, BigDecimal amount
    ) {
        FeeRule rule = resolveFeeRule(emitterCode, beneficiaryCode, countryCode, railCode);
        return computeFee(rule, amount);
    }

    @Transactional(readOnly = true)
    public List<TransferRailResponse> listRails(boolean activeOnly) {
        return railService.listAll(activeOnly);
    }

    @Transactional(readOnly = true)
    public TransferRailResponse getRail(UUID id) {
        return railService.get(id);
    }

    @Transactional(readOnly = true)
    public List<TransferRailResponse> listEligibleRails(
            String emitterProviderCode, String beneficiaryProviderCode, BigDecimal amount
    ) {
        return railService.listEligible(emitterProviderCode, beneficiaryProviderCode, amount);
    }

    @Transactional(readOnly = true)
    public List<TransferModeOptionResponse> estimateModes(TransferEstimateRequest request) {
        String emitter = normalize(request.emitterProviderCode());
        String beneficiary = normalize(request.beneficiaryProviderCode());
        validateAmount(request.amount());

        List<TransferRail> rails = railService.findEligible(
                emitter, beneficiary, request.amount());

        String country = request.countryCode() == null || request.countryCode().isBlank()
                ? "BF"
                : request.countryCode();

        List<TransferModeOptionResponse> options = new ArrayList<>();
        boolean first = true;
        for (TransferRail rail : rails) {
            FeeQuote quote = computeFeeQuoteForProviders(
                    emitter,
                    beneficiary,
                    country,
                    rail.getCode(),
                    request.amount());
            TransferRailService.RailUxMeta ux = railService.uxMeta(rail);
            options.add(new TransferModeOptionResponse(
                    rail.getCode(),
                    rail.getName(),
                    ux.displayLabel(),
                    ux.etaLabel(),
                    ux.warning(),
                    rail.getDescription(),
                    rail.getRailScope(),
                    first,
                    quote.principal(),
                    quote.feeAmount(),
                    quote.totalDebit(),
                    quote.amountReceived(),
                    quote.feeBearer(),
                    quote.breakdown(),
                    rail.getCurrency() != null ? rail.getCurrency() : "XOF"
            ));
            first = false;
        }
        return options;
    }

    @Transactional
    public TransferRailResponse createRail(TransferRailRequest request) {
        return railService.create(request);
    }

    @Transactional
    public TransferRailResponse updateRail(UUID id, TransferRailRequest request) {
        return railService.update(id, request);
    }

    @Transactional(readOnly = true)
    public void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("AMOUNT_REQUIRED", "Montant invalide");
        }
    }

    /**
     * Priorité : PAIR_OVERRIDE+rail → PAIR_OVERRIDE+null rail → SAME/CROSS+rail → SAME/CROSS+null rail → null.
     */
    @Transactional(readOnly = true)
    public FeeRule resolveFeeRule(String emitterCode, String beneficiaryCode, String countryCode, String railCode) {
        String country = countryCode == null || countryCode.isBlank() ? "BF" : countryCode;
        String rail = railCode == null || railCode.isBlank() ? null : railCode.trim();
        List<TransferFeePolicy> policies = feePolicyRepository.findActiveByCountry(country);
        boolean same = emitterCode.equalsIgnoreCase(beneficiaryCode);

        FeeRule pairWithRail = findPairOverride(policies, emitterCode, beneficiaryCode, rail, true);
        if (pairWithRail != null) {
            return pairWithRail;
        }
        FeeRule pairFallback = findPairOverride(policies, emitterCode, beneficiaryCode, rail, false);
        if (pairFallback != null) {
            return pairFallback;
        }

        FeePolicyType expected = same ? FeePolicyType.SAME_PROVIDER : FeePolicyType.CROSS_PROVIDER;
        FeeRule typedWithRail = findByTypeAndRail(policies, expected, rail, true);
        if (typedWithRail != null) {
            return typedWithRail;
        }
        return findByTypeAndRail(policies, expected, rail, false);
    }

    private FeeRule findPairOverride(
            List<TransferFeePolicy> policies,
            String emitterCode,
            String beneficiaryCode,
            String railCode,
            boolean requireRailMatch
    ) {
        for (TransferFeePolicy p : policies) {
            if (p.getPolicyType() != FeePolicyType.PAIR_OVERRIDE) {
                continue;
            }
            if (!emitterCode.equalsIgnoreCase(p.getEmitterCode())
                    || !beneficiaryCode.equalsIgnoreCase(p.getBeneficiaryCode())) {
                continue;
            }
            if (matchesRailPreference(p.getRailCode(), railCode, requireRailMatch)) {
                return p.getFeeRule();
            }
        }
        return null;
    }

    private FeeRule findByTypeAndRail(
            List<TransferFeePolicy> policies,
            FeePolicyType expected,
            String railCode,
            boolean requireRailMatch
    ) {
        for (TransferFeePolicy p : policies) {
            if (p.getPolicyType() != expected) {
                continue;
            }
            if (matchesRailPreference(p.getRailCode(), railCode, requireRailMatch)) {
                return p.getFeeRule();
            }
        }
        return null;
    }

    /** requireRailMatch=true → policy.railCode equals request; false → policy.railCode is null (fallback). */
    private boolean matchesRailPreference(String policyRail, String requestRail, boolean requireRailMatch) {
        if (requireRailMatch) {
            return requestRail != null && policyRail != null && requestRail.equalsIgnoreCase(policyRail);
        }
        return policyRail == null || policyRail.isBlank();
    }

    @Transactional(readOnly = true)
    public List<TransferFeePolicyResponse> listFeePolicies() {
        return feePolicyRepository.findAllByOrderByOrdreAscCodeAsc().stream()
                .map(this::toFeePolicyResponse)
                .toList();
    }

    @Transactional
    public TransferFeePolicyResponse createFeePolicy(TransferFeePolicyRequest request) {
        String code = normalize(request.code());
        if (feePolicyRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("FEE_POLICY_EXISTS", "Politique déjà existante");
        }
        TransferFeePolicy policy = mapFeePolicy(new TransferFeePolicy(), request, code);
        return toFeePolicyResponse(feePolicyRepository.save(policy));
    }

    @Transactional
    public TransferFeePolicyResponse updateFeePolicy(UUID id, TransferFeePolicyRequest request) {
        TransferFeePolicy policy = feePolicyRepository.findById(id)
                .orElseThrow(() -> new BusinessException("FEE_POLICY_NOT_FOUND", "Politique introuvable", HttpStatus.NOT_FOUND));
        String code = normalize(request.code());
        if (!policy.getCode().equalsIgnoreCase(code) && feePolicyRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("FEE_POLICY_EXISTS", "Politique déjà existante");
        }
        mapFeePolicy(policy, request, code);
        return toFeePolicyResponse(feePolicyRepository.save(policy));
    }

    public List<TransferFieldDto> readFields(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception ex) {
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public List<FeeRuleResponse> listFeeRules() {
        return feeRuleRepository.findAllByOrderByCodeAsc().stream().map(this::toFeeRuleResponse).toList();
    }

    @Transactional
    public FeeRuleResponse createFeeRule(FeeRuleRequest request) {
        String code = normalize(request.code());
        if (feeRuleRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("FEE_RULE_EXISTS", "Grille déjà existante");
        }
        FeeRule rule = FeeRule.builder()
                .code(code)
                .name(request.name().trim())
                .currency(request.currency() != null ? request.currency() : "XOF")
                .description(request.description())
                .actif(Boolean.TRUE.equals(request.actif()))
                .feeBearer(request.feeBearer() != null ? request.feeBearer() : FeeBearer.SENDER)
                .build();
        rule.replaceLines(mapLines(request.lines()));
        return toFeeRuleResponse(feeRuleRepository.save(rule));
    }

    @Transactional
    public FeeRuleResponse updateFeeRule(UUID id, FeeRuleRequest request) {
        FeeRule rule = feeRuleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("FEE_RULE_NOT_FOUND", "Grille introuvable", HttpStatus.NOT_FOUND));
        String code = normalize(request.code());
        if (!rule.getCode().equalsIgnoreCase(code) && feeRuleRepository.existsByCodeIgnoreCase(code)) {
            throw new BusinessException("FEE_RULE_EXISTS", "Grille déjà existante");
        }
        rule.setCode(code);
        rule.setName(request.name().trim());
        if (request.currency() != null) {
            rule.setCurrency(request.currency());
        }
        rule.setDescription(request.description());
        rule.setActif(Boolean.TRUE.equals(request.actif()));
        if (request.feeBearer() != null) {
            rule.setFeeBearer(request.feeBearer());
        }
        rule.replaceLines(mapLines(request.lines()));
        return toFeeRuleResponse(feeRuleRepository.save(rule));
    }

    private TransferFeePolicy mapFeePolicy(TransferFeePolicy policy, TransferFeePolicyRequest request, String code) {
        if (request.policyType() == FeePolicyType.PAIR_OVERRIDE
                && (request.emitterCode() == null || request.beneficiaryCode() == null)) {
            throw new BusinessException("PAIR_REQUIRED", "Override de paire nécessite émetteur et bénéficiaire");
        }
        FeeRule rule = feeRuleRepository.findById(request.feeRuleId())
                .orElseThrow(() -> new BusinessException("FEE_RULE_NOT_FOUND", "Grille introuvable", HttpStatus.NOT_FOUND));
        policy.setCode(code);
        policy.setName(request.name().trim());
        policy.setPolicyType(request.policyType());
        policy.setCountryCode(request.countryCode() != null ? request.countryCode().trim().toUpperCase(Locale.ROOT) : "BF");
        policy.setCurrency(request.currency() != null ? request.currency().trim().toUpperCase(Locale.ROOT) : "XOF");
        policy.setEmitterCode(request.emitterCode() != null ? request.emitterCode().trim().toUpperCase(Locale.ROOT) : null);
        policy.setBeneficiaryCode(request.beneficiaryCode() != null ? request.beneficiaryCode().trim().toUpperCase(Locale.ROOT) : null);
        policy.setRailCode(request.railCode() != null && !request.railCode().isBlank()
                ? request.railCode().trim().toUpperCase(Locale.ROOT) : null);
        policy.setFeeRule(rule);
        policy.setActif(Boolean.TRUE.equals(request.actif()));
        policy.setOrdre(request.ordre() != null ? request.ordre() : 0);
        return policy;
    }

    private List<FeeRuleLine> mapLines(List<FeeRuleRequest.FeeRuleLineRequest> lines) {
        List<FeeRuleLine> result = new ArrayList<>();
        if (lines == null) {
            return result;
        }
        int i = 0;
        for (FeeRuleRequest.FeeRuleLineRequest line : lines) {
            BigDecimal fixed = line.fixedAmount();
            BigDecimal percent = line.percentValue();
            if (fixed == null && percent == null) {
                if (line.feeType() == FeeType.PERCENT) {
                    percent = line.value();
                } else {
                    fixed = line.value();
                }
            }
            result.add(FeeRuleLine.builder()
                    .feeType(line.feeType())
                    .minAmount(line.minAmount())
                    .maxAmount(line.maxAmount())
                    .value(line.value())
                    .fixedAmount(fixed)
                    .percentValue(percent)
                    .minFee(line.minFee())
                    .maxFee(line.maxFee())
                    .ordre(line.ordre() != null ? line.ordre() : i)
                    .build());
            i++;
        }
        return result;
    }

    private FeeQuote computeFee(FeeRule rule, BigDecimal amount) {
        FeeBearer bearer = rule != null && rule.getFeeBearer() != null ? rule.getFeeBearer() : FeeBearer.SENDER;
        if (rule == null || !rule.isActif() || rule.getLines() == null || rule.getLines().isEmpty()) {
            return amountsForBearer(amount, BigDecimal.ZERO.setScale(0, RoundingMode.HALF_UP), bearer, null, "Sans frais");
        }
        FeeRuleLine matched = rule.getLines().stream()
                .filter(line -> inBand(amount, line.getMinAmount(), line.getMaxAmount()))
                .findFirst()
                .orElse(rule.getLines().get(rule.getLines().size() - 1));

        BigDecimal fee = BigDecimal.ZERO;
        if (matched.getFixedAmount() != null) {
            fee = fee.add(matched.getFixedAmount());
        }
        if (matched.getPercentValue() != null) {
            fee = fee.add(amount.multiply(matched.getPercentValue())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP));
        }
        if (matched.getFixedAmount() == null && matched.getPercentValue() == null) {
            if (matched.getFeeType() == FeeType.PERCENT) {
                fee = amount.multiply(matched.getValue())
                        .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
            } else {
                fee = matched.getValue().setScale(0, RoundingMode.HALF_UP);
            }
        }
        if (matched.getMinFee() != null) {
            fee = fee.max(matched.getMinFee());
        }
        if (matched.getMaxFee() != null) {
            fee = fee.min(matched.getMaxFee());
        }
        fee = fee.setScale(0, RoundingMode.HALF_UP);

        String breakdown = buildBreakdown(matched, fee);
        return amountsForBearer(amount, fee, bearer, rule.getCode(), breakdown);
    }

    private FeeQuote amountsForBearer(
            BigDecimal amount, BigDecimal fee, FeeBearer bearer, String ruleCode, String breakdown
    ) {
        BigDecimal totalDebit;
        BigDecimal amountReceived;
        if (bearer == FeeBearer.RECIPIENT) {
            totalDebit = amount;
            amountReceived = amount.subtract(fee).max(BigDecimal.ZERO);
        } else {
            totalDebit = amount.add(fee);
            amountReceived = amount;
        }
        return new FeeQuote(amount, fee, totalDebit, amountReceived, bearer, ruleCode, breakdown);
    }

    private String buildBreakdown(FeeRuleLine line, BigDecimal fee) {
        List<String> parts = new ArrayList<>();
        if (line.getFixedAmount() != null) {
            parts.add("fixe " + line.getFixedAmount().stripTrailingZeros().toPlainString());
        }
        if (line.getPercentValue() != null) {
            parts.add(line.getPercentValue().stripTrailingZeros().toPlainString() + "%");
        }
        if (parts.isEmpty()) {
            if (line.getFeeType() == FeeType.PERCENT) {
                parts.add(line.getValue().stripTrailingZeros().toPlainString() + "%");
            } else {
                parts.add("fixe " + line.getValue().stripTrailingZeros().toPlainString());
            }
        }
        return String.join(" + ", parts) + " = " + fee;
    }

    private boolean inBand(BigDecimal amount, BigDecimal min, BigDecimal max) {
        if (min != null && amount.compareTo(min) < 0) {
            return false;
        }
        if (max != null && amount.compareTo(max) > 0) {
            return false;
        }
        return true;
    }

    private static BusinessException removedParticipants() {
        return new BusinessException(
                "PARTICIPANT_REMOVED",
                "Le référentiel local des participants a été retiré",
                HttpStatus.GONE);
    }

    private TransferFeePolicyResponse toFeePolicyResponse(TransferFeePolicy p) {
        return new TransferFeePolicyResponse(
                p.getId(),
                p.getCode(),
                p.getName(),
                p.getPolicyType(),
                p.getCountryCode(),
                p.getCurrency(),
                p.getEmitterCode(),
                p.getBeneficiaryCode(),
                p.getRailCode(),
                p.getFeeRule().getId(),
                p.getFeeRule().getCode(),
                p.isActif(),
                p.getOrdre()
        );
    }

    private FeeRuleResponse toFeeRuleResponse(FeeRule rule) {
        List<FeeRuleLineDto> lines = rule.getLines() == null ? List.of() : rule.getLines().stream()
                .map(l -> new FeeRuleLineDto(
                        l.getId(), l.getFeeType(), l.getMinAmount(), l.getMaxAmount(), l.getValue(),
                        l.getFixedAmount(), l.getPercentValue(), l.getMinFee(), l.getMaxFee(), l.getOrdre()))
                .toList();
        return new FeeRuleResponse(
                rule.getId(), rule.getCode(), rule.getName(), rule.getCurrency(),
                rule.getDescription(), rule.isActif(),
                rule.getFeeBearer() != null ? rule.getFeeBearer() : FeeBearer.SENDER,
                lines
        );
    }

    private String normalize(String code) {
        return code.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }

}
