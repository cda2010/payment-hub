package com.gateway.payment.content.api;

import com.gateway.payment.content.api.dto.ContentDtos.ContentCardDto;
import com.gateway.payment.content.service.ContentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Lecture publiée — consommé par le mobile (plugin promo, etc.). */
@RestController
@RequestMapping("/api/v1/content")
public class ContentController {

    private final ContentService service;

    public ContentController(ContentService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ContentCardDto> list(
            @RequestParam String slot,
            @RequestParam(defaultValue = "fr") String locale,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String audience
    ) {
        return service.published(slot, locale, channel, audience);
    }
}
