package com.m2i.omnicanal.presentation.repository;

import com.m2i.omnicanal.presentation.config.Constants;
import com.m2i.omnicanal.presentation.config.audit.AuditEventConverter;
import com.m2i.omnicanal.presentation.domain.PersistentAuditEvent;

import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
        LocalDateTime afterDate = after == null ? null : LocalDateTime.ofInstant(after, ZoneId.systemDefault());
        Iterable<PersistentAuditEvent> persistentAuditEvents;
        if (principal != null && afterDate != null && type != null) {
            persistentAuditEvents = persistenceAuditEventRepository.findByPrincipalAndAuditEventDateAfterAndAuditEventType(principal, afterDate, type);
        } else if (principal != null && afterDate != null) {
            persistentAuditEvents = persistenceAuditEventRepository.findByPrincipalAndAuditEventDateAfter(principal, afterDate);
        } else if (afterDate != null) {
            persistentAuditEvents = persistenceAuditEventRepository.findByAuditEventDateAfter(afterDate);
        } else if (principal != null) {
            persistentAuditEvents = persistenceAuditEventRepository.findByPrincipal(principal);
        } else {
            persistentAuditEvents = persistenceAuditEventRepository.findAll();
        }
        return auditEventConverter.convertToAuditEvent(persistentAuditEvents);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void add(AuditEvent event) {
        if (!AUTHORIZATION_FAILURE.equals(event.getType()) &&
            !Constants.ANONYMOUS_USER.equals(event.getPrincipal())) {

            PersistentAuditEvent persistentAuditEvent = new PersistentAuditEvent();
            persistentAuditEvent.setPrincipal(event.getPrincipal());
            persistentAuditEvent.setAuditEventType(event.getType());
            persistentAuditEvent.setAuditEventDate(LocalDateTime.ofInstant(event.getTimestamp(), ZoneId.systemDefault()));
            persistentAuditEvent.setData(auditEventConverter.convertDataToStrings(event.getData()));
            persistenceAuditEventRepository.save(persistentAuditEvent);
        }
    }
}
