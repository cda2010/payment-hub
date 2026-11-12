package com.gateway.payment.billing.connector;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gateway.payment.billing.connector.dto.Bill;
import com.gateway.payment.billing.connector.dto.BillResponse;
import com.gateway.payment.billing.connector.dto.MParam;
import com.gateway.payment.payment.domain.PaymentTransaction;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Parse / applique {@link BillResponse} dans le contexte paiement (sans chasse stringly ad hoc).
 */
@Component
public class BillResponseSupport {

    private final ObjectMapper objectMapper;

    public BillResponseSupport(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Optional<BillResponse> tryParse(Map<String, Object> rawBody) {
        if (rawBody == null || rawBody.isEmpty()) {
            return Optional.empty();
        }
        Object bills = rawBody.get("bills");
        boolean looksLikeBillResponse = bills instanceof List<?>
                || rawBody.containsKey("montant")
                || (rawBody.containsKey("reference") && rawBody.containsKey("code"));
        if (!looksLikeBillResponse && !rawBody.containsKey("billReference") && !rawBody.containsKey("amountDue")) {
            return Optional.empty();
        }
        try {
            BillResponse parsed = objectMapper.convertValue(rawBody, BillResponse.class);
            if (parsed.bills().isEmpty() && rawBody.containsKey("billReference")) {
                // Legacy flat inquiry → 1 bill
                    Bill single = objectMapper.convertValue(Map.of(
                        "reference", String.valueOf(rawBody.get("billReference")),
                        "montant", String.valueOf(rawBody.getOrDefault("amountDue", rawBody.get("amount"))),
                        "amountDue", String.valueOf(rawBody.getOrDefault("amountDue", rawBody.get("amount"))),
                        "libelle", String.valueOf(rawBody.getOrDefault("customerName",
                                rawBody.getOrDefault("billLabel", rawBody.get("billReference")))),
                        "currency", rawBody.get("currency"),
                        "dueDate", rawBody.get("dueDate"),
                        "status", rawBody.getOrDefault("billStatus", rawBody.get("status"))
                ), Bill.class);
                return Optional.of(new BillResponse(
                        String.valueOf(rawBody.getOrDefault("code", "00")),
                        String.valueOf(rawBody.getOrDefault("status", "00")),
                        stringOrNull(rawBody.get("providerReference")),
                        single.reference(),
                        1,
                        List.of(single)
                ));
            }
            if (parsed.bills().isEmpty()) {
                return Optional.empty();
            }
            // Legacy list items (billReference/amountDue) → normalize via convert each element
            List<Bill> normalized = new ArrayList<>();
            for (Bill b : parsed.bills()) {
                if (b != null && StringUtils.hasText(b.reference())) {
                    normalized.add(b);
                    continue;
                }
            }
            if (normalized.isEmpty() && bills instanceof List<?> rawList) {
                for (Object item : rawList) {
                    if (!(item instanceof Map<?, ?> m)) {
                        continue;
                    }
                    String ref = firstStr(m, "reference", "billReference", "id");
                    if (!StringUtils.hasText(ref)) {
                        continue;
                    }
                    String montant = firstStr(m, "amountDue", "montant", "amount");
                    String libelle = firstStr(m, "libelle", "billLabel", "customerName", "label");
                    normalized.add(new Bill(
                            ref,
                            montant,
                            montant,
                            libelle != null ? libelle : ref,
                            firstStr(m, "currency"),
                            firstStr(m, "dueDate"),
                            firstStr(m, "status", "billStatus"),
                            List.of()
                    ));
                }
            }
            if (normalized.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new BillResponse(
                    parsed.code() != null ? parsed.code() : "00",
                    parsed.status() != null ? parsed.status() : "00",
                    parsed.providerReference(),
                    parsed.reference(),
                    normalized.size(),
                    normalized
            ));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    /**
     * Pose {@code billResponse}, {@code bills} (UI-friendly), montant si lookup mono-facture.
     */
    public void applyToContext(
            BillResponse response,
            PaymentTransaction tx,
            Map<String, Object> context,
            boolean mapAmountFromLookup,
            Map<String, Object> flowVars
    ) {
        if (response == null || context == null) {
            return;
        }
        context.put("billResponse", response);
        context.put("billCount", response.billCount());
        context.put("count", response.billCount());
        if (StringUtils.hasText(response.providerReference())) {
            context.put("providerReference", response.providerReference());
            context.putIfAbsent("reference", response.providerReference());
        }

        List<Map<String, Object>> uiBills = new ArrayList<>();
        for (Bill bill : response.bills()) {
            if (bill == null || !StringUtils.hasText(bill.reference())) {
                continue;
            }
            uiBills.add(toUiMap(bill));
        }
        context.put("bills", uiBills);

        if (response.bills().size() == 1) {
            Bill only = response.bills().get(0);
            context.put("billReference", only.reference());
            context.put("billLabel", only.libelle());
            if (StringUtils.hasText(only.currency())) {
                context.put("currency", only.currency());
            }
            if (StringUtils.hasText(only.dueDate())) {
                context.put("dueDate", only.dueDate());
            }
            String customerName = only.param("customerName");
            if (StringUtils.hasText(customerName)) {
                context.put("customerName", customerName);
            }
            String subscriber = only.param("subscriberReference");
            if (StringUtils.hasText(subscriber)) {
                context.put("subscriberReference", subscriber);
            }
            if (mapAmountFromLookup) {
                applyAmount(only, tx, context, flowVars);
            }
        }
    }

    public Optional<Bill> findByReference(Map<String, Object> context, String billReference) {
        if (!StringUtils.hasText(billReference) || context == null) {
            return Optional.empty();
        }
        Object stored = context.get("billResponse");
        if (stored instanceof BillResponse br) {
            return findIn(br.bills(), billReference);
        }
        if (stored instanceof Map<?, ?> raw) {
            Optional<BillResponse> parsed = tryParse(castMap(raw));
            if (parsed.isPresent()) {
                return findIn(parsed.get().bills(), billReference);
            }
        }
        Object billsRaw = context.get("bills");
        if (billsRaw instanceof List<?> list) {
            for (Object item : list) {
                Bill bill = coerceBill(item);
                if (bill != null && billReference.trim().equalsIgnoreCase(bill.reference())) {
                    return Optional.of(bill);
                }
            }
        }
        return Optional.empty();
    }

    public void applySelectedBill(
            Bill bill,
            PaymentTransaction tx,
            Map<String, Object> vars
    ) {
        if (bill == null || vars == null) {
            return;
        }
        vars.put("billReference", bill.reference());
        if (StringUtils.hasText(bill.libelle())) {
            vars.put("billLabel", bill.libelle());
        }
        if (StringUtils.hasText(bill.currency())) {
            tx.setCurrency(bill.currency().trim());
            vars.put("currency", bill.currency().trim());
        }
        if (StringUtils.hasText(bill.dueDate())) {
            vars.put("dueDate", bill.dueDate());
        }
        if (StringUtils.hasText(bill.status())) {
            vars.put("billStatus", bill.status());
        }
        String customerName = bill.param("customerName");
        if (StringUtils.hasText(customerName)) {
            vars.put("customerName", customerName);
        }
        String subscriber = bill.param("subscriberReference");
        if (StringUtils.hasText(subscriber)) {
            vars.put("subscriberReference", subscriber);
        }
        for (MParam p : bill.extendedData()) {
            if (p != null && p.isAffiche() && StringUtils.hasText(p.code())) {
                vars.putIfAbsent(p.code(), p.valeur());
            }
        }
        applyAmount(bill, tx, vars, vars);
    }

    private static void applyAmount(
            Bill bill,
            PaymentTransaction tx,
            Map<String, Object> context,
            Map<String, Object> flowVars
    ) {
        BigDecimal amount = bill.montantAsDecimal();
        if (amount == null) {
            return;
        }
        if (tx != null) {
            tx.setAmount(amount);
        }
        context.put("amount", amount);
        context.put("amountDue", amount);
        context.put("montant", amount);
        if (flowVars != null && flowVars != context) {
            flowVars.put("amount", amount);
            flowVars.put("amountDue", amount);
        }
    }

    private static Map<String, Object> toUiMap(Bill bill) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("reference", bill.reference());
        m.put("billReference", bill.reference());
        m.put("montant", bill.montant() != null ? bill.montant() : bill.amountDue());
        m.put("amountDue", bill.amountDue() != null ? bill.amountDue() : bill.montant());
        m.put("amount", bill.amountDue() != null ? bill.amountDue() : bill.montant());
        m.put("libelle", bill.libelle());
        m.put("billLabel", bill.libelle());
        if (StringUtils.hasText(bill.currency())) {
            m.put("currency", bill.currency());
        }
        if (StringUtils.hasText(bill.dueDate())) {
            m.put("dueDate", bill.dueDate());
        }
        if (StringUtils.hasText(bill.status())) {
            m.put("status", bill.status());
            m.put("billStatus", bill.status());
        }
        if (bill.extendedData() != null && !bill.extendedData().isEmpty()) {
            m.put("extendedData", bill.extendedData());
            for (MParam p : bill.extendedData()) {
                if (p != null && StringUtils.hasText(p.code())) {
                    m.putIfAbsent(p.code(), p.valeur());
                }
            }
        }
        return m;
    }

    private Optional<Bill> findIn(List<Bill> bills, String billReference) {
        if (bills == null) {
            return Optional.empty();
        }
        for (Bill bill : bills) {
            if (bill != null && billReference.trim().equalsIgnoreCase(bill.reference())) {
                return Optional.of(bill);
            }
        }
        return Optional.empty();
    }

    private Bill coerceBill(Object item) {
        if (item instanceof Bill b) {
            return b;
        }
        if (item instanceof Map<?, ?> m) {
            String ref = firstStr(m, "reference", "billReference", "id");
            if (!StringUtils.hasText(ref)) {
                return null;
            }
            return new Bill(
                    ref,
                    firstStr(m, "montant", "amountDue", "amount"),
                    firstStr(m, "amountDue", "montant", "amount"),
                    firstStr(m, "libelle", "billLabel", "customerName", "label"),
                    firstStr(m, "currency"),
                    firstStr(m, "dueDate"),
                    firstStr(m, "status", "billStatus"),
                    List.of()
            );
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Map<?, ?> raw) {
        return (Map<String, Object>) raw;
    }

    private static String stringOrNull(Object v) {
        return v != null && StringUtils.hasText(String.valueOf(v)) ? String.valueOf(v).trim() : null;
    }

    private static String firstStr(Map<?, ?> m, String... keys) {
        for (String key : keys) {
            Object v = m.get(key);
            if (v != null && StringUtils.hasText(String.valueOf(v))) {
                return String.valueOf(v).trim();
            }
        }
        return null;
    }
}
