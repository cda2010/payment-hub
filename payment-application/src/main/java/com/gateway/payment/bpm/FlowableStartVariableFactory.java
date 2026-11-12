package com.gateway.payment.bpm;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.common.exception.BusinessException;
import com.gateway.payment.configuration.domain.PaymentFlow;
import com.gateway.payment.configuration.repository.PaymentProviderRepository;
import com.gateway.payment.payment.domain.PaymentTransaction;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Construction des variables de démarrage Flowable (hors logique métier produit).
 * <p>
 * {@code terminalOnConfirm} : priorité contexte explicite →
 * {@code catalogConfiguration.terminalOnConfirm} → défaut {@code true}.
 * Un nouveau produit (ex. RTGS) déclare le flag dans la config catalogue, sans code Java.
 */
@Component
public class FlowableStartVariableFactory {

    private final PaymentProviderRepository providerRepository;

    public FlowableStartVariableFactory(PaymentProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    public Map<String, Object> build(
            PaymentTransaction tx,
            PaymentFlow flow,
            String processKey,
            Map<String, Object> context
    ) {
        Map<String, Object> vars = new HashMap<>();
        if (context != null) {
            vars.putAll(context);
        }
        vars.put(PaymentProcessSupport.VAR_PAYMENT_ID, tx.getPaymentId());
        vars.put(PaymentProcessSupport.VAR_FLOW_ID, flow.getId().toString());
        vars.put(PaymentProcessSupport.VAR_FLOW_CODE, flow.getCode());
        vars.put(PaymentProcessSupport.VAR_FLOW_NOM, flow.getNom());
        vars.put(PaymentProcessSupport.VAR_BPM_PROCESS_KEY, processKey);
        vars.put(PaymentProcessSupport.VAR_PROVIDER_CODE, tx.getProviderCode());
        vars.put("amount", tx.getAmount());
        vars.put("currency", tx.getCurrency());
        if (tx.getPhoneNumber() != null) {
            vars.put("phoneNumber", tx.getPhoneNumber());
        }
        putTrimmed(vars, context, "payProviderCode");
        putBillProvider(vars, context, tx);
        applyTerminalOnConfirm(vars, context);
        enrichOrchestrationVars(tx, vars);
        return vars;
    }

    public static String resolveProcessKey(PaymentFlow flow, Map<String, Object> context) {
        if (context != null) {
            Object catalogKey = context.get("catalogWorkflowKey");
            if (catalogKey != null && StringUtils.hasText(String.valueOf(catalogKey))) {
                return String.valueOf(catalogKey).trim();
            }
        }
        if (flow == null || !StringUtils.hasText(flow.getBpmProcessKey())) {
            throw new BusinessException("BPM_PROCESS_KEY_MISSING", "Aucune clé BPMN sur le parcours");
        }
        return flow.getBpmProcessKey().trim();
    }

    /**
     * Alias de champs formulaire → paramètres FSE / schéma.
     */
    public static Map<String, String> buildUserParams(Map<String, Object> data) {
        Map<String, String> userParams = new LinkedHashMap<>();
        if (data == null) {
            return userParams;
        }
        data.forEach((k, v) -> {
            if (v == null || "otp".equalsIgnoreCase(k)) {
                return;
            }
            String value = String.valueOf(v);
            userParams.put(k, value);
            if ("accountId".equals(k)) {
                userParams.putIfAbsent("numeroCompte", value);
                userParams.putIfAbsent("compteDebiteur", value);
                userParams.putIfAbsent("CPTSOURCE", value);
            }
        });
        return userParams;
    }

    private void putBillProvider(Map<String, Object> vars, Map<String, Object> context, PaymentTransaction tx) {
        Object billProvider = vars.get("billProviderCode");
        if (billProvider == null && context != null) {
            billProvider = context.get("billProviderCode");
        }
        if (billProvider != null && !String.valueOf(billProvider).isBlank()) {
            vars.put("billProviderCode", String.valueOf(billProvider).trim());
        } else {
            vars.put("billProviderCode", tx.getProviderCode());
        }
    }

    private static void putTrimmed(Map<String, Object> vars, Map<String, Object> context, String key) {
        Object value = vars.get(key);
        if (value == null && context != null) {
            value = context.get(key);
        }
        if (value != null && !String.valueOf(value).isBlank()) {
            vars.put(key, String.valueOf(value).trim());
        }
    }

    /**
     * 1) {@code context.terminalOnConfirm} explicite
     * 2) {@code catalogConfiguration.terminalOnConfirm} (produit catalogue)
     * 3) défaut {@code true} (confirm = fin de paiement)
     */
    static void applyTerminalOnConfirm(Map<String, Object> vars, Map<String, Object> context) {
        Boolean explicit = asBoolean(vars.get("terminalOnConfirm"));
        if (explicit == null && context != null) {
            explicit = asBoolean(context.get("terminalOnConfirm"));
        }
        if (explicit == null && context != null) {
            Object catalogCfg = context.get("catalogConfiguration");
            if (catalogCfg instanceof Map<?, ?> cfg) {
                explicit = asBoolean(cfg.get("terminalOnConfirm"));
                if (explicit == null) {
                    explicit = asBoolean(cfg.get("terminal_on_confirm"));
                }
            }
        }
        vars.put("terminalOnConfirm", explicit == null || explicit);
    }

    private static Boolean asBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean b) {
            return b;
        }
        String s = String.valueOf(value).trim();
        if (s.isEmpty()) {
            return null;
        }
        if ("true".equalsIgnoreCase(s) || "1".equals(s) || "yes".equalsIgnoreCase(s)) {
            return true;
        }
        if ("false".equalsIgnoreCase(s) || "0".equals(s) || "no".equalsIgnoreCase(s)) {
            return false;
        }
        return null;
    }

    private void enrichOrchestrationVars(PaymentTransaction tx, Map<String, Object> vars) {
        ServiceTypeCode serviceType = tx.getServiceType() != null
                ? tx.getServiceType()
                : ServiceTypeCode.PAYEMENT;
        if (serviceType == ServiceTypeCode.CONSULTATION_SOLDE) {
            vars.put("typeMethode", "CONSULTATIONSOLDE");
            vars.put("apiUtil", "FSESOURCE");
        }
        providerRepository.findByCode(tx.getProviderCode()).ifPresent(p -> {
            if (StringUtils.hasText(p.getCodeFse())) {
                vars.put(GatewayCallFseBpmnErrors.VAR_SOURCE_CODE_FSE, p.getCodeFse());
                vars.put(GatewayCallFseBpmnErrors.VAR_DEST_CODE_FSE, p.getCodeFse());
            }
        });
    }
}
