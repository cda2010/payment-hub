package com.gateway.payment.configuration.repository;

import com.gateway.payment.configuration.domain.Pays;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaysRepository extends JpaRepository<Pays, UUID> {

    Optional<Pays> findByCodeIgnoreCase(String code);

    Optional<Pays> findByNomIgnoreCase(String nom);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByNomIgnoreCase(String nom);

    List<Pays> findAllByOrderByOrdreAscNomAsc();
}
