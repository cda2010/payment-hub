package com.gateway.payment.remittance.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.payment.repository.PaymentTransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Lookup / clôture d'un envoi remittance pour le produit REMITTANCE_RECEIVE.
 */
@Service
public class RemittancePayoutSupport {

    private final PaymentTransactionRepository transactionRepository;
    private final ObjectMapper objectMapper;

    public RemittancePayoutSupport(
            PaymentTransactionRepository transactionRepository,
            ObjectMapper objectMapper
    ) {
        this.transactionRepository = transactionRepository;
        this.objectMapper = objectMapper;
    }

    public record RemittanceSendSnapshot(
            PaymentTransaction sendTx,
            Map<String, Object> sendContext,
            String remittanceReference,
            String payoutMethod,
            String remittanceStatus
    ) {}

    public RemittanceSendSnapshot lookupAvailable(String reference) {
        if (!StringUtils.hasText(reference)) {
            throw new BusinessException("REMITTANCE_REF_REQUIRED", "Référence remittance requise",
                    HttpStatus.BAD_REQUEST);
        }
        String ref = reference.trim();
        List<PaymentTransaction> candidates = transactionRepository.findRemittanceCandidatesByReference(ref);
        PaymentTransaction match = null;
        Map<String, Object> ctx = Map.of();
        for (PaymentTransaction tx : candidates) {
            Map<String, Object> c = readContext(tx);
            if (matchesReference(tx, c, ref) && isRemittanceSend(c, tx)) {
                match = tx;
                ctx = c;
                break;
            }
        }
        if (match == null) {
            // Fallback provider_reference / payment_id exact
            Optional<PaymentTransaction> byProv = transactionRepository.findByProviderReference(ref);
            if (byProv.isPresent() && isRemittanceSend(readContext(byProv.get()), byProv.get())) {
                match = byProv.get();
                ctx = readContext(match);
            } else {
                Optional<PaymentTransaction> byId = transactionRepository.findByPaymentId(ref);
                if (byId.isPresent() && isRemittanceSend(readContext(byId.get()), byId.get())) {
                    match = byId.get();
                    ctx = readContext(match);
                }
            }
        }
        if (match == null) {
            throw new BusinessException("REMITTANCE_NOT_FOUND",
                    "Aucun envoi remittance pour la référence " + ref, HttpStatus.NOT_FOUND);
        }

        String status = firstNonBlank(
                str(ctx.get("remittanceStatus")),
                match.getStatus() == PaymentStatus.WAITING_CALLBACK ? "AVAILABLE_FOR_PAYOUT" : null,
                match.getStatus() != null ? match.getStatus().name() : null);
        if (!"AVAILABLE_FOR_PAYOUT".equalsIgnoreCase(status)
                && match.getStatus() != PaymentStatus.WAITING_CALLBACK
                && match.getStatus() != PaymentStatus.PENDING
                && match.getStatus() != PaymentStatus.PROCESSING) {
            throw new BusinessException("REMITTANCE_NOT_AVAILABLE",
                    "Envoi non disponible pour payout (statut=" + status + ")");
        }
        if ("PAID_OUT".equalsIgnoreCase(status)
                || "CREDITED".equalsIgnoreCase(status)
                || "COMPLETED".equalsIgnoreCase(status)
                || match.getStatus() == PaymentStatus.SUCCESS) {
            throw new BusinessException("REMITTANCE_ALREADY_PAID",
                    "Cet envoi a déjà été remis / crédité");
        }

        String remittanceReference = firstNonBlank(
                str(ctx.get("remittanceReference")),
                match.getProviderReference(),
                str(ctx.get("quoteId")),
                match.getPaymentId());
        String payoutMethod = firstNonBlank(
                str(ctx.get("payoutMethod")),
                "CASH_PICKUP");

        return new RemittanceSendSnapshot(match, ctx, remittanceReference, payoutMethod.toUpperCase(Locale.ROOT),
                "AVAILABLE_FOR_PAYOUT");
    }

