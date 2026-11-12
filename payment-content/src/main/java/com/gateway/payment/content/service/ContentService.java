package com.gateway.payment.content.service;

import com.gateway.payment.content.api.dto.ContentDtos.ContentCardDto;
import com.gateway.payment.content.api.dto.ContentDtos.ContentItemDto;
import com.gateway.payment.content.api.dto.ContentDtos.ContentUpsertRequest;
import com.gateway.payment.content.api.dto.ContentDtos.LocaleDto;
import com.gateway.payment.content.domain.ContentItem;
import com.gateway.payment.content.domain.ContentItemLocale;
import com.gateway.payment.content.domain.ContentVisibility;
import com.gateway.payment.content.repository.ContentItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ContentService {

    private final ContentItemRepository repository;
    private final PublishedContentCache publishedCache;

    public ContentService(ContentItemRepository repository, PublishedContentCache publishedCache) {
        this.repository = repository;
        this.publishedCache = publishedCache;
    }

    @Transactional(readOnly = true)
    public List<ContentCardDto> published(String slot, String locale, String channel, String audience) {
        String lang = normalizeLocale(locale);
        String slotKey = slot.trim();
        String ch = blankToNull(channel);
        String aud = blankToNull(audience);
        return publishedCache.getOrLoad(slotKey, lang, ch, aud, () -> loadPublished(slotKey, lang, ch, aud));
    }

    private List<ContentCardDto> loadPublished(String slot, String lang, String channel, String audience) {
        return repository.findPublished(slot, Instant.now()).stream()
                .filter(item -> ContentVisibility.matches(item.getChannels(), channel))
                .filter(item -> ContentVisibility.matches(item.getAudiences(), audience))
                .map(item -> toCard(item, lang))
                .flatMap(java.util.Optional::stream)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ContentItemDto> listAdmin(String slot) {
        return repository.findAllForAdmin(blankToNull(slot)).stream()
                .map(ContentItemDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContentItemDto getAdmin(UUID id) {
        return ContentItemDto.from(require(id));
    }

    @Transactional
    public ContentItemDto create(ContentUpsertRequest req) {
        if (repository.existsByCode(req.code())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Code déjà utilisé : " + req.code());
        }
        ContentItem item = new ContentItem();
        apply(item, req);
        ContentItem saved = repository.save(item);
        publishedCache.evictSlot(saved.getSlot());
        return ContentItemDto.from(saved);
    }

    @Transactional
    public ContentItemDto update(UUID id, ContentUpsertRequest req) {
        ContentItem item = require(id);
        String previousSlot = item.getSlot();
        if (!item.getCode().equals(req.code()) && repository.existsByCode(req.code())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Code déjà utilisé : " + req.code());
        }
        apply(item, req);
        ContentItem saved = repository.save(item);
        publishedCache.evictSlot(previousSlot);
        if (!previousSlot.equals(saved.getSlot())) {
            publishedCache.evictSlot(saved.getSlot());
        }
        return ContentItemDto.from(saved);
    }

    @Transactional
    public void delete(UUID id) {
        ContentItem item = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contenu introuvable"));
        String slot = item.getSlot();
        repository.delete(item);
        publishedCache.evictSlot(slot);
    }

    private ContentItem require(UUID id) {
        return repository.findByIdWithLocales(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contenu introuvable"));
    }

    private void apply(ContentItem item, ContentUpsertRequest req) {
        item.setCode(req.code().trim());
        item.setSlot(req.slot().trim());
        item.setEnabled(Boolean.TRUE.equals(req.enabled()));
        item.setPriority(req.priority() == null ? 0 : req.priority());
        item.setPluginId(blankToNull(req.pluginId()));
        item.setRoute(blankToNull(req.route()));
        item.setIcon(blankToNull(req.icon()));
        item.setTone(blankToNull(req.tone()));
        item.setImageUrl(blankToNull(req.imageUrl()));
        item.setImageAlt(blankToNull(req.imageAlt()));
        item.setStartsAt(req.startsAt());
        item.setEndsAt(req.endsAt());
        item.setChannels(new ArrayList<>(req.normalizedChannels()));
        item.setAudiences(new ArrayList<>(req.normalizedAudiences()));
        // Flush avant réinsertion : sinon uq_content_item_locale (delete + insert même flush).
        item.clearLocales();
        repository.flush();
        for (LocaleDto loc : req.locales()) {
            ContentItemLocale row = new ContentItemLocale();
            row.setLocale(normalizeLocale(loc.locale()));
            row.setEyebrow(loc.eyebrow());
            row.setTitle(loc.title());
            row.setBody(loc.body());
            row.setCta(loc.cta());
            item.addLocale(row);
        }
    }

    private static java.util.Optional<ContentCardDto> toCard(ContentItem item, String locale) {
        ContentItemLocale match = item.getLocales().stream()
                .filter(l -> locale.equalsIgnoreCase(l.getLocale()))
                .findFirst()
                .or(() -> item.getLocales().stream()
                        .filter(l -> "fr".equalsIgnoreCase(l.getLocale()))
                        .findFirst())
                .or(() -> item.getLocales().stream().findFirst())
                .orElse(null);
        if (match == null) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(ContentCardDto.from(item, match));
    }

    private static String normalizeLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return "fr";
        }
        return locale.trim().toLowerCase(Locale.ROOT).split("[-_]")[0];
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
