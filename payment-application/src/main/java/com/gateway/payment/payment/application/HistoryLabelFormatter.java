package com.gateway.payment.payment.application;

import com.gateway.payment.common.i18n.GatewayI18nService;
import com.gateway.payment.common.i18n.LocalizedMessages;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Libellés d'historique canal — format relevé {@code TYPE · CONTREPARTIE · CANAL|RÉF},
 * résolus selon {@code Accept-Language} ({@link GatewayI18nService}).
 */
@Component
public class HistoryLabelFormatter {

    private static final Set<String> INFLOW_PRODUCTS = Set.of("CASH_IN", "REMITTANCE_RECEIVE");

    private static final Map<String, Map<String, String>> RAIL_LABELS = Map.ofEntries(
            entry("CORIS_MONEY", "Coris Money", "Coris Money"),
            entry("CORIS_BANK", "Coris Bank", "Coris Bank"),
            entry("ORANGE", "Orange Money", "Orange Money"),
            entry("ORANGE_MONEY", "Orange Money", "Orange Money"),
            entry("MOOV", "Moov Money", "Moov Money"),
            entry("MOOV_MONEY", "Moov Money", "Moov Money"),
            entry("WAVE", "Wave", "Wave"),
            entry("TELMA", "Telma", "Telma"),
            entry("AIRTEL", "Airtel Money", "Airtel Money"),
            entry("FREE", "Free Money", "Free Money"),
            entry("GAB", "GAB", "ATM"),
            entry("AGENT", "Agent", "Agent"),
            entry("AGENCY", "Agence", "Branch"),
            entry("STAR", "STAR", "STAR"),
            entry("RTGS", "RTGS", "RTGS"),
            entry("SWIFT", "SWIFT", "SWIFT")
    );

    private final GatewayI18nService i18n;
    private final TemplateMapper templateMapper;

    public HistoryLabelFormatter(GatewayI18nService i18n, TemplateMapper templateMapper) {
        this.i18n = i18n;
        this.templateMapper = templateMapper;
    }

    public record Labels(String title, String subtitle, String productLabel, String statusLabel) {}

    /**
     * @param catalogProductName nom catalogue déjà localisé (peut être null)
     */
    public Labels format(
            String catalogServiceCode,
            String serviceType,
            String status,
            String provider,
            Instant createdAt,
            Map<String, Object> context,
            String catalogProductName
    ) {
        return format(catalogServiceCode, serviceType, status, provider, createdAt, context, catalogProductName, null);
    }

    /**
     * @param historyConfig section {@code configuration.history} déjà localisée (optionnelle)
     */
    public Labels format(
            String catalogServiceCode,
            String serviceType,
            String status,
            String provider,
            Instant createdAt,
            Map<String, Object> context,
            String catalogProductName,
            Map<String, Object> historyConfig
    ) {
        Map<String, Object> ctx = context != null ? context : Map.of();
        String product = firstNonBlank(catalogServiceCode, serviceType);
        product = product != null ? product.toUpperCase(Locale.ROOT) : "";
        boolean inflow = isInflow(product, serviceType);

        String productLabel = firstNonBlank(catalogProductName, fallbackProductLabel(product, inflow));
        String statusLabel = statusLabel(status);
        String kind = operationKind(product, productLabel, inflow);
        String kindOverride = historyConfig != null ? str(historyConfig.get("kindLabel")) : null;
        if (StringUtils.hasText(kindOverride)) {
            kind = kindOverride;
        }
        String party = partyLabel(ctx, inflow);
        String channel = channelLabel(product, ctx, provider);
        String ref = primaryReference(product, ctx);
        String agent = agentPointLabel(ctx);
        String when = createdAt != null ? timeShort(createdAt) : null;

        List<String> titleParts = new ArrayList<>();
        titleParts.add(kind);
        if (StringUtils.hasText(party)) {
            titleParts.add(party);
        }
        if (StringUtils.hasText(channel) && !channel.equals(party)) {
            titleParts.add(channel);
        } else if (StringUtils.hasText(ref) && (party == null || !party.contains(ref))) {
            titleParts.add(ref.startsWith(msg("ref.prefix")) ? ref : msg("ref.prefix") + " " + ref);
        }
        if (titleParts.size() == 1 && StringUtils.hasText(ref)) {
            titleParts.add(msg("ref.prefix") + " " + ref);
        }

        String title = String.join(" · ", titleParts);
        String subtitle = buildSubtitle(statusLabel, ctx, provider, createdAt);

        if (historyConfig != null && !historyConfig.isEmpty()) {
            Map<String, Object> vars = templateVars(
                    ctx, kind, party, channel, ref, agent, productLabel, statusLabel, when, provider);
            String titleTpl = str(historyConfig.get("titleTemplate"));
            if (StringUtils.hasText(titleTpl)) {
                title = templateMapper.apply(titleTpl, vars).trim();
            }
            String subtitleTpl = str(historyConfig.get("subtitleTemplate"));
            if (StringUtils.hasText(subtitleTpl)) {
                subtitle = templateMapper.apply(subtitleTpl, vars).trim();
            }
        }
        return new Labels(title, subtitle, productLabel, statusLabel);
    }

