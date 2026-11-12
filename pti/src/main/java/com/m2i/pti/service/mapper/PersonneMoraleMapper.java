package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.PersonneMoraleDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity PersonneMorale and its DTO PersonneMoraleDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface PersonneMoraleMapper extends EntityMapper<PersonneMoraleDTO, PersonneMorale> {



    default PersonneMorale fromId(Long id) {
        if (id == null) {
            return null;
        }
        PersonneMorale personneMorale = new PersonneMorale();
        personneMorale.setId(id);
        return personneMorale;
    }
}
