package com.m2i.pti.service;

import com.m2i.pti.domain.LigneRepartitionCommission;
import com.m2i.pti.domain.TypeUniteOrganisation;
import com.m2i.pti.domain.enumeration.TypeTarif;
import com.m2i.pti.repository.LigneRepartitionCommissionRepository;
import com.m2i.pti.repository.TypeUniteOrganisationRepository;
import com.m2i.pti.service.dto.LigneRepartitionCommissionDTO;
import com.m2i.pti.service.dto.TypeUniteOrganisationDTO;
import com.m2i.pti.service.mapper.LigneRepartitionCommissionMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * Service Implementation for managing LigneRepartitionCommission.
 */
@Service
@Transactional
public class LigneRepartitionCommissionService {

    private final Logger log = LoggerFactory.getLogger(LigneRepartitionCommissionService.class);

    private final LigneRepartitionCommissionRepository ligneRepartitionCommissionRepository;

    private final TypeUniteOrganisationRepository typeUniteOrganisationRepository;

    private final LigneRepartitionCommissionMapper ligneRepartitionCommissionMapper;

    public LigneRepartitionCommissionService(LigneRepartitionCommissionRepository ligneRepartitionCommissionRepository, TypeUniteOrganisationRepository typeUniteOrganisationRepository, LigneRepartitionCommissionMapper ligneRepartitionCommissionMapper) {
        this.ligneRepartitionCommissionRepository = ligneRepartitionCommissionRepository;
        this.typeUniteOrganisationRepository = typeUniteOrganisationRepository;
        this.ligneRepartitionCommissionMapper = ligneRepartitionCommissionMapper;
    }

    /**
     * Enregistrer une ligne de repartition
     *
     * @param ligneRepartitionCommissionDTO the entity to save
     * @return the persisted entity
     */
    public LigneRepartitionCommissionDTO save(LigneRepartitionCommissionDTO ligneRepartitionCommissionDTO) {
        verificationIntervalles(ligneRepartitionCommissionDTO);
        return verifLigneRepartitionCommissionDTO(ligneRepartitionCommissionDTO);
    }
    private void verificationIntervalles(LigneRepartitionCommissionDTO ligneRepartitionCommissionDTO){
        BigDecimal min=ligneRepartitionCommissionDTO.getMin();
        BigDecimal max=ligneRepartitionCommissionDTO.getMax();
        if(min!=null&&max!=null&&max.doubleValue()<min.doubleValue()){
            throw new CustomParameterizedException("La valeur maximale doit être superieure ou égale à la valeur minimale !");
        }
        List<LigneRepartitionCommission> holdLines=ligneRepartitionCommissionRepository.findByTableauRepartitionCommissionIdAndNiveau(ligneRepartitionCommissionDTO.getTableauRepartitionCommissionId(),ligneRepartitionCommissionDTO.getNiveau());
        //On recupère les anciennes lignes et on vérifie qu'il n'ya pas de chevauchement entre intervalles
        if(holdLines!=null){
            holdLines.forEach(line -> {
                //Si l'un des extemités du nouveau intervale se trouve dans un autre et que ce dernier n'est pas l'intervalle de la nouvelle ligne
                if((inIntervalle(line.getMin(),line.getMax(),min)||inIntervalle(line.getMin(),line.getMax(),max))&&!(line.getId().equals(ligneRepartitionCommissionDTO.getId())&&line.getMin().doubleValue()==min.doubleValue()&&line.getMax().doubleValue()==max.doubleValue())){
                    throw new  CustomParameterizedException("L'intervalle que vous avez rensigné chevauche l'intervalle: ["+line.getMin()+" - "+line.getMax()+"] ");
                }
            });
        }
    }

    private boolean inIntervalle(BigDecimal min,BigDecimal max,BigDecimal value){
        if(min==null||max==null||value==null){
            throw new CustomParameterizedException("Vous avez des données qui ne respectent pas les contraintes d'intégrité.\n" +
                    "Veuillez les effacer et reprendre le parametrage");
        }
        if(min.doubleValue()!=value.doubleValue()&&max.doubleValue()!=value.doubleValue()){
            if(!(min.doubleValue()<value.doubleValue()&&max.doubleValue()>value.doubleValue())){
                return false;
            }
        }
        return true;
    }

