package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.HistExecutionJobDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity HistExecutionJob and its DTO HistExecutionJobDTO.
 */
@Mapper(componentModel = "spring", uses = {PtiJobDetailsMapper.class})
public interface HistExecutionJobMapper extends EntityMapper<HistExecutionJobDTO, HistExecutionJob> {

    @Mapping(source = "ptiJobDetail.id", target = "ptiJobDetailId")
    HistExecutionJobDTO toDto(HistExecutionJob histExecutionJob); 

    @Mapping(source = "ptiJobDetailId", target = "ptiJobDetail")
    HistExecutionJob toEntity(HistExecutionJobDTO histExecutionJobDTO);

    default HistExecutionJob fromId(Long id) {
        if (id == null) {
            return null;
        }
        HistExecutionJob histExecutionJob = new HistExecutionJob();
        histExecutionJob.setId(id);
        return histExecutionJob;
    }
}
