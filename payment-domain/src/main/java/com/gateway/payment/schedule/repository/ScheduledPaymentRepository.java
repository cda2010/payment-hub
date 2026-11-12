package com.gateway.payment.schedule.repository;

import com.gateway.payment.schedule.domain.ScheduledPayment;
import com.gateway.payment.schedule.domain.ScheduledPaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduledPaymentRepository extends JpaRepository<ScheduledPayment, UUID> {

    List<ScheduledPayment> findByActifTrueOrderByNextRunAtAscCreatedAtDesc();

    Optional<ScheduledPayment> findByIdAndActifTrue(UUID id);

    List<ScheduledPayment> findByActifTrueAndStatusOrderByNextRunAtAsc(
            ScheduledPaymentStatus status
    );
}
