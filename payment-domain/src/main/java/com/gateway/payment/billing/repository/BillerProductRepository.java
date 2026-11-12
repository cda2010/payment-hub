package com.gateway.payment.billing.repository;

import com.gateway.payment.billing.domain.BillerProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillerProductRepository extends JpaRepository<BillerProduct, UUID> {

    List<BillerProduct> findByBillerIdOrderByOrdreAffichageAscNameAsc(UUID billerId);

    List<BillerProduct> findByBillerIdAndActifTrueOrderByOrdreAffichageAscNameAsc(UUID billerId);

    Optional<BillerProduct> findByBiller_CodeIgnoreCaseAndCodeIgnoreCase(String billerCode, String productCode);

    boolean existsByBillerIdAndCodeIgnoreCase(UUID billerId, String code);
}
