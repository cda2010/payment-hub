package com.m2i.pti.service;

import com.m2i.pti.domain.Piece;
import com.m2i.pti.repository.PieceRepository;
import com.m2i.pti.service.dto.PieceDTO;
import com.m2i.pti.service.mapper.PieceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Service Implementation for managing Piece.
 */
@Service
@Transactional
public class PieceService {

    private final Logger log = LoggerFactory.getLogger(PieceService.class);

    private final PieceRepository pieceRepository;

    private final PieceMapper pieceMapper;

    public PieceService(PieceRepository pieceRepository, PieceMapper pieceMapper) {
        this.pieceRepository = pieceRepository;
        this.pieceMapper = pieceMapper;
    }

    /**
     * Save a piece.
     *
     * @param pieceDTO the entity to save
     * @return the persisted entity
     */
    public PieceDTO save(PieceDTO pieceDTO) {
        log.debug("Request to save Piece : {}", pieceDTO);
        Piece piece = pieceMapper.toEntity(pieceDTO);
        piece = pieceRepository.save(piece);
        return pieceMapper.toDto(piece);
    }

    /**
     * Get all the pieces.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<PieceDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Pieces");
        return pieceRepository.findAll(pageable)
                .map(pieceMapper::toDto);
    }

    public List<Piece> findByTypeAndReference(Long typeId, String reference) {
        return pieceRepository.findByTypePieceIdAndReferencePiece(typeId, reference);
    }

    /**
     * Get one piece by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public PieceDTO findOne(Long id) {
        log.debug("Request to get Piece : {}", id);
        Piece piece = pieceRepository.findById(id).orElse(null);
        return pieceMapper.toDto(piece);
    }

    /**
     * Get one piece by TitulaireId.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public PieceDTO findOneByTitulaireId(Long id) {
        log.debug("Request to get Piece : {}", id);
        PieceDTO piece = pieceRepository.findTopByTitulaireIdNew(id);
        System.out.println("--------------------");
        System.out.println("-----------PIECE---------");
        //return pieceMapper.toDto(piece);
        return piece;
    }

    @Transactional(readOnly = true)
    public List<PieceDTO> findAllByTitulaireId(Long id) {
        log.debug("Request to get Piece : {}", id);
        List<Piece> pieces = pieceRepository.findByTitulaireId(id);
        return pieceMapper.toDto(pieces);
    }

    /**
     * Delete the piece by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Piece : {}", id);
        pieceRepository.deleteById(id);
    }
}
