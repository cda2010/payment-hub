package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.MotifOperationDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity MotifOperation and its DTO MotifOperationDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface MotifOperationMapper extends EntityMapper<MotifOperationDTO, MotifOperation> {



    default MotifOperation fromId(Long id) {
        if (id == null) {
            return null;
        }
        MotifOperation motifOperation = new MotifOperation();
        motifOperation.setId(id);
        return motifOperation;
    }
}
