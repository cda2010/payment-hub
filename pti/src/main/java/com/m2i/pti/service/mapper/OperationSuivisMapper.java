package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.OperationSuivis;
import com.m2i.pti.service.dto.OperationSuivisDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity OperationSuivis and its DTO OperationSuivisDTO.
 */
@Mapper(componentModel = "spring", uses = {ClientMapper.class, OperationMapper.class})
public interface OperationSuivisMapper extends EntityMapper<OperationSuivisDTO, OperationSuivis> {

    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "client.personne.personnePhysique.nomClient", target = "nomClient")
    @Mapping(source = "client.personne.personnePhysique.prenomClient", target = "prenomClient")
    @Mapping(source = "operation.id", target = "operationId")
    @Mapping(source = "operation.montantOp", target = "montantOp")
    @Mapping(source = "operation.statut", target = "statut")
    OperationSuivisDTO toDto(OperationSuivis operationSuivis);

    @Mapping(source = "clientId", target = "client")
    @Mapping(source = "operationId", target = "operation")
    OperationSuivis toEntity(OperationSuivisDTO operationSuivisDTO);

    default OperationSuivis fromId(Long id) {
        if (id == null) {
            return null;
        }
        OperationSuivis operationSuivis = new OperationSuivis();
        operationSuivis.setId(id);
        return operationSuivis;
    }
}
