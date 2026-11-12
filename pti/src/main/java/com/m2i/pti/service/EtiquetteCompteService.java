package com.m2i.pti.service;

import com.m2i.pti.domain.EtiquetteCompte;
import com.m2i.pti.domain.EtiquetteLibelle;
import com.m2i.pti.domain.SommeSolde;
import com.m2i.pti.repository.EtiquetteCompteRepository;
import com.m2i.pti.service.dto.EtiquetteCompteDTO;
import com.m2i.pti.service.mapper.EtiquetteCompteMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Service Implementation for managing EtiquetteCompte.
 */
@Service
@Transactional
public class EtiquetteCompteService {

    private final Logger log = LoggerFactory.getLogger(EtiquetteCompteService.class);

    private final EtiquetteCompteRepository etiquetteCompteRepository;

    private final EtiquetteCompteMapper etiquetteCompteMapper;

    public EtiquetteCompteService(EtiquetteCompteRepository etiquetteCompteRepository, EtiquetteCompteMapper etiquetteCompteMapper) {
        this.etiquetteCompteRepository = etiquetteCompteRepository;
        this.etiquetteCompteMapper = etiquetteCompteMapper;
    }

    /**
     * Save a etiquetteCompte.
     *
     * @param etiquetteCompteDTO the entity to save
     * @return the persisted entity
     */
    public EtiquetteCompteDTO save(EtiquetteCompteDTO etiquetteCompteDTO) {
        log.debug("Request to save EtiquetteCompte : {}", etiquetteCompteDTO);
        if(etiquetteCompteDTO.getId()==null) {
            if (etiquetteCompteRepository.findTop1ByCode(etiquetteCompteDTO.getCode()) != null) {
                throw new CustomParameterizedException("Cette etiquette existe déja! ");
            }
        }
        if(etiquetteCompteDTO.getId()!=null){
            if(!etiquetteCompteRepository.findById(etiquetteCompteDTO.getId()).orElse(null).getCode().equals(etiquetteCompteDTO.getCode()) && etiquetteCompteRepository.findTop1ByCode(etiquetteCompteDTO.getCode())!=null){
                throw new CustomParameterizedException("Cette etiquette existe déja! ");
            }
        }
        if(etiquetteCompteDTO.isTransversal()==null){
            etiquetteCompteDTO.setTransversal(false);
        }
        EtiquetteCompte etiquetteCompte = etiquetteCompteMapper.toEntity(etiquetteCompteDTO);
        etiquetteCompte = etiquetteCompteRepository.save(etiquetteCompte);
        return etiquetteCompteMapper.toDto(etiquetteCompte);
    }

    /**
     * Get all the etiquetteComptes.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<EtiquetteCompteDTO> findAll(Pageable pageable) {
        log.debug("Request to get all EtiquetteComptes");
        return etiquetteCompteRepository.findAll(pageable)
            .map(etiquetteCompteMapper::toDto);
    }

    /**
     * Get all the etiquetteComptes.
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<EtiquetteCompteDTO> findAllList() {
        log.debug("Request to get all EtiquetteComptes");
        return etiquetteCompteMapper.toDto(etiquetteCompteRepository.findAll());

    }

    /**
     * Get one etiquetteCompte by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public EtiquetteCompteDTO findOne(Long id) {
        log.debug("Request to get EtiquetteCompte : {}", id);
        EtiquetteCompte etiquetteCompte = etiquetteCompteRepository.findById(id).orElse(null);
        return etiquetteCompteMapper.toDto(etiquetteCompte);
    }

    /**
     * Delete the etiquetteCompte by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete EtiquetteCompte : {}", id);
        etiquetteCompteRepository.deleteById(id);
    }
    public EtiquetteLibelle getEtiquetteName(String libelle) {
        EtiquetteLibelle libel = new EtiquetteLibelle();
       String lib = etiquetteCompteRepository.finByEtiquette(libelle);
       libel.setLibelle(lib);
       return libel;
    }
    public List<EtiquetteCompte> getEtiquetteNamePTI() {
        return etiquetteCompteRepository.finByEtiquettePTI();
    }

    public HashMap getListeLib(List<String> liste) {
        Map<Integer,String> listes = new HashMap<>();
        int i=0;
        for (String l: liste){
            EtiquetteCompte lib = etiquetteCompteRepository.findTop1ByCode(l);
            if(lib!=null)
            {
                i=i+1;
                listes.put(i,lib.getLibelle());
            }
        }
        return (HashMap) listes;
    }
}
