package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.mobile.dto.MLigneChargement;
import com.m2i.pti.service.dto.CompteAutreDTO;
import com.m2i.pti.service.dto.LienDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity CompteAutre and its DTO CompteAutreDTO.
 */
@Mapper(componentModel = "spring", uses = {ChargementTransfertMapper.class})
public interface LigneChargementMapper extends EntityMapper<MLigneChargement, LigneChargementTransfert> {

    @Mapping(source = "id", target = "ligneChargementTransfertId")
    @Mapping(source = "chargementTransfert.codePays", target = "codePays")
    @Mapping(source = "chargementTransfert.motifOperation", target = "motif")
    @Mapping(source = "chargementTransfert.typeTransfert", target = "typeTransfert")
    @Mapping(source = "chargementTransfert.reference", target = "reference")
    @Mapping(source = "chargementTransfert.id", target = "chargementId")
    MLigneChargement toDto(LigneChargementTransfert ligneChargementTransfert);

    @Mapping(source = "chargementId", target = "chargementTransfert")
     @Mapping(source = "ligneChargementTransfertId", target = "id")
    LigneChargementTransfert toEntity(MLigneChargement mLigneChargement);
}
