package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.UniteOrganisationDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity UniteOrganisation and its DTO UniteOrganisationDTO.
 */
@Mapper(componentModel = "spring", uses = {TypeUniteOrganisationMapper.class, PersonneMapper.class, FournisseurServiceMapper.class,VilleMapper.class,ProfilActeurMapper.class,LocaliteMapper.class})
public interface UniteOrganisationMapper extends EntityMapper<UniteOrganisationDTO, UniteOrganisation> {

    @Mapping(source = "pereUO.id", target = "pereUOId")
    @Mapping(source = "pereUO.libelleUniteOrganisation", target = "pereUOLibelleUniteOrganisation")
    @Mapping(source = "typeUO.id", target = "typeUOId")
    @Mapping(source = "typeUO.codeTypeUO", target = "typeUOCode")
    @Mapping(source = "typeUO.libelleTypeUO", target = "typeUOLibelleTypeUO")
    @Mapping(source = "representant.id", target = "representantId")
    @Mapping(source = "ville.id", target = "villeId")
    @Mapping(source = "profilActeur.id", target = "profilActeurId")
    @Mapping(source = "profilActeur.nom", target = "profilActeurNom")
    @Mapping(source = "groupeProfilActeur.id", target = "groupeProfilActeurId")
    @Mapping(source = "groupeProfilActeur.nom", target = "groupeProfilActeurNom")
    @Mapping(source = "fournisseur.id", target = "fournisseurId")
    @Mapping(source = "fournisseur.libelleFse", target = "fournisseurLibelleFse")
    @Mapping(source = "ville.nom", target = "villeNom")
    @Mapping(source = "serviceProfil.id", target = "serviceProfilId")
    @Mapping(source = "serviceProfil.nom", target = "serviceProfilNom")
    @Mapping(source = "commerciale.id", target = "commercialeId")
    @Mapping(source = "commerciale.nom", target = "commercialeNom")
    @Mapping(source = "commerciale.prenom", target = "commercialePrenom")
    @Mapping(source = "localite.nomVille", target = "localiteVille")
    @Mapping(source = "localite.nomCommune", target = "localiteCommune")
    @Mapping(source = "localite.nomProvince", target = "localiteProvince")
    @Mapping(source = "localite.secteur", target = "localiteSecteur")
    @Mapping(source = "localite.quartier", target = "localiteQuartier")
    @Mapping(source = "localite.nomRegion", target = "localiteRegion")
    @Mapping(source = "localite.id", target = "localiteId")
    UniteOrganisationDTO toDto(UniteOrganisation uniteOrganisation);

  
  
    
 
    @Mapping(source = "pereUOId", target = "pereUO")
    @Mapping(source = "typeUOId", target = "typeUO.id")
    @Mapping(source = "typeUOCode", target = "typeUO.codeTypeUO")
    @Mapping(source = "typeUOLibelleTypeUO", target = "typeUO.libelleTypeUO")
    @Mapping(source = "niveauTypeUo", target = "typeUO.niveau")
    @Mapping(source = "representantId", target = "representant")
    @Mapping(source = "villeId", target = "ville")
    @Mapping(source = "profilActeurId", target = "profilActeur")
    @Mapping(source = "fournisseurId", target = "fournisseur")
    @Mapping(source = "serviceProfilId", target = "serviceProfil.id")
    @Mapping(source  ="localiteId", target = "localite")
  
    UniteOrganisation toEntity(UniteOrganisationDTO uniteOrganisationDTO);

    default UniteOrganisation fromId(Long id) {
        if (id == null) {
            return null;
        }
        UniteOrganisation uniteOrganisation = new UniteOrganisation();
        uniteOrganisation.setId(id);
        return uniteOrganisation;
    }
}
