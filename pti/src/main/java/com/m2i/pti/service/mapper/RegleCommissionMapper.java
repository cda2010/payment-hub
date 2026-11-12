package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.RegleCommissionDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity RegleCommission and its DTO RegleCommissionDTO.
 */
@Mapper(componentModel = "spring", uses = {TypeServiceMapper.class, UniteOrganisationMapper.class})
public interface RegleCommissionMapper extends EntityMapper<RegleCommissionDTO, RegleCommission> {

    @Mapping(source = "typeService.id", target = "typeServiceId")
    @Mapping(source = "teneurSrc.id", target = "teneurSrcId")
    @Mapping(source = "teneurDest.id", target = "teneurDestId")
    @Mapping(source = "teneurSrc.libelleUniteOrganisation", target = "teneurSrcLibelle")
    @Mapping(source = "teneurDest.libelleUniteOrganisation", target = "teneurDestLibelle")
    @Mapping(source = "uoSrc.id", target = "uoSrcId")
    @Mapping(source = "uoDest.id", target = "uoDestId")
    @Mapping(source = "uoSrc.libelleUniteOrganisation", target = "uoSrcLibelle")
    @Mapping(source = "uoDest.libelleUniteOrganisation", target = "uoDestLibelle")
    @Mapping(source = "typeService.libelle", target = "typeServiceLibelle")
    @Mapping(source = "priseEnCharge.id", target = "priseEnChargeId")
    @Mapping(source = "priseEnCharge.libelleUniteOrganisation", target = "priseEnChargeLibelle")
    RegleCommissionDTO toDto(RegleCommission regleCommission);

    @Mapping(source = "priseEnChargeId", target = "priseEnCharge")
    @Mapping(source = "typeServiceId", target = "typeService")
    @Mapping(source = "teneurSrcId", target = "teneurSrc")
    @Mapping(source = "teneurDestId", target = "teneurDest")
    @Mapping(source = "uoSrcId", target = "uoSrc")
    @Mapping(source = "uoDestId", target = "uoDest")
    RegleCommission toEntity(RegleCommissionDTO regleCommissionDTO);

    default RegleCommission fromId(Long id) {
        if (id == null) {
            return null;
        }
        RegleCommission regleCommission = new RegleCommission();
        regleCommission.setId(id);
        return regleCommission;
    }
}
