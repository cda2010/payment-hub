package com.m2i.pti.service.mapper;
import com.m2i.pti.domain.DetailsLot;

import com.m2i.pti.service.dto.DetailsLotDTO;

import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {})
public interface DetailsLotMapper extends EntityMapper<DetailsLotDTO, DetailsLot> {
    
    default DetailsLot fromId(Long id) {
        if (id == null) {
            return null;
        }
        DetailsLot detailsLot = new DetailsLot();
        detailsLot.setId(id);
        return detailsLot;
    }
}
