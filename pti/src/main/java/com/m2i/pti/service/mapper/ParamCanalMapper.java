package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.*;

import org.mapstruct.*;

/**
 * Mapper for the entity ParamCanal and its DTO ParamCanalDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface ParamCanalMapper extends EntityMapper<ParamCanalDTO, ParamCanal> {

    ParamCanal toEntity(ParamCanalDTO canalApplicationDTO);

    default ParamCanal fromId(Long id) {
        if (id == null) {
            return null;
        }
        ParamCanal paramCanal = new ParamCanal();
        paramCanal.setId(id);
        return paramCanal;
    }
    
}
