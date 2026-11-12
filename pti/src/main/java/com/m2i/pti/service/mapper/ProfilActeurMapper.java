package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.ProfilActeurDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity ProfilActeur and its DTO ProfilActeurDTO.
 */
@Mapper(componentModel = "spring", uses = {HoraireAuthentificationMapper.class, TableauFraisMapper.class, TableauCumulMapper.class, TableauRepartitionCommissionMapper.class})
public interface ProfilActeurMapper extends EntityMapper<ProfilActeurDTO, ProfilActeur> {

    

    @Mapping(target = "organisations", ignore = true)
    @Mapping(target = "clients", ignore = true)
    ProfilActeur toEntity(ProfilActeurDTO profilActeurDTO);

    default ProfilActeur fromId(Long id) {
        if (id == null) {
            return null;
        }
        ProfilActeur profilActeur = new ProfilActeur();
        profilActeur.setId(id);
        return profilActeur;
    }
}