    private Map<String, Object> templateVars(
            Map<String, Object> ctx,
            String kind,
            String party,
            String channel,
            String ref,
            String agent,
            String productLabel,
            String statusLabel,
            String when,
            String provider
    ) {
        Map<String, Object> vars = new java.util.LinkedHashMap<>(ctx);
        vars.put("kind", kind);
        vars.put("party", party);
        vars.put("channel", channel);
        vars.put("ref", ref);
        vars.put("agentDisplay", agent);
        vars.put("productLabel", productLabel);
        vars.put("statusLabel", statusLabel);
        vars.put("when", when);
        if (StringUtils.hasText(provider)) {
            vars.putIfAbsent("provider", provider);
        }
        return vars;
    }

    public boolean isInflow(String catalogOrService, String serviceType) {
        String p = catalogOrService != null ? catalogOrService.toUpperCase(Locale.ROOT) : "";
        String s = serviceType != null ? serviceType.toUpperCase(Locale.ROOT) : "";
        return INFLOW_PRODUCTS.contains(p)
                || INFLOW_PRODUCTS.contains(s)
                || p.contains("CASH_IN")
                || p.contains("REMITTANCE_RECEIVE");
    }

    private String buildSubtitle(
            String statusLabel,
            Map<String, Object> ctx,
            String provider,
            Instant createdAt
    ) {
        List<String> parts = new ArrayList<>();
        parts.add(statusLabel);

        String motive = firstNonBlank(
                str(ctx.get("motive")),
                str(ctx.get("purpose")),
                str(ctx.get("remittanceInfo")),
                str(ctx.get("description")),
                str(ctx.get("narration")));
        if (StringUtils.hasText(motive)) {
            parts.add(truncate(motive, 42));
        } else {
            String rail = firstNonBlank(
                    railLabel(str(ctx.get("destinationProvider"))),
                    railLabel(str(ctx.get("provider"))),
                    railLabel(provider),
                    railLabel(str(ctx.get("withdrawalChannel"))));
            if (StringUtils.hasText(rail)) {
                parts.add(rail);
            }
        }

        String accountHint = maskAccount(firstNonBlank(
                str(ctx.get("destinationAccount")),
                str(ctx.get("destAccount")),
                str(ctx.get("sourceAccount")),
                str(ctx.get("accountNumber"))));
        if (StringUtils.hasText(accountHint) && parts.stream().noneMatch(p -> p.contains(accountHint))) {
            parts.add(accountHint);
        }

        if (createdAt != null) {
            parts.add(timeShort(createdAt));
        }
        return String.join(" · ", parts.stream().filter(StringUtils::hasText).toList());
    }

    private String operationKind(String product, String productLabel, boolean inflow) {
        if (containsAny(product, "ACCOUNT_TRANSFER", "WALLET_TRANSFER") || "TRANSFER".equals(product) || "TRANSFERT".equals(product)) {
            return msg(inflow ? "kind.transfer.in" : "kind.transfer.out");
        }
        if (containsAny(product, "BILL", "FACTURE", "PAIEMENT_FACTURE")) {
            return msg("kind.bill");
        }
        if (containsAny(product, "CASH_OUT", "RETRAIT")) {
            return msg("kind.cash_out");
        }
        if (containsAny(product, "CASH_IN", "DEPOT")) {
            return msg("kind.cash_in");
        }
        if (containsAny(product, "AIRTIME", "CREDIT")) {
            return msg("kind.airtime");
        }
        if (containsAny(product, "QR", "MERCHANT")) {
            return msg("kind.merchant");
        }
        if (product.contains("RTGS")) {
            return msg("kind.rtgs");
        }
        if (product.contains("SWIFT")) {
            return msg("kind.swift");
        }
        if (product.contains("REMITTANCE_RECEIVE") || (product.contains("REMITTANCE") && inflow)) {
            return msg("kind.remittance.in");
        }
        if (product.contains("REMITTANCE")) {
            return msg("kind.remittance.out");
        }
        if (containsAny(product, "LINK", "ALIAS")) {
            return msg("kind.link");
        }
        if (containsAny(product, "COFFRE", "SAVING")) {
            return msg("kind.saving");
        }
        if (containsAny(product, "COTISATION", "TONTINE")) {
            return msg("kind.dues");
        }
        if (inflow) {
            String prefix = msg("kind.receive.prefix");
            if (productLabel != null && productLabel.regionMatches(true, 0, prefix, 0, prefix.length())) {
                return productLabel;
            }
            return prefix + " — " + (StringUtils.hasText(productLabel) ? productLabel : msg("kind.operation"));
        }
        return StringUtils.hasText(productLabel) ? productLabel : msg("kind.operation");
    }

