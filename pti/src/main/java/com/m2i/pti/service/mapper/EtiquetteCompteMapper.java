package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.EtiquetteCompteDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity EtiquetteCompte and its DTO EtiquetteCompteDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface EtiquetteCompteMapper extends EntityMapper<EtiquetteCompteDTO, EtiquetteCompte> {



    default EtiquetteCompte fromId(Long id) {
        if (id == null) {
            return null;
        }
        EtiquetteCompte etiquetteCompte = new EtiquetteCompte();
        etiquetteCompte.setId(id);
        return etiquetteCompte;
    }
}
