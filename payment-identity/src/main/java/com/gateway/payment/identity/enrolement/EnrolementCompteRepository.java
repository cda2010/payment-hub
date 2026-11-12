package com.gateway.payment.identity.enrolement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EnrolementCompteRepository extends JpaRepository<EnrolementCompte, UUID> {

    List<EnrolementCompte> findByTenantIdAndNumero(String tenantId, String numero);

    boolean existsByTenantIdAndMaisonAndNumero(String tenantId, String maison, String numero);
}
