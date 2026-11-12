package com.m2i.pti.repository;

import com.m2i.pti.domain.TypePiece;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.Optional;


/**
 * Spring Data JPA repository for the TypePiece entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TypePieceRepository extends JpaRepository<TypePiece, Long> {
    Boolean existsByLibelleTypePiece(String libelle);
    Optional<TypePiece> findByLibelleTypePiece(String libelle);
    Optional<TypePiece> findByCodeTypePiece(String code);
}
