package com.m2i.pti.service;

import com.m2i.pti.domain.*;
import com.m2i.pti.domain.enumeration.EtiquetteCompteCode;
import com.m2i.pti.domain.enumeration.HistoryType;
import com.m2i.pti.domain.enumeration.TypeUO;
import com.m2i.pti.domain.util.JsonLigneHistoriqueCompte;
import com.m2i.pti.repository.*;
import com.m2i.pti.service.dto.CompteDTO;
import com.m2i.pti.service.dto.TypeUniteOrganisationDTO;
import com.m2i.pti.service.dto.UniteOrganisationDTO;
import com.m2i.pti.service.mapper.UniteOrganisationMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Service Implementation for managing Historique.
 */
@Service
@Transactional
public class HistoriqueService {

    private final Logger log = LoggerFactory.getLogger(HistoriqueService.class);

    private final HistoriqueRepository historiqueRepository;

    private final UniteOrganisationService uniteOrganisationService;

    private final UniteOrganisationMapper uniteOrganisationMapper;

    private final DetailHistoriqueRepository detailHistoriqueRepository;

    private final CompteService compteService;

    private final CompteAutreService compteAutreService;
    private final ClientService clientService;
    private final CompteRepository compteRepository;
    private final TypeUniteOrganisationRepository typeUniteOrganisationRepository;
    private final UniteOrganisationRepository uniteOrganisationRepository;

    public HistoriqueService(CompteAutreService compteAutreService, HistoriqueRepository historiqueRepository, UniteOrganisationService uniteOrganisationService, UniteOrganisationMapper uniteOrganisationMapper, DetailHistoriqueRepository detailHistoriqueRepository, CompteService compteService, ClientService clientService, CompteRepository compteRepository, TypeUniteOrganisationRepository typeUniteOrganisationRepository, UniteOrganisationRepository uniteOrganisationRepository) {
        this.historiqueRepository = historiqueRepository;
        this.uniteOrganisationService = uniteOrganisationService;
        this.uniteOrganisationMapper = uniteOrganisationMapper;
        this.detailHistoriqueRepository = detailHistoriqueRepository;
        this.compteService = compteService;
        this.clientService = clientService;
        this.compteRepository = compteRepository;
        this.typeUniteOrganisationRepository = typeUniteOrganisationRepository;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.compteAutreService = compteAutreService;
    }

    /**
     * Save a historique.
     *
     * @param historique the entity to save
     * @return the persisted entity
     */
    public Historique save(Historique historique) {
        log.debug("Request to save Historique : {}", historique);
        return historiqueRepository.save(historique);
    }