    private String fallbackProductLabel(String product, boolean inflow) {
        if (containsAny(product, "ACCOUNT_TRANSFER") || "TRANSFER".equals(product) || "TRANSFERT".equals(product)) {
            return msg("product.transfer");
        }
        if (containsAny(product, "BILL", "FACTURE", "PAIEMENT_FACTURE")) {
            return msg("product.bill");
        }
        if (product.contains("CASH_OUT") || product.contains("RETRAIT")) {
            return msg("product.cash_out");
        }
        if (product.contains("CASH_IN") || product.contains("DEPOT")) {
            return msg("product.cash_in");
        }
        if (product.contains("AIRTIME")) {
            return msg("product.airtime");
        }
        if (product.contains("QR") || product.contains("MERCHANT")) {
            return msg("product.merchant");
        }
        if (product.contains("RTGS")) {
            return msg("product.rtgs");
        }
        if (product.contains("SWIFT")) {
            return msg("product.swift");
        }
        if (product.contains("REMITTANCE_RECEIVE")) {
            return msg("product.remittance.in");
        }
        if (product.contains("REMITTANCE")) {
            return msg("product.remittance.out");
        }
        if (inflow) {
            return msg("product.receive");
        }
        return StringUtils.hasText(product) ? humanizeCode(product) : msg("kind.operation");
    }

    private String partyLabel(Map<String, Object> ctx, boolean inflow) {
        // Agent : affiché dans le canal (via …), pas en contrepartie « vers ».
        String name = firstNonBlank(
                str(ctx.get("beneficiaryName")),
                str(ctx.get("destinationName")),
                str(ctx.get("recipientName")),
                str(ctx.get("counterpartyName")),
                str(ctx.get("billerName")),
                str(ctx.get("merchantName")),
                str(ctx.get("senderName")),
                str(ctx.get("productName")));
        String phone = firstNonBlank(
                formatPhone(str(ctx.get("destPhone"))),
                formatPhone(str(ctx.get("beneficiaryPhone"))),
                formatPhone(str(ctx.get("phoneNumber"))),
                formatPhone(str(ctx.get("msisdn"))));
        String account = maskAccount(firstNonBlank(
                str(ctx.get("destinationAccount")),
                str(ctx.get("destAccount")),
                str(ctx.get("beneficiaryAccount")),
                str(ctx.get("iban")),
                str(ctx.get("accountNumber"))));

        String prep = msg(inflow ? "party.from" : "party.to");
        if (StringUtils.hasText(name) && StringUtils.hasText(phone)) {
            return prep + " " + name + " (" + phone + ")";
        }
        if (StringUtils.hasText(name) && StringUtils.hasText(account) && !inflow) {
            return prep + " " + name + " (" + account + ")";
        }
        if (StringUtils.hasText(name)) {
            return prep + " " + name;
        }
        if (StringUtils.hasText(phone)) {
            return prep + " " + phone;
        }
        if (StringUtils.hasText(account)) {
            return prep + " " + account;
        }
        return null;
    }

    private String channelLabel(String product, Map<String, Object> ctx, String provider) {
        if (containsAny(product, "CASH_OUT", "RETRAIT", "CASH_IN", "DEPOT")) {
            String agentPoint = agentPointLabel(ctx);
            String channelCode = str(ctx.get("withdrawalChannel"));
            String channelRail = railLabel(channelCode);
            // Priorité au point agent/agence nommé — pas seulement « via Agent ».
            if (StringUtils.hasText(agentPoint)) {
                return msg("channel.via") + " " + agentPoint;
            }
            if (StringUtils.hasText(channelRail)) {
                return msg("channel.via") + " " + channelRail;
            }
        }
        String dest = railLabel(str(ctx.get("destinationProvider")));
        if (StringUtils.hasText(dest)) {
            return dest;
        }
        return railLabel(firstNonBlank(str(ctx.get("provider")), provider));
    }

