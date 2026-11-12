package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.PaysDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Pays and its DTO PaysDTO.
 */
@Mapper(componentModel = "spring", uses = {DeviseMapper.class})
public interface PaysMapper extends EntityMapper<PaysDTO, Pays> {

    @Mapping(source = "devise.id", target = "deviseId")
    @Mapping(source = "devise.code", target = "deviseCode")
    PaysDTO toDto(Pays pays);

  
    @Mapping(source = "deviseId", target = "devise")
    Pays toEntity(PaysDTO paysDTO);

    default Pays fromId(Long id) {
        if (id == null) {
            return null;
        }
        Pays pays = new Pays();
        pays.setId(id);
        return pays;
    }
}
