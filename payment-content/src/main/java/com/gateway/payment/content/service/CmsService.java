package com.gateway.payment.content.service;

import com.gateway.payment.content.api.dto.CmsDtos.CollectionDto;
import com.gateway.payment.content.api.dto.CmsDtos.CollectionLocaleDto;
import com.gateway.payment.content.api.dto.CmsDtos.CollectionUpsertRequest;
import com.gateway.payment.content.api.dto.CmsDtos.DocumentDto;
import com.gateway.payment.content.api.dto.CmsDtos.DocumentLocaleDto;
import com.gateway.payment.content.api.dto.CmsDtos.DocumentUpsertRequest;
import com.gateway.payment.content.api.dto.CmsDtos.PublishedDocumentDto;
import com.gateway.payment.content.domain.ContentCollection;
import com.gateway.payment.content.domain.ContentCollectionLocale;
import com.gateway.payment.content.domain.ContentDocument;
import com.gateway.payment.content.domain.ContentDocumentLocale;
import com.gateway.payment.content.domain.ContentVisibility;
import com.gateway.payment.content.repository.ContentCollectionRepository;
import com.gateway.payment.content.repository.ContentDocumentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class CmsService {

    private final ContentCollectionRepository collections;
    private final ContentDocumentRepository documents;

    public CmsService(ContentCollectionRepository collections, ContentDocumentRepository documents) {
        this.collections = collections;
        this.documents = documents;
    }

    /* ---------- Admin collections ---------- */

    @Transactional(readOnly = true)
    public List<CollectionDto> listCollections() {
        return collections.findAllWithLocales().stream().map(CollectionDto::from).toList();
    }

    @Transactional(readOnly = true)
    public CollectionDto getCollection(UUID id) {
        return CollectionDto.from(requireCollection(id));
    }

    @Transactional
    public CollectionDto createCollection(CollectionUpsertRequest req) {
        if (collections.existsByCode(req.code().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Code rubrique déjà utilisé : " + req.code());
        }
        ContentCollection c = new ContentCollection();
        applyCollection(c, req);
        return CollectionDto.from(collections.save(c));
    }

    @Transactional
    public CollectionDto updateCollection(UUID id, CollectionUpsertRequest req) {
        ContentCollection c = requireCollection(id);
        if (!c.getCode().equals(req.code().trim()) && collections.existsByCode(req.code().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Code rubrique déjà utilisé : " + req.code());
        }
        applyCollection(c, req);
        return CollectionDto.from(collections.save(c));
    }

    @Transactional
    public void deleteCollection(UUID id) {
        ContentCollection c = collections.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rubrique introuvable"));
        collections.delete(c);
    }

    /* ---------- Admin documents ---------- */

    @Transactional(readOnly = true)
    public List<DocumentDto> listDocuments(String collectionCode) {
        return documents.findAllByCollectionCode(collectionCode.trim()).stream()
                .map(DocumentDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentDto getDocument(UUID id) {
        return DocumentDto.from(requireDocument(id));
    }

    @Transactional
    public DocumentDto createDocument(DocumentUpsertRequest req) {
        if (documents.existsByCode(req.code().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Code document déjà utilisé : " + req.code());
        }
        ContentCollection collection = collections.findByCode(req.collectionCode().trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Rubrique introuvable : " + req.collectionCode()));
        ContentDocument d = new ContentDocument();
        d.setCollection(collection);
        applyDocument(d, req);
        return DocumentDto.from(documents.save(d));
    }

    @Transactional
    public DocumentDto updateDocument(UUID id, DocumentUpsertRequest req) {
        ContentDocument d = requireDocument(id);
        if (!d.getCode().equals(req.code().trim()) && documents.existsByCode(req.code().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Code document déjà utilisé : " + req.code());
        }
        if (!d.getCollection().getCode().equals(req.collectionCode().trim())) {
            ContentCollection collection = collections.findByCode(req.collectionCode().trim())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Rubrique introuvable : " + req.collectionCode()));
            d.setCollection(collection);
        }
        applyDocument(d, req);
        return DocumentDto.from(documents.save(d));
    }

    @Transactional
    public void deleteDocument(UUID id) {
        ContentDocument d = documents.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document introuvable"));
        documents.delete(d);
    }

    /* ---------- Public ---------- */

    @Transactional(readOnly = true)
    public CollectionDto publishedCollection(String code, String channel, String audience) {
        ContentCollection c = collections.findByCode(code.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rubrique introuvable"));
        if (!c.isEnabled()
                || !ContentVisibility.matches(c.getChannels(), channel)
                || !ContentVisibility.matches(c.getAudiences(), audience)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rubrique introuvable");
        }
        return CollectionDto.from(c);
    }

    @Transactional(readOnly = true)
    public List<PublishedDocumentDto> publishedDocuments(
            String collectionCode,
            String locale,
            String channel,
            String audience,
            String q,
            Boolean onDuty,
            String duty
    ) {
        String lang = normalizeLocale(locale);
        String query = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);
        return documents.findPublishedByCollection(collectionCode.trim(), Instant.now()).stream()
                .filter(d -> ContentVisibility.matches(effectiveChannels(d), channel))
                .filter(d -> ContentVisibility.matches(effectiveAudiences(d), audience))
                .filter(d -> onDuty == null || onDuty.equals(asBool(d.getAttrs().get("onDuty"))))
                .filter(d -> duty == null || duty.isBlank()
                        || duty.equalsIgnoreCase(asString(d.getAttrs().get("duty"))))
                .filter(d -> query.isEmpty() || matchesQuery(d, lang, query))
                .map(d -> toPublished(d, lang))
                .flatMap(java.util.Optional::stream)
                .toList();
    }

    @Transactional(readOnly = true)
    public PublishedDocumentDto publishedDocument(String code, String locale, String channel, String audience) {
        ContentDocument d = documents.findByCode(code.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document introuvable"));
        Instant now = Instant.now();
        if (!d.isEnabled()
                || (d.getStartsAt() != null && d.getStartsAt().isAfter(now))
                || (d.getEndsAt() != null && d.getEndsAt().isBefore(now))
                || !ContentVisibility.matches(effectiveChannels(d), channel)
                || !ContentVisibility.matches(effectiveAudiences(d), audience)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document introuvable");
        }
        return toPublished(d, normalizeLocale(locale))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document introuvable"));
    }

    /* ---------- Helpers ---------- */

    private ContentCollection requireCollection(UUID id) {
        return collections.findByIdWithLocales(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rubrique introuvable"));
    }

    private ContentDocument requireDocument(UUID id) {
        return documents.findByIdWithLocales(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document introuvable"));
    }

    private void applyCollection(ContentCollection c, CollectionUpsertRequest req) {
        c.setCode(req.code().trim());
        c.setKind(req.kind().trim().toUpperCase(Locale.ROOT));
        c.setEnabled(Boolean.TRUE.equals(req.enabled()));
        c.setPriority(req.priority() == null ? 0 : req.priority());
        c.setIcon(blankToNull(req.icon()));
        c.setChannels(new ArrayList<>(req.normalizedChannels()));
        c.setAudiences(new ArrayList<>(req.normalizedAudiences()));
        c.setSchemaJson(new HashMap<>(req.safeSchema()));
        c.clearLocales();
        collections.flush();
        for (CollectionLocaleDto loc : req.locales()) {
            ContentCollectionLocale row = new ContentCollectionLocale();
            row.setLocale(normalizeLocale(loc.locale()));
            row.setTitle(loc.title());
            row.setDescription(loc.description());
            c.addLocale(row);
        }
    }

    private void applyDocument(ContentDocument d, DocumentUpsertRequest req) {
        d.setCode(req.code().trim());
        d.setEnabled(Boolean.TRUE.equals(req.enabled()));
        d.setPriority(req.priority() == null ? 0 : req.priority());
        d.setRoute(blankToNull(req.route()));
        d.setIcon(blankToNull(req.icon()));
        d.setImageUrl(blankToNull(req.imageUrl()));
        d.setImageAlt(blankToNull(req.imageAlt()));
        d.setAttrs(new HashMap<>(req.safeAttrs()));
        d.setChannels(new ArrayList<>(req.normalizedChannels()));
        d.setAudiences(new ArrayList<>(req.normalizedAudiences()));
        d.setStartsAt(req.startsAt());
        d.setEndsAt(req.endsAt());
        d.clearLocales();
        documents.flush();
        for (DocumentLocaleDto loc : req.locales()) {
            ContentDocumentLocale row = new ContentDocumentLocale();
            row.setLocale(normalizeLocale(loc.locale()));
            row.setTitle(loc.title());
            row.setSummary(loc.summary());
            row.setBody(loc.body());
            d.addLocale(row);
        }
    }

    private static List<String> effectiveChannels(ContentDocument d) {
        if (d.getChannels() != null && !d.getChannels().isEmpty()) {
            return d.getChannels();
        }
        return d.getCollection() != null ? d.getCollection().getChannels() : List.of();
    }

    private static List<String> effectiveAudiences(ContentDocument d) {
        if (d.getAudiences() != null && !d.getAudiences().isEmpty()) {
            return d.getAudiences();
        }
        return d.getCollection() != null ? d.getCollection().getAudiences() : List.of();
    }

    private static java.util.Optional<PublishedDocumentDto> toPublished(ContentDocument d, String lang) {
        ContentDocumentLocale match = d.getLocales().stream()
                .filter(l -> lang.equalsIgnoreCase(l.getLocale()))
                .findFirst()
                .or(() -> d.getLocales().stream().filter(l -> "fr".equalsIgnoreCase(l.getLocale())).findFirst())
                .or(() -> d.getLocales().stream().findFirst())
                .orElse(null);
        if (match == null) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(PublishedDocumentDto.from(d, match));
    }

    private static boolean matchesQuery(ContentDocument d, String lang, String query) {
        String title = d.getLocales().stream()
                .filter(l -> lang.equalsIgnoreCase(l.getLocale()))
                .map(ContentDocumentLocale::getTitle)
                .findFirst()
                .orElseGet(() -> d.getLocales().isEmpty() ? "" : d.getLocales().get(0).getTitle());
        Map<String, Object> attrs = d.getAttrs() == null ? Map.of() : d.getAttrs();
        return contains(title, query)
                || contains(asString(attrs.get("city")), query)
                || contains(asString(attrs.get("district")), query)
                || contains(asString(attrs.get("address")), query)
                || contains(d.getCode(), query);
    }

    private static boolean contains(String value, String query) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(query);
    }

    private static Boolean asBool(Object raw) {
        if (raw instanceof Boolean b) {
            return b;
        }
        if (raw == null) {
            return null;
        }
        return Boolean.parseBoolean(String.valueOf(raw));
    }

    private static String asString(Object raw) {
        return raw == null ? null : String.valueOf(raw);
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
