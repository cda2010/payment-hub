package com.m2i.pti.service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.m2i.pti.domain.Facturier;
import com.m2i.pti.domain.FacturierProfils;
import com.m2i.pti.domain.GroupeProfilActeur;
import com.m2i.pti.domain.enumeration.TypeActeurFacturier;
import com.m2i.pti.repository.FacturierProfilRepository;
import com.m2i.pti.service.dto.FacturierDTO;
import com.m2i.pti.service.dto.FacturierProfilDTO;


@Service
@Transactional
public class FacturierProfilService  {
private final Logger log = LoggerFactory.getLogger(FacturierProfilService.class);
    private final FacturierProfilRepository facturierProfilRepository;
    private String pathname;
    private final ResourceLoader resourceLoader;
    public FacturierProfilService(FacturierProfilRepository facturierProfilRepository, ResourceLoader resourceLoader) {
        this.facturierProfilRepository = facturierProfilRepository;
        this.resourceLoader = resourceLoader;
    }

      /**
   * Save a facturierProfil.
   *
   * @param typeServiceDTO the entity to save
   * @return the persisted entity
   */
  public FacturierProfilDTO save(FacturierProfilDTO facturierProfilDTO) {
    log.debug("Request to save facturierProfil : {}", facturierProfilDTO);
    Facturier facturier = new Facturier(facturierProfilDTO.getFacturier().getId(),facturierProfilDTO.getFacturier().getNomFacturier());
    FacturierProfils facturierProfils = new FacturierProfils();
    for (GroupeProfilActeur groupeProfilActeur : facturierProfilDTO.getGroupeProfilActeur()) {
      FacturierProfils facturierProfil = new FacturierProfils();
      facturierProfil.setTypeActeur(facturierProfilDTO.getTypeActeur()); 
      facturierProfil.setFacturier(facturier);
      facturierProfil.setGroupeProfilActeur(groupeProfilActeur);
      facturierProfils = facturierProfilRepository.saveAndFlush(facturierProfil); 
    } 
     
    FacturierProfilDTO facturierProfilDTO2 = new FacturierProfilDTO();
    facturierProfilDTO2.setId(facturierProfils.getId());
    facturierProfilDTO2.setFacturier(facturierProfilDTO.getFacturier());
    facturierProfilDTO2.setGroupeProfilActeur(facturierProfilDTO.getGroupeProfilActeur());
    facturierProfilDTO2.setTypeActeur(facturierProfilDTO.getTypeActeur());
    return facturierProfilDTO2;
}

    @Transactional(readOnly = true)
    public Page<FacturierProfilDTO> findByFacturierProfilsAll(Pageable pageable) {
        List<FacturierProfilDTO> result;
        List<Object[]> intermediareResults;
        intermediareResults= facturierProfilRepository.AllFacturierProfils();

        Map<Long, FacturierDTO> facturierMap = new HashMap<>();
        List<FacturierProfilDTO> facturierProfilDTOs = new ArrayList<>();
        for (Object[] resultinter : intermediareResults) {
          Long facturierId = ((BigInteger) resultinter[0]).longValue();
          String nomFacturier = (String) resultinter[1];
          Long groupeProfilActeurId = ((BigInteger) resultinter[2]).longValue();
          String nomGroupeProfilActeur = (String) resultinter[3];
          String typeActeur = (String) resultinter[4];

          GroupeProfilActeur groupeProfilActeur = new GroupeProfilActeur();
          groupeProfilActeur.setId(groupeProfilActeurId);
          groupeProfilActeur.setNom(nomGroupeProfilActeur);
          // Vérifiez si le type de service existe déjà dans la map, sinon créez-le
          if (!facturierMap.containsKey(facturierId)) {
              FacturierDTO facturier = new FacturierDTO();
              facturier.setId(facturierId);
              facturier.setNomFacturier(nomFacturier);
              facturier.setTypeActeur(typeActeur);
              facturier.setGroupeProfilActeurDTOs(new ArrayList<>());
              facturierMap.put(facturierId, facturier);
        }
        // Ajouter l'action à la liste des actions du service
        facturierMap.get(facturierId).getGroupeProfilActeurDTOs().add(groupeProfilActeur);
    }
    // Convertir la map en une liste de TypeServiceDTO
    List<FacturierDTO> uniqueFacturier = new ArrayList<>(facturierMap.values());
    
    // Créer les facturierProfilDTO avec les TypeServiceDTO correspondants
    for (FacturierDTO facturierDTO : uniqueFacturier) {
        FacturierProfilDTO facturierProfilDTO = new FacturierProfilDTO();
        facturierProfilDTO.setFacturier(facturierDTO);
        facturierProfilDTOs.add(facturierProfilDTO);
    }
    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), facturierProfilDTOs.size());
    return new PageImpl<>(facturierProfilDTOs.subList(start, end), pageable, facturierProfilDTOs.size());
  }
}
