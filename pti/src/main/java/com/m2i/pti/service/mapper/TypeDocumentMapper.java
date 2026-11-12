package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.TypeDocumentDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity TypeDocument and its DTO TypeDocumentDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface TypeDocumentMapper extends EntityMapper<TypeDocumentDTO, TypeDocument> {

    

    

    default TypeDocument fromId(Long id) {
        if (id == null) {
            return null;
        }
        TypeDocument typeDocument = new TypeDocument();
        typeDocument.setId(id);
        return typeDocument;
    }
}
