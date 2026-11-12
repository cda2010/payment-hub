package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.TaxeOperationDTO;
import com.m2i.pti.service.dto.VilleDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Ville and its DTO VilleDTO.
 */
@Mapper(componentModel = "spring", uses = {UniteOrganisationMapper.class})
public interface VilleMapper extends EntityMapper<VilleDTO, Ville> {


    @Mapping(source = "pays.libelleUniteOrganisation", target = "libellePays")
    @Mapping(source = "pays.id", target = "paysId")
    VilleDTO toDto(Ville ville);

    @Mapping(source = "paysId", target = "pays")
    Ville toEntity(VilleDTO villeDTO);

    default Ville fromId(Long id) {
        if (id == null) {
            return null;
        }
        Ville ville = new Ville();
        ville.setId(id);
        return ville;
    }
}
