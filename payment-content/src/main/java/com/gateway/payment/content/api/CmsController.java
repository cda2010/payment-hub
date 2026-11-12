package com.gateway.payment.content.api;

import com.gateway.payment.content.api.dto.CmsDtos.CollectionDto;
import com.gateway.payment.content.api.dto.CmsDtos.PublishedDocumentDto;
import com.gateway.payment.content.service.CmsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Lecture publiée CMS — collections / documents. */
@RestController
@RequestMapping("/api/v1/content")
public class CmsController {

    private final CmsService cms;

    public CmsController(CmsService cms) {
        this.cms = cms;
    }

    @GetMapping("/collections/{code}")
    @PreAuthorize("isAuthenticated()")
    public CollectionDto collection(
            @PathVariable String code,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String audience
    ) {
        return cms.publishedCollection(code, channel, audience);
    }

    @GetMapping("/collections/{code}/documents")
    @PreAuthorize("isAuthenticated()")
    public List<PublishedDocumentDto> documents(
            @PathVariable String code,
            @RequestParam(defaultValue = "fr") String locale,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String audience,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean onDuty,
            @RequestParam(required = false) String duty
    ) {
        return cms.publishedDocuments(code, locale, channel, audience, q, onDuty, duty);
    }

    @GetMapping("/documents/{code}")
    @PreAuthorize("isAuthenticated()")
    public PublishedDocumentDto document(
            @PathVariable String code,
            @RequestParam(defaultValue = "fr") String locale,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String audience
    ) {
        return cms.publishedDocument(code, locale, channel, audience);
    }
}
