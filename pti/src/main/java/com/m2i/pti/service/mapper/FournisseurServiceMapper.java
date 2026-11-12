package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.FournisseurServiceDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity FournisseurService and its DTO FournisseurServiceDTO.
 */
@Mapper(componentModel = "spring", uses = {PaysMapper.class})
public interface FournisseurServiceMapper extends EntityMapper<FournisseurServiceDTO, FournisseurService> {

    @Mapping(source = "pays.id", target = "paysId")
    @Mapping(source = "pays.libellePays", target = "paysLibellePays")
    FournisseurServiceDTO toDto(FournisseurService fournisseurService);

    @Mapping(target = "organisations", ignore = true)
    @Mapping(source = "paysId", target = "pays")
    FournisseurService toEntity(FournisseurServiceDTO fournisseurServiceDTO);

    default FournisseurService fromId(Long id) {
        if (id == null) {
            return null;
        }
        FournisseurService fournisseurService = new FournisseurService();
        fournisseurService.setId(id);
        return fournisseurService;
    }
}
