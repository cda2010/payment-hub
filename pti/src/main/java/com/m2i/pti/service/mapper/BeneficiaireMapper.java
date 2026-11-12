package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.BeneficiaireDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Beneficiaire and its DTO BeneficiaireDTO.
 */
@Mapper(componentModel = "spring", uses = {PersonneMapper.class})
public interface BeneficiaireMapper extends EntityMapper<BeneficiaireDTO, Beneficiaire> {

    @Mapping(source = "personne.id", target = "personneId")
    BeneficiaireDTO toDto(Beneficiaire beneficiaire);

    @Mapping(source = "personneId", target = "personne")
  
    Beneficiaire toEntity(BeneficiaireDTO beneficiaireDTO);

    default Beneficiaire fromId(Long id) {
        if (id == null) {
            return null;
        }
        Beneficiaire beneficiaire = new Beneficiaire();
        beneficiaire.setId(id);
        return beneficiaire;
    }
}
