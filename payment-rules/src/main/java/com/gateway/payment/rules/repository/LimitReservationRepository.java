package com.gateway.payment.rules.repository;

import com.gateway.payment.rules.domain.LimitReservation;
import com.gateway.payment.rules.domain.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface LimitReservationRepository extends JpaRepository<LimitReservation, UUID> {
    List<LimitReservation> findByStatusAndExpiresAtBefore(ReservationStatus status, Instant before);
    List<LimitReservation> findByTransactionIdAndStatus(String transactionId, ReservationStatus status);
}
