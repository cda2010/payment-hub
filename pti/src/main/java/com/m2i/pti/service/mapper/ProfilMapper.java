package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.ProfilDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Profil and its DTO ProfilDTO.
 */
@Mapper(componentModel = "spring", uses = {UniteOrganisationMapper.class,TypeUniteOrganisationMapper.class})
public interface ProfilMapper extends EntityMapper<ProfilDTO, Profil> {

    @Mapping(source = "organisation.id", target = "organisationId")
    @Mapping(source = "typeUO.id", target = "typeUOId")
    @Mapping(source = "organisation.libelleUniteOrganisation", target = "organisationNom")
    @Mapping(source = "typeUO.libelleTypeUO", target = "typeUONom")
    ProfilDTO toDto(Profil profil);

    @Mapping(target = "users", ignore = true)
    @Mapping(source = "organisationId", target = "organisation")
    @Mapping(source = "typeUOId", target = "typeUO")
    Profil toEntity(ProfilDTO profilDTO);

    default Profil fromId(Long id) {
        if (id == null) {
            return null;
        }
        Profil profil = new Profil();
        profil.setId(id);
        return profil;
    }
}
