package com.gateway.payment.divers.repository;

import com.gateway.payment.divers.domain.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    List<Budget> findByPartyIdOrderByCategorie_PriorityAsc(UUID partyId);

    Optional<Budget> findByIdAndPartyId(UUID id, UUID partyId);

    boolean existsByPartyIdAndCategorie_Code(UUID partyId, String categorieCode);
}
