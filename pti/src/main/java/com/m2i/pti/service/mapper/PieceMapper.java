package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.PieceDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Piece and its DTO PieceDTO.
 */
@Mapper(componentModel = "spring", uses = {PersonneMapper.class, TypePieceMapper.class})
public interface PieceMapper extends EntityMapper<PieceDTO, Piece> {

    @Mapping(source = "titulaire.id", target = "titulaireId")
    @Mapping(source = "typePiece.id", target = "typePieceId")
    @Mapping(source = "dateDelivrance", target = "dateDelivrance")
    @Mapping(source = "typePiece.libelleTypePiece", target = "typePieceLibelleTypePiece")
    PieceDTO toDto(Piece piece);

    @Mapping(source = "titulaireId", target = "titulaire")
    @Mapping(source = "typePieceId", target = "typePiece")
    Piece toEntity(PieceDTO pieceDTO);

    default Piece fromId(Long id) {
        if (id == null) {
            return null;
        }
        Piece piece = new Piece();
        piece.setId(id);
        return piece;
    }
}
