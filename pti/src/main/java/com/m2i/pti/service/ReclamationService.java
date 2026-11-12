package com.m2i.pti.service;

import com.m2i.pti.domain.Reclamation;
import com.m2i.pti.domain.enumeration.EtatReclamation;
import com.m2i.pti.domain.enumeration.QualiteReclamation;
import com.m2i.pti.repository.ReclamationRepository;
import com.m2i.pti.service.dto.UniteOrganisationDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service Implementation for managing {@link Reclamation}.
 */
@Service
@Transactional
public class ReclamationService {

    private final Logger log = LoggerFactory.getLogger(ReclamationService.class);

    private final ReclamationRepository reclamationRepository;

    public ReclamationService(ReclamationRepository reclamationRepository) {
        this.reclamationRepository = reclamationRepository;
    }

    /**
     * Save a reclamation.
     *
     * @param reclamation the entity to save.
     * @return the persisted entity.
     */
    public Reclamation save(Reclamation reclamation) {
        log.debug("Request to save Reclamation : {}", reclamation);
        return reclamationRepository.save(reclamation);
    }

    /**
     * Get all the reclamations.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<Reclamation> findAll() {
        log.debug("Request to get all Reclamations");
        return reclamationRepository.findAll();
    }
    /**
     * Get one reclamation by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<Reclamation> findOne(Long id) {
        log.debug("Request to get Reclamation : {}", id);
        return reclamationRepository.findById(id);
    }

    /**
     * Delete the reclamation by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Reclamation : {}", id);
        reclamationRepository.deleteById(id);
    }

    public Page<Reclamation> findAll(Long titre, EtatReclamation etat, QualiteReclamation qualiteReclamation, String telephoneAuteur, ZonedDateTime dateCreation, ZonedDateTime dateModification, Pageable pageable) {
    List<Reclamation> list= reclamationRepository.findByCriteria(titre, etat, qualiteReclamation, telephoneAuteur);
    List<Reclamation> result=new ArrayList<Reclamation>();
    list.forEach(item->{
        if((dateCreation==null || (item.getDateHeureCreation().getYear()==dateCreation.getYear()
        &&item.getDateHeureCreation().getMonthValue()==dateCreation.getMonthValue()
        &&item.getDateHeureCreation().getDayOfYear()==dateCreation.getDayOfYear()))
        &&(dateModification==null ||( item.getDateModification()!=null && (item.getDateModification().getYear()==dateModification.getYear()
                &&item.getDateModification().getMonthValue()==dateModification.getMonthValue()
                &&item.getDateModification().getDayOfYear()==dateModification.getDayOfYear())))){
            result.add(item);
        }
    });
        int start= (int) pageable.getOffset();
        int end=(start+pageable.getPageSize())>result.size()?result.size():(start+pageable.getPageSize());
        Page<Reclamation> retour=new PageImpl<Reclamation>(result.subList(start,end),pageable,result.size());
        return retour;
    }
}
