package com.m2i.pti.service;

import com.m2i.pti.domain.GroupRepartition;
import com.m2i.pti.domain.HistoriqueGroupeProfilActeur;
import com.m2i.pti.domain.HoraireAuthentification;
import com.m2i.pti.domain.LigneGroupeProfilTemplate;
import com.m2i.pti.domain.ProfilActeur;
import com.m2i.pti.domain.TableauCumul;
import com.m2i.pti.domain.TableauFrais;
import com.m2i.pti.security.SecurityUtils;
import com.m2i.pti.domain.TableauRepartitionCommission;
import com.m2i.pti.domain.enumeration.TypeProfilActeur;
import com.m2i.pti.repository.HistoriqueGroupeProfilActeurRepository;
import com.m2i.pti.repository.ProfilActeurRepository;
import com.m2i.pti.repository.TableauRepartitionCommissionRepository;
import com.m2i.pti.service.dto.DonneeDTO;
import com.m2i.pti.service.dto.ProfilActeurDTO;
import com.m2i.pti.service.dto.TableauRepartitionCommissionDTO;
import com.m2i.pti.service.mapper.ProfilActeurMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import java.util.ArrayList;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;


/**
 * Service Implementation for managing ProfilActeur.
 */
@Service
@Transactional
public class ProfilActeurService {

    private final Logger log = LoggerFactory.getLogger(ProfilActeurService.class);

    private final ProfilActeurRepository profilActeurRepository;

    private final ProfilActeurMapper profilActeurMapper;
    private final TableauRepartitionCommissionRepository tableauRepartitionCommissionRepository;
    private final HistoriqueGroupeProfilActeurRepository historiqueGroupeProfilActeurRepository ;

    public ProfilActeurService(ProfilActeurRepository profilActeurRepository, ProfilActeurMapper profilActeurMapper, TableauRepartitionCommissionRepository tableauRepartitionCommissionRepository,HistoriqueGroupeProfilActeurRepository historiqueGroupeProfilActeurRepository) {
        this.profilActeurRepository = profilActeurRepository;
        this.profilActeurMapper = profilActeurMapper;
        this.tableauRepartitionCommissionRepository = tableauRepartitionCommissionRepository;
        this.historiqueGroupeProfilActeurRepository=historiqueGroupeProfilActeurRepository;
    }

    /**
     * Save a profilActeur.
     *
     * @param profilActeurDTO the entity to save
     * @return the persisted entity
     */
    public ProfilActeurDTO save(ProfilActeur profilActeurDTO) {
        log.debug("Request to save ProfilActeur : {}", profilActeurDTO);
        verifDefaulProfil(profilActeurDTO);
        ProfilActeur profilActeur;
        if(profilActeurDTO.getGroupRepartitions()!=null && profilActeurDTO.getGroupRepartitions().size()>0){
            Set<TableauRepartitionCommission> tableauRepartitionCommission=new HashSet<>();
            for (GroupRepartition groupRepartition:profilActeurDTO.getGroupRepartitions()){
                tableauRepartitionCommission.addAll
                        (tableauRepartitionCommissionRepository.findAllByGroup(groupRepartition));
            }
            profilActeurDTO.setTableauRepartitionCommissions(tableauRepartitionCommission);
        }else {
            profilActeurDTO.setTableauRepartitionCommissions(null);
        }
    //    ProfilActeur profilActeur = profilActeurMapper.toEntity(profilActeurDTO);
        profilActeur = profilActeurRepository.save(profilActeurDTO);
        return profilActeurMapper.toDto(profilActeur);
    }

    /**
     *
     * @param profilActeurDTO
     */
    public void verifDefaulProfil(ProfilActeur profilActeurDTO) {
        Optional<ProfilActeur> profilExist = profilActeurRepository.findByTypeAndActiveTrue(profilActeurDTO.getType());
        if (profilExist.isPresent()) {
            if (profilActeurDTO.getId() == null) {
                if (profilActeurDTO.isActive() == Boolean.TRUE)
                    throw new CustomParameterizedException("Il existe déja un profil par defaut !");
            } else {
                ProfilActeur oldProfil = profilActeurRepository.findById(profilActeurDTO.getId()).orElse(null);
                if ((profilActeurDTO.isActive() != oldProfil.isActive())
                        || profilActeurDTO.getType() != oldProfil.getType()) {
                    if (profilActeurDTO.isActive() == Boolean.TRUE)
                        throw new CustomParameterizedException("Il existe déja un profil par defaut!");
                }
            }
        }
    }

