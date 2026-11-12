package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.CanalDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Canal and its DTO CanalDTO.
 */
@Mapper(componentModel = "spring", uses = {TypeServiceMapper.class})
public interface    CanalMapper extends EntityMapper<CanalDTO, Canal> {


   
    Canal toEntity(CanalDTO canalDTO);

    default Canal fromId(Long id) {
        if (id == null) {
            return null;
        }
        Canal canal = new Canal();
        canal.setId(id);
        return canal;
    }
}
