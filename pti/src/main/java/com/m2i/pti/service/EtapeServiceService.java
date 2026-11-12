package com.m2i.pti.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.m2i.pti.domain.EtapeService;
import com.m2i.pti.domain.TypeService;
import com.m2i.pti.domain.enumeration.ActionService;
import com.m2i.pti.repository.EtapeServiceRepository;
import com.m2i.pti.service.dto.EtapeServiceDTO;
import com.m2i.pti.service.dto.TypeServiceDTO;
import com.m2i.pti.service.mapper.EtapeServiceMapper;

@Service
@Transactional
public class EtapeServiceService {
    private final Logger log = LoggerFactory.getLogger(EtapeServiceService.class);

    private final EtapeServiceRepository etapeServiceRepository;
  
    private final EtapeServiceMapper etapeServiceMapper;
  
    @Autowired
    public EtapeServiceService(
        EtapeServiceRepository etapeServiceRepository,
        EtapeServiceMapper etapeServiceMapper
    ) {
      this.etapeServiceRepository = etapeServiceRepository;
      this.etapeServiceMapper = etapeServiceMapper;
    }

     /**
   * Save a etapeService.
   *
   * @param typeServiceDTO the entity to save
   * @return the persisted entity
   */
  public EtapeServiceDTO save(EtapeServiceDTO etapeServiceDTO) {
    log.debug("Request to save EtapeService : {}", etapeServiceDTO);
    TypeService typeService = new TypeService(etapeServiceDTO.getTypeService().getId(), etapeServiceDTO.getTypeService().getCode());
    EtapeService etapeServices = new EtapeService();
    for (String action : etapeServiceDTO.getActionService()) {
      EtapeService etapeService = new EtapeService();
      etapeService.setTypeService(typeService);
      etapeService.setActionService(ActionService.valueOf(action));
      etapeServices = etapeServiceRepository.saveAndFlush(etapeService);
    }
    EtapeServiceDTO etapeServiceDTO2 = new EtapeServiceDTO();
    etapeServiceDTO2.setActionService(etapeServiceDTO.getActionService());
    etapeServiceDTO2.setId(etapeServices.getId());
    etapeServiceDTO2.setTypeService(etapeServiceDTO.getTypeService());
      return etapeServiceDTO2;
}

    @Transactional(readOnly = true)
    public Page<EtapeServiceDTO> findByCriteria(ActionService actionService, Pageable pageable) {
        log.debug("Request to get all Notifications"+actionService);
        List<EtapeServiceDTO> result;
        List<Object[]> intermediareResults;
       if(actionService != null){
        intermediareResults= etapeServiceRepository.findByCriteria(actionService.name());
       }else{
        intermediareResults= etapeServiceRepository.findByCriteria(null);
      }
      Map<Long, TypeServiceDTO> typeServiceMap = new HashMap<>();
      List<EtapeServiceDTO> etapeServiceDTOs = new ArrayList<>();
      for (Object[] resultinter : intermediareResults) {
        Long typeServiceId = ((Number) resultinter[2]).longValue();
        String action = (String) resultinter[1];
        String code = (String) resultinter[3];
    
        // Vérifiez si le type de service existe déjà dans la map, sinon créez-le
        if (!typeServiceMap.containsKey(typeServiceId)) {
            TypeServiceDTO typeServiceDTO = new TypeServiceDTO();
            typeServiceDTO.setId(typeServiceId);
            typeServiceDTO.setCode(code); 
            typeServiceDTO.setAction(new ArrayList<>());
            typeServiceMap.put(typeServiceId, typeServiceDTO);
        }
    
        // Ajouter l'action à la liste des actions du service
        typeServiceMap.get(typeServiceId).getAction().add(action);
    }
    // Convertir la map en une liste de TypeServiceDTO
    List<TypeServiceDTO> uniqueTypeServices = new ArrayList<>(typeServiceMap.values());
    
    // Créer les EtapeServiceDTO avec les TypeServiceDTO correspondants
    for (TypeServiceDTO typeServiceDTO : uniqueTypeServices) {
        EtapeServiceDTO etapeServiceDTO = new EtapeServiceDTO();
        etapeServiceDTO.setTypeService(typeServiceDTO);
        etapeServiceDTOs.add(etapeServiceDTO);
    }
    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), etapeServiceDTOs.size());
    return new PageImpl<>(etapeServiceDTOs.subList(start, end), pageable, etapeServiceDTOs.size());
  }
}