    /**
     *  Get all the profilActeurs.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<ProfilActeur> findAll(Pageable pageable) {
        log.debug("Request to get all ProfilActeurs");
        return profilActeurRepository.findAll(pageable);
          
    }

    /**
     *  Get one profilActeur by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public ProfilActeur findOne(Long id) {
        log.debug("Request to get ProfilActeur : {}", id);
        ProfilActeur profilActeur = profilActeurRepository.findOneWithEagerRelationships(id);
        return profilActeur;
    }


    @Transactional(readOnly = true)
    public List<ProfilActeur> findAllByGroupe(Long idGroupe) {
        return profilActeurRepository.findAllByGroupeWithEagerRelationshipsNew(idGroupe);
    }
    /**
     *  Delete the  profilActeur by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete ProfilActeur : {}", id);
        profilActeurRepository.deleteById(id);
    }

    public List<ProfilActeur> findAllByType(TypeProfilActeur acteur) {
        return profilActeurRepository.findByTypeAndActiveAll(acteur);
           
    }
    public void verifProfilActeur(ProfilActeur profilActeur) {

        ProfilActeur profilActeurAvant = profilActeurRepository.findById(profilActeur.getId()).orElse(null) ;
        HistoriqueGroupeProfilActeur historiqueGroupeProfilActeur = new HistoriqueGroupeProfilActeur() ;

        if(!Objects.equals(profilActeurAvant.getTableauFrais(), profilActeur.getTableauFrais()))
        {
            List<DonneeDTO> listeDonnees = new ArrayList<>();
            LigneGroupeProfilTemplate jsonTableauFrais = new LigneGroupeProfilTemplate() ;
            for (TableauFrais tab:  profilActeur.getTableauFrais())
            {
                DonneeDTO d1 = new DonneeDTO(tab.getPoidsNum().longValue(),tab.getLibelle());
                listeDonnees.add(d1);
            }
            jsonTableauFrais.setTableau(listeDonnees);
            historiqueGroupeProfilActeur.setJsonTableauFrais(jsonTableauFrais);
        }
        if(!Objects.equals(profilActeurAvant.getTableauCumuls() , profilActeur.getTableauCumuls()))
        {
            List<DonneeDTO> listeDonnees = new ArrayList<>();
            LigneGroupeProfilTemplate jsonTableCucumul = new LigneGroupeProfilTemplate() ;
            for (TableauCumul tab:  profilActeur.getTableauCumuls())
            {
                DonneeDTO d1 = new DonneeDTO(tab.getId(),tab.getCodeRegle()) ;
                listeDonnees.add(d1);
            }
            jsonTableCucumul.setTableau(listeDonnees);
            historiqueGroupeProfilActeur.setJsonTableauCumul(jsonTableCucumul);
        }
        if(!Objects.equals(profilActeurAvant.getGroupRepartitions() , profilActeur.getGroupRepartitions()))
        {
            List<DonneeDTO> listeDonnees = new ArrayList<>();
            LigneGroupeProfilTemplate jsonRpartition = new LigneGroupeProfilTemplate() ;
            for (GroupRepartition group:  profilActeur.getGroupRepartitions())
            {
                DonneeDTO d1 = new DonneeDTO(group.getId(),group.getLibelleGroup()) ;
                listeDonnees.add(d1);
            }
            jsonRpartition.setTableau(listeDonnees);
            historiqueGroupeProfilActeur.setJsonGroupeRepartition(jsonRpartition);
        }
        if(!Objects.equals(profilActeurAvant.getTableauRepartitionCommissions() , profilActeur.getTableauRepartitionCommissions()))
        {
            List<DonneeDTO> listeDonnees = new ArrayList<>();
            LigneGroupeProfilTemplate jsonTabRepartion = new LigneGroupeProfilTemplate() ;
            for (TableauRepartitionCommission tab:  profilActeur.getTableauRepartitionCommissions())
            {
                DonneeDTO d1 = new DonneeDTO(tab.getNiveaoOperation().longValue(),tab.getTypeService().getLibelle()) ;
                listeDonnees.add(d1);
            }
            jsonTabRepartion.setTableau(listeDonnees);
            historiqueGroupeProfilActeur.setJsonGroupeRepartition(jsonTabRepartion);
        }
        if(!Objects.equals(profilActeurAvant.getHoraireAuthentifications() , profilActeur.getHoraireAuthentifications()))
        {
            List<DonneeDTO> listeDonnees = new ArrayList<>();
            LigneGroupeProfilTemplate jsonHorraire = new LigneGroupeProfilTemplate() ;
            for ( HoraireAuthentification tab:  profilActeur.getHoraireAuthentifications())
            {
                DonneeDTO d1 = new DonneeDTO(tab.getId(),tab.getJour().name()) ;
                listeDonnees.add(d1);
            }
            jsonHorraire.setTableau(listeDonnees);
            historiqueGroupeProfilActeur.setJsonHoraire(jsonHorraire);
        }
        if(historiqueGroupeProfilActeur.getJsonGroupeRepartition()!=null || historiqueGroupeProfilActeur.getJsonRepartitionCom() !=null || historiqueGroupeProfilActeur.getJsonTableauCumul()!=null || historiqueGroupeProfilActeur.getJsonTableauFrais()!=null)
        {
            historiqueGroupeProfilActeur.setProfilActeur(profilActeur);
            historiqueGroupeProfilActeur.setType(profilActeur.getType());
            historiqueGroupeProfilActeur.setCreatedBy(SecurityUtils.getCurrentUserLogin());
            this.historiqueGroupeProfilActeurRepository.save(historiqueGroupeProfilActeur) ;
        }
    }
}
