package com.gateway.payment.network.repository;

import com.gateway.payment.network.domain.PositionGuichet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PositionGuichetRepository extends JpaRepository<PositionGuichet, UUID> {
}
