package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.BilanTaxeDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity BilanTaxe and its DTO BilanTaxeDTO.
 */
@Mapper(componentModel = "spring", uses = {OperationMapper.class, TaxeOperationMapper.class})
public interface BilanTaxeMapper extends EntityMapper<BilanTaxeDTO, BilanTaxe> {

    @Mapping(source = "operation.id", target = "operationId")
    @Mapping(source = "taxeOperation.id", target = "taxeOperationId")
    BilanTaxeDTO toDto(BilanTaxe bilanTaxe);

    @Mapping(source = "operationId", target = "operation")
    @Mapping(source = "taxeOperationId", target = "taxeOperation")
    BilanTaxe toEntity(BilanTaxeDTO bilanTaxeDTO);

    default BilanTaxe fromId(Long id) {
        if (id == null) {
            return null;
        }
        BilanTaxe bilanTaxe = new BilanTaxe();
        bilanTaxe.setId(id);
        return bilanTaxe;
    }
}
