package com.gateway.payment.document.repository;

import com.gateway.payment.document.domain.DocumentProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DocumentProfileRepository extends JpaRepository<DocumentProfileEntity, UUID> {

    Optional<DocumentProfileEntity> findByCodeIgnoreCaseAndActifTrue(String code);

    Optional<DocumentProfileEntity> findByCodeIgnoreCase(String code);
}
