package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.PointVenteDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity PointVente and its DTO PointVenteDTO.
 */
@Mapper(componentModel = "spring", uses = {UniteOrganisationMapper.class})
public interface PointVenteMapper extends EntityMapper<PointVenteDTO, PointVente> {

    @Mapping(source = "uniteOrganisation.id", target = "uniteOrganisationId")
    @Mapping(source = "uniteOrganisation.libelleUniteOrganisation", target = "uniteOrganisationLibelleUniteOrganisation")
    PointVenteDTO toDto(PointVente pointVente);

 
    @Mapping(source = "uniteOrganisationId", target = "uniteOrganisation.id")
    @Mapping(source = "typeUoId", target = "uniteOrganisation.typeUO.id")
    @Mapping(source = "libelleUniteOrganisation", target = "uniteOrganisation.libelleUniteOrganisation")
    @Mapping(source = "typeUOCode", target = "uniteOrganisation.typeUO.codeTypeUO")
    @Mapping(source = "niveauTypeUo", target = "uniteOrganisation.typeUO.niveau")
    PointVente toEntity(PointVenteDTO pointVenteDTO);

    default PointVente fromId(Long id) {
        if (id == null) {
            return null;
        }
        PointVente pointVente = new PointVente();
        pointVente.setId(id);
        return pointVente;
    }
}
