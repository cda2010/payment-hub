package com.m2i.pti.repository;

import com.m2i.pti.config.Constants;
import com.m2i.pti.config.audit.AuditEventConverter;
import com.m2i.pti.domain.PersistentAuditEvent;

import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * An implementation of Spring Boot's AuditEventRepository.
 */
@Repository
public class CustomAuditEventRepository implements AuditEventRepository {

    private static final String AUTHORIZATION_FAILURE = "AUTHORIZATION_FAILURE";

    private final PersistenceAuditEventRepository persistenceAuditEventRepository;

    private final AuditEventConverter auditEventConverter;

    public CustomAuditEventRepository(PersistenceAuditEventRepository persistenceAuditEventRepository,
            AuditEventConverter auditEventConverter) {
        this.persistenceAuditEventRepository = persistenceAuditEventRepository;
        this.auditEventConverter = auditEventConverter;
    }



    @Override
    public List<AuditEvent> find(String principal, Instant after, String type) {
        Iterable<PersistentAuditEvent> persistentAuditEvents =
                persistenceAuditEventRepository.findByPrincipalAndAuditEventDateAfterAndAuditEventType(principal, after, type);
        return auditEventConverter.convertToAuditEvent(persistentAuditEvents);
    }

    public List<AuditEvent> find(Date after) {
        Iterable<PersistentAuditEvent> persistentAuditEvents =
            persistenceAuditEventRepository.findByAuditEventDateAfter(after.toInstant());
        return auditEventConverter.convertToAuditEvent(persistentAuditEvents);
    }

    public List<AuditEvent> find(String principal, Date after) {
        Iterable<PersistentAuditEvent> persistentAuditEvents;
        if (principal == null && after == null) {
            persistentAuditEvents = persistenceAuditEventRepository.findAll();
        } else if (after == null) {
            persistentAuditEvents = persistenceAuditEventRepository.findByPrincipal(principal);
        } else {
            persistentAuditEvents =
                persistenceAuditEventRepository.findByPrincipalAndAuditEventDateAfter(principal, after.toInstant());
        }
        return auditEventConverter.convertToAuditEvent(persistentAuditEvents);
    }

    public List<AuditEvent> find(String principal, Date after, String type) {
        Iterable<PersistentAuditEvent> persistentAuditEvents =
            persistenceAuditEventRepository.findByPrincipalAndAuditEventDateAfterAndAuditEventType(principal, after.toInstant(), type);
        return auditEventConverter.convertToAuditEvent(persistentAuditEvents);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void add(AuditEvent event) {
        if (!AUTHORIZATION_FAILURE.equals(event.getType()) &&
            !Constants.ANONYMOUS_USER.equals(event.getPrincipal())) {
            System.out.println("Enregistrement des informations de logs de connexion ||....");
            PersistentAuditEvent persistentAuditEvent = new PersistentAuditEvent();
            persistentAuditEvent.setPrincipal(event.getPrincipal());
            persistentAuditEvent.setAuditEventType(event.getType());
            persistentAuditEvent.setAuditEventDate(event.getTimestamp());
            persistentAuditEvent.setData(auditEventConverter.convertDataToStrings(event.getData()));
            //persistenceAuditEventRepository.save(persistentAuditEvent);
        }
    }
}
