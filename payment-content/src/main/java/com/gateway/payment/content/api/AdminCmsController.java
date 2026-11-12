package com.gateway.payment.content.api;

import com.gateway.payment.content.api.dto.CmsDtos.CollectionDto;
import com.gateway.payment.content.api.dto.CmsDtos.CollectionUpsertRequest;
import com.gateway.payment.content.api.dto.CmsDtos.DocumentDto;
import com.gateway.payment.content.api.dto.CmsDtos.DocumentUpsertRequest;
import com.gateway.payment.content.service.CmsService;
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

/** CRUD admin CMS — rubriques & documents. */
@RestController
@RequestMapping("/api/v1/admin/cms")
public class AdminCmsController {

    private final CmsService cms;

    public AdminCmsController(CmsService cms) {
        this.cms = cms;
    }

    @GetMapping("/collections")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','SUPPORT','AUDITOR','OPERATOR')")
    public List<CollectionDto> listCollections() {
        return cms.listCollections();
    }

    @GetMapping("/collections/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','SUPPORT','AUDITOR','OPERATOR')")
    public CollectionDto getCollection(@PathVariable UUID id) {
        return cms.getCollection(id);
    }

    @PostMapping("/collections")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR')")
    public CollectionDto createCollection(@Valid @RequestBody CollectionUpsertRequest body) {
        return cms.createCollection(body);
    }

    @PutMapping("/collections/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR')")
    public CollectionDto updateCollection(@PathVariable UUID id, @Valid @RequestBody CollectionUpsertRequest body) {
        return cms.updateCollection(id, body);
    }

    @DeleteMapping("/collections/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER')")
    public void deleteCollection(@PathVariable UUID id) {
        cms.deleteCollection(id);
    }

    @GetMapping("/documents")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','SUPPORT','AUDITOR','OPERATOR')")
    public List<DocumentDto> listDocuments(@RequestParam String collection) {
        return cms.listDocuments(collection);
    }

    @GetMapping("/documents/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','SUPPORT','AUDITOR','OPERATOR')")
    public DocumentDto getDocument(@PathVariable UUID id) {
        return cms.getDocument(id);
    }

    @PostMapping("/documents")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR')")
    public DocumentDto createDocument(@Valid @RequestBody DocumentUpsertRequest body) {
        return cms.createDocument(body);
    }

    @PutMapping("/documents/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR')")
    public DocumentDto updateDocument(@PathVariable UUID id, @Valid @RequestBody DocumentUpsertRequest body) {
        return cms.updateDocument(id, body);
    }

    @DeleteMapping("/documents/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER')")
    public void deleteDocument(@PathVariable UUID id) {
        cms.deleteDocument(id);
    }
}
