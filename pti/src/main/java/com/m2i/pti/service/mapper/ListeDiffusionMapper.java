package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.ListeDiffusionDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity ListeDiffusion and its DTO ListeDiffusionDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface ListeDiffusionMapper extends EntityMapper<ListeDiffusionDTO, ListeDiffusion> {

    

    @Mapping(target = "detailListeDiffusions", ignore = true)
    ListeDiffusion toEntity(ListeDiffusionDTO listeDiffusionDTO);

    default ListeDiffusion fromId(Long id) {
        if (id == null) {
            return null;
        }
        ListeDiffusion listeDiffusion = new ListeDiffusion();
        listeDiffusion.setId(id);
        return listeDiffusion;
    }
}
