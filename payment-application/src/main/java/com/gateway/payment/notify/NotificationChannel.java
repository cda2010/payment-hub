package com.gateway.payment.notify;

import java.util.Map;

/** Canal de notification (SMS, push…) — adapters branchés sur l'outbox. */
public interface NotificationChannel {

    String channel();

    boolean supports(String channelHint);

    void send(Map<String, Object> payload);
}
