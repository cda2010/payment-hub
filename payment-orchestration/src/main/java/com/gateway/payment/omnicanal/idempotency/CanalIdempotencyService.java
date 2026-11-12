package com.gateway.payment.omnicanal.idempotency;

import com.gateway.payment.common.concurrency.DistributedLockService;
import com.gateway.payment.common.config.GatewayPerformanceProperties;
import com.gateway.payment.omnicanal.response.CanalTransferResponse;
import com.gateway.payment.payment.domain.PaymentTransaction;
import com.gateway.payment.payment.repository.PaymentTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Idempotence canal via {@code Idempotency-Key} — DB + lock distribué (Redis multi-pods).
 */
@Service
public class CanalIdempotencyService {

    public static final String HEADER_IDEMPOTENCY = "Idempotency-Key";
    private static final String LOCK_PREFIX = "canal:idem:";

    private static final Logger log = LoggerFactory.getLogger(CanalIdempotencyService.class);

    private final PaymentTransactionRepository transactionRepository;
    private final DistributedLockService lockService;
    private final ConcurrentHashMap<String, Object> localLocks = new ConcurrentHashMap<>();
    private final int lockTtlSeconds;
    private final boolean redisEnabled;

    public CanalIdempotencyService(
            PaymentTransactionRepository transactionRepository,
            DistributedLockService lockService,
            @Autowired(required = false) GatewayPerformanceProperties performance
    ) {
        this.transactionRepository = transactionRepository;
        this.lockService = lockService;
        this.lockTtlSeconds = performance != null ? performance.getRedisLockTtlSeconds() : 120;
        this.redisEnabled = performance != null && performance.isRedisEnabled();
    }

    public Object lockFor(String key) {
        if (!StringUtils.hasText(key)) {
            return new Object();
        }
        if (redisEnabled) {
            String owner = Thread.currentThread().getName();
            boolean ok = lockService.tryLock(LOCK_PREFIX + key.trim(), owner, lockTtlSeconds);
            if (!ok) {
                // spin léger : le caller synchronise déjà sur l'objet retourné
                log.debug("Canal idempotency redis lock busy key={}", key);
            }
            return LOCK_PREFIX + key.trim();
        }
        return localLocks.computeIfAbsent(key, k -> new Object());
    }

    public void releaseLock(String key) {
        if (!StringUtils.hasText(key)) {
            return;
        }
        if (redisEnabled) {
            lockService.unlock(LOCK_PREFIX + key.trim(), Thread.currentThread().getName());
            return;
        }
        localLocks.remove(key);
    }

    public Optional<CanalTransferResponse> findCompletedResponse(String key) {
        if (!StringUtils.hasText(key)) {
            return Optional.empty();
        }
        return transactionRepository.findByIdempotencyKey(key.trim())
                .filter(tx -> tx.getCompletedAt() != null
                        || (tx.getStatus() != null && "SUCCESS".equals(tx.getStatus().name())))
                .map(tx -> {
                    CanalTransferResponse replay = CanalTransferResponse.ok("IDEMPOTENT_REPLAY");
                    replay.setPaymentId(tx.getPaymentId());
                    replay.setCodeOperation(tx.getProviderReference());
                    replay.setText(tx.getContextJson());
                    log.info("Canal idempotency hit key={} paymentId={}", key, tx.getPaymentId());
                    return replay;
                });
    }

    public boolean hasInFlight(String key) {
        if (!StringUtils.hasText(key)) {
            return false;
        }
        Optional<PaymentTransaction> tx = transactionRepository.findByIdempotencyKey(key.trim());
        if (tx.isEmpty()) {
            return false;
        }
        PaymentTransaction t = tx.get();
        return t.getCompletedAt() == null
                && t.getStatus() != null
                && !"FAILED".equals(t.getStatus().name())
                && !"CANCELLED".equals(t.getStatus().name())
                && !"SUCCESS".equals(t.getStatus().name());
    }
}
