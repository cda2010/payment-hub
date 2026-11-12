package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.PersonneDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Personne and its DTO PersonneDTO.
 */
@Mapper(componentModel = "spring", uses = {PersonnePhysiqueMapper.class, PersonneMoraleMapper.class,BrancheActiviteMapper.class,UniteOrganisationMapper.class,LocaliteMapper.class})
public interface PersonneMapper extends EntityMapper<PersonneDTO, Personne> {


    @Mapping(source = "localite.nomVille", target = "localiteVille")
    @Mapping(source = "localite.nomCommune", target = "localiteCommune")
    @Mapping(source = "localite.nomProvince", target = "localiteProvince")
    @Mapping(source = "localite.secteur", target = "localiteSecteur")
    @Mapping(source = "localite.quartier", target = "localiteQuartier")
    @Mapping(source = "localite.nomRegion", target = "localiteRegion")
    @Mapping(source = "localite.id", target = "localiteId")
    @Mapping(source = "personnePhysique.id", target = "personnePhysiqueId")
    @Mapping(source = "personnePhysique.nomClient", target = "personnePhysiqueNomClient")
    @Mapping(source = "personnePhysique.prenomClient", target = "personnePhysiquePrenomClient")
    @Mapping(source = "personnePhysique.dateNaissance", target = "personneDateNaissance")
    @Mapping(source = "personnePhysique.nomMereClient", target = "personneNomMereClient")
    @Mapping(source = "personneMorale.id", target = "personneMoraleId")
    @Mapping(source = "brancheActivite.id", target = "brancheActiviteId")
    @Mapping(source = "paysCompte.id", target = "paysCompteId")
    @Mapping(source = "paysCompte.libelleUniteOrganisation", target = "paysCompteLibelle")
    @Mapping(source = "brancheActivite.libelle", target = "brancheActiviteLibelle")
    PersonneDTO toDto(Personne personne);

    @Mapping(source  ="localiteId", target = "localite")
    @Mapping(source = "personnePhysiqueId", target = "personnePhysique")
    @Mapping(source = "personneMoraleId", target = "personneMorale")
    @Mapping(source = "brancheActiviteId", target = "brancheActivite")
    @Mapping(source ="paysCompteId", target = "paysCompte")
    @Mapping(target = "pieces", ignore = true)
   /* @Mapping(target = "emetteurs", ignore = true)
    @Mapping(target = "beneficiaires", ignore = true)*/
    /*@Mapping(target = "uniteOrganisations", ignore = true)*/
    Personne toEntity(PersonneDTO personneDTO);


    default Personne fromId(Long id) {
        if (id == null) {
            return null;
        }
        Personne personne = new Personne();
        personne.setId(id);
        return personne;
    }
}
