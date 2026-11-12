package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.TaxeDTO;

import com.m2i.pti.service.dto.TaxeOperationDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity Taxe and its DTO TaxeDTO.
 */
@Mapper(componentModel = "spring", uses = {TypeServiceMapper.class, UniteOrganisationMapper.class, TaxeMapper.class})
public interface TaxeOperationMapper extends EntityMapper<TaxeOperationDTO, TaxeOperation> {

    @Mapping(source = "taxe.nom", target = "taxeNom")
    @Mapping(source = "zone.libelleUniteOrganisation", target = "libelleZone")
    @Mapping(source = "zone.id", target = "zoneId")
    @Mapping(source = "uniteOrganisation.libelleUniteOrganisation", target = "libelleUniteOrganisation")
    @Mapping(source = "uniteOrganisation.id", target = "uniteOrganisationId")
    @Mapping(source = "taxe.id", target = "taxeId")
    TaxeOperationDTO toDto(TaxeOperation taxeOperation);

    @Mapping(source = "uniteOrganisationId", target = "uniteOrganisation")
    @Mapping(source = "zoneId", target = "zone")
    @Mapping(source = "taxeId", target = "taxe")
    TaxeOperation toEntity(TaxeOperationDTO taxeOperationDTO);

    default TaxeOperation fromId(Long id) {
        if (id == null) {
            return null;
        }
        TaxeOperation taxeOperation = new TaxeOperation();
        taxeOperation.setId(id);
        return taxeOperation;
    }
}
