package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.RepartionFraisEmAcqDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity RepartionFraisEmAcq and its DTO RepartionFraisEmAcqDTO.
 */
@Mapper(componentModel = "spring", uses = {TypeServiceMapper.class})
public interface RepartionFraisEmAcqMapper extends EntityMapper<RepartionFraisEmAcqDTO, RepartionFraisEmAcq> {

    @Mapping(source = "typeService.id", target = "typeServiceId")
    @Mapping(source = "typeService.libelle", target = "typeServiceLibelle")
    RepartionFraisEmAcqDTO toDto(RepartionFraisEmAcq repartionFraisEmAcq);

    @Mapping(source = "typeServiceId", target = "typeService")
    RepartionFraisEmAcq toEntity(RepartionFraisEmAcqDTO repartionFraisEmAcqDTO);

    default RepartionFraisEmAcq fromId(Long id) {
        if (id == null) {
            return null;
        }
        RepartionFraisEmAcq repartionFraisEmAcq = new RepartionFraisEmAcq();
        repartionFraisEmAcq.setId(id);
        return repartionFraisEmAcq;
    }
}
