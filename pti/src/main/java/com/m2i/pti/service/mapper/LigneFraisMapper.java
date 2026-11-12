package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.LigneFraisDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity LigneFrais and its DTO LigneFraisDTO.
 */
@Mapper(componentModel = "spring", uses = {TableauFraisMapper.class})
public interface LigneFraisMapper extends EntityMapper<LigneFraisDTO, LigneFrais> {

    @Mapping(source = "tableauFrais.id", target = "tableauFraisId")
    LigneFraisDTO toDto(LigneFrais ligneFrais);

    @Mapping(source = "tableauFraisId", target = "tableauFrais")
    LigneFrais toEntity(LigneFraisDTO ligneFraisDTO);

    default LigneFrais fromId(Long id) {
        if (id == null) {
            return null;
        }
        LigneFrais ligneFrais = new LigneFrais();
        ligneFrais.setId(id);
        return ligneFrais;
    }
}
