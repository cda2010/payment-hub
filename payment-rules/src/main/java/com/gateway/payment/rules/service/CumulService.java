package com.gateway.payment.rules.service;

import com.gateway.payment.rules.domain.*;
import com.gateway.payment.rules.moteur.DemandeEvaluation;
import com.gateway.payment.rules.moteur.PeriodKeys;
import com.gateway.payment.rules.moteur.ScopeKeys;
import com.gateway.payment.rules.repository.LimitCounterRepository;
import com.gateway.payment.rules.repository.LimitReservationRepository;
import com.gateway.payment.rules.source.CumulSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CumulService {

    private final LimitCounterRepository counters;
    private final LimitReservationRepository reservations;
    private final Clock clock;
    private final long ttlSeconds;

    @Autowired
    public CumulService(
            LimitCounterRepository counters,
            LimitReservationRepository reservations,
            @Value("${rules.reservation.ttl-seconds:300}") long ttlSeconds
    ) {
        this.counters = counters;
        this.reservations = reservations;
        this.clock = Clock.systemUTC();
        this.ttlSeconds = ttlSeconds;
    }

    /** Constructeur tests. */
    CumulService(
            LimitCounterRepository counters,
            LimitReservationRepository reservations,
            Clock clock,
            long ttlSeconds
    ) {
        this.counters = counters;
        this.reservations = reservations;
        this.clock = clock;
        this.ttlSeconds = ttlSeconds;
    }

    public CumulSnapshot snapshot(String limitCode, LimitScope scope, LimitPeriod period, DemandeEvaluation d) {
        String scopeKey = ScopeKeys.key(scope, d);
        String periodKey = PeriodKeys.key(period, clock);
        return counters.findByLimitCodeAndScopeKeyAndPeriodKey(limitCode, scopeKey, periodKey)
                .map(c -> new CumulSnapshot(c.getAmountSum(), c.getCountSum(), c.getReservedAmount(), c.getReservedCount()))
                .orElse(CumulSnapshot.empty());
    }

    /**
     * Réserve atomiquement la capacité. Retourne null si le plafond serait dépassé.
     */
    @Transactional
    public UUID tryReserve(
            String limitCode,
            LimitScope scope,
            LimitPeriod period,
            DemandeEvaluation d,
            BigDecimal amount,
            BigDecimal limitValue,
            boolean amountMetric
    ) {
        String scopeKey = ScopeKeys.key(scope, d);
        String periodKey = PeriodKeys.key(period, clock);
        LimitCounter counter = counters.findForUpdate(limitCode, scopeKey, periodKey)
                .orElseGet(() -> counters.save(LimitCounter.builder()
                        .limitCode(limitCode)
                        .scopeKey(scopeKey)
                        .periodKey(periodKey)
                        .amountSum(BigDecimal.ZERO)
                        .countSum(0)
                        .reservedAmount(BigDecimal.ZERO)
                        .reservedCount(0)
                        .build()));

        BigDecimal addAmount = amountMetric ? (amount == null ? BigDecimal.ZERO : amount) : BigDecimal.ZERO;
        int addCount = amountMetric ? 0 : 1;

        BigDecimal projected = counter.getAmountSum().add(counter.getReservedAmount()).add(addAmount);
        int projectedCount = counter.getCountSum() + counter.getReservedCount() + addCount;

        if (amountMetric && limitValue != null && projected.compareTo(limitValue) > 0) {
            return null;
        }
        if (!amountMetric && limitValue != null && projectedCount > limitValue.intValue()) {
            return null;
        }

        counter.setReservedAmount(counter.getReservedAmount().add(addAmount));
        counter.setReservedCount(counter.getReservedCount() + addCount);
        counters.save(counter);

        LimitReservation res = LimitReservation.builder()
                .transactionId(d.transactionId())
                .limitCode(limitCode)
                .scopeKey(scopeKey)
                .periodKey(periodKey)
                .amount(addAmount)
                .countUnits(Math.max(addCount, amountMetric ? 0 : 1))
                .status(ReservationStatus.HELD)
                .expiresAt(Instant.now(clock).plusSeconds(ttlSeconds))
                .build();
        return reservations.save(res).getId();
    }

    @Transactional
    public void confirm(UUID reservationId) {
        LimitReservation res = reservations.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable: " + reservationId));
        if (res.getStatus() != ReservationStatus.HELD) {
            throw new IllegalStateException("Réservation non HELD: " + res.getStatus());
        }
        LimitCounter counter = counters.findForUpdate(res.getLimitCode(), res.getScopeKey(), res.getPeriodKey())
                .orElseThrow(() -> new IllegalStateException("Compteur introuvable pour réservation"));
        counter.setAmountSum(counter.getAmountSum().add(res.getAmount()));
        counter.setCountSum(counter.getCountSum() + res.getCountUnits());
        counter.setReservedAmount(counter.getReservedAmount().subtract(res.getAmount()).max(BigDecimal.ZERO));
        counter.setReservedCount(Math.max(0, counter.getReservedCount() - res.getCountUnits()));
        counters.save(counter);
        res.setStatus(ReservationStatus.CONFIRMED);
        res.setConfirmedAt(Instant.now(clock));
        reservations.save(res);
    }

    @Transactional
    public void release(UUID reservationId) {
        LimitReservation res = reservations.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Réservation introuvable: " + reservationId));
        if (res.getStatus() != ReservationStatus.HELD) {
            return;
        }
        counters.findForUpdate(res.getLimitCode(), res.getScopeKey(), res.getPeriodKey()).ifPresent(counter -> {
            counter.setReservedAmount(counter.getReservedAmount().subtract(res.getAmount()).max(BigDecimal.ZERO));
            counter.setReservedCount(Math.max(0, counter.getReservedCount() - res.getCountUnits()));
            counters.save(counter);
        });
        res.setStatus(ReservationStatus.RELEASED);
        res.setReleasedAt(Instant.now(clock));
        reservations.save(res);
    }

    @Transactional
    public int confirmByTransaction(String transactionId) {
        List<LimitReservation> held = reservations.findByTransactionIdAndStatus(transactionId, ReservationStatus.HELD);
        for (LimitReservation res : held) {
            confirm(res.getId());
        }
        return held.size();
    }

    @Transactional
    public int releaseByTransaction(String transactionId) {
        List<LimitReservation> held = reservations.findByTransactionIdAndStatus(transactionId, ReservationStatus.HELD);
        for (LimitReservation res : held) {
            release(res.getId());
        }
        return held.size();
    }

    @Transactional
    public int expireHeld() {
        List<LimitReservation> expired = reservations
                .findByStatusAndExpiresAtBefore(ReservationStatus.HELD, Instant.now(clock));
        for (LimitReservation res : expired) {
            releaseExpired(res);
        }
        return expired.size();
    }

    private void releaseExpired(LimitReservation res) {
        counters.findForUpdate(res.getLimitCode(), res.getScopeKey(), res.getPeriodKey()).ifPresent(counter -> {
            counter.setReservedAmount(counter.getReservedAmount().subtract(res.getAmount()).max(BigDecimal.ZERO));
            counter.setReservedCount(Math.max(0, counter.getReservedCount() - res.getCountUnits()));
            counters.save(counter);
        });
        res.setStatus(ReservationStatus.EXPIRED);
        res.setReleasedAt(Instant.now(clock));
        reservations.save(res);
    }

    /** Prépare les réservations à créer après un ALLOW (une par règle cumul). */
    public List<PendingReservation> pendingFor(DemandeEvaluation d, String limitCode, LimitScope scope,
                                               LimitPeriod period, BigDecimal amount, boolean amountMetric) {
        List<PendingReservation> list = new ArrayList<>();
        list.add(new PendingReservation(limitCode, scope, period, amount, amountMetric));
        return list;
    }

    public record PendingReservation(
            String limitCode, LimitScope scope, LimitPeriod period, BigDecimal amount, boolean amountMetric
    ) {}
}
