package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.BlackList;
import com.m2i.pti.service.dto.BlackListDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity BlackList and its DTO BlackListDTO.
 */
@Mapper(componentModel = "spring", uses = {ClientMapper.class})
public interface BlackListMapper extends EntityMapper<BlackListDTO, BlackList> {

    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "client.personne.personnePhysique.nomClient", target = "nomClient")
    @Mapping(source = "client.personne.personnePhysique.prenomClient", target = "prenomClient")
    @Mapping(source = "client.personne.telephone", target = "telephone")
    @Mapping(source = "client.personne.typePersonne", target = "typePersonne")
    @Mapping(source = "client.personne.personnePhysique.nomMereClient", target = "nomMereClient")
    BlackListDTO toDto(BlackList blackList);

    @Mapping(source = "clientId", target = "client")
    BlackList toEntity(BlackListDTO blackListDTO);

    default BlackList fromId(Long id) {
        if (id == null) {
            return null;
        }
        BlackList blackList = new BlackList();
        blackList.setId(id);
        return blackList;
    }
}
