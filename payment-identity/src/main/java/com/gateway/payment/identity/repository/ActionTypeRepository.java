package com.gateway.payment.identity.repository;

import com.gateway.payment.identity.domain.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActionTypeRepository extends JpaRepository<ActionType, String> {
}
