package com.gateway.payment.fidelite.repository;

import com.gateway.payment.fidelite.domain.AvantageMonetaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AvantageMonetaireRepository extends JpaRepository<AvantageMonetaire, UUID> {
}
