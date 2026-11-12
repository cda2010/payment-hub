package com.gateway.payment.billing.service;

import com.gateway.payment.billing.domain.AmountPolicy;
import com.gateway.payment.billing.domain.BillerProduct;
import com.gateway.payment.common.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class BillAmountPolicyService {

    private final BillerService billerService;

    public BillAmountPolicyService(BillerService billerService) {
        this.billerService = billerService;
    }

    /**
     * Valide les lignes facture du contexte de paiement (data.billLines ou champs plats).
     */
    @SuppressWarnings("unchecked")
    public BigDecimal validateAndResolveTotal(Map<String, Object> data, BigDecimal requestedAmount) {
        if (data == null || data.isEmpty()) {
            return requestedAmount;
        }

        Object linesObj = data.get("billLines");
        if (linesObj instanceof List<?> lines && !lines.isEmpty()) {
            BigDecimal total = BigDecimal.ZERO;
            for (Object lineObj : lines) {
                if (!(lineObj instanceof Map<?, ?> line)) {
                    continue;
                }
                String billerCode = str(line.get("billerCode"));
                String productCode = str(line.get("productCode"));
                BigDecimal amountDue = decimal(line.get("amountDue"));
                BigDecimal amountPaid = decimal(line.get("amountPaid"));
                if (amountPaid == null) {
                    amountPaid = amountDue;
                }
                if (billerCode != null && productCode != null) {
                    BillerProduct product = billerService.requireProductByCodes(billerCode, productCode);
                    validateLine(product, amountDue, amountPaid);
                }
                total = total.add(amountPaid);
            }
            if (requestedAmount != null && requestedAmount.compareTo(total) != 0) {
                throw new BusinessException(
                        "BILL_AMOUNT_MISMATCH",
                        "Montant total (" + requestedAmount + ") ≠ somme des lignes (" + total + ")");
            }
            return total;
        }

        String billerCode = str(data.get("billerCode"));
        String productCode = str(data.get("productCode"));
        if (billerCode != null && productCode != null
                && requestedAmount != null
                && requestedAmount.compareTo(BigDecimal.ZERO) > 0) {
            BillerProduct product = billerService.requireProductByCodes(billerCode, productCode);
            BigDecimal amountDue = decimal(data.get("amountDue"));
            validateLine(product, amountDue, requestedAmount);
        }
        return requestedAmount;
    }

    public void validateLine(BillerProduct product, BigDecimal amountDue, BigDecimal amountPaid) {
        if (amountPaid == null || amountPaid.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("AMOUNT_REQUIRED", "Montant invalide pour " + product.getCode());
        }
        AmountPolicy policy = product.getAmountPolicy();
        BigDecimal min = product.getMinAmount() != null ? product.getMinAmount() : BigDecimal.ONE;
        BigDecimal max = product.getMaxAmount();

        switch (policy) {
            case FULL_ONLY -> {
                if (amountDue == null) {
                    throw new BusinessException("AMOUNT_DUE_REQUIRED", "Montant dû requis pour " + product.getCode());
                }
                if (amountPaid.compareTo(amountDue) != 0) {
                    throw new BusinessException(
                            "FULL_AMOUNT_REQUIRED",
                            "Le produit " + product.getCode() + " exige le montant dû exact (" + amountDue + ")");
                }
            }
            case PARTIAL -> {
                if (amountDue == null) {
                    throw new BusinessException("AMOUNT_DUE_REQUIRED", "Montant dû requis pour paiement partiel");
                }
                if (amountPaid.compareTo(min) < 0) {
                    throw new BusinessException("AMOUNT_TOO_LOW", "Montant minimum: " + min);
                }
                if (!product.isAllowOverpay() && amountPaid.compareTo(amountDue) > 0) {
                    throw new BusinessException(
                            "AMOUNT_TOO_HIGH",
                            "Paiement partiel plafonné au dû (" + amountDue + ")");
                }
                if (max != null && amountPaid.compareTo(max) > 0) {
                    throw new BusinessException("AMOUNT_TOO_HIGH", "Montant maximum: " + max);
                }
            }
            case EDITABLE -> {
                if (amountPaid.compareTo(min) < 0) {
                    throw new BusinessException("AMOUNT_TOO_LOW", "Montant minimum: " + min);
                }
                if (max != null && amountPaid.compareTo(max) > 0) {
                    throw new BusinessException("AMOUNT_TOO_HIGH", "Montant maximum: " + max);
                }
            }
        }
    }

    private static String str(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private static BigDecimal decimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bd) {
            return bd;
        }
        if (value instanceof Number n) {
            return BigDecimal.valueOf(n.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

