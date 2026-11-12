package com.gateway.payment.pricing.repository;

import com.gateway.payment.pricing.domain.PricingRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PricingRateRepository extends JpaRepository<PricingRate, UUID> {
    Optional<PricingRate> findByCodeAndActiveTrue(String code);

    List<PricingRate> findByBaseCurrencyAndQuoteCurrencyAndActiveTrue(
            String baseCurrency, String quoteCurrency);

    List<PricingRate> findByActiveTrueOrderByCodeAsc();
}
