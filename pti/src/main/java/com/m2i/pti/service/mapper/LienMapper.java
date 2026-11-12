package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.LienDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Lien and its DTO LienDTO.
 */
@Mapper(componentModel = "spring", uses = {PersonneMapper.class})
public interface LienMapper extends EntityMapper<LienDTO, Lien> {

    @Mapping(source = "personne.id", target = "personneId")
    @Mapping(source = "personne.personnePhysique.nomClient", target = "personneNom")
    @Mapping(source = "personne.personnePhysique.prenomClient", target = "personnePrenom")
    @Mapping(source = "personne.personneMorale.raisonSociale", target = "personneRaisonSocial")
    @Mapping(source = "personne.personneMorale.numreoIfu", target = "personneIfu")
    @Mapping(source = "personne.typePersonne", target = "personneType")
    @Mapping(source = "personne.telephone", target = "personneTelephone")
    LienDTO toDto(Lien lien); 

    @Mapping(source = "personneId", target = "personne")
    Lien toEntity(LienDTO lienDTO);

    default Lien fromId(Long id) {
        if (id == null) {
            return null;
        }
        Lien lien = new Lien();
        lien.setId(id);
        return lien;
    }
}
