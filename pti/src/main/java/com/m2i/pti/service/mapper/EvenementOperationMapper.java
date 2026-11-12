package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.EvenementOperation;

import com.m2i.pti.service.dto.EvenementOperationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OperationMapper.class,CompteMapper.class})
public interface EvenementOperationMapper extends EntityMapper<EvenementOperationDTO, EvenementOperation> {

    @Mapping(source = "compte.id", target = "compteId")
    @Mapping(source = "operation.id", target = "operationId")
    @Mapping(source = "createdDate", target = "createdDate")
    @Mapping(source = "operation.typeService.code", target= "typeService")
    @Mapping(source = "operation.description", target = "description")
    EvenementOperationDTO toDto(EvenementOperation evenementOperation);

    @Mapping(source = "compteId", target = "compte")
    @Mapping(source = "operationId", target = "operation")
    EvenementOperation toEntity(EvenementOperationDTO evenementOperationDTO);

    default EvenementOperation fromId(Long id){
        if (id == null) {
            return null;
        }
        EvenementOperation evenementOperation=new EvenementOperation();
        evenementOperation.setId(id);
        return evenementOperation;
    }

}
