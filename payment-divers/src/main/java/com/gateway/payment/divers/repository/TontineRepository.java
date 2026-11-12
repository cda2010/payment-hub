package com.gateway.payment.divers.repository;

import com.gateway.payment.divers.domain.Tontine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TontineRepository extends JpaRepository<Tontine, UUID> {

    Optional<Tontine> findByInviteCodeIgnoreCase(String inviteCode);

    boolean existsByInviteCode(String inviteCode);
}
