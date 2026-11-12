package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.TableauCumulDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity TableauCumul and its DTO TableauCumulDTO.
 */
@Mapper(componentModel = "spring", uses = {TypeServiceMapper.class, CanalMapper.class, ClientMapper.class, UniteOrganisationMapper.class})
public interface TableauCumulMapper extends EntityMapper<TableauCumulDTO, TableauCumul> {

    @Mapping(source = "typeService.id", target = "typeServiceId")
    @Mapping(source = "typeService.libelle", target = "typeServiceLibelle")
    @Mapping(source = "canal.id", target = "canalId")
    @Mapping(source = "canal.libelleCanal", target = "canalLibelleCanal")
    @Mapping(source = "beneficiaire.id", target = "beneficiaireId")
    @Mapping(source = "beneficiaire.codeClient", target = "beneficiaireCodeClient")
    @Mapping(source = "emetteur.id", target = "emetteurId")
    @Mapping(source = "emetteur.codeClient", target = "emetteurCodeClient")
    @Mapping(source = "organisationEm.id", target = "organisationEmId")
    @Mapping(source = "organisationEm.libelleUniteOrganisation", target = "organisationEmLibelleUniteOrganisation")
    @Mapping(source = "organisationAcq.id", target = "organisationAcqId")
    @Mapping(source = "organisationAcq.libelleUniteOrganisation", target = "organisationAcqLibelleUniteOrganisation")
    TableauCumulDTO toDto(TableauCumul tableauCumul); 

    @Mapping(source = "typeServiceId", target = "typeService")
    @Mapping(source = "canalId", target = "canal")
    @Mapping(source = "beneficiaireId", target = "beneficiaire")
    @Mapping(source = "emetteurId", target = "emetteur")
    @Mapping(source = "organisationEmId", target = "organisationEm")
    @Mapping(source = "organisationAcqId", target = "organisationAcq")
    TableauCumul toEntity(TableauCumulDTO tableauCumulDTO);

    default TableauCumul fromId(Long id) {
        if (id == null) {
            return null;
        }
        TableauCumul tableauCumul = new TableauCumul();
        tableauCumul.setId(id);
        return tableauCumul;
    }
}
