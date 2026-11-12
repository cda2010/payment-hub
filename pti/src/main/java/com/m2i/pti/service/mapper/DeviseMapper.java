package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.DeviseDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Devise and its DTO DeviseDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface DeviseMapper extends EntityMapper<DeviseDTO, Devise> {


  
  
    Devise toEntity(DeviseDTO deviseDTO);

    default Devise fromId(Long id) {
        if (id == null) {
            return null;
        }
        Devise devise = new Devise();
        devise.setId(id);
        return devise;
    }
}
