package com.gateway.payment.network.repository;

import com.gateway.payment.network.domain.UniteUtilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UniteUtilisateurRepository extends JpaRepository<UniteUtilisateur, UUID> {

    List<UniteUtilisateur> findByUniteIdOrderByNomAsc(UUID uniteId);

    boolean existsByUniteIdAndIdentifiant(UUID uniteId, String identifiant);

    boolean existsByIdentifiantIgnoreCase(String identifiant);

    List<UniteUtilisateur> findAllByIdentifiantIgnoreCase(String identifiant);

    boolean existsByUniteIdAndRoleAndStatut(UUID uniteId, String role, String statut);

    Optional<UniteUtilisateur> findByIdAndUniteId(UUID id, UUID uniteId);
}
