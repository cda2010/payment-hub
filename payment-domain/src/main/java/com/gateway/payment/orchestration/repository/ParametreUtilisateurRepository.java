package com.gateway.payment.orchestration.repository;

import com.gateway.payment.orchestration.domain.ParametreUtilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParametreUtilisateurRepository extends JpaRepository<ParametreUtilisateur, UUID> {

    List<ParametreUtilisateur> findByInterfaceUtilisateurIdOrderByOrdreAscCodeParametreAsc(UUID interfaceId);

    Optional<ParametreUtilisateur> findByInterfaceUtilisateurIdAndCodeParametre(UUID interfaceId, String codeParametre);

    void deleteByInterfaceUtilisateurId(UUID interfaceId);
}
