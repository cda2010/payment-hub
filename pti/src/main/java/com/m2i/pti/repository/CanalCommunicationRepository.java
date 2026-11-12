package com.m2i.pti.repository;

import com.m2i.pti.domain.CanalCommunication;
import com.m2i.pti.domain.Facturier;
import com.m2i.pti.service.dto.CanalCommunicationDTO;
import com.m2i.pti.service.dto.ClientDTO;
import java.util.List;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;


/**
 * Spring Data JPA repository for the CanalCommunication entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CanalCommunicationRepository extends JpaRepository<CanalCommunication, Long> {
    @Query(value = "SELECT * FROM canal_communication WHERE facturier_id=?1", nativeQuery = true)
    public CanalCommunication getCCByIdFacturier(Long idFacturier);

    @Query(value = "SELECT recepteur_email_id FROM canal_communication WHERE facturier_id=?1", nativeQuery = true)
    public Long getIdRecepteurEmailByIdFacturier(Long idFacturier);

    @Query(value = "SELECT ftp_id FROM canal_communication WHERE facturier_id=?1", nativeQuery = true)
    public Long getIdFtpByIdFacturier(Long idFacturier);
    
     
     @Query("select c from CanalCommunication  c where  c.facturier.activated=true")
    public List<CanalCommunication>  findListeOfAllFacturierWithSendMAil();
    
    @Query(name = "CanalCommunication.findCanalCommunication", nativeQuery = true)
     List<CanalCommunicationDTO> findListCommunication();
}
