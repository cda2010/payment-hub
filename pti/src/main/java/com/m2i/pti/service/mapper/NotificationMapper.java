package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.NotificationDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Notification and its DTO NotificationDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface NotificationMapper extends EntityMapper<NotificationDTO, Notification> {

    @Mapping(source = "client.id", target = "clientId")
    @Mapping(source = "uniteOrganisation.id", target = "uniteOrganisationId")
    NotificationDTO toDto(Notification notification);

   // @Mapping(source = "clientId", target = "client.id")
    //@Mapping(source = "uniteOrganisationId", target = "uniteOrganisation.id")
    Notification toEntity(NotificationDTO notificationDTO);

    default Notification fromId(Long id) {
        if (id == null) {
            return null;
        }
        Notification notification = new Notification();
        notification.setId(id);
        return notification;
    }
}
