package com.gateway.payment.identity.enrolement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EnrolementPassageRepository extends JpaRepository<EnrolementPassage, UUID> {
}
