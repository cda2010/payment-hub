package com.m2i.pti.service;

import com.m2i.pti.domain.Notification;
import com.m2i.pti.domain.enumeration.TypeNotification;
import com.m2i.pti.repository.ClientRepository;
import com.m2i.pti.repository.NotificationRepository;
import com.m2i.pti.repository.UniteOrganisationRepository;
import com.m2i.pti.service.dto.NotificationDTO;
import com.m2i.pti.service.mapper.NotificationMapper;
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


/**
 * Service Implementation for managing Notification.
 */
@Service
@Transactional
public class NotificationService {

    private final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    private final NotificationMapper notificationMapper;
    private  final ClientRepository clientRepository;
    private final UniteOrganisationRepository uniteOrganisationRepository;

    public NotificationService(NotificationRepository notificationRepository, NotificationMapper notificationMapper, ClientRepository clientRepository, UniteOrganisationRepository uniteOrganisationRepository) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
        this.clientRepository = clientRepository;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
    }

    /**
     * Save a notification.
     *
     * @param notificationDTO the entity to save
     * @return the persisted entity
     */
    public NotificationDTO save(NotificationDTO notificationDTO) {
        log.debug("Request to save Notification : {}", notificationDTO);
        Notification notification = notificationMapper.toEntity(notificationDTO);
        if(notificationDTO.getClientId()!=null){
            notification.setClient(clientRepository.findById(notificationDTO.getClientId()).orElse(null));
        }
        if(notificationDTO.getUniteOrganisationId()!=null){
            notification.setUniteOrganisation(uniteOrganisationRepository.findById(notificationDTO.getUniteOrganisationId()).orElse(null));
        }
        notification = notificationRepository.save(notification);
        return notificationMapper.toDto(notification);
    }

    /**
     *  Get all the notifications.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<NotificationDTO> findByCriteria(TypeNotification typeNotification, ZonedDateTime dateEnvoi,String destination, Pageable pageable) {
        log.debug("Request to get all Notifications");

        List<Notification> list= notificationRepository.findByCriteria(typeNotification,destination);
        List<NotificationDTO> result=new ArrayList<NotificationDTO>();
        list.forEach(item->{
            if((dateEnvoi==null || (item.getDateHeureEnvoie().getYear()==dateEnvoi.getYear()
                    &&item.getDateHeureEnvoie().getMonthValue()==dateEnvoi.getMonthValue()
                    &&item.getDateHeureEnvoie().getDayOfYear()==dateEnvoi.getDayOfYear()))){
                result.add(notificationMapper.toDto(item));
            }
        });
        int start= (int) pageable.getOffset();
        int end=(start+pageable.getPageSize())>result.size()?result.size():(start+pageable.getPageSize());
        Page<NotificationDTO> retour=new PageImpl<NotificationDTO>(result.subList(start,end),pageable,result.size());
        return retour;
    }

    /**
     *  Get one notification by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public NotificationDTO findOne(Long id) {
        log.debug("Request to get Notification : {}", id);
        Notification notification = notificationRepository.findById(id).orElse(null);
        return notificationMapper.toDto(notification);
    }

    /**
     *  Delete the  notification by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Notification : {}", id);
        notificationRepository.deleteById(id);
    }
}
