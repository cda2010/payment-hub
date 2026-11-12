package com.gateway.payment.transfer.repository;

import com.gateway.payment.transfer.domain.TransferRail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferRailRepository extends JpaRepository<TransferRail, UUID> {

    Optional<TransferRail> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<TransferRail> findAllByOrderByOrdreAscCodeAsc();

    List<TransferRail> findByActifTrueOrderByOrdreAscCodeAsc();
}
