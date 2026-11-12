package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.UoDeviseDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity UoDevise and its DTO UoDeviseDTO.
 */
@Mapper(componentModel = "spring", uses = {DeviseMapper.class, UniteOrganisationMapper.class})
public interface UoDeviseMapper extends EntityMapper<UoDeviseDTO, UoDevise> {
 @Mapping(source = "devise.code", target = "code")
    @Mapping(source = "devise.id", target = "deviseId")
    @Mapping(source = "devise.libelle", target = "deviseLibelle")
    @Mapping(source = "uniteOrganisation.id", target = "uniteOrganisationId")
    @Mapping(source = "uniteOrganisation.libelleUniteOrganisation", target = "libelleUniteOrganisation")
    UoDeviseDTO toDto(UoDevise uoDevise); 

    @Mapping(source = "deviseId", target = "devise")
    @Mapping(source = "uniteOrganisationId", target = "uniteOrganisation")
    UoDevise toEntity(UoDeviseDTO uoDeviseDTO);

    default UoDevise fromId(Long id) {
        if (id == null) {
            return null;
        }
        UoDevise uoDevise = new UoDevise();
        uoDevise.setId(id);
        return uoDevise;
    }
}