    /**
     * Get all the historiques.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Historique> findAll(Pageable pageable) {
        log.debug("Request to get all Historiques");
        return historiqueRepository.findAll(pageable);
    }

    /**
     * Get one historique by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Historique findOne(Long id) {
        log.debug("Request to get Historique : {}", id);
        return historiqueRepository.findById(id).orElse(null);
    }

    /**
     * Delete the historique by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Historique : {}", id);
        historiqueRepository.deleteById(id);
    }

    public void historiqueTableauSolde(String codeUo) {
        UniteOrganisation uo = uniteOrganisationService.findByCode(codeUo);
        log.debug("****************{ }" + codeUo);
        log.debug("****************{ }" + uo);
        if (uo != null) {
            HashMap result = uniteOrganisationService.getAllSolde(uo.getId());
            UniteOrganisationDTO fse = uniteOrganisationService.findOne(uo.getId());
            Historique historique = new Historique();

            historique.setEtiquette(HistoryType.TABLEAUSOLDE);
            historique.setFournisseurService(fse.getCodeFse());
            ZonedDateTime zonedDateTime = ZonedDateTime.now();
            historique.setDateHistorique(zonedDateTime);

            Long nombre = historiqueRepository.findCount(historique.getEtiquette(), zonedDateTime.toLocalDate());
            if (nombre <= 0) {
                historique = historiqueRepository.save(historique);
                for (Object o : result.entrySet()) {
                    DetailHistorique detailHistorique = new DetailHistorique();
                    Map.Entry<String, BigDecimal> set = (Map.Entry<String, BigDecimal>) o;
                    System.out.println(set.getKey() + " = " + set.getValue());

                    if (set.getKey().equals("distribution")) {
                        detailHistorique.setSolde(set.getValue());
                        detailHistorique.setLibelle("Somme Compte distribution");
                        detailHistorique.setHistorique(historique);
                        detailHistoriqueRepository.save(detailHistorique);
                    }
                    if (set.getKey().equals("clients")) {
                        detailHistorique.setSolde(set.getValue());
                        detailHistorique.setLibelle("Somme des Soldes clients");
                        detailHistorique.setHistorique(historique);
                        detailHistoriqueRepository.save(detailHistorique);
                    }
                    if (set.getKey().equals("marchands")) {
                        detailHistorique.setSolde(set.getValue());
                        detailHistorique.setLibelle("Somme des Soldes marchands");
                        detailHistorique.setHistorique(historique);
                        detailHistoriqueRepository.save(detailHistorique);
                    }
                    if (set.getKey().equals("master")) {
                        detailHistorique.setSolde(set.getValue());
                        detailHistorique.setLibelle("Somme des Soldes agents");
                        detailHistorique.setHistorique(historique);
                        detailHistoriqueRepository.save(detailHistorique);
                    }
                    if (set.getKey().equals("agence")) {
                        detailHistorique.setSolde(set.getValue());
                        detailHistorique.setLibelle("Somme des Soldes agences");
                        detailHistorique.setHistorique(historique);
                        detailHistoriqueRepository.save(detailHistorique);
                    }
                    if (set.getKey().equals("commission")) {
                        detailHistorique.setSolde(set.getValue());
                        detailHistorique.setLibelle("Somme des Soldes des commissions");
                        detailHistorique.setHistorique(historique);
                        detailHistoriqueRepository.save(detailHistorique);
                    }
                    if (set.getKey().equals("commissionAtt")) {
                        detailHistorique.setSolde(set.getValue());
                        detailHistorique.setLibelle("Somme des Soldes des commissions en attentes");
                        detailHistorique.setHistorique(historique);
                        detailHistoriqueRepository.save(detailHistorique);
                    }
                    if (set.getKey().equals("transfertAtt")) {
                        detailHistorique.setSolde(set.getValue());
                        detailHistorique.setLibelle("Somme des Soldes des transferts en attentes");
                        detailHistorique.setHistorique(historique);
                        detailHistoriqueRepository.save(detailHistorique);
                    }
                    if (set.getKey().equals("taxe")) {
                        detailHistorique.setSolde(set.getValue());
                        detailHistorique.setLibelle("Somme des Soldes taxes");
                        detailHistorique.setHistorique(historique);
                        detailHistoriqueRepository.save(detailHistorique);
                    }

                    if (set.getKey().equals("plateforme")) {
                        detailHistorique.setSolde(set.getValue());
                        detailHistorique.setLibelle("Somme des frais de la plateforme");
                        detailHistorique.setHistorique(historique);
                        detailHistoriqueRepository.save(detailHistorique);
                    }

                }
            }
        } else {
            throw new CustomParameterizedException("Impossible d'exécuter la fonction.");
        }

    }

 
private   Historique   saveHistorique(){
     Historique historique = new Historique();
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        historique.setDateHistorique(zonedDateTime);
        historique.setEtiquette(HistoryType.SOLDECLIENT);
          Long nombre = historiqueRepository.findCount(historique.getEtiquette(), zonedDateTime.toLocalDate());
         if (nombre <= 0) {
            historique = historiqueRepository.saveAndFlush(historique);
            return historique;
         }else{
             return null;
         }
        
}
    @Transactional(readOnly = true)
    private List<DetailHistorique> getDetail(List<CompteDTO> compteClient) {
        List<DetailHistorique> listeDetailHist = new ArrayList();
        String libelleClient = null;
        for (CompteDTO cpt : compteClient) {
            if (cpt.getCompteAutreId() != null) {
                DetailHistorique txtdetailHistorique = new DetailHistorique();
                Client client = clientService.findClient(cpt.getClientId());
                if (client != null) {
                    if (client.getPersonne() != null) {
                        if (client.getPersonne().getPersonnePhysique() != null) {
                            libelleClient = client.getPersonne().getPersonnePhysique().getNomClient() + " " + client.getPersonne().getPersonnePhysique().getPrenomClient();
                        } else if (client.getPersonne().getPersonneMorale() != null) {
                            libelleClient = client.getPersonne().getPersonneMorale().getRaisonSociale();
                        }
                        txtdetailHistorique.setTelephone(client.getPersonne().getTelephone());
                        txtdetailHistorique.setLibelle(libelleClient);
                    }
                }
                txtdetailHistorique.setNumeroCpt(cpt.getValeurCompte());
                txtdetailHistorique.setSolde(cpt.getSoldeCompte());
                listeDetailHist.add(txtdetailHistorique);

            }

        }

        return listeDetailHist;
    }

    public void historiqueSoldeClient() {
       Historique historique =   saveHistorique();
        if (historique !=null) {
        //    historique = historiqueRepository.saveAndFlush(historique);
            Long nombreCompte = compteService.findNbreCompteActif();
            Long limit = 100000L;
            Long i = 0L;
           
            while (i < nombreCompte) {
                 List<DetailHistorique> listeDetailHistSave = new ArrayList();
                DetailHistorique detailHistorique = new DetailHistorique();
                JsonLigneHistoriqueCompte txttLigneHistoriqueCompte = new JsonLigneHistoriqueCompte();
                List<CompteDTO> compteClient = compteService.findCompteAllClientActif(limit, i);
                List<DetailHistorique> listeDetailHist = getDetail(compteClient);
                txttLigneHistoriqueCompte.setLigneDetailHistorique(listeDetailHist);
                detailHistorique.setHistorique(historique);
                detailHistorique.setTxtLigneHistoriqueCompte(txttLigneHistoriqueCompte);
                listeDetailHistSave.add(detailHistorique);
                System.out.println("======================="+i+"=========================");
                i = i + limit;
                 detailHistoriqueRepository.saveAll(listeDetailHistSave);
            }
          
        }
        //historique.setFournisseurService(fse.getCodeFse());

    }

    public void historiqueSoldeAgent() {
        List<String> etiquettes = Arrays.asList(EtiquetteCompteCode.CPTEOPERATIONACTEUR.name(), EtiquetteCompteCode.CPTECOMACTEUR.name());
        Historique historique = new Historique();
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        historique.setDateHistorique(zonedDateTime);
        historique.setEtiquette(HistoryType.SOLDEAGENT);
        historique = historiqueRepository.save(historique);
        if (historique != null) {
            saveHistoriqueOrganisation(etiquettes, historique);
        }
    }

    public void saveHistoriqueOrganisation(List<String> etiquettes, Historique historique) {
        int totalUo = uniteOrganisationRepository.countFindForViewPaginated(null, null, null, null, null, null, null);
        Long taille = (long) totalUo;
        List<UniteOrganisationDTO> listeAgent = uniteOrganisationRepository.findAllForViewPaginatedOptimise(null, null, null, null, null, null, null, taille, (long) 0);
        if (listeAgent != null) {
            listeAgent.forEach(agent -> {
                log.debug("etiquette***----" + etiquettes);
                List<CompteDTO> compteDTO = compteRepository.findCptOpAndCptComByUoIdOpt(agent.getId(), etiquettes);
                log.debug("compte***-- {}" + compteDTO);
                if (compteDTO != null) {
                    compteDTO.forEach(cpt -> {
                        DetailHistorique agentDetails = new DetailHistorique();
                        agentDetails.setNumeroCpt(cpt.getValeurCompte());
                        agentDetails.setSolde(cpt.getSoldeCompte());
                        agentDetails.setTelephone(agent.getTelephone());
                        agentDetails.setLibelle(cpt.getEtiquette());
                        agentDetails.setCodeUo(agent.getCodeUO());
                        agentDetails.setTypeUo(agent.getTypeUOLibelleTypeUO());
                        agentDetails.setDescription(agent.getLibelleUniteOrganisation());
                        agentDetails.setHistorique(historique);
                        detailHistoriqueRepository.save(agentDetails);
                    });
                }
            });
        }
    }
}
