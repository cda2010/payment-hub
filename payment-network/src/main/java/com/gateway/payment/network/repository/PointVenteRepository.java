package com.gateway.payment.network.repository;

import com.gateway.payment.network.domain.PointVente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PointVenteRepository extends JpaRepository<PointVente, UUID> {

    List<PointVente> findByUniteIdOrderByCodeAsc(UUID uniteId);

    boolean existsByCode(String code);
}
