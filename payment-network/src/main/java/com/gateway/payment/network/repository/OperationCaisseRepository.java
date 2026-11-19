package com.gateway.payment.network.repository;

import com.gateway.payment.network.domain.OperationCaisse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OperationCaisseRepository extends JpaRepository<OperationCaisse, UUID> {
}
