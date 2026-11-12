package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.Lot;
import com.m2i.pti.service.dto.LotDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {})
public interface LotMapper extends EntityMapper<LotDTO, Lot> {
    
    default Lot fromId(Long id) {
        if (id == null) {
            return null;
        }
        Lot lot = new Lot();
        lot.setId(id);
        return lot;
    }
}
