package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.DocumentDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Document and its DTO DocumentDTO.
 */
@Mapper(componentModel = "spring", uses = {TypeDocumentMapper.class, ClientMapper.class})
public interface DocumentMapper extends EntityMapper<DocumentDTO, Document> {

    @Mapping(source = "typeDocument.id", target = "typeDocumentId")
    @Mapping(source = "titulaire.id", target = "titulaireId")
    @Mapping(source = "typeDocument.libelle", target = "typeDocumentLibelle")
    @Mapping(source = "typeDocument.code", target = "typeDocumentCode")
    DocumentDTO toDto(Document document);

    @Mapping(source = "typeDocumentId", target = "typeDocument")
    @Mapping(source = "titulaireId", target = "titulaire")
    Document toEntity(DocumentDTO documentDTO);

    default Document fromId(Long id) {
        if (id == null) {
            return null;
        }
        Document document = new Document();
        document.setId(id);
        return document;
    }
}
