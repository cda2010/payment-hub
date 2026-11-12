package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.InfoSupp;
import com.m2i.pti.service.dto.InfosuppDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface InfosuppMapper extends EntityMapper<InfosuppDTO, InfoSupp> {

    InfosuppDTO toDto(InfoSupp infosupp);

    InfoSupp toEntity(InfosuppDTO infosuppDTO);

    default InfoSupp fromId(Long id) {
        if (id == null) {
            return null;
        }
        InfoSupp infosupp = new InfoSupp();
        infosupp.setId(id);
        return infosupp;
    }
}
