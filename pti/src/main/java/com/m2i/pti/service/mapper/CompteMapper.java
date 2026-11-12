package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.CompteDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Compte and its DTO CompteDTO.
 */
@Mapper(componentModel = "spring", uses = {CarteMapper.class, CompteBanqueMapper.class, CompteAutreMapper.class, ClientMapper.class, TypeCompteMapper.class, UniteOrganisationMapper.class})
public interface CompteMapper extends EntityMapper<CompteDTO, Compte> {

    @Mapping(source = "comptePincipale.id", target = "comptePincipaleId")
    @Mapping(source = "compteBanque.id", target = "compteBanqueId")
    @Mapping(source = "compteAutre.id", target = "compteAutreId")
    @Mapping(source = "carte.id", target = "carteId")

    @Mapping(source = "compteAutre.solde", target = "soldeCompte")
    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "client.codeClient", target = "clientCodeClient")
    @Mapping(source = "typeCompte.id", target = "typeCompteId")
    @Mapping(source = "typeCompte.libelleTypeCompte", target = "typeCompteLibelleTypeCompte")
    @Mapping(source = "uniteOrganisation.id", target = "uniteOrganisationId")
    @Mapping(source = "uniteOrganisation.libelleUniteOrganisation", target = "uniteOrganisationLibelleUniteOrganisation")
    
        
    
    CompteDTO toDto(Compte compte);

    
    
    @Mapping(source = "comptePincipaleId", target = "comptePincipale")
    @Mapping(source = "compteBanqueId", target = "compteBanque")

             @Mapping(source = "last4Digit", target = "carte.last4Digit")
    @Mapping(source = "carteId", target = "carte.id")
    @Mapping(source = "compteAutreId", target = "compteAutre.id")
    @Mapping(source = "soldeCompte", target = "compteAutre.solde")
    @Mapping(source = "codeFse", target = "compteAutre.codeFse")

    @Mapping(source = "clientId", target = "client")
    @Mapping(source = "typeCompteId", target = "typeCompte")
    @Mapping(source = "uniteOrganisationId", target = "uniteOrganisation")

    Compte toEntity(CompteDTO compteDTO);

    default Compte fromId(Long id) {
        if (id == null) {
            return null;
        }
        Compte compte = new Compte();
        compte.setId(id);
        return compte;
    }
}
