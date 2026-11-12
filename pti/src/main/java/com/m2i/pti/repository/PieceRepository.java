package com.m2i.pti.repository;

import com.m2i.pti.domain.Piece;
import com.m2i.pti.service.dto.PieceDTO;
import com.m2i.pti.service.dto.TypeServiceDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


/**
 * Spring Data JPA repository for the Piece entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PieceRepository extends JpaRepository<Piece, Long> {
    List<Piece> findByTypePieceIdAndReferencePiece(Long typeId, String reference);

    Optional<Piece> findTopByTypePieceIdAndReferencePiece(Long typeId, String reference);
   @Query("select p from Piece p where p.titulaire.id=?1")
    Piece findTopByTitulaireId(Long titulaireId);

   @Query("select p from Piece p where p.titulaire.id=?1")
   List<Piece>     findListPieceByTitulaireId(Long titulaireId); 
    
    
    @Query(name = "Piece.findTopByTitulaireId", nativeQuery = true)
    PieceDTO findTopByTitulaireIdNew(@Param("titulaireId") Long titulaireId);

    List<Piece> findByTitulaireIdAndTypePieceIdAndReferencePiece(Long titulaireId, Long typeId, String reference);

    List<Piece> findByTitulaireId(Long titulaireId);

    List<Piece> findByTypePieceId(Long typeId);

    @Query(value = "select reference_piece from piece where titulaire_id=:titulaireId",nativeQuery = true)
    List<String> findReferencePieceByTitulaireIdOptimise(@Param("titulaireId") Long titulaireId);


}
