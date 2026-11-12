package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.TypeCompteDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity TypeCompte and its DTO TypeCompteDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface TypeCompteMapper extends EntityMapper<TypeCompteDTO, TypeCompte> {


    @Mapping(target = "comptes", ignore = true)
    TypeCompte toEntity(TypeCompteDTO typeCompteDTO);

    default TypeCompte fromId(Long id) {
        if (id == null) {
            return null;
        }
        TypeCompte typeCompte = new TypeCompte();
        typeCompte.setId(id);
        return typeCompte;
    }
}
