package com.gateway.payment.omnicanal.concurrency;

import com.gateway.payment.common.concurrency.DistributedLockService;
import com.gateway.payment.common.config.GatewayPerformanceProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Claim step — mémoire locale + lock distribué (Redis si activé) pour multi-pods.
 */
@Service
public class StepClaimService {

    private static final String CLAIM_PREFIX = "step:claim:";
    private static final String DONE_PREFIX = "step:done:";

    private final Map<String, String> claims = new ConcurrentHashMap<>();
    private final Map<String, StepStateMachine> machines = new ConcurrentHashMap<>();
    private final DistributedLockService lockService;
    private final int lockTtlSeconds;
    private final boolean redisEnabled;

    public StepClaimService(
            DistributedLockService lockService,
            @Autowired(required = false) GatewayPerformanceProperties performance
    ) {
        this.lockService = lockService;
        this.lockTtlSeconds = performance != null ? performance.getRedisLockTtlSeconds() : 120;
        this.redisEnabled = performance != null && performance.isRedisEnabled();
    }

    public StepClaimResult tryClaim(String paymentId, String stepKey, String workerId) {
        String key = paymentId + "::" + stepKey;
        StepStateMachine sm = machines.computeIfAbsent(paymentId, k -> new StepStateMachine());
        if (sm.isDone(stepKey) || (redisEnabled && lockService.get(DONE_PREFIX + key) != null)) {
            return StepClaimResult.ALREADY_DONE;
        }
        String owner = workerId != null ? workerId : "default";
        if (redisEnabled) {
            boolean acquired = lockService.putIfAbsent(CLAIM_PREFIX + key, owner, lockTtlSeconds);
            if (!acquired) {
                String current = lockService.get(CLAIM_PREFIX + key);
                if (current != null && !current.equals(owner)) {
                    return StepClaimResult.BUSY;
                }
            }
        }
        String prev = claims.putIfAbsent(key, owner);
        if (prev != null && !prev.equals(owner)) {
            return StepClaimResult.BUSY;
        }
        return StepClaimResult.CLAIMED;
    }

    public void release(String paymentId, String stepKey) {
        String key = paymentId + "::" + stepKey;
        claims.remove(key);
        if (redisEnabled) {
            lockService.delete(CLAIM_PREFIX + key);
        }
    }

    public void markDone(String paymentId, String stepKey) {
        String key = paymentId + "::" + stepKey;
        if (redisEnabled) {
            lockService.set(DONE_PREFIX + key, "1", lockTtlSeconds);
            lockService.delete(CLAIM_PREFIX + key);
        }
        claims.remove(key);
    }

    public StepStateMachine machine(String paymentId) {
        return machines.computeIfAbsent(paymentId, k -> new StepStateMachine());
    }
}