    /** Point agent/agence pour le détail opération (peut être null). */
    public String agentDisplay(Map<String, Object> context) {
        return agentPointLabel(context != null ? context : Map.of());
    }

    /**
     * Nom du point de remise / encaissement (UO agent, agence…).
     * Ex. {@code Agent Ouahigouya (AG-OUAHIGOUYA)}.
     */
    private static String agentPointLabel(Map<String, Object> ctx) {
        String libelle = firstNonBlank(
                str(ctx.get("agentUniteLibelle")),
                str(ctx.get("agentName")),
                str(ctx.get("agentLabel")),
                str(ctx.get("uniteLabel")),
                str(ctx.get("agencyName")),
                str(ctx.get("agentFseLibelle")));
        String code = firstNonBlank(str(ctx.get("agentCode")), str(ctx.get("agentAliasCode")));
        String display = str(ctx.get("agentUniteDisplay"));

        if (StringUtils.hasText(libelle) && StringUtils.hasText(code) && !libelle.contains(code)) {
            return libelle + " (" + code + ")";
        }
        if (StringUtils.hasText(libelle)) {
            return libelle;
        }
        if (StringUtils.hasText(display)) {
            return display;
        }
        if (StringUtils.hasText(code)) {
            return code;
        }
        return null;
    }

    private String primaryReference(String product, Map<String, Object> ctx) {
        if (containsAny(product, "BILL", "FACTURE", "PAIEMENT_FACTURE")) {
            return firstNonBlank(
                    str(ctx.get("billReference")),
                    str(ctx.get("meterNumber")),
                    str(ctx.get("invoiceNumber")),
                    str(ctx.get("contratNumber")));
        }
        if (product.contains("CASH_OUT")) {
            return firstNonBlank(
                    str(ctx.get("voucherCode")),
                    str(ctx.get("withdrawalCode")),
                    str(ctx.get("generatedCode")));
        }
        if (containsAny(product, "SWIFT", "RTGS")) {
            return firstNonBlank(
                    str(ctx.get("endToEndId")),
                    str(ctx.get("uetr")),
                    str(ctx.get("bankReference")));
        }
        return firstNonBlank(
                str(ctx.get("externalReference")),
                str(ctx.get("providerReference")),
                str(ctx.get("customerReference")));
    }

    private String statusLabel(String status) {
        if (!StringUtils.hasText(status)) {
            return "—";
        }
        return switch (status.trim().toUpperCase(Locale.ROOT)) {
            case "SUCCESS" -> msg("status.success");
            case "FAILED" -> msg("status.failed");
            case "CANCELLED" -> msg("status.cancelled");
            case "EXPIRED" -> msg("status.expired");
            case "WAITING_APPROVAL" -> msg("status.waiting_approval");
            case "AWAITING_USER_INPUT" -> msg("status.awaiting_input");
            case "OTP_REQUIRED" -> msg("status.otp");
            case "RETURNED_FOR_CORRECTION" -> msg("status.correction");
            case "PROCESSING", "PENDING" -> msg("status.processing");
            default -> status;
        };
    }

    private String railLabel(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        String key = code.trim().toUpperCase(Locale.ROOT);
        Map<String, String> localized = RAIL_LABELS.get(key);
        if (localized != null) {
            return i18n.resolveMessage(localized);
        }
        return humanizeCode(key);
    }

    private String msg(String key) {
        return i18n.resolveMessage(MESSAGES.get(key));
    }

    private static Map.Entry<String, Map<String, String>> entry(String code, String fr, String en) {
        return Map.entry(code, LocalizedMessages.of(fr, en));
    }

