package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.BrancheActiviteDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity BrancheActivite and its DTO BrancheActiviteDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface BrancheActiviteMapper extends EntityMapper<BrancheActiviteDTO, BrancheActivite> {



    default BrancheActivite fromId(Long id) {
        if (id == null) {
            return null;
        }
        BrancheActivite brancheActivite = new BrancheActivite();
        brancheActivite.setId(id);
        return brancheActivite;
    }
}
