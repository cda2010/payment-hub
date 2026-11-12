package com.m2i.pti.repository;

import com.m2i.pti.domain.TypeReclamation;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data  repository for the TypeReclamation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TypeReclamationRepository extends JpaRepository<TypeReclamation, Long> {
    Optional<TypeReclamation> findById(Long id);

    void deleteById(Long id);
}
