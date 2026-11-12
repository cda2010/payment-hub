package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.Echange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EchangeRepository extends JpaRepository<Echange, UUID> {
}
