package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.ReleveCompteDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity ReleveCompte and its DTO ReleveCompteDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface ReleveCompteMapper extends EntityMapper<ReleveCompteDTO, ReleveCompte> {

    

    @Mapping(target = "detailReleveComptes", ignore = true)
    ReleveCompte toEntity(ReleveCompteDTO releveCompteDTO);

    default ReleveCompte fromId(Long id) {
        if (id == null) {
            return null;
        }
        ReleveCompte releveCompte = new ReleveCompte();
        releveCompte.setId(id);
        return releveCompte;
    }
}
