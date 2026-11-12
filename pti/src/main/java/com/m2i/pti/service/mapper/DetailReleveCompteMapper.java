package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.DetailReleveCompteDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity DetailReleveCompte and its DTO DetailReleveCompteDTO.
 */
@Mapper(componentModel = "spring", uses = {ReleveCompteMapper.class})
public interface DetailReleveCompteMapper extends EntityMapper<DetailReleveCompteDTO, DetailReleveCompte> {

    @Mapping(source = "releve.id", target = "releveId")
    DetailReleveCompteDTO toDto(DetailReleveCompte detailReleveCompte); 

    @Mapping(source = "releveId", target = "releve")
    DetailReleveCompte toEntity(DetailReleveCompteDTO detailReleveCompteDTO);

    default DetailReleveCompte fromId(Long id) {
        if (id == null) {
            return null;
        }
        DetailReleveCompte detailReleveCompte = new DetailReleveCompte();
        detailReleveCompte.setId(id);
        return detailReleveCompte;
    }
}
