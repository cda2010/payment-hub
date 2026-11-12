package com.m2i.pti.service;

import com.m2i.pti.domain.DetailHistorique;
import com.m2i.pti.domain.Historique;
import com.m2i.pti.domain.enumeration.HistoryType;
import com.m2i.pti.repository.DetailHistoriqueRepository;
import com.m2i.pti.repository.HistoriqueRepository;
import com.m2i.pti.service.dto.DetailHistoriqueDTO;
import com.m2i.pti.service.mapper.DetailHistoriqueMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;


/**
 * Service Implementation for managing DetailHistorique.
 */
@Service
@Transactional
public class DetailHistoriqueService {

    private final Logger log = LoggerFactory.getLogger(DetailHistoriqueService.class);

    private final DetailHistoriqueRepository detailHistoriqueRepository;

    private final DetailHistoriqueMapper detailHistoriqueMapper;

    private final HistoriqueRepository historiqueRepository;

    public DetailHistoriqueService(DetailHistoriqueRepository detailHistoriqueRepository, DetailHistoriqueMapper detailHistoriqueMapper, HistoriqueRepository historiqueRepository) {
        this.detailHistoriqueRepository = detailHistoriqueRepository;
        this.detailHistoriqueMapper = detailHistoriqueMapper;
        this.historiqueRepository = historiqueRepository;
    }

    /**
     * Save a detailHistorique.
     *
     * @param detailHistoriqueDTO the entity to save
     * @return the persisted entity
     */
    public DetailHistoriqueDTO save(DetailHistoriqueDTO detailHistoriqueDTO) {
        log.debug("Request to save DetailHistorique : {}", detailHistoriqueDTO);
        DetailHistorique detailHistorique = detailHistoriqueMapper.toEntity(detailHistoriqueDTO);
        detailHistorique = detailHistoriqueRepository.save(detailHistorique);
        return detailHistoriqueMapper.toDto(detailHistorique);
    }

    /**
     *  Get all the detailHistoriques.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<DetailHistoriqueDTO> findAll(Pageable pageable, String typeHistorique, String dateEnregistrement,String telephone) {
        log.debug("Request to get all DetailHistoriques");
        Historique historique;
        HistoryType historyType;
        historyType= HistoryType.valueOf(typeHistorique);
        LocalDate zonedDateTime = LocalDate.parse(dateEnregistrement);
        System.out.println("---------------"+zonedDateTime+historyType);

        historique=historiqueRepository.findByCriteria(historyType,zonedDateTime);

        Page<DetailHistoriqueDTO> result;
        if(historique!=null){
            result= detailHistoriqueRepository.findByHistoriqueIdWithCriteria(historique.getId(),telephone,pageable).map(detailHistoriqueMapper::toDto);
            if(result!=null){
                return result;
            } else {
                return null;
            }
        }
        throw new CustomParameterizedException("Aucun résultat!");

    }

    /**
     *  Get one detailHistorique by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public DetailHistoriqueDTO findOne(Long id) {
        log.debug("Request to get DetailHistorique : {}", id);
        DetailHistorique detailHistorique = detailHistoriqueRepository.findById(id).orElse(null);
        return detailHistoriqueMapper.toDto(detailHistorique);
    }

    /**
     *  Delete the  detailHistorique by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete DetailHistorique : {}", id);
        detailHistoriqueRepository.deleteById(id);
    }
}
