package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.CommissionDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Commission and its DTO CommissionDTO.
 */
@Mapper(componentModel = "spring", uses = {OperationMapper.class, UniteOrganisationMapper.class})
public interface CommissionMapper extends EntityMapper<CommissionDTO, Commission> {

    @Mapping(source = "operation.id", target = "operationId")
    @Mapping(source = "operation.codeOperation", target = "operationCodeOperation")
    @Mapping(source = "organisation.id", target = "organisationId")
    @Mapping(source = "organisation.libelleUniteOrganisation", target = "organisationLibelleUniteOrganisation")
    @Mapping(source= "operation.typeService.code", target = "typeService")
    @Mapping(source ="operation.montantOp", target= "montantOp")
    CommissionDTO toDto(Commission commission);

    @Mapping(source = "operationId", target = "operation")
    @Mapping(source = "organisationId", target = "organisation")
    Commission toEntity(CommissionDTO commissionDTO);

    default Commission fromId(Long id) {
        if (id == null) {
            return null;
        }
        Commission commission = new Commission();
        commission.setId(id);
        return commission;
    }
}
