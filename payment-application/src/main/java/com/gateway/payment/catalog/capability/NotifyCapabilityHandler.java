package com.gateway.payment.catalog.capability;

import com.gateway.payment.catalog.i18n.CatalogConfigurationLocalizer;
import com.gateway.payment.events.DomainEventPublisher;
import com.gateway.payment.payment.application.TemplateMapper;
import com.gateway.payment.payment.domain.PaymentTransaction;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Notification produit : publie {@code notification.requested} dans l'outbox.
 * Corps / titre issus de {@code configuration.notifications} (i18n + templates),
 * avec repli sur un événement générique si le produit n'en définit pas.
 */
@Component
public class NotifyCapabilityHandler implements CapabilityHandler {

    private final SoftRuleCapabilitySupport softRules;
    private final DomainEventPublisher domainEvents;
    private final CatalogConfigurationLocalizer configurationLocalizer;
    private final TemplateMapper templateMapper;

    public NotifyCapabilityHandler(
            SoftRuleCapabilitySupport softRules,
            DomainEventPublisher domainEvents,
            CatalogConfigurationLocalizer configurationLocalizer,
            TemplateMapper templateMapper
    ) {
        this.softRules = softRules;
        this.domainEvents = domainEvents;
        this.configurationLocalizer = configurationLocalizer;
        this.templateMapper = templateMapper;
    }

    @Override
    public String code() {
        return CapabilityCodes.NOTIFY;
    }

    @Override
    public CapabilityResult execute(CapabilityExecutionContext context, Map<String, Object> configuration) {
        SoftRuleCapabilitySupport.SoftBundle soft = softRules.apply(context.step(), context.context());
        List<CapabilityResult.CapabilityEvent> events = new ArrayList<>(soft.events());
        events.add(new CapabilityResult.CapabilityEvent("NOTIFY", "Notification produit catalogue"));

        publishProductNotification(context, configuration);

        // Remittance : ne pas clôturer SUCCESS tant que le payout n'est pas confirmé
        Object remittancePending = context.context().get("availableForPayout");
        String catalog = context.context().get("catalogServiceCode") != null
                ? String.valueOf(context.context().get("catalogServiceCode"))
                : "";
        boolean deferSuccess = Boolean.TRUE.equals(remittancePending)
                || "true".equalsIgnoreCase(String.valueOf(remittancePending))
                || "REMITTANCE".equalsIgnoreCase(catalog);

        if (deferSuccess) {
            events.add(new CapabilityResult.CapabilityEvent(
                    "NOTIFY_DEFERRED", "Notification demandée — payout encore en attente"));
            return CapabilityResult.of(
                    soft.matched(),
                    events,
                    soft.vars(),
                    CapabilityResult.TerminalEffect.NONE,
                    null
            );
        }

        events.add(new CapabilityResult.CapabilityEvent("SUCCESS", "Transfert terminé (parcours catalogue)"));
        return CapabilityResult.of(
                soft.matched(),
                events,
                soft.vars(),
                CapabilityResult.TerminalEffect.SUCCESS,
                null
        );
    }

    private void publishProductNotification(
            CapabilityExecutionContext context,
            Map<String, Object> configuration
    ) {
        PaymentTransaction tx = context.transaction();
        Map<String, Object> binding = context.bindingParams() != null ? context.bindingParams() : Map.of();
        String eventCode = firstNonBlank(
                str(binding.get("notificationCode")),
                str(binding.get("event")),
                statusCode(tx),
                "SUCCESS");

        UUID versionId = versionId(context.context());
        List<Map<String, Object>> notifications = versionId != null
                ? configurationLocalizer.notificationsOf(versionId, configuration)
                : List.of();
        Map<String, Object> template = findNotification(notifications, eventCode);
        if (template == null && !"SUCCESS".equalsIgnoreCase(eventCode)) {
            template = findNotification(notifications, "SUCCESS");
        }

        String channelHint = firstNonBlank(
                template != null ? str(template.get("channelHint")) : null,
                str(binding.get("channelHint")),
                "SMS");

        Map<String, Object> extras = new LinkedHashMap<>();
        extras.put("notificationCode", eventCode);
        if (template != null) {
            Map<String, Object> vars = new LinkedHashMap<>(context.context());
            if (tx.getPaymentId() != null) {
                vars.putIfAbsent("paymentId", tx.getPaymentId());
            }
            if (tx.getAmount() != null) {
                vars.putIfAbsent("amount", tx.getAmount());
            }
            if (tx.getCurrency() != null) {
                vars.putIfAbsent("currency", tx.getCurrency());
            }
            if (tx.getPhoneNumber() != null) {
                vars.putIfAbsent("phoneNumber", tx.getPhoneNumber());
            }
            String body = str(template.get("body"));
            if (StringUtils.hasText(body)) {
                extras.put("body", templateMapper.apply(body, vars));
            }
            String title = str(template.get("title"));
            if (StringUtils.hasText(title)) {
                extras.put("title", templateMapper.apply(title, vars));
            }
        }

        domainEvents.notificationRequested(tx, channelHint, extras);
    }

    private static Map<String, Object> findNotification(List<Map<String, Object>> list, String code) {
        if (list == null || !StringUtils.hasText(code)) {
            return null;
        }
        String needle = code.trim().toUpperCase(Locale.ROOT);
        for (Map<String, Object> item : list) {
            String c = str(item.get("code"));
            if (c != null && c.equalsIgnoreCase(needle)) {
                return item;
            }
        }
        return null;
    }

    private static UUID versionId(Map<String, Object> ctx) {
        Object raw = ctx != null ? ctx.get("catalogServiceVersionId") : null;
        if (raw == null) {
            return null;
        }
        try {
            return UUID.fromString(String.valueOf(raw).trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static String statusCode(PaymentTransaction tx) {
        return tx != null && tx.getStatus() != null ? tx.getStatus().name() : null;
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

    private static String str(Object value) {
        if (value == null) {
            return null;
        }
        String s = String.valueOf(value).trim();
        return s.isEmpty() || "null".equals(s) ? null : s;
    }
}