    private LigneRepartitionCommissionDTO verifLigneRepartitionCommissionDTO(LigneRepartitionCommissionDTO ligneRepartitionCommissionDTO) {
        if(getTotalPourcentage(ligneRepartitionCommissionDTO)>100){
            throw new CustomParameterizedException("Le pourcentage groupé ne doit pas être superieur à 100 .");
        }
        LigneRepartitionCommission ligneRepartitionCommission = ligneRepartitionCommissionMapper.toEntity(ligneRepartitionCommissionDTO);
        ligneRepartitionCommission = ligneRepartitionCommissionRepository.save(ligneRepartitionCommission);
        return ligneRepartitionCommissionMapper.toDto(ligneRepartitionCommission);
    }

    private Double getTotalPourcentage(LigneRepartitionCommissionDTO ligneRepartitionCommissionDTO) {
        List<LigneRepartitionCommission> lignes = ligneRepartitionCommissionRepository.findByTableauRepartitionCommissionIdAndMinAndMax(ligneRepartitionCommissionDTO.getTableauRepartitionCommissionId(),ligneRepartitionCommissionDTO.getMin(),ligneRepartitionCommissionDTO.getMax());
        Double total = Double.valueOf(0);
        if(ligneRepartitionCommissionDTO.getTypeTarif().equals(TypeTarif.POURCENTAGE) && ligneRepartitionCommissionDTO.getValeur()!=null&&ligneRepartitionCommissionDTO.getId()==null){
            total+=ligneRepartitionCommissionDTO.getValeur();
        }
        if (!lignes.isEmpty()) {
            for (LigneRepartitionCommission ligneRepartitionCommission : lignes) {
                if(ligneRepartitionCommission.getTypeTarif().equals(TypeTarif.POURCENTAGE)){
                    total += ligneRepartitionCommission.getValeur();
                }

            }
        }
        return total;
    }
    private BigDecimal getTotalSomme(LigneRepartitionCommissionDTO ligneRepartitionCommissionDTO) {
        List<LigneRepartitionCommission> lignes = ligneRepartitionCommissionRepository.findByTableauRepartitionCommissionIdAndMinAndMax(ligneRepartitionCommissionDTO.getTableauRepartitionCommissionId(),ligneRepartitionCommissionDTO.getMin(),ligneRepartitionCommissionDTO.getMax());
        BigDecimal total = BigDecimal.valueOf(0);
        if(ligneRepartitionCommissionDTO.getTypeTarif().equals(TypeTarif.FIXE)&&ligneRepartitionCommissionDTO.getSomme()!=null&&ligneRepartitionCommissionDTO.getId()==null){
            total.add(ligneRepartitionCommissionDTO.getSomme());
        }
        if (!lignes.isEmpty()) {
            for (LigneRepartitionCommission ligneRepartitionCommission : lignes) {
                if(ligneRepartitionCommission.getTypeTarif().equals(TypeTarif.FIXE)){
                   total= total.add(ligneRepartitionCommission.getSomme());
                }

            }
        }
        return total;
    }

