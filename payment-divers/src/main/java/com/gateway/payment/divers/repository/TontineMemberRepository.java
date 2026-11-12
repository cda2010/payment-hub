package com.gateway.payment.divers.repository;

import com.gateway.payment.divers.domain.TontineMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TontineMemberRepository extends JpaRepository<TontineMember, UUID> {

    List<TontineMember> findByUsernameOrderByTontine_CreatedAtDesc(String username);

    List<TontineMember> findByTontineIdOrderByTurnNoAsc(UUID tontineId);

    Optional<TontineMember> findByTontineIdAndUsername(UUID tontineId, String username);

    int countByTontineId(UUID tontineId);

    boolean existsByTontineIdAndUsername(UUID tontineId, String username);
}
