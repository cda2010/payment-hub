package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.RecepteurAlarmeDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity RecepteurAlarme and its DTO RecepteurAlarmeDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface RecepteurAlarmeMapper extends EntityMapper<RecepteurAlarmeDTO, RecepteurAlarme> {

    

    

    default RecepteurAlarme fromId(Long id) {
        if (id == null) {
            return null;
        }
        RecepteurAlarme recepteurAlarme = new RecepteurAlarme();
        recepteurAlarme.setId(id);
        return recepteurAlarme;
    }
}
