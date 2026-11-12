package com.gateway.payment.events.repository;

import com.gateway.payment.events.domain.DomainOutbox;
import com.gateway.payment.events.domain.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface DomainOutboxRepository extends JpaRepository<DomainOutbox, UUID> {

    @Query("""
            select o from DomainOutbox o
            where o.status = :status and o.nextAttemptAt <= :now
            order by o.createdAt asc
            """)
    List<DomainOutbox> findDue(OutboxStatus status, Instant now, Pageable pageable);
}
