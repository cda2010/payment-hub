package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.Reglement;
import com.m2i.pti.service.dto.ReglementDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity Reglement and its DTO ReglementDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface ReglementMapper extends  EntityMapper<ReglementDTO, Reglement> {

    default Reglement formId(Long id) {
        if (id == null) {
            return null;
        }
        Reglement reglement= new Reglement();
        reglement.setId(id);
        return reglement;
        }
}
