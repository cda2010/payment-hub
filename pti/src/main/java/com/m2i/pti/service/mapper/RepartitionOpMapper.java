package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.RepartitionOpDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity RepartitionOp and its DTO RepartitionOpDTO.
 */
@Mapper(componentModel = "spring", uses = {OperationMapper.class})
public interface RepartitionOpMapper extends EntityMapper<RepartitionOpDTO, RepartitionOp> {

    @Mapping(source = "operation.id", target = "operationId")
    RepartitionOpDTO toDto(RepartitionOp repartitionOp); 

    @Mapping(source = "operationId", target = "operation")
    RepartitionOp toEntity(RepartitionOpDTO repartitionOpDTO);

    default RepartitionOp fromId(Long id) {
        if (id == null) {
            return null;
        }
        RepartitionOp repartitionOp = new RepartitionOp();
        repartitionOp.setId(id);
        return repartitionOp;
    }
}
