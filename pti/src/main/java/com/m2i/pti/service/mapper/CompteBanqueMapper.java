package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.CompteBanqueDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity CompteBanque and its DTO CompteBanqueDTO.
 */
@Mapper(componentModel = "spring", uses = {DeviseMapper.class})
public interface CompteBanqueMapper extends EntityMapper<CompteBanqueDTO, CompteBanque> {

    @Mapping(source = "devise.id", target = "deviseId")
    @Mapping(source = "devise.code", target = "deviseCode")
    CompteBanqueDTO toDto(CompteBanque compteBanque);

    @Mapping(source = "deviseId", target = "devise")
    CompteBanque toEntity(CompteBanqueDTO compteBanqueDTO);

    default CompteBanque fromId(Long id) {
        if (id == null) {
            return null;
        }
        CompteBanque compteBanque = new CompteBanque();
        compteBanque.setId(id);
        return compteBanque;
    }
}
