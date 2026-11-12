package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.GroupRepartitionDTO;

import com.m2i.pti.service.dto.PersonneDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity GroupRepartition and its DTO GroupRepartitionDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface GroupRepartitionMapper extends EntityMapper<GroupRepartitionDTO, GroupRepartition> {


    @Mapping(source = "typeServiceId", target = "typeService.id")
    GroupRepartition toEntity(GroupRepartitionDTO groupRepartitionDTO);

    @Mapping(source = "typeService.id", target = "typeServiceId")
    @Mapping(source = "typeService.libelle", target = "typeServiceLibelle")
    GroupRepartitionDTO toDto( GroupRepartition groupRepartition);
    

    default GroupRepartition fromId(Long id) {
        if (id == null) {
            return null;
        }
        GroupRepartition groupRepartition = new GroupRepartition();
        groupRepartition.setId(id);
        return groupRepartition;
    }
}
