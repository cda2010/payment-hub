package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.ClientDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Client and its DTO ClientDTO.
 */
@Mapper(componentModel = "spring", uses = {PersonneMapper.class, ServiceProfilMapper.class, ProfilActeurMapper.class,UniteOrganisationMapper.class,InfosuppMapper.class})
public interface ClientMapper extends EntityMapper<ClientDTO, Client> {

    @Mapping(source = "personne.id", target = "personneId")
    @Mapping(source = "profil.id", target = "profilId")
    @Mapping(source = "profil.nom", target = "profilNom")
    @Mapping(source = "profilActeur.id", target = "profilActeurId")
    @Mapping(source = "profilActeur.nom", target = "profilActeurNom")
    @Mapping(source = "groupeProfilActeur.id", target = "groupeProfilActeurId")
    @Mapping(source = "groupeProfilActeur.nom", target = "groupeProfilActeurNom")
    @Mapping(source = "uniteOrganisationCreateur.libelleUniteOrganisation", target = "agentEnroleur")
    @Mapping(source = "infoSupp.id", target = "infosuppId")
    ClientDTO toDto(Client client);

    @Mapping(source = "profilId", target = "profil")
    @Mapping(source = "personneId", target = "personne")
    @Mapping(source = "infosuppId", target = "infoSupp")

   
    @Mapping(target = "documents", ignore = true)
   
    @Mapping(source = "profilActeurId", target = "profilActeur.id")
    @Mapping(source = "groupeProfilActeurId", target = "groupeProfilActeur.id")
    Client toEntity(ClientDTO clientDTO);

    default Client fromId(Long id) {
        if (id == null) {
            return null;
        }
        Client client = new Client();
        client.setId(id);
        return client;
    }
}
