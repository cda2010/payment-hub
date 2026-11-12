package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.TemplateMessageDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity TemplateMessage and its DTO TemplateMessageDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface TemplateMessageMapper extends EntityMapper<TemplateMessageDTO, TemplateMessage> {

    @Mapping(source = "fse.id", target = "fseId")
    @Mapping(source = "fse.libelleUniteOrganisation", target = "fseLibelleUniteOrganisation")
    TemplateMessageDTO toDto(TemplateMessage templateMessage);

    @Mapping(source = "fseId", target = "fse.id")
    TemplateMessage toEntity(TemplateMessageDTO templateMessageDTO);

    default TemplateMessage fromId(Long id) {
        if (id == null) {
            return null;
        }
        TemplateMessage templateMessage = new TemplateMessage();
        templateMessage.setId(id);
        return templateMessage;
    }
}
