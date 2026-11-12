package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.RoleValidationDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity RoleValidation and its DTO RoleValidationDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface RoleValidationMapper extends EntityMapper<RoleValidationDTO, RoleValidation> {

    @Mapping(source = "typeService.id",target = "typeServiceId")
    @Mapping(source = "typeService.libelle",target = "typeServiceLibelle")
    @Mapping(source = "role.name",target = "roleName")
    RoleValidationDTO toDto(RoleValidation roleValidation);

    @Mapping(source = "typeServiceId", target = "typeService.id")
    @Mapping(source = "roleName", target = "role.name")
    RoleValidation toEntity(RoleValidationDTO roleValidationDTO);

    default RoleValidation fromId(Long id) {
        if (id == null) {
            return null;
        }
        RoleValidation roleValidation = new RoleValidation();
        roleValidation.setId(id);
        return roleValidation;
    }
}
