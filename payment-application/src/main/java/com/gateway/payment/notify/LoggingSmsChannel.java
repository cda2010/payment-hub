package com.gateway.payment.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

/**
 * Adaptateur SMS V1 : journalise la demande (branchement provider SMS ultérieur).
 */
@Component
public class LoggingSmsChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(LoggingSmsChannel.class);

    @Override
    public String channel() {
        return "SMS";
    }

    @Override
    public boolean supports(String channelHint) {
        if (channelHint == null || channelHint.isBlank()) {
            return true;
        }
        String h = channelHint.toUpperCase(Locale.ROOT);
        return h.contains("SMS") || h.contains("PHONE") || "ALL".equals(h);
    }

    @Override
    public void send(Map<String, Object> payload) {
        log.info("SMS notification requested paymentId={} phone={} status={} code={} body={}",
                payload.get("paymentId"),
                payload.get("phoneNumber"),
                payload.get("status"),
                payload.get("notificationCode"),
                payload.get("body"));
    }
}
