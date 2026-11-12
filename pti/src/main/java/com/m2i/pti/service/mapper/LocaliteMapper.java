package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.LocaliteDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Localite and its DTO LocaliteDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface LocaliteMapper extends EntityMapper<LocaliteDTO, Localite> {


    default Localite fromId(Long id) {
        if (id == null) {
            return null;
        }
        Localite localite = new Localite();
        localite.setId(id);
        return localite;
    }
}
