package com.gateway.payment.configuration.repository;

import com.gateway.payment.common.domain.ServiceTypeCode;
import com.gateway.payment.configuration.domain.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceTypeRepository extends JpaRepository<ServiceType, ServiceTypeCode> {
    List<ServiceType> findByActifTrueOrderByOrdreAffichageAsc();
}
