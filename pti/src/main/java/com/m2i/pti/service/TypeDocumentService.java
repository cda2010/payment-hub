package com.m2i.pti.service;

import com.m2i.pti.domain.TypeDocument;
import com.m2i.pti.repository.TypeDocumentRepository;
import com.m2i.pti.service.dto.TypeDocumentDTO;
import com.m2i.pti.service.mapper.TypeDocumentMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * Service Implementation for managing TypeDocument.
 */
@Service
@Transactional
public class TypeDocumentService {

    private final Logger log = LoggerFactory.getLogger(TypeDocumentService.class);

    private final TypeDocumentRepository typeDocumentRepository;

    private final TypeDocumentMapper typeDocumentMapper;

    public TypeDocumentService(TypeDocumentRepository typeDocumentRepository, TypeDocumentMapper typeDocumentMapper) {
        this.typeDocumentRepository = typeDocumentRepository;
        this.typeDocumentMapper = typeDocumentMapper;
    }

    /**
     * Save a typeDocument.
     *
     * @param typeDocumentDTO the entity to save
     * @return the persisted entity
     */
    public TypeDocumentDTO save(TypeDocumentDTO typeDocumentDTO) {
        log.debug("Request to save TypeDocument : {}", typeDocumentDTO);
        TypeDocument typeDocument = typeDocumentMapper.toEntity(typeDocumentDTO);
        typeDocument = typeDocumentRepository.save(typeDocument);
        return typeDocumentMapper.toDto(typeDocument);
    }

    /**
     *  Get all the typeDocuments.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<TypeDocumentDTO> findAll(Pageable pageable) {
        log.debug("Request to get all TypeDocuments");
        return typeDocumentRepository.findAll(pageable)
            .map(typeDocumentMapper::toDto);
    }

    /**
     *  Get one typeDocument by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public TypeDocumentDTO findOne(Long id) {
        log.debug("Request to get TypeDocument : {}", id);
        TypeDocument typeDocument = typeDocumentRepository.findById(id).orElse(null);
        return typeDocumentMapper.toDto(typeDocument);
    }

    /**
     *  Delete the  typeDocument by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete TypeDocument : {}", id);
        typeDocumentRepository.deleteById(id);
    }
}
