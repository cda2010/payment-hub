package com.gateway.payment.content.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Fiche / article d’une rubrique. */
@Entity
@Table(name = "content_document")
@Getter
@Setter
public class ContentDocument {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "collection_id", nullable = false)
    private ContentCollection collection;

    @Column(nullable = false, length = 64, unique = true)
    private String code;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private int priority = 0;

    @Column(length = 255)
    private String route;

    @Column(length = 64)
    private String icon;

    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    @Column(name = "image_alt", length = 255)
    private String imageAlt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "attrs", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> attrs = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "channels", columnDefinition = "jsonb", nullable = false)
    private List<String> channels = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "audiences", columnDefinition = "jsonb", nullable = false)
    private List<String> audiences = new ArrayList<>();

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ContentDocumentLocale> locales = new ArrayList<>();

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void addLocale(ContentDocumentLocale locale) {
        locales.add(locale);
        locale.setDocument(this);
    }

    public void clearLocales() {
        locales.forEach(l -> l.setDocument(null));
        locales.clear();
    }
}
