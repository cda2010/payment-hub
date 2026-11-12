package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.HoraireAuthentificationDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity HoraireAuthentification and its DTO HoraireAuthentificationDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface HoraireAuthentificationMapper extends EntityMapper<HoraireAuthentificationDTO, HoraireAuthentification> {

    

    

    default HoraireAuthentification fromId(Long id) {
        if (id == null) {
            return null;
        }
        HoraireAuthentification horaireAuthentification = new HoraireAuthentification();
        horaireAuthentification.setId(id);
        return horaireAuthentification;
    }
}
