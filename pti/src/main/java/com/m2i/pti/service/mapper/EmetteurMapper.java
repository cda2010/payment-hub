package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.EmetteurDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Emetteur and its DTO EmetteurDTO.
 */
@Mapper(componentModel = "spring", uses = {PersonneMapper.class})
public interface EmetteurMapper extends EntityMapper<EmetteurDTO, Emetteur> {

    @Mapping(source = "personne.id", target = "personneId")
    EmetteurDTO toDto(Emetteur emetteur);

    @Mapping(source = "personneId", target = "personne")
  
    Emetteur toEntity(EmetteurDTO emetteurDTO);

    default Emetteur fromId(Long id) {
        if (id == null) {
            return null;
        }
        Emetteur emetteur = new Emetteur();
        emetteur.setId(id);
        return emetteur;
    }
}
