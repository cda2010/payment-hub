package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.ParametreAppDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity ParametreApp and its DTO ParametreAppDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface ParametreAppMapper extends EntityMapper<ParametreAppDTO, ParametreApp> {



    default ParametreApp fromId(Long id) {
        if (id == null) {
            return null;
        }
        ParametreApp parametreApp = new ParametreApp();
        parametreApp.setId(id);
        return parametreApp;
    }
}
