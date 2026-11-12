package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.GroupeProfilActeurDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity GroupeProfilActeur and its DTO GroupeProfilActeurDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface GroupeProfilActeurMapper extends EntityMapper<GroupeProfilActeurDTO, GroupeProfilActeur> {

    

    

    default GroupeProfilActeur fromId(Long id) {
        if (id == null) {
            return null;
        }
        GroupeProfilActeur groupeProfilActeur = new GroupeProfilActeur();
        groupeProfilActeur.setId(id);
        return groupeProfilActeur;
    }
}
