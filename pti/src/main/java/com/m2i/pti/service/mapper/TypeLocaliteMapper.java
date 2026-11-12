package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.TypeLocaliteDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity TypeLocalite and its DTO TypeLocaliteDTO.
 */
@Mapper(componentModel = "spring", uses = {UniteOrganisationMapper.class})
public interface TypeLocaliteMapper extends EntityMapper<TypeLocaliteDTO, TypeLocalite> {

    @Mapping(source = "pays.libelleUniteOrganisation", target = "libellePays")
    @Mapping(source = "pays.id", target = "paysId")
    TypeLocaliteDTO toDto(TypeLocalite typeLocalite); 

    @Mapping(source = "paysId", target = "pays")
    TypeLocalite toEntity(TypeLocaliteDTO typeLocaliteDTO);

    default TypeLocalite fromId(Long id) {
        if (id == null) {
            return null;
        }
        TypeLocalite typeLocalite = new TypeLocalite();
        typeLocalite.setId(id);
        return typeLocalite;
    }
}
