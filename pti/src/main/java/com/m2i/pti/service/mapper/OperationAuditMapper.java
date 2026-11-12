package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.OperationAuditDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity OperationAudit and its DTO OperationAuditDTO.
 */
@Mapper(componentModel = "spring", uses = {OperationMapper.class,User.class})
public interface OperationAuditMapper extends EntityMapper<OperationAuditDTO, OperationAudit> {

    @Mapping(source = "operation.id", target = "operationId")
    @Mapping(source = "operation.codeOperation", target = "operationCodeOperation")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.login", target = "login")
    OperationAuditDTO toDto(OperationAudit operationAudit);

    @Mapping(source = "operationId", target = "operation")
    OperationAudit toEntity(OperationAuditDTO operationAuditDTO);

    default OperationAudit fromId(Long id) {
        if (id == null) {
            return null;
        }
        OperationAudit operationAudit = new OperationAudit();
        operationAudit.setId(id);
        return operationAudit;
    }
}
