package com.m2i.pti.service;

import com.m2i.pti.domain.TypePiece;
import com.m2i.pti.repository.PieceRepository;
import com.m2i.pti.repository.TypePieceRepository;
import com.m2i.pti.service.dto.TypePieceDTO;
import com.m2i.pti.service.mapper.TypePieceMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


/**
 * Service Implementation for managing TypePiece.
 */
@Service
@Transactional
public class TypePieceService {

    private final Logger log = LoggerFactory.getLogger(TypePieceService.class);

    private final TypePieceRepository typePieceRepository;

    private final TypePieceMapper typePieceMapper;

    private final PieceRepository pieceRepository;

    public TypePieceService(TypePieceRepository typePieceRepository, TypePieceMapper typePieceMapper, PieceRepository pieceRepository) {
        this.typePieceRepository = typePieceRepository;
        this.typePieceMapper = typePieceMapper;
        this.pieceRepository = pieceRepository;
    }

    /**
     * Save a typePiece.
     *
     * @param typePieceDTO the entity to save
     * @return the persisted entity
     */
    public TypePieceDTO save(TypePieceDTO typePieceDTO) {
        log.debug("Request to save TypePiece : {}", typePieceDTO);
        Optional<TypePiece> typePieceExistWithCode = typePieceRepository.findByCodeTypePiece(typePieceDTO.getCodeTypePiece());
        if (typePieceExistWithCode.isPresent() && (!typePieceExistWithCode.get().getId().equals(typePieceDTO.getId()))) {
            throw new CustomParameterizedException("Le code est déjà utilisé !");
        }

        Optional<TypePiece> typePieceExistWithLibelle = typePieceRepository.findByLibelleTypePiece(typePieceDTO.getLibelleTypePiece());
        if (typePieceExistWithLibelle.isPresent() && !typePieceExistWithLibelle.get().getId().equals(typePieceDTO.getId())) {
            throw new CustomParameterizedException("Le libelle est déjà utilisé !");
        }
        TypePiece typePiece = typePieceMapper.toEntity(typePieceDTO);
        typePiece = typePieceRepository.save(typePiece);
        return typePieceMapper.toDto(typePiece);
    }

    /**
     * Get all the typePieces.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<TypePieceDTO> findAll(Pageable pageable) {
        log.debug("Request to get all TypePieces");
        return typePieceRepository.findAll(pageable)
                .map(typePieceMapper::toDto);
    }

    /**
     * Get one typePiece by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public TypePieceDTO findOne(Long id) {
        log.debug("Request to get TypePiece : {}", id);
        TypePiece typePiece = typePieceRepository.findById(id).orElse(null);
        return typePieceMapper.toDto(typePiece);
    }

    /**
     * Delete the typePiece by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete TypePiece : {}", id);
        if (pieceRepository.findByTypePieceId(id).isEmpty())
            typePieceRepository.deleteById(id);
        else {
            throw new CustomParameterizedException("Impossible de supprimer,Ce type de pièce est déjà utilisé");
        }
    }
}
