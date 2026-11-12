package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.*;

import org.mapstruct.*;

/**
 * Mapper for the entity TypeService and its DTO TypeServiceDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface CanalApplicationMapper extends EntityMapper<CanalApplicationDTO, CanalApplication> {

    CanalApplication toEntity(CanalApplicationDTO canalApplicationDTO);

    default CanalApplication fromId(Long id) {
        if (id == null) {
            return null;
        }
        CanalApplication canalApplication = new CanalApplication();
        canalApplication.setId(id);
        return canalApplication;
    }
    
}
