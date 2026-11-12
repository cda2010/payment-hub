package com.m2i.pti.repository;

import com.m2i.pti.domain.EntityAuditEvent;
import com.m2i.pti.service.dto.EntityAuditEventDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the EntityAuditEvent entity.
 */
public interface EntityAuditEventRepository extends JpaRepository<EntityAuditEvent, Long> {

    List<EntityAuditEvent> findAllByEntityTypeAndEntityId(String entityType, Long entityId);

    @Query("SELECT max(a.commitVersion) FROM EntityAuditEvent a where a.entityType = :type and a.entityId = :entityId")
    Integer findMaxCommitVersion(@Param("type") String type, @Param("entityId") Long entityId);

    @Query("SELECT  e FROM EntityAuditEvent e where e.entityType = :type and e.entityId = :entityId and  "
            + " e.commitVersion= :version")
    EntityAuditEvent findTopByEntityTypeAndEntityIdOrderByIdDesc(@Param("type") String type, @Param("entityId") Long entityId,  @Param("version") Integer version);
    

    @Query("SELECT DISTINCT (a.entityType) from EntityAuditEvent a")
    List<String> findAllEntityTypes();

    Page<EntityAuditEvent> findAllByEntityType(String entityType, Pageable pageRequest);
    List<EntityAuditEvent> findAllByEntityType(String entityType);

    @Query("SELECT ae FROM EntityAuditEvent ae where ae.entityType = :type and ae.entityId = :entityId and " +
        "ae.commitVersion =(SELECT max(a.commitVersion) FROM EntityAuditEvent a where a.entityType = :type and " +
        "a.entityId = :entityId and a.commitVersion < :commitVersion)")
    EntityAuditEvent findOneByEntityTypeAndEntityIdAndCommitVersion(@Param("type") String type, @Param("entityId")
        Long entityId, @Param("commitVersion") Integer commitVersion);

    @Query("SELECT a FROM EntityAuditEvent a where (" +
            "(:action is null or :action='' or UPPER(a.action) " +
            "like UPPER('%'||:action||'%')) and (:adresse is null or :adresse='' or UPPER(a.adresse) " +
            "like UPPER('%'||:adresse||'%')) and (:modifiedBy is null or :modifiedBy='' or UPPER(a.modifiedBy) " +
            "like UPPER('%'||:modifiedBy||'%'))) order by a.id desc")
    List<EntityAuditEvent> findAuditByCredential(@Param("action") String action,@Param("adresse") String adresse,@Param("modifiedBy") String modifiedBy);

    List<EntityAuditEvent> findAllByAction(String action);
    
  @Query("SELECT a from EntityAuditEvent a where a.entityId=?1 and UPPER(a.entityType)  like concat('%',?2)")
    public   List<EntityAuditEvent> findEntityValuejson(Long id, String entite);

    @Query("SELECT a from EntityAuditEvent a where a.entityId=?1 and UPPER(a.entityType)  like concat('%',?2)")
    public   Page<EntityAuditEvent> findEntityValuejsonPage(Long id, String entite,Pageable pageable);

    @Query(name = "EntityAuditEvent.findAllAuditEvent", nativeQuery = true)
    List<EntityAuditEventDTO> findAuditByCredentialOptimiser(@Param("action") String action, @Param("adresse") String adresse, @Param("modifiedBy") String modifiedBy);

}
