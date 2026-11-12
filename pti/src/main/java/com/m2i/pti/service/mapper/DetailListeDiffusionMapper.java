package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.DetailListeDiffusionDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity DetailListeDiffusion and its DTO DetailListeDiffusionDTO.
 */
@Mapper(componentModel = "spring", uses = {ListeDiffusionMapper.class})
public interface DetailListeDiffusionMapper extends EntityMapper<DetailListeDiffusionDTO, DetailListeDiffusion> {

    @Mapping(source = "liste.id", target = "listeId")
    DetailListeDiffusionDTO toDto(DetailListeDiffusion detailListeDiffusion); 

    @Mapping(source = "listeId", target = "liste")
    DetailListeDiffusion toEntity(DetailListeDiffusionDTO detailListeDiffusionDTO);

    default DetailListeDiffusion fromId(Long id) {
        if (id == null) {
            return null;
        }
        DetailListeDiffusion detailListeDiffusion = new DetailListeDiffusion();
        detailListeDiffusion.setId(id);
        return detailListeDiffusion;
    }
}
