package com.m2i.pti.service;

import com.m2i.pti.domain.TemplateMessage;
import com.m2i.pti.domain.enumeration.Destination;
import com.m2i.pti.domain.enumeration.TypeServiceLibelle;
import com.m2i.pti.repository.TemplateMessageRepository;
import com.m2i.pti.service.dto.CanalDTO;
import com.m2i.pti.service.dto.TemplateMessageDTO;
import com.m2i.pti.service.dto.TypeServiceDTO;
import com.m2i.pti.service.mapper.TemplateMessageMapper;
import java.util.List;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing TemplateMessage.
 */
@Service
@Transactional
public class TemplateMessageService {

    private final Logger log = LoggerFactory.getLogger(TemplateMessageService.class);

    private final TemplateMessageRepository templateMessageRepository;

    private final TemplateMessageMapper templateMessageMapper;

    private final TypeServiceService typeServiceService;
    PassiveExpiringMap<String, List<TemplateMessage>> listTemplateMapDTODto = new PassiveExpiringMap<>(1800000);
    PassiveExpiringMap<String, TemplateMessage> templateMapDTODto = new PassiveExpiringMap<>(1800000);

    public TemplateMessageService(TemplateMessageRepository templateMessageRepository, TemplateMessageMapper templateMessageMapper, TypeServiceService typeServiceService) {
        this.templateMessageRepository = templateMessageRepository;
        this.templateMessageMapper = templateMessageMapper;
        this.typeServiceService = typeServiceService;
    }

    /**
     * Save a templateMessage.
     *
     * @param templateMessageDTO the entity to save
     * @return the persisted entity
     */
    public TemplateMessageDTO save(TemplateMessageDTO templateMessageDTO) {
        log.debug("Request to save TemplateMessage : {}", templateMessageDTO);
        TemplateMessage templateMessage = templateMessageMapper.toEntity(templateMessageDTO);
        templateMessage = templateMessageRepository.save(templateMessage);
        return templateMessageMapper.toDto(templateMessage);
    }

    /**
     * Get all the templateMessages.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<TemplateMessageDTO> findAll(Pageable pageable, Long typeServiceId) {
        log.debug("Request to get all TemplateMessages");
        TypeServiceDTO typeServiceDTO = new TypeServiceDTO();
        TypeServiceLibelle typeServiceLibelle = null;
        if (typeServiceId != null) {
            typeServiceDTO = typeServiceService.findOne(typeServiceId);
            typeServiceLibelle = TypeServiceLibelle.valueOf(typeServiceDTO.getLibelle());
        }

        System.out.println("----------");
        System.out.println("----------");
        return templateMessageRepository.findAllByTypeServiceLibelle(pageable, typeServiceLibelle)
                .map(templateMessageMapper::toDto);
    }

    /**
     * Get one templateMessage by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public TemplateMessageDTO findOne(Long id) {
        log.debug("Request to get TemplateMessage : {}", id);
        TemplateMessage templateMessage = templateMessageRepository.findById(id).orElse(null);
        return templateMessageMapper.toDto(templateMessage);
    }

    /**
     * Delete the templateMessage by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete TemplateMessage : {}", id);
        templateMessageRepository.deleteById(id);
    }

    TemplateMessage findByTypeService(TypeServiceLibelle typeservice, Destination dest) {
        TemplateMessage templateMessage;
        if (templateMapDTODto.containsKey(typeservice.name() + dest.name()) && templateMapDTODto.get(typeservice.name() + dest.name())!=null) {
            log.info("LE TEMPLATE EXIST DANS LE MAP");
            templateMessage = templateMapDTODto.get(typeservice.name() + dest.name());
        } else {
            templateMessage = templateMessageRepository.findByTypeService(typeservice, dest);
            if (templateMessage!=null){
                  templateMapDTODto.put(typeservice.name() + dest.name(), templateMessage);
            }
          
        }
        return templateMessage;
    }

    List<TemplateMessage> findByTypeServiceList(TypeServiceLibelle typeservice, Destination dest) {
        List<TemplateMessage> templateMessage;
        if (listTemplateMapDTODto.containsKey(typeservice.name() + dest.name()) && listTemplateMapDTODto.get(typeservice.name() + dest.name())!=null) {
            log.info("LE TEMPLATE EXIST DANS LE MAP");
            templateMessage = listTemplateMapDTODto.get(typeservice.name() + dest.name());
        } else {
            templateMessage = templateMessageRepository.findByTypeServiceList(typeservice, dest);
            if (templateMessage!=null){
                listTemplateMapDTODto.put(typeservice.name() + dest.name(), templateMessage);
            }
            
        }

        return templateMessage;

    }

    TemplateMessage findByEtiquette(String etiquette, Destination dest) {
        
         TemplateMessage templateMessage;
        if (templateMapDTODto.containsKey(etiquette + dest.name()) && templateMapDTODto.get(etiquette + dest.name())!=null) {
            log.info("LE TEMPLATE EXIST DANS LE MAP");
            templateMessage = templateMapDTODto.get(etiquette + dest.name());
        } else {
            templateMessage = templateMessageRepository.findByEtiquette(etiquette, dest);
            if (templateMessage!=null){
                 templateMapDTODto.put(etiquette + dest.name(), templateMessage);
            }
           
        }
       
        return templateMessage;
    }
}
