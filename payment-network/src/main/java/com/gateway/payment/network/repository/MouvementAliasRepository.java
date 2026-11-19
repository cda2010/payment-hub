package com.gateway.payment.network.repository;

import com.gateway.payment.network.domain.MouvementAlias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MouvementAliasRepository extends JpaRepository<MouvementAlias, UUID> {
}