    /** Copie les champs utiles de l'envoi dans le contexte receive. */
    public Map<String, Object> projectToReceiveContext(RemittanceSendSnapshot snap) {
        Map<String, Object> out = new LinkedHashMap<>();
        Map<String, Object> send = snap.sendContext();
        PaymentTransaction tx = snap.sendTx();

        out.put("parentPaymentId", tx.getPaymentId());
        out.put("remittanceReference", snap.remittanceReference());
        out.put("payoutMethod", snap.payoutMethod());
        out.put("remittanceStatus", snap.remittanceStatus());
        out.put("availableForPayout", true);

        copy(send, out,
                "senderName", "senderPhone", "beneficiaryName", "beneficiaryPhone", "beneficiaryCity",
                "destinationCountry", "receiveCurrency", "receiveAmount", "sendAmount", "sendCurrency",
                "amount", "currency", "fees", "fxRate", "totalDebit", "quoteId", "purpose",
                "providerCode", "payoutAccount", "documentProfileCode");

        if (out.get("receiveAmount") == null && tx.getAmount() != null) {
            out.put("receiveAmount", tx.getAmount());
        }
        if (out.get("receiveCurrency") == null && tx.getCurrency() != null) {
            out.put("receiveCurrency", tx.getCurrency());
        }
        if (out.get("amount") == null) {
            out.put("amount", out.get("receiveAmount"));
        }
        if (out.get("currency") == null) {
            out.put("currency", out.get("receiveCurrency"));
        }
        if (out.get("providerCode") == null) {
            Object fromRoles = null;
            if (send.get("roleAssignments") instanceof Map<?, ?> roles) {
                fromRoles = roles.get("PROVIDER");
            }
            out.put("providerCode", firstNonBlank(str(fromRoles), tx.getProviderCode()));
        }
        return out;
    }

    @Transactional
    public void markSendPaidOut(String parentPaymentId, String receivePaymentId, String payoutMethod) {
        if (!StringUtils.hasText(parentPaymentId)) {
            return;
        }
        transactionRepository.findByPaymentId(parentPaymentId).ifPresent(send -> {
            Map<String, Object> ctx = new HashMap<>(readContext(send));
            String method = firstNonBlank(payoutMethod, str(ctx.get("payoutMethod")), "CASH_PICKUP");
            String terminal = "CASH_PICKUP".equalsIgnoreCase(method) ? "PAID_OUT" : "CREDITED";
            ctx.put("remittanceStatus", terminal);
            ctx.put("availableForPayout", false);
            ctx.put("receivePaymentId", receivePaymentId);
            ctx.put("payoutCompletedAt", Instant.now().toString());
            send.applyStatus(PaymentStatus.SUCCESS);
            send.setCompletedAt(Instant.now());
            send.setCurrentStep(terminal);
            send.setContextJson(writeJson(ctx));
            transactionRepository.save(send);
        });
    }

    private static boolean matchesReference(PaymentTransaction tx, Map<String, Object> ctx, String ref) {
        return ref.equalsIgnoreCase(str(ctx.get("remittanceReference")))
                || ref.equalsIgnoreCase(str(ctx.get("quoteId")))
                || ref.equalsIgnoreCase(tx.getProviderReference())
                || ref.equalsIgnoreCase(tx.getPaymentId());
    }

    private static boolean isRemittanceSend(Map<String, Object> ctx, PaymentTransaction tx) {
        String catalog = firstNonBlank(str(ctx.get("catalogServiceCode")), str(ctx.get("catalogProductCode")));
        if ("REMITTANCE".equalsIgnoreCase(catalog)) {
            return true;
        }
        // Heuristique : a un payoutMethod ou remittanceStatus
        return ctx.containsKey("payoutMethod")
                || ctx.containsKey("remittanceStatus")
                || ctx.containsKey("remittanceReference");
    }

    private Map<String, Object> readContext(PaymentTransaction tx) {
        if (tx.getContextJson() == null || tx.getContextJson().isBlank()) {
            return new LinkedHashMap<>();
        }
        try {
            return objectMapper.readValue(tx.getContextJson(), new TypeReference<>() {});
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return "{}";
        }
    }

    private static void copy(Map<String, Object> from, Map<String, Object> to, String... keys) {
        for (String k : keys) {
            Object v = from.get(k);
            if (v != null) {
                to.put(k, v);
            }
        }
    }

    private static String str(Object v) {
        return v != null ? String.valueOf(v).trim() : null;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                return v.trim();
            }
        }
        return null;
    }
}
