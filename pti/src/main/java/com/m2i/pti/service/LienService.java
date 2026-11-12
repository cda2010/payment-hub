package com.m2i.pti.service;

import com.m2i.pti.domain.*;
import com.m2i.pti.repository.LienRepository;
import com.m2i.pti.repository.PersonneRepository;
import com.m2i.pti.repository.PieceRepository;
import com.m2i.pti.service.dto.*;
import com.m2i.pti.service.mapper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


/**
 * Service Implementation for managing Lien.
 */
@Service
@Transactional
public class LienService {

    private final Logger log = LoggerFactory.getLogger(LienService.class);

    private final LienRepository lienRepository;

    private final LienMapper lienMapper;

    private final PersonneMapper personneMapper;

    private final PieceMapper pieceMapper;

    private final PersonnePhysiqueMapper personnePhysiqueMapper;

    private final PersonneMoraleMapper personneMoraleMapper;

    private final PersonneService personneService;

    private final PersonneRepository personneRepository;

    private final PieceRepository pieceRepository;

    public LienService(LienRepository lienRepository, LienMapper lienMapper, PersonneMapper personneMapper, PieceMapper pieceMapper, PersonnePhysiqueMapper personnePhysiqueMapper, PersonneMoraleMapper personneMoraleMapper, PersonneService personneService, PersonneRepository personneRepository, PieceRepository pieceRepository) {
        this.lienRepository = lienRepository;
        this.lienMapper = lienMapper;
        this.personneMapper = personneMapper;
        this.pieceMapper = pieceMapper;
        this.personnePhysiqueMapper = personnePhysiqueMapper;
        this.personneMoraleMapper = personneMoraleMapper;
        this.personneService = personneService;
        this.personneRepository = personneRepository;
        this.pieceRepository = pieceRepository;
    }

    /**
     * Save a lien.
     *
     * @param lienDTO the entity to save
     * @return the persisted entity
     */
    public LienDTO save(LienDTO lienDTO) {
        log.debug("Request to save Lien : {}", lienDTO);
        Lien lien = lienMapper.toEntity(lienDTO);
        lien = lienRepository.save(lien);
        return lienMapper.toDto(lien);
    }

    /**
     *  Get all the liens.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<LienDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Liens");
        return lienRepository.findAll(pageable)
            .map(lienMapper::toDto);
    }

    /**
     *  Get one lien by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public LienDTO findOne(Long id) {
        log.debug("Request to get Lien : {}", id);
        Lien lien = lienRepository.findById(id).orElse(null);
        return lienMapper.toDto(lien);
    }

    /**
     *  Delete the  lien by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Lien : {}", id);
        lienRepository.deleteById(id);
    }

    public PersonneAllDTO getLienOfLien(Long lienId) {
        List<Lien> liens=new ArrayList<>();
        Personne personne=new Personne();
        PersonneAllDTO personneAllDTO=new PersonneAllDTO();
        PieceDTO pieceDTO=new PieceDTO();
        Lien lien;
        lien=lienRepository.getOne(lienId);
        if(lien!=null){
            System.out.println("------------"+lien);
            System.out.println("------------"+lien.getLienPotentiel());
            personne =personneRepository.getOne(lien.getLienPotentiel());

            System.out.println("------------"+personne);
            System.out.println("------------");
            personneMapper.toDto(personne);
            Piece piece=pieceRepository.findTopByTitulaireId(personne.getId());
            pieceDTO=pieceMapper.toDto(piece);
            PersonneMorale personneMorale =personne.getPersonneMorale();
            PersonnePhysique personnePhysique=personne.getPersonnePhysique();
            personneAllDTO.setPersonneDTO(personneMapper.toDto(personne));
            personneAllDTO.setPersonneMoraleDTO(personneMoraleMapper.toDto(personneMorale));
            personneAllDTO.setPersonnePhysiqueDTO(personnePhysiqueMapper.toDto(personnePhysique));
            personneAllDTO.setPieceDTO(pieceDTO);
            return personneAllDTO;
        }else {
            return null;
        }
    }

    public List<Lien> saveDoublon(DoublonDTO listP) {
       return personneService.saveDoublon(listP);
    }
}
