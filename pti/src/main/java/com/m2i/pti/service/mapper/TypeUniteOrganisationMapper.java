package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.TypeUniteOrganisationDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity TypeUniteOrganisation and its DTO TypeUniteOrganisationDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface TypeUniteOrganisationMapper extends EntityMapper<TypeUniteOrganisationDTO, TypeUniteOrganisation> {


   
    
    TypeUniteOrganisation toEntity(TypeUniteOrganisationDTO typeUniteOrganisationDTO);

    default TypeUniteOrganisation fromId(Long id) {
        if (id == null) {
            return null;
        }
        TypeUniteOrganisation typeUniteOrganisation = new TypeUniteOrganisation();
        typeUniteOrganisation.setId(id);
        return typeUniteOrganisation;
    }
}
