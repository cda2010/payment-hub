package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.InstructionComptableDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity InstructionComptable and its DTO InstructionComptableDTO.
 */
@Mapper(componentModel = "spring", uses = {TypeServiceMapper.class})
public interface InstructionComptableMapper extends EntityMapper<InstructionComptableDTO, InstructionComptable> {

    @Mapping(source = "typeService.id", target = "typeServiceId")
    @Mapping(source = "typeService.libelle", target = "typeServiceLibelle")
    InstructionComptableDTO toDto(InstructionComptable instructionComptable);

    @Mapping(source = "typeServiceId", target = "typeService")
    InstructionComptable toEntity(InstructionComptableDTO instructionComptableDTO);

    default InstructionComptable fromId(Long id) {
        if (id == null) {
            return null;
        }
        InstructionComptable instructionComptable = new InstructionComptable();
        instructionComptable.setId(id);
        return instructionComptable;
    }
}
