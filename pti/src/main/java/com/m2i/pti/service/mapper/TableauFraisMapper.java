package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.TableauFraisDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity TableauFrais and its DTO TableauFraisDTO.
 */
@Mapper(componentModel = "spring", uses = {TypeServiceMapper.class, CanalMapper.class, TypeUniteOrganisationMapper.class, ClientMapper.class, UniteOrganisationMapper.class})
public interface TableauFraisMapper extends EntityMapper<TableauFraisDTO, TableauFrais> {

    @Mapping(source = "typeService.id", target = "typeServiceId")
    @Mapping(source = "typeService.libelle", target = "typeServiceLibelle")
    @Mapping(source = "canal.id", target = "canalId")
    @Mapping(source = "canal.libelleCanal", target = "canalLibelleCanal")
    @Mapping(source = "typeUoEmetteur.id", target = "typeUoEmetteurId")
    @Mapping(source = "typeUoEmetteur.libelleTypeUO", target = "typeUoEmetteurLibelleTypeUO")
    @Mapping(source = "typeUoAcquereur.id", target = "typeUoAcquereurId")
    @Mapping(source = "typeUoAcquereur.libelleTypeUO", target = "typeUoAcquereurLibelleTypeUO")
    @Mapping(source = "beneficiaire.id", target = "beneficiaireId")
    @Mapping(source = "beneficiaire.codeClient", target = "beneficiaireCodeClient")
    @Mapping(source = "emetteur.id", target = "emetteurId")
    @Mapping(source = "emetteur.codeClient", target = "emetteurCodeClient")
    @Mapping(source = "organisationEm.id", target = "organisationEmId")
    @Mapping(source = "organisationEm.libelleUniteOrganisation", target = "organisationEmLibelleUniteOrganisation")
    @Mapping(source = "organisationAcq.id", target = "organisationAcqId")
    @Mapping(source = "organisationAcq.libelleUniteOrganisation", target = "organisationAcqLibelleUniteOrganisation")
    TableauFraisDTO toDto(TableauFrais tableauFrais);

    @Mapping(target = "ligneFrais", ignore = true)
    @Mapping(source = "typeServiceId", target = "typeService")
    @Mapping(source = "canalId", target = "canal")
    @Mapping(source = "typeUoEmetteurId", target = "typeUoEmetteur")
    @Mapping(source = "typeUoAcquereurId", target = "typeUoAcquereur")
    @Mapping(source = "beneficiaireId", target = "beneficiaire")
    @Mapping(source = "emetteurId", target = "emetteur")
    @Mapping(source = "organisationEmId", target = "organisationEm")
    @Mapping(source = "organisationAcqId", target = "organisationAcq")
    TableauFrais toEntity(TableauFraisDTO tableauFraisDTO);

    default TableauFrais fromId(Long id) {
        if (id == null) {
            return null;
        }
        TableauFrais tableauFrais = new TableauFrais();
        tableauFrais.setId(id);
        return tableauFrais;
    }
}
