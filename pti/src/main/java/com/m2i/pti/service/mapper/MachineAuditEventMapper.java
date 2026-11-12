package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.MachineAuditEvent;
import com.m2i.pti.service.dto.MachineAuditEventDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity MachineAuditEvent and its DTO MachineAuditEventDTO.
 */
@Mapper(componentModel = "spring", uses = {MachineMapper.class})
public interface MachineAuditEventMapper extends EntityMapper<MachineAuditEventDTO, MachineAuditEvent> {


    @Mapping(source = "machine.computerModel", target = "machineHostName")
    @Mapping(source = "machine.id", target = "machineId")
    MachineAuditEventDTO toDto(MachineAuditEvent operationAudit);

    @Mapping(source = "machineId", target = "machine")
    MachineAuditEvent toEntity(MachineAuditEventDTO erationAuditDTO);

    default MachineAuditEvent fromId(Long id) {
        if (id == null) {
            return null;
        }
        MachineAuditEvent machineAuditEvent = new MachineAuditEvent();
        machineAuditEvent.setId(id);
        return machineAuditEvent;
    }
}