    private static final Map<String, Map<String, String>> MESSAGES = Map.ofEntries(
            entry("kind.transfer.out", "Transfert", "Transfer"),
            entry("kind.transfer.in", "Réception transfert", "Incoming transfer"),
            entry("kind.bill", "Paiement de facture", "Bill payment"),
            entry("kind.cash_out", "Retrait d’espèces", "Cash withdrawal"),
            entry("kind.cash_in", "Dépôt d’espèces", "Cash deposit"),
            entry("kind.airtime", "Achat crédit téléphone", "Airtime purchase"),
            entry("kind.merchant", "Paiement marchand", "Merchant payment"),
            entry("kind.rtgs", "Virement RTGS", "RTGS transfer"),
            entry("kind.swift", "Virement SWIFT international", "International SWIFT transfer"),
            entry("kind.remittance.in", "Réception diaspora", "Incoming remittance"),
            entry("kind.remittance.out", "Envoi diaspora", "Outgoing remittance"),
            entry("kind.link", "Liaison de compte", "Account linking"),
            entry("kind.saving", "Épargne / coffre", "Savings"),
            entry("kind.dues", "Cotisation", "Contribution"),
            entry("kind.operation", "Opération", "Operation"),
            entry("kind.receive.prefix", "Réception", "Incoming"),
            entry("product.transfer", "Transfert", "Transfer"),
            entry("product.bill", "Paiement de facture", "Bill payment"),
            entry("product.cash_out", "Retrait d’espèces", "Cash withdrawal"),
            entry("product.cash_in", "Dépôt d’espèces", "Cash deposit"),
            entry("product.airtime", "Achat crédit téléphone", "Airtime purchase"),
            entry("product.merchant", "Paiement marchand", "Merchant payment"),
            entry("product.rtgs", "Virement RTGS", "RTGS transfer"),
            entry("product.swift", "Virement SWIFT", "SWIFT transfer"),
            entry("product.remittance.in", "Réception diaspora", "Incoming remittance"),
            entry("product.remittance.out", "Envoi diaspora", "Outgoing remittance"),
            entry("product.receive", "Réception", "Incoming"),
            entry("party.to", "vers", "to"),
            entry("party.from", "de", "from"),
            entry("channel.via", "via", "via"),
            entry("ref.prefix", "Réf.", "Ref."),
            entry("status.success", "Validée", "Completed"),
            entry("status.failed", "Échouée", "Failed"),
            entry("status.cancelled", "Annulée", "Cancelled"),
            entry("status.expired", "Expirée", "Expired"),
            entry("status.waiting_approval", "En validation", "Pending approval"),
            entry("status.awaiting_input", "En attente", "Awaiting input"),
            entry("status.otp", "Code à saisir", "OTP required"),
            entry("status.correction", "À corriger", "Needs correction"),
            entry("status.processing", "En cours", "Processing")
    );

    private static boolean containsAny(String product, String... needles) {
        if (product == null || product.isEmpty()) {
            return false;
        }
        for (String n : needles) {
            if (product.contains(n)) {
                return true;
            }
        }
        return false;
    }

    private static String humanizeCode(String code) {
        String[] parts = code.split("[_-]+");
        StringBuilder sb = new StringBuilder();
        for (String w : parts) {
            if (w.isBlank()) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(w.charAt(0)));
            if (w.length() > 1) {
                sb.append(w.substring(1).toLowerCase(Locale.ROOT));
            }
        }
        return sb.toString();
    }

    private static String formatPhone(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() == 11 && digits.startsWith("226")) {
            return "+226 " + digits.substring(3, 5) + " " + digits.substring(5, 7)
                    + " " + digits.substring(7, 9) + " " + digits.substring(9);
        }
        if (digits.length() == 8) {
            return digits.substring(0, 2) + " " + digits.substring(2, 4)
                    + " " + digits.substring(4, 6) + " " + digits.substring(6);
        }
        return raw.trim();
    }

    private static String maskAccount(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String s = raw.replaceAll("\\s+", "");
        if (s.length() < 6) {
            return raw.trim();
        }
        if (s.length() >= 4 && Character.isLetter(s.charAt(0)) && Character.isLetter(s.charAt(1))) {
            return s.substring(0, 4) + " ···· " + s.substring(s.length() - 4);
        }
        return "···· " + s.substring(s.length() - 4);
    }

    private static String truncate(String s, int max) {
        String t = s.trim();
        return t.length() <= max ? t : t.substring(0, max - 1) + "…";
    }

    private static String str(Object v) {
        if (v instanceof String s && StringUtils.hasText(s)) {
            return s.trim();
        }
        if (v instanceof Number n) {
            return String.valueOf(n);
        }
        return null;
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

    private String timeShort(Instant instant) {
        Locale locale = Locale.forLanguageTag(i18n.currentLanguage());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm", locale).withZone(ZoneId.systemDefault());
        return fmt.format(instant);
    }
}
