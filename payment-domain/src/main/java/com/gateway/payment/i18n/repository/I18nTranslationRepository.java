package com.gateway.payment.i18n.repository;

import com.gateway.payment.i18n.domain.I18nTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface I18nTranslationRepository extends JpaRepository<I18nTranslation, UUID> {

    List<I18nTranslation> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    List<I18nTranslation> findByEntityTypeAndEntityIdIn(String entityType, Collection<UUID> entityIds);

    List<I18nTranslation> findByEntityTypeAndEntityIdAndFieldPath(
            String entityType, UUID entityId, String fieldPath);

    void deleteByEntityTypeAndEntityId(String entityType, UUID entityId);
}
