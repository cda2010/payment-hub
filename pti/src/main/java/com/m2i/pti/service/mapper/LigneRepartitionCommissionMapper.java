package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.LigneRepartitionCommissionDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity LigneRepartitionCommission and its DTO LigneRepartitionCommissionDTO.
 */
@Mapper(componentModel = "spring", uses = {TableauRepartitionCommissionMapper.class})
public interface LigneRepartitionCommissionMapper extends EntityMapper<LigneRepartitionCommissionDTO, LigneRepartitionCommission> {

    @Mapping(source = "tableauRepartitionCommission.id", target = "tableauRepartitionCommissionId")
    LigneRepartitionCommissionDTO toDto(LigneRepartitionCommission ligneRepartitionCommission);

    @Mapping(source = "tableauRepartitionCommissionId", target = "tableauRepartitionCommission")
    LigneRepartitionCommission toEntity(LigneRepartitionCommissionDTO ligneRepartitionCommissionDTO);

    default LigneRepartitionCommission fromId(Long id) {
        if (id == null) {
            return null;
        }
        LigneRepartitionCommission ligneRepartitionCommission = new LigneRepartitionCommission();
        ligneRepartitionCommission.setId(id);
        return ligneRepartitionCommission;
    }
}
