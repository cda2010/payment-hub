package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.AutorisationDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Autorisation and its DTO AutorisationDTO.
 */
@Mapper(componentModel = "spring", uses = {UniteOrganisationMapper.class, TypeServiceMapper.class,ClientMapper.class})
public interface AutorisationMapper extends EntityMapper<AutorisationDTO, Autorisation> {

    @Mapping(source = "uniteOrganisation.id", target = "uniteOrganisationId")
    @Mapping(source = "uniteOrganisation.libelleUniteOrganisation", target = "uniteOrganisationLibelleUniteOrganisation")
    @Mapping(source = "typeService.id", target = "typeServiceId")
    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "typeService.libelle", target = "typeServiceLibelle")
    AutorisationDTO toDto(Autorisation autorisation);

    @Mapping(source = "uniteOrganisationId", target = "uniteOrganisation")
    @Mapping(source = "typeServiceId", target = "typeService")
    @Mapping(source = "clientId", target = "client")
    Autorisation toEntity(AutorisationDTO autorisationDTO);

    default Autorisation fromId(Long id) {
        if (id == null) {
            return null;
        }
        Autorisation autorisation = new Autorisation();
        autorisation.setId(id);
        return autorisation;
    }
}
