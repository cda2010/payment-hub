package com.m2i.omnicanal.application.service;


import com.m2i.omnicanal.application.domain.CompteFse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Service Interface for managing Compte.
 */
public interface CompteService {

    /**
     * Save a compte.
     *
     * @param compte the entity to save
     * @return the persisted entity
     */
    CompteFse save(CompteFse compte);

    /**
     *  Get all the comptes.
     *  
     *  @return the list of entities
     */
    List<CompteFse> findAll();

    /**
     *  Get the "id" compte.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    CompteFse findOne(Long id);

    /**
     *  Delete the "id" compte.
     *
     *  @param id the id of the entity
     */
    void delete(Long id);

    public List<CompteFse> findByRelation(Long idrelation);
    public CompteFse findCompteFse(String codeFseTitulaire, String codeParametre, String codeUoTitulaire, Long codeTypeService);

    public List<CompteFse> findCompteMorroir(Long idCompte);

    public Page<CompteFse> findAll(Pageable pageable, Long teneur, Long titulaire, Long type);
}
