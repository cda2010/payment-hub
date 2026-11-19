package com.gateway.payment.identity.enrolement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EnrolementRepository extends JpaRepository<Enrolement, UUID> {

    List<Enrolement> findByPartyIdOrderByUpdatedAtDesc(UUID partyId);
}
