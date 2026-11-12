package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.NationaliteDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Nationalite and its DTO NationaliteDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface NationaliteMapper extends EntityMapper<NationaliteDTO, Nationalite> {


    @Mapping(target = "personnePhysiques", ignore = true)
    Nationalite toEntity(NationaliteDTO nationaliteDTO);

    default Nationalite fromId(Long id) {
        if (id == null) {
            return null;
        }
        Nationalite nationalite = new Nationalite();
        nationalite.setId(id);
        return nationalite;
    }
}
