package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.PersonnePhysiqueDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity PersonnePhysique and its DTO PersonnePhysiqueDTO.
 */
@Mapper(componentModel = "spring", uses = {NationaliteMapper.class})
public interface PersonnePhysiqueMapper extends EntityMapper<PersonnePhysiqueDTO, PersonnePhysique> {

    @Mapping(source = "nationalite.id", target = "nationaliteId")
    @Mapping(source = "nationalite.libelle", target = "nationaliteLibelle")
    @Mapping(source = "paysResidence.id", target = "paysResidenceId")
    @Mapping(source = "paysResidence.libelle", target = "paysResidenceLibelle")
    @Mapping(source = "nomMereClient", target = "nomMereClient")
    @Mapping(source="nomJeuneFille",target ="nomJeuneFille")
    @Mapping(source="profession",target ="profession")
    PersonnePhysiqueDTO toDto(PersonnePhysique personnePhysique);

    @Mapping(source = "paysResidenceId", target = "paysResidence")
    @Mapping(source = "nationaliteId", target = "nationalite")
    @Mapping(source = "nomMereClient", target = "nomMereClient")
    @Mapping(source="nomJeuneFille",target ="nomJeuneFille")
    @Mapping(source="profession",target ="profession")
    PersonnePhysique toEntity(PersonnePhysiqueDTO personnePhysiqueDTO);

    default PersonnePhysique fromId(Long id) {
        if (id == null) {
            return null;
        }
        PersonnePhysique personnePhysique = new PersonnePhysique();
        personnePhysique.setId(id);
        return personnePhysique;
    }
}
