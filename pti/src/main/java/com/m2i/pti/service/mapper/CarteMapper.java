package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.CarteDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity CompteAutre and its DTO CompteAutreDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface CarteMapper extends EntityMapper<CarteDTO, Carte> {


    Carte toEntity(CarteDTO CarteDTO);

    default Carte fromId(Long id) {
        if (id == null) {
            return null;
        }
        Carte carte = new Carte();
        carte.setId(id);
        return carte;
    }
}
