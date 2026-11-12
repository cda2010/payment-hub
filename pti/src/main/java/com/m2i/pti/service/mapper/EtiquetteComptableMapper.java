package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.EtiquetteComptableDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity EtiquetteComptable and its DTO EtiquetteComptableDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface EtiquetteComptableMapper extends EntityMapper<EtiquetteComptableDTO, EtiquetteComptable> {



    default EtiquetteComptable fromId(Long id) {
        if (id == null) {
            return null;
        }
        EtiquetteComptable etiquetteComptable = new EtiquetteComptable();
        etiquetteComptable.setId(id);
        return etiquetteComptable;
    }
}
