package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.TaxeDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Taxe and its DTO TaxeDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface TaxeMapper extends EntityMapper<TaxeDTO, Taxe> {



    default Taxe fromId(Long id) {
        if (id == null) {
            return null;
        }
        Taxe taxe = new Taxe();
        taxe.setId(id);
        return taxe;
    }
}
