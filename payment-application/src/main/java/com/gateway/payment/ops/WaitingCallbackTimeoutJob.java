package com.gateway.payment.ops;

import com.gateway.payment.common.domain.PaymentStatus;
import com.gateway.payment.events.DomainEventPublisher;
import com.gateway.payment.events.DomainEventTypes;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.payment.repository.PaymentTransactionRepository;
import com.gateway.payment.settlement.PaymentSettlementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Expire les paiements bloqués en WAITING_CALLBACK au-delà du TTL ops.
 */
@Component
public class WaitingCallbackTimeoutJob {

    private static final Logger log = LoggerFactory.getLogger(WaitingCallbackTimeoutJob.class);

    private final PaymentTransactionRepository transactions;
    private final GatewayOpsProperties ops;
    private final DomainEventPublisher domainEvents;
    private final PaymentSettlementService settlement;

    public WaitingCallbackTimeoutJob(
            PaymentTransactionRepository transactions,
            GatewayOpsProperties ops,
            DomainEventPublisher domainEvents,
            PaymentSettlementService settlement
    ) {
        this.transactions = transactions;
        this.ops = ops;
        this.domainEvents = domainEvents;
        this.settlement = settlement;
    }

    @Scheduled(fixedDelayString = "${gateway.ops.wait-timeout-poll-ms:60000}")
    @Transactional
    public void expireStaleWaits() {
        if (!ops.waitTimeoutEnabled()) {
            return;
        }
        Instant cutoff = Instant.now().minus(ops.waitTimeoutMinutes(), ChronoUnit.MINUTES);
        List<PaymentTransaction> stale = transactions.findByStatusAndUpdatedAtBefore(
                PaymentStatus.WAITING_CALLBACK, cutoff);
        for (PaymentTransaction tx : stale) {
            if (tx.applyStatus(PaymentStatus.EXPIRED)) {
                tx.setCompletedAt(Instant.now());
                tx.setCurrentStep("FAILURE");
                tx.setErrorCode("WAIT_TIMEOUT");
                tx.setErrorMessage("Timeout attente callback (" + ops.waitTimeoutMinutes() + " min)");
                transactions.save(tx);
                domainEvents.paymentTerminal(tx, DomainEventTypes.PAYMENT_FAILED);
                settlement.onFailure(tx);
                log.info("WAIT timeout → EXPIRED {}", tx.getPaymentId());
            }
        }
    }
}
