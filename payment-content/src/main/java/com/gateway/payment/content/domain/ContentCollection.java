package com.gateway.payment.content.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
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

/** Rubrique CMS (directory, news, promo…). */
@Entity
@Table(name = "content_collection")
@Getter
@Setter
public class ContentCollection {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 64, unique = true)
    private String code;

    /** DIRECTORY | NEWS | PROMO | DOCS */
    @Column(nullable = false, length = 32)
    private String kind = "DIRECTORY";

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private int priority = 0;

    @Column(length = 64)
    private String icon;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "channels", columnDefinition = "jsonb", nullable = false)
    private List<String> channels = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "audiences", columnDefinition = "jsonb", nullable = false)
    private List<String> audiences = new ArrayList<>();

    /** Description des champs attrs des documents enfants. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "schema_json", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> schemaJson = new HashMap<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ContentCollectionLocale> locales = new ArrayList<>();

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

    public void addLocale(ContentCollectionLocale locale) {
        locales.add(locale);
        locale.setCollection(this);
    }

    public void clearLocales() {
        locales.forEach(l -> l.setCollection(null));
        locales.clear();
    }
}
