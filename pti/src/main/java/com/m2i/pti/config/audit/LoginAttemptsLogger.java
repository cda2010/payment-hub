/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.config.audit;

import com.m2i.pti.domain.EntityAuditEvent;
import com.m2i.pti.repository.EntityAuditEventRepository;
import java.time.Instant;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

/**
 *
 * @author cda
 */
@Component
public class LoginAttemptsLogger {
    private final Logger log = LoggerFactory.getLogger(AsyncEntityAuditEventWriter.class);
    @Autowired
  EntityAuditEventRepository auditingEntityRepository;
     @EventListener
    public void auditEventHappened(
      AuditApplicationEvent auditApplicationEvent) {
        
        AuditEvent auditEvent = auditApplicationEvent.getAuditEvent();
      
         log.debug("Principal " + auditEvent.getPrincipal() + " - " + auditEvent.getType());
        EntityAuditEvent auditedEntity = new EntityAuditEvent();
        auditedEntity.setAction(auditEvent.getType());
        auditedEntity.setEntityId(1L);
        auditedEntity.setEntityType("CONNEXION");
        auditedEntity.setModifiedBy(auditEvent.getPrincipal());
        auditedEntity.setCommitVersion(1);
        auditedEntity.setModifiedDate(Instant.now());
        

 
 
 Object details = (Object) auditEvent.getData().get("details");
        if (details instanceof WebAuthenticationDetails) {
            WebAuthenticationDetails detailObj =  (WebAuthenticationDetails) details;
            
              log.debug("Remote IP address: " 
          + detailObj.getRemoteAddress());
          auditedEntity.setAdresse(detailObj.getRemoteAddress());
        }
         if (auditEvent.getType().equals("AUTHORIZATION_FAILURE"))
                auditingEntityRepository.save(auditedEntity);
    }
}
