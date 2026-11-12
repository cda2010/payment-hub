package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.TableauRepartitionCommissionDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity TableauRepartitionCommission and its DTO TableauRepartitionCommissionDTO.
 */
@Mapper(componentModel = "spring", uses = {TypeServiceMapper.class,UniteOrganisationMapper.class})
public interface TableauRepartitionCommissionMapper extends EntityMapper<TableauRepartitionCommissionDTO, TableauRepartitionCommission> {

    @Mapping(source = "typeService.id", target = "typeServiceId")
    @Mapping(source = "typeService.libelle", target = "typeServiceLibelle")
    @Mapping(source = "organisation.id", target = "organisationId")
    @Mapping(source = "group.id", target = "groupId")
    @Mapping(source = "group.libelleGroup", target = "groupLibelle")
    @Mapping(source = "organisation.libelleUniteOrganisation", target = "organisationLibelle")
    TableauRepartitionCommissionDTO toDto(TableauRepartitionCommission tableauRepartitionCommission);

    @Mapping(target = "ligneRepartitionCommissions", ignore = true)
    @Mapping(source = "typeServiceId", target = "typeService")
    @Mapping(source = "organisationId", target = "organisation")
    @Mapping(source = "groupId", target = "group.id")
    TableauRepartitionCommission toEntity(TableauRepartitionCommissionDTO tableauRepartitionCommissionDTO);

    default TableauRepartitionCommission fromId(Long id) {
        if (id == null) {
            return null;
        }
        TableauRepartitionCommission tableauRepartitionCommission = new TableauRepartitionCommission();
        tableauRepartitionCommission.setId(id);
        return tableauRepartitionCommission;
    }
}
