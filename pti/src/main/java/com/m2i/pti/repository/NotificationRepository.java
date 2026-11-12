package com.m2i.pti.repository;

import com.m2i.pti.domain.Notification;
import com.m2i.pti.domain.enumeration.TypeNotification;
import com.m2i.pti.service.dto.NotificationDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Spring Data JPA repository for the Notification entity.
 */
@SuppressWarnings("unused")
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("select n from Notification n where ((:typeNotification is null or n.typeNotification=:typeNotification)" +
            "and (:destination is null or :destination='' or n.destinataire like UPPER ('%'||:destination||'%') ) ) order by n.id desc ")
    List<Notification> findByCriteria(@Param("typeNotification") TypeNotification typeNotification, @Param("destination") String destination);
    @Query("select n from Notification n where (n.destinataire=?1 or n.destinataire= concat('+',?1 ) )  order by n.dateHeureEnvoie desc    ")
    public List<Notification> findByDestinataireOrderByDateHeureEnvoieDesc(String numero);

   // @Query(name = "Client.findByCriteriaoptimiser",nativeQuery = true)
   // List<NotificationDTO> findByCriteriaoptimiser(@Param("typeNotification") TypeNotification typeNotification, @Param("destination") String destination);
}
