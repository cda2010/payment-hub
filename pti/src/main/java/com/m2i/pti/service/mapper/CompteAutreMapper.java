package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.CompteAutreDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity CompteAutre and its DTO CompteAutreDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface CompteAutreMapper extends EntityMapper<CompteAutreDTO, CompteAutre> {


    CompteAutre toEntity(CompteAutreDTO compteAutreDTO);

    default CompteAutre fromId(Long id) {
        if (id == null) {
            return null;
        }
        CompteAutre compteAutre = new CompteAutre();
        compteAutre.setId(id);
        return compteAutre;
    }
}
