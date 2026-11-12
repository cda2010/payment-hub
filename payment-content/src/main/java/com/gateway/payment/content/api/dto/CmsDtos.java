package com.gateway.payment.content.api.dto;

import com.gateway.payment.content.domain.ContentCollection;
import com.gateway.payment.content.domain.ContentCollectionLocale;
import com.gateway.payment.content.domain.ContentDocument;
import com.gateway.payment.content.domain.ContentDocumentLocale;
import com.gateway.payment.content.domain.ContentVisibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CmsDtos {

    private CmsDtos() {
    }

    public record CollectionLocaleDto(
            @NotBlank String locale,
            @NotBlank String title,
            String description
    ) {
        public static CollectionLocaleDto from(ContentCollectionLocale loc) {
            return new CollectionLocaleDto(loc.getLocale(), loc.getTitle(), loc.getDescription());
        }
    }

    public record DocumentLocaleDto(
            @NotBlank String locale,
            @NotBlank String title,
            String summary,
            String body
    ) {
        public static DocumentLocaleDto from(ContentDocumentLocale loc) {
            return new DocumentLocaleDto(loc.getLocale(), loc.getTitle(), loc.getSummary(), loc.getBody());
        }
    }

    public record CollectionDto(
            UUID id,
            String code,
            String kind,
            boolean enabled,
            int priority,
            String icon,
            List<String> channels,
            List<String> audiences,
            Map<String, Object> schema,
            List<CollectionLocaleDto> locales,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static CollectionDto from(ContentCollection c) {
            return new CollectionDto(
                    c.getId(),
                    c.getCode(),
                    c.getKind(),
                    c.isEnabled(),
                    c.getPriority(),
                    c.getIcon(),
                    c.getChannels() == null ? List.of() : List.copyOf(c.getChannels()),
                    c.getAudiences() == null ? List.of() : List.copyOf(c.getAudiences()),
                    c.getSchemaJson() == null ? Map.of() : Map.copyOf(c.getSchemaJson()),
                    c.getLocales().stream().map(CollectionLocaleDto::from).toList(),
                    c.getCreatedAt(),
                    c.getUpdatedAt()
            );
        }
    }

    public record DocumentDto(
            UUID id,
            String code,
            String collectionCode,
            boolean enabled,
            int priority,
            String route,
            String icon,
            String imageUrl,
            String imageAlt,
            Map<String, Object> attrs,
            List<String> channels,
            List<String> audiences,
            Instant startsAt,
            Instant endsAt,
            List<DocumentLocaleDto> locales,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static DocumentDto from(ContentDocument d) {
            return new DocumentDto(
                    d.getId(),
                    d.getCode(),
                    d.getCollection() != null ? d.getCollection().getCode() : null,
                    d.isEnabled(),
                    d.getPriority(),
                    d.getRoute(),
                    d.getIcon(),
                    d.getImageUrl(),
                    d.getImageAlt(),
                    d.getAttrs() == null ? Map.of() : Map.copyOf(d.getAttrs()),
                    d.getChannels() == null ? List.of() : List.copyOf(d.getChannels()),
                    d.getAudiences() == null ? List.of() : List.copyOf(d.getAudiences()),
                    d.getStartsAt(),
                    d.getEndsAt(),
                    d.getLocales().stream().map(DocumentLocaleDto::from).toList(),
                    d.getCreatedAt(),
                    d.getUpdatedAt()
            );
        }
    }

    /** Carte publiée (locale résolue + attrs). */
    public record PublishedDocumentDto(
            String id,
            String code,
            String collectionCode,
            String title,
            String summary,
            String body,
            String route,
            String icon,
            String imageUrl,
            String imageAlt,
            int priority,
            Map<String, Object> attrs
    ) {
        public static PublishedDocumentDto from(ContentDocument d, ContentDocumentLocale loc) {
            return new PublishedDocumentDto(
                    d.getCode(),
                    d.getCode(),
                    d.getCollection() != null ? d.getCollection().getCode() : null,
                    loc.getTitle(),
                    loc.getSummary(),
                    loc.getBody(),
                    d.getRoute(),
                    d.getIcon(),
                    d.getImageUrl(),
                    d.getImageAlt(),
                    d.getPriority(),
                    d.getAttrs() == null ? Map.of() : Map.copyOf(d.getAttrs())
            );
        }
    }

    public record CollectionUpsertRequest(
            @NotBlank String code,
            @NotBlank String kind,
            @NotNull Boolean enabled,
            @NotNull Integer priority,
            String icon,
            List<String> channels,
            List<String> audiences,
            Map<String, Object> schema,
            @NotEmpty @Valid List<CollectionLocaleDto> locales
    ) {
        public List<String> normalizedChannels() {
            return ContentVisibility.normalizeChannels(channels);
        }

        public List<String> normalizedAudiences() {
            return ContentVisibility.normalizeAudiences(audiences);
        }

        public Map<String, Object> safeSchema() {
            return schema == null ? Map.of() : new HashMap<>(schema);
        }
    }

    public record DocumentUpsertRequest(
            @NotBlank String code,
            @NotBlank String collectionCode,
            @NotNull Boolean enabled,
            @NotNull Integer priority,
            String route,
            String icon,
            String imageUrl,
            String imageAlt,
            Map<String, Object> attrs,
            List<String> channels,
            List<String> audiences,
            Instant startsAt,
            Instant endsAt,
            @NotEmpty @Valid List<DocumentLocaleDto> locales
    ) {
        public List<String> normalizedChannels() {
            return ContentVisibility.normalizeChannels(channels);
        }

        public List<String> normalizedAudiences() {
            return ContentVisibility.normalizeAudiences(audiences);
        }

        public Map<String, Object> safeAttrs() {
            return attrs == null ? Map.of() : new HashMap<>(attrs);
        }
    }
}
