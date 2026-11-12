package com.gateway.payment.ops;

import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.payment.application.PaymentEngine;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.payment.repository.PaymentTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Tente un CHECK_STATUS provider sur les WAITING_CALLBACK anciens (recon basique).
 */
@Component
public class WaitingCallbackReconJob {

    private static final Logger log = LoggerFactory.getLogger(WaitingCallbackReconJob.class);

    private final PaymentTransactionRepository transactions;
    private final GatewayOpsProperties ops;
    private final PaymentEngine paymentEngine;

    public WaitingCallbackReconJob(
            PaymentTransactionRepository transactions,
            GatewayOpsProperties ops,
            @Lazy PaymentEngine paymentEngine
    ) {
        this.transactions = transactions;
        this.ops = ops;
        this.paymentEngine = paymentEngine;
    }

    @Scheduled(fixedDelayString = "${gateway.ops.recon-poll-ms:120000}")
    public void recon() {
        if (!ops.reconEnabled()) {
            return;
        }
        Instant cutoff = Instant.now().minus(ops.reconAfterMinutes(), ChronoUnit.MINUTES);
        Instant expireCut = Instant.now().minus(ops.waitTimeoutMinutes(), ChronoUnit.MINUTES);
        List<PaymentTransaction> candidates = transactions.findByStatusAndUpdatedAtBefore(
                PaymentStatus.WAITING_CALLBACK, cutoff);
        for (PaymentTransaction tx : candidates) {
            if (tx.getUpdatedAt() != null && tx.getUpdatedAt().isBefore(expireCut)) {
                continue; // laissé au timeout job
            }
            try {
                paymentEngine.reconWaitingCallback(tx.getPaymentId());
                log.debug("Recon CHECK_STATUS tenté pour {}", tx.getPaymentId());
            } catch (Exception e) {
                log.debug("Recon {} échoué: {}", tx.getPaymentId(), e.getMessage());
            }
        }
    }
}
