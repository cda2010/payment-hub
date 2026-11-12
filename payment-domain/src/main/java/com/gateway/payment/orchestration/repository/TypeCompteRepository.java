package com.gateway.payment.orchestration.repository;

import com.gateway.payment.orchestration.domain.TypeCompte;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeCompteRepository extends JpaRepository<TypeCompte, String> {
}
