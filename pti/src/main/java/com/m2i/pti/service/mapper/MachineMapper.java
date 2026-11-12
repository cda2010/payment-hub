package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.MachineDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Machine and its DTO MachineDTO.
 */
@Mapper(componentModel = "spring", uses = {UniteOrganisationMapper.class})
public interface MachineMapper extends EntityMapper<MachineDTO, Machine> {

    @Mapping(source = "organisation.libelleUniteOrganisation", target = "libelleUniteOrganisation")
    @Mapping(source = "organisation.id", target = "organisationId")
    MachineDTO toDto(Machine machine);

    @Mapping(source = "organisationId", target = "organisation")
    Machine toEntity(MachineDTO machineDTO);

    default Machine fromId(Long id) {
        if (id == null) {
            return null;
        }
        Machine machine = new Machine();
        machine.setId(id);
        return machine;
    }
}
