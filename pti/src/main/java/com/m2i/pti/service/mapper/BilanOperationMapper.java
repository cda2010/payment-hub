package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.BilanOperationDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity BilanOperation and its DTO BilanOperationDTO.
 */
@Mapper(componentModel = "spring", uses = {TypeServiceMapper.class, CanalMapper.class, ClientMapper.class, UniteOrganisationMapper.class,PersonneMapper.class})
public interface BilanOperationMapper extends EntityMapper<BilanOperationDTO, BilanOperation> {

    @Mapping(source = "typeService.id", target = "typeServiceId")
    @Mapping(source = "typeService.libelle", target = "typeServiceLibelle")
    @Mapping(source = "canal.id", target = "canalId")
    @Mapping(source = "canal.libelleCanal", target = "canalLibelleCanal")
    @Mapping(source = "beneficiaire.id", target = "beneficiaireId")
    @Mapping(source = "clientBeneficiaire.id", target = "clientBeneficiaireId")
    @Mapping(source = "beneficiaire.codeClient", target = "beneficiaireCodeClient")
    @Mapping(source = "emetteur.id", target = "emetteurId")
    @Mapping(source = "clientEmetteur.id", target = "clientEmetteurId")
    @Mapping(source = "emetteur.codeClient", target = "emetteurCodeClient")
    @Mapping(source = "organisationEm.id", target = "organisationEmId")
    @Mapping(source = "organisationEm.libelleUniteOrganisation", target = "organisationEmLibelleUniteOrganisation")
    @Mapping(source = "organisationAcq.id", target = "organisationAcqId")
    @Mapping(source = "organisationAcq.libelleUniteOrganisation", target = "organisationAcqLibelleUniteOrganisation")
    @Mapping(source = "acteur.id", target = "acteurId")
    @Mapping(source = "acteur.libelleUniteOrganisation", target = "acteurLibelleUniteOrganisation")
    BilanOperationDTO toDto(BilanOperation bilanOperation); 

    @Mapping(source = "typeServiceId", target = "typeService")
    @Mapping(source = "canalId", target = "canal")
    @Mapping(source = "beneficiaireId", target = "beneficiaire")
    @Mapping(source = "emetteurId", target = "emetteur")
    @Mapping(source = "clientBeneficiaireId", target = "clientBeneficiaire")
    @Mapping(source = "clientEmetteurId", target = "clientEmetteur")
    @Mapping(source = "organisationEmId", target = "organisationEm")
    @Mapping(source = "organisationAcqId", target = "organisationAcq")
    @Mapping(source = "acteurId", target = "acteur")
    BilanOperation toEntity(BilanOperationDTO bilanOperationDTO);

    default BilanOperation fromId(Long id) {
        if (id == null) {
            return null;
        }
        BilanOperation bilanOperation = new BilanOperation();
        bilanOperation.setId(id);
        return bilanOperation;
    }
}
