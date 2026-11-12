package com.gateway.payment.pricing.tarif.repository;

import com.gateway.payment.pricing.tarif.domain.Formule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FormuleRepository extends JpaRepository<Formule, UUID> {

    Optional<Formule> findByCodeAndVersion(String code, int version);

    List<Formule> findAllByOrderByCodeAscVersionAsc();
}
