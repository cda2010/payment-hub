package com.gateway.payment.content.api.dto;

import com.gateway.payment.content.domain.ContentItem;
import com.gateway.payment.content.domain.ContentItemLocale;
import com.gateway.payment.content.domain.ContentVisibility;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ContentDtos {

    private ContentDtos() {
    }

    /** Carte publiée (locale déjà résolue). */
    public record ContentCardDto(
            String id,
            String code,
            String slot,
            String eyebrow,
            String title,
            String body,
            String cta,
            String route,
            String pluginId,
            String icon,
            String tone,
            String imageUrl,
            String imageAlt,
            int priority
    ) {
        public static ContentCardDto from(ContentItem item, ContentItemLocale loc) {
            return new ContentCardDto(
                    item.getCode(),
                    item.getCode(),
                    item.getSlot(),
                    loc.getEyebrow(),
                    loc.getTitle(),
                    loc.getBody(),
                    loc.getCta(),
                    item.getRoute(),
                    item.getPluginId(),
                    item.getIcon(),
                    item.getTone(),
                    item.getImageUrl(),
                    item.getImageAlt(),
                    item.getPriority()
            );
        }
    }

    public record LocaleDto(
            @NotBlank String locale,
            String eyebrow,
            @NotBlank String title,
            String body,
            String cta
    ) {
        public static LocaleDto from(ContentItemLocale loc) {
            return new LocaleDto(loc.getLocale(), loc.getEyebrow(), loc.getTitle(), loc.getBody(), loc.getCta());
        }
    }

    public record ContentItemDto(
            UUID id,
            String code,
            String slot,
            boolean enabled,
            int priority,
            String pluginId,
            String route,
            String icon,
            String tone,
            String imageUrl,
            String imageAlt,
            Instant startsAt,
            Instant endsAt,
            List<String> channels,
            List<String> audiences,
            List<LocaleDto> locales,
            Instant createdAt,
            Instant updatedAt
    ) {
        public static ContentItemDto from(ContentItem item) {
            return new ContentItemDto(
                    item.getId(),
                    item.getCode(),
                    item.getSlot(),
                    item.isEnabled(),
                    item.getPriority(),
                    item.getPluginId(),
                    item.getRoute(),
                    item.getIcon(),
                    item.getTone(),
                    item.getImageUrl(),
                    item.getImageAlt(),
                    item.getStartsAt(),
                    item.getEndsAt(),
                    item.getChannels() == null ? List.of() : List.copyOf(item.getChannels()),
                    item.getAudiences() == null ? List.of() : List.copyOf(item.getAudiences()),
                    item.getLocales().stream().map(LocaleDto::from).toList(),
                    item.getCreatedAt(),
                    item.getUpdatedAt()
            );
        }
    }

    public record ContentUpsertRequest(
            @NotBlank String code,
            @NotBlank String slot,
            @NotNull Boolean enabled,
            @NotNull Integer priority,
            String pluginId,
            String route,
            String icon,
            String tone,
            String imageUrl,
            String imageAlt,
            Instant startsAt,
            Instant endsAt,
            List<String> channels,
            List<String> audiences,
            @NotEmpty @Valid List<LocaleDto> locales
    ) {
        public List<String> normalizedChannels() {
            return ContentVisibility.normalizeChannels(channels);
        }

        public List<String> normalizedAudiences() {
            return ContentVisibility.normalizeAudiences(audiences);
        }
    }
}
