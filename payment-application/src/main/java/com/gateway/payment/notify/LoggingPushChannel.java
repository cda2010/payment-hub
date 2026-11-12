package com.gateway.payment.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

/**
 * Adaptateur push V1 : journalise la demande (FCM/APNs ultérieur).
 */
@Component
public class LoggingPushChannel implements NotificationChannel {

    private static final Logger log = LoggerFactory.getLogger(LoggingPushChannel.class);

    @Override
    public String channel() {
        return "PUSH";
    }

    @Override
    public boolean supports(String channelHint) {
        if (channelHint == null || channelHint.isBlank()) {
            return false;
        }
        String h = channelHint.toUpperCase(Locale.ROOT);
        return h.contains("PUSH") || h.contains("MOBILE") || "ALL".equals(h);
    }

    @Override
    public void send(Map<String, Object> payload) {
        log.info("PUSH notification requested paymentId={} customer={} status={}",
                payload.get("paymentId"), payload.get("customerReference"), payload.get("status"));
    }
}
