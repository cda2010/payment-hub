package com.gateway.payment.transfer.repository;

import com.gateway.payment.transfer.domain.TransferBeneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransferBeneficiaryRepository extends JpaRepository<TransferBeneficiary, UUID> {

    List<TransferBeneficiary> findByActifTrueOrderByOrdreAffichageAscLabelAsc();
}
