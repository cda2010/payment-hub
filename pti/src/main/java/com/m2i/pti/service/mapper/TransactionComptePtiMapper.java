package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.TransactionComptePtiDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity TransactionComptePti and its DTO TransactionComptePtiDTO.
 */
@Mapper(componentModel = "spring", uses = {OperationMapper.class,CompteAutreMapper.class})
public interface TransactionComptePtiMapper extends EntityMapper<TransactionComptePtiDTO, TransactionComptePti> {

    @Mapping(source = "compteAutre.id", target = "compteAutreId")
    @Mapping(source = "operation.id", target = "operationId")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "operation.statut", target = "operationStatut")
    @Mapping(source = "operation.codeOperation", target = "codeOperation")
    @Mapping(source = "operation.description", target = "operationMotif")
    @Mapping(source = "operation.typeService.code", target= "typeService")
    @Mapping(source = "operation.compteSrc.valeurCompte", target = "operationCompteSrc")
    @Mapping(source = "operation.codeExterne", target = "codeExterne")
    TransactionComptePtiDTO toDto(TransactionComptePti transactionComptePti);

    @Mapping(source = "compteAutreId", target = "compteAutre")
    @Mapping(source = "operationId", target = "operation")
    TransactionComptePti toEntity(TransactionComptePtiDTO transactionComptePtiDTO);

    default TransactionComptePti fromId(Long id) {
        if (id == null) {
            return null;
        }
        TransactionComptePti transactionComptePti = new TransactionComptePti();
        transactionComptePti.setId(id);
        return transactionComptePti;
    }
}
