package com.gateway.payment.content.api;

import com.gateway.payment.content.api.dto.ContentDtos.ContentItemDto;
import com.gateway.payment.content.api.dto.ContentDtos.ContentUpsertRequest;
import com.gateway.payment.content.service.ContentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** CRUD admin — payment-front / ops. */
@RestController
@RequestMapping("/api/v1/admin/content")
public class AdminContentController {

    private final ContentService service;

    public AdminContentController(ContentService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','SUPPORT','AUDITOR','OPERATOR')")
    public List<ContentItemDto> list(@RequestParam(required = false) String slot) {
        return service.listAdmin(slot);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','SUPPORT','AUDITOR','OPERATOR')")
    public ContentItemDto get(@PathVariable UUID id) {
        return service.getAdmin(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR')")
    public ContentItemDto create(@Valid @RequestBody ContentUpsertRequest body) {
        return service.create(body);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR')")
    public ContentItemDto update(@PathVariable UUID id, @Valid @RequestBody ContentUpsertRequest body) {
        return service.update(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER')")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
