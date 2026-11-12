package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.LigneReglement;
import com.m2i.pti.service.LigneReglementDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OperationMapper.class,ReglementMapper.class,UniteOrganisationMapper.class,})

public interface LigneReglementMapper extends EntityMapper<LigneReglementDTO, LigneReglement> {

    @Mapping(source = "operation.id", target = "operationId")
    @Mapping(source = "reglement.id", target = "reglementId")
    @Mapping(source = "reglement.statut", target = "reglementStatut")
    @Mapping(source = "organisation.libelleUniteOrganisation", target = "libelleUO")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "createdBy", target = "createdBy")
    LigneReglementDTO toDto(LigneReglement ligneReglement);

    @Mapping(source = "reglementId", target = "reglement")
    @Mapping(source = "operationId", target = "operation")
    LigneReglement toEntity(LigneReglementDTO ligneReglementDTO);

    default LigneReglement formId(Long id) {
        if (id == null) {
            return null;
        }
        LigneReglement ligneReglement= new LigneReglement();
        ligneReglement.setId(id);
        return ligneReglement;
    }
}
