package com.gateway.payment.divers.repository;

import com.gateway.payment.divers.domain.Coffre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CoffreRepository extends JpaRepository<Coffre, UUID> {

    List<Coffre> findByOwnerUsernameOrderByCreatedAtDesc(String ownerUsername);

    Optional<Coffre> findByIdAndOwnerUsername(UUID id, String ownerUsername);
}
