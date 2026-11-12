package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.RecepteurEmail;
import com.m2i.pti.service.dto.RecepteurEmailDTO;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {})
public interface RecepteurEmailMapper extends EntityMapper<RecepteurEmailDTO, RecepteurEmail>{

    RecepteurEmailDTO toDto(RecepteurEmail recepteurEmail);


    RecepteurEmail toEntity(RecepteurEmailDTO recepteurEmailDTO);

    default RecepteurEmail fromId(Long id) {
        if (id == null) {
            return null;
        }
        RecepteurEmail recepteurEmail = new RecepteurEmail();
        recepteurEmail.setId(id);
        return recepteurEmail;
    }
}
