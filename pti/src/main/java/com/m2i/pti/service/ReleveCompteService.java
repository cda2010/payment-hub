package com.m2i.pti.service;

import com.m2i.pti.domain.*;
import com.m2i.pti.repository.*;
import com.m2i.pti.service.dto.ReleveCompteDTO;
import com.m2i.pti.service.mapper.ReleveCompteMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;


/**
 * Service Implementation for managing ReleveCompte.
 */
@Service
@Transactional
public class ReleveCompteService {

    private final Logger log = LoggerFactory.getLogger(ReleveCompteService.class);

    private final ReleveCompteRepository releveCompteRepository;

    private final ReleveCompteMapper releveCompteMapper;
    private final CompteRepository compteRepository;
    private final TransactionComptePtiRepository transactionComptePtiRepository;
    private final DetailReleveCompteRepository detailReleveCompteRepository;
    public ReleveCompteService(ReleveCompteRepository releveCompteRepository, ReleveCompteMapper releveCompteMapper, ClientRepository clientRepository, CompteRepository compteRepository, TransactionComptePtiRepository transactionComptePtiRepository, DetailReleveCompteRepository detailReleveCompteRepository) {
        this.releveCompteRepository = releveCompteRepository;
        this.releveCompteMapper = releveCompteMapper;
        this.compteRepository = compteRepository;
        this.transactionComptePtiRepository = transactionComptePtiRepository;
        this.detailReleveCompteRepository = detailReleveCompteRepository;
    }

    /**
     * Save a releveCompte.
     *
     * @param releveCompteDTO the entity to save
     * @return the persisted entity
     */
    public ReleveCompteDTO save(ReleveCompteDTO releveCompteDTO) {
        log.debug("Request to save ReleveCompte : {}", releveCompteDTO);
        ReleveCompte releveCompte = releveCompteMapper.toEntity(releveCompteDTO);
        releveCompte = releveCompteRepository.save(releveCompte);
        return releveCompteMapper.toDto(releveCompte);
    }

    /**
     *  Get all the releveComptes.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<ReleveCompteDTO> findAll(Pageable pageable) {
        log.debug("Request to get all ReleveComptes");
        return releveCompteRepository.findAll(pageable)
            .map(releveCompteMapper::toDto);
    }

    /**
     *  Get one releveCompte by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public ReleveCompteDTO findOne(Long id) {
        log.debug("Request to get ReleveCompte : {}", id);
        ReleveCompte releveCompte = releveCompteRepository.findById(id).orElse(null);
        return releveCompteMapper.toDto(releveCompte);
    }

    public void generateReleveOfPastMonth(){
        List<Compte> comptes=compteRepository.findComptesAutres();//liste des comptes autres
        ZonedDateTime dateDebut=ZonedDateTime.now().minusMonths(1);
        dateDebut=dateDebut.minusDays(dateDebut.getDayOfMonth()-1).minusHours(dateDebut.getHour()).minusMinutes(dateDebut.getMinute()).minusSeconds(dateDebut.getSecond());//date de debut du mois precedent
        ZonedDateTime dateFin=ZonedDateTime.now();
        dateFin=dateFin.minusDays(dateFin.getDayOfMonth()-1).minusHours(dateFin.getHour()).minusMinutes(dateFin.getMinute()).minusSeconds(dateFin.getSecond()+1);//date de fin du mois precedent
        if(comptes!=null){//si la liste des comptes est non nulle
            ZonedDateTime finalDateDebut = dateDebut;
            ZonedDateTime finalDateFin = dateFin;
            comptes.forEach(compte -> {//on parcour les comptes
                CompteAutre compteAutre=compte.getCompteAutre();//on recupère le compteAutre
                if(compteAutre!=null) {//Si le compteAutre es non null
                    //On recupere la liste des transactions du compte qui sont entre dateDebut et dateFin
                    List<TransactionComptePti> transactions=transactionComptePtiRepository.findByCompteAutreAndDateBetweenOrderByDateDesc(compteAutre, finalDateDebut, finalDateFin);
                        ReleveCompte  releveCompte=null;
                        List<ReleveCompte> releveComptes=releveCompteRepository.findOptionalByCompteAndDateDebutAndDateFin(compte.getId());
                        if(releveComptes!=null){
                            for (ReleveCompte releveCompteOld : releveComptes) {
                                if (releveCompteOld.getDateDebut().getMonthValue() == finalDateDebut.getMonthValue()&&releveCompteOld.getDateDebut().getYear() == finalDateDebut.getYear()) {
                                    releveCompte = releveCompteOld;
                                }
                            }
                        }
                        if(releveCompte!=null&&releveCompte.getId()!=null){//On supprime les anciens details du relevé existant
                            detailReleveCompteRepository.deleteByReleve(releveCompte);
                        }else {//On cree un nouveau releve s'il n'ya pas de releve pour le compte dans la periode
                            releveCompte=new ReleveCompte();
                            releveCompte.setCompte(compte);
                            releveCompte.setDateDebut(finalDateDebut);
                            releveCompte.setDateFin(finalDateFin);
                        }
                        if(transactions!=null&&!transactions.isEmpty()){
                            releveCompte=releveCompteRepository.save(releveCompte);
                            releveCompte.setSoldeInitial(transactions.get(0).getSoldeCourant());
                            releveCompte.setSoldeFinal(transactions.get(transactions.size()-1).getSoldeApres());
                            for (TransactionComptePti transaction : transactions) {
                                DetailReleveCompte detail=new DetailReleveCompte();
                                detail.setDate(transaction.getDate());
                                detail.setEtat(transaction.getEtat());
                                detail.setMontant(transaction.getMontant());
                                detail.setSoldeApres(transaction.getSoldeApres());
                                detail.setSoldeCourant(transaction.getSoldeCourant());
                                detail.setType(transaction.getType());
                                detail.setReleve(releveCompte);
                                detailReleveCompteRepository.save(detail);
                            }
                        }else {
                            releveCompte.setSoldeInitial(compteAutre.getSolde());
                            releveCompte.setSoldeFinal(compteAutre.getSolde());
                            releveCompteRepository.save(releveCompte);
                        }
                }
            });
        }
    }

    /**
     *  Delete the  releveCompte by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete ReleveCompte : {}", id);
        releveCompteRepository.deleteById(id);
    }
}
