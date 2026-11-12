package com.gateway.payment.transfer.repository;

import com.gateway.payment.transfer.domain.FeePolicyType;
import com.gateway.payment.transfer.domain.TransferFeePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferFeePolicyRepository extends JpaRepository<TransferFeePolicy, UUID> {

    Optional<TransferFeePolicy> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<TransferFeePolicy> findAllByOrderByOrdreAscCodeAsc();

    @Query("""
            select p from TransferFeePolicy p
            join fetch p.feeRule
            where p.actif = true
              and upper(p.countryCode) = upper(:countryCode)
            order by p.ordre asc
            """)
    List<TransferFeePolicy> findActiveByCountry(String countryCode);
}