    /**
     * Get all the ligneRepartitionCommissions.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<LigneRepartitionCommissionDTO> findAll(Pageable pageable) {
        log.info("Request to get all LigneRepartitionCommissions");
        return ligneRepartitionCommissionRepository.findAll(pageable)
                .map(ligneRepartitionCommissionMapper::toDto);
    }


    @Transactional(readOnly = true)
    public Page<LigneRepartitionCommissionDTO> findAllByTableauRepartition(Long tableauRepartitionId, Pageable pageable) {
        log.info("Request to get all LigneRepartitionCommissions");
        return ligneRepartitionCommissionRepository.findByTableauRepartitionCommissionId(tableauRepartitionId, pageable)
                .map(ligneRepartitionCommissionMapper::toDto);
    }
    @Transactional(readOnly = true)
    public List<LigneRepartitionCommissionDTO> findAllByTableauRepartitionList(Long tableauRepartitionId) {
        log.info("Request to get all LigneRepartitionCommissions");
        return ligneRepartitionCommissionMapper.toDto(ligneRepartitionCommissionRepository.findByTableauRepartitionCommissionListId(tableauRepartitionId));
    }

    @Transactional(readOnly = true)
    public Page<LigneRepartitionCommissionDTO> findAllByTableauRepartitionNew(Long tableauRepartitionId, Pageable pageable) {
        log.info("Request to get all LigneRepartitionCommissions New");
        List<LigneRepartitionCommissionDTO> liste=new ArrayList<>();
        Page<LigneRepartitionCommissionDTO> ligneRepartitionCommissionDTO;
        ligneRepartitionCommissionDTO=ligneRepartitionCommissionRepository.findByTableauRepartitionCommissionId(tableauRepartitionId, pageable).map(ligneRepartitionCommissionMapper::toDto);
        for(LigneRepartitionCommissionDTO result:ligneRepartitionCommissionDTO){
            List<TypeUniteOrganisation> typeUniteOrganisation=typeUniteOrganisationRepository.findListByNiveau(result.getNiveau());
            result.setNiveauLibelle(typeUniteOrganisation.get(0).getLibelleTypeUO());
            liste.add(result);
        }
        return new PageImpl<>(liste, pageable, liste.size());
    }

    @Transactional(readOnly = true)
    public List<LigneRepartitionCommissionDTO> findAllByTableauRepartitionNewList(Long tableauRepartitionId, Pageable pageable) {
        log.info("Request to get all LigneRepartitionCommissions New--");
        List<LigneRepartitionCommissionDTO> liste=new ArrayList<>();
        List<LigneRepartitionCommissionDTO> ligneRepartitionCommissionDTO;
        ligneRepartitionCommissionDTO= ligneRepartitionCommissionMapper.toDto(ligneRepartitionCommissionRepository.findByTableauRepartitionCommissionListId(tableauRepartitionId));
        for(LigneRepartitionCommissionDTO result:ligneRepartitionCommissionDTO){
            List<TypeUniteOrganisation> typeUniteOrganisation=typeUniteOrganisationRepository.findListByNiveau(result.getNiveau());
            result.setNiveauLibelle(typeUniteOrganisation.get(0).getLibelleTypeUO());
            liste.add(result);
        }
        return liste;
    }

    /**
     * Get one ligneRepartitionCommission by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public LigneRepartitionCommissionDTO findOne(Long id) {
        log.info("Request to get LigneRepartitionCommission : {}", id);
        LigneRepartitionCommission ligneRepartitionCommission = ligneRepartitionCommissionRepository.findById(id).orElse(null);
        return ligneRepartitionCommissionMapper.toDto(ligneRepartitionCommission);
    }

    /**
     * Delete the ligneRepartitionCommission by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.info("Request to delete LigneRepartitionCommission : {}", id);
        ligneRepartitionCommissionRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<LigneRepartitionCommissionDTO> findAllByTableauRepartitionNewListOptimise(Long tableauRepartitionId, Pageable pageable) {
        log.info("Request to get all LigneRepartitionCommissions New--");
        List<LigneRepartitionCommissionDTO> liste=new ArrayList<>();
        List<LigneRepartitionCommissionDTO> ligneRepartitionCommissionDTO;
        ligneRepartitionCommissionDTO= ligneRepartitionCommissionRepository.findByTableauRepartitionCommissionOptimise(tableauRepartitionId);
        for(LigneRepartitionCommissionDTO result:ligneRepartitionCommissionDTO){
            List<TypeUniteOrganisationDTO> typeUniteOrganisation=typeUniteOrganisationRepository.findListByNiveauOptimise(result.getNiveau());
            result.setNiveauLibelle(typeUniteOrganisation.get(0).getLibelleTypeUO());
            liste.add(result);
        }
        return liste;
    }

}
