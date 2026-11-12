package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.TypePieceDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity TypePiece and its DTO TypePieceDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface TypePieceMapper extends EntityMapper<TypePieceDTO, TypePiece> {


    @Mapping(target = "pieces", ignore = true)
    TypePiece toEntity(TypePieceDTO typePieceDTO);

    default TypePiece fromId(Long id) {
        if (id == null) {
            return null;
        }
        TypePiece typePiece = new TypePiece();
        typePiece.setId(id);
        return typePiece;
    }
}
