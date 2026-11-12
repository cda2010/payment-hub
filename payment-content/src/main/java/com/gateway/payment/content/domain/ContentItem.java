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
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "content_item")
@Getter
@Setter
public class ContentItem {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 64, unique = true)
    private String code;

    /** Ex. {@code home.promo}, {@code home.todo}. */
    @Column(nullable = false, length = 64)
    private String slot;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private int priority = 0;

    @Column(name = "plugin_id", length = 64)
    private String pluginId;

    @Column(length = 255)
    private String route;

    @Column(length = 64)
    private String icon;

    @Column(length = 32)
    private String tone;

    /** URL absolue (upload local ou CDN externe). */
    @Column(name = "image_url", length = 1024)
    private String imageUrl;

    @Column(name = "image_alt", length = 255)
    private String imageAlt;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    /**
     * Canaux d'accès ({@code MOBILE}, {@code WEB}, …). Liste vide = tous.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "channels", columnDefinition = "jsonb", nullable = false)
    private List<String> channels = new ArrayList<>();

    /**
     * Audiences ({@code RETAIL}, {@code CORPORATE}, …). Liste vide = toutes.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "audiences", columnDefinition = "jsonb", nullable = false)
    private List<String> audiences = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ContentItemLocale> locales = new ArrayList<>();

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

    public void addLocale(ContentItemLocale locale) {
        locales.add(locale);
        locale.setItem(this);
    }

    public void clearLocales() {
        locales.forEach(l -> l.setItem(null));
        locales.clear();
    }
}
