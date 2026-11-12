package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.TypeServiceDTO;

import org.mapstruct.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Mapper for the entity TypeService and its DTO TypeServiceDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface TypeServiceMapper extends EntityMapper<TypeServiceDTO, TypeService> {


 
    TypeService toEntity(TypeServiceDTO typeServiceDTO);

    default TypeService fromId(Long id) {
        if (id == null) {
            return null;
        }
        TypeService typeService = new TypeService();
        typeService.setId(id);
        return typeService;
    }
}
